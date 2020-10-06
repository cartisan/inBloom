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
					 
+!happyEnd <- 	happyEnd;
				-wish(happyEnd).
				
+!getRescued <- goOnShip;
				-wish(getRescued).

@rescueSelf0[priority(2)] 
+!rescueSelf <- swimToIsland;
				-wish(rescueSelf).
				
	
//@heal0[priority(1)] 	
//+!heal <- if(has(healingPlant)) {
//			useHealingPlants;
//			-wish(heal);
//		} else {
//			findHealingPlants;
//		}.

@heal0[priority(1)] 	
+!heal : not(has(healingPlant)) <-
			findHealingPlants;
			useHealingPlants;
			-wish(heal).
			
@heal1[priority(1)] 	
+!heal : has(healingPlant) <-
			useHealingPlants;
			-wish(heal).

//@food_plan	
//+!eat <- if(has(food)) {
//			eat;
//			-wish(eat);
//		} else {
//			getFood;
//		}.
		
@eat0[priority(1)] 
+!eat : not(has(food)) <- 
		getFood;
		eat;
		-wish(eat).
		
@eat1[priority(1)] 
+!eat : has(food) <-
		eat;
		-wish(eat).
		  
//@sleep0[priority(1)] 
//+!sleep <- if(exists(hut)) {
//				sleep;
//				-wish(sleep);
//				// TODO - only if they wish to heal?
//				// 1. is it necessary?
//				// 2. how do I do knowledge abfrage?
//				// find out object of belief:    ?belief(X)
//				// find out existence of belief: if(belief)
//				//-wish(heal);
//		   } else {
//		   		buildHut;
//		   }.
	
@sleep0[priority(1)] 	   
+!sleep : not(exists(hut)) <-
				buildHut;
				sleep;
				-wish(sleep).
		
@sleep1[priority(1)] 		
+!sleep : exists(hut) <-
				sleep;
				-wish(sleep).


@extinguish_fire0[priority(2)] 
+!extinguish_fire <- extinguishFire;
					 -wish(extinguish_fire).
		  
//+!complain <- if(has(friend)) {
//				complain;
//				-wish(complain);
//			  } else {
//				findFriend;
//			  }.
			  
+!complain : not(has(friend)) <-
				findFriend;
				complain;
				-wish(complain).
				
+!complain : has(friend) <-
				complain;
				-wish(complain).


/* React to new Belifes / Percepts */
// Name of source is f.e. "percept"

+shipWrecked[source(Name)] <- +wish(rescueSelf).

+endStory[source(Name)] <- +wish(happyEnd).

+rescueEnd[source(Name)] <- +wish(getRescued).

+sick[source(Name)] <- +wish(heal).

+hungry[source(Name)] <- +wish(eat).

+fatigue[source(Name)] <- +wish(sleep).

// not used so far
+stolen(food)[source(Name)] <- +hate(monkey).

// if f.e. friend is eaten, then agent has no friend anymore :(
+eaten(X)[source(Name)] <- -has(X).

+homesick[source(Name)] <- +wish(complain).

+fire[source(Name)] <- +wish(extinguish_fire).

// TODO why does this not print anything?
+happening[source(Name)] <- .print("A HAPPENING HAPPENED.").


// ASL Debug mode -> Run Configurations, duplicate Launcher, add -debug
