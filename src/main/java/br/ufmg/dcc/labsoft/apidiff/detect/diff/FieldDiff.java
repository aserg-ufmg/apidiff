package br.ufmg.dcc.labsoft.apidiff.detect.diff;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import br.ufmg.dcc.labsoft.apidiff.UtilTools;
import br.ufmg.dcc.labsoft.apidiff.detect.exception.BindingException;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class FieldDiff {
	
	private final String CATEGORY_FIELD_CHANGED_DEFAULT_VALUE = "FIELD CHANGED DEFAULT VALUE";
	private final String CATEGORY_FIELD_CHANGED_DEFAULT_VALUE_DEPRECIATED = "FIELD CHANGED DEFAULT VALUE DEPRECIATED";
	
	private final String CATEGORY_FIELD_CHANGED_TYPE_FIELD = "FIELD CHANGED TYPE FIELD";
	private final String CATEGORY_FIELD_CHANGED_TYPE_FIELD_DEPRECIATED = "FIELD CHANGED TYPE FIELD DEPRECIATED";
	
	private final String CATEGORY_FIELD_LOST_VISIBILITY = "FIELD LOST VISIBILITY";
	private final String CATEGORY_FIELD_LOST_VISIBILITY_DEPRECIATED = "FIELD LOST VISIBILITY DEPRECIATED";
	private final String CATEGORY_FIELD_GAIN_VISIBILITY = "FIELD GAIN VISIBILITY";
	
	private final String CATEGORY_FIELD_REMOVED_FIELD = "FIELD REMOVED";
	private final String CATEGORY_FIELD_REMOVED_FIELD_DEPRECIATED = "FIELD REMOVED DEPRECIATED";
	
	private final String CATEGORY_FIELD_ADD = "FIELD ADDED"; //non-breaking change
	
	private final String CATEGORY_FIELD_DEPRECIATED = "FIELD DEPRECIATED";
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	
	public Result calculateDiff(final APIVersion version1, final APIVersion version2){
		
		//Lista breacking changes.
		this.findDefaultValueFields(version1, version2);
		this.findChangedTypeFields(version1, version2);
		this.findRemovedFields(version1, version2);
		this.findChangedVisibilityFields(version1, version2);
		
		//Conta non-breaking changes.
		this.findAddedFields(version1, version2);
		this.findAddedDeprecatedFields(version1, version2);
		
		Result result = new Result();
		result.setListBreakingChange(this.listBreakingChange);
		return result;
	}

	private void findDefaultValueFields(APIVersion version1, APIVersion version2){
		
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
							List<VariableDeclarationFragment> variable1Fragments = fieldInVersion1.fragments();
							List<VariableDeclarationFragment> variable2Fragments = fieldInVersion2.fragments();
							Expression valueVersion1 = null;
							Expression valueVersion2 = null;
							
							for (VariableDeclarationFragment variable1DeclarationFragment : variable1Fragments) {
								valueVersion1 = variable1DeclarationFragment.getInitializer();
							}
							
							for (VariableDeclarationFragment variable2DeclarationFragment : variable2Fragments) {
								valueVersion2 = variable2DeclarationFragment.getInitializer();
							}
							
							if(valueVersion1 == null && valueVersion2 != null){
								try {
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FIELD_CHANGED_DEFAULT_VALUE));
								} catch (BindingException e) {
									continue;
								}
							}
							else if(valueVersion1 != null && valueVersion2 == null){
								try {
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FIELD_CHANGED_DEFAULT_VALUE));
								} catch (BindingException e) {
									continue;
								}
							}
							else if(valueVersion1 != null && valueVersion2 != null)
								if(!valueVersion1.toString().equals(valueVersion2.toString())) {
									try {
										this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FIELD_CHANGED_DEFAULT_VALUE));
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
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FIELD_CHANGED_TYPE_FIELD));
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

	private void findChangedVisibilityFields(APIVersion version1, APIVersion version2) {
		
		for (TypeDeclaration type : version1.getApiAcessibleTypes()) {
			if(version2.containsAccessibleType(type)){

				for (FieldDeclaration fieldInVersion1 : type.getFields()) {
					FieldDeclaration fieldInVersion2;
					try {
						fieldInVersion2 = version2.getVersionField(fieldInVersion1, type);
						if(fieldInVersion2 != null){
							if(UtilTools.isVisibilityPrivate(fieldInVersion1) && !UtilTools.isVisibilityPrivate(fieldInVersion2)){
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FIELD_GAIN_VISIBILITY));
							} else if(!UtilTools.isVisibilityPrivate(fieldInVersion1) && UtilTools.isVisibilityPrivate(fieldInVersion2)){
								if(this.isDeprecatedField(fieldInVersion1)){
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FIELD_LOST_VISIBILITY_DEPRECIATED));
								} else {
									try {
										this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FIELD_LOST_VISIBILITY));
									} catch (BindingException e) {
										continue;
									}
								}
							}
						}
					} catch (BindingException e) {
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
						FieldDeclaration fieldInVersion1 = version2.getVersionField(fieldVersion2, typeVersion2);
						//Se o field não estava depreciado na versão anterior ou não existia e foi criado depreciado.
						if(fieldInVersion1 == null || !this.isDeprecated(fieldInVersion1, version1.getVersionAccessibleType(typeVersion2))){
							this.listBreakingChange.add(new BreakingChange(typeVersion2.resolveBinding().getQualifiedName(),  UtilTools.getFieldName(fieldVersion2), this.CATEGORY_FIELD_DEPRECIATED, false));
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
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FIELD_ADD, false));
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
							this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(field), this.CATEGORY_FIELD_ADD, false));
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
								if(this.isDeprecatedField(fieldInVersion1)){
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), this.CATEGORY_FIELD_REMOVED_FIELD_DEPRECIATED, false));
								} else{
									try {
										this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), this.CATEGORY_FIELD_REMOVED_FIELD, true));
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
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), this.CATEGORY_FIELD_REMOVED_FIELD_DEPRECIATED, false));
							} 
							else{
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), this.CATEGORY_FIELD_REMOVED_FIELD, true));
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
	
}
