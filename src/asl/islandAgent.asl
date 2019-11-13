// Agent islandAgent in project inBloom

/******************************************************************************/
/************ knowledge base  *************************************************/
/******************************************************************************/
// Here I can store my knowledge base externally if I want to (or if I have a knowledge base at all)
// This rn is the farming knowledge base that obviously isn't that important for our agent, but is here for example reasons

{include("agent-knowledge_base.asl")}
	
/********************************************/
/*****     wishes and obligations ***********/
/********************************************/
// This is some imported code that helps managing wishes and obligations in a new way
// The code is project independent, so I also import and use it

{include("agent-desire_wish_management.asl")}

//wish(seeTheWorld).
//+self(farm_animal) <- +obligation(farm_work).

/******************************************************************************/
/********** perception management *********************************************/
/******************************************************************************/





/* Initial beliefs and rules */


/* Initial goals */

!start.

/* Plans */

@go_on_cruise[affect(personality(openness,high))]
+!start <- goOnCruise.

@go_on_cruise_default
+!start <- stayHome.

@food_plan
+!eat <- if(has(food)) {
			eat;
			-wish(eat);
		} else {
			getFood;
		}.
		
+!heal <- sleep;
		  -wish(heal).


/* React to new Belifes / Percepts */

+hungry[source(Name)] <- +wish(eat).

+sick[source(Name)] <- +wish(heal).

// I could also react to percepts triggered by Happening directly:
// +poisoned(food)[source(Name)] <- .print("MY FOOD IS FUCKING DISGUSTING").

+stolen(food)[source(Name)] <- +hate(monkey).


// ASL Debug mode -> Run Configurations, duplicate Launcher, add -debug
