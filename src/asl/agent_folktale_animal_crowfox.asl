/******************************************************************************/
/********** perception management *********************************************/
/******************************************************************************/

+see(Thing)[location(Loc), owner(Per)] : is_useful(Thing) <-   // crowfox
	+at(Per,Loc);
	+has(Per,Thing);
	.appraise_emotion(hope, "see(Thing)");
	.suspend(wish(relax));
	+wish(has(Thing)).
	
+see(Thing)[owner(Per)] : is_useful(Thing) <-   // crowfox
	+has(Per,Thing);
	.my_name(Name);
	.appraise_emotion(hope, "see(Thing)");
	.suspend(wish(relax));
	+wish(has(Thing)).
	
+is_dropped(Thing)[owner(Agent)] : .my_name(Agent) <-
	-has(Thing);
	.appraise_emotion(remorse, "is_dropped(Thing)", Agent).


@is_dropped[atomic]
+is_dropped(Thing)[owner(Agent)] : wish(has(Thing)) <-
	.appraise_emotion(gloating, "is_dropped(Thing)", Agent);
	!collect(Thing);
	.resume(wish(relax)).

@compliment[atomic]
+complimented : .my_name(Agent)  <-
	.appraise_emotion(pride, "complimented", Agent);
	!sing.

+threatened(Item)[source(Other)] <-
	.print("Oh no, don't hurt me!");
	.appraise_emotion(fear, "threatened(Item)");
	!handOver(Item, Other).
	
@threat_2[affect(personality(conscientiousness,low))]
+threatened(Item)[source(Other)]  : .my_name(Me) <-
	.print("No, I will not give you anything!");
	.appraise_emotion(reproach, "threatened(Item)", Other);
	!refuseHandOver(Item, Other).

+refuseHandOver(Thing, Agent) <-
	// TODO: continue story by an attack mechanism?
	.resume(wish(relax)).


/***** action-perception management **********************************************/
/******************************************************************************/

@collect[atomic]
+collect(Thing)[success(true)] <-
	+has(Thing);
	-wish(has(Thing)).
	
+handOver(Other, Item, Me)[success(true)] : .my_name(Me) <-
	+has(Item).
	
+handOver(Item, Agent)[success(true)] <-
	-has(Item).
	
/******************************************************************************/
/***** plans  *****************************************************************/
/******************************************************************************/

+!has(Thing) : has(Agent, Thing) & at(Agent, Loc1) & at(Loc2) & not Loc1==Loc2 <-
	!approach(Agent).

+!has(Thing) : has(Agent, Thing) & at(Agent, Loc1) & at(Loc2)  & Loc1==Loc2 <-
	!get(Thing, Agent).
	
/******************************************************************************/
/*****      Action Execution Goals ********************************************/
/******************************************************************************/

+!approach(Agent) : agent(Agent) & at(Agent, Loc) <-
	goTo(Loc).

+!approach(Loc) : location(Loc) <-
	goTo(Loc).

@get_threaten[affect(personality(agreeableness,low))]
+!get(Thing, Agent) :	at(Loc)[level(L)] & at(Agent,Loc)[level(L)] <-
	.print("Give ", Thing, " to me or I will take it from you!");
	.send(Agent, tell, threatened(Thing));
	.wait(has(Thing) | refuseHandOver(Thing, Agent)).

@get_flatter[affect(personality(agreeableness,low))]
+!get(Thing, Agent) : at(Loc) & at(Agent,Loc) <-
	.print("So lovely your feathers, so shiny thy beak!");
	.send(Agent, tell, complimented);
	.wait({+is_dropped(Thing)}).
	
+!get(Thing, Agent) : at(Loc) & at(Agent,Loc)  <-
	.print("I am so hungry, would you share your ", Thing," with me please?");
	.my_name(Me);
	.send(Agent, achieve, share_food(Thing, Me));
	.wait(has(Thing)  | refuseHandOver(Thing, Agent)).	

+!collect(Thing) <-
	collect(Thing).

+!sing <-
	sing.

@share_2[affect(personality(agreeableness,low))]
+!share(Item, Agent) <- 
	.print("I'm not sharing with anyone!");
	!refuseHandOver(Item, Agent).

+!handOver(Item, Agent) <-
	handOver(Item, Agent).

+!refuseHandOver(Item, Agent) <-
	refuseHandOver(Item, Agent).		