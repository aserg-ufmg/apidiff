package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.labsoft.apidiff.UtilTools;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class EnumDiff {
	
	private final String CATEGORY_ENUM_LOST_VISIBILITY = "ENUM LOST VISIBILITY";
	private final String CATEGORY_ENUM_LOST_VISIBILITY_DEPRECIATED = "ENUM LOST VISIBILITY DEPRECIATED";
	private final String CATEGORY_ENUM_GAIN_VISIBILITY = "ENUM GAIN VISIBILITY";
	
	private final String CATEGORY_ENUM_REMOVED = "ENUM REMOVED";
	private final String CATEGORY_ENUM_REMOVED_DEPRECIATED = "ENUM REMOVED DEPRECIATED";
	
	private final String CATEGORY_ENUM_ADD = "ENUM ADDED";
	
	private final String CATEGORY_ENUM_DEPRECIATED = "ENUM DEPRECIATED";
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	
	private Logger logger = LoggerFactory.getLogger(EnumDiff.class);

	public Result calculateDiff(final APIVersion version1, final APIVersion version2) {
		
		this.logger.info("Processing Method Enums...");
		
		//Adiciona lista de Breaking Changes.
		this.findRemovedEnums(version1, version2);
		this.findChangedVisibilityEnums(version1, version2);
		
		//Conta non-Breaking Changes.
		this.findAddedEnums(version1, version2);
		this.findAddedDeprecatedEnums(version1, version2);
		
		Result result = new Result();
		result.setListBreakingChange(this.listBreakingChange);
		return result;
	}

	private void findAddedDeprecatedEnums(APIVersion version1, APIVersion version2) {
		for(EnumDeclaration acessibleEnumVersion2 : version2.getApiAccessibleEnums()){
			if(this.isDeprecated(acessibleEnumVersion2)){
				EnumDeclaration accessibleEnumVersion1 = version1.getVersionAccessibleEnum(acessibleEnumVersion2);
				String category = this.CATEGORY_ENUM_DEPRECIATED + UtilTools.getSufixJavadoc(acessibleEnumVersion2);;
				if(accessibleEnumVersion1 == null){
					this.listBreakingChange.add(new BreakingChange(acessibleEnumVersion2.resolveBinding().getQualifiedName(), acessibleEnumVersion2.getName().toString(), category, false));
				} else {
					if(accessibleEnumVersion1.resolveBinding() != null && !accessibleEnumVersion1.resolveBinding().isDeprecated()){
						this.listBreakingChange.add(new BreakingChange(accessibleEnumVersion1.resolveBinding().getQualifiedName(), accessibleEnumVersion1.getName().toString(), category, false));
					}
				}
			}
		}
	}
	
	/**
	 * Retorna o type na lista com o mesmo nameNode do type recebido.
	 * Retorna nulo se não for encontrado.
	 * @param list
	 * @param type
	 * @return
	 */
	private EnumDeclaration findEnumDeclarationInList(List<EnumDeclaration> list, EnumDeclaration type){
		for(int i=0; i< list.size(); i++){
			EnumDeclaration typeDeclaration = list.get(i);
			if(UtilTools.getNameNode(type).equals(UtilTools.getNameNode(typeDeclaration))){
				return typeDeclaration;
			}
		}
		return null;
	}

	
	/**
	 * Retorna verdadeiro se é um type depreciado.
	 * @param enumVersion
	 * @return
	 */
	private Boolean isDeprecated(EnumDeclaration enumVersion){
		return (enumVersion != null && enumVersion.resolveBinding() != null && enumVersion.resolveBinding().isDeprecated()) ? true: false;
	}
	
	/**
	 * Busca enums que tiveram perda ou ganho de visibilidade.
	 * @param version1
	 * @param version2
	 */
	private void findChangedVisibilityEnums(APIVersion version1, APIVersion version2) {
		
		List<EnumDeclaration> listEnumsVersion1 = version1.getAllEnums();
		List<EnumDeclaration> listEnumsVersion2 = version2.getAllEnums();
		
		//Percorre types da versão anterior.
		for(EnumDeclaration enum1: listEnumsVersion1){
			EnumDeclaration enum2 = this.findEnumDeclarationInList(listEnumsVersion2, enum1);
			if(enum2 != null){
				String visibilityType1 = UtilTools.getVisibility(enum1);
				String visibilityType2 = UtilTools.getVisibility(enum2);
				if(!visibilityType1.equals(visibilityType2)){ //Se visibilidade mudou, verifica se houve perda ou ganho.
					String category = "";
					Boolean isBreakingChange = false;
					//Breaking change: public --> qualquer modificador de acesso, protected --> qualquer modificador de acesso, exceto public.
					if((UtilTools.isVisibilityPublic(enum1) || (UtilTools.isVisibilityProtected(enum1)) && !UtilTools.isVisibilityPublic(enum2))){
						category = this.isDeprecated(enum1)? this.CATEGORY_ENUM_LOST_VISIBILITY_DEPRECIATED: this.CATEGORY_ENUM_LOST_VISIBILITY;
						isBreakingChange = this.isDeprecated(enum1)? false: true;
					}
					else{
						//non-breaking change: private ou default --> qualquer modificador de acesso, demais casos.
						category = UtilTools.isVisibilityDefault(enum1) && UtilTools.isVisibilityPrivate(enum2)? this.CATEGORY_ENUM_LOST_VISIBILITY: this.CATEGORY_ENUM_GAIN_VISIBILITY;
						isBreakingChange = false;
					}
					category += UtilTools.getSufixJavadoc(enum2);
					this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(enum2), enum2.getName().toString(), category, isBreakingChange));
				}
			}
		}
	}

	private void findAddedEnums(APIVersion version1, APIVersion version2) {
		for(EnumDeclaration enumVersion2 : version2.getApiAccessibleEnums()){
			if(version1.getVersionAccessibleEnum(enumVersion2) == null && 
					version1.getVersionNonAccessibleEnum(enumVersion2) == null){
				this.listBreakingChange.add(new BreakingChange(enumVersion2.resolveBinding().getQualifiedName(), enumVersion2.getName().toString(), this.CATEGORY_ENUM_ADD + UtilTools.getSufixJavadoc(enumVersion2), false));
			}
		}
	}

	private void findRemovedEnums(APIVersion version1, APIVersion version2){
		for(EnumDeclaration enumVersion1 : version1.getApiAccessibleEnums()){
			if(version2.getVersionAccessibleEnum(enumVersion1) == null && version2.getVersionNonAccessibleEnum(enumVersion1) == null){
				if(this.isDeprecated(enumVersion1)){
					this.listBreakingChange.add(new BreakingChange(enumVersion1.resolveBinding().getQualifiedName(), enumVersion1.getName().toString(), this.CATEGORY_ENUM_REMOVED_DEPRECIATED + UtilTools.getSufixJavadoc(enumVersion1), false));
				} else{
					this.listBreakingChange.add(new BreakingChange(enumVersion1.resolveBinding().getQualifiedName(), enumVersion1.getName().toString(), this.CATEGORY_ENUM_REMOVED + UtilTools.getSufixJavadoc(enumVersion1) , true));
				}
			}
		}
	}
}
