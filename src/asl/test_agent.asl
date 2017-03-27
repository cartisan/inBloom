// Agent test_agent in project little_red_hen

/*********** Initial beliefs and goals ***********/
emotion(anger(_)).
emotion(happiness(_)).

anger(0)[target([])].
happiness(10)[target([bob])].

/* Ideal world */
!test.

+!test <- !reset_emotion(happiness(_)).
+!reset_emotion(Em) : emotion(Em) <-  
	-Em;
	.add_annot(Em,target([]), X);
	+X(0).

/* Plans 
!test.

+!test <-
	?happiness(Value);
	!reset_emotion(happiness(_), happiness(0)).

@reset_emotions[atomic]
+!reset_emotion(Em1, Em2) : emotion(Em1) & emotion(Em2) <-
	-Em1;
	+Em2[target([])]. */
	



