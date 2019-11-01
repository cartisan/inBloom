// Agent agentJulia in project plotmas

/* Initial beliefs and rules */
is_okay(everything).

/* Initial goals */

!default_activity.



/* Plans */

@obligationStart_1[affect((personality(neuroticism,high)))]
+!default_activity : is_okay(everything) <- print("I will burn!"); burn.

//until you find sth better, perform default activity
@obligationStart_2
+!default_activity : not burning <- smile;
				!default_activity.
				
@obligationStart_3
+!default_activity : burning <- .succeed_goal(default_activity).

@fire_plan_1[affect(personality(conscientiousness,high))]
+!react_to_fire <- extinguishFire.

@fire_plan_2[affect(mood(pleasure, negative))]
+!react_to_fire <- run.

@fire_plan_3
+!react_to_fire <- scream.


/* React to new Beliefs / Percepts (TODO: equal? -> see old code for def_act) */

+burning[source(Name)] <- print("It is burning!");
				.suspend(default_activity);
				.appraise_emotion(fear, Name, "burning[source(Name)]", true);
				!react_to_fire.

+been_saved[source(Name)] <- .appraise_emotion(relief, Name, "been_saved[source(Name)]", true).


// pause    a goal via .suspend(goal).
// continue a goal via .resume(goal).
// end		a goal via .succeed_goal(goal).
//							  .fail_goal(goal).
//	remove  beliefs via .abolish(belief)
//							  .abolish(belief(_))