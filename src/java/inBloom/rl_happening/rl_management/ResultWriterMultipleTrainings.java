package inBloom.rl_happening.rl_management;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import inBloom.storyworld.Happening;

import java.io.FileWriter;

/**
 * 
 * 
 * @author Julia Wippermann
 * @version 29.7.20
 *
 */
public class ResultWriterMultipleTrainings {

	public String episodesFile;
	// public final 
		
	private int episode;
	
	
	// TODO delete RLCycle from this, just for log purposes
	public ResultWriterMultipleTrainings(String fileName) {
		
		this.episode = 1;
		this.episodesFile = fileName;
		
		createFile(episodesFile);
	}

	
	
	
	public void createFile(String filename) {
		try {
			System.out.println(filename);
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
		message += tellability;
		for(String feature: weights.keySet()) {
			for(Happening<?> happening: weights.get(feature).keySet()) {
				message += ",";
				double weight = weights.get(feature).get(happening);
				message += weight;
			}
		}
		message += "\n";
		
		writeToFile(episodesFile, message);
		
		episode++;
	}



}
