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

creatable_from(wheat,bread).
is_pleasant(eat(bread)).

already_asked(farm_work).

/******************************************************************************/
/*****     inference rules ****************************************************/
/******************************************************************************/

is_useful(A) :- is_pleasant(eat(A)) & hungry.

agent(X) :- agents(Agents) & .member(X, Agents).
location(X) :- locations(Locations) & .member(X, Locations).