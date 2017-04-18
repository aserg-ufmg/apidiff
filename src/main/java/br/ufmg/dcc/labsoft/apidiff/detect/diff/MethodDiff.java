package br.ufmg.dcc.labsoft.apidiff.detect.diff;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.labsoft.apidiff.UtilTools;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class MethodDiff {
	
	private final String CATEGORY_METHOD_CHANGED_EXCEPTION = "METHOD CHANGED EXCEPTION"; //breaking change
	private final String CATEGORY_METHOD_CHANGED_EXCEPTION_DEPRECIATED = "METHOD CHANGED EXCEPTION DEPRECIATED"; //non-breaking change
	
	private final String CATEGORY_METHOD_LOST_MODIFIER_FINAL = "METHOD LOST MODIFIER FINAL"; //non-breaking change
	private final String CATEGORY_METHOD_GAIN_MODIFIER_FINAL = "METHOD GAIN MODIFIER FINAL"; //breaking change
	private final String CATEGORY_METHOD_GAIN_MODIFIER_FINAL_DEPRECIATED = "METHOD GAIN MODIFIER FINAL DEPRECIATED"; //non-breaking change
	
	private final String CATEGORY_METHOD_LOST_MODIFIER_STATIC = "METHOD LOST MODIFIER STATIC"; //breaking change
	private final String CATEGORY_METHOD_LOST_MODIFIER_STATIC_DEPRECIATED = "METHOD LOST MODIFIER STATIC DEPRECIATED"; //non-breaking change
	private final String CATEGORY_METHOD_GAIN_MODIFIER_STATIC = "METHOD GAIN MODIFIER STATIC"; //non-breaking change
	
	private final String CATEGORY_METHOD_CHANGED_PARAMETERS = "METHOD CHANGED PARAMETERS"; //breaking change
	private final String CATEGORY_METHOD_CHANGED_PARAMETERS_DEPRECIATED = "METHOD CHANGED PARAMETERS DEPRECIATED"; //non-breaking change
	
	private final String CATEGORY_METHOD_CHANGED_RETURN_TYPE = "METHOD CHANGED RETURN TYPE"; //breaking change
	private final String CATEGORY_METHOD_CHANGED_RETURN_TYPE_DEPRECIATED = "METHOD CHANGED RETURN TYPE DEPRECIATED"; //non-breaking change
	
	private final String CATEGORY_METHOD_REMOVED = "METHOD REMOVED"; //breaking change
	private final String CATEGORY_METHOD_REMOVED_DEPRECIATED = "METHOD REMOVED DEPRECIATED"; //non-breaking change
	
	private final String CATEGORY_METHOD_ADDED = "METHOD ADDED"; //non-breaking change
	
	private final String CATEGORY_METHOD_DEPRECIATED = "METHOD DEPRECIATED"; //non-breaking change
	
	private final String CATEGORY_METHOD_LOST_VISIBILITY = "METHOD LOST VISIBILITY"; //breaking change
	private final String CATEGORY_METHOD_LOST_VISIBILITY_DEPRECIATED = "METHOD LOST VISIBILITY DEPRECIATED"; //non-breaking change
	private final String CATEGORY_METHOD_GAIN_VISIBILITY = "METHOD GAIN VISIBILITY"; //non-breaking change
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	
	private Logger logger = LoggerFactory.getLogger(MethodDiff.class);

	public Result calculateDiff(final APIVersion version1, final APIVersion version2) {
		
		this.logger.info("Processing Methods...");
		
		//Lista breaking changes.
		this.findRemovedMethods(version1, version2);
		this.findChangedVisibilityMethods(version1, version2);
		this.findChangedReturnTypeMethods(version1, version2);
		this.findChangedParametersMethods(version1, version2);
		this.findChangedExceptionTypeMethods(version1, version2);
		this.findChangedFinalAndStatic(version1, version2);
		
		//Lista non-breaking changes.
		this.findAddedMethods(version1, version2);
		this.findAddedDeprecatedMethods(version1, version2);
		
		Result result = new Result();
		result.setListBreakingChange(this.listBreakingChange);
		return result;
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
	private Boolean isDeprecated(MethodDeclaration method, TypeDeclaration type){
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
							
							String category = this.isDeprecated(methodVersion1, typeVersion1)? this.CATEGORY_METHOD_CHANGED_EXCEPTION_DEPRECIATED: this.CATEGORY_METHOD_CHANGED_EXCEPTION;
							category += UtilTools.getSufixJavadoc(methodVersion2);
							Boolean isBreakingChange = this.isDeprecated(methodVersion1, typeVersion1)? false: true;
							
							if(exceptionsVersion1.size() != exceptionsVersion2.size() || (this.diffListExceptions(exceptionsVersion1, exceptionsVersion2))) {
								this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion1.getName().toString(), category, isBreakingChange));
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Retorna verdadeiro se dois métodos tem assinaturas diferentes, incluindo a ordem dos parâmetros.
	 * Exemplo:
	 * 
	 * int, String -- int, String : iguais.
	 * String, Integer -- Integer, String: Diferentes.
	 * float, boolean -- boolean: diferentes.
	 * 
	 * @return
	 */
	private Boolean diffListParameter(MethodDeclaration methodVersion1, MethodDeclaration methodVersion2){
		//Se a lista de parâmetros tem tamanhos diferentes.
		if(methodVersion1.parameters().size() != methodVersion2.parameters().size()){
			return true;
		}
		else{
			//Se tem o mesma quantidade de parâmetros, mas eles são diferentes.
			for(int i = 0; i < methodVersion1.parameters().size(); i++){
				String parameterVersion1[] = methodVersion1.parameters().get(i).toString().split(" ");
				String parameterVersion2[] = methodVersion2.parameters().get(i).toString().split(" ");
				if(parameterVersion1.length == 2 && parameterVersion2.length == 2 && !parameterVersion1[0].equals(parameterVersion2[0])){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Verifica se ocorreu mudanças na assinatura dos métodos. 
	 * Mundanças: parâmetro foi adicionado, removido, trocado, ou ordem dos parâmetros foi modificada.
	 * @param version1
	 * @param version2
	 */
	private void findChangedParametersMethods(APIVersion version1, APIVersion version2) {
		
		for(TypeDeclaration typeVersion1 : version1.getApiAcessibleTypes()){
			if(version2.containsAccessibleType(typeVersion1)){
				//Percorre os métodos da classe.
				for(MethodDeclaration methodVersion1 : typeVersion1.getMethods()){
					//Se era um método acessível na versão anterior, e não foi entrado na versão atual, pode ser uma mudança de parâmetros.
					if(this.isMethodAcessible(methodVersion1) && (version2.getEqualVersionMethod(methodVersion1, typeVersion1) == null)){
						//Busca métodos na versão 2 com o mesmo nome.
						ArrayList<MethodDeclaration> methodsVersion2 = version2.getAllEqualMethodsByName(methodVersion1, typeVersion1);
						for(MethodDeclaration methodVersion2: methodsVersion2){
							if(this.isMethodAcessible(methodVersion2) && version1.getEqualVersionMethod(methodVersion2, typeVersion1) == null && this.diffListParameter(methodVersion1, methodVersion2)){
								String category = this.isDeprecated(methodVersion1, typeVersion1)? this.CATEGORY_METHOD_CHANGED_PARAMETERS_DEPRECIATED: this.CATEGORY_METHOD_CHANGED_PARAMETERS;
								category += UtilTools.getSufixJavadoc(methodVersion2);
								Boolean isBreakingChange = this.isDeprecated(methodVersion1, typeVersion1)? false: true;
								this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion1.getName().toString(), category, isBreakingChange));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Retorna verdadeiro se os métodos tem tipos de retornos diferentes.
	 * Falso se os métodos são iguais.
	 * @param methodVersion1
	 * @param methodVersion2
	 * @return
	 */
	private Boolean diffReturnType(MethodDeclaration methodVersion1, MethodDeclaration methodVersion2){
		Type returnType1 = methodVersion1.getReturnType2();
		Type returnType2 = methodVersion2.getReturnType2();
		
		if(returnType1 != null && returnType2 != null &&  !returnType1.toString().equals(returnType2.toString())){
			return true;
		}
		return false;
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
						MethodDeclaration methodVersion2 = version2.getEqualVersionMethod(methodVersion1, typeVersion1);
						if(this.isMethodAcessible((methodVersion2))){
							if(this.diffReturnType(methodVersion1, methodVersion2)){
								String category = this.isDeprecated(methodVersion1, typeVersion1)? this.CATEGORY_METHOD_CHANGED_RETURN_TYPE_DEPRECIATED: this.CATEGORY_METHOD_CHANGED_RETURN_TYPE;
								category += UtilTools.getSufixJavadoc(methodVersion2);
								Boolean isBreakingChange = this.isDeprecated(methodVersion1, typeVersion1)? false: true;
								this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion1.getName().toString(), category, isBreakingChange));
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
				String category = "";
				Boolean isBreakingChange = false;
				
				//Breaking change: public --> qualquer modificador de acesso, protected --> qualquer modificador de acesso, exceto public.
				if(this.isMethodAcessible(methodVersion1) && !UtilTools.isVisibilityPublic(methodVersion2)){
					category = this.isDeprecated(methodVersion1, typeVersion1)? this.CATEGORY_METHOD_LOST_VISIBILITY_DEPRECIATED: this.CATEGORY_METHOD_LOST_VISIBILITY;
					isBreakingChange = this.isDeprecated(methodVersion1, typeVersion1)? false: true;
				}
				else{
					//non-breaking change: private ou default --> qualquer modificador de acesso, demais casos.
					category = UtilTools.isVisibilityDefault(methodVersion1) && UtilTools.isVisibilityPrivate(methodVersion2)? this.CATEGORY_METHOD_LOST_VISIBILITY: this.CATEGORY_METHOD_GAIN_VISIBILITY;
					isBreakingChange = false;
				}
				category += UtilTools.getSufixJavadoc(methodVersion2);
				this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(typeVersion1), methodVersion2.getName().toString(), category, isBreakingChange));
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
					MethodDeclaration methodInVersion1 = version1.getEqualVersionMethod(methodVersion2, typeVersion2);
					if(methodInVersion1 == null || !this.isDeprecated(methodInVersion1, version1.getVersionAccessibleType(typeVersion2))){
						String category =  this.CATEGORY_METHOD_DEPRECIATED +  UtilTools.getSufixJavadoc(methodVersion2);
						this.listBreakingChange.add(new BreakingChange(typeVersion2.resolveBinding().getQualifiedName(), methodVersion2.getName().toString(), category,false));
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
	private void findRemovedMethods(APIVersion version1, APIVersion version2) {
		
		for (TypeDeclaration typeInVersion1 : version1.getApiAcessibleTypes()) {
			if(version2.containsAccessibleType(typeInVersion1)){//Se a classe não foi removida.
				for (MethodDeclaration methodInVersion1 : typeInVersion1.getMethods()) {
					if(this.isMethodAcessible(methodInVersion1)){
						MethodDeclaration methodInVersion2 = version2.getEqualVersionMethod(methodInVersion1, typeInVersion1);
						if(methodInVersion2 == null){ //Se método foi removido na última versão.
							String category = this.isDeprecated(methodInVersion1, typeInVersion1)? this.CATEGORY_METHOD_REMOVED_DEPRECIATED: this.CATEGORY_METHOD_REMOVED;
							category += UtilTools.getSufixJavadoc(methodInVersion1);
							Boolean isBreakingChange = this.isDeprecated(methodInVersion1, typeInVersion1)? false: true;
							this.listBreakingChange.add(new BreakingChange(typeInVersion1.resolveBinding().getQualifiedName(), methodInVersion1.getName().toString(), category, isBreakingChange));
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
		
		String category = this.CATEGORY_METHOD_ADDED;
		
		for (TypeDeclaration typeInVersion2 : version2.getApiAcessibleTypes()) {
			if(version1.containsType(typeInVersion2)){//Se type já existia, verifica quais são os novos métodos.
				for(MethodDeclaration methodInVersion2: typeInVersion2.getMethods()){
					if(this.isMethodAcessible(methodInVersion2)){
						MethodDeclaration methodInVersion1 = version1.getEqualVersionMethod(methodInVersion2, typeInVersion2);
						if(methodInVersion1 == null){
							category += UtilTools.getSufixJavadoc(methodInVersion1);
							this.listBreakingChange.add(new BreakingChange(typeInVersion2.resolveBinding().getQualifiedName(), methodInVersion2.getName().toString(), category, false));
						}
					}
				}
			} else {
				for(MethodDeclaration methodInVersion2: typeInVersion2.getMethods()){//Se type foi adicionado.
					if(this.isMethodAcessible(methodInVersion2)){
						category += UtilTools.getSufixJavadoc(methodInVersion2);
						this.listBreakingChange.add(new BreakingChange(typeInVersion2.resolveBinding().getQualifiedName(), methodInVersion2.getName().toString(), category, false));
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
		String category = "";
		Boolean isBreakingChange = false;
		//Se ganhou o modificador "final"
		if((!UtilTools.isFinal(methodVersion1) && UtilTools.isFinal(methodVersion2))){
			category = this.isDeprecated(methodVersion1, typeVersion1)?this.CATEGORY_METHOD_GAIN_MODIFIER_FINAL_DEPRECIATED:CATEGORY_METHOD_GAIN_MODIFIER_FINAL;
			isBreakingChange = this.isDeprecated(methodVersion1, typeVersion1)?false:true;
		}
		else{
			//Se perdeu o modificador "final"
			category = this.CATEGORY_METHOD_LOST_MODIFIER_FINAL;
			isBreakingChange = false;
		}
		category += UtilTools.getSufixJavadoc(methodVersion2);
		this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion2.getName().toString(), category, isBreakingChange));
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
		String category = "";
		Boolean isBreakingChange = false;
		//Se ganhou o modificador "static"
		if((!UtilTools.isStatic(methodVersion1) && UtilTools.isStatic(methodVersion2))){
			category = CATEGORY_METHOD_GAIN_MODIFIER_STATIC;
			isBreakingChange = false;
		}
		else{
			//Se perdeu o modificador "static"
			category = this.isDeprecated(methodVersion1, typeVersion1)?this.CATEGORY_METHOD_LOST_MODIFIER_STATIC_DEPRECIATED:CATEGORY_METHOD_LOST_MODIFIER_STATIC;
			isBreakingChange = this.isDeprecated(methodVersion1, typeVersion1)?false:true;
		}
		category += UtilTools.getSufixJavadoc(methodVersion2);
		this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion2.getName().toString(), category, isBreakingChange));
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
					MethodDeclaration methodVersion2 = version2.getEqualVersionMethod(methodVersion1, typeVersion1);
					if(this.isMethodAcessible(methodVersion1) && (methodVersion2 != null)){
						this.diffModifierFinal(typeVersion1, methodVersion1, methodVersion2);
						this.diffModifierStatic(typeVersion1, methodVersion1, methodVersion2);
					}
				}
			}
		}
	}

}
