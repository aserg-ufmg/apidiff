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
	 * Imprime a lista em uma arquivo. Cada item em uma linha.
	 * @param nameFile
	 * @param listMsg
	 */
	public static void writeFile(final String nameFile, final List<String> listMsg){
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter (new FileOutputStream(nameFile, true), "utf-8"));
			for(String msg: listMsg){
				writer.write(msg + "\n");
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("Erro ao escrever resultado no arquivo de saida. " + e);
		} 
	}
	
	/**
	 * Lê as linhas do arquivo CSV com a lista dos projetos.
	 * Formato esperado: nome do projeto; url do projeto.
	 * 
	 * Os projetos clonados são armazenados no caminho definido na variável PATH_PROJECT.
	 * @param nameFile
	 * @throws IOException
	 */
	public static Map<String, String> readCSV(final String nameFile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(nameFile));
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
	 * Transforma um csv numa lista de maps.
	 * A primeira linha define os nomes dos cabeçalhos.
	 * A primeira coluna define a chave do map.
	 * @param nameFile
	 * @return
	 * @throws IOException
	 */
	public static List<Map<String, String>> csvToMapList(final String nameFile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(nameFile));
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
