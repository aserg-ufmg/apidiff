package apidiff.internal.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apidiff.enums.Category;
import apidiff.internal.analysis.description.TypeDescription;
import apidiff.internal.visitor.APIVersion;
import apidiff.util.UtilTools;
import refdiff.core.api.RefactoringType;
import refdiff.core.rm2.model.refactoring.SDRefactoring;

public class TypeDiff {
	
	private List<Change> listChange = new ArrayList<Change>();
	
	private Logger logger = LoggerFactory.getLogger(TypeDiff.class);
	
	private TypeDescription description = new TypeDescription();
	
	private APIVersion version1;
	
	private APIVersion version2;
	
	private Map<RefactoringType, List<SDRefactoring>> refactorings = new HashMap<RefactoringType, List<SDRefactoring>>();
	
	private List<String> typesWithPathChanged = new ArrayList<String>();
	
	private RevCommit revCommit;
	
	/**
	 * Calculates the diff for classes
	 * @param version1 older version of an API
	 * @param version2 newer version of an API
	 */
	public List<Change> detectChange(final APIVersion version1, final APIVersion version2, final Map<RefactoringType, List<SDRefactoring>> refactorings, final RevCommit revCommit) {
		this.version1 = version1;
		this.version2 = version2;
		this.revCommit = revCommit;
		this.refactorings = refactorings;
		this.logger.info("Processing Types ...");
		this.findRemovedAndRenameAndMoveTypes();
		this.findAddedTypes();
		this.findChangedVisibilityTypes();
		this.findAddTypeDeprecate();
		this.findChangedSuperTypes();
		this.findChangedFinalAndStatic();
		return this.listChange;
	}
	
	private void addChange(final AbstractTypeDeclaration type, Category category, Boolean isBreakingChange, final String description){
		Change change = new Change();
		change.setJavadoc(UtilTools.containsJavadoc(type));
		change.setDepreciated(this.isDeprecated(type));
		change.setBreakingChange(this.isDeprecated(type) ? false : isBreakingChange);
		change.setPath(UtilTools.getPath(type));
		change.setStruture(type.getName().toString());
		change.setCategory(category);
		change.setDescription(description);
		change.setRevCommit(this.revCommit);
		this.listChange.add(change);
	}
	
	private Boolean processRenameType(final AbstractTypeDeclaration type){
		List<SDRefactoring> listRenames = this.refactorings.get(RefactoringType.RENAME_CLASS);
		if(listRenames != null){
			for(SDRefactoring ref : listRenames){
				if(UtilTools.getPath(type).equals(ref.getEntityBefore().fullName())){
					String description = this.description.rename(ref.getEntityBefore().fullName(), ref.getEntityAfter().fullName());
					this.typesWithPathChanged.add(ref.getEntityAfter().fullName());
					this.addChange(type, Category.TYPE_RENAME, true, description);
					return true;
				}
			}
		}
		return false;
	}
	
	private Boolean processMoveType(final AbstractTypeDeclaration type){
		List<SDRefactoring> listRenames = this.refactorings.get(RefactoringType.MOVE_CLASS);
		if(listRenames != null){
			for(SDRefactoring ref : listRenames){
				if(UtilTools.getPath(type).equals(ref.getEntityBefore().fullName())){
					String description = this.description.move(ref.getEntityBefore().fullName(), ref.getEntityAfter().fullName());
					this.typesWithPathChanged.add(ref.getEntityAfter().fullName());
					this.addChange(type, Category.TYPE_MOVE, true, description);
					return true;
				}
			}
		}
		return false;
	}
	
	private Boolean processMoveAndRenameType(final AbstractTypeDeclaration type){
		List<SDRefactoring> listRenames = this.refactorings.get(RefactoringType.MOVE_RENAME_CLASS);
		if(listRenames != null){
			for(SDRefactoring ref : listRenames){
				if(UtilTools.getPath(type).equals(ref.getEntityBefore().fullName())){
					String description = this.description.moveAndRename(ref.getEntityBefore().fullName(), ref.getEntityAfter().fullName());
					this.typesWithPathChanged.add(ref.getEntityAfter().fullName());
					this.addChange(type, Category.TYPE_MOVE_AND_RENAME, true, description);
					return true;
				}
			}
		}
		return false;
	}
	
	private Boolean processExtractSuperType(final AbstractTypeDeclaration type){
		List<SDRefactoring> listRenames = new ArrayList<SDRefactoring>();
		
		if(this.refactorings.containsKey(RefactoringType.EXTRACT_SUPERCLASS)){
			listRenames.addAll(this.refactorings.get(RefactoringType.EXTRACT_SUPERCLASS));
		}
		if(this.refactorings.containsKey(RefactoringType.EXTRACT_INTERFACE)){
			listRenames.addAll(this.refactorings.get(RefactoringType.EXTRACT_INTERFACE));
		}
		
		if(listRenames != null){
			for(SDRefactoring ref : listRenames){
				if(UtilTools.getPath(type).equals(ref.getEntityBefore().fullName())){
					this.typesWithPathChanged.add(ref.getEntityAfter().fullName());
					this.addChange(type, Category.TYPE_EXTRACT_SUPERTYPE, false, ""); //TODO: create description
					return true;
				}
			}
		}
		return false;
	}
	
	protected void processRemoveType(final AbstractTypeDeclaration type){
		String description = this.description.remove(UtilTools.getPath(type));
		this.addChange(type, Category.TYPE_REMOVE, true, description);
	}
	
	protected Boolean checkAndProcessRefactoring(final AbstractTypeDeclaration type){
		Boolean rename = this.processRenameType(type);
		Boolean move = this.processMoveType(type);
		Boolean moveAndRename = this.processMoveAndRenameType(type);
		Boolean extract = this.processExtractSuperType(type);
		return rename || move || moveAndRename || extract;
	}

	/**
	 * Busca sub classes que tiveram mudança de super classe.
	 * 
	 * Classes que tiveram a super classe removida, são breaking change [CATEGORY_SUPER_TYPE_REMOVED].
	 * Classes que tiveram a super classe removida e estavam depreciadas na versão anterior são non-breaking change [CATEGORY_SUPER_TYPE_REMOVED_DEPRECIATED].
	 * Classes que ganharam uma super classe são non-breaking change [CATEGORY_SUPER_TYPE_ADD].
	 * Classes que tiveram a super classe modificada são breaking change [CATEGORY_SUPER_TYPE_CHANGED].
	 * Classes que tiveram a super classe modificada  e estavam depreciadas na versão anterior são non-breaking change [CATEGORY_SUPER_TYPE_CHANGED_DEPRECIATED].
	 * 
	 * @param version1
	 * @param version2
	 */
	private void findChangedSuperTypes() {
		for(AbstractTypeDeclaration accessibleTypeVersion1 : version1.getTypesPublicAndProtected()){
			AbstractTypeDeclaration accessibleTypeVersion2 = version2.getVersionAccessibleType(accessibleTypeVersion1);
			if(accessibleTypeVersion2 != null){
				String super1 = this.getNameSuperClass(accessibleTypeVersion1);  
				String super2 = this.getNameSuperClass(accessibleTypeVersion2);
				
				if(super1 != null && super2 != null){
					Boolean isBreakingChange = true;
					String nameClassComplete = UtilTools.getPath(accessibleTypeVersion2);
					String description = "";
					//Se tinha super classe, e foi removida.
					if(this.containsSuperClass(accessibleTypeVersion1) && !this.containsSuperClass(accessibleTypeVersion2)){
						description = this.description.changeSuperType(nameClassComplete, super1, "");
						this.addChange(accessibleTypeVersion2, Category.TYPE_REMOVE_SUPERCLASS, isBreakingChange, description);
					}
					//Se não tinha super classe, e foi adicionada.
					if(!this.containsSuperClass(accessibleTypeVersion1) && this.containsSuperClass(accessibleTypeVersion2)){
						description = this.description.changeSuperType(nameClassComplete, "", super2);
						this.addChange(accessibleTypeVersion2, Category.TYPE_ADD_SUPER_CLASS, false, description);
					}
					//Se tinha super classe, e foi modificada.
					if(this.containsSuperClass(accessibleTypeVersion1) && this.containsSuperClass(accessibleTypeVersion2) && !super1.equals(super2)){
						description = this.description.changeSuperType(nameClassComplete, super1, super2);
						this.addChange(accessibleTypeVersion2, Category.TYPE_CHANGE_SUPERCLASS, false, description);
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
	private void findAddTypeDeprecate() {
		for(AbstractTypeDeclaration accessibleTypeVersion1 : version1.getTypesPublicAndProtected()){
			AbstractTypeDeclaration accessibleTypeVersion2 = version2.getVersionAccessibleType(accessibleTypeVersion1);
			if(accessibleTypeVersion2 != null){
				if(!this.isDeprecated(accessibleTypeVersion1) && this.isDeprecated(accessibleTypeVersion2)){
					String description = this.description.deprecate(UtilTools.getPath(accessibleTypeVersion2));
					this.addChange(accessibleTypeVersion1, Category.TYPE_DEPRECIATE, false, description);
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
	private AbstractTypeDeclaration findTypeDeclarationInList(List<AbstractTypeDeclaration> list, AbstractTypeDeclaration type){
		for(int i=0; i< list.size(); i++){
			AbstractTypeDeclaration typeDeclaration = list.get(i);
			if(UtilTools.getPath(type).equals(UtilTools.getPath(typeDeclaration))){
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
	private void findChangedVisibilityTypes() {
		List<AbstractTypeDeclaration> listTypesVersion1 = version1.getAllTypes();
		List<AbstractTypeDeclaration> listTypesVersion2 = version2.getAllTypes();
		//Percorre types da versão anterior.
		for(AbstractTypeDeclaration type1: listTypesVersion1){
			AbstractTypeDeclaration type2 = this.findTypeDeclarationInList(listTypesVersion2, type1);
			if(type2 != null){
				String visibilityType1 = UtilTools.getVisibility(type1);
				String visibilityType2 = UtilTools.getVisibility(type2);
				if(!visibilityType1.equals(visibilityType2)){ //Se visibilidade mudou, verifica se houve perda ou ganho.
					String description = this.description.visibility(UtilTools.getPath(type2), visibilityType1, visibilityType2);
					//Breaking change: public --> qualquer modificador de acesso, protected --> qualquer modificador de acesso, exceto public.
					if(UtilTools.isVisibilityPublic(type1) || (UtilTools.isVisibilityProtected(type1) && !UtilTools.isVisibilityPublic(type2))){
						this.addChange(type2, Category.TYPE_LOST_VISIBILITY, true, description);
					}
					else{
						//non-breaking change: private ou default --> qualquer modificador de acesso, demais casos.
						Category category = UtilTools.isVisibilityDefault(type1) && UtilTools.isVisibilityPrivate(type2)? Category.TYPE_LOST_VISIBILITY: Category.TYPE_GAIN_VISIBILITY;
						this.addChange(type2, category, false, description);
					}
				}
			}
		}
	}

	/**
	 * Busca classes que foram adicionadas [CATEGORY_ADD_TYPE].
	 * @param version1
	 * @param version2
	 */
	private void findAddedTypes() {
		//Busca types na segunda versão que não estão na primeira.
		List<AbstractTypeDeclaration> listTypesVersion2 = UtilTools.getAcessibleTypes(version2);
		for (AbstractTypeDeclaration typeVersion2 : listTypesVersion2) {
			//Busca entre os types acessíveis e não acessíveis porque pode ser um type que já existia e ganhou visibilidade.
			if(!version1.containsAccessibleType(typeVersion2) && !version1.containsNonAccessibleType(typeVersion2) && !this.typesWithPathChanged.contains(UtilTools.getPath(typeVersion2))){
				String description = this.description.addition(UtilTools.getPath(typeVersion2));
				this.addChange(typeVersion2, Category.TYPE_ADD, false, description);
			}
		}
	}

	/**
	 * Busca classes que foram removidas, renomeadas ou movidas. 
	 * Se a classe removida estava depreciada na versão anterior é uma non-breaking change [CATEGORY_REMOVED_DEPRECIATED_TYPE].
	 * Caso contrário é uma breaking change [CATEGORY_REMOVED_TYPE] 
	 * @param version1
	 * @param version2
	 */
	private void findRemovedAndRenameAndMoveTypes() {
		for (AbstractTypeDeclaration type : version1.getTypesPublicAndProtected()) {
			if(!version2.containsAccessibleType(type) && !version2.containsNonAccessibleType(type)){
				Boolean refactoring = this.checkAndProcessRefactoring(type);
				if(!refactoring){
					this.processRemoveType(type);
				}
			}
		}
	}
	
	/**
	 * Retorna verdadeiro se é um type depreciado.
	 * @param type
	 * @return
	 */
	private Boolean isDeprecated(final AbstractTypeDeclaration type){
		return (type != null && type.resolveBinding() != null && type.resolveBinding().isDeprecated()) ? true: false;
	}
	
	/**
	 * Se o type está depreciado, retorna o sufixo.
	 * @param node
	 * @return
	 */
	private String getSufixDepreciated(final TypeDeclaration type){
		return this.isDeprecated(type) ? " DEPRECIATED" : "";
	}
	
	/**
	 * Retorna nome da super classe. Se não existir retorna uma string vazia.
	 * @param type
	 * @return
	 */
	private String getNameSuperClass(AbstractTypeDeclaration type){
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
	private boolean containsSuperClass(AbstractTypeDeclaration type){
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
	private void diffModifierFinal(AbstractTypeDeclaration typeVersion1, AbstractTypeDeclaration typeVersion2){
		//Se não houve mudança no identificador final.
		if((UtilTools.isFinal(typeVersion1) && UtilTools.isFinal(typeVersion2)) || ((!UtilTools.isFinal(typeVersion1) && !UtilTools.isFinal(typeVersion2)))){
			return;
		}
		//Se ganhou o modificador "final"
		if((!UtilTools.isFinal(typeVersion1) && UtilTools.isFinal(typeVersion2))){
			String description = this.description.modifierFinal(UtilTools.getPath(typeVersion2), true);
			this.addChange(typeVersion2, Category.TYPE_ADD_MODIFIER_FINAL, true, description);
		}
		else{
			//Se perdeu o modificador "final"
			String description = this.description.modifierFinal(UtilTools.getPath(typeVersion2), false);
			this.addChange(typeVersion2, Category.TYPE_REMOVE_MODIFIER_FINAL, false, description);
		}
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
	private void diffModifierStatic(AbstractTypeDeclaration typeVersion1, AbstractTypeDeclaration typeVersion2){
		//Se não houve mudança no identificador static.
		if((UtilTools.isStatic(typeVersion1) && UtilTools.isStatic(typeVersion2)) || ((!UtilTools.isStatic(typeVersion1) && !UtilTools.isStatic(typeVersion2)))){
			return;
		}
		//Se ganhou o modificador "static"
		if((!UtilTools.isStatic(typeVersion1) && UtilTools.isStatic(typeVersion2))){
			String description = this.description.modifierStatic(UtilTools.getPath(typeVersion2), true);
			this.addChange(typeVersion2, Category.TYPE_ADD_MODIFIER_STATIC, false, description);
		}
		else{
			//Se perdeu o modificador "static"
			String description = this.description.modifierStatic(UtilTools.getPath(typeVersion2), false);
			this.addChange(typeVersion2, Category.TYPE_REMOVE_MODIFIER_STATIC, true, description);
		}
		
	}
	
	/**
	 * Busca modificadores final/static que foram removidos ou adicionados.
	 * 
	 * @param version1
	 * @param version2
	 */
	private void findChangedFinalAndStatic() {
		//Percorre todos os types da versão corrente.
		for (AbstractTypeDeclaration typeVersion1 : version1.getTypesPublicAndProtected()) {
			AbstractTypeDeclaration typeVersion2 = version2.getVersionAccessibleType(typeVersion1);
			if(typeVersion2 != null){
				this.diffModifierFinal(typeVersion1, typeVersion2);
				this.diffModifierStatic(typeVersion1, typeVersion2);
			}
		}
	}
	
	private String getSimpleNameClass(AbstractTypeDeclaration typeVersion){
		return typeVersion.getName().toString();
	}
	
	private String getNamePackage(AbstractTypeDeclaration typeVersion){
		String simpleName = this.getSimpleNameClass(typeVersion);
		String nameComplete = UtilTools.getPath(typeVersion);
		nameComplete = nameComplete.replaceAll("." + simpleName, "");
		return nameComplete;
	}
}
