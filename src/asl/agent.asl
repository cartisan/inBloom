/********************************************/
/***** General knowledge  *******************/
/*****      Common sense beliefs ************/
/********************************************/

is_work(farm_work).
is_work(create(bread)).
is_work(plant(_)).
is_work(tend(_)).
is_work(harvest(_)).
is_work(grind(_)).
is_work(bake(_)).

complex_plan(create(bread)).

creatable_from(wheat,bread).
is_pleasant(eat(bread)).
is_useful(A,true) :- is_pleasant(eat(A)).

wish(relax).

agent(X) :- agents(Agents) & .member(X, Agents).
location(X) :- locations(Locations) & .member(X, Locations).

already_asked(farm_work).
	
/********************************************/
/*****     Wishes and Obligation ************/
/********************************************/

+self(farm_animal) <- +obligation(farm_work).

+obligation(Plan) <-
	.print("before !obligation(", Plan, ") initial");
	!obligation(Plan).				// adds desire to fulfill obligation
	
-obligation(Plan) <-				// removes desire to fulfill obligation, and obligation
	.drop_desire(obligation(Plan));
	.succeed_goal(obligation(Plan)).

@obligation1[affect(personality(conscientiousness,high))]
+!obligation(Plan) : is_work(Plan) <-		// only do hard obligations if conscientious
	!Plan; 
	!obligation(Plan).
+!obligation(Plan) : not is_work(Plan) <-
	!Plan; 
	!obligation(Plan).
+!obligation(Plan) : true <-		// universal fallback, obligations remain desires even when unfulfillable atm 
	!obligation(Plan).
	
	
+wish(Plan) <-						// adds desire to fulfill wish
	!wish(Plan).
-wish(Plan) <-						// removes desire to fulfill wish, and wish
	.drop_desire(wish(Plan));
	.succeed_goal(wish(Plan)).
	
@wish1[affect(personality(conscientiousness,high))]
+!wish(Plan) : obligation(Plan2) <-	// if conscientious only do wish when no obligations desired
	!wish(Plan).
+!wish(Plan) <-
	!Plan; 
	!wish(Plan).
+!wish(Plan) : true <- 				// universal fallback, wishes remain desires even when unfulfillable atm
	!wish(Plan).

/********************************************/
/*****      Common sense reasoning ************/
/********************************************/

+see(Thing)[location(Loc), owner(Per)] : is_pleasant(eat(Thing)) & hungry(true) <-   // crowfox
	+at(Per,Loc);
	+has(Per,Thing);
	.my_name(Name);
	.appraise_emotion(hope, Name, "see(Thing)");
	.suspend(wish(relax));
	+wish(has(Thing)).

// Share when very agreeable character, unless in a bad mood
@share_food_plan2[atomic, affect(and(personality(agreeableness,high), not(mood(pleasure,low))))]
+has(X) : is_pleasant(eat(X)) & has(X) & hungry(true) <- 			// still has X when event selected
	?agents(Anims);
	!share(X, Anims);
	.print("Shared: ", X, " with the others");
	!eat(X);
	-wish(has(X));
	.resume(wish(relax)).

+has(X) : is_pleasant(eat(X)) & has(X) & hungry(true)  <-			// still has X when event selected 
	-wish(has(X));
	.resume(wish(relax));
	!eat(X).
	
+found(X) <-
	?creatable_from(X,Y);
	?is_useful(Y,Z)
	if(Z) {
		.suspend(obligation(farm_work));	// only for brevity of graph
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

//-has(X): is_pleasant(eat(X)) <-
//	.appraise_emotion(distress).
	
//+has(X): is_pleasant(eat(X)) & has(X) <-
//	.appraise_emotion(joy).

//+is_dropped(X): at(underTree)  <-
//	.appraise_emotion(joy).

/********************************************/
/*****      Personality *********************/
/********************************************/

// Ask for help if extraverted, unless one feels powerless
@general_help_acquisition_plan[affect(and(personality(extraversion,positive),not(mood(dominance,low))))]
+!X[_] : is_work(X) & not complex_plan(X) & not already_asked(X) <-
	?agents(Animals);
	+already_asked(X);
	for (.member(Animal, Animals)) {
		.print("Asking ", Animal, " to help with ", X)
		.send(Animal, achieve, help_with(X));
		+asking(help_with(X), Animal);
	}
	.suspend(X);
	!X.

/********************************************/
/****** Mood  *******************************/
/********************************************/
+mood(hostile) <-
	?affect_target(Anims);
	if (.empty(Anims)) {
		.print("What a foul mood, and no-one to blame for it!");
	} else {
		!punish;
	}.

// relativised commitment: finish desire if not in hostile mood anymore
-mood(hostile) <-
	.drop_desire(punish).
	
+punished(L) : true <- 
	-punished(L);
	.succeed_goal(punish).

// begin declarative goal  (p. 174; Bordini,2007)*/
+!punish : punished(L) <- true.

// insert all actual punishment plans
@punished_plan_1[atomic]	
+!punish : mood(hostile) & has(X) & is_pleasant(eat(X)) & not(hungry(false))<-
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
	
/********************************************/
/***** Plans  *******************************/
/********************************************/

@create_bread_1[affect(personality(conscientiousness,high))]
+!create(bread) : existant(wheat[state(seed)])<-
	!plant(wheat);
	!create(bread).

@create_bread_2[affect(personality(conscientiousness,high))]
+!create(bread) : existant(wheat[state(growing)])<-
	!tend(wheat);
	!create(bread).

@create_bread_3[affect(personality(conscientiousness,high))]
+!create(bread) : existant(wheat[state(ripe)])<-
	!harvest(wheat);
	!create(bread).

@create_bread_4[affect(personality(conscientiousness,high))]
+!create(bread) : existant(wheat[state(harvested)])<-
	!grind(wheat);
	!create(bread).

@create_bread_5[affect(personality(conscientiousness,high))]
+!create(bread) : existant(wheat[state(flour)])<-
	!bake(bread);
	.resume(obligation(farm_work));
	-obligation(create(bread)).

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
	
+!has(Thing) : has(Person, Thing) & at(Person, Loc1) & at(Loc2) & not Loc1==Loc2 <- 	//crowfox
	!approach(Person).

+!has(Thing) : has(Person, Thing) & at(Person, Loc1) & at(Loc2) & Loc1==Loc2 <- 	//crowfox
	!get(Thing, Person).
	
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
+!eat(X) <- 
	eat(X);
	-has(X);
	.succeed_goal(eat(X)).

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
	share(X, Anims).

@share2[affect(and(personality(agreeableness,low)))]
+!share(X, Anims) <- true.
