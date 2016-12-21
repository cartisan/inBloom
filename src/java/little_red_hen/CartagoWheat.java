package little_red_hen;

import java.util.HashMap;

import cartago.*;



public class CartagoWheat extends CollaborationArtifact {
	
	public static enum STATE {SEED, GROWING, RIPE, HARVESTED, FLOUR;}
	static final int EFFORT = 4000;
	public STATE state;
	
	public CartagoWheat(){
		this.state = STATE.SEED;
	}
	
	@OPERATION
	void plant() throws IllegalStateException, InterruptedException{
		// can only plant if seed
		if (!(this.state == STATE.SEED)) {
			throw new IllegalStateException();
		}
		
		Thread.sleep(EFFORT / (this.collaborators + 1));
		this.state = STATE.GROWING;
		signal("wheat planted");
	}
	
	@OPERATION
	void tend() throws InterruptedException{
		// can only tend if planted
		if (!(this.state == STATE.GROWING)) {
			throw new IllegalStateException();
		}
		
		Thread.sleep(EFFORT / (this.collaborators + 1));
		this.state = STATE.RIPE;
		signal("wheat tended");
	}
	
	@OPERATION 
	void harvest() throws InterruptedException{
		// can only harvest if ripe
		if (!(this.state == STATE.RIPE)) {
			throw new IllegalStateException();
		}
		
		Thread.sleep(EFFORT / (this.collaborators + 1));
		this.state = STATE.HARVESTED;
		signal("wheat harvested");
	}
	
	@OPERATION 
	void grind() throws InterruptedException{
		// can only grind if harvested
		if (!(this.state == STATE.HARVESTED)) {
			throw new IllegalStateException();
		}
		
		Thread.sleep(EFFORT / (this.collaborators + 1));
		this.state = STATE.FLOUR;
		signal("wheat milled");
	}
	
}
