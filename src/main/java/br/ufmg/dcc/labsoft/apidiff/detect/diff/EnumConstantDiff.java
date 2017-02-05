package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;

import br.ufmg.dcc.labsoft.apidiff.UtilTools;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class EnumConstantDiff {
	
	private final String CATEGORY_REMOVED_CONSTANT = "REMOVED CONSTANT";
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	private int breakingChange;
	private int nonBreakingChange;
	private int enunConstantAdd;
	private int enumConstantRemoval;
	private int enumConstantModif;
	private int enumConstantDeprecated;
	
	public Result calculateDiff(final APIVersion version1, final APIVersion version2) {
		
		//Lista breaking Change.
		this.findRemovedConstant(version1, version2);
		
		//Conta non-Breaking Change.
		this.findAddedDeprecatedConstant(version1, version2);
		this.findAddedConstant(version1, version2);
		
		Result result = new Result();
		result.setElementAdd(enunConstantAdd);
		result.setElementDeprecated(this.enumConstantDeprecated);
		result.setElementModified(this.enumConstantModif);
		result.setElementRemoved(this.enumConstantRemoval);
		result.setBreakingChange(this.breakingChange);
		result.setNonBreakingChange(this.nonBreakingChange);
		result.setListBreakingChange(this.listBreakingChange);
		return result;
	}

	private void findAddedConstant(APIVersion version1, APIVersion version2) {
		
		for(EnumDeclaration enumVersion2 : version2.getApiAccessibleEnums()){
			if(!UtilTools.isPrivate(enumVersion2)){
				EnumDeclaration enumVersion1 = version1.getVersionAccessibleEnum(enumVersion2);
				if(enumVersion1 != null && !UtilTools.isPrivate(enumVersion1)){
					for(Object constant : enumVersion2.enumConstants()){
						if(version1.getEqualVersionConstant((EnumConstantDeclaration) constant, enumVersion2) == null){
							this.nonBreakingChange++;
							this.enunConstantAdd++;
						}
					}
				} else {
					this.nonBreakingChange += enumVersion2.enumConstants().size();
					this.enunConstantAdd += enumVersion2.enumConstants().size();
				}
			}
		}
	}

	private void findRemovedConstant(APIVersion version1, APIVersion version2) {
		
		for(EnumDeclaration enumVersion1 : version1.getApiAccessibleEnums()){
			if(!UtilTools.isPrivate(enumVersion1)){
				EnumDeclaration enumVersion2 = version2.getVersionAccessibleEnum(enumVersion1);
				if(enumVersion2 != null && !UtilTools.isPrivate(enumVersion2)){
					for(Object constantVersion1 : enumVersion1.enumConstants()){
						if(version2.getEqualVersionConstant((EnumConstantDeclaration) constantVersion1, enumVersion1) == null){
							if(((EnumConstantDeclaration)constantVersion1).resolveVariable() != null &&
									((EnumConstantDeclaration)constantVersion1).resolveVariable().isDeprecated()){
								this.nonBreakingChange++;
								this.enumConstantDeprecated++;
							} else {
								this.breakingChange++;
								this.enumConstantRemoval++;
								this.listBreakingChange.add(new BreakingChange(enumVersion1.resolveBinding().getQualifiedName(), constantVersion1.toString(), this.CATEGORY_REMOVED_CONSTANT));
							}
						}
					}
				} else {
					for(Object constantVersion1 : enumVersion1.enumConstants()){
						if(((EnumConstantDeclaration)constantVersion1).resolveVariable() != null &&
								((EnumConstantDeclaration)constantVersion1).resolveVariable().isDeprecated()){
							this.nonBreakingChange++;
							this.enumConstantDeprecated++;
						} else {
							this.breakingChange++;
							this.enumConstantRemoval++;
							this.listBreakingChange.add(new BreakingChange(enumVersion1.resolveBinding().getQualifiedName(), constantVersion1.toString(), this.CATEGORY_REMOVED_CONSTANT));
						}
					}
				}
			}
		}
	}

	private void findAddedDeprecatedConstant(APIVersion version1, APIVersion version2) {
		
		for(EnumDeclaration enumVersion1 : version1.getApiAccessibleEnums()){
			if(!UtilTools.isPrivate(enumVersion1)){
				EnumDeclaration enumVersion2 = version2.getVersionAccessibleEnum(enumVersion1);
				if(enumVersion2 != null && !UtilTools.isPrivate(enumVersion2)){
					for(Object constantVersion2 : enumVersion2.enumConstants()){
						if(((EnumConstantDeclaration) constantVersion2).resolveVariable() != null &&
								((EnumConstantDeclaration) constantVersion2).resolveVariable().isDeprecated()){
							EnumConstantDeclaration constantVersion1 = version1.getEqualVersionConstant
									((EnumConstantDeclaration) constantVersion2, enumVersion2);
							if(constantVersion1 == null || (constantVersion1.resolveVariable() != null &&
									!constantVersion1.resolveVariable().isDeprecated()))
								this.nonBreakingChange++;
						}
					}
				}
			}
		}
	}
}
