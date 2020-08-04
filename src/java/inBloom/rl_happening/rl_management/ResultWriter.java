package inBloom.rl_happening.rl_management;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import inBloom.graph.isomorphism.FunctionalUnit;
import inBloom.graph.isomorphism.FunctionalUnits;
import inBloom.helper.Tellability;
import inBloom.storyworld.Happening;

import java.io.FileWriter;

/**
 * 
 * 
 * @author Julia Wippermann
 * @version 03.08.20
 *
 */
public class ResultWriter {

	public final String episodesFile = "training8.csv";
	public final String plotFile = "plotText.csv";
		
	private int episode;
	
	
	// TODO delete RLCycle from this, just for log purposes
	public ResultWriter() {
		
		this.episode = 1;
		
		createFile(episodesFile);
		createFile(plotFile);
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
				
		String message = "";
		message += "Episode"				+ ",";
		message += "Tellability"			+ ",";
		message += "#Steps"					+ ",";
		message += "#FunctionalUnits"		+ ",";
		message += "#PolyvalentVertices"	+ ",";
		message += "Suspense"				+ ",";
		
		// Add Functional Units
		for(FunctionalUnit unit : FunctionalUnits.ALL) {
			message += unit + ",";
		}
		
		for(String feature: weights.keySet()) {
			for(Happening<?> happening: weights.get(feature).keySet()) {
				message += feature + "-" + happening + ",";
			}
		}
		
		message = deleteLastChar(message);
		
		message += "\n";
		
		writeToFile(episodesFile, message);
	}
	
	public void writeResultOfEpisode(double tellabilityValue, Tellability tell, SarsaLambda sarsa) {
		
		int steps = sarsa.step;
		int nrFU = tell.numFunctionalUnits;
		int nrPoly = tell.numPolyvalentVertices;
		int suspense = tell.suspense;
		Map<FunctionalUnit, Integer> fuCount = tell.functionalUnitCount;
		HashMap<String,HashMap<Happening<?>, Double>> weights = sarsa.weights;
		
		String message = "";
		message += episode			+ ",";
		message += tellabilityValue	+ ",";
		message += steps			+ ",";
		message += nrFU				+ ",";
		message += nrPoly			+ ",";
		message += suspense			+ ",";
		
		// Print Values of all functional Units
		for(FunctionalUnit unit: fuCount.keySet()) {
			message += fuCount.get(unit) + ",";
		}
	    
		
		for(String feature: weights.keySet()) {
			for(Happening<?> happening: weights.get(feature).keySet()) {
				double weight = weights.get(feature).get(happening);
				message += weight;
				message += ",";
			}
		}

		// Delete last character of String (unnecessary ',')
		message = deleteLastChar(message);
		
		// Start new line for new episode
		message += "\n";
		
		writeToFile(episodesFile, message);
		
		episode++;
	}

	
	
	public void writeTitlesOfPlot() {
		String message = "";
		message += "Episode" + ",";
		message += "Selected Happenings" + ",";
		message += "Actived Features" + ",";
		message += "Plot Text";
		message += "\n";
		
		writeToFile(plotFile, message);
	}
	
	public void writePlotStep() {
		
	}
	
	
	private String deleteLastChar(String message) {
		return message.substring(0, message.length()-1);
	}


}
