package br.ufmg.dcc.labsoft.apidiff;
import br.ufmg.dcc.labsoft.apidiff.detect.diff.APIDiff;

public class Main {

	public static void main(String[] args) {
		if(args.length < 2){
			System.err.println("[ERROR] Missing input parameters.");
			System.err.println("\nusage: java -jar APIDiff.jar <name of library> <path of the old library version> <path of the new library version>");
			System.exit(0);;
		}
		
		String nameLibray = args[0];
		String url = args[1];
		
		//APIDiff diff = new APIDiff("api-breaking-changes-toy-example", "https://github.com/alinebrito/api-breaking-changes-toy-example.git");
		APIDiff diff = new APIDiff(nameLibray, url);
		diff.calculateDiff();
		
	}

}
