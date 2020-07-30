package inBloom.rl_happening.rl_management;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import inBloom.storyworld.Happening;

import java.io.FileWriter;

/**
 * 
 * 
 * @author Julia Wippermann
 * @version 29.7.20
 *
 */
public class ResultWriter {

	public final String episodesFile = "episodes3.csv";
	
	private ReinforcementLearningCycle rl;
	
	private int episode;
	
	
	// TODO delete RLCycle from this, just for log purposes
	public ResultWriter(ReinforcementLearningCycle rl) {
		episode = 1;
		this.rl = rl;
		//if(!f.exists()) { 
		    createFile(episodesFile);
		//}
	}

	
	
	
	public void createFile(String filename) {
		try {
			File myObj = new File(filename);
			myObj.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeToFile(String filename, String message) {
		try {
			FileWriter myWriter = new FileWriter(episodesFile, true);
			myWriter.append(message);
			myWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	
	
	public void writeTitlesOfEpisodes(HashMap<String,HashMap<Happening<?>, Double>> weights) {
		
		rl.log("TIIIIIITKTLTLTLLLLLLEEEEE");
		
		String message = "Episode" + ",";
		message += "Tellability" + ",";
		for(String feature: weights.keySet()) {
			for(Happening<?> happening: weights.get(feature).keySet()) {
				message += feature + "-" + happening + ",";
			}
		}
		message += "\n";
		
		writeToFile(episodesFile, message);
	}
	
	public void writeResultOfEpisode(double tellability, HashMap<String,HashMap<Happening<?>, Double>> weights) {
				
		String message = episode + ",";
		message += tellability + ",";
		for(String feature: weights.keySet()) {
			for(Happening<?> happening: weights.get(feature).keySet()) {
				double weight = weights.get(feature).get(happening);
				message += weight + ",";
			}
		}
		message += "\n";
		
		writeToFile(episodesFile, message);
		
		episode++;
	}



}
