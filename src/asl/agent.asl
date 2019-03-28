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

/******************************************************************************/
/********** perception management *********************************************/
/******************************************************************************/

// Share when very agreeable character, unless in a bad mood
@share_food_plan2[atomic, affect(and(personality(agreeableness,high), not(mood(pleasure,low))))]
+has(X) : hungry & is_pleasant(eat(X)) & has(X) <- 			// still has X when event selected
	?agents(Anims);
	+wish(share_food(X, Anims));
	-wish(has(X));
	.resume(wish(relax)).

+has(X) : hungry & is_pleasant(eat(X)) & has(X)  <-			// still has X when event selected 
	+wish(eat(X));
	-wish(has(X));
	.resume(wish(relax)).

+see(Thing)[location(Loc), owner(Per)] : is_useful(Thing) <-   // crowfox
	+at(Per,Loc);
	+has(Per,Thing);
	.my_name(Name);
	.appraise_emotion(hope, Name, "see(Thing)");
	.suspend(wish(relax));
	+wish(has(Thing)).
	
+found(X) <-
	?creatable_from(X,Y);
	if(is_useful(Y)) {
		.suspend(obligation(farm_work));	// only for brevity of graph
		.suspend(wish(relax));
		+obligation(create(Y));
	}.

+is_dropped(Thing)[owner(Person)] : .my_name(Person) <-		//crowfox
	.appraise_emotion(remorse, Person, "is_dropped(Thing)").

@is_dropped[atomic]
+is_dropped(Thing)[owner(Person)] : wish(has(Thing)) <-		//crowfox
	.appraise_emotion(gloating, Person, "is_dropped(Thing)");
	!collect(Thing);
	-wish(has(X));
	.resume(wish(relax)).

@compliment[atomic]
+complimented : .my_name(Person)  <-  		//crowfox
	.appraise_emotion(pride, Person, "complimented");
	!sing.

+threatened(Item)[source(Other)] : .my_name(Me)  <- 				 //crowfox
	.print("Oh no, don't hurt me!");
	.appraise_emotion(fear, Me, "threatened(Item)");
	handOver(Other, Item).
	
@threat_2[affect(personality(conscientiousness,low))]
+threatened(Item)[source(Other)]  : .my_name(Me) <- 				 //crowfox
	.print("No, I will not give you anything!");
	.appraise_emotion(reproach, Other, "threatened(Item)");
	refuseToGive(Other,Item).
	
/***** request answer management **********************************************/
/******************************************************************************/

+request(help_with(Plan))[source(Name)] <-
	+obligation(help_with(Name, Plan)).

+rejected_request(help_with(Req))[source(Name)] <-
	.appraise_emotion(anger, Name, "rejected_request(help_with(Req))[source(Name)]", true);
	.abolish(rejected_request(help_with(Req)));
	-asking(help_with(Req), Name);
	if(not asking(help_with(Req), _)) {
		.resume(Req);
	}.
	
+accepted_request(help_with(Req))[source(Name)] <-
	.appraise_emotion(gratitude, Name, "accepted_request(help_with(Req))[source(Name)]", true);
	.abolish(accepted_help_request(help_with(Req)));
	-asking(help_with(Req), Name);
	if(not asking(help_with(Req), _)) {
		.resume(Req);
	}.

@reject_request[atomic]
+!reject(Req, Name) <-
	.concat("request(", Req, ")", Listen);
	.appraise_emotion(reproach, Name, Listen);
	.print("can't help you! request(", Req, ") is too much work for me!");
	.send(Name, tell, rejected_request(Req));
	-obligation(Req).
	
// TODO: How to turn this into an analogous accept without breaking FU structure?	
@accept_request[atomic]
+!help_with(Name, Plan) <-
	.print("I'll help you with ", Plan, ", ", Name);
	.send(Name, tell, accepted_request(help_with(Plan)));
	help(Name);
	.appraise_emotion(happy_for, Name, "request(help_with(Plan))");
	-obligation(help_with(Name, Plan)).

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
	
/******************************************************************************/
/***** Plans  *****************************************************************/
/******************************************************************************/

// Ask for help if extraverted, unless one feels powerless
@general_help_acquisition_plan[affect(and(personality(extraversion,positive),not(mood(dominance,low))))]
+!X[_] : is_work(X) & not complex_plan(X) & not already_asked(X) <-
	?agents(Animals);
	+already_asked(X);
	for (.member(Animal, Animals)) {
		.print("Asking ", Animal, " to help with ", X)
		.send(Animal, tell, request(help_with(X)));
		+asking(help_with(X), Animal);
	}
	.suspend(X);
	!X.

@create_bread_1[affect(personality(conscientiousness,high))]
+!create(bread) : has(wheat[state(seed)])<-
	!plant(wheat).

@create_bread_2[affect(personality(conscientiousness,high))]
+!create(bread) : existant(wheat[state(growing)])<-
	!tend(wheat).

@create_bread_3[affect(personality(conscientiousness,high))]
+!create(bread) : existant(wheat[state(ripe)])<-
	!harvest(wheat).

@create_bread_4[affect(personality(conscientiousness,high))]
+!create(bread) : has(wheat[state(harvested)])<-
	!grind(wheat).

@create_bread_5[affect(personality(conscientiousness,high))]
+!create(bread) : has(wheat[state(flour)])<-
	!bake(bread);
	.resume(obligation(farm_work));
	.resume(wish(relax));
	-obligation(create(bread)).
	
+!has(Thing) : has(Person, Thing) & at(Person, Loc1) & at(Loc2) & not Loc1==Loc2 <- 	//crowfox
	!approach(Person).

+!has(Thing) : has(Person, Thing) & at(Person, Loc1) & at(Loc2) & Loc1==Loc2 <- 	//crowfox
	!get(Thing, Person).

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

+!share_food(Food, Others) <-
	!share(Food, Others);
	!eat(Food);
	-wish(share_food(X, Anims)).
	
/******************************************************************************/
/*****      Action Execution Goals ********************************************/
/******************************************************************************/
+!relax <-
	relax.
	
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

+!bake(bread) <-
	bake(bread).

@eat_1[atomic]	
+!eat(X) <- 
	eat(X);
	-has(X);
	-wish(eat(X)).
//	.succeed_goal(eat(X)).

+!approach(Person) : agent(Person) & at(Person, Loc) <-  	//crowfox
	goTo(Loc).

+!approach(Loc) : location(Loc) <-  	//crowfox
	goTo(Loc).

@get_flatter[affect(personality(agreeableness,medium))]
+!get(Thing, Person) <-			//crowfox
	.print("So lovely your feathers, so shiny thy beak!");
	.send(Person, tell, complimented);
	.wait({+is_dropped(Thing)}).

@get_2[affect(personality(agreeableness,low))]
+!get(Thing, Person) <-				//crowfox
	.print("Give ", X, " to me or I will take it from you!");
	.send(Person, tell, threatened(Thing));
	.wait({+has(Thing)}).
	
@get_3[affect(personality(agreeableness,high))]
+!get(Thing, Person) <-
	.print("I am so hungry, would you share your ", X," with me please?");
	.my_name(Me);
	.send(Person, achieve, share(Thing, Me));
	.wait({+has(Thing)}).	

+!collect(Thing) <-
	collect(Thing).

+!sing <-
	sing.

@share1[affect(and(personality(agreeableness,high), not(mood(pleasure,low))))]
+!share(X, Anims) <-
	.print("Sharing: ", X, " with", Anims);
	share(X, Anims).

@share2[affect(personality(agreeableness,low))]
+!share(X, Anims) <- 
	.print("I'm not sharing with anyone!");
	true.
