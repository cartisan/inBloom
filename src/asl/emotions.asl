// PARAMETERS
emo_threshold(10).

// EMOTIONS
emotion(angry).
emotion(happy).

emotion_scale(anger(_)).
emotion_scale(happiness(_)).

anger(0)[target([])].
happiness(0)[target([])].


@reset_emotions[atomic]
+!reset_emotion(Em1, Em2) : emotion_scale(Em1) & emotion_scale(Em2) <-
	-Em1;
	+Em2[target([])].
	
@increment_happiness[atomic]
+!increment_happiness(Name) <-
	?happiness(Val)[target(L)];
	.union([Name], L, NewL);
	+happiness(Val+1)[target(NewL)];
	-happiness(Val)[target(L)].

@increment_anger[atomic]
+!increment_anger(Name) <-
	?anger(Val)[target(L)];
	.union([Name], L, NewL);
	-anger(Val)[target(L)];
	+anger(Val+1)[target(NewL)].
	
+self(X) : emotion(X) <-
 	little_red_hen.asl_actions_plot.add_emotion(X).
 	
-self(X) : emotion(X) <-
 	little_red_hen.asl_actions_plot.remove_emotion(X).


// HAPPINESS SPECIFIC

/* happiness causes reward desire */
+happiness(X)[target(L)] : emo_threshold(X) <-
	+self(happy);
	!rewarded(L).
	
/* begin declarative goal  (p. 174; Bordini,2007)*/
+!rewarded(L) : rewarded(L) <- true.
	
+!rewarded(L) : true <- 
	.send(L, tell, praised(self));
	+rewarded(L);
	?rewarded(L).
	
+rewarded(L) : true <- 
	!reset_emotion(happiness(_), happiness(0));
	-rewarded(L);
	-self(happy);
	.succeed_goal(rewarded(L)).

//     +backtracking goal
-!rewarded(L) : true <- !!rewarded(L).

//	   +blind commitment
+!rewarded(L) : true <- !!rewarded(L).

//	   +relativised commitment
-happiness(X) : ~happiness(Y) & Y>9 <- 
	-rewarded(L);
	-self(happy);
	.succeed_goal(rewarded(L)).


	
// ANGER SPECIFIC
/* anger causes punishment desire */
+anger(X)[target(L)] : emo_threshold(X) <-
	.remove_plan(helpfullness);
	+self(angry);
	!punished(L).
	
// begin declarative goal  (p. 174; Bordini,2007)*/
+!punished(L) : punished(L) <- true.

@bPlan[atomic]	
+!punished(L) :has(X) & is_pleasant(eat(X)) <-
	.send(L, achieve, eat(X));
	.print("Asking ", L, " to eat ", X, ". But not shareing necessary ressources. xoxo");
	+punished(L);
	?punished(L).

+punished(L) : true <- 
	!reset_emotion(anger(_), anger(0));
	-punished(L);
	-self(angry);
//	.add_plan(helpfullness);		// ASL doesn't support adding plans by label?! -.-
	.succeed_goal(punished(L)).

//     +backtracking goal
-!punished(L) : true <- !!punished(L).

//	   +blind commitment
+!punished(L) : true <- !!punished(L).

//	   + relativised commitment
-anger(X) : ~anger(Y) & Y>9 <-
	-self(angry);
	.add_plan(helpfullness);
	.succeed_goal(punished(L)).