// PARAMETERS
emo_threshold(10).

// EMOTIONS
emotion(anger).
emotion(happiness).

anger(0)[target([])].
happiness(0)[target([])].

// HAPPINESS SPECIFIC

// TODO: Abstract to reset_emotion!!!
+!reset_happiness <-
	?happiness(X)[target(L)];
	-happiness(X)[target(L)];
	+happiness(0)[target([])].

/* happiness causes reward desire */
+happiness(X)[target(L)] : emo_threshold(X) <-
	!rewarded(L).
	
/* begin declarative goal  (p. 174; Bordini,2007)*/
+!rewarded(L) : rewarded(L) <- true.
	
+!rewarded(L) : true <- 
	.send(L, tell, praised(self));
	+rewarded(L);
	?rewarded(L).

//     +backtracking goal
-!rewarded(L) : true <- !!rewarded(L).

//	   +blind commitment
+!rewarded(L) : true <- !!rewarded(L).

//	   +relativised commitment
-happiness(X) : ~happiness(Y) & Y>9 <- 
	-rewarded(L);
	.succeed_goal(rewarded(L)).

	
// ANGER SPECIFIC
+!reset_anger <-
	?anger(X)[target(L)];
	-anger(X)[target(L)];
	+anger(0)[target([])].


/* anger causes punishment desire */
+anger(X)[target(L)] : emo_threshold(X) <-
	.remove_plan(helpfullness);
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
	!reset_anger;
	-punished(L);	
	.succeed_goal(punished(L)).

//     +backtracking goal
-!punished(L) : true <- !!punished(L).

//	   +blind commitment
+!punished(L) : true <- !!punished(L).

//	   + relativised commitment
-anger(X) : ~anger(Y) & Y>9 <- 
	.succeed_goal(punished(L)).