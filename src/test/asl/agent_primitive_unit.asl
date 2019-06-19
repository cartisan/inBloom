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

+!do_stuff <-
	do_stuff.
	
+self(has_purpose) <-
	.suspend(intrinsic_motivation).
	
-self(has_purpose) <-
	.resume(intrinsic_motivation).

/************************/
/* Positive tradeoff in */
/* mental notes         */
/************************/

+!take_day_off(X) : is_holiday(X) <-
	.drop_intention(take_day_off(X)).
	
+!take_day_off(X) <-
	.print("That was exhausting! I'm taking ", X, " off to relax.");
	+is_day_off(X);
	.appraise_emotion(joy, "self", "is_day_off(X)[source(self)]").

+is_holiday(X) <-
	-is_day_off(X).

/*********************/
/* Resolution & Loss */
/*********************/

-has(X) <-
	if(is_important(X)) {
		.appraise_emotion(distress, "self", "has(X)[source(percept)]", false);
		+self(has_purpose);
	};
	+lost(X).

+has(X) : not lost(X) & is_important(X) <-
	.appraise_emotion(pride, "self", "has(X)[source(percept)]").

+has(X) : lost(X) <-
	.appraise_emotion(relief, "self", "has(X)[source(percept)]");
	.succeed_goal(find(X));
	-self(has_purpose);
	-lost(X);												  // creates termination edge to lost
	.appraise_emotion(joy, "self", "lost(X)[source(self)]");  // artifice to create positive trade-off of second kind (see Wilke thesis pp. 19) 
	+is_bad(lost(X)).
	
	
+lost(X) : is_important(X) <-
	.print("I need to find my ", X, ", it's very important to me! :(");
	!find(X).
	

+step_on(X) : smelly(X) <-
	!clean.
	
+avoid_accident <-
	!get(drink).

+!find(X) <-
	search(X);
	if(lost(X)) {
		!find(X)
	}.
	
+!clean <-
	clean.
	
+!get(drink) <-
	get(drink).
