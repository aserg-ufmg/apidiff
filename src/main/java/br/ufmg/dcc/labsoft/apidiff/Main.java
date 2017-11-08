package br.ufmg.dcc.labsoft.apidiff;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.labsoft.apidiff.detect.diff.APIDiff;

public class Main {
	
	public static void main(String[] args) {
		
		Logger logger = LoggerFactory.getLogger(Main.class);
		
		if(args.length < 1){
			System.err.println("[ERROR] Missing input parameters.");
			System.err.println("\nusage: java -jar APIDiff.jar <CSV file>");
			System.exit(0);
		}
		
		String nameFile = args[0];
//		String nameFile = "/home/aline/utilitarios/workspaces/wsc_apiBreakingChange/APIDiff/src/main/resources/files/projects-tests.csv";
		try {
			Map<String, String> projects = UtilFile.readCSV(nameFile);
			int index = 1;
			for(String nameLibray: projects.keySet()){
				String url = projects.get(nameLibray);
				APIDiff diff = new APIDiff(nameLibray, url);
				logger.info("\n\n["+nameLibray+"][" + index + "/" + projects.size()+ "]");
				index++;
				diff.calculateDiffCommit();
			}
		} catch (IOException e) {
			logger.error("Erro ao ler arquivo com a lista de projetos.", e);
		}
	}

}
