// Agent test_agent in project little_red_hen

/*********** Initial beliefs and goals ***********/
is_work(plant(_)).
is_communal(self).

!plant(wheat).

/*********** Initial rules ***********/

+!X[_] : is_communal(self) & is_work(X) & not asked(X) <-
	.print("Asking dog to ", X)
	.send(dog, achieve, help_with(X));
	+asked(X);
	!X;
	-asked(X).


/* Plans */

+!plant(wheat) <-
	.print("Planting wheat myself").
