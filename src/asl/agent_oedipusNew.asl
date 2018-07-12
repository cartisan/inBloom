/********************************************/
/***** General knowledge  *******************/
/*****      Common sense beliefs ************/
/********************************************/


obligation(working).
wish(chilling).

!default_activity.

/********************************************/
/*****      Common sense reasoning, getriggert durch hinzufügen oder entfernen von beliefs ************/
/********************************************/

	
 
+self(has_purpose) <-
	.suspend(default_activity).

-self(has_purpose) <-
	.resume(default_activity).
	
+wasAsked <-
	.print("wasAsked");
	+perceived(wasAsked);
	!answer_question(laios).
	
+wifePregnant <-
	.print("wife pregnant");
	+perceived(wifePregnant);
	!ask(oracle).
	
/*+sonKillsMe <-
	.print("My son will kill me");
	+perceived(sonKillsMe);
	!giveAway(Oedipus). */
	
/*+pregnant <-
	getChild;
	.print("got child").*/
	
	

/********************************************/
/***** Self-specifications  *****************/
/*****      Emotion management **************/
/********************************************/


/********************************************/
/*****      Personality *********************/
/********************************************/

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



+!suicide: perceived(deathWish) <-
	+self(has_purpose);
	!killSelf;
	-self(has_purpose).
	
+!blinding: perceived(deadMom) <-					// perception deadMom kann z.B. obligation blindSelf triggern
	+self(has_purpose);
	!blindSelf;
	-self(has_purpose).


	



/********************************************/
/*****      Action Execution Goals **********/
/********************************************/

+!chilling <-
	chilling.
	
+!working <-
	working.
	
+!reigning <-
	reigning.
	
+!goToPlace(location) <-
	goToPlace(location).
	
+!suicide <-
	suicide.
	
+!blinding <-
	blinding.

+!ask(Anims) <-
	ask(Anims).
	
+!answer_question(Anims) <-
	answer_question(Anims).
	
/*+!giveAway(Anims) <-
	giveAway(Anims).*/


	