// Agent hen in project little_red_hen

/* Initial beliefs and goals */

!make_great_again(farm).

!eat(bread).

/* Initial rules */

+baked(X) <- +has(X).
+found(X) <- +has(X).

+has(wheat) <- 
	.suspend(make_great_again(farm));
	.resume(create_bread).
	
+has(bread) <- 	
	.resume(eat(bread)).

/* Plans */

+!make_great_again(farm) : true	<- 
	randomFarming;
	!make_great_again(farm).

+!eat(bread) : has(bread) <- 
	.my_name(Name);
	eat(bread, Name).
	   
+!eat(bread) : not has(bread) <-
	!create_bread;
	!eat(bread).

// TODO: Make the hen ask for help on every step
// preferably in a generic manner	
+!create_bread : has(wheat) <- 	
	plant(wheat);
	tend(wheat);
	harvest(wheat);
	grind(wheat);
	bake(bread).   

+!create_bread : not has(wheat)	<- 
	.suspend(create_bread).