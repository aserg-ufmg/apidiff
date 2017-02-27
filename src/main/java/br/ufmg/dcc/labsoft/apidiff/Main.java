package br.ufmg.dcc.labsoft.apidiff;
import br.ufmg.dcc.labsoft.apidiff.detect.diff.APIDiff;

public class Main {

	public static void main(String[] args) {
		if(args.length < 3){
			System.err.println("[ERROR] Missing input parameters.");
			System.err.println("\nusage: java -jar APIDiff.jar <nameLibrary> <urlGitHub> <commitId>");
			System.exit(0);;
		}
		
		String nameLibray = args[0];
		String url = args[1];
		String commitId = args[2];
		
//		Testes
//		String nameLibray = "APIDiffExamplesBreakingChange";
//		String url = "https://github.com/alinebrito/APIDiffExamplesBreakingChange.git";
//		String commitId = "";
		
		APIDiff diff = new APIDiff(nameLibray, url, commitId);
		diff.calculateDiff();
		
	}

}
