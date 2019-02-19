package plotmas.tutorial;

import java.util.List;

import plotmas.LauncherAgent;
import plotmas.PlotModel;
import plotmas.storyworld.HappeningDirector;

public class TutorialModel extends PlotModel<TutorialEnviroment> {
	//static Logger logger = Logger.getLogger(PlotModel.class.getName());
	
	public int position;
	public boolean[] dirty = { true, true, true, true };   // all dirty
           
	
	public TutorialModel(List<LauncherAgent> agents, HappeningDirector hapDir) {
		super(agents, hapDir);
		this.position = 0;
	}
	
	
	public boolean suck() {
		if (dirty[position] == true){
			dirty[position] = false;
			logger.info( "Floor is now cleaned at position " +(position+1));
			return true;
		}
		return false;
	}	
	
	public boolean left() {
		if (position == 1){
			position = 2;
			logger.info("Robot is now at position "+(position+1));
			return true;
		}
		else if (position == 3){
			this.position = 2;
			logger.info("Robot is now at position "+(position+1));
			return true;
		}
		return false;
	}
	
	public boolean right() {
		if (position == 0){
			position = 1;
			logger.info("Robot is now at position "+(position+1));
			return true;
		}
		else if (position == 2){
			position = 3;
			logger.info("Robot is now at position "+(position+1));
			return true;
		}
		return false;
	}
	
	public boolean up() {
		if (position == 2){
			position = 0;
			logger.info("Robot is now at position "+(position+1));
			return true;
		}
		else if (position == 3){
			position = 1;
			logger.info("Robot is now at position "+(position+1));
			return true;
		}
		return false;
	}
	
	public boolean down() {
		if (position == 0){
			position = 2;
			logger.info("Robot is now at position "+(position+1));
			return true;
		}
		else if (position == 1){
			position = 3;
			logger.info("Robot is now at position "+(position+1));
			return true;
		}
		return false;
	}

}
