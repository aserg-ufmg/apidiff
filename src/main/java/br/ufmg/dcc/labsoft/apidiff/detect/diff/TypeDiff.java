package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.TypeDeclaration;

import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class TypeDiff {
	
	private final String CATEGORY_CHANGED_SUPER_TYPE = "CHANGED SUPER TYPE";
	private final String CATEGORY_LOST_VISIBILITY = "LOST VISIBILITY";
	private final String CATEGORY_REMOVED_TYPE = "REMOVED TYPE";
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	private int typeBreakingChange;
	private int typeNonBreakingChange;
	private int typeAdd;
	private int typeRemoval;
	private int typeModif;
	private int typeDeprecatedOp;

	/**
	 * Calculates the diff for classes
	 * @param version1 older version of an API
	 * @param version2 newer version of an API
	 */
	public Result calculateDiff(final APIVersion version1, final APIVersion version2) {
		this.findRemovedTypes(version1, version2);
		this.findAddedTypes(version1, version2);
		this.findChangedVisibilityTypes(version1, version2);
		this.findAddedDeprecated(version1, version2);
		this.changedSuperTypes(version1, version2);
		
		Result result = new Result();
		result.setElementAdd(this.typeAdd);
		result.setElementDeprecated(this.typeDeprecatedOp);
		result.setElementModified(this.typeModif);
		result.setElementRemoved(this.typeRemoval);
		result.setListBreakingChange(this.listBreakingChange);
		result.setBreakingChange(typeBreakingChange);
		result.setNonBreakingChange(typeNonBreakingChange);
		return result;
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
					this.listBreakingChange.add(new BreakingChange(accessibleTypeVersion1.resolveBinding().getQualifiedName(), accessibleTypeVersion1.getName().toString(), this.CATEGORY_CHANGED_SUPER_TYPE));
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
					this.listBreakingChange.add(new BreakingChange(acessibleTypeVersion1.resolveBinding().getQualifiedName(), acessibleTypeVersion1.getName().toString(), this.CATEGORY_LOST_VISIBILITY));
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
					this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), type.getName().toString(), this.CATEGORY_REMOVED_TYPE));
				}
			}
		}
	}
}
