package br.ufmg.dcc.labsoft.apidiff;
import java.io.File;

import br.ufmg.dcc.labsoft.apidiff.detect.diff.APIDiff;

public class Main {

	public static void main(String[] args) {
		if(args.length < 3){
			System.err.println("[ERROR] Missing input parameters.");
			System.err.println("\nusage: java -jar APIDiff.jar <name of library> <path of the old library version> <path of the new library version>");
			System.exit(0);;
		}
		
		File path1 = new File(args[1]);
		File path2 = new File(args[2]);
		String library = args[0];
		
		APIDiff diff = new APIDiff(library, path1, path2); //versao mais antiga - versao mais nova
		diff.calculateDiff();
		
	}

}
