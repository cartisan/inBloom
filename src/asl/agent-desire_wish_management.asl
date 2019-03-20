/******************************************************************************/
/***** obligations*************************************************************/
/******************************************************************************/

// adds desire to fulfill obligation
+obligation(Plan) <-				
	!obligation(Plan).
	
// removes desire to fulfill obligation, and obligation itself
-obligation(Plan) <-
	.drop_desire(obligation(Plan));
	.succeed_goal(obligation(Plan)).

// only do hard obligations if really conscientious
@obligation1[affect(personality(conscientiousness,high))]
+!obligation(Plan) : is_work(Plan) <-		
	!Plan; 
	!obligation(Plan).

// generally do obligations if its not much work
+!obligation(Plan) : not is_work(Plan) <-
	!Plan; 
	!obligation(Plan).

// universal fallback: obligations remain desirable even when unfulfillable atm
+!obligation(Plan) : true <-		 
	!obligation(Plan).
-!obligation(Plan) <-
	!obligation(Plan).	

/******************************************************************************/
/***** wishes  ****************************************************************/
/******************************************************************************/

// adds desire to fulfill wish
+wish(Plan) <-
	!wish(Plan).

// removes desire to fulfill wish, and wish	
-wish(Plan) <-
	.drop_desire(wish(Plan));
	.succeed_goal(wish(Plan)).

// if conscientious do not follow (non-coping) wishes when an obligation is desired --> coping wishes, like !punish, don't fall under this
@wish1[affect(personality(conscientiousness,high))]
+!wish(Plan) : not coping_behavior(Plan) & obligation(Plan2) <-
	!wish(Plan).

// generally do wishes
+!wish(Plan) <-
	!Plan;
	!wish(Plan).

// universal fallback: wishes remain desirable even when unfulfillable atm
-!wish(Plan) <-
	!wish(Plan).