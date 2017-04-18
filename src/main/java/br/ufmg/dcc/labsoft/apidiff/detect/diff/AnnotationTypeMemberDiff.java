package br.ufmg.dcc.labsoft.apidiff.detect.diff;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.labsoft.apidiff.UtilTools;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class AnnotationTypeMemberDiff {
	
	private final String CATEGORY_ANNOTATION_TYPE_MEMBER_ADDED_WITH_DEFAULT = "ANNOTATION TYPE MEMBER ADDED WITH DEFAULT"; //non-breaking change
	private final String CATEGORY_ANNOTATION_TYPE_MEMBER_ADDED_WITHOUT_DEFAULT = "ANNOTATION TYPE MEMBER ADDED WHITHOUT DEFAULT"; //non-breaking change
	private final String CATEGORY_ANNOTATION_TYPE_MEMBER_REMOVED = "ANNOTATION TYPE MEMBER REMOVED"; //breaking change
	private final String CATEGORY_ANNOTATION_TYPE_MEMBER_REMOVED_DEPRECIATED = "ANNOTATION TYPE MEMBER REMOVED DEPRECIATED"; //non-breaking change
	private final String CATEGORY_ANNOTATION_TYPE_MEMBER_DEPRECIATED = "ANNOTATION TYPE MEMBER DEPRECIATED"; //non-breaking change
	private final String CATEGORY_ANNOTATION_TYPE_MEMBER_GAIN_DEFAULT_VALUE = "ANNOTATION TYPE MEMBER GAIN DEFAULT VALUE"; //non-breaking change
	private final String CATEGORY_ANNOTATION_TYPE_MEMBER_CHANGE_DEFAULT_VALUE = "ANNOTATION TYPE MEMBER CHANGE DEFAULT VALUE"; //breaking change
	private final String CATEGORY_ANNOTATION_TYPE_MEMBER_CHANGE_DEFAULT_VALUE_DEPRECIATED = "ANNOTATION TYPE MEMBER CHANGE DEFAULT VALUE DEPRECIATED"; //non-breaking change
	private final String CATEGORY_ANNOTATION_TYPE_MEMBER_CHANGE_RETURN = "ANNOTATION TYPE MEMBER CHANGE RETURN"; //breaking change
	private final String CATEGORY_ANNOTATION_TYPE_MEMBER_CHANGE_RETURN_DEPRECIATED = "ANNOTATION TYPE MEMBER CHANGE RETURN DEPRECIATED"; //non-breaking change
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	
	private Logger logger = LoggerFactory.getLogger(AnnotationTypeMemberDiff.class);

	public Result calculateDiff(final APIVersion version1, final APIVersion version2) {
		
		this.logger.info("Processing Methods...");
		
		this.findAddedAndDepreciatedTypeMember(version1, version2);
		this.findChangedDefaultValue(version1, version2);
		this.findRemovedAnnotationTypeMember(version1, version2);
		this.findChangedReturnAnnotationTypeMember(version1, version2);
		
		Result result = new Result();
		result.setListBreakingChange(this.listBreakingChange);
		return result;
	}
	


	private void findAddedAndDepreciatedTypeMember(APIVersion version1, APIVersion version2) {
		for (AnnotationTypeDeclaration annotationTypeDeclaration2 : version2.getApiAccessibleAnnotation()) {
			List<AnnotationTypeMemberDeclaration> membersVersion2 =  annotationTypeDeclaration2.bodyDeclarations();
			this.findAddedAnnotationTypeMember(version1, annotationTypeDeclaration2, membersVersion2);
			this.findAddedDeprecatedAnnotationTypeMember(version1, annotationTypeDeclaration2, membersVersion2);
		}
	}
	
	/**
	 * Busca métodos/membros da anotação que foram adicionados.
	 * @param version1
	 * @param version2
	 */
	private void findAddedAnnotationTypeMember(APIVersion version1, AnnotationTypeDeclaration annotationTypeDeclaration2, List<AnnotationTypeMemberDeclaration> membersVersion2){
		if(version1.containsAnnotationType(annotationTypeDeclaration2)){//Se annotation já existia, verifica quais são os novos métodos.
			 for(int i=0; i< membersVersion2.size(); i++){
				 AnnotationTypeMemberDeclaration memberVersion2 = membersVersion2.get(i);
				 AnnotationTypeMemberDeclaration memberVersion1 = version1.getEqualVersionAnnotationTypeMember(memberVersion2, annotationTypeDeclaration2);
				 if(memberVersion1 == null){
					 String category = "".equals(this.getDefaultValue(memberVersion2))?this.CATEGORY_ANNOTATION_TYPE_MEMBER_ADDED_WITHOUT_DEFAULT:this.CATEGORY_ANNOTATION_TYPE_MEMBER_ADDED_WITH_DEFAULT;
					 Boolean isBreakingChange = "".equals(this.getDefaultValue(memberVersion2))?true:false;
					 category += UtilTools.getSufixJavadoc(memberVersion2);
					 this.listBreakingChange.add(new BreakingChange(annotationTypeDeclaration2.resolveBinding().getQualifiedName(), memberVersion2.getName().toString(), category, isBreakingChange));
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
	private void findAddedDeprecatedAnnotationTypeMember(APIVersion version1, AnnotationTypeDeclaration annotationTypeDeclaration2, List<AnnotationTypeMemberDeclaration> membersVersion2){
		
		
		
		for(int i=0; i< membersVersion2.size(); i++){
			 AnnotationTypeMemberDeclaration memberVersion2 = membersVersion2.get(i);
			 AnnotationTypeMemberDeclaration memberVersion1 = version1.getEqualVersionAnnotationTypeMember(memberVersion2, annotationTypeDeclaration2);
			if((memberVersion1 == null || !this.isDeprecated(memberVersion1, version1.getVersionAccessibleAnnotationType(annotationTypeDeclaration2)))
					&& (this.isDeprecated(memberVersion2, annotationTypeDeclaration2))){
				String category = this.CATEGORY_ANNOTATION_TYPE_MEMBER_DEPRECIATED + UtilTools.getSufixJavadoc(memberVersion2);
				this.listBreakingChange.add(new BreakingChange(annotationTypeDeclaration2.resolveBinding().getQualifiedName(), memberVersion2.getName().toString(), category,false));
			}
		}	 
	}
	
	/**
	 * Busca métodos/membros da anotação que foram removidos.
	 * Se a @interface existe ainda, verifica se algum método foi removido.
	 * Se a @interface foi removida, os métodos dela não são contabilizados. A remoção do @interface é a breaking change.
	 * @param version1
	 * @param version2
	 */
	private void findRemovedAnnotationTypeMember(APIVersion version1, APIVersion version2) {
		for (AnnotationTypeDeclaration typeInVersion1 : version1.getApiAccessibleAnnotation()) {
			List<AnnotationTypeMemberDeclaration> membersVersion1 =  typeInVersion1.bodyDeclarations();
			for(int i=0; i< membersVersion1.size(); i++){
				AnnotationTypeMemberDeclaration memberVersion1 = membersVersion1.get(i);
				AnnotationTypeMemberDeclaration memberVersion2 = version2.getEqualVersionAnnotationTypeMember(memberVersion1, typeInVersion1);
				if(memberVersion2 == null){ //Se método foi removido na última versão.
					String category = this.isDeprecated(memberVersion1, typeInVersion1)? this.CATEGORY_ANNOTATION_TYPE_MEMBER_REMOVED_DEPRECIATED: this.CATEGORY_ANNOTATION_TYPE_MEMBER_REMOVED;
					category += UtilTools.getSufixJavadoc(memberVersion1);
					Boolean isBreakingChange = this.isDeprecated(memberVersion1, typeInVersion1)? false: true;
					this.listBreakingChange.add(new BreakingChange(typeInVersion1.resolveBinding().getQualifiedName(), memberVersion1.getName().toString(), category, isBreakingChange));
				}
			}
		}
	}
	
	/**
	 * Retorna verdadeiro se o membro da anotação está depreciado ou a anotação do método está depreciada.
	 * @param annotationType
	 * @return
	 */
	private Boolean isDeprecated(AnnotationTypeMemberDeclaration member, AnnotationTypeDeclaration annotationType){
		Boolean isMembeerDeprecated =  (member != null && member.resolveBinding() != null && member.resolveBinding().isDeprecated()) ? true: false;
		Boolean isAnnotationTypeDeprecated = (annotationType != null && annotationType.resolveBinding() != null && annotationType.resolveBinding().isDeprecated()) ? true: false;
		
		return isMembeerDeprecated || isAnnotationTypeDeprecated;
	}
	
	private String getDefaultValue(AnnotationTypeMemberDeclaration member){
		return (member == null || member.getDefault() == null) ? "":member.getDefault().toString();
	}
	
	private void diffDefaultValue(AnnotationTypeDeclaration typeAnnotation1, AnnotationTypeMemberDeclaration member1, AnnotationTypeMemberDeclaration member2){
		String defaultValue1 = this.getDefaultValue(member1);
		String defaultValue2 = this.getDefaultValue(member2);
		
		//Se não houve mudança.
		if(defaultValue1.equals(defaultValue2)){
			return;
		}
		String category = "";
		Boolean isBreakingChange = false;
		//Se ganhou valor default.
		if(("".equals(defaultValue1) && !"".equals(defaultValue2))){
			category = this.CATEGORY_ANNOTATION_TYPE_MEMBER_GAIN_DEFAULT_VALUE;
			isBreakingChange = false;
		}
		else{
			//Se perdeu ou modificou modificador "final"
			category = this.isDeprecated(member1, typeAnnotation1)?this.CATEGORY_ANNOTATION_TYPE_MEMBER_CHANGE_DEFAULT_VALUE_DEPRECIATED:CATEGORY_ANNOTATION_TYPE_MEMBER_CHANGE_DEFAULT_VALUE;
			isBreakingChange = this.isDeprecated(member1, typeAnnotation1)?false:true;
		}
		category += UtilTools.getSufixJavadoc(member1);
		this.listBreakingChange.add(new BreakingChange(typeAnnotation1.resolveBinding().getQualifiedName(), member2.getName().toString(), category, isBreakingChange));
	}

	private void findChangedDefaultValue(APIVersion version1, APIVersion version2) {
		//Percorre todos os types da versão anterior.
		List<AnnotationTypeDeclaration> annotations =  version1.getApiAccessibleAnnotation();
		for (AnnotationTypeDeclaration annotationVersion1 : annotations) {
			if(version2.containsAnnotationType(annotationVersion1)){//Se type ainda existe.
				List<AnnotationTypeMemberDeclaration> membersVersion1 =  annotationVersion1.bodyDeclarations();
				for(int i=0; i< membersVersion1.size(); i++){
					AnnotationTypeMemberDeclaration memberVersion1 = membersVersion1.get(i);
					AnnotationTypeMemberDeclaration memberVersion2 = version2.getEqualVersionAnnotationTypeMember(memberVersion1, annotationVersion1);
					this.diffDefaultValue(annotationVersion1, memberVersion1, memberVersion2);
				}
			}
		}
	}
	
	private void findChangedReturnAnnotationTypeMember (APIVersion version1, APIVersion version2) {
		
		for(AnnotationTypeDeclaration typeVersion1 : version1.getApiAccessibleAnnotation()){
			if(version2.containsAccessibleAnnotationType(typeVersion1)){
				List<AnnotationTypeMemberDeclaration> membersVersion1 =  typeVersion1.bodyDeclarations();
				for(int i=0; i< membersVersion1.size(); i++){
					AnnotationTypeMemberDeclaration methodVersion1 = membersVersion1.get(i);
					AnnotationTypeMemberDeclaration methodVersion2 = version2.getEqualVersionAnnotationTypeMember(methodVersion1, typeVersion1);
						if(this.diffReturnType(methodVersion1, methodVersion2)){
							String category = this.isDeprecated(methodVersion1, typeVersion1)? this.CATEGORY_ANNOTATION_TYPE_MEMBER_CHANGE_RETURN_DEPRECIATED: this.CATEGORY_ANNOTATION_TYPE_MEMBER_CHANGE_RETURN;
							category += UtilTools.getSufixJavadoc(methodVersion1);
							Boolean isBreakingChange = this.isDeprecated(methodVersion1, typeVersion1)? false: true;
							this.listBreakingChange.add(new BreakingChange(typeVersion1.resolveBinding().getQualifiedName(), methodVersion1.getName().toString(), category, isBreakingChange));
						}
				}
			}
		}
	}
	
	private Boolean diffReturnType(AnnotationTypeMemberDeclaration methodVersion1, AnnotationTypeMemberDeclaration methodVersion2){
		if(methodVersion1!= null && methodVersion2!=null){
			Type returnType1 = methodVersion1.getType();
			Type returnType2 = methodVersion2.getType();
			
			if(returnType1 != null && returnType2 != null &&  !returnType1.toString().equals(returnType2.toString())){
				return true;
			}
		}
		return false;
	}
	
}
