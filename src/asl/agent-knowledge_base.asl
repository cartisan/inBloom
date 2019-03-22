/******************************************************************************/
/***** propositional knowledge  ***********************************************/
/******************************************************************************/

is_work(farm_work).
is_work(create(bread)).
is_work(plant(_)).
is_work(tend(_)).
is_work(harvest(_)).
is_work(grind(_)).
is_work(bake(_)).

complex_plan(create(bread)).
coping_behavior(punish).
coping_behavior(eat(_)).
coping_behavior(share_food(_, _)).

creatable_from(wheat,bread).
is_pleasant(eat(bread)).

already_asked(farm_work).

/******************************************************************************/
/*****     inference rules ****************************************************/
/******************************************************************************/

is_work(help_with(Name, X)) :- is_work(X).

is_useful(A) :- is_pleasant(eat(A)) & hungry.

agent(X) :- agents(Agents) & .member(X, Agents).
location(X) :- locations(Locations) & .member(X, Locations).