is_important(wallet).

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
	
+!take_day_off(X) : is_holiday(X) <-
	.drop_intention(take_day_off(X)).
	
+!take_day_off(X) <-
	+is_day_off(X);
	.appraise_emotion(joy, "self", "is_day_off(X)[source(self)]").

+is_holiday(X) <-
	-is_day_off(X).
	
+self(has_purpose) <-
	.suspend(intrinsic_motivation).
	
-self(has_purpose) <-
	.resume(intrinsic_motivation).
	
-has(X) <-
	if(is_important(X)) {
		.appraise_emotion(fear, "self", "has(X)[source(percept)]");
		+self(has_purpose);
	};
	+lost(X).
	

+has(X) : lost(X) <-
	.appraise_emotion(relief, "self", "has(X)[source(percept)]");
	.succeed_goal(find(X));
	-self(has_purpose);
	-lost(X);
	.appraise_emotion(joy, "self", "lost(X)[source(self)]");
	+is_bad(lost(X)).
	
	
+lost(X) : is_important(X) <-
	!find(X).
	
+!find(X) <-
	search(X);
	if(lost(X)) {
		!find(X)
	}.