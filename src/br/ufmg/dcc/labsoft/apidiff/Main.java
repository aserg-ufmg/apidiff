package br.ufmg.dcc.labsoft.apidiff;
import java.io.File;

import br.ufmg.dcc.labsoft.apidiff.detect.diff.APIDiff;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class Main {

	public static void main(String[] args) {
//		if(args.length < 3){
//			System.err.println("[ERROR] Missing input parameters.");
//			System.err.println("\nusage: java -jar APIDiff.jar <name of library> <path of the old library version> <path of the new library version>");
//			System.exit(0);;
//		}
//		
//		File path1 = new File(args[1]);
//		File path2 = new File(args[2]);
//		String library = args[0];
		
		File path1 = new File("/media/aline/Dados/Aline/projetos/wsc_ufmg/wsc_apiBreakingChange/APIChangeTypeFinderSanner/scripts/result/rq1/result/RxJava_older");
		File path2 = new File("/media/aline/Dados/Aline/projetos/wsc_ufmg/wsc_apiBreakingChange/APIChangeTypeFinderSanner/scripts/result/rq1/dataset/RxJava");
		String library = "RxJava";
		
				
		APIVersion version1 = new APIVersion(path1);
		APIVersion version2 = new APIVersion(path2);
		APIDiff diff = new APIDiff(library, version1, version2); //versao mais antiga - versao mais nova
		diff.calculateDiff();
	}

}
