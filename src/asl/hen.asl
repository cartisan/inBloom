// Agent hen in project little_red_hen

/*********** Initial beliefs and goals ***********/
is_work(plant(_)).
is_work(tend(_)).
is_work(harvest(_)).
is_work(grind(_)).
is_work(bake(_)).

is_communal(self).

indignation(0).

!make_great_again(farm).
!eat(bread).

/*********** Initial rules ***********/

+baked(X) <- +has(X).
+find(X) <- 
	+has(X);
	-find(X).

+has(wheat) <- 
	.suspend(make_great_again(farm));
	.resume(create_bread).
	
+has(bread) <- 	
	.resume(eat(bread)).

+reject_help_request(_) <-
	?indignation(X);
	-+indignation(X+1).
	
	
/*********** Plans ***********/
+!X[_] : is_communal(self) & is_work(X) & not asked(X) <-
	.print("Asking farm animals to help with ", X)
	.send(dog, achieve, help_with(X));
	+asked(X);
	!X;
	-asked(X).

+!make_great_again(farm) : true	<- 
	randomFarming;
	!make_great_again(farm).

+!eat(bread) : has(bread) <- 
	.my_name(Name);
	eat(bread, Name).
	   
+!eat(bread) : not has(bread) <-
	!create_bread;
	!eat(bread).

+!create_bread : not has(wheat)	<- 
	.suspend(create_bread).
	
+!create_bread : has(wheat) <- 	
	!plant(wheat);
	!tend(wheat);
	!harvest(wheat);
	!grind(wheat);
	!bake(bread).   


// action-execution goals
+!plant(wheat) <-
	plant(wheat).
	
+!tend(wheat) <-
	tend(wheat).
	
+!harvest(wheat) <-
	harvest(wheat).

+!grind(wheat) <-
	grind(wheat).

+!bake(bread) <-
	bake(bread).	