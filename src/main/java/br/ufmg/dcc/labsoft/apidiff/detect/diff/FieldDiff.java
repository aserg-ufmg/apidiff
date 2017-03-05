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
	
	private final String CATEGORY_CHANGED_DEFAULT_VALUE = "CHANGED DEFAULT VALUE";
	private final String CATEGORY_CHANGED_TYPE_FIELD = "CHANGED TYPE FIELD";
	private final String CATEGORY_LOST_VISIBILITY = "LOST VISIBILITY";
	private final String CATEGORY_REMOVED_FIELD = "REMOVED FIELD";
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	private int fieldBreakingChange;
	private int fieldNonBreakingChange;
	private int fieldAdd;
	private int fieldRemoval;
	private int fieldModif;
	private int fieldDeprecated;
	
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
		result.setBreakingChange(fieldBreakingChange);
		result.setNonBreakingChange(fieldNonBreakingChange);
		result.setElementDeprecated(this.fieldDeprecated);
		result.setElementModified(this.fieldModif);
		result.setElementRemoved(this.fieldRemoval);
		result.setElementAdd(this.fieldAdd);
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
								this.fieldBreakingChange++;
								this.fieldModif++;
								try {
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_CHANGED_DEFAULT_VALUE));
								} catch (BindingException e) {
									continue;
								}
							}
							else if(valueVersion1 != null && valueVersion2 == null){
								this.fieldModif++;
								this.fieldBreakingChange++;
								try {
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_CHANGED_DEFAULT_VALUE));
								} catch (BindingException e) {
									continue;
								}
							}
							else if(valueVersion1 != null && valueVersion2 != null)
								if(!valueVersion1.toString().equals(valueVersion2.toString())) {
									this.fieldModif++;
									this.fieldBreakingChange++;
									try {
										this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_CHANGED_DEFAULT_VALUE));
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
								this.fieldBreakingChange++;
								this.fieldModif++;
								try {
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_CHANGED_TYPE_FIELD));
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
					} catch (BindingException e) {
						continue;
					}
					if(fieldInVersion2 != null){
						if(UtilTools.isVisibilityPrivate(fieldInVersion1) && !UtilTools.isVisibilityPrivate(fieldInVersion2)){
							this.fieldNonBreakingChange++;
						} else if(!UtilTools.isVisibilityPrivate(fieldInVersion1) && UtilTools.isVisibilityPrivate(fieldInVersion2)){
							if(UtilTools.isDeprecatedField(fieldInVersion1)){
								this.fieldNonBreakingChange++;
								this.fieldDeprecated++;
							} else {
								this.fieldBreakingChange++;
								this.fieldModif++;
								try {
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion2), this.CATEGORY_LOST_VISIBILITY));
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

	private void findAddedDeprecatedFields(APIVersion version1, APIVersion version2) {
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
							//os dois tipos tem os mesmos fields

							if((!UtilTools.isDeprecatedField(fieldInVersion1)) && (UtilTools.isDeprecatedField(fieldInVersion2))){
								this.fieldNonBreakingChange++;
							}
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
					} catch (BindingException e) {
						continue;
					}
					if((fieldInVersion1 == null) && (UtilTools.isDeprecatedField(fieldInVersion2))){
						this.fieldNonBreakingChange++;
					}
				}
			}
		}
	}

	public int getFieldBreakingChange() {
		return fieldBreakingChange;
	}

	public int getFieldNonBreakingChange() {
		return fieldNonBreakingChange;
	}

	private void findAddedFields(APIVersion version1, APIVersion version2) {
		for (TypeDeclaration type : version2.getApiAcessibleTypes()) {
			if(version1.containsAccessibleType(type)){

				for (FieldDeclaration fieldInVersion2 : type.getFields()) {
					if(!UtilTools.isVisibilityPrivate(fieldInVersion2)){
						FieldDeclaration fieldInVersion1;
						try {
							fieldInVersion1 = version1.getVersionField(fieldInVersion2, type);
						} catch (BindingException e) {
							continue;
						}
						if(fieldInVersion1 == null){
							this.fieldNonBreakingChange++; //added field
							this.fieldAdd++;
						}
					}
				}
			} else{
				//tipo foi adicionado na versao 2, todos os fields foram adicionados
				for (FieldDeclaration field : type.getFields()) {
					if(!UtilTools.isVisibilityPrivate(field)){
						this.fieldNonBreakingChange++; //addded field
						this.fieldAdd++;
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
						} catch (BindingException e) {
							continue;
						}
						if(fieldInVersion2 == null){
							if(UtilTools.isDeprecatedField(fieldInVersion1)){
								this.fieldNonBreakingChange++; //removed deprecated field
								this.fieldDeprecated++;
							} else{
								this.fieldBreakingChange++; //removed field
								this.fieldRemoval++;
								try {
									this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), this.CATEGORY_REMOVED_FIELD));
								} catch (BindingException e) {
									continue;
								}
							}
						}
					}

				}
			} else{ 
				//tipo foi removido na versao 2, todos os fields foram removidos
				for (FieldDeclaration fieldInVersion1 : type.getFields()) {
					if(!UtilTools.isVisibilityPrivate(fieldInVersion1)){
						if(UtilTools.isDeprecatedField(fieldInVersion1)){
							this.fieldNonBreakingChange++; //removed deprecated field
							this.fieldDeprecated++;
						} else{
							this.fieldBreakingChange++; //removed field
							this.fieldRemoval++;
							try {
								this.listBreakingChange.add(new BreakingChange(type.resolveBinding().getQualifiedName(), UtilTools.getFieldName(fieldInVersion1), this.CATEGORY_REMOVED_FIELD));
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
