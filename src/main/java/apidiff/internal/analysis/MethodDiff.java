package apidiff.internal.analysis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apidiff.Change;
import apidiff.enums.Category;
import apidiff.internal.analysis.description.MethodDescription;
import apidiff.internal.util.UtilTools;
import apidiff.internal.visitor.APIVersion;
import refdiff.core.api.RefactoringType;
import refdiff.core.rm2.model.refactoring.SDRefactoring;

public class MethodDiff {
	
	private List<Change> listChange = new ArrayList<Change>();
	
	private Logger logger = LoggerFactory.getLogger(MethodDiff.class);
	
	private MethodDescription description = new MethodDescription();
	
	private Map<RefactoringType, List<SDRefactoring>> refactorings = new HashMap<RefactoringType, List<SDRefactoring>>();
	
	private List<String> methodsWithPathChanged = new ArrayList<String>();
	
	private RevCommit revCommit;

	public List<Change> detectChange(final APIVersion version1, final APIVersion version2, final Map<RefactoringType, List<SDRefactoring>> refactorings, final RevCommit revCommit) {
		this.logger.info("Processing Methods...");
		this.refactorings = refactorings;
		this.revCommit = revCommit;
		this.findRemoveAndRefactoringMethods(version1, version2);
		this.findChangedVisibilityMethods(version1, version2);
		this.findChangedReturnTypeMethods(version1, version2);
		this.findChangedExceptionTypeMethods(version1, version2);
		this.findChangedFinalAndStatic(version1, version2);
		this.findAddedMethods(version1, version2);
		this.findAddedDeprecatedMethods(version1, version2);
		return this.listChange;
	}

	private void addChange(final TypeDeclaration type, final MethodDeclaration method, Category category, Boolean isBreakingChange, final String description){
		Change change = new Change();
		change.setJavadoc(UtilTools.containsJavadoc(type, method));
		change.setDepreciated(this.isDeprecated(method, type));
		change.setBreakingChange(this.isDeprecated(method, type) ? false : isBreakingChange);
		change.setPath(UtilTools.getPath(type));
		change.setElement(this.getFullNameMethod(method));
		change.setCategory(category);
		change.setDescription(description);
		change.setRevCommit(this.revCommit);
		this.listChange.add(change);
	}
	
	private List<SDRefactoring> getAllRefactorOperations(){
		List<SDRefactoring> listMove = new ArrayList<SDRefactoring>();
		if(this.refactorings.containsKey(RefactoringType.PULL_UP_OPERATION)){
			listMove.addAll(this.refactorings.get(RefactoringType.PULL_UP_OPERATION));
		}
		if(this.refactorings.containsKey(RefactoringType.PUSH_DOWN_OPERATION)){
			listMove.addAll(this.refactorings.get(RefactoringType.PUSH_DOWN_OPERATION));
		}
		if(this.refactorings.containsKey(RefactoringType.MOVE_OPERATION)){
			listMove.addAll(this.refactorings.get(RefactoringType.MOVE_OPERATION));
		}
		if(this.refactorings.containsKey(RefactoringType.INLINE_OPERATION)){
			listMove.addAll(this.refactorings.get(RefactoringType.INLINE_OPERATION));
		}
		if(this.refactorings.containsKey(RefactoringType.RENAME_METHOD)){
			listMove.addAll(this.refactorings.get(RefactoringType.RENAME_METHOD));
		}
		if(this.refactorings.containsKey(RefactoringType.EXTRACT_OPERATION)){
			listMove.addAll(this.refactorings.get(RefactoringType.EXTRACT_OPERATION));
		}
		return listMove;
	}
	
	private Category getCategory(RefactoringType refactoringType){
		Category category;
		switch (refactoringType) {
			case MOVE_OPERATION:
				category = Category.METHOD_MOVE;
				break;
	
			case PUSH_DOWN_ATTRIBUTE:
				category = Category.METHOD_PUSH_DOWN;
				break;
				
			case INLINE_OPERATION:
				category = Category.METHOD_INLINE;
				break;
				
			case RENAME_METHOD:
				category = Category.METHOD_RENAME;
				break;
				
			case EXTRACT_OPERATION:
				category = Category.METHOD_EXTRACT;
				break;
				
			default:
				category = Category.METHOD_PULL_UP;
				break;
		}
		return category;
	}
	
	/**
	 * Processa refactoring em movimentação de métodos (move, pull up, push down, inline method) and rename.
	 * @param method
	 * @param type
	 * @return
	 */
	private Boolean processRefactorMethod(final MethodDeclaration method, final TypeDeclaration type){
		List<SDRefactoring> listMove = this.getAllRefactorOperations();
		if(listMove != null){
			for(SDRefactoring ref : listMove){
				String fullNameAndPath = this.getFullNameMethodAndPath(method, type);
				if(fullNameAndPath.equals(ref.getEntityBefore().fullName())){
					Boolean isBreakingChange = (RefactoringType.PULL_UP_OPERATION.equals(ref.getRefactoringType()) || RefactoringType.EXTRACT_OPERATION.equals(ref.getRefactoringType()))? false:true;
					Category category = this.getCategory(ref.getRefactoringType());
					String description = this.description.refactorMethod(category, ref); 
					this.addChange(type, method, category, isBreakingChange, description);
					this.methodsWithPathChanged.add(ref.getEntityAfter().fullName());
					return true;
				}
			}
		}
		return false;
	}
	

	private Boolean processChangeListParametersMethod(final MethodDeclaration method, final TypeDeclaration type){
		List<SDRefactoring> listRename = this.refactorings.get(RefactoringType.CHANGE_METHOD_SIGNATURE);
		if(listRename != null){
			for(SDRefactoring ref : listRename){
				String fullNameAndPath = this.getFullNameMethodAndPath(method, type);
				if(fullNameAndPath.equals(ref.getEntityBefore().fullName())){
					String description = this.description.parameter(ref.getEntityAfter().simpleName(), ref.getEntityBefore().simpleName(), UtilTools.getPath(type)); 
					this.addChange(type, method, Category.METHOD_CHANGE_PARAMETER_LIST, true, description);
					this.methodsWithPathChanged.add(ref.getEntityAfter().fullName());
					return true;
				}
			}
		}
		return false;
	}
	
	private void processRemoveMethod(final MethodDeclaration method, final TypeDeclaration type){
		String description = this.description.remove(this.getSimpleNameMethod(method), UtilTools.getPath(type));
		this.addChange(type, method, Category.METHOD_REMOVE, true, description);
	}

	private Boolean checkAndProcessRefactoring(final MethodDeclaration method, final TypeDeclaration type){
		Boolean moveAndRename = this.processRefactorMethod(method, type);
		Boolean listParameters = this.processChangeListParametersMethod(method, type);
		return moveAndRename || listParameters;
	}
	
	/**
	 * Verifica se uma exceção está contida na lista recebida.
	 * @param list
	 * @param ex
	 * @return
	 */
	private boolean containsExceptionList(List<SimpleType> list, SimpleType ex){
		for(SimpleType simpleType : list){
			if(simpleType.getName().toString().equals(ex.getName().toString())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Retorna verdadeiro se é um método acessível pelo cliente externo ao projeto.
	 * @param methodDeclaration
	 * @return
	 */
	private boolean isMethodAcessible(MethodDeclaration methodDeclaration){
		return methodDeclaration != null && methodDeclaration.resolveBinding() !=null && (UtilTools.isVisibilityProtected(methodDeclaration) || UtilTools.isVisibilityPublic(methodDeclaration))?true:false;
	}
	
	/**
	 * Retorna verdadeiro se existe exceções na lista 1 que não estão presentes na lista 2.
	 * @param listExceptionsVersion1
	 * @param listExceptionsVersion2
	 * @return
	 */
	private boolean diffListExceptions(List<SimpleType> listExceptionsVersion1, List<SimpleType> listExceptionsVersion2){
		for(SimpleType exceptionVersion1 : listExceptionsVersion1){
			if(!this.containsExceptionList(listExceptionsVersion2, exceptionVersion1)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Retorna verdadeiro se o método está depreciado ou a classe do método está depreciada.
	 * @param type
	 * @return
	 */
	private Boolean isDeprecated(MethodDeclaration method, AbstractTypeDeclaration type){
		Boolean isMethodDeprecated =  (method != null && method.resolveBinding() != null && method.resolveBinding().isDeprecated()) ? true: false;
		Boolean isTypeDeprecated = (type != null && type.resolveBinding() != null && type.resolveBinding().isDeprecated()) ? true: false;
		
		return isMethodDeprecated || isTypeDeprecated;
	}
	
	/**
	 * Verifica se ocorreu uma mudança na lista de exceções dos métodos. 
	 * @param version1
	 * @param version2
	 */
	private void findChangedExceptionTypeMethods(APIVersion version1, APIVersion version2) {
		for(TypeDeclaration typeVersion1 : version1.getApiAcessibleTypes()){
			if(version2.containsAccessibleType(typeVersion1)){
				for(MethodDeclaration methodVersion1 : typeVersion1.getMethods()){
					if(this.isMethodAcessible(methodVersion1)){
						MethodDeclaration methodVersion2 = version2.getEqualVersionMethod(methodVersion1, typeVersion1);
						if(this.isMethodAcessible(methodVersion2)){
							List<SimpleType> exceptionsVersion1 = methodVersion1.thrownExceptionTypes();
							List<SimpleType> exceptionsVersion2 = methodVersion2.thrownExceptionTypes();
							if(exceptionsVersion1.size() != exceptionsVersion2.size() || (this.diffListExceptions(exceptionsVersion1, exceptionsVersion2))) {
								String nameMethod = this.getSimpleNameMethod(methodVersion1);
								String nameClass = UtilTools.getPath(typeVersion1);
								String description = this.description.exception(nameMethod, exceptionsVersion1, exceptionsVersion2, nameClass);
								this.addChange(typeVersion1, methodVersion1, Category.METHOD_CHANGE_EXCEPTION_LIST, true, description);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Busca métodos que tiveram o tipo de retorno modificado.
	 * @param version1
	 * @param version2
	 */
	private void findChangedReturnTypeMethods(APIVersion version1, APIVersion version2) {
		for(TypeDeclaration typeVersion1 : version1.getApiAcessibleTypes()){
			if(version2.containsAccessibleType(typeVersion1)){
				for(MethodDeclaration methodVersion1 : typeVersion1.getMethods()){
					if(this.isMethodAcessible(methodVersion1)){
						MethodDeclaration methodVersion2 = version2.findMethodByNameAndParametersAndReturn(methodVersion1, typeVersion1);
						if(methodVersion2 == null){//Método não foi encontrado (com mesmo nome, parâmtros e tipo de retorno).
							methodVersion2 = version2.findMethodByNameAndParameters(methodVersion1, typeVersion1);
							if(methodVersion2 != null){//Existe um método com mesmo nome e parâmetros, mas tipo de retorno diferente.
								String description = this.description.returnType(this.getSimpleNameMethod(methodVersion1), UtilTools.getPath(typeVersion1));
								this.addChange(typeVersion1, methodVersion1, Category.METHOD_CHANGE_RETURN_TYPE, true, description);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Compara duas versões de um métodos e verifica se houve perda ou ganho de visibilidade.
	 * Resultaodos são registrados na saída.
	 * @param typeVersion1
	 * @param methodVersion1
	 * @param methodVersion2
	 */
	private void checkGainOrLostVisibility(TypeDeclaration typeVersion1, MethodDeclaration methodVersion1, MethodDeclaration methodVersion2){
		if(methodVersion2 != null && methodVersion1!=null){//Se o método ainda existe na versão atual.
			String visibilityMethod1 = UtilTools.getVisibility(methodVersion1);
			String visibilityMethod2 = UtilTools.getVisibility(methodVersion2);
			if(!visibilityMethod1.equals(visibilityMethod2)){ // Se o modificador de acesso foi alterado.
				String description = this.description.visibility(this.getSimpleNameMethod(methodVersion2), UtilTools.getPath(typeVersion1), visibilityMethod1, visibilityMethod2);
				//Breaking change: public --> qualquer modificador de acesso, protected --> qualquer modificador de acesso, exceto public.
				if(this.isMethodAcessible(methodVersion1) && !UtilTools.isVisibilityPublic(methodVersion2)){
					this.addChange(typeVersion1, methodVersion2, Category.METHOD_LOST_VISIBILITY, true, description);
				}
				else{
					//non-breaking change: private ou default --> qualquer modificador de acesso, demais casos.
					Category category = UtilTools.isVisibilityDefault(methodVersion1) && UtilTools.isVisibilityPrivate(methodVersion2)? Category.METHOD_LOST_VISIBILITY: Category.METHOD_GAIN_VISIBILITY;
					this.addChange(typeVersion1, methodVersion2, category, false, description);
				}
			}
		}
	}
	
	/**
	 * Busca métodos que tiveram ganho ou perda de visibilidade.
	 * @param version1
	 * @param version2
	 */
	private void findChangedVisibilityMethods(APIVersion version1, APIVersion version2) {
		for(TypeDeclaration typeVersion1 : version1.getApiAcessibleTypes()){
			if(version2.containsAccessibleType(typeVersion1)){
				for(MethodDeclaration methodVersion1 : typeVersion1.getMethods()){
					MethodDeclaration methodVersion2 = version2.getEqualVersionMethod(methodVersion1, typeVersion1);
					this.checkGainOrLostVisibility(typeVersion1, methodVersion1, methodVersion2);
				}
			}
		}
	}

	/**
	 * Busca métodos que foram depreciados.
	 * Se a classe foi depreciada, o método é considerado depreciado também.
	 * 
	 * @param version1
	 * @param version2
	 */
	private void findAddedDeprecatedMethods(APIVersion version1, APIVersion version2) {
		//Percorre todos os types acessíveis da versão 2.
		for(TypeDeclaration typeVersion2 : version2.getApiAcessibleTypes()){
			for(MethodDeclaration methodVersion2 : typeVersion2.getMethods()){
				//Se não estava depreciado na versão anterior, insere na saída.
				//Se o type foi criado depreciado, insere na saída.
				if(this.isMethodAcessible(methodVersion2) && this.isDeprecated(methodVersion2, typeVersion2)){
					MethodDeclaration methodInVersion1 = version1.findMethodByNameAndParametersAndReturn(methodVersion2, typeVersion2);
					if(methodInVersion1 == null || !this.isDeprecated(methodInVersion1, version1.getVersionAccessibleType(typeVersion2))){
						String description = this.description.deprecate(this.getSimpleNameMethod(methodVersion2), UtilTools.getPath(typeVersion2));
						this.addChange(typeVersion2, methodVersion2, Category.METHOD_DEPRECIATE, false, description);
					}
				}
			}
		}
	}

	/**
	 * Busca métodos que foram removidos.
	 * Se o type existe ainda, verifica se algum método foi removido.
	 * Se o type foi removido, os métodos dele não são contabilizados. A remoção do type é a breaking change.
	 * @param version1
	 * @param version2
	 */
	private void findRemoveAndRefactoringMethods(APIVersion version1, APIVersion version2) {
		for (TypeDeclaration typeInVersion1 : version1.getApiAcessibleTypes()) {
			if(version2.containsAccessibleType(typeInVersion1)){//Se a classe não foi removida.
				for (MethodDeclaration methodInVersion1 : typeInVersion1.getMethods()) {
					if(this.isMethodAcessible(methodInVersion1)){
						MethodDeclaration methodInVersion2 = version2.findMethodByNameAndParameters(methodInVersion1, typeInVersion1);
						if(methodInVersion2 == null){ //Se método foi removido na última versão.
							Boolean refactoring = this.checkAndProcessRefactoring(methodInVersion1, typeInVersion1);
							if(!refactoring){
								this.processRemoveMethod(methodInVersion1, typeInVersion1);
							}
						}
					}
				}
			} 
		}
	}

	/**
	 * Busca métodos que foram adicionados.
	 * Se um type foi adicionado, os métodos dele são contabilizados também.
	 * @param version1
	 * @param version2
	 */
	private void findAddedMethods(APIVersion version1, APIVersion version2) {
		for (TypeDeclaration typeInVersion2 : version2.getApiAcessibleTypes()) {
			if(version1.containsType(typeInVersion2)){//Se type já existia, verifica quais são os novos métodos.
				for(MethodDeclaration methodInVersion2: typeInVersion2.getMethods()){
					if(this.isMethodAcessible(methodInVersion2)){
						MethodDeclaration methodInVersion1 = version1.findMethodByNameAndParameters(methodInVersion2, typeInVersion2);
						String fullNameAndPathMethodVersion2 = this.getFullNameMethodAndPath(methodInVersion2, typeInVersion2);
						if(methodInVersion1 == null && !this.methodsWithPathChanged.contains(fullNameAndPathMethodVersion2)){
							String nameMethod = this.getSimpleNameMethod(methodInVersion2);
							String nameClass = UtilTools.getPath(typeInVersion2);
							String description = this.description.addition(nameMethod, nameClass);
							this.addChange(typeInVersion2, methodInVersion2, Category.METHOD_ADD, false, description);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Compara se dois métodos tem ou não o modificador "final".
	 * Registra na saída, se houver diferença.
	 * @param methodVersion1
	 * @param methodVersion2
	 */
	private void diffModifierFinal(TypeDeclaration typeVersion1, MethodDeclaration methodVersion1, MethodDeclaration methodVersion2){
		//Se não houve mudança no identificador final.
		if((UtilTools.isFinal(methodVersion1) && UtilTools.isFinal(methodVersion2)) || ((!UtilTools.isFinal(methodVersion1) && !UtilTools.isFinal(methodVersion2)))){
			return;
		}
		String nameClass = UtilTools.getPath(typeVersion1);
		String nameMethod = this.getSimpleNameMethod(methodVersion2);
		//Se ganhou o modificador "final"
		if((!UtilTools.isFinal(methodVersion1) && UtilTools.isFinal(methodVersion2))){
			String description = this.description.modifierFinal(nameMethod, nameClass, true);
			this.addChange(typeVersion1, methodVersion1, Category.METHOD_ADD_MODIFIER_FINAL, true, description);
		}
		else{
			//Se perdeu o modificador "final"
			String description = this.description.modifierFinal(nameMethod, nameClass, false);
			this.addChange(typeVersion1, methodVersion1, Category.METHOD_REMOVE_MODIFIER_FINAL, false, description);
		}
	}
	
	/**
	 * Verifica se duas versões de um método tiveram mudanças no modificador "static"
	 * Registra na saída, se houver diferença.
	 * @param methodVersion1
	 * @param methodVersion2
	 */
	private void diffModifierStatic(TypeDeclaration typeVersion1, MethodDeclaration methodVersion1, MethodDeclaration methodVersion2){
		//Se não houve mudança no identificador final.
		if((UtilTools.isStatic(methodVersion1) && UtilTools.isStatic(methodVersion2)) || ((!UtilTools.isStatic(methodVersion1) && !UtilTools.isStatic(methodVersion2)))){
			return;
		}
		String nameClass = UtilTools.getPath(typeVersion1);
		String nameMethod = this.getSimpleNameMethod(methodVersion2);
		//Se ganhou o modificador "static"
		if((!UtilTools.isStatic(methodVersion1) && UtilTools.isStatic(methodVersion2))){
			String description = this.description.modifierStatic(nameMethod, nameClass, true);
			this.addChange(typeVersion1, methodVersion1, Category.METHOD_ADD_MODIFIER_STATIC, false, description);
		}
		else{
			//Se perdeu o modificador "static"
			String description = this.description.modifierStatic(nameMethod, nameClass, false);
			this.addChange(typeVersion1, methodVersion1, Category.METHOD_REMOVE_MODIFIER_STATIC, true, description);
		}
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
			if(version2.containsType(typeVersion1)){//Se type ainda existe.
				for(MethodDeclaration methodVersion1: typeVersion1.getMethods()){
					MethodDeclaration methodVersion2 = version2.findMethodByNameAndParametersAndReturn(methodVersion1, typeVersion1);
					if(this.isMethodAcessible(methodVersion1) && (methodVersion2 != null)){
						this.diffModifierFinal(typeVersion1, methodVersion1, methodVersion2);
						this.diffModifierStatic(typeVersion1, methodVersion1, methodVersion2);
					}
				}
			}
		}
	}
	
	/**
	 * Retorna nome completo do método. [modificador de acesso + retorno + nome + ( + listas de paraâmetros + )]
	 * @param methodVersion
	 * @return
	 */
	private String getFullNameMethod(MethodDeclaration methodVersion){
		String nameMethod = "";
		if(methodVersion != null){
			String modifiersMethod = (methodVersion.modifiers() != null) ? (StringUtils.join(methodVersion.modifiers(), " ") + " ") : " ";
			String returnMethod = (methodVersion.getReturnType2() != null) ? (methodVersion.getReturnType2().toString() + " ") : "";
			String parametersMethod = (methodVersion.parameters() != null) ? StringUtils.join(methodVersion.parameters(), ", ") : " ";
			nameMethod = modifiersMethod + returnMethod + methodVersion.getName() + "(" + parametersMethod + ")";
		}
		return nameMethod;
	}
	
	/**
	 * Retorna nome do método. [nome + ( + listas de paraâmetros + )]
	 * @param methodVersion
	 * @return
	 */
	private String getSimpleNameMethod(MethodDeclaration methodVersion){
		String nameMethod = "";
		if(methodVersion != null){
			String parametersMethod = (methodVersion.parameters() != null) ? StringUtils.join(methodVersion.parameters(), ", ") : " ";
			nameMethod = methodVersion.getName() + "(" + parametersMethod + ")";
		}
		return nameMethod;
	}
	
	/**
	 * Retorna Type + Método. Exemplo: org.felines.Tiger#setAge(int)
	 * @param method
	 * @param type
	 * @return
	 */
	private String getFullNameMethodAndPath(final MethodDeclaration method, final TypeDeclaration type){
		String nameMethod = "";
		if(method != null){
			nameMethod = UtilTools.getPath(type) + "#" + method.getName() + "(" + this.getSignatureMethod(method) + ")";
		}
		return nameMethod;
	}
	
	private String getSignatureMethod(final MethodDeclaration method){
		List<String> signatureList = new ArrayList<String>();
		String signature = "";
		if(method != null){
			for(Object p : method.parameters()){
				String[] parameters = p.toString().split(" ");
				signatureList.add(parameters[parameters.length - 2]);
			}
			signature = StringUtils.join(signatureList, ", ");
		}
		return signature; 
	}

}
