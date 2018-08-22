/********************************************/
/***** General knowledge  *******************/
/*****      Common sense beliefs ************/
/********************************************/

is_pleasant(eat(cheese)).

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

+has(X) : is_pleasant(eat(X)) & hungry & has(X)  <-			// still has X when event selected 
	!eat(X).
	

+seen(cheese) <-
	.print(" seen cheese");	
	+perceived(seen(cheese));
	!get_cheese_from_animal.
	
+freeCheese <-
	.print(" seen cheese fall on the ground");
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

// usually this should be decided via personality/mood
// i.e. extraversion high/arousal high --> walkAround
+!default_activity <-
	.my_name(N);
	if(N==crow) {
		sitAround;	
	} else {
		walkAround;
	}
	!default_activity.	
	
/********************************************/
/***** Plans  *******************************/
/********************************************/


@get_cheese_from_animal_1[affect(not(mood(pleasure,high)))]
+!get_cheese_from_animal: perceived(seen(cheese)) <-
	+self(has_purpose);
	.print("trying to get cheese by flattery");
	!flatter(crow).
	
@get_cheese_from_animal_2[affect((mood(pleasure,high)))]
+!get_cheese_from_animal: perceived(seen(cheese)) <-
	.print("trying to get cheese by asking");
	+self(has_purpose);
	!askForCheese(crow).

	
+!get_cheese_from_ground: perceived(freeCheese) <-
	.print("trying to pick up cheese");
	!pickUpCheese;
	-self(has_purpose).
	
+!bragging: perceived(wasFlattered) <-
	.print("bragging");
	!sing.
	
@answer_cheese_request_1[affect(and(personality(agreeableness,low)))]
+!answer_cheese_request: perceived(wasAsked) | perceived(wasAnsweredNegatively) <-
	!answerNegatively(fox).
	
@answer_cheese_request_2[affect(and(personality(agreeableness,high)))]
+!answer_cheese_request: perceived(wasAsked) | perceived(wasAnsweredNegatively) <-
	!share(cheese,fox).
	


/********************************************/
/*****      Action Execution Goals **********/
/********************************************/

+!sitAround <-
	sitAround.
	
+!walkAround <-
	walkAround. 
	
+!flatter(Anims) <-
	.print("flattering")
	flatter(Anims).
	
+!askForCheese(Anims) <-
	.print("asking for cheese: ", Anims)
	askForCheese(Anims).
		
+!sing <-
	sing.
	
+!pickUpCheese <-
	.print("picking up cheese");
	pickUpCheese.
	
+!answerNegatively(Anims) <-
	.print("answering negatively");
	answerNegatively(Anims).
	
+!share(X, Anims) <-
	share(X, Anims). 

+!eat(X) <-
	eat(X).

	