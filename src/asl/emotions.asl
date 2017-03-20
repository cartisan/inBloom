// PARAMETERS
emo_threshold(10).

// EMOTIONS
anger(0)[target([])].


// ANGER SPECIFIC
+anger(X)[target(L)] : emo_threshold(X) <-
	.remove_plan(helpfullness);
	!punished(L).
	
+!reset_anger <-
	?anger(X)[target(L)];
	-anger(X)[target(L)];
	+anger(0)[target([])].
	
	
/* anger causes punishment desire */
/* begin declarative goal  (p. 174; Bordini,2007)*/
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
	.succeed_goal(punish(L)).