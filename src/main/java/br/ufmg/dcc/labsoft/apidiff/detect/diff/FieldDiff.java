package br.ufmg.dcc.labsoft.apidiff.detect.diff;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.labsoft.apidiff.UtilTools;
import br.ufmg.dcc.labsoft.apidiff.detect.exception.BindingException;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class FieldDiff {
	
	private Logger logger = LoggerFactory.getLogger(FieldDiff.class);
	
	private final String CATEGORY_FIELD_CHANGED_DEFAULT_VALUE = "FIELD CHANGED DEFAULT VALUE";
	private final String CATEGORY_FIELD_CHANGED_DEFAULT_VALUE_DEPRECIATED = "FIELD CHANGED DEFAULT VALUE DEPRECIATED";
	
	private final String CATEGORY_FIELD_CHANGED_TYPE_FIELD = "FIELD CHANGED TYPE";
	private final String CATEGORY_FIELD_CHANGED_TYPE_FIELD_DEPRECIATED = "FIELD CHANGED TYPE FIELD DEPRECIATED";
	
	private final String CATEGORY_FIELD_LOST_MODIFIER_FINAL = "FIELD LOST MODIFIER FINAL"; //non-breaking change
	private final String CATEGORY_FIELD_GAIN_MODIFIER_FINAL = "FIELD GAIN MODIFIER FINAL"; //breaking change
	private final String CATEGORY_FIELD_GAIN_MODIFIER_FINAL_DEPRECIATED = "FIELD GAIN MODIFIER FINAL DEPRECIATED"; //non-breaking change
	
	private final String CATEGORY_FIELD_LOST_VISIBILITY = "FIELD LOST VISIBILITY";
	private final String CATEGORY_FIELD_LOST_VISIBILITY_DEPRECIATED = "FIELD LOST VISIBILITY DEPRECIATED";
	private final String CATEGORY_FIELD_GAIN_VISIBILITY = "FIELD GAIN VISIBILITY";
	
	private final String CATEGORY_FIELD_REMOVED_FIELD = "FIELD REMOVED";
	private final String CATEGORY_FIELD_REMOVED_FIELD_DEPRECIATED = "FIELD REMOVED DEPRECIATED";
	
	private final String CATEGORY_FIELD_ADD = "FIELD ADDED"; //non-breaking change
	
	private final String CATEGORY_FIELD_DEPRECIATED = "FIELD DEPRECIATED";
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	
	public Result calculateDiff(final APIVersion version1, final APIVersion version2){
		
		this.logger.info("Processing Filds...");
		
		//Lista breaking changes.
		this.findDefaultValueFields(version1, version2);
		this.findChangedTypeFields(version1, version2);
		this.findRemovedFields(version1, version2);
		this.findChangedVisibilityFields(version1, version2);
		this.findChangedFinal(version1, version2);
		this.findAddedFields(version1, version2);
		this.findAddedDeprecatedFields(version1, version2);
		
		Result result = new Result();
		result.setListBreakingChange(this.listBreakingChange);
		return result;
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
						try {
							FieldDeclaration fieldInVersion2 = version2.getVersionField(fieldInVersion1, type);
							if(this.isFildAcessible(fieldInVersion2) && this.diffValueDefaultField(fieldInVersion1, fieldInVersion2)){
								String category = this.isDeprecated(fieldInVersion1, type)? this.CATEGORY_FIELD_CHANGED_DEFAULT_VALUE_DEPRECIATED: this.CATEGORY_FIELD_CHANGED_DEFAULT_VALUE;
								category += UtilTools.getSufixJavadoc(fieldInVersion2);
								Boolean isBreakingChange = this.isDeprecated(fieldInVersion1, type)? false: true;
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), category, isBreakingChange));
							}
						} catch (BindingException e) {
							continue;
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
					if(!UtilTools.isVisibilityPrivate(fieldInVersion1)){
						FieldDeclaration fieldInVersion2;
						try {
							fieldInVersion2 = version2.getVersionField(fieldInVersion1, type);
						} catch (BindingException e) {
							continue;
						}
						if(fieldInVersion2 != null && !UtilTools.isVisibilityPrivate(fieldInVersion2)){
							if(!fieldInVersion1.getType().toString().equals(fieldInVersion2.getType().toString())){
								try {
									String category = this.isDeprecated(fieldInVersion1, type)? this.CATEGORY_FIELD_CHANGED_TYPE_FIELD_DEPRECIATED: this.CATEGORY_FIELD_CHANGED_TYPE_FIELD;
									category += UtilTools.getSufixJavadoc(fieldInVersion2);
									Boolean isBreakingChange = this.isDeprecated(fieldInVersion1, type)? false: true;
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), category, isBreakingChange));
								} catch (BindingException e) {
									continue;
								}
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
				String category = "";
				Boolean isBreakingChange = false;
				
				//Breaking change: public --> qualquer modificador de acesso, protected --> qualquer modificador de acesso, exceto public.
				if(this.isFildAcessible(fieldVersion1) && !UtilTools.isVisibilityPublic(fieldVersion2)){
					category = this.isDeprecated(fieldVersion1, typeVersion1)? this.CATEGORY_FIELD_LOST_VISIBILITY_DEPRECIATED: this.CATEGORY_FIELD_LOST_VISIBILITY;
					isBreakingChange = this.isDeprecated(fieldVersion1, typeVersion1)? false: true;
				}
				else{
					//non-breaking change: private ou default --> qualquer modificador de acesso, demais casos.
					category = UtilTools.isVisibilityDefault(fieldVersion1) && UtilTools.isVisibilityPrivate(fieldVersion2)? this.CATEGORY_FIELD_LOST_VISIBILITY: this.CATEGORY_FIELD_GAIN_VISIBILITY;
					isBreakingChange = false;
				}
				try {
					category += UtilTools.getSufixJavadoc(fieldVersion2);
					this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(typeVersion1), UtilTools.getFieldName(fieldVersion2), category, isBreakingChange));
				} catch (BindingException e) {
					this.logger.error("Erro ao ler FildName.", e);
					return;
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
					try {
						FieldDeclaration fieldVersion2 = version2.getVersionField(fieldVersion1, typeVersion1);
						this.checkGainOrLostVisibility(typeVersion1, fieldVersion1, fieldVersion2);
					} catch (BindingException e) {
						this.logger.error("Erro ao ler FildName.", e);
						continue;
					}
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
					try {
						FieldDeclaration fieldInVersion1 = version1.getVersionField(fieldVersion2, typeVersion2);
						//Se o field não estava depreciado na versão anterior ou não existia e foi criado depreciado.
						if(fieldInVersion1 == null || !this.isDeprecated(fieldInVersion1, version1.getVersionAccessibleType(typeVersion2))){
							String category = this.CATEGORY_FIELD_DEPRECIATED + UtilTools.getSufixJavadoc(fieldVersion2);
							this.listBreakingChange.add(new BreakingChange(typeVersion2.resolveBinding().getQualifiedName(),  UtilTools.getFieldName(fieldVersion2), category, false));
						}
					} catch (BindingException e) {
						continue;
					}
				}
			}
		}
	}

	private void findAddedFields(APIVersion version1, APIVersion version2) {
		
		for (TypeDeclaration type : version2.getApiAcessibleTypes()) {
			if(version1.containsAccessibleType(type)){

				for (FieldDeclaration fieldInVersion2 : type.getFields()) {
					if(!UtilTools.isVisibilityPrivate(fieldInVersion2)){
						FieldDeclaration fieldInVersion1;
						try {
							fieldInVersion1 = version1.getVersionField(fieldInVersion2, type);
							if(fieldInVersion1 == null){
								String category = this.CATEGORY_FIELD_ADD + UtilTools.getSufixJavadoc(fieldInVersion2);
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), category, false));
							}
						} catch (BindingException e) {
							continue;
						}
					}
				}
			} else{
				//tipo foi adicionado na versao 2, todos os fields foram adicionados
				for (FieldDeclaration field : type.getFields()) {
					if(!UtilTools.isVisibilityPrivate(field)){
						try {
							String category = this.CATEGORY_FIELD_ADD + UtilTools.getSufixJavadoc(field);
							this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(field), category, false));
						} catch (BindingException e) {
							continue;
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
	private void findRemovedFields(APIVersion version1, APIVersion version2) {
		
		for (TypeDeclaration type : version1.getApiAcessibleTypes()) {
			if(version2.containsAccessibleType(type)){
				for (FieldDeclaration fieldInVersion1 : type.getFields()) {

					if(!UtilTools.isVisibilityPrivate(fieldInVersion1)){
						FieldDeclaration fieldInVersion2;
						try {
							fieldInVersion2 = version2.getVersionField(fieldInVersion1, type);
							if(fieldInVersion2 == null){
								if(this.isDeprecated(fieldInVersion1, type)){
									String category = this.CATEGORY_FIELD_REMOVED_FIELD_DEPRECIATED +  UtilTools.getSufixJavadoc(fieldInVersion1);
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), category, false));
								} else{
									try {
										String category = this.CATEGORY_FIELD_REMOVED_FIELD +  UtilTools.getSufixJavadoc(fieldInVersion1);
										this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), category, true));
									} catch (BindingException e) {
										continue;
									}
								}
							}
						} catch (BindingException e) {
							continue;
						}
					}

				}
			} else{ 
				//tipo foi removido na versao 2, todos os fields foram removidos
				for (FieldDeclaration fieldInVersion1 : type.getFields()) {
					if(!UtilTools.isVisibilityPrivate(fieldInVersion1)){
						try {
							if(this.isDeprecatedField(fieldInVersion1)){
								String category = this.CATEGORY_FIELD_REMOVED_FIELD_DEPRECIATED +  UtilTools.getSufixJavadoc(fieldInVersion1);
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), category, false));
							} 
							else{
								String category = this.CATEGORY_FIELD_REMOVED_FIELD +  UtilTools.getSufixJavadoc(fieldInVersion1);
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), category, true));
							} 
						}catch (BindingException e) {
								continue;
						}
					}
				
				}
			}
		}
	}
	
	/**
	 * Retorna verdadeiro se o field está depreciado ou a classe do field está depreciada.
	 * @param field
	 * @param type
	 * @return
	 */
	private Boolean isDeprecated(FieldDeclaration field, TypeDeclaration type){
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
	private void diffModifierFinal(TypeDeclaration typeVersion1, FieldDeclaration fieldVersion1, FieldDeclaration fieldVersion2) throws BindingException{
		
		//Se não houve mudança no identificador final.
		if((UtilTools.isFinal(fieldVersion1) && UtilTools.isFinal(fieldVersion2)) || ((!UtilTools.isFinal(fieldVersion1) && !UtilTools.isFinal(fieldVersion2)))){
			return;
		}
		
		String category = "";
		Boolean isBreakingChange = false;
		//Se ganhou o modificador "final"
		if((!UtilTools.isFinal(fieldVersion1) && UtilTools.isFinal(fieldVersion2))){
			category = this.isDeprecated(fieldVersion1, typeVersion1)?this.CATEGORY_FIELD_GAIN_MODIFIER_FINAL_DEPRECIATED:CATEGORY_FIELD_GAIN_MODIFIER_FINAL;
			isBreakingChange = this.isDeprecated(fieldVersion1, typeVersion1)?false:true;
		}
		else{
			//Se perdeu o modificador "final"
			category = this.CATEGORY_FIELD_LOST_MODIFIER_FINAL;
			isBreakingChange = false;
		}
		category += UtilTools.getSufixJavadoc(fieldVersion2);
		this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldVersion2), category, isBreakingChange));
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
					try {
						FieldDeclaration fieldVersion2 = version2.getVersionField(fieldVersion1, typeInVersion1);
						if(this.isFildAcessible(fieldVersion1) && (fieldVersion2 != null)){
							this.diffModifierFinal(typeInVersion1, fieldVersion1, fieldVersion2);
						}
					} catch (BindingException e) {
						this.logger.error("Erro reading field path", e);
						continue;
					}
				}
			}
		}
	}
	
}
