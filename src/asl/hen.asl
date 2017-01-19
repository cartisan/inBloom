// Agent hen in project little_red_hen

/*********** General beliefs  ***********/
is_work(plant(_)).
is_work(tend(_)).
is_work(harvest(_)).
is_work(grind(_)).
is_work(bake(_)).

is_pleasant(eat(_)).

/*********** Self-specifications  ***********/
indignation(0).

is_communal(self).

!make_great_again(farm).

/*********** Initial rules ***********/
/* Emotion management 				 */	
+indignation(X) : X > 1 <-
	+is_angry(self)[reason(indignation)].

+indignation(0) : is_angry(self)[reason(indignation)] <-
	-is_angry(self)[reason(indignation)].

+is_angry(self) <-
	.print("I am angry!");
	.remove_plan(helpfullness).

-is_angry(self) <-
	.print("I am not angry anymore!");
	.add_plan(helpfullness).

// TODO: Is it a good idea to remove this
//belief after processing it into indignation?
//or rather keep it as memory?
+rejected_help_request(Req) <-
	?indignation(X);
	-rejected_help_request(Req);
	-+indignation(X+1).
	
/* Goal management 				 */
+has(wheat(seed)) <- 
	.suspend(make_great_again(farm));
	!create_bread.
	
+has(bread) <- 	
	!eat(bread).


	
/*********** Plans ***********/

// TODO: Generalize for all animals!
// TODO: Wait for response before do yourself?
@communality
+!X[_] : is_communal(self) & is_work(X) & not asked(X) <-
	.print("Asking farm animals to help with ", X)
	.send(dog, achieve, help_with(X));
	+asked(X);
	!X;
	-asked(X).

//TODO: What if to execute X her animals need an item? How does hen know she should share it?
// This would mean the hen needs to simulate the other animals and find out what is missing?
@helpfullness	
+!X[_] : is_communal(self) & is_pleasant(X) & not asked(X) <-
	.print("Offer farm animals to share the activity: ", X)
	.send(dog, achieve, share_in(X));
	+asked(X);
	!X;
	-asked(X).	

+!make_great_again(farm) : true	<- 
	randomFarming;
	!make_great_again(farm).

+!eat(bread) : not has(bread) <-
	!create_bread;
	!eat(bread).
	
+!create_bread : not has(wheat(seed)) <- 
	.suspend(create_bread).
	
+!create_bread : has(wheat(seed)) <- 	
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
	
+!eat(bread) : has(bread) <-
	.my_name(Name);
	eat(bread, Name).