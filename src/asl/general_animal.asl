// general agent in project little_red_hen

/*********** General beliefs  ***********/
is_work(create_bread).
is_work(make_great_again(_)).
is_work(plant(_)).
is_work(tend(_)).
is_work(harvest(_)).
is_work(grind(_)).
is_work(bake(_)).

is_pleasant(eat(bread)).

animals([dog, cow, pig]).

/*********** Self-specifications  ***********/
anger(0)[target([])].

/*********** Initial rules ***********/
/* Emotion management 				 */	

+anger(10)[target(L)] <-
	.remove_plan(helpfullness);
	!punish(L).

// begin declarative goal: punishment for angering
+!punish(L) : punish(L) <- true.
@bPlan[atomic]	
+!punish(L) :has(X) & is_pleasant(eat(X)) <-
	.send(L, achieve, eat(X));
	.print("Asking ", L, " to eat ", X, ". But not shareing necessary ressources. xoxo");
	+punish(L);
	?punish(L).
+punish(L) : true <- 
	!reset_anger;
	-punish(L);	
	.succeed_goal(punish(L)).

//     +backtracking goal
-!punish(L) : true <- !!punish(L).
//	   +blind commitment
+!punish(L) : true <- !!punish(L).
//	   + relativised commitment
-anger(X) : ~anger(Y) & Y>9 <- 
	.succeed_goal(punish(L)).
	

+!reset_anger <-
	?anger(X)[target(L)];
	-anger(X)[target(L)];
	+anger(0)[target([])].

@aPlan[atomic]
+rejected_help_request(Name, Req) <-
	?anger(X)[target(L)];
	.union([Name], L, NewL);
	+anger(X+1)[target(NewL)];
	-anger(X)[target(L)];
	.abolish(rejected_help_request(Name, Req)).


/* Goal management 				 */
+has(wheat(seed)) <- 
	.suspend(make_great_again(farm));
	!create_bread.

// TODO: How to make Java side find sender on itself? 
@helpfullness
+has(X) : is_communal(self) & is_pleasant(eat(X)) <-
	.print("Sharing: ", X, " with the others");
	.my_name(MyName);
	?animals(Anims);
	!share(MyName, X, Anims);
	!eat(X).
	
+has(X) : is_pleasant(eat(X)) <-
	!eat(X).
	
/*********** Plans ***********/

// TODO: Wait for response before do yourself?
@communality
+!X[_] : is_communal(self) & is_work(X) & not asked(X) <-
	.print("Asking farm animals to help with ", X)
	?animals(A);
	.send(A, achieve, help_with(X));
	+asked(X);
	!X;
	-asked(X).

@vindication	
+!X[_] : is_angry(self) & is_pleasant(X) & not asked(X) <-
	.print("Offering farm animals to: ", X);
	?animals(A);
	.send(A, achieve, X);
	.print("But not shareing necessary ressources.");
	+asked(X);
	!X;
	-asked(X).
	
@lazyness
+!X[_] : is_lazy(self) & is_work(X) <-
	.print(X, " is too much work for me!");
	.suspend(X).	

+!make_great_again(farm) : true	<- 
	!randomFarming;
	!make_great_again(farm).
	
+!create_bread : has(wheat(seed)) <- 	
	!plant(wheat);
	!tend(wheat);
	!harvest(wheat);
	!grind(wheat);
	!bake(bread).   


// TODO: I belief this is a misuse of tell!
// But how do we otherwise inform of such
// a rejection
+!help_with(X)[source(Name)]
	: is_lazy(self) & is_work(X) <-
	.print("can't help you! ", X, " is too much work for me!");
	.my_name(MyName);
	.send(Name, tell, rejected_help_request(MyName, X)).

+!help_with(X)[source(Name)] <-
	.print("I'll help you with ", X, ", ", Name);
	help(Name).

+!cazzegiare : is(silence, gold) 
	<- !cazzegiare.

+!cazzegiare : true	<- 	
	.print("I'm doing sweet nothing.");
	+is(silence, gold);
	!cazzegiare.

// action-execution goals
+!randomFarming <-
	.my_name(MyName);
	randomFarming(MyName).

+!plant(wheat) <-
	.my_name(MyName);
	plant(MyName, wheat).
	
+!tend(wheat) <-
	tend(wheat).
	
+!harvest(wheat) <-
	harvest(wheat).

+!grind(wheat) <-
	grind(wheat).

+!bake(bread) <-
	.my_name(MyName);
	bake(MyName, bread).
	
+!eat(X) : has(X) <- 
	.my_name(Name);
	eat(Name, X).
	
+!eat(X) <- 
	.print("Can't eat ", X, ", I don't have any! :( ")
	.suspend(eat(X)).
	
+!share(MyName, X, [H|T]) : .length(T) > 0 <-
	share(MyName, X, H);
	!share(MyName, X, T).	
	
+!share(MyName, X, [H|T]) : .length(T) == 0 <-
	share(MyName, X, H).	
	
+!share(MyName, X, Anim) <-
	share(MyName, X, Anim).	