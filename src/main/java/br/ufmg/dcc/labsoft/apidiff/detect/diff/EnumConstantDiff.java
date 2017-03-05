package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;

import br.ufmg.dcc.labsoft.apidiff.UtilTools;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class EnumConstantDiff {
	
	private final String CATEGORY_ENUM_CONSTANT_REMOVED = "ENUM CONSTANT REMOVED";
	private final String CATEGORY_ENUM_CONSTANT_REMOVED_DEPRECIATED = "ENUM CONSTANT REMOVED DEPRECIATED";
	
	private final String CATEGORY_ENUM_CONSTANT_ADD = "ENUM CONSTANT ADDED";
	
	private final String CATEGORY_ENUM_CONSTANT_DEPRECIATED = "ENUM CONSTANT DEPRECIATED";
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	
	public Result calculateDiff(final APIVersion version1, final APIVersion version2) {
		
		//Lista breaking Change.
		this.findRemovedConstant(version1, version2);
		
		//Conta non-Breaking Change.
		this.findAddedDeprecatedConstant(version1, version2);
		this.findAddedConstant(version1, version2);
		
		Result result = new Result();
		result.setListBreakingChange(this.listBreakingChange);
		return result;
	}

	private void findAddedConstant(APIVersion version1, APIVersion version2) {
		
		for(EnumDeclaration enumVersion2 : version2.getApiAccessibleEnums()){
			if(!UtilTools.isVisibilityPrivate(enumVersion2)){
				EnumDeclaration enumVersion1 = version1.getVersionAccessibleEnum(enumVersion2);
				//Se é um Enum que já existia, verifica se alguma constante foi adicionada.
				if(enumVersion1 != null && !UtilTools.isVisibilityPrivate(enumVersion1)){
					for(Object constant : enumVersion2.enumConstants()){
						if(version1.getEqualVersionConstant((EnumConstantDeclaration) constant, enumVersion2) == null){
							this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(enumVersion1), constant.toString(), this.CATEGORY_ENUM_CONSTANT_ADD, false));
						}
					}
				} else {
					//Se é um novo Enum, aciona suas constantes.
					for(Object constant : enumVersion2.enumConstants()){
						this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(enumVersion1), constant.toString(), this.CATEGORY_ENUM_CONSTANT_ADD, false));
					}
				}
			}
		}
	}

	/**
	 * Retorna verdadeiro se é um enum acessível pelo cliente externo ao projeto.
	 * @param enumVersion
	 * @return
	 */
	private boolean isEnumAcessible(EnumDeclaration enumVersion){
		return enumVersion != null && enumVersion.resolveBinding() !=null && (UtilTools.isVisibilityProtected(enumVersion) || UtilTools.isVisibilityPublic(enumVersion))?true:false;
	}
	
	private void findRemovedConstant(APIVersion version1, APIVersion version2) {
		
		for(EnumDeclaration enumVersion1 : version1.getApiAccessibleEnums()){
			if(!UtilTools.isVisibilityPrivate(enumVersion1)){
				EnumDeclaration enumVersion2 = version2.getVersionAccessibleEnum(enumVersion1);
				if(this.isEnumAcessible(enumVersion2)){
					for(Object constantVersion1 : enumVersion1.enumConstants()){
						if(version2.getEqualVersionConstant((EnumConstantDeclaration) constantVersion1, enumVersion1) == null){
							if(((EnumConstantDeclaration)constantVersion1).resolveVariable() != null &&
									((EnumConstantDeclaration)constantVersion1).resolveVariable().isDeprecated()){
								this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(enumVersion1), constantVersion1.toString(), this.CATEGORY_ENUM_CONSTANT_REMOVED_DEPRECIATED, false));
							} else {
								this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(enumVersion1), constantVersion1.toString(), this.CATEGORY_ENUM_CONSTANT_REMOVED, true));
							}
						}
					}
				} else {
					for(Object constantVersion1 : enumVersion1.enumConstants()){
						if(((EnumConstantDeclaration)constantVersion1).resolveVariable() != null &&
								((EnumConstantDeclaration)constantVersion1).resolveVariable().isDeprecated()){
							this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(enumVersion1), constantVersion1.toString(), this.CATEGORY_ENUM_CONSTANT_REMOVED_DEPRECIATED, false));
						} else {
							this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(enumVersion1), constantVersion1.toString(), this.CATEGORY_ENUM_CONSTANT_REMOVED, true));
						}
					}
				}
			}
		}
	}

	private void findAddedDeprecatedConstant(APIVersion version1, APIVersion version2) {
		
		for(EnumDeclaration enumVersion1 : version1.getApiAccessibleEnums()){
			if(!UtilTools.isVisibilityPrivate(enumVersion1)){
				EnumDeclaration enumVersion2 = version2.getVersionAccessibleEnum(enumVersion1);
				if(enumVersion2 != null && !UtilTools.isVisibilityPrivate(enumVersion2)){
					for(Object constantVersion2 : enumVersion2.enumConstants()){
						if(((EnumConstantDeclaration) constantVersion2).resolveVariable() != null &&
								((EnumConstantDeclaration) constantVersion2).resolveVariable().isDeprecated()){
							EnumConstantDeclaration constantVersion1 = version1.getEqualVersionConstant
									((EnumConstantDeclaration) constantVersion2, enumVersion2);
							if(constantVersion1 == null || (constantVersion1.resolveVariable() != null &&
									!constantVersion1.resolveVariable().isDeprecated()))
								this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(enumVersion1), constantVersion1.toString(), this.CATEGORY_ENUM_CONSTANT_DEPRECIATED, false));
						}
					}
				}
			}
		}
	}
}
