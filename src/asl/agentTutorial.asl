// Goal
!cleaning.

// Belief Update Rules
+pos(X) <-
	.print("My position is ", X+1).

+dirty: dirty <-
	.print("I perceive my position is dirty.").	
	
+clean: clean <-
	.print("I perceive my position is clean.").	
	


// Plans

// plans for dirty location
+!cleaning: dirty <- 
	.print("Start cleaning at position ",X+1);
	suck;
	!move;
	!cleaning.		
	
+!cleaning: clean <- 
	.print("Position ",X+1, " is clean");
	!move;
	!cleaning.				
	
+!move: pos(0) <-
	.print("Moving to next position.");
	right.

+!move: pos(1) <-						
	.print("Moving to next position.");
	down.

+!move: pos(2) <-
	.print("Moving to next position.");
	left.
	
+!move: pos(3) <-
	.print("Moving to next position.");
	up.
