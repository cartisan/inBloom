/********************************************/
/***** General knowledge  *******************/
/*****      Common sense beliefs ************/
/********************************************/

is_pleasant(eat(cheese)).

evil_plan(peek_around_for_cheese).
neutral_behavior(sitAround).

!default_activity.

/********************************************/
/*****      Common sense reasoning ************/
/********************************************/


// Share when in a good mood, and not "misanthrophic"
@share_food_plan[atomic, affect(and(mood(pleasure,high),not(personality(agreeableness,low))))]
+has(X) : is_pleasant(eat(X)) & has(X) <- 			// still has X when event selected
	?agents(Anims);
	!share(X, Anims);
	.print("Shared: ", X, " with the others");
	!eat(X).

// Share when very agreeable character, unless in a bad mood
@share_food_plan2[atomic, affect(and(personality(agreeableness,high), not(mood(pleasure,low))))]
+has(X) : is_pleasant(eat(X)) & has(X) <- 			// still has X when event selected
	?agents(Anims);
	!share(X, Anims);
	.print("Shared: ", X, " with the others");
	!eat(X).

+has(X) : is_pleasant(eat(X)) & has(X)  <-			// still has X when event selected 
	!eat(X).
	

+seen(cheese) <-					// the belief seen(cheese)
	+perceived(seen(cheese));	// mental note
	!get_cheese1.
	
+freeCheese <-						// mental noes bleiben erhalten
	+perceived(freeCheese);
	!get_cheese2.
	
+wasFlattered <-
	.suspend(default_activity);
	+perceived(wasFlattered);
	!bragging;
	.resume(default_activity).
 
+self(has_purpose) <-
	.suspend(default_activity).

-self(has_purpose) <-
	.resume(default_activity).


/********************************************/
/***** Self-specifications  *****************/
/*****      Emotion management **************/
/********************************************/


/********************************************/
/*****      Personality *********************/
/********************************************/

// Don't follow evil plan if neutral or high on agreeableness or feeling passive
@default_activity_2[affect(not(personality(agreeableness,low)))]
+!default_activity <-
	?neutral_behavior(X);
	!X;
	!default_activity.
	
// Always follow evil plan if low on agreeableness, and feels like being active
@default_activity_1[affect(and(personality(agreeableness,low),mood(dominance,high)))]
+!default_activity <-
	?evil_plan(X);
	!X;
	!default_activity.

// If not high on consc, but feels active: randomly choose between desires and wishes
@default_activity_3
+!default_activity <-
	.random(R);
	if(R>0.5) {
		?neutral_behavior(X);
	} else {
		?evil_plan(X);
	}
	!X;
	!default_activity.
	
/********************************************/
/***** Plans  *******************************/
/********************************************/

// Perform an evil plan to get the cheese if "anti-social" tendencies
@flatter_somebody[affect(personality(conscientiousness, low))]
+!get_cheese1: perceived(seen(cheese)) <-
	!flatter(crow).

	
+!get_cheese2: perceived(freeCheese) <-
	!pickUpCheese(cheese).

// Start singing when you were flattered and you are extraverted and in a good mood
@start_singing[affectand((personality(extraversion,positive)),mood(pleasure,high))]
+!bragging: perceived(wasFlattered) <-
	!sing.

/********************************************/
/*****      Action Execution Goals **********/
/********************************************/

+!sitAround <-
	sitAround.
	
+!peek_around_for_cheese <-
	peek_around_for_cheese.
	
+!flatter(Anims) <-
	flatter(Anims).
	
+!sing <-
	sing.
	
+!pickUpCheese(cheese) <-
	pickUpCheese(cheese).
	