/********************************************/
/***** General knowledge  *******************/
/*****      Common sense beliefs ************/
/********************************************/

is_work(plant(_)).
is_work(tend(_)).
is_work(harvest(_)).
is_work(grind(_)).
is_work(bake(_)).

is_pleasant(eat(bread)).

animals([dog, cow, pig]).



/********************************************/
/*****      Common sense desires ************/
/********************************************/
+has(X) : is_pleasant(eat(X)) <-
	!eat(X).
	
+has(wheat(seed)) <- 
	+self(has_purpose);
	!create_bread;
	-self(has_purpose).	

/********************************************/
/***** Self-specifications  *****************/
/*****      Emotion management **************/
/********************************************/

{ include("emotions.asl") }
	
+rejected_help_request(Name, Req) <-
	!!increment_anger(Name);
	.abolish(rejected_help_request(Name, Req)).



/********************************************/
/*****      Personality management **********/
/********************************************/

{ include("personality.asl") }

@helpfullness
+has(X) : self(communal) & is_pleasant(eat(X)) <-
	.print("Sharing: ", X, " with the others");
	?animals(Anims);
	!share(X, Anims);
	!eat(X).

// TODO: Wait for response before do yourself?
@communality
+!X[_] : self(communal) & is_work(X) & not asked(X) <-
	.print("Asking farm animals to help with ", X)
	?animals(A);
	.send(A, achieve, help_with(X));
	+asked(X);
	!X;
	-asked(X).
	
@lazyness
+!X[_] : self(lazy) & is_work(X) <-
	.print(X, " is too much work for me!");
	.fail(X).	



/********************************************/
/***** Plans  *******************************/
/********************************************/

//TODO: how to change this to not have to suspend intention?
+!farm_work <- 
	!random_farming;
	!farm_work.

+self(has_purpose) <-
	.print("Life has a meaning!");
	.suspend(farm_work).

-self(has_purpose) <-
	.print("...aaaaaaand it's gone.");
	!farm_work.
	
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
	: self(lazy) & is_work(X) <-
	.print("can't help you! ", X, " is too much work for me!");
	.my_name(MyName);
	.send(Name, tell, rejected_help_request(MyName, X)).

+!help_with(X)[source(Name)] <-
	.print("I'll help you with ", X, ", ", Name);
	help(Name).


+!cazzegiare <-
	.print("I'm doing sweet nothing.");
	.my_name(MyName);
	!!increment_happiness(MyName);
	!relax;
	!cazzegiare.


/********************************************/
/*****      Action Execution Goals **********/
/********************************************/
+!relax <-
	relax.
	
+!random_farming <-
	random_farming.

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
	
+!eat(X) : has(X) <- 
	eat(X).
	
+!eat(X) <- 
	.print("Can't eat ", X, ", I don't have any! :( ")
	.suspend(eat(X)).
	
+!share(X, [H|T]) : .length(T) > 0 <-
	share(X, H);
	!share(X, T).	
	
+!share(X, [H|T]) : .length(T) == 0 <-
	share(X, H).	
	
+!share(X, Anim) <-
	share(X, Anim).	