/********************************************/
/***** General knowledge  *******************/
/*****      Common sense beliefs ************/
/********************************************/

is_work(plant(_)).
is_work(tend(_)).
is_work(harvest(_)).
is_work(grind(_)).
is_work(bake(_)).

creatable_from(wheat,bread).

is_pleasant(eat(bread)).

obligation(farm_work).
wish(relax).
wish(stroll).

is_useful(A,true) :- is_pleasant(eat(A)).

agent(X) :- agents(Agents) & .member(X, Agents).
location(X) :- locations(Locations) & .member(X, Locations).

!default_activity.
	

/********************************************/
/*****      Common sense reasoning ************/
/********************************************/

// follow work-intensive wishes when somewhat active
@wish_1[affect(not(mood(arousal,low)))]
+wish(X) : is_work(X) <-
	!!X.

+wish(X)  <-
	// TODO: what is this one doing here?!
	!!X.

+see(Thing)[location(Loc), owner(Per),source(Name)] : is_pleasant(eat(Thing)) & hungry(true) <-   // crowfox
	.appraise_emotion(hope, Name, "see(Thing)");
	+want(Thing);
	+at(Per,Loc);
	+has(Per,Thing);
	!has(Thing).

@has_3[affect(personality(agreeableness,low))]
+has(X) : is_pleasant(eat(X)) & has(X) & hungry(true) <-   // crowfox
	!eat(X).
	
// Share when very agreeable character, unless in a bad mood
@share_food_plan2[atomic, affect(and(personality(agreeableness,high), not(mood(pleasure,low))))]
+has(X) : is_pleasant(eat(X)) & has(X) & hungry(true) <- 			// still has X when event selected
	?agents(Anims);
	!share(X, Anims);
	.print("Shared: ", X, " with the others");
	!eat(X).

+has(X) : is_pleasant(eat(X)) & has(X) & hungry(true)  <-			// still has X when event selected 
	!eat(X).
	
+found(X) <-
	?creatable_from(X,Y);
	?is_useful(Y,Z)
	if(Z) {
		+obligation(create(Y));
	}.

+compliment  <-  		//crowfox
	sing.

+is_dropped(X) : want(X) <-		//crowfox
	collect(X);
	-want(X).

+threat(X)[owner(Y)]<- 				 //crowfox
	handOver(X, Y).
	
@threat_2[affect(personality(conscientiousness,low))]
+threat<- 				 //crowfox
	.print("No, I will not give you anything!");
	refuseToGive(X,Y).
		
+politeQuery(X)[owner(Y)] <- 			 //crowfox
	handOver(X, Y).
	
@politeQuery_1[affect(and(personality(conscientiousness,low), not(mood(dominance,low))))]
+politeQuery(X)[owner(Y)] <-				//crowfox
	.print("No, I will not give you anything!");
	refuseToGive(X,Y).

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

@default_activity_1[affect(personality(conscientiousness,high))]
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
	if(R>0.5) {
		?wish(X);
	} else {
		?obligation(X);
	}
	!X;
	!default_activity.	

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
	
+!has(Thing) : has(Person, Thing) <- 	//crowfox
	.print("I want ", Thing, " from ", Person); 
	!approach(Person);
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

	
@get_1[affect(personality(agreeableness,medium))]
+!get(Thing, Person) <-			//crowfox
	.print("How well you are looking today: how glossy your feathers, how bright your eye")
	.print("I feel sure your voice must surpass that of other birds, just as your figure does")
	.print("let me hear but one song from you that I may greet you as the Queen of Birds");
	flatter(Person).

@get_2[affect(personality(agreeableness,low))]
+!get(Thing, Person) <-				//crowfox
	.print("Give ",X, " to me or I will take it from you!");
	threaten(Person).
	
@get_3[affect(personality(agreeableness,high))]
+!get(Thing, Person) <-
	.print("I am so hungry, would you share your ", X," with me please?");
	ask(Person).	

+!stroll <-      //crowfox
	stroll.

+!thankOther <-
	.print("Thank you!").
	
+!giveAdvice <-
	.print("I have one advice for you: Never trust a flatterer!").
	
+!share(X, Anims) <-
	share(X, Anims).
	
//-!X[_] <- .print("I failed while trying to ", X).