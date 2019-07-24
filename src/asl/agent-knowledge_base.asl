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

is_work(help_with(Name, Plan)) :- is_work(Plan).

is_useful(Item) :- is_pleasant(eat(Item)) & hungry.

agent(Ag) :- agents(List) & .member(Ag, List).
location(Loc) :- locations(Locations) & .member(Loc, Locations).

present(List) :- .findall(Agent, (at(Agent,Loc) & at(Loc) & agent(Agent)), List).