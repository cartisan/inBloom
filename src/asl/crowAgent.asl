/********************************************/
/***** General knowledge  *******************/
/*****      Common sense beliefs ************/
/********************************************/

is_work(plant(_)).
is_work(tend(_)).
is_work(harvest(_)).
is_work(grind(_)).
is_work(bake(_)).

is_pleasant(eat(cheese)).

obligation(walkAround).
wish(sitAround).

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

// Don't follow obligations if feeling passive, or very "anti-social" tendencies
@default_activity_2[affect(or(personality(conscientiousness,low), mood(arousal,negative)))]
+!default_activity <-
	?wish(X);
	!X;
	!default_activity.
	
// Always follow obligations if high on consc, and feels like being active
@default_activity_1[affect(and(personality(conscientiousness,high), mood(arousal,positive)))]
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
/***** Plans  *******************************/
/********************************************/

+!get_cheese : cheeseSeen <-
	+self(has_purpose);
	!flatter(crow);
	!pickUpCheese(cheese);
	-self(has_purpose). 	

/********************************************/
/*****      Action Execution Goals **********/
/********************************************/

+!sitAround <-
	sitAround.
	
+!walkAround <-
	walkAround. 
	
+!flatter(Anims) <-
	flatter(Anims).
	
+!sing <-
	sing.
	
+!pickUpCheese(cheese) <-
	pickUpCheese(cheese).
	