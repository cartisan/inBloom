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

default_activity(cazzegiare).
default_activity(farm_work).



/********************************************/
/*****      Common sense reasoning ************/
/********************************************/
+has(X) : is_pleasant(eat(X)) <-
	!eat(X).
	
+has(wheat(seed)) <- 
	!create_bread.

+self(has_purpose) <-
	for (default_activity(X) ) {
		+suspended(X);
		.suspend(X);
	}.

-self(has_purpose) <-
	for (suspended(X) ) {
		.resume(X);
	}.

/********************************************/
/***** Self-specifications  *****************/
/*****      Emotion management **************/
/********************************************/

{ include("emotions.asl") }
	
+rejected_help_request(Req)[source(Name)] <-
	!!increment_anger(Name);
	.abolish(rejected_help_request(Req)).



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
	.abolish(X).	



/********************************************/
/***** Plans  *******************************/
/********************************************/

+!farm_work <- 
	!random_farming;
	!farm_work.

+!create_bread : has(wheat(seed)) <-
	+self(has_purpose);
	!plant(wheat);
	!tend(wheat);
	!harvest(wheat);
	!grind(wheat);
	!bake(bread);
	-self(has_purpose). 	

// TODO: I belief this is a misuse of tell!
// But how do we otherwise inform of such
// a rejection
+!help_with(X)[source(Name)]
	: self(lazy) & is_work(X) <-
	.print("can't help you! ", X, " is too much work for me!");
	.my_name(MyName);
	.send(Name, tell, rejected_help_request(X)).

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