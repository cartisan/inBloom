// Agent lazy_animal in project little_red_hen

/* Initial beliefs and goals */

is_lazy(self).
is_work(create_bread).

!cazzegiare.
!eat(bread).

/* Initial rules */

+baked(X) <- +has(X).
+found(X) <- +has(X).
+receives(X) <- +has(X).

+has(bread) <- 	
	.resume(eat(bread)).

// lazy agents don't do things that
// are work
+!X[_] : is_lazy(self) & is_work(X) <-
	.print(X, " is too much work for me!")
	.suspend(X).

/* Plans */
+!cazzegiare : is(silence, gold) 
	<- !cazzegiare.

+!cazzegiare : true	<- 	
	.print("I'm doing sweet nothing.");
	+is(silence, gold);
	!cazzegiare.


+!eat(bread) : has(bread) <- 
	.my_name(Name);
	eat(bread, Name).
	   
+!eat(bread) : not has(bread) <-
	!create_bread;
	!eat(bread).

	
+!create_bread : has(wheat) <- 	
	plant(wheat);
	tend(wheat);
	harvest(wheat);
	grind(wheat);
	bake(bread).   

+!create_bread : not has(wheat)	<- 
	.suspend(create_bread).