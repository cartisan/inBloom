{include("agent-desire_wish_management.asl")}

/* Initial beliefs and rules */
+friend(X) <-
	-wish(drown_sorrow).

/* Initial goals */

/* Plans */
+!drown_sorrow : true <- 
	get(drink).	// triggers happening find(friend)
	
+!work(bar) : true <- wipe(glass).

