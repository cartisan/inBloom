/******************************************************************************/
/************ knowledge base  *************************************************/
/******************************************************************************/

{include("agent-knowledge_base.asl")}
	
/********************************************/
/*****     wishes and obligations ***********/
/********************************************/

{include("agent-desire_wish_management.asl")}

wish(relax).
+self(farm_animal) <- +obligation(farm_work).

+hungry <- 
	.appraise_emotion(distress, "hungry");
	+wish(eat).
-hungry <- 
	-wish(eat);
	-wish(eat(_)).

/******************************************************************************/
/********** perception management *********************************************/
/******************************************************************************/

+has(X) : hungry & edible(X)  <-
	-wish(has(X));
	.appraise_emotion(joy, "has(X)");
	.resume(wish(relax)).

+has(X) : dislike(X) <-
	-wish(has(X));
	.appraise_emotion(hate, "has(X)").

+has(X)  <-
	-wish(has(X));
	.appraise_emotion(love, "has(X)").

//TODO: how to make creatable_from(X,Y) recursive?
+found(Item[Annots]) : creatable_from(Item,Y) & is_useful(Y) <-
	+has(Item[Annots]);
	.appraise_emotion(hope, "found(Item[Annots])");
	.suspend(obligation(farm_work));	// only for brevity of graph
	.suspend(wish(relax));
	+obligation(create(Y)).

/***** action-perception management **********************************************/
/******************************************************************************/

+share(Other, Item, Me)[success(true)] : .my_name(Me) <-
	+received(Item);
	+has(Item).
	
+share(Other, Item, List)[success(true)] : .my_name(Me) & .list(List) & .member(Me,List) <-
	+received(Item);
	+has(Item).

@eatP[atomic]
+eat(Food)[success(true)] <-
	-hungry;
	-has(Food).
		
/***** request answer management **********************************************/
/******************************************************************************/

+rejected_request(help_with(Helpee,Req))[source(Name)] <-
	if (not(mood(hostile))) {
		.appraise_emotion(disappointment, "rejected_request(help_with(Helpee,Req))[source(Name)]", Name);
	} else {
		.appraise_emotion(anger, "rejected_request(help_with(Helpee,Req))[source(Name)]", Name);
	}
	.abolish(rejected_request(help_with(Helpee,Req)));
	-asking(help_with(Req), Name).
	
+accepted_request(help_with(Helpee,Req))[source(Name)] <-
	.appraise_emotion(gratitude, "accepted_request(help_with(Helpee,Req))[source(Name)]", Name);
	.abolish(accepted_request(help_with(Helpee,Req)));
	-asking(help_with(Req), Name).

+!help_with(Helpee, Plan)[source(Other)] <-
	+obligation(help_with(Helpee, Plan)).

@rejectrequest[atomic]
+!reject(Helpee, Plan) <-
	.print("can't help you! request(", Plan, ") is too much work for me!");
	.send(Helpee, tell, rejected_request(help_with(Helpee, Plan)));
	.appraise_emotion(reproach, "tell(rejected_request(help_with(Helpee,Plan)))", Helpee, 2);
	-obligation(help_with(Helpee, Plan)).

@acceptrequest[atomic]
+!accept(Helpee, Plan) : .my_name(Me)  <-
	.print("I'll help you with ", Plan, ", ", Helpee);
	.send(Helpee, tell, accepted_request(help_with(Helpee, Plan)));
	.appraise_emotion(pride, "tell(accepted_request(help_with(Helpee,Plan)))", Helpee, 2);
	help(Helpee,Plan);
	-obligation(help_with(Helpee, Plan)).

/****** Mood  management ******************************************************/
/******************************************************************************/

+mood(hostile) <-
	?affect_target(Anims);
	if (.empty(Anims)) {
		.print("What a foul mood, and no-one to blame for it!");
	} else {
		+wish(punish);
	}.

-mood(hostile) <-
	-wish(punish).
	
+mood(relaxed) <-
	-wish(relax);
	+wish(relief_boredom).
	
+mood(anxious) <-
	+wish(relax).
	
/******************************************************************************/
/***** Plans  *****************************************************************/
/******************************************************************************/

// Ask for help if extraverted, unless one feels powerless
@general_help_acquisition_plan[affect(and(personality(extraversion,positive),not(mood(dominance,low))))]
+!X[_] : is_work(X) & not complex_plan(X) & not already_asked(X) <-
	.my_name(Me);
	?present(Agents);
	+already_asked(X);
	for (.member(Agent, Agents)) {
		.print("Asking ", Agent, " to help with ", X)
		.send(Agent, achieve, help_with(Me,X));
		+asking(help_with(X), Agent);
	}
	.wait(not asking(help_with(X), _), 500);
	!X;
	-already_asked(X).


+!create(bread) : has(wheat[state(seed)]) <-
	!plant(wheat);
	-has(wheat[_]);
	!create(bread).

+!create(bread) : at(wheat[state(growing)], farm) <-
	!tend(wheat);
	!create(bread).

+!create(bread) : at(wheat[state(ripe)], farm) <-
	!harvest(wheat);
	+has(wheat[state(harvested)]); // TODO: get env to give this information?
	!create(bread).
	
+!create(bread) : has(wheat[state(harvested)]) <-
	!grind(wheat);
	-+has(wheat[state(flour)]); // TODO: get env to give this information? 
	!create(bread).
	
+!create(bread) : has(wheat[state(flour)]) <-
	!bake(bread);
	.resume(obligation(farm_work));
	.resume(wish(relax));
	-obligation(create(bread)).

@punish_1[atomic]	
+!punish : mood(hostile) & has(X) & is_pleasant(eat(X)) & hungry <-
//+!punish : mood(hostile) & has(X) & is_pleasant(eat(X)) & not(hungry(false)) <-
	?affect_target(Anims);
	if (.empty(Anims)) {
		true;
	} else {
		.send(Anims, achieve, eat(X));
		.print("Asked ", Anims, " to eat ", X, ". But not shareing necessary ressources. xoxo");
	};
	!eat(X);
	-wish(punish).
	
@punish_2[atomic, affect(personality(neuroticism, high))]	
+!punish : mood(hostile) & has(X) & is_pleasant(eat(X)) & hungry & .my_name(Me)<-
	fret;
	?affect_target(Anims);
	if (.empty(Anims)) {
		true;
	} else {
		.send(Anims, achieve, eat(X));
		.print("Asked ", Anims, " to eat ", X, ". But not shareing necessary ressources. xoxo");
	};
	!eat(X);
	-wish(punish).

//+!share_food(Food, Others) <-
//	!share(Food, Others);
//	!eat(Food);
//	-wish(share_food(X, Anims)).
	
/******************************************************************************/
/*****      Action Execution Goals ********************************************/
/******************************************************************************/
// positive extraversion means that a relax action removes the desire to relax
@relax[affect(personality(extraversion,positive))]
+!relax <-
	relax;
	-wish(relax);
	+wish(relief_boredom).

// while negative extraversion means relaxing remains a desire
+!relax <-
	relax.

+!relief_boredom <-
	sing.
	
+!farm_work <-
	farm_work.

+!plant(wheat) <-
	plant(wheat).
	
+!tend(wheat) <-
	tend(wheat).
	
+!harvest(wheat) <-
	harvest(wheat).

+!grind(wheat) <-
	grind(wheat).

@bake[atomic]
+!bake(bread) <-
	-has(wheat[_]);
	bake(bread);
	+has(bread).

+!eat : has(Item) & edible(Item) <-
	!eat(Item).

+!eat <-
	.print("Ain't got no food");
	-wish(eat).

@eat2[atomic, affect(and(personality(agreeableness,high), not(mood(pleasure,low))))]
+!eat(Food) : not wish(punish) & not received(Food) <-
	?present(Others);
	!share(Food, Others);
	eat(Food);
	-wish(eat).
	
@eat4[atomic, affect(and(personality(agreeableness,medium), mood(pleasure,high)))]
+!eat(Food) : not wish(punish) & not received(Food) <-
	?present(Others);
	!share(Food, Others);
	eat(Food);
	-wish(eat).

+!eat(Food) <- 
	eat(Food);
	-wish(eat).

+!eat(X) : not has(X)<- 
	.appraise_emotion(disappointment, "eat(X)").

+!share(Item, Agent) <-
	.print("Sharing: ", Item, " with", Agent);
	share(Item, Agent).

+!sing <-
	sing.

{include("agent_folktale_animal_crowfox.asl")}	