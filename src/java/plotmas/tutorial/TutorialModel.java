package plotmas.tutorial;

import java.util.List;

import plotmas.LauncherAgent;
import plotmas.storyworld.PlotModel;

public class TutorialModel extends PlotModel<TutorialEnviroment> {
	
	public TutorialModel(List<LauncherAgent> agentList, TutorialEnviroment env) {
		super(agentList, env);
	}
	
	
	boolean [] state = new boolean[4];  // Felder mit Zustand dreckig oder sauber
	public int position; 						//Position des Agenten
		
	
	public void suck(){
		if (state[position] == true){
			state[position] = false;}  //true hei�t dreckig, false hei�t sauber! 
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

