// Agent liz in project testenv.mas2j

// (default) means it is there from the beginning.
// Not dynamically added during execution




/* Initial beliefs and rules */

// Beliefs are added / removed to the belief base
//
// 1. EXTERNALLY (in Environment):	ADD TO ONE AGENTS:
//												- addPercept(agent_name, belief_name)
//												- removePercept(agent_name, belief_name)
//												- removePerceptsByUnif(agent_name, belief_name)
//												ADD TO ALL AGENTS:
//												- leave out agent_name
//
// 2. INTERNALLY (in Agent):		 	+belief_name;
//												-belief_name;

// a (default) general belief
is_sunny.
not_fire.

// a (default) specific belief
is_good(dr_who).




/* Initial goals */

// a (default) goal
!start.



/* Plans */

// a plan triggered by a goal:
// @plan_name[list_of_annotations]
// +!goal_name : if_precondition <- then_plan_body

// a plan triggered by a percept / belief:
// @plan_name[list_of_annotations]
// +percept/belief_name : if_precondition <- then_plan_body

// This plan is triggered when the goal START has been perceived
+!start : true <- burn.

// This plan is triggered when the percept FIRE has been perceived
// That is the belief FIRE has been added to the belief base
+fire <- run.



/* What are Actions? -> Usually in plan body */

// end with ;

// environment actions	-> can change state of environment
//		env_action;			-> implemented in the Environment in executeAction (bzw. doExecuteAction for Leonid)
//
// internal actions   	-> start with dot / have dot in their name
//		.int_action;		-> given by jason
//								-> e.g. print, random, range
//								-> AFFECTIVE INTERNAL ACTION:
//									.appraise_emotion(emotion, target)
//								-> COMMUNICATION BETWEEN AGENTS
//									.send(target, speech_act_type, content)

// burn is an environment action -> it changes the state of the environment
// burn itself will be implemented in the environment class -> and also in the Model


// SYNTAX:
//
// action;			-> environment action
// .action;			-> internal action (predefined by jason)
// if(belief) {	-> if-statemanet
// 	action;
//	}


// Preconditions:
// - beliefs
// - &, not
// - is there an or-operator? TODODO
