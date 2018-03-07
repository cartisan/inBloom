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
	

+seen(cheese) <-					// the belief seen(cheese)
	.print(" seen cheese");	// TODO delete
	+perceived(seen(cheese));	// mental note
	!get_cheese_from_animal.
	
+freeCheese <-
	+perceived(freeCheese);
	!get_cheese_from_ground.
	
+wasFlattered <-
	.print("was flattered");
	+perceived(wasFlattered);
	!bragging. 
	
+wasAsked <-
	.print("was asked");
	+perceived(wasAsked);
	!answer_cheese_request.
	//.abolish(_[perception(wasAsked)]).
	
+wasAnsweredNegatively <-
	.print("was answered negatively");
	+perceived(wasAnsweredNegatively);
	!get_cheese_from_animal.
	
 
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


@get_cheese_from_animal_1[affect(and(personality(agreeableness,low),not(mood(pleasure,high))))]
+!get_cheese_from_animal: perceived(seen(cheese)) <-
	+self(has_purpose);
	!flatter(crow);
	-self(has_purpose).
	
	
@get_cheese_from_animal_2[affect(and(not(personality(agreeableness,low)),(mood(pleasure,high))))]
+!get_cheese_from_animal: perceived(seen(cheese)) <-
	+self(has_purpose);
	!askForCheese(crow);
	-self(has_purpose).
	
	
	@get_cheese_from_animal_3[affect(and(not(personality(agreeableness,low)),not(mood(pleasure,high))))]
+!get_cheese_from_animal: perceived(seen(cheese)) <-
	+self(has_purpose);
	!flatter(crow);
	-self(has_purpose).
	
	
	@get_cheese_from_animal_4[affect(and((personality(agreeableness,low)),(mood(pleasure,high))))]
+!get_cheese_from_animal: perceived(seen(cheese)) <-
	+self(has_purpose);
	!askForCheese(crow);
	-self(has_purpose).
	 
	 
	
+!get_cheese_from_ground: perceived(freeCheese) <-
	+self(has_purpose);
	!pickUpCheese(cheese);
	-self(has_purpose).
	
+!bragging: perceived(wasFlattered) <-
	+self(has_purpose);
	!sing;
	-self(has_purpose).
	
@answer_cheese_request_1[affect(and(personality(agreeableness,low)))]
+!answer_cheese_request: perceived(wasAsked) | perceived(wasAnsweredNegatively) <-
	+self(has_purpose);
	!answerNegatively(fox);
	-self(has_purpose).	
	
@answer_cheese_request_2[affect(and(personality(agreeableness,high)))]
+!answer_cheese_request: perceived(wasAsked) | perceived(wasAnsweredNegatively) <-
	+self(has_purpose);
	!share(cheese,fox);
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
	
+!askForCheese(Anims) <-
	askForCheese(Anims).
		
+!sing <-
	sing.
	
+!pickUpCheese(cheese) <-
	pickUpCheese(cheese).
	
+!answerNegatively(Anims) <-
	answerNegatively(Anims).
	
+!share(X, Anims) <-
	share(X, Anims). 


	