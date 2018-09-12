// Goal
!clean_all.

// Belief Update Rules

// Plans
+!clean_all <- !clean; !move; !clean_all.

+!move:position(0)  <- right.
+!move:position(1)  <- down.
+!move:position(2)  <- left.
+!move:position(3)  <- up.

+!clean <- clean.