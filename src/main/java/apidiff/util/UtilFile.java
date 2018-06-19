package apidiff.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilFile {
	
	/**
	 * Writing the list in the CSV files, a position for line.
	 * @param fileName - File name (i.e., "output.csv")
	 * @param listMsg - Message list
	 */
	public static void writeFile(final String fileName, final List<String> listMsg){
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter (new FileOutputStream(fileName, true), "utf-8"));
			for(String msg: listMsg){
				writer.write(msg + "\n");
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("Error writing results in the output file [" + fileName + "]. " + e);
		} 
	}
	
	/**
	 * Converting a CSV file with  two columns to a map. The separator is ";".
	 * For example, "nameProject;URL" = {{"nameProject" : "URL"}}
	 * @param fileName - File name (i.e., "output.csv")
	 * @return - map
	 * @throws IOException - Exception for file operations
	 */
	public static Map<String, String> convertCSVFileToMap(final String fileName) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String SEPARATOR = ";";
		String line = "";
		Map<String, String> result = new HashMap<String, String>();
		try {
		    while ((line = br.readLine()) != null){
		    	String [] data = line.split(SEPARATOR);
		    	if(data.length == 2){
		    		result.put(data[0], data[1]);
		    	}
		    	else{
		    		System.err.println("File format invalid! " + line);
		    	}
		    }
		}
		finally {
		    br.close();
		}
		return result;
	}
	
	/**
	 * Converting a CSV file to a list of maps. The separator is ";".
	 * The first line defines the header.
	 * The first column defines the map key.
	 * 
	 * CSV:
	 * nameproject;URL;
	 * aserg-ufmg/apidiff;https://github.com/aserg-ufmg/apidiff.git
	 * aserg-ufmg/RefDiff;https://github.com/aserg-ufmg/RefDiff.git
	 * 
	 * Output:
	 * [
	 * 	{namepProject=aserg-ufmg/apidiff, URL=https://github.com/aserg-ufmg/apidiff.git},
	 * 	{namepProject=aserg-ufmg/RefDiff, URL=https://github.com/aserg-ufmg/RefDiff.git}
	 * ]
	 * 
	 * @param fileName - File name (i.e., "output.csv")
	 * @return - list of maps
	 * @throws IOException - Exception for file operations
	 */
	public static List<Map<String, String>> convertCSVFileToListofMaps(final String fileName) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String SEPARATOR = ";";
		String line = "";
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		try {
			String[] header = br.readLine().split(SEPARATOR);
		    while ((line = br.readLine()) != null){
		    	String [] data = line.split(SEPARATOR);
		    	Map<String, String> value = new HashMap<String, String>();
		    	for(int i = 0; i < data.length; i++){
	    			value.put(header[i], data[i]);
	    		}
		    	result.add(value);
		    }
		}
		finally {
		    br.close();
		}
		return result;
	}
}
