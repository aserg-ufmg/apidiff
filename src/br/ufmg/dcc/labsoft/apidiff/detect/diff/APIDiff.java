package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.labsoft.apidiff.UtilFile;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class APIDiff {
	

	private final String nameFile = "output.csv";
	
	private String library;
	private APIVersion version1;
	private APIVersion version2;
	
	private Result resultType; 
	private Result resultFild;
	private Result resultMethod;
	private Result resultEnum;
	private Result resultEnumConstant;

	public APIDiff(final String library, final APIVersion version1, final APIVersion version2) {
		this.library = library;
		this.version1 = version1;
		this.version2 = version2;
	}

	public void calculateDiff() {
		System.out.println("Processing Types... (Wait)");
		this.resultType = new TypeDiff().calculateDiff(this.version1, this.version2);
		
		System.out.println("Processing Filds... (Wait)");
		this.resultFild = new FieldDiff().calculateDiff(this.version1, this.version2);
		
		System.out.println("Processing Methods... (Wait)");
		this.resultMethod = new MethodDiff().calculateDiff(this.version1, this.version2);
		
		System.out.println("Method Enums... (Wait)");
		this.resultEnum = new EnumDiff().calculateDiff(this.version1, this.version2);
		
		System.out.println("Method Enuns Constant... (Wait)");
		this.resultEnumConstant = new EnumConstantDiff().calculateDiff(this.version1, this.version2);
		
		this.print();//Escreve saída em arquivo.
		System.out.println("Finished processing. Check the output file <" + this.nameFile + ">");
	}
	
	/**
	 * Imprime resultado em um arquivo CSV.
	 */
	private void print(){
		
		List<String> result =  new ArrayList<String>();
		
		//Breaking changes e non-breaking changes contabilizadas.
		result.add("Structure;Added;Removed;Modified;Depreciated");
		result.add(this.printCount(this.resultEnum, "Enum"));
		result.add(this.printCount(this.resultEnumConstant, "EnumConstant"));
		result.add(this.printCount(this.resultFild, "Fild"));
		result.add(this.printCount(this.resultMethod, "Method"));
		result.add(this.printCount(this.resultType, "Type"));
		
		//Lista de Breaking Changes.
		result.add("Library;ChangedType;StructureName;Category");
		result.addAll(this.printListBreakingChange(this.resultType));
		result.addAll(this.printListBreakingChange(this.resultFild));
		result.addAll(this.printListBreakingChange(this.resultMethod));
		result.addAll(this.printListBreakingChange(this.resultEnum));
		result.addAll(this.printListBreakingChange(this.resultEnumConstant));
		
		UtilFile.writeFile(this.nameFile, result);
	}
	
	/**
	 * Imprime lista de breaking change detectadas.
	 * @param r
	 */
	private List<String> printListBreakingChange(Result r){
		List<String> list =  new ArrayList<String>();
		for(BreakingChange bc: r.getListBreakingChange()){
			list.add(this.library  + ";" + bc.getPath() + ";" + bc.getStruture() + ";" + bc.getCategory());
		}
		return list;
	}
	
	private String printCount(final Result result, final String struture){
		return struture + ";" + result.getElementAdd() + ";" + result.getElementRemoved() + ";" +result.getElementModified() + ";" +  result.getElementDeprecated();
	}

}
