package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import org.eclipse.jdt.core.dom.TypeDeclaration;

import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class TypeDiff {

	public int getTypeBreakingChange() {
		return typeBreakingChange;
	}

	public int getTypeNonBreakingChange() {
		return typeNonBreakingChange;
	}

	private int typeBreakingChange;
	private int typeNonBreakingChange;
	
	private int typeAdd;
	private int typeRemoval;
	private int typeModif;
	private int typeDeprecatedOp;
	private String library;

	public TypeDiff() {
		this.typeBreakingChange = 0;
		this.typeNonBreakingChange = 0;
		
		this.typeAdd = 0;
		this.typeRemoval = 0;
		this.typeModif = 0;
		this.typeDeprecatedOp = 0;
	}

	public int getTypeAdd() {
		return typeAdd;
	}

	public int getTypeRemoval() {
		return typeRemoval;
	}

	public int getTypeModif() {
		return typeModif;
	}

	public int getTypeDeprecatedOp() {
		return typeDeprecatedOp;
	}

	/**
	 * Calculates the diff for classes
	 * @param version1 older version of an API
	 * @param version2 newer version of an API
	 */
	public void calculateDiff(String library, APIVersion version1, APIVersion version2) {
		this.library = library;
		this.findRemovedTypes(version1, version2);
		this.findAddedTypes(version1, version2);
		this.findChangedVisibilityTypes(version1, version2);
		this.findAddedDeprecated(version1, version2);
		this.changedSuperTypes(version1, version2);
	}

	private void changedSuperTypes(APIVersion version1, APIVersion version2) {
		for(TypeDeclaration accessibleTypeVersion1 : version1.getApiAcessibleTypes()){
			TypeDeclaration accessibleTypeVersion2 = version2.getVersionAccessibleType(accessibleTypeVersion1);
			if(accessibleTypeVersion2 != null){
				String super1 = null; 
				String super2 = null;
				if(accessibleTypeVersion1.resolveBinding() != null &&
						accessibleTypeVersion1.resolveBinding().getSuperclass() != null){
					super1 = accessibleTypeVersion1.resolveBinding().getSuperclass().getQualifiedName().toString();
				}
				if(accessibleTypeVersion2.resolveBinding() != null &&
						accessibleTypeVersion2.resolveBinding().getSuperclass() != null){
					super2 = accessibleTypeVersion2.resolveBinding().getSuperclass().getQualifiedName().toString();
				}
				if(super1 != null && super2 != null && !super1.equals(super2)){
					this.typeBreakingChange++; //changed super type
					this.typeModif++;
					System.out.println(this.library + ";" + accessibleTypeVersion1.resolveBinding().getQualifiedName() + 
							";" + accessibleTypeVersion1.getName() + ";" + "CHANGED SUPER TYPE");
				}
			}
		}
	}

	private void findAddedDeprecated(APIVersion version1, APIVersion version2) {
		for(TypeDeclaration accessibleTypeVersion1 : version1.getApiAcessibleTypes()){
			TypeDeclaration accessibleTypeVersion2 = version2.getVersionAccessibleType(accessibleTypeVersion1);
			if(accessibleTypeVersion2 != null){
				if(accessibleTypeVersion1.resolveBinding() != null && accessibleTypeVersion2.resolveBinding() != null){
					if(!accessibleTypeVersion1.resolveBinding().isDeprecated() && accessibleTypeVersion2.resolveBinding().isDeprecated())
						this.typeNonBreakingChange++; //added deprecated
				}
			}
		}

		for(TypeDeclaration accessibleTypeVersion2 : version2.getApiAcessibleTypes()){
			if(!version1.contaisAccessibleType(accessibleTypeVersion2) && 
					accessibleTypeVersion2.resolveBinding() != null &&
					accessibleTypeVersion2.resolveBinding().isDeprecated())
				this.typeNonBreakingChange++; //added deprecated
		}
	}

	private void findChangedVisibilityTypes(APIVersion version1, APIVersion version2) {
		for(TypeDeclaration acessibleTypeVersion1 : version1.getApiAcessibleTypes()){
			if(version2.contaisNonAccessibleType(acessibleTypeVersion1)){
				if(acessibleTypeVersion1.resolveBinding() != null && 
						acessibleTypeVersion1.resolveBinding().isDeprecated()){
					this.typeNonBreakingChange++; //lost visibility deprecated
					this.typeDeprecatedOp++;
				} else {
					this.typeBreakingChange++; //lost visibility
					this.typeModif++;
					System.out.println(this.library + ";" + acessibleTypeVersion1.resolveBinding().getQualifiedName() + 
							";" + acessibleTypeVersion1.getName() + ";" + "LOST VISIBILITY");
				}
			}
		}

		for(TypeDeclaration nonAcessibleTypeVersion1 : version1.getApiNonAcessibleTypes()){
			if(version2.contaisAccessibleType(nonAcessibleTypeVersion1))
				this.typeNonBreakingChange++; //gained visibility
		}
	}

	private void findAddedTypes(APIVersion version1, APIVersion version2) {
		for (TypeDeclaration type : version2.getApiAcessibleTypes()) {
			if(!version1.contaisAccessibleType(type) && !version1.contaisNonAccessibleType(type)){
				this.typeNonBreakingChange++; //added type
				this.typeAdd++;
			}
		}
	}

	private void findRemovedTypes(APIVersion version1, APIVersion version2) {
		for (TypeDeclaration type : version1.getApiAcessibleTypes()) {
			if(!version2.contaisAccessibleType(type) && !version2.contaisNonAccessibleType(type)){
				if(type.resolveBinding() != null &&
						type.resolveBinding().isDeprecated()){
					this.typeNonBreakingChange++; //removed deprecated
					this.typeDeprecatedOp++;
				}else{
					this.typeBreakingChange++; //removed
					this.typeRemoval++;
					System.out.println(this.library + ";" + type.resolveBinding().getQualifiedName() + 
							";" + type.getName() + ";" + "REMOVED TYPE");
				}
			}
		}
	}
}
