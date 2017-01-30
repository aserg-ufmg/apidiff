package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class APIDiff {
	

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
		
		TypeDiff typediff = new TypeDiff();
		FieldDiff fieldDiff  = new FieldDiff();
		MethodDiff methodDiff = new MethodDiff();
		EnumDiff enumDiff = new EnumDiff();
		EnumConstantDiff enumConstantDiff = new EnumConstantDiff();
		
		this.resultType = typediff.calculateDiff(this.version1, this.version2);
		this.resultFild = fieldDiff.calculateDiff(this.version1, this.version2);
		this.resultMethod = methodDiff.calculateDiff(this.version1, this.version2);
		this.resultEnum = enumDiff.calculateDiff(this.version1, this.version2);
		this.resultEnumConstant = enumConstantDiff.calculateDiff(this.version1, this.version2);
		
		this.printOutput(resultType);
		this.printOutput(resultFild);
		this.printOutput(resultMethod);
		this.printOutput(resultEnum);
		this.printOutput(resultEnumConstant);
	}
	
	
	public void printOutput(Result r){
		System.out.println("Library;ChangedType;StructureName;Category");
		for(BreakingChange bc: r.getListBreakingChange()){
			System.out.println(this.library  + ";" + bc.getPath() + ";" + bc.getStruture() + ";" + bc.getCategory());
		}
	}

}
