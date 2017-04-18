package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.labsoft.apidiff.UtilTools;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class TypeDiff {
	
	private final String CATEGORY_TYPE_REMOVED_DEPRECIATED = "TYPE REMOVED DEPRECIATED"; //non-breaking change
	private final String CATEGORY_TYPE_REMOVED = "TYPE REMOVED"; //breaking change
	
	private final String CATEGORY_TYPE_LOST_VISIBILIT_DEPRECIATED = "TYPE LOST VISIBILIT DEPRECIATED"; //non-breaking change
	private final String CATEGORY_TYPE_LOST_VISIBILITY = "TYPE LOST VISIBILIT"; //breaking change
	private final String CATEGORY_TYPE_GAIN_VISIBILITY = "TYPE GAIN VISIBILITY"; //non-breaking change
	
	private final String CATEGORY_TYPE_LOST_MODIFIER_FINAL = "TYPE LOST MODIFIER FINAL"; //non-breaking change
	private final String CATEGORY_TYPE_GAIN_MODIFIER_FINAL = "TYPE GAIN MODIFIER FINAL"; //breaking change
	private final String CATEGORY_TYPE_GAIN_MODIFIER_FINAL_DEPRECIATED = "TYPE GAIN MODIFIER FINAL DEPRECIATED"; //non-breaking change
	
	private final String CATEGORY_TYPE_LOST_MODIFIER_STATIC = "TYPE LOST MODIFIER STATIC"; //breaking change
	private final String CATEGORY_TYPE_LOST_MODIFIER_STATIC_DEPRECIATED = "TYPE LOST MODIFIER STATIC DEPRECIATED"; //non-breaking change
	private final String CATEGORY_TYPE_GAIN_MODIFIER_STATIC = "TYPE GAIN MODIFIER STATIC"; //non-breaking change
	
	private final String CATEGORY_TYPE_ADD = "TYPE ADDED"; //non-breaking change
	
	private final String CATEGORY_TYPE_DEPRECIATED = "TYPE DEPRECIATED"; //non-breaking change
	
	private final String CATEGORY_SUPER_TYPE_CHANGED_DEPRECIATED = "SUPER TYPE CHANGED DEPRECIATED"; //non-breaking change
	private final String CATEGORY_SUPER_TYPE_CHANGED = "SUPER TYPE CHANGED"; //breaking changes
	private final String CATEGORY_SUPER_TYPE_REMOVED_DEPRECIATED = "SUPER TYPE REMOVED DEPRECIATED"; //non-breaking change
	private final String CATEGORY_SUPER_TYPE_REMOVED = "SUPER TYPE REMOVED"; //breaking change
	private final String CATEGORY_SUPER_TYPE_ADD = "SUPER TYPE ADDED"; //non-breaking change
	private final String CATEGORY_SUPER_TYPE_ADD_DEPRECIATED = "SUPER TYPE ADDED DEPRECIATED"; //non-breaking change
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	
	private Logger logger = LoggerFactory.getLogger(TypeDiff.class);

	/**
	 * Calculates the diff for classes
	 * @param version1 older version of an API
	 * @param version2 newer version of an API
	 */
	public Result calculateDiff(final APIVersion version1, final APIVersion version2) {
		
		this.logger.info("Processing Types ...");
		
		this.findRemovedTypes(version1, version2);
		this.findAddedTypes(version1, version2);
		this.findChangedVisibilityTypes(version1, version2);
		this.findAddedDeprecated(version1, version2);
		this.changedSuperTypes(version1, version2);
		this.findChangedFinalAndStatic(version1, version2);
		
		Result result = new Result();
		result.setListBreakingChange(this.listBreakingChange);
		return result;
	}

	/**
	 * Busca sub classes que tiveram mudança de super classe.
	 * 
	 * Classes que tirevem a super classe removida, são breaking change [CATEGORY_SUPER_TYPE_REMOVED].
	 * Classes que tirevem a super classe removida e estavam depreciadas na versão anterior são non-breaking change [CATEGORY_SUPER_TYPE_REMOVED_DEPRECIATED].
	 * Classes que ganharam uma super classe são non-breaking change [CATEGORY_SUPER_TYPE_ADD].
	 * Classes que tiver a super classe modificada são breaking change [CATEGORY_SUPER_TYPE_CHANGED].
	 * Classes que tiver a super classe modificada  e estavam depreciadas na versão anterior são non-breaking change [CATEGORY_SUPER_TYPE_CHANGED_DEPRECIATED].
	 * 
	 * @param version1
	 * @param version2
	 */
	private void changedSuperTypes(APIVersion version1, APIVersion version2) {
		for(TypeDeclaration accessibleTypeVersion1 : version1.getApiAcessibleTypes()){
			TypeDeclaration accessibleTypeVersion2 = version2.getVersionAccessibleType(accessibleTypeVersion1);
			if(accessibleTypeVersion2 != null){
				String super1 = this.getNameSuperClass(accessibleTypeVersion1);  
				String super2 = this.getNameSuperClass(accessibleTypeVersion2);
				
				if(super1 != null && super2 != null){
					Boolean isBreakingChange = !this.isDeprecated(accessibleTypeVersion1);
					
					//Se tinha super classe, e foi removida.
					if(this.containsSuperClass(accessibleTypeVersion1) && !this.containsSuperClass(accessibleTypeVersion2)){
						String category = this.isDeprecated(accessibleTypeVersion1)? this.CATEGORY_SUPER_TYPE_REMOVED_DEPRECIATED: this.CATEGORY_SUPER_TYPE_REMOVED;
						category += UtilTools.getSufixJavadoc(accessibleTypeVersion2);
						this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(accessibleTypeVersion1), accessibleTypeVersion1.getName().toString(), category, isBreakingChange));
					}
					//Se não tinha super classe, e foi adicionada.
					if(!this.containsSuperClass(accessibleTypeVersion1) && this.containsSuperClass(accessibleTypeVersion2)){
						String category = this.isDeprecated(accessibleTypeVersion1)? this.CATEGORY_SUPER_TYPE_ADD_DEPRECIATED: this.CATEGORY_SUPER_TYPE_ADD;
						category += UtilTools.getSufixJavadoc(accessibleTypeVersion2);
						this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(accessibleTypeVersion2), accessibleTypeVersion2.getName().toString(), category, false));
					}
					//Se tinha super classe, e foi modificada.
					if(this.containsSuperClass(accessibleTypeVersion1) && this.containsSuperClass(accessibleTypeVersion2) && !super1.equals(super2)){
						String category = this.isDeprecated(accessibleTypeVersion1)? this.CATEGORY_SUPER_TYPE_CHANGED_DEPRECIATED: this.CATEGORY_SUPER_TYPE_CHANGED;
						category += UtilTools.getSufixJavadoc(accessibleTypeVersion2);
						this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(accessibleTypeVersion2), accessibleTypeVersion2.getName().toString(), category, isBreakingChange));
					}
				}
			}
		}
	}

	/**
	 * Busca classes que foram depreciadas [CATEGORY_TYPE_DEPRECIATED].
	 * @param version1
	 * @param version2
	 */
	private void findAddedDeprecated(APIVersion version1, APIVersion version2) {
		
		String category = this.CATEGORY_TYPE_DEPRECIATED;
		//Se type não era depreciado na versão 1 e foi depreciado na versão 2.
		for(TypeDeclaration accessibleTypeVersion1 : version1.getApiAcessibleTypes()){
			TypeDeclaration accessibleTypeVersion2 = version2.getVersionAccessibleType(accessibleTypeVersion1);
			if(accessibleTypeVersion2 != null){
				if(!this.isDeprecated(accessibleTypeVersion1) && this.isDeprecated(accessibleTypeVersion2)){
					category += UtilTools.getSufixJavadoc(accessibleTypeVersion2);
					this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(accessibleTypeVersion1), accessibleTypeVersion1.getName().toString(), category, false));
				}
			}
		}

		//Se type não existia na versão 1, e foi adicionado na versão 2 já depreciado.
		for(TypeDeclaration accessibleTypeVersion2 : version2.getApiAcessibleTypes()){
			if(!version1.containsAccessibleType(accessibleTypeVersion2) && this.isDeprecated(accessibleTypeVersion2)){
				category += UtilTools.getSufixJavadoc(accessibleTypeVersion2);
				this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(accessibleTypeVersion2), accessibleTypeVersion2.getName().toString(), category, false));
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
	private TypeDeclaration findTypeDeclarationInList(List<TypeDeclaration> list, TypeDeclaration type){
		for(int i=0; i< list.size(); i++){
			TypeDeclaration typeDeclaration = list.get(i);
			if(UtilTools.getNameNode(type).equals(UtilTools.getNameNode(typeDeclaration))){
				return typeDeclaration;
			}
		}
		return null;
	}
	

	
	/**
	 * Busca classes que tiveram perda ou ganho de visibilidade.
	 * Classes que perderam visibilidade são breaking changes [CATEGORY_LOST_VISIBILITY_TYPE].
	 * Classes que perderam visibilidade mas estavam depreciadas na versão anterior são non-breaking changes [CATEGORY_LOST_DEPRECIATED_VISIBILITY_TYPE].
	 * Classes que ganharam visibilidade são non-breaking changes [CATEGORY_GAIN_VISIBILITY_TYPE].
	 * @param version1
	 * @param version2
	 */
	private void findChangedVisibilityTypes(APIVersion version1, APIVersion version2) {
		
		List<TypeDeclaration> listTypesVersion1 = version1.getAllTypes();
		List<TypeDeclaration> listTypesVersion2 = version2.getAllTypes();
		
		//Percorre types da versão anterior.
		for(TypeDeclaration type1: listTypesVersion1){
			TypeDeclaration type2 = this.findTypeDeclarationInList(listTypesVersion2, type1);
			if(type2 != null){
				
				String visibilityType1 = UtilTools.getVisibility(type1);
				String visibilityType2 = UtilTools.getVisibility(type2);
				
				if(!visibilityType1.equals(visibilityType2)){ //Se visibilidade mudou, verifica se houve perda ou ganho.
					
					String category = "";
					Boolean isBreakingChange = false;
					
					//Breaking change: public --> qualquer modificador de acesso, protected --> qualquer modificador de acesso, exceto public.
					if(UtilTools.isVisibilityPublic(type1) || (UtilTools.isVisibilityProtected(type1) && !UtilTools.isVisibilityPublic(type2))){
						category = this.isDeprecated(type1)? this.CATEGORY_TYPE_LOST_VISIBILIT_DEPRECIATED: this.CATEGORY_TYPE_LOST_VISIBILITY;
						category += UtilTools.getSufixJavadoc(type2);
						isBreakingChange = this.isDeprecated(type1)? false: true;
					}
					else{
						//non-breaking change: private ou default --> qualquer modificador de acesso, demais casos.
						category = UtilTools.isVisibilityDefault(type1) && UtilTools.isVisibilityPrivate(type2)? this.CATEGORY_TYPE_LOST_VISIBILITY: this.CATEGORY_TYPE_GAIN_VISIBILITY;
						category += UtilTools.getSufixJavadoc(type2);
						isBreakingChange = false;
					}
					this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(type2), type2.getName().toString(), category, isBreakingChange));
				}
			}
		}
	}

	/**
	 * Busca classes que foram adicionadas [CATEGORY_ADD_TYPE].
	 * @param version1
	 * @param version2
	 */
	private void findAddedTypes(APIVersion version1, APIVersion version2) {
		//Busca types na segunda versão que não estão na primeira.
		List<TypeDeclaration> listTypesVersion2 = UtilTools.getAcessibleTypes(version2);
		for (TypeDeclaration typeVersion2 : listTypesVersion2) {
			//Busca entre os types acessíveis e não acessíveis porque pode ser um type que já existia e ganhou visibilidade.
			if(!version1.containsAccessibleType(typeVersion2) && !version1.containsNonAccessibleType(typeVersion2)){
				this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(typeVersion2), typeVersion2.getName().toString(), this.CATEGORY_TYPE_ADD + UtilTools.getSufixJavadoc(typeVersion2), false));
			}
		}
	}

	/**
	 * Busca classes que foram removidas. 
	 * Se a classe removida estava depreciada na versão anterior é uma non-breaking change [CATEGORY_REMOVED_DEPRECIATED_TYPE].
	 * Caso contrário é uma breaking change [CATEGORY_REMOVED_TYPE] 
	 * @param version1
	 * @param version2
	 */
	private void findRemovedTypes(APIVersion version1, APIVersion version2) {
		for (TypeDeclaration type : version1.getApiAcessibleTypes()) {
			if(!version2.containsAccessibleType(type) && !version2.containsNonAccessibleType(type)){
				String category = this.isDeprecated(type)? this.CATEGORY_TYPE_REMOVED_DEPRECIATED: this.CATEGORY_TYPE_REMOVED;
				category += UtilTools.getSufixJavadoc(type);
				Boolean isBreakingChange = this.isDeprecated(type)? false: true;
				this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(type), type.getName().toString(), category, isBreakingChange));
			}
		}
	}
	
	/**
	 * Retorna verdadeiro se é um type depreciado.
	 * @param type
	 * @return
	 */
	private Boolean isDeprecated(TypeDeclaration type){
		return (type != null && type.resolveBinding() != null && type.resolveBinding().isDeprecated()) ? true: false;
	}
	
	/**
	 * Retorna nome da super classe. Se não existir retorna uma string vazia.
	 * @param type
	 * @return
	 */
	private String getNameSuperClass(TypeDeclaration type){
		if(type.resolveBinding() != null && type.resolveBinding().getSuperclass() != null){
			return type.resolveBinding().getSuperclass().getQualifiedName().toString();
		}
		return null;
	}
	
	/**
	 * Retorna verdadeiro se a classe herda de uma classe diferente do default (Object).
	 * @param type
	 * @return
	 */
	private boolean containsSuperClass(TypeDeclaration type){
		String nameSuperClass = this.getNameSuperClass(type);
		return (nameSuperClass != null && !"java.lang.Object".equals(nameSuperClass))?true:false;
	}
	
	
	/**
	 * Compara se dois métodos tem ou não o modificador "final".
	 * Registra na saída, se houver diferença.
	 * 
	 * Adicionar final é breaking change.
	 * Remover final não é breaking change.
	 * 
	 * @param methodInVersion1
	 * @param methodInVersion2
	 */
	private void diffModifierFinal(TypeDeclaration typeVersion1, TypeDeclaration typeVersion2){
		//Se não houve mudança no identificador final.
		if((UtilTools.isFinal(typeVersion1) && UtilTools.isFinal(typeVersion2)) || ((!UtilTools.isFinal(typeVersion1) && !UtilTools.isFinal(typeVersion2)))){
			return;
		}
		String category = "";
		Boolean isBreakingChange = false;
		//Se ganhou o modificador "final"
		if((!UtilTools.isFinal(typeVersion1) && UtilTools.isFinal(typeVersion2))){
			category = this.isDeprecated(typeVersion1)?this.CATEGORY_TYPE_GAIN_MODIFIER_FINAL_DEPRECIATED:CATEGORY_TYPE_GAIN_MODIFIER_FINAL;
			isBreakingChange = this.isDeprecated(typeVersion1)?false:true;
		}
		else{
			//Se perdeu o modificador "final"
			category = this.CATEGORY_TYPE_LOST_MODIFIER_FINAL;
			isBreakingChange = false;
		}
		category += UtilTools.getSufixJavadoc(typeVersion2);
		this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(typeVersion2), typeVersion2.getName().toString(), category, isBreakingChange));
	}
	
	/**
	 * Verifica se duas versões de um type tiveram mudanças no modificador "static"
	 * Registra na saída, se houver diferença.
	 * 
	 * Adicionar final não é breaking change.
	 * Remover "Static" é breaking change.
	 * 
	 * @param methodInVersion1
	 * @param methodInVersion2
	 */
	private void diffModifierStatic(TypeDeclaration typeVersion1, TypeDeclaration typeVersion2){
		//Se não houve mudança no identificador static.
		if((UtilTools.isStatic(typeVersion1) && UtilTools.isStatic(typeVersion2)) || ((!UtilTools.isStatic(typeVersion1) && !UtilTools.isStatic(typeVersion2)))){
			return;
		}
		String category = "";
		Boolean isBreakingChange = false;
		//Se ganhou o modificador "static"
		if((!UtilTools.isStatic(typeVersion1) && UtilTools.isStatic(typeVersion2))){
			category =  this.CATEGORY_TYPE_GAIN_MODIFIER_STATIC;
			isBreakingChange = false;
		}
		else{
			//Se perdeu o modificador "static"
			category = this.isDeprecated(typeVersion1)?this.CATEGORY_TYPE_LOST_MODIFIER_STATIC_DEPRECIATED:CATEGORY_TYPE_LOST_MODIFIER_STATIC;
			isBreakingChange = this.isDeprecated(typeVersion1)?false:true;
		}
		category += UtilTools.getSufixJavadoc(typeVersion2);
		this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(typeVersion2), typeVersion2.getName().toString(), category, isBreakingChange));
	}
	
	/**
	 * Busca modificadores final/static que foram removidos ou adicionados.
	 * 
	 * @param version1
	 * @param version2
	 */
	private void findChangedFinalAndStatic(APIVersion version1, APIVersion version2) {
		//Percorre todos os types da versão corrente.
		for (TypeDeclaration typeVersion1 : version1.getApiAcessibleTypes()) {
			TypeDeclaration typeVersion2 = version2.getVersionAccessibleType(typeVersion1);
			if(typeVersion2 != null){
				this.diffModifierFinal(typeVersion1, typeVersion2);
				this.diffModifierStatic(typeVersion1, typeVersion2);
			}
		}
	}
}
