/*********************/
/* General Knowledge */
/*********************/

is_important(wallet).
smelly(poo).

/***********************/
/* Entry point & drive */
/***********************/

!intrinsic_motivation.

+!intrinsic_motivation <-
	if(is_bad(X) & not is_day_off(_)) {
		-is_bad(X);
		!take_day_off(friday);
	} else {
		!do_stuff;
	};
	!intrinsic_motivation.
	
+self(has_purpose) <-
	.suspend(intrinsic_motivation).
	
-self(has_purpose) <-
	.resume(intrinsic_motivation).

/************************/
/* Positive tradeoff in */
/* mental notes         */
/************************/
	
+!take_day_off(X) <-
	.print("That was exhausting! I'm taking ", X, " off to relax.");
	+is_day_off(X);
	.appraise_emotion(joy, "is_day_off(X)[source(self)]", "self").

+is_holiday(X) <-
	-is_day_off(X).

/*********************/
/* Resolution & Loss */
/*********************/

-has(X) <-
	+missing(X);
	if(is_important(X)) {
		.appraise_emotion(distress, "has(X)[source(percept)]", "self", true);
		+self(has_purpose);
		.print("I need to find my ", X, ", it's very important to me! :(");
		!find(X);
	}.
	
+has(X) : not missing(X) & is_important(X) <-
	.appraise_emotion(pride, "has(X)[source(percept)]", "self").

@p0[atomic]
+has(X) : missing(X) <-
	.appraise_emotion(relief, "has(X)[source(percept)]", "self");
	.succeed_goal(find(X));
	-self(has_purpose);
	-missing(X);												  // creates termination edge to missing
	.appraise_emotion(joy, "missing(X)[source(self)]", "self");   // artifice to create positive trade-off of second kind (see Wilke thesis pp. 19) 
	+is_bad(lost(X)).
	
	
@p1[atomic]
+step_on(X) : smelly(X) <-
	!clean.

+lost(X) <-
	-has(X).
	
@p2[atomic]	
+found(X) <-
	+has(X).
	
+avoid_accident <-
	!get(drink).


+!do_stuff <-
	do_stuff.

+!find(X) <-
	search(X);
	if(not has(X)) {
		!find(X)
	}.

@p3[atomic]	
+!clean <-
	clean.
	
+!get(drink) <-
	get(drink).

/******************/
/* Change of Mind */
/******************/

+bored <- !rethink_life.
+!rethink_life <- 
	.drop_intention(intrinsic_motivation).	// creates termination edge to intrinsic_motivation
//	.drop_desire(intrinsic_motivation).		// alternative way of doing this