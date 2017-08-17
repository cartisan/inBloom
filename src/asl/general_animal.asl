/********************************************/
/***** General knowledge  *******************/
/*****      Common sense beliefs ************/
/********************************************/

is_work(plant(_)).
is_work(tend(_)).
is_work(harvest(_)).
is_work(grind(_)).
is_work(bake(_)).

is_pleasant(eat(bread)).

animals([dog, cow, pig]).

default_activity(cazzegiare).
default_activity(farm_work).



/********************************************/
/*****      Common sense reasoning ************/
/********************************************/
+has(X) : is_pleasant(eat(X)) <-
	!eat(X).

@share_food_plan[affect(personality(agreeableness,medium), mood(pleasure,positive))]
+has(X) : has(X) & is_pleasant(eat(X)) <-
	.print("Sharing: ", X, " with the others");
	?animals(Anims);
	!share(X, Anims);
	!eat(X).
	
+has(wheat(seed)) <- 
	!!create_bread.

+self(has_purpose) <-
	for (default_activity(X) ) {
		+suspended(X);
		.suspend(X);
	}.

-self(has_purpose) <-
	for (suspended(X) ) {
		.resume(X);
	}.

/********************************************/
/***** Self-specifications  *****************/
/*****      Emotion management **************/
/********************************************/

+rejected_help_request(Req)[source(Name)] <-
	.appraise_emotion(anger);
	.abolish(rejected_help_request(Req));
	-asking(Req, Name);
	if(not asking(Req, _)) {
		.resume(Req);
	}.
	
+accepted_help_request(Req)[source(Name)] <-
	.appraise_emotion(gratitude);
	.abolish(accepted_help_request(Req));
	-asking(Req, Name);
	if(not asking(Req, _)) {
		.resume(Req);
	}.


/********************************************/
/*****      Personality management **********/
/********************************************/

@general_help_acquisition_plan[affect(personality(extraversion, high))]
+!X[_] : is_work(X) & not already_asked(X) <-
	?animals(Animals);
	+already_asked(X);
	for (.member(Animal, Animals)) {
		.print("Asking ", Animal, " to help with ", X)
		.send(Animal, achieve, help_with(X));
		+asking(X, Animal);
	}
	.suspend(X);
	!X.

/********************************************/
/****** Mood  *******************************/
/********************************************/
+mood(hostile) <-
	?animals(Anims);
	// TODO: punish only targets of mood!
	!!punished(Anims).

//	   + relativised commitment
-mood(hostile) <-
	?animals(Anims);
	.succeed_goal(punished(Anims)).

// begin declarative goal  (p. 174; Bordini,2007)*/
+!punished(L) : punished(L) <- true.

// insert all actual punishment plans	
@punished_plan[atomic]	
+!punished(L) : mood(hostile) & has(X) & is_pleasant(eat(X)) <-
	.print("Asking ", L, " to eat ", X, ". But not shareing necessary ressources. xoxo");
	.send(L, achieve, eat(X));
	!eat(X);
	+punished(L).
	
//	   +blind commitment
+!punished(L) : true <- !!punished(L).

+punished(L) : true <- 
	-punished(L);
	.succeed_goal(punished(L)).

/********************************************/
/***** Plans  *******************************/
/********************************************/

+!farm_work <- 
	!random_farming;
	!farm_work.

+!create_bread : has(wheat(seed)) <-
	+self(has_purpose);
	!plant(wheat);
	!tend(wheat);
	!harvest(wheat);
	!grind(wheat);
	!bake(bread);
	-self(has_purpose). 	

// TODO: I belief this is a misuse of tell!
// But how do we otherwise inform of such
// a rejection
@help_with_plan[affect(personality(conscientiousness,low))]
+!help_with(X)[source(Name)] : is_work(X) <-
	.print("can't help you! ", X, " is too much work for me!");
	.send(Name, tell, rejected_help_request(X)).

+!help_with(X)[source(Name)] <-
	.print("I'll help you with ", X, ", ", Name);
	.send(Name, tell, accepted_help_request(X));
	help(Name).
	
+!cazzegiare <-
	.print("I'm doing sweet nothing.");
	!relax;
	!cazzegiare.


/********************************************/
/*****      Action Execution Goals **********/
/********************************************/
+!relax <-
	relax.
	
+!random_farming <-
	random_farming.

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
	
+!eat(X) : has(X) <- 
	eat(X).
	
+!eat(X) <- 
	.print("Can't eat ", X, ", I don't have any! :( ");
	.appraise_emotion(disappointment);
	.suspend(eat(X)).
	
+!share(X, Anims) <-
	share(X, Anims).	