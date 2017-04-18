package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.labsoft.apidiff.UtilTools;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

/**
 * Classe para procurar breaking changes e non-breaking changes em anotações (@interface).
 * @author aline
 */
public class AnnotationTypeDiff {
	
	private final String CATEGORY_ANNOTATION_TYPE_REMOVED_DEPRECIATED = "ANNNOTATION TYPE REMOVED DEPRECIATED"; //non-breaking change
	private final String CATEGORY_ANNOTATION_TYPE_REMOVED = "ANNNOTATION TYPE REMOVED"; //breaking change
	private final String CATEGORY_ANNOTATION_TYPE_LOST_VISIBILIT_DEPRECIATED = "ANNNOTATION TYPE LOST VISIBILIT DEPRECIATED"; //non-breaking change
	private final String CATEGORY_ANNOTATION_TYPE_LOST_VISIBILITY = "ANNNOTATION TYPE LOST VISIBILIT"; //breaking change
	private final String CATEGORY_ANNOTATION_TYPE_GAIN_VISIBILITY = "ANNNOTATION TYPE GAIN VISIBILITY"; //non-breaking change
	private final String CATEGORY_ANNOTATION_TYPE_ADD = "ANNOTATION TYPE ADDED"; //non-breaking change
	private final String CATEGORY_ANNOTATION_TYPE_DEPRECIATED = "ANNOTATION TYPE DEPRECIATED"; //non-breaking change
	
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	private Logger logger = LoggerFactory.getLogger(AnnotationTypeDiff.class);

	/**
	 * Calculates the diff for classes
	 * @param version1 older version of an API
	 * @param version2 newer version of an API
	 */
	public Result calculateDiff(final APIVersion version1, final APIVersion version2) {
		
		this.logger.info("Processing Types ...");
		
		this.findRemovedAnnotationTypes(version1, version2);
		this.findAddedAnnotationTypes(version1, version2);
		this.findChangedAnnotationVisibilityTypes(version1, version2);
		this.findAddedAnnotationDeprecated(version1, version2);
		
		Result result = new Result();
		result.setListBreakingChange(this.listBreakingChange);
		return result;
	}

	/**
	 * Busca anotações que foram depreciadas.
	 * @param version1
	 * @param version2
	 */
	private void findAddedAnnotationDeprecated(APIVersion version1, APIVersion version2) {
		
		String category = this.CATEGORY_ANNOTATION_TYPE_DEPRECIATED;
		
		//Se anotação não era depreciada na versão 1 e foi depreciada na versão 2.
		for(AnnotationTypeDeclaration accessibleAnnotationTypeVersion1 : version1.getApiAccessibleAnnotation()){
			AnnotationTypeDeclaration accessibleAnnotationTypeVersion2 = version2.getVersionAccessibleAnnotationType(accessibleAnnotationTypeVersion1);
			if(accessibleAnnotationTypeVersion2 != null){
				if(!this.isDeprecated(accessibleAnnotationTypeVersion1) && this.isDeprecated(accessibleAnnotationTypeVersion2)){
					category += UtilTools.getSufixJavadoc(accessibleAnnotationTypeVersion2);
					this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(accessibleAnnotationTypeVersion1), accessibleAnnotationTypeVersion1.getName().toString(), category, false));
				}
			}
		}

		//Se anotação não existia na versão 1, e foi adicionado na versão 2 já depreciada.
		for(AnnotationTypeDeclaration accessibleTypeVersion2 : version2.getApiAccessibleAnnotation()){
			if(!version1.containsAccessibleAnnotationType(accessibleTypeVersion2) && this.isDeprecated(accessibleTypeVersion2)){
				category += UtilTools.getSufixJavadoc(accessibleTypeVersion2);
				this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(accessibleTypeVersion2), accessibleTypeVersion2.getName().toString(), category, false));
			}
		}
	}

	/**
	 * Retorna a anotação na lista com o mesmo nome da anotação recebida.
	 * Retorna nulo se não for encontrado.
	 * @param list
	 * @param type
	 * @return
	 */
	private AnnotationTypeDeclaration findAnnotationTypeDeclarationInList(List<AnnotationTypeDeclaration> list, AnnotationTypeDeclaration type){
		for(int i=0; i< list.size(); i++){
			AnnotationTypeDeclaration annotationTypeDeclaration = list.get(i);
			if(UtilTools.getNameNode(type).equals(UtilTools.getNameNode(annotationTypeDeclaration))){
				return annotationTypeDeclaration;
			}
		}
		return null;
	}
	

	
	/**
	 * Busca anotações que tiveram perda ou ganho de visibilidade.
	 * Anotações que perderam visibilidade são breaking changes.
	 * Anotações que perderam visibilidade mas estavam depreciadas na versão anterior são non-breaking changes.
	 * Anotações que ganharam visibilidade são non-breaking changes.
	 * @param version1
	 * @param version2
	 */
	private void findChangedAnnotationVisibilityTypes(APIVersion version1, APIVersion version2) {
		
		List<AnnotationTypeDeclaration> listTypesVersion1 = version1.getAllAnnotationTypes();
		List<AnnotationTypeDeclaration> listTypesVersion2 = version2.getAllAnnotationTypes();
		
		//Percorre types da versão anterior.
		for(AnnotationTypeDeclaration annotation1: listTypesVersion1){
			AnnotationTypeDeclaration annotation2 = this.findAnnotationTypeDeclarationInList(listTypesVersion2, annotation1);
			if(annotation2 != null){
				
				String visibilityAnnotation1 = UtilTools.getVisibility(annotation1);
				String visibilityAnnotation2 = UtilTools.getVisibility(annotation2);
				
				if(!visibilityAnnotation1.equals(visibilityAnnotation2)){ //Se visibilidade mudou, verifica se houve perda ou ganho.
					
					String category = "";
					Boolean isBreakingChange = false;
					
					//Breaking change: public --> qualquer modificador de acesso, protected --> qualquer modificador de acesso, exceto public.
					if(UtilTools.isVisibilityPublic(annotation1) || (UtilTools.isVisibilityProtected(annotation1) && !UtilTools.isVisibilityPublic(annotation2))){
						category = this.isDeprecated(annotation1)? this.CATEGORY_ANNOTATION_TYPE_LOST_VISIBILIT_DEPRECIATED: this.CATEGORY_ANNOTATION_TYPE_LOST_VISIBILITY;
						isBreakingChange = this.isDeprecated(annotation1)? false: true;
					}
					else{
						//non-breaking change: private ou default --> qualquer modificador de acesso, demais casos.
						category = UtilTools.isVisibilityDefault(annotation1) && UtilTools.isVisibilityPrivate(annotation2)? this.CATEGORY_ANNOTATION_TYPE_LOST_VISIBILITY: this.CATEGORY_ANNOTATION_TYPE_GAIN_VISIBILITY;
						isBreakingChange = false;
					}
					category += UtilTools.getSufixJavadoc(annotation2);
					this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(annotation2), annotation2.getName().toString(), category, isBreakingChange));
				}
			}
		}
	}

	/**
	 * Busca anotações que foram adicionadas
	 * @param version1
	 * @param version2
	 */
	private void findAddedAnnotationTypes(APIVersion version1, APIVersion version2) {
		//Busca types na segunda versão que não estão na primeira.
		List<AnnotationTypeDeclaration> listAnnotationTypesVersion2 = version2.getApiAccessibleAnnotation();
		for (AnnotationTypeDeclaration annotationVersion2 : listAnnotationTypesVersion2) {
			//Busca entre as anotações acessíveis e não acessíveis porque pode ser um type que já existia e ganhou visibilidade.
			if(!version1.containsAccessibleAnnotationType(annotationVersion2) && !version1.containsNonAccessibleAnnotationType(annotationVersion2)){
				this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(annotationVersion2), annotationVersion2.getName().toString(), this.CATEGORY_ANNOTATION_TYPE_ADD + UtilTools.getSufixJavadoc(annotationVersion2), false));
			}
		}
	}

	/**
	 * Busca anotações que foram removidas. 
	 * Se a anotação removida estava depreciada na versão anterior é uma non-breaking change.
	 * Caso contrário é uma breaking change. 
	 * @param version1
	 * @param version2
	 */
	private void findRemovedAnnotationTypes(APIVersion version1, APIVersion version2) {
		for (AnnotationTypeDeclaration annotation : version1.getApiAccessibleAnnotation()) {
			if(!version2.containsAccessibleAnnotationType(annotation) && !version2.containsNonAccessibleAnnotationType(annotation)){
				String category = this.isDeprecated(annotation)? this.CATEGORY_ANNOTATION_TYPE_REMOVED_DEPRECIATED: this.CATEGORY_ANNOTATION_TYPE_REMOVED;
				category += UtilTools.getSufixJavadoc(annotation);
				Boolean isBreakingChange = this.isDeprecated(annotation)? false: true;
				this.listBreakingChange.add(new BreakingChange(UtilTools.getNameNode(annotation), annotation.getName().toString(), category, isBreakingChange));
			}
		}
	}
	
	/**
	 * Retorna verdadeiro se é uma anotação depreciada.
	 * @param annotation
	 * @return
	 */
	private Boolean isDeprecated(AnnotationTypeDeclaration type){
		return (type != null && type.resolveBinding() != null && type.resolveBinding().isDeprecated()) ? true: false;
	}
}
