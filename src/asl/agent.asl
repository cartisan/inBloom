/********************************************/
/***** General knowledge  *******************/
/*****      Common sense beliefs ************/
/********************************************/

is_work(plant(_)).
is_work(tend(_)).
is_work(harvest(_)).
is_work(grind(_)).
is_work(bake(_)).

//is_work(create(bread)).

creatable_from(wheat,bread).

is_pleasant(eat(bread)).

obligation(farm_work).
wish(relax).

is_useful(A,true) :- is_pleasant(eat(A)).

!default_activity.

/********************************************/
/*****      Common sense reasoning ************/
/********************************************/

// follow work-intensive wishes when somewhat active
@wish_1[affect(not(mood(arousal,low)))]
+wish(X) : is_work(X) <-
	!!X.

+wish(X)  <-
	!!X.

// Share when in a good mood, and not "misanthrophic"
//@share_food_plan[atomic, affect(and(mood(pleasure,high),not(personality(agreeableness,low))))]
//+has(X) : is_pleasant(eat(X)) & has(X) <- 			// still has X when event selected
//	?agents(Anims);
//	!share(X, Anims);
//	.print("Shared: ", X, " with the others");
//	!eat(X).

// Share when very agreeable character, unless in a bad mood
@share_food_plan2[atomic, affect(and(personality(agreeableness,high), not(mood(pleasure,low))))]
+has(X) : is_pleasant(eat(X)) & has(X) <- 			// still has X when event selected
	-wish(create(X));
	?agents(Anims);
	!share(X, Anims);
	.print("Shared: ", X, " with the others");
	!eat(X).

+has(X) : is_pleasant(eat(X)) & has(X)  <-			// still has X when event selected 
	-wish(create(X));
	!eat(X).
	
//+has(X) : has(X) <-
+found(X) : has(X) <-
	-wish(create(X));
	?creatable_from(X,Y);
	?is_useful(Y,Z)
	if(Z) {
		+obligation(create(Y));
		+wish(create(Y));
	}.

+self(has_purpose) <-
	.suspend(default_activity).

-self(has_purpose) <-
	.resume(default_activity).


/********************************************/
/***** Self-specifications  *****************/
/*****      Emotion management **************/
/********************************************/

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


/********************************************/
/*****      Personality *********************/
/********************************************/

// Ask for help if extraverted, unless one feels powerless
@general_help_acquisition_plan[affect(and(personality(extraversion,positive),not(mood(dominance,low))))]
+!X[_] : is_work(X) & not already_asked(X) <-
	?agents(Animals);
	+already_asked(X);
	for (.member(Animal, Animals)) {
		.print("Asking ", Animal, " to help with ", X)
		.send(Animal, achieve, help_with(X));
		+asking(help_with(X), Animal);
	}
	.suspend(X);
	!X.

// Always follow obligations if high on consc, and feels like being active
//@default_activity_1[affect(and(personality(conscientiousness,high), mood(arousal,positive)))]
//+!default_activity <-
//	?obligation(X);
//	!X;
//	!default_activity.
@default_activity_1[affect(and(personality(conscientiousness,high)))]
+!default_activity <-
	?obligation(X);
	!X;
	!default_activity.

// Don't follow obligations if feeling passive, or very "anti-social" tendencies
@default_activity_2[affect(or(personality(conscientiousness,low), mood(arousal,negative)))]
+!default_activity <-
	?wish(X);
	!X;
	!default_activity.

// If not high on consc, but feels active: randomly choose between desires and wishes
@default_activity_3
+!default_activity <-
	.random(R);
//	if(R>0.5) {
		?wish(X);
//	} else {
//		?obligation(X);
//	}
	!X;
	!default_activity.	

/********************************************/
/****** Mood  *******************************/
/********************************************/
+mood(hostile) <-
	!punish.

// begin declarative goal  (p. 174; Bordini,2007)*/
+!punish : punished(L) <- true.

// insert all actual punishment plans
@punished_plan_1[atomic]	
+!punish : mood(hostile) & has(X) & is_pleasant(eat(X)) <-
	?affect_target(Anims);
	if (.empty(Anims)) {
		!eat(X);
		+punished(Anims);		
	} else {
		.send(Anims, achieve, eat(X));
		.print("Asked ", Anims, " to eat ", X, ". But not shareing necessary ressources. xoxo");
		!eat(X);
		+punished(Anims)
	}.
	
// blind commitment: if no means to punish present now, keep trying
+!punish : true <- 
	!punish.

// relativised commitment: finish desire if not in hostile mood anymore, or punishment done
-mood(hostile) <-
	.succeed_goal(punish).

+punished(L) : true <- 
	-punished(L);
	.drop_intention(punished(_)).
	
/********************************************/
/***** Plans  *******************************/
/********************************************/

//+!create(bread) : has(wheat) <-
//	+self(has_purpose);
//	!plant(wheat);
//	!tend(wheat);
//	!harvest(wheat);
//	!grind(wheat);
//	!bake(bread);
//	-self(has_purpose).

+!create(bread) : has(wheat) & state(wheat(seed))<-
	+self(has_purpose);
	!plant(wheat);
	!create(bread).

+!create(bread) : state(wheat(growing))<-
	!tend(wheat);
	!create(bread).

+!create(bread) : state(wheat(ripe))<-
	!harvest(wheat);
	!create(bread).

+!create(bread) : state(wheat(harvested))<-
	!grind(wheat);
	!create(bread).

+!create(bread) : state(wheat(flour))<-
	!bake(bread);
	-wish(create(bread));
	-self(has_purpose).

// Reject helping others if "antisocial", but not feeling powerless
@reject_request_1[affect(and(personality(conscientiousness,low), not(mood(dominance,low))))]
+!help_with(X)[source(Name)] : is_work(X) <-
	.print("can't help you! ", X, " is too much work for me!");
	.send(Name, tell, rejected_request(help_with(X))).

// Reject helping others if "anti-social tendencies" and feeling strong
@reject_request_2[affect(and(personality(conscientiousness,negative), mood(dominance,high)))]
+!help_with(X)[source(Name)] : is_work(X) <-
	.print("can't help you! ", X, " is too much work for me!");
	.send(Name, tell, rejected_request(help_with(X))).

@accept_request
+!help_with(X)[source(Name)] <-
	.print("I'll help you with ", X, ", ", Name);
	.send(Name, tell, accepted_request(help_with(X)));
	help(Name).
	

/********************************************/
/*****      Action Execution Goals **********/
/********************************************/
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
+!eat(X) : has(X) <- 
	eat(X);
	-has(X);
	.succeed_goal(eat(X)).

+!share(X, Anims) <-
	share(X, Anims).	