package plotmas.tutorial;

import java.util.List;

import java.util.logging.Logger;

import plotmas.PlotEnvironment;
import plotmas.PlotLauncher.LauncherAgent;
import plotmas.storyworld.Model;

public class TutorialModel extends Model {
	
	static Logger logger = Logger.getLogger(TutorialModel.class.getName());
	
	public TutorialModel(List<LauncherAgent> agentList, PlotEnvironment<TutorialModel> env) {
		super(agentList, env);
	}
	
	
	boolean [] state = new boolean[4];  // Felder mit Zustand dreckig oder sauber
	public int position; 						//Position des Agenten
		
	
	public void suck(){
		if (state[position] == true){
			state[position] = false;}  //true heiﬂt dreckig, false heiﬂt sauber! 
			logger.info("position " + String.valueOf(position) + " is clean");
		}
	
	
	public void down() {
		if (position == 0){
				position = 3;
		}
		
		if (position == 1) {
				position =2;}
		}
	
	public void left() {
		if (position == 1){
				position = 0;
		}
		
		if (position == 2) {
				position =3;}
		}
	
	public void right() {
		if (position == 0){
				position = 1;
		}
		
		if (position == 3) {
				position =2;}
		}
	
	public void up() {
		if (position == 2){
				position = 1;
		}
		
		if (position == 3) {
				position =0;}
		}
}

