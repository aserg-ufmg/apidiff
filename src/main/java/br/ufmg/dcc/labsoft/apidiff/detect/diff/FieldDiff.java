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
	
	private final String CATEGORY_FILD_CHANGED_DEFAULT_VALUE = "CHANGED DEFAULT VALUE";
	private final String CATEGORY_FILD_CHANGED_DEFAULT_VALUE_DEPRECIATED = "CHANGED DEFAULT VALUE DEPRECIATED";
	
	private final String CATEGORY_FILD_CHANGED_TYPE_FIELD = "CHANGED TYPE FIELD";
	private final String CATEGORY_FILD_CHANGED_TYPE_FIELD_DEPRECIATED = "CHANGED TYPE FIELD DEPRECIATED";
	
	private final String CATEGORY_FILD_LOST_VISIBILITY = "LOST VISIBILITY";
	private final String CATEGORY_FILD_LOST_VISIBILITY_DEPRECIATED = "LOST VISIBILITY DEPRECIATED";
	private final String CATEGORY_FILD_GAIN_VISIBILITY = "LOST VISIBILITY";
	
	private final String CATEGORY_FILD_REMOVED_FIELD = "REMOVED FIELD";
	private final String CATEGORY_FILD_REMOVED_FIELD_DEPRECIATED = "REMOVED FIELD DEPRECIATED";
	
	private final String CATEGORY_FILD_ADD = "FILD ADDED"; //non-breaking change
	
	private final String CATEGORY_FILD_DEPRECIATED = "FILD DEPRECIATED";
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	
	public Result calculateDiff(final APIVersion version1, final APIVersion version2){
		
		//Lista breacking Change.
		this.findDefaultValueFields(version1, version2);
		this.findChangedTypeFields(version1, version2);
		this.findRemovedFields(version1, version2);
		this.findChangedVisibilityFields(version1, version2);
		
		//Conta non-Breaking Change.
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
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FILD_CHANGED_DEFAULT_VALUE));
								} catch (BindingException e) {
									continue;
								}
							}
							else if(valueVersion1 != null && valueVersion2 == null){
								try {
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FILD_CHANGED_DEFAULT_VALUE));
								} catch (BindingException e) {
									continue;
								}
							}
							else if(valueVersion1 != null && valueVersion2 != null)
								if(!valueVersion1.toString().equals(valueVersion2.toString())) {
									try {
										this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FILD_CHANGED_DEFAULT_VALUE));
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
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FILD_CHANGED_TYPE_FIELD));
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
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FILD_GAIN_VISIBILITY));
							} else if(!UtilTools.isVisibilityPrivate(fieldInVersion1) && UtilTools.isVisibilityPrivate(fieldInVersion2)){
								if(UtilTools.isDeprecatedField(fieldInVersion1)){
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FILD_LOST_VISIBILITY_DEPRECIATED));
								} else {
									try {
										this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FILD_LOST_VISIBILITY));
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

	private void findAddedDeprecatedFields(APIVersion version1, APIVersion version2) {
		for (TypeDeclaration type : version1.getApiAcessibleTypes()) {
			if(version2.containsAccessibleType(type)){

				for (FieldDeclaration fieldInVersion1 : type.getFields()) {
					if(!UtilTools.isVisibilityPrivate(fieldInVersion1)){
						FieldDeclaration fieldInVersion2;
						try {
							fieldInVersion2 = version2.getVersionField(fieldInVersion1, type);
							if(fieldInVersion2 != null && !UtilTools.isVisibilityPrivate(fieldInVersion2)){
								//os dois tipos tem os mesmos fields

								if((!UtilTools.isDeprecatedField(fieldInVersion1)) && (UtilTools.isDeprecatedField(fieldInVersion2))){
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FILD_DEPRECIATED, false));
								}
							}
						} catch (BindingException e) {
							continue;
						}
					}
				}
			}	
		}

		for (TypeDeclaration type : version2.getApiAcessibleTypes()) {
			for (FieldDeclaration fieldInVersion2 : type.getFields()) {
				if(!UtilTools.isVisibilityPrivate(fieldInVersion2)){
					FieldDeclaration fieldInVersion1;
					try {
						fieldInVersion1 = version1.getVersionField(fieldInVersion2, type);
						if((fieldInVersion1 == null) && (UtilTools.isDeprecatedField(fieldInVersion2))){
							this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FILD_DEPRECIATED, false));
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
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_FILD_ADD, false));
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
							this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(field), this.CATEGORY_FILD_ADD, false));
						} catch (BindingException e) {
							continue;
						}
					}
				}
			}
		}
	}

	private void findRemovedFields(APIVersion version1, APIVersion version2) {
		
		for (TypeDeclaration type : version1.getApiAcessibleTypes()) {
			if(version2.containsAccessibleType(type)){
				for (FieldDeclaration fieldInVersion1 : type.getFields()) {

					if(!UtilTools.isVisibilityPrivate(fieldInVersion1)){
						FieldDeclaration fieldInVersion2;
						try {
							fieldInVersion2 = version2.getVersionField(fieldInVersion1, type);
							if(fieldInVersion2 == null){
								if(UtilTools.isDeprecatedField(fieldInVersion1)){
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), this.CATEGORY_FILD_REMOVED_FIELD_DEPRECIATED, false));
								} else{
									try {
										this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), this.CATEGORY_FILD_REMOVED_FIELD, true));
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
							if(UtilTools.isDeprecatedField(fieldInVersion1)){
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), this.CATEGORY_FILD_REMOVED_FIELD_DEPRECIATED, false));
							} 
							else{
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), this.CATEGORY_FILD_REMOVED_FIELD, true));
							} 
						}catch (BindingException e) {
								continue;
						}
					}
				
				}
			}
		}
	}
	
}
