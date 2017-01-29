package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class APIDiff {
	private TypeDiff typediff = new TypeDiff();
	private FieldDiff fieldDiff  = new FieldDiff();
	private MethodDiff methodDiff = new MethodDiff();
	private EnumDiff enumDiff = new EnumDiff();
	private EnumConstantDiff enumConstantDiff = new EnumConstantDiff();
	private String library;


	public APIDiff(String library, APIVersion version1, APIVersion version2) {
		this.library = library;
		this.calculateDiff(version1, version2);
	}

	private void calculateDiff(APIVersion version1, APIVersion version2) {
		this.typediff.calculateDiff(library, version1, version2);
		this.fieldDiff.calculateDiff(library, version1, version2);
		this.methodDiff.calculateDiff(library, version1, version2);
		this.enumDiff.calculateDiff(library, version1, version2);
		this.enumConstantDiff.calculateDiff(library, version1, version2);
	}
	
	public EnumConstantDiff getEnumConstantDiff() {
		return this.enumConstantDiff;
	}

	public TypeDiff getTypeDiff(){
		return this.typediff;
	}
	
	public FieldDiff getFieldDiff(){
		return this.fieldDiff;
	}
	

	public MethodDiff getMethodDiff() {
		return this.methodDiff;
	}
	
	public EnumDiff getEnumDiff() {
		return this.enumDiff;
	}

}
