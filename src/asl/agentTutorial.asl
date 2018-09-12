// Goal
//!addition.

// Belief Update Rules
+sum(X) <-
	.print("I perceive a result");	// print is an internal action provided by Jason, hence the dot at the beginning	
	.print("Sum of 2+4 =", X).

// Plans
+!addition <-
	.print("Started adding");
	add(2,4);						// call environment action
	.print("Finished adding").