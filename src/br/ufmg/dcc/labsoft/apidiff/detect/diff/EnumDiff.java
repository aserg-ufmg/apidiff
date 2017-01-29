package br.ufmg.dcc.labsoft.apidiff.detect.diff;
import org.eclipse.jdt.core.dom.EnumDeclaration;

import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class EnumDiff {
	
	private int enumBreakingChange;
	private int enumNonBreakingChange;
	
	private int enumAdd;
	private int enumRemoval;
	private int enumModif;
	private int enumDeprecatedOp;
	private String library;

	public EnumDiff(){
		this.enumBreakingChange = 0;
		this.enumNonBreakingChange = 0;
		
		this.enumAdd = 0;
		this.enumRemoval = 0;
		this.enumModif = 0;
		this.enumDeprecatedOp = 0;
	}

	public int getEnunAdd() {
		return enumAdd;
	}

	public int getEnumRemoval() {
		return enumRemoval;
	}

	public int getEnumModif() {
		return enumModif;
	}

	public int getEnumDeprecatedOp() {
		return enumDeprecatedOp;
	}

	public void calculateDiff(String library, APIVersion version1, APIVersion version2) {
		this.library = library;
		this.findAddedEnums(version1, version2);
		this.findRemovedEnums(version1, version2);
		this.findChangedVisibilityEnums(version1, version2);
		this.findAddedDeprecatedEnums(version1, version2);
	}

	private void findAddedDeprecatedEnums(APIVersion version1, APIVersion version2) {
		for(EnumDeclaration acessibleEnumVersion2 : version2.getApiAccessibleEnums()){
			if(acessibleEnumVersion2.resolveBinding() != null && 
					acessibleEnumVersion2.resolveBinding().isDeprecated()){
				EnumDeclaration accessibleEnumVersion1 = version1.getVersionAccessibleEnum(acessibleEnumVersion2);
				if(accessibleEnumVersion1 == null){
					this.enumNonBreakingChange++;
				} else {
					if(accessibleEnumVersion1.resolveBinding() != null && 
							!accessibleEnumVersion1.resolveBinding().isDeprecated())
						this.enumNonBreakingChange++;
				}
			}
		}
	}

	private void findChangedVisibilityEnums(APIVersion version1, APIVersion version2) {
		for(EnumDeclaration acessibleEnumVersion1 : version1.getApiAccessibleEnums()){
			if(version2.contaisNonAccessibleEnum(acessibleEnumVersion1)){
				if(acessibleEnumVersion1.resolveBinding() != null &&
						acessibleEnumVersion1.resolveBinding().isDeprecated()){
					this.enumNonBreakingChange++;
					this.enumDeprecatedOp++;
				} else {
					this.enumBreakingChange++;
					this.enumModif++;
					
					System.out.println(this.library + ";" + acessibleEnumVersion1.resolveBinding().getQualifiedName() + 
							";" + acessibleEnumVersion1.getName() + ";" + "LOST VISIBILITY");
				}
			}
		}

		for(EnumDeclaration nonAcessibleEnumVersion1 : version1.getApiNonAccessibleEnums()){
			if(version2.contaisAccessibleEnum(nonAcessibleEnumVersion1)){
				this.enumNonBreakingChange++;
			}
		}
	}

	private void findAddedEnums(APIVersion version1, APIVersion version2) {
		for(EnumDeclaration enumVersion2 : version2.getApiAccessibleEnums()){
			if(version1.getVersionAccessibleEnum(enumVersion2) == null && 
					version1.getVersionNonAccessibleEnum(enumVersion2) == null){
				this.enumNonBreakingChange++;
				this.enumAdd++;
			}
		}
	}

	private void findRemovedEnums(APIVersion version1, APIVersion version2){
		for(EnumDeclaration enumVersion1 : version1.getApiAccessibleEnums()){
			if(version2.getVersionAccessibleEnum(enumVersion1) == null && 
					version2.getVersionNonAccessibleEnum(enumVersion1) == null){
				if(enumVersion1.resolveBinding() != null && 
						enumVersion1.resolveBinding().isDeprecated()){
					this.enumNonBreakingChange++;
					this.enumDeprecatedOp++;
				} else{
					this.enumBreakingChange++;
					this.enumRemoval++;
					
					System.out.println(this.library + ";" + enumVersion1.resolveBinding().getQualifiedName() + 
							";" + enumVersion1.getName() + ";" + "REMOVED ENUM");
				}
			}
		}
	}

	public int getEnumBreakingChange() {
		return enumBreakingChange;
	}

	public int getEnumNonBreakingChange() {
		return enumNonBreakingChange;
	}
}
