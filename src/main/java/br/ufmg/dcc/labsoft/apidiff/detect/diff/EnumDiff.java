package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.EnumDeclaration;

import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class EnumDiff {
	
	private final String CATEGORY_LOST_VISIBILITY = "LOST_VISIBILITY";
	private final String CATEGORY_REMOVED_ENUM = "REMOVED ENUM";
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	private int enumBreakingChange;
	private int enumNonBreakingChange;
	private int enumAdd;
	private int enumRemoval;
	private int enumModif;
	private int enumDeprecated;

	public Result calculateDiff(final APIVersion version1, final APIVersion version2) {
		
		//Adiciona lista de Breaking Changes.
		this.findRemovedEnums(version1, version2);
		this.findChangedVisibilityEnums(version1, version2);
		
		//Conta non-Breaking Changes.
		this.findAddedEnums(version1, version2);
		this.findAddedDeprecatedEnums(version1, version2);
		
		Result result = new Result();
		result.setElementAdd(this.enumAdd);
		result.setElementDeprecated(this.enumDeprecated);
		result.setElementModified(this.enumModif);
		result.setElementRemoved(this.enumRemoval);
		result.setBreakingChange(this.enumBreakingChange);
		result.setNonBreakingChange(this.enumNonBreakingChange);
		result.setListBreakingChange(this.listBreakingChange);
		return result;
	}

	private void findAddedDeprecatedEnums(APIVersion version1, APIVersion version2) {
		for(EnumDeclaration acessibleEnumVersion2 : version2.getApiAccessibleEnums()){
			if(acessibleEnumVersion2.resolveBinding() != null && 
					acessibleEnumVersion2.resolveBinding().isDeprecated()){
				EnumDeclaration accessibleEnumVersion1 = version1.getVersionAccessibleEnum(acessibleEnumVersion2);
				if(accessibleEnumVersion1 == null){
					this.enumNonBreakingChange++;
				} else {
					if(accessibleEnumVersion1.resolveBinding() != null && !accessibleEnumVersion1.resolveBinding().isDeprecated()){
						this.enumNonBreakingChange++;
					}
				}
			}
		}
	}

	private void findChangedVisibilityEnums(APIVersion version1, APIVersion version2) {
		
		for(EnumDeclaration acessibleEnumVersion1 : version1.getApiAccessibleEnums()){
			if(version2.containsNonAccessibleEnum(acessibleEnumVersion1)){
				if(acessibleEnumVersion1.resolveBinding() != null &&
						acessibleEnumVersion1.resolveBinding().isDeprecated()){
					this.enumNonBreakingChange++;
					this.enumDeprecated++;
				} else {
					this.enumBreakingChange++;
					this.enumModif++;
					this.listBreakingChange.add(new BreakingChange(acessibleEnumVersion1.resolveBinding().getQualifiedName(), acessibleEnumVersion1.getName().toString(), this.CATEGORY_LOST_VISIBILITY));
				}
			}
		}

		for(EnumDeclaration nonAcessibleEnumVersion1 : version1.getApiNonAccessibleEnums()){
			if(version2.containsAccessibleEnum(nonAcessibleEnumVersion1)){
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
					this.enumDeprecated++;
				} else{
					this.enumBreakingChange++;
					this.enumRemoval++;
					this.listBreakingChange.add(new BreakingChange(enumVersion1.resolveBinding().getQualifiedName(), enumVersion1.getName().toString(), this.CATEGORY_REMOVED_ENUM));
				}
			}
		}
	}
}
