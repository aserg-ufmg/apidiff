package br.ufmg.dcc.labsoft.apidiff.detect.diff;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import br.ufmg.dcc.labsoft.apidiff.UtilTools;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class MethodDiff {
	
	private final String CATEGORY_METHOD_CHANGED_EXCEPTION = "METHOD CHANGED EXCEPTION"; //breaking change
	private final String CATEGORY_METHOD_CHANGED_EXCEPTION_DEPRECIATED = "METHOD CHANGED EXCEPTION DEPRECIATED"; //breaking change
	
	private final String CATEGORY_METHOD_CHANGED_PARAMETERS = "METHOD CHANGED PARAMETERS"; //breaking change
	private final String CATEGORY_METHOD_CHANGED_PARAMETERS_DEPRECIATED = "METHOD CHANGED PARAMETERS DEPRECIATED"; //non-breaking change
	
	private final String CATEGORY_METHOD_CHANGED_RETURN_TYPE = "METHOD CHANGED RETURN TYPE"; //breaking change
	private final String CATEGORY_METHOD_CHANGED_RETURN_TYPE_DEPRECIATED = "METHOD CHANGED RETURN TYPE DEPRECIATED"; //non-breaking change
	
	private final String CATEGORY_METHOD_REMOVED = "METHOD REMOVED"; //breaking change
	private final String CATEGORY_METHOD_REMOVED_DEPRECIATED = "METHOD REMOVED DEPRECIATED"; //non-breaking change
	
	private final String CATEGORY_METHOD_ADDED = "METHOD ADDED"; //non-breaking change
	
	private final String CATEGORY_METHOD_LOST_VISIBILITY = "METHOD LOST VISIBILITY"; //breaking change
	private final String CATEGORY_METHOD_LOST_VISIBILITY_DEPRECIATED = "METHOD LOST VISIBILITY DEPRECIATED"; //non-breaking change
	private final String CATEGORY_METHOD_GAIN_VISIBILITY = "METHOD GAIN VISIBILITY"; //non-breaking change
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	private int methodBreakingChange;
	private int methodNonBreakingChange;
	private int methodAdd;
	private int methodRemoval;
	private int methodModif;
	private int methodDeprecatedOp;

	public Result calculateDiff(final APIVersion version1, final APIVersion version2) {
		
		//Lista breaking Change.
		this.findRemovedMethods(version1, version2);
		this.findChangedVisibilityMethods(version1, version2);
		this.findChangedReturnTypeMethods(version1, version2);
		this.findChangedParametersMethods(version1, version2);
		this.findChangedExceptionTypeMethods(version1, version2);
		
		//Lista breaking Change.
		this.findAddedMethods(version1, version2);
		this.findAddedDeprecatedMethods(version1, version2);
		
		Result result = new Result();
		result.setBreakingChange(this.methodBreakingChange);
		result.setNonBreakingChange(this.methodNonBreakingChange);
		result.setElementAdd(this.methodAdd);
		result.setElementDeprecated(this.methodDeprecatedOp);
		result.setElementModified(this.methodModif);
		result.setElementRemoved(this.methodRemoval);
		result.setListBreakingChange(this.listBreakingChange);
		return result;
	}

	private void findChangedExceptionTypeMethods(APIVersion version1, APIVersion version2) {
		
		
		for(TypeDeclaration typeVersion1 : version1.getApiAcessibleTypes()){
			if(version2.contaisAccessibleType(typeVersion1)){
				for(MethodDeclaration methodVersion1 : typeVersion1.getMethods()){
					if(!UtilTools.isVisibilityPrivate(methodVersion1)){
						MethodDeclaration methodVersion2 = version2.getEqualVersionMethod(methodVersion1, typeVersion1);
						if(methodVersion2 != null && !UtilTools.isVisibilityPrivate(methodVersion2)){
							List<SimpleType> exceptionsVersion1 = methodVersion1.thrownExceptionTypes();
							List<SimpleType> exceptionsVersion2 = methodVersion2.thrownExceptionTypes();

							if(exceptionsVersion1.size() < exceptionsVersion2.size()) {
								this.methodBreakingChange++; //added exception type
								this.methodModif++;
								
								this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion1.getName().toString(), this.CATEGORY_METHOD_CHANGED_EXCEPTION));
								
							} else if(exceptionsVersion1.size() > exceptionsVersion2.size()){
								this.methodBreakingChange++; //removed exception type
								this.methodModif++;
								
								this.listBreakingChange.add(new BreakingChange( typeVersion1.resolveBinding().getQualifiedName(), methodVersion1.getName().toString(), this.CATEGORY_METHOD_CHANGED_EXCEPTION));
								
							} 
							for(SimpleType exceptionVersion1 : exceptionsVersion1){
								Boolean found = false;
								for(SimpleType exceptionVersion2 : exceptionsVersion2){
									if(exceptionVersion1.getName().toString().equals(exceptionVersion2.getName().toString())){
										found = true;
										break;
									}
								}

								if (!found){
									this.methodBreakingChange++; //changed exception type;
									this.methodModif++;
									
									this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion1.getName().toString(), this.CATEGORY_METHOD_CHANGED_EXCEPTION));
									
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	private void findChangedParametersMethods(APIVersion version1, APIVersion version2) {
		
		for(TypeDeclaration typeVersion1 : version1.getApiAcessibleTypes()){
			if(version2.contaisAccessibleType(typeVersion1)){

				for(MethodDeclaration methodVersion1 : typeVersion1.getMethods()){
					if(!UtilTools.isVisibilityPrivate(methodVersion1)){
						if(version2.getEqualVersionMethod(methodVersion1, typeVersion1) == null){
							ArrayList<MethodDeclaration> methodsVersion2 = version2.
									getAllEqualMethodsByName(methodVersion1, typeVersion1);
							for(MethodDeclaration methodVersion2: methodsVersion2){
								if(!UtilTools.isVisibilityPrivate(methodVersion2) && version1.getEqualVersionMethod(methodVersion2, typeVersion1) == null){
									int smallerSize = methodVersion1.parameters().size();
									if(methodVersion1.parameters().size() > methodVersion2.parameters().size()){
										smallerSize = methodVersion2.parameters().size();
										this.methodBreakingChange++; //removed parameter
										this.methodModif++;
										this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion1.getName().toString(), this.CATEGORY_METHOD_CHANGED_PARAMETERS));
										
									} else if (methodVersion1.parameters().size() < methodVersion2.parameters().size()){
										this.methodBreakingChange++; //added parameter
										this.methodModif++;
										this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion1.getName().toString(), this.CATEGORY_METHOD_CHANGED_PARAMETERS));
									} 
									
									for(int i = 0; i < smallerSize; i++){
										String parameterVersion1 = methodVersion1.parameters().get(i).toString();
										String parameterVersion2 = methodVersion2.parameters().get(i).toString();
										
										if(!parameterVersion1.equals(parameterVersion2)){
											this.methodBreakingChange++; //changed parameter
											this.methodModif++;
											
											this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion1.getName().toString(), this.CATEGORY_METHOD_CHANGED_PARAMETERS));
											
											break;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void findChangedReturnTypeMethods(APIVersion version1, APIVersion version2) {
		
		for(TypeDeclaration typeVersion1 : version1.getApiAcessibleTypes()){
			if(version2.contaisAccessibleType(typeVersion1)){
				for(MethodDeclaration methodVersion1 : typeVersion1.getMethods()){
					if(!UtilTools.isVisibilityPrivate(methodVersion1)){
						MethodDeclaration methodVersion2 = version2.getEqualVersionMethod(methodVersion1, typeVersion1);
						if(methodVersion2 != null && !UtilTools.isVisibilityPrivate(methodVersion2)){
							if(methodVersion1.getReturnType2() != null && methodVersion2.getReturnType2() != null && 
									!methodVersion1.getReturnType2().toString().equals(methodVersion2.getReturnType2().toString())){
								this.methodBreakingChange++;
								this.methodModif++;
								this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion1.getName().toString(), this.CATEGORY_METHOD_CHANGED_RETURN_TYPE));
							}
						}
					}
				}
			}
		}
	}

	private void findChangedVisibilityMethods(APIVersion version1, APIVersion version2) {
		
		for(TypeDeclaration typeVersion1 : version1.getApiAcessibleTypes()){
			if(version2.contaisAccessibleType(typeVersion1)){
				for(MethodDeclaration methodVersion1 : typeVersion1.getMethods()){
					MethodDeclaration methodVersion2 = version2.getEqualVersionMethod(methodVersion1, typeVersion1);
					if(methodVersion2 != null){
						if(UtilTools.isVisibilityPrivate(methodVersion1) && !UtilTools.isVisibilityPrivate(methodVersion2)){
							this.methodNonBreakingChange++; //gained visibility
						}
						else if(!UtilTools.isVisibilityPrivate(methodVersion1) && UtilTools.isVisibilityPrivate(methodVersion2)){
							if(methodVersion1.resolveBinding() != null && 
									methodVersion1.resolveBinding().isDeprecated()){
								this.methodNonBreakingChange++; //lost visibility deprecated
								this.methodDeprecatedOp++;
							} else {
								this.methodBreakingChange++; //lost visibility
								this.methodModif++;
								this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion1.getName().toString(), this.CATEGORY_METHOD_LOST_VISIBILITY));
							}
						}
					}
				}
			}
		}
	}

	private void findAddedDeprecatedMethods(APIVersion version1, APIVersion version2) {
		for (TypeDeclaration typeInVersion1 : version1.getApiAcessibleTypes()) {
			if(version2.contaisAccessibleType(typeInVersion1)){
				for(MethodDeclaration methodInVersion1 : typeInVersion1.getMethods()){
					if(!UtilTools.isVisibilityPrivate(methodInVersion1)){
						MethodDeclaration methodInVersion2 = version2.getEqualVersionMethod(methodInVersion1, typeInVersion1);
						if(methodInVersion2 != null && !UtilTools.isVisibilityPrivate(methodInVersion2)){
							if(methodInVersion1.resolveBinding() != null && methodInVersion2.resolveBinding() != null){
								if(!methodInVersion1.resolveBinding().isDeprecated() && methodInVersion2.resolveBinding().isDeprecated()){
									this.methodNonBreakingChange++;
								}
							}
						}
					}
				}
			}
		}

		for(TypeDeclaration typeInVersion2 : version2.getApiAcessibleTypes()){
			for(MethodDeclaration methodInVersion2 : typeInVersion2.getMethods()){
				if(!UtilTools.isVisibilityPrivate(methodInVersion2)){
					MethodDeclaration methodInVersion1 = version1.getEqualVersionMethod(methodInVersion2, typeInVersion2);
					if(methodInVersion1 == null && methodInVersion2.resolveBinding() != null && 
							methodInVersion2.resolveBinding().isDeprecated()){
						this.methodNonBreakingChange++;
					}
				}
			}
		}

	}

	private void findRemovedMethods(APIVersion version1, APIVersion version2) {
		
		for (TypeDeclaration typeInVersion1 : version1.getApiAcessibleTypes()) {
			if(version2.contaisAccessibleType(typeInVersion1)){
				for (MethodDeclaration methodInVersion1 : typeInVersion1.getMethods()) {
					if(!UtilTools.isVisibilityPrivate(methodInVersion1)){
						MethodDeclaration methodInVersion2 = version2.getEqualVersionMethod(methodInVersion1, typeInVersion1);
						if(methodInVersion2 == null){
							if(methodInVersion1.resolveBinding() != null && 
									methodInVersion1.resolveBinding().isDeprecated()){
								this.methodNonBreakingChange++; //removed deprecated
								this.methodDeprecatedOp++;
							} else {
								this.methodBreakingChange++; // removed
								this.methodRemoval++;
								
								this.listBreakingChange.add(new BreakingChange(typeInVersion1.resolveBinding().getQualifiedName(), methodInVersion1.getName().toString(), this.CATEGORY_METHOD_REMOVED));
								
							}
						}
					}
				}
			} else{
				for (MethodDeclaration methodInVersion1 : typeInVersion1.getMethods()) {
					if(!UtilTools.isVisibilityPrivate(methodInVersion1)){
						if(methodInVersion1.resolveBinding() != null && 
								methodInVersion1.resolveBinding().isDeprecated()){
							this.methodNonBreakingChange++; //removed deprecated
							this.methodDeprecatedOp++;
						} else {
							this.methodBreakingChange++; //removed
							this.methodRemoval++;
							this.listBreakingChange.add(new BreakingChange(typeInVersion1.resolveBinding().getQualifiedName(), methodInVersion1.getName().toString(), this.CATEGORY_METHOD_REMOVED));
						}
					}
				}
			}
		}
	}

	private void findAddedMethods(APIVersion version1, APIVersion version2) {
		
		for (TypeDeclaration typeInVersion2 : version2.getApiAcessibleTypes()) {
			if(version1.contaisAccessibleType(typeInVersion2)){
				for(MethodDeclaration methodInVersion2: typeInVersion2.getMethods()){
					if(!UtilTools.isVisibilityPrivate(methodInVersion2)){
						MethodDeclaration methodInVersion1 = version1.getEqualVersionMethod(methodInVersion2, typeInVersion2);
						if(methodInVersion1 == null){
							this.methodNonBreakingChange++;
							this.methodAdd++;
						}
					}
				}
			} else {
				for(MethodDeclaration methodInVersion2: typeInVersion2.getMethods()){
					if(!UtilTools.isVisibilityPrivate(methodInVersion2)){
						this.methodNonBreakingChange++;
						this.methodAdd++;
					}
				}
			}
		}
	}

}
