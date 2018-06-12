package apidiff.internal.analysis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apidiff.Change;
import apidiff.enums.Category;
import apidiff.internal.analysis.description.FieldDescription;
import apidiff.internal.exception.BindingException;
import apidiff.internal.util.UtilTools;
import apidiff.internal.visitor.APIVersion;
import refdiff.core.api.RefactoringType;
import refdiff.core.rm2.model.refactoring.SDRefactoring;

public class FieldDiff {
	
	private Logger logger = LoggerFactory.getLogger(FieldDiff.class);
	
	private Map<RefactoringType, List<SDRefactoring>> refactorings = new HashMap<RefactoringType, List<SDRefactoring>>();
	
	private List<String> fieldWithPathChanged = new ArrayList<String>();
	
	private List<Change> listChange = new ArrayList<Change>();
	
	private FieldDescription description = new FieldDescription();
	
	private RevCommit revCommit;
	
	public List<Change> detectChange(final APIVersion version1, final APIVersion version2, final Map<RefactoringType, List<SDRefactoring>> refactorings, final RevCommit revCommit){
		this.logger.info("Processing Filds...");
		this.refactorings = refactorings;
		this.revCommit = revCommit;
		this.findDefaultValueFields(version1, version2);
		this.findChangedTypeFields(version1, version2);
		this.findRemoveAndRefactoringFields(version1, version2);
		this.findChangedVisibilityFields(version1, version2);
		this.findChangedFinal(version1, version2);
		this.findAddedFields(version1, version2);
		this.findAddedDeprecatedFields(version1, version2);
		return this.listChange;
	}
	
	private void addChange(final TypeDeclaration type, final FieldDeclaration field, Category category, Boolean isBreakingChange, final String description){
		
		String name = UtilTools.getFieldName(field);
		if(name != null){
			Change change = new Change();
			change.setJavadoc(UtilTools.containsJavadoc(type, field));
			change.setDeprecated(this.isDeprecated(field, type));
			change.setBreakingChange(this.isDeprecated(field, type) ? false : isBreakingChange);
			change.setPath(UtilTools.getPath(type));
			change.setElement(name);
			change.setCategory(category);
			change.setDescription(description);
			change.setRevCommit(this.revCommit);
			isBreakingChange = this.isDeprecated(field, type) ? false : isBreakingChange;
			this.listChange.add(change);
		}
		else{
			this.logger.error("Removing field with null name " + field);
		}

	}
	
	private List<SDRefactoring> getAllMoveOperations(){
		List<SDRefactoring> listMove = new ArrayList<SDRefactoring>();
		if(this.refactorings.containsKey(RefactoringType.PULL_UP_ATTRIBUTE)){
			listMove.addAll(this.refactorings.get(RefactoringType.PULL_UP_ATTRIBUTE));
		}
		if(this.refactorings.containsKey(RefactoringType.PUSH_DOWN_ATTRIBUTE)){
			listMove.addAll(this.refactorings.get(RefactoringType.PUSH_DOWN_ATTRIBUTE));
		}
		if(this.refactorings.containsKey(RefactoringType.MOVE_ATTRIBUTE)){
			listMove.addAll(this.refactorings.get(RefactoringType.MOVE_ATTRIBUTE));
		}
		return listMove;
	}
	
	private Category getCategory(RefactoringType refactoringType){
		switch (refactoringType) {
			case PULL_UP_ATTRIBUTE:
				return Category.FIELD_PULL_UP;
				
			case PUSH_DOWN_ATTRIBUTE:
				return Category.FIELD_PUSH_DOWN;
				
			default:
				return Category.FIELD_MOVE;
		}
	}
	
	private Boolean processMoveField(final FieldDeclaration field, final TypeDeclaration type){
		List<SDRefactoring> listMove = this.getAllMoveOperations();
		if(listMove != null){
			for(SDRefactoring ref : listMove){
				String fullNameAndPath = this.getNameAndPath(field, type);
				if(fullNameAndPath.equals(ref.getEntityBefore().fullName())){
					Boolean isBreakingChange = RefactoringType.PULL_UP_OPERATION.equals(ref.getRefactoringType())? false:true;
					Category category = this.getCategory(ref.getRefactoringType());
					String description = this.description.refactorField(category, ref);
					this.addChange(type, field, category, isBreakingChange, description);
					this.fieldWithPathChanged.add(ref.getEntityAfter().fullName());
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @param field
	 * @param type
	 * @return Return Name class + name field (e.g. : org.lib.Math#value)
	 */
	private String getNameAndPath(final FieldDeclaration field, final TypeDeclaration type){
		return UtilTools.getPath(type) + "#" + UtilTools.getFieldName(field);
	}
	
	private Boolean processRemoveField(final FieldDeclaration field, final TypeDeclaration type){
		String description = this.description.remove( UtilTools.getFieldName(field), UtilTools.getPath(type));
		this.addChange(type, field, Category.FIELD_REMOVE, true, description);
		return false;
	}
	
	private Boolean checkAndProcessRefactoring(final FieldDeclaration field, final TypeDeclaration type){
		return this.processMoveField(field, type);
	}
	
	/**
	 * Retorna verdadeiro se existe diferença entre os valores default de dois fields.
	 * @param fieldInVersion1
	 * @param fieldInVersion2
	 * @return
	 */
	private Boolean diffValueDefaultField(FieldDeclaration fieldInVersion1, FieldDeclaration fieldInVersion2){
		
		List<VariableDeclarationFragment> variable1Fragments = fieldInVersion1.fragments();
		List<VariableDeclarationFragment> variable2Fragments = fieldInVersion2.fragments();
		
		Expression valueVersion1 = variable1Fragments.get(0).getInitializer();
		Expression valueVersion2 = variable2Fragments.get(0).getInitializer();
		
		//Se um valor default foi removido ou adicionado.
		if((valueVersion1 == null && valueVersion2 != null) || (valueVersion1 != null && valueVersion2 == null)){
			return true;
		}
		
		//Se as duas versões possuem um valor default, verifica se são diferentes.
		if((valueVersion1 != null && valueVersion2 != null) && (!valueVersion1.toString().equals(valueVersion2.toString()))){
			return true;
		}
		
		return false;
	}
	/**
	 * Verifica se o valor default do field foi modificado.
	 * @param version1
	 * @param version2
	 */
	private void findDefaultValueFields(APIVersion version1, APIVersion version2){
		for (TypeDeclaration type : version1.getApiAcessibleTypes()) {
			if(version2.containsAccessibleType(type)){
				for (FieldDeclaration fieldInVersion1 : type.getFields()) {
					if(this.isFildAcessible(fieldInVersion1)){
						FieldDeclaration fieldInVersion2 = version2.getVersionField(fieldInVersion1, type);
						if(this.isFildAcessible(fieldInVersion2) && this.diffValueDefaultField(fieldInVersion1, fieldInVersion2)){
							String description = this.description.changeDefaultValue(UtilTools.getFieldName(fieldInVersion2), UtilTools.getPath(type));
							this.addChange(type, fieldInVersion2, Category.FIELD_CHANGE_DEFAULT_VALUE, true, description);
						}
					}
				}
			}
		}
	}

	private void findChangedTypeFields(APIVersion version1, APIVersion version2) {
		for (TypeDeclaration type : version1.getApiAcessibleTypes()) {
			if(version2.containsAccessibleType(type)){
				for (FieldDeclaration fieldInVersion1 : type.getFields()) {
					if(!UtilTools.isVisibilityPrivate(fieldInVersion1) && !UtilTools.isVisibilityDefault(fieldInVersion1)){
						FieldDeclaration fieldInVersion2 = version2.getVersionField(fieldInVersion1, type);
						if(fieldInVersion2 != null && !UtilTools.isVisibilityPrivate(fieldInVersion2)){
							if(!fieldInVersion1.getType().toString().equals(fieldInVersion2.getType().toString())){
								String description = this.description.returnType(UtilTools.getFieldName(fieldInVersion2), UtilTools.getPath(type));
								this.addChange(type, fieldInVersion2, Category.FIELD_CHANGE_TYPE, true, description);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Compara duas versões de um field e verifica se houve perda ou ganho de visibilidade.
	 * Resultados são registrados na saída.
	 * @param typeVersion1
	 * @param fieldVersion1
	 * @param fieldVersion2
	 */
	private void checkGainOrLostVisibility(TypeDeclaration typeVersion1, FieldDeclaration fieldVersion1, FieldDeclaration fieldVersion2){
		if(fieldVersion2 != null && fieldVersion1!=null){//Se o método ainda existe na versão atual.
			String visibilityMethod1 = UtilTools.getVisibility(fieldVersion1);
			String visibilityMethod2 = UtilTools.getVisibility(fieldVersion2);
			if(!visibilityMethod1.equals(visibilityMethod2)){ // Se o modificador de acesso foi alterado.
				String description = this.description.visibility(UtilTools.getFieldName(fieldVersion1), UtilTools.getPath(typeVersion1), visibilityMethod1, visibilityMethod2);
				//Breaking change: public --> qualquer modificador de acesso, protected --> qualquer modificador de acesso, exceto public.
				if(this.isFildAcessible(fieldVersion1) && !UtilTools.isVisibilityPublic(fieldVersion2)){
					this.addChange(typeVersion1, fieldVersion1, Category.FIELD_LOST_VISIBILITY, true, description);
				}
				else{
					//non-breaking change: private ou default --> qualquer modificador de acesso, demais casos.
					Category category = UtilTools.isVisibilityDefault(fieldVersion1) && UtilTools.isVisibilityPrivate(fieldVersion2)? Category.FIELD_LOST_VISIBILITY: Category.FIELD_GAIN_VISIBILITY;
					this.addChange(typeVersion1, fieldVersion1, category, false, description);
				}
			}
		}
	}

	/**
	 * Busca fields que tiveram ganho ou perda de visibilidade.
	 * @param version1
	 * @param version2
	 */
	private void findChangedVisibilityFields(APIVersion version1, APIVersion version2) {
		for(TypeDeclaration typeVersion1 : version1.getApiAcessibleTypes()){
			if(version2.containsAccessibleType(typeVersion1)){
				for (FieldDeclaration fieldVersion1 : typeVersion1.getFields()){
					FieldDeclaration fieldVersion2 = version2.getVersionField(fieldVersion1, typeVersion1);
					this.checkGainOrLostVisibility(typeVersion1, fieldVersion1, fieldVersion2);
				}
			}
		}
	}

	/**
	 * Busca fields que foram depreciados.
	 * @param version1
	 * @param version2
	 */
	private void findAddedDeprecatedFields(APIVersion version1, APIVersion version2) {
		//Percorre todos os types acessíveis da versão 2.
		for(TypeDeclaration typeVersion2 : version2.getApiAcessibleTypes()){
			for(FieldDeclaration fieldVersion2 : typeVersion2.getFields()){
				//Se não estava depreciado na versão anterior, insere na saída.
				//Se o type foi criado depreciado, insere na saída.
				if(this.isFildAcessible(fieldVersion2) && this.isDeprecated(fieldVersion2, typeVersion2)){
					FieldDeclaration fieldInVersion1 = version1.getVersionField(fieldVersion2, typeVersion2);
					//Se o field não estava depreciado na versão anterior ou não existia e foi criado depreciado.
					if(fieldInVersion1 == null || !this.isDeprecated(fieldInVersion1, version1.getVersionAccessibleType(typeVersion2))){
						String description = this.description.deprecate(UtilTools.getFieldName(fieldVersion2), UtilTools.getPath(typeVersion2));
						this.addChange(typeVersion2, fieldVersion2, Category.FIELD_DEPRECATED, false, description);
					}
				}
			}
		}
	}

	private void findAddedFields(APIVersion version1, APIVersion version2) {
		for (TypeDeclaration typeVersion2 : version2.getApiAcessibleTypes()) {
			if(version1.containsAccessibleType(typeVersion2)){
				for (FieldDeclaration fieldInVersion2 : typeVersion2.getFields()) {
					String fullNameAndPath = this.getNameAndPath(fieldInVersion2, typeVersion2);
					if(!UtilTools.isVisibilityPrivate(fieldInVersion2) && !UtilTools.isVisibilityDefault(fieldInVersion2) && !this.fieldWithPathChanged.contains(fullNameAndPath)){
						FieldDeclaration fieldInVersion1;
							fieldInVersion1 = version1.getVersionField(fieldInVersion2, typeVersion2);
							if(fieldInVersion1 == null){
								String description = this.description.addition(UtilTools.getFieldName(fieldInVersion2), UtilTools.getPath(typeVersion2));
								this.addChange(typeVersion2, fieldInVersion2, Category.FIELD_ADD, false, description);
							}
					}
				}
			} 
		}
	}

	/**
	 * Busca field que foram removidos.
	 * Se a classe foi removida, os fields não são analisados. A remoção da classe é a breaking change.
	 * @param version1
	 * @param version2
	 */
	private void findRemoveAndRefactoringFields(APIVersion version1, APIVersion version2) {
		for (TypeDeclaration type : version1.getApiAcessibleTypes()) {
			if(version2.containsAccessibleType(type)){
				for (FieldDeclaration fieldInVersion1 : type.getFields()) {
					if(!UtilTools.isVisibilityPrivate(fieldInVersion1)){
						FieldDeclaration fieldInVersion2 = version2.getVersionField(fieldInVersion1, type);
						if(fieldInVersion2 == null){
							Boolean refactoring = this.checkAndProcessRefactoring(fieldInVersion1, type);
							if(!refactoring){
								this.processRemoveField(fieldInVersion1, type);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Se o type está depreciado, retorna o sufixo.
	 * @param node
	 * @return
	 */
	private String getSufixDepreciated(final FieldDeclaration field, final AbstractTypeDeclaration type){
		return this.isDeprecated(field, type) ? " DEPRECIATED" : "";
	}
	
	/**
	 * Retorna verdadeiro se o field está depreciado ou a classe do field está depreciada.
	 * @param field
	 * @param type
	 * @return
	 */
	private Boolean isDeprecated(FieldDeclaration field, AbstractTypeDeclaration type){
		Boolean isFildDeprecated = this.isDeprecatedField(field);
		Boolean isTypeDeprecated = (type != null && type.resolveBinding() != null && type.resolveBinding().isDeprecated()) ? true: false;
		return isFildDeprecated || isTypeDeprecated;
	}
	
	/**
	 * Verifica se um field está depreciado.
	 * @param field
	 * @return
	 */
	private Boolean isDeprecatedField(FieldDeclaration field){
		if(field != null){
			List<VariableDeclarationFragment> variableFragments = field.fragments();
			for (VariableDeclarationFragment variableDeclarationFragment : variableFragments) {
				if(variableDeclarationFragment.resolveBinding() != null &&	variableDeclarationFragment.resolveBinding().isDeprecated()){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Retorna verdadeiro se é um método acessível pelo cliente externo ao projeto.
	 * @param methodDeclaration
	 * @return
	 */
	private boolean isFildAcessible(FieldDeclaration field){
		return field != null && (UtilTools.isVisibilityProtected(field) || UtilTools.isVisibilityPublic(field))?true:false;
	}
	
	/**
	 * Compara se dois campos tem ou não o modificador "final".
	 * Registra na saída, se houver diferença.
	 * @param fieldVersion1
	 * @param fieldVersion2
	 * @throws BindingException 
	 */
	private void diffModifierFinal(TypeDeclaration typeVersion1, FieldDeclaration fieldVersion1, FieldDeclaration fieldVersion2){
		//Se não houve mudança no identificador final.
		if((UtilTools.isFinal(fieldVersion1) && UtilTools.isFinal(fieldVersion2)) || ((!UtilTools.isFinal(fieldVersion1) && !UtilTools.isFinal(fieldVersion2)))){
			return;
		}
		String description = "";
		//Se ganhou o modificador "final"
		if((!UtilTools.isFinal(fieldVersion1) && UtilTools.isFinal(fieldVersion2))){
			description = this.description.modifierFinal(UtilTools.getFieldName(fieldVersion2), UtilTools.getPath(typeVersion1), true);
			this.addChange(typeVersion1, fieldVersion2, Category.FIELD_ADD_MODIFIER_FINAL, true, description);
		}
		else{
			//Se perdeu o modificador "final"
			description = this.description.modifierFinal(UtilTools.getFieldName(fieldVersion2), UtilTools.getPath(typeVersion1), false);
			this.addChange(typeVersion1, fieldVersion2, Category.FIELD_REMOVE_MODIFIER_FINAL, false, description);
		}
	}
	
	/**
	 * Busca modificador "final" removido ou adicionado.
	 * 
	 * @param version1
	 * @param version2
	 */
	private void findChangedFinal(APIVersion version1, APIVersion version2) {
		//Percorre todos os types da versão corrente.
		for (TypeDeclaration typeInVersion1 : version1.getApiAcessibleTypes()) {
			if(version2.containsType(typeInVersion1)){//Se type ainda existe.
				for(FieldDeclaration fieldVersion1: typeInVersion1.getFields()){
					FieldDeclaration fieldVersion2 = version2.getVersionField(fieldVersion1, typeInVersion1);
					if(this.isFildAcessible(fieldVersion1) && (fieldVersion2 != null)){
						this.diffModifierFinal(typeInVersion1, fieldVersion1, fieldVersion2);
					}
				}
			}
		}
	}
	
}
