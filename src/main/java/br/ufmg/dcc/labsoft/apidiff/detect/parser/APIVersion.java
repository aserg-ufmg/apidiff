package br.ufmg.dcc.labsoft.apidiff.detect.parser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.labsoft.apidiff.UtilTools;
import br.ufmg.dcc.labsoft.apidiff.detect.diff.comparator.ComparatorMethod;
import br.ufmg.dcc.labsoft.apidiff.detect.diff.service.git.GitFile;
import br.ufmg.dcc.labsoft.apidiff.detect.exception.BindingException;
import br.ufmg.dcc.labsoft.apidiff.enums.ClassifierAPI;

public class APIVersion {

	private ArrayList<TypeDeclaration> apiAccessibleTypes = new ArrayList<TypeDeclaration>();;
	private ArrayList<TypeDeclaration> apiNonAccessibleTypes = new ArrayList<TypeDeclaration>();;
	private ArrayList<EnumDeclaration> apiAccessibleEnums = new ArrayList<EnumDeclaration>();
	private ArrayList<EnumDeclaration> apiNonAccessibleEnums = new ArrayList<EnumDeclaration>();
	private ArrayList<AnnotationTypeDeclaration> apiAccessibleAnnotation = new ArrayList<AnnotationTypeDeclaration>();
	private ArrayList<AnnotationTypeDeclaration> apiNonAccessibleAnnotation = new ArrayList<AnnotationTypeDeclaration>();
	private Map<ChangeType, List<GitFile>> mapModifications = new HashMap<ChangeType, List<GitFile>>();
	private List<String> listFilesMofify = new ArrayList<String>();
	private ClassifierAPI classifierAPI;
	private String nameProject;
	
	private Logger logger = LoggerFactory.getLogger(APIVersion.class);

	public APIVersion(final File path, final Map<ChangeType, List<GitFile>> mapModifications, ClassifierAPI classifierAPI) {
		
		try {
			this.classifierAPI = classifierAPI;
			this.mapModifications = mapModifications;
			this.nameProject = path.getAbsolutePath().replaceAll(UtilTools.getPathProjects() + "/", "");
	    	String prefix = path.getAbsolutePath() + "/";
			for(ChangeType changeType : this.mapModifications.keySet()){
				for(GitFile gitFile: mapModifications.get(changeType)){
					if(gitFile.getPathOld()!= null){
						this.listFilesMofify.add(prefix + gitFile.getPathOld());
					}
					if(gitFile.getPathNew() != null && !gitFile.getPathNew().equals(gitFile.getPathOld())){
						this.listFilesMofify.add(prefix + gitFile.getPathNew());
					}
				}
			}
			this.parseFilesInDir(path, false);
		} catch (IOException e) {
			this.logger.error("Erro ao criar APIVersion", e);
		}
	}
	
	/**
	 * Retorna uma APIVersion com os arquivos do projeto.
	 * @param path - path da biblioteca analisada.
	 * @param classifierAPI - tipo da API analisada.
	 */
	public APIVersion(final String nameProject, ClassifierAPI classifierAPI) {
		try {
			this.nameProject = nameProject;
			this.classifierAPI = classifierAPI;
			File path = new File(UtilTools.getPathProjects() + "/" + this.nameProject);
			this.parseFilesInDir(path, true);
		} catch (IOException e) {
			this.logger.error("Erro ao criar APIVersion", e);
		}
		
	}

	/**
	 * @param Path - path da biblioteca analisada.
	 * @param ignoreTreeDiff - true para ignorar a árvore de modificações (comparação de projetos). False para considerar a árvore de modificações (análise em nível de commit).
	 * @throws IOException
	 */
	public void parseFilesInDir(File file, final Boolean ignoreTreeDiff) throws IOException {
		if (file.isFile()) {
			String simpleNameFile = file.getAbsolutePath().replaceAll(UtilTools.getPathProjects() + "/" + this.nameProject, "");
			if (UtilTools.isJavaFile(file.getName()) && this.isFileModification(file, ignoreTreeDiff) && UtilTools.isAPIByClassifier(simpleNameFile, this.classifierAPI)) {
				this.parse(UtilTools.readFileToString(file.getAbsolutePath()), file, ignoreTreeDiff);		
			}
		} else {
			if(file.listFiles() != null){
				for (File f : file.listFiles()) {
					this.parseFilesInDir(f, ignoreTreeDiff);
				}
			}
		}
	}

	public void parse(String str, File source, final Boolean ignoreTreeDiff) throws IOException {
		
		if(this.mapModifications.size() > 0 && !this.isFileModification(source,ignoreTreeDiff)){
			return;
		}
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		String[] classpath = java.lang.System.getProperty("java.class.path").split(";");
		String[] sources = { source.getParentFile().getAbsolutePath() };

		Hashtable<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		parser.setUnitName(source.getAbsolutePath());

		parser.setCompilerOptions(options);
		parser.setEnvironment(classpath, sources, new String[] { "UTF-8" },	true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);

		final CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		

		TypeDeclarationVisitor visitorType = new TypeDeclarationVisitor();
		EnumDeclarationVisitor visitorEnum = new EnumDeclarationVisitor();
		AnnotationTypeDeclarationVisitor visitorAnnotation = new AnnotationTypeDeclarationVisitor();
		
		compilationUnit.accept(visitorType);
		compilationUnit.accept(visitorEnum);
		compilationUnit.accept(visitorAnnotation);
		
		this.configureAcessiblesAndNonAccessibleTypes(visitorType);
		this.configureAcessiblesAndNonAccessibleEnums(visitorEnum);
		this.configureAcessiblesAndNonAccessibleAnnotations(visitorAnnotation);
	}
	
	/**
	 *  Salva os types acessíveis para o cliente externo são (public ou protected) e não acessíveis (default e private).
	 * @param visitorType
	 */
	private void configureAcessiblesAndNonAccessibleTypes(TypeDeclarationVisitor visitorType){
		this.apiNonAccessibleTypes.addAll(visitorType.getNonAcessibleTypes());
		this.apiAccessibleTypes.addAll(visitorType.getAcessibleTypes());
	}
	
	/**
	 *  Salva os enums acessíveis para o cliente externo são (public ou protected) e não acessíveis (default e private).
	 * @param visitorType
	 */
	private void configureAcessiblesAndNonAccessibleEnums(EnumDeclarationVisitor visitorType){
		this.apiNonAccessibleEnums.addAll(visitorType.getNonAcessibleEnums());
		this.apiAccessibleEnums.addAll(visitorType.getAcessibleEnums());
	}
	
	/**
	 *  Salva as anotações (@interface) acessíveis para o cliente externo são (public ou protected) e não acessíveis (default e private).
	 * @param visitorType
	 */
	private void configureAcessiblesAndNonAccessibleAnnotations(AnnotationTypeDeclarationVisitor visitorAnnotation){
		this.apiNonAccessibleAnnotation.addAll(visitorAnnotation.getNonAcessibleAnnotation());
		this.apiAccessibleAnnotation.addAll(visitorAnnotation.getAcessibleAnnotation());
	}
	
	/**
	 *  Retorna verdadeiro se o arquivo está na lista de arquivos modificados na última versão, ou se a lista de midificações não for considerada na análise.
	 *  Falso caso contrário.
	 * @param source - Arquivo analisado.
	 * @param ignoreTreeDiff - Se verdadeiro ignora a lista de modificações. Se falso, verifica se o arquivo pertence está contido na lista de arquivos modificados.
	 * @return - Verdadeiro se é um arquivo que deve ser incluído no diff, falso caso contrário.
	 */
	private Boolean isFileModification(final File source, final Boolean ignoreTreeDiff){
		return (ignoreTreeDiff || this.listFilesMofify.contains(source.getAbsolutePath()))? true: false;
	}

	public ArrayList<EnumDeclaration> getApiAccessibleEnums() {
		return apiAccessibleEnums;
	}

	public ArrayList<EnumDeclaration> getApiNonAccessibleEnums() {
		return apiNonAccessibleEnums;
	}

	public ArrayList<TypeDeclaration> getApiAcessibleTypes(){
		return this.apiAccessibleTypes;
	}
	
	
	/**
	 * Retorna a lista de types de foram removidos.
	 * @return
	 * TODO: Pendente
	 */
	public List<TypeDeclaration> getApiRemovedTypes(){
		List<TypeDeclaration> list = new ArrayList<>();
		return list;
	}
	
	/**
	 * Retorna a lista de types que foram renomeados.
	 * @return
	 * TODO: Pendente
	 */
	public List<TypeDeclaration> getApiRenamedTypes(){
		List<TypeDeclaration> list = new ArrayList<>();
		return list;
	}
	

	public ArrayList<TypeDeclaration> getApiNonAcessibleTypes(){
		return this.apiNonAccessibleTypes;
	}

	public EnumDeclaration getVersionNonAccessibleEnum(EnumDeclaration enumVersrionReference){
		for (EnumDeclaration enumDeclarion : this.apiNonAccessibleEnums) {
			if(enumDeclarion.resolveBinding() != null && enumVersrionReference.resolveBinding() != null){
				if(enumDeclarion.resolveBinding().getQualifiedName().equals(enumVersrionReference.resolveBinding().getQualifiedName())){
					return enumDeclarion;
				}
			}
		}

		return null;
	}

	public EnumDeclaration getVersionAccessibleEnum(EnumDeclaration enumVersrionReference){
		for (EnumDeclaration enumDeclarion : this.apiAccessibleEnums) {
			if(enumDeclarion.resolveBinding() != null && enumVersrionReference.resolveBinding() != null){
				if(enumDeclarion.resolveBinding().getQualifiedName().equals(enumVersrionReference.resolveBinding().getQualifiedName())){
					return enumDeclarion;
				}
			}
		}

		return null;
	}

	public TypeDeclaration getVersionNonAccessibleType(TypeDeclaration typeVersrionReference){
		for (TypeDeclaration typeDeclarion : this.apiNonAccessibleTypes) {
			if(typeDeclarion.resolveBinding() != null && typeVersrionReference.resolveBinding() != null){
				if(typeDeclarion.resolveBinding().getQualifiedName().equals(typeVersrionReference.resolveBinding().getQualifiedName())){
					return typeDeclarion;
				}
			}
		}

		return null;
	}
	
	public AnnotationTypeDeclaration getVersionNonAccessibleAnnotationType(AnnotationTypeDeclaration annotationVersionReference){
		for (AnnotationTypeDeclaration annotationDeclarion : this.apiNonAccessibleAnnotation) {
			if(annotationDeclarion.resolveBinding() != null && annotationVersionReference.resolveBinding() != null){
				if(annotationDeclarion.resolveBinding().getQualifiedName().equals(annotationVersionReference.resolveBinding().getQualifiedName())){
					return annotationDeclarion;
				}
			}
		}

		return null;
	}

	public TypeDeclaration getVersionAccessibleType(TypeDeclaration typeVersrionReference){
		for (TypeDeclaration typeDeclarion : this.apiAccessibleTypes) {
			if(typeDeclarion.resolveBinding() != null && typeVersrionReference.resolveBinding() != null){
				if(typeDeclarion.resolveBinding().getQualifiedName().equals(typeVersrionReference.resolveBinding().getQualifiedName())){
					return typeDeclarion;
				}
			}
		}
		return null;
	}
	
	public AnnotationTypeDeclaration getVersionAccessibleAnnotationType(AnnotationTypeDeclaration typeVersionReference){
		for (AnnotationTypeDeclaration annotationTypeDeclaration : this.apiAccessibleAnnotation) {
			if(annotationTypeDeclaration.resolveBinding() != null && typeVersionReference.resolveBinding() != null){
				if(annotationTypeDeclaration.resolveBinding().getQualifiedName().equals(typeVersionReference.resolveBinding().getQualifiedName())){
					return annotationTypeDeclaration;
				}
			}
		}
		return null;
	}
	
	public boolean containsAnnotationType(AnnotationTypeDeclaration annotationTypeDeclaration){
		return this.containsAccessibleAnnotationType(annotationTypeDeclaration) || this.containsNonAccessibleAnnotationType(annotationTypeDeclaration);
	}
	
	public boolean containsType(TypeDeclaration type){
		return this.containsAccessibleType(type) || this.containsNonAccessibleType(type);
	}
	
	public boolean containsAccessibleType(TypeDeclaration type){
		return this.getVersionAccessibleType(type) != null;
	}
	
	public boolean containsAccessibleAnnotationType(AnnotationTypeDeclaration annotationTypeDeclaration){
		return this.getVersionAccessibleAnnotationType(annotationTypeDeclaration) != null;
	}

	public boolean containsNonAccessibleType(TypeDeclaration type){
		return this.getVersionNonAccessibleType(type) != null;
	}
	
	public boolean containsNonAccessibleAnnotationType(AnnotationTypeDeclaration annotation){
		return this.getVersionNonAccessibleAnnotationType(annotation) != null;
	}

	public boolean containsAccessibleEnum(EnumDeclaration type){
		return this.getVersionAccessibleEnum(type) != null;
	}

	public boolean containsNonAccessibleEnum(EnumDeclaration type){
		return this.getVersionNonAccessibleEnum(type) != null;
	}

	public FieldDeclaration getVersionField(FieldDeclaration field, TypeDeclaration type) throws BindingException{
		for (TypeDeclaration versionType : this.apiAccessibleTypes) {
			if(versionType.getName().toString().equals(type.getName().toString())){
				for (FieldDeclaration versionField : versionType.getFields()) {
					String name1 = UtilTools.getFieldName(versionField);
					String name2  = UtilTools.getFieldName(field);
					if(name1 != null && name2 != null && name1.equals(name2)){
						return versionField;
					}
				}
			}
		}
		return null;
	}

	public ArrayList<MethodDeclaration> getAllEqualMethodsByName(MethodDeclaration method, TypeDeclaration type) {
		ArrayList<MethodDeclaration> result = new ArrayList<MethodDeclaration>();
		for (TypeDeclaration versionType : this.apiAccessibleTypes) {
			if(versionType.getName().toString().equals(type.getName().toString())){
				for(MethodDeclaration versionMethod : versionType.getMethods()){
					if(versionMethod.getName().toString().equals(method.getName().toString()))
						result.add(versionMethod);
				}
			}
		}
		return result;
	}
	
	/**
	 * Retorna o método da classe que possui o mesmo nome, retorno e assinatura. Retorna nulo se o método não for encontrado.
	 * @param method
	 * @param type
	 * @return
	 */
	public MethodDeclaration findMethodByNameAndParametersAndReturn(MethodDeclaration method, TypeDeclaration type){
		MethodDeclaration methodVersionOld = null;
		for (TypeDeclaration versionType : this.apiAccessibleTypes) {
			if(versionType.getName().toString().equals(type.getName().toString())){
				for(MethodDeclaration versionMethod : versionType.getMethods()){
					if(!ComparatorMethod.isDiffMethodByNameAndParametersAndReturn(versionMethod, method)){
						methodVersionOld =  versionMethod;
					}
				}
			}
		}
		return methodVersionOld;
	}
	
	/**
	 * Retorna o método da classe que possui o mesmo nome e tipo de  retorno. Retorna nulo se o método não for encontrado.
	 * @param method
	 * @param type
	 * @return
	 */
	private MethodDeclaration findMethodByNameAndReturn(MethodDeclaration method, TypeDeclaration type){
		MethodDeclaration methodVersionOld = null;
		for (TypeDeclaration versionType : this.apiAccessibleTypes) {
			if(versionType.getName().toString().equals(type.getName().toString())){
				for(MethodDeclaration versionMethod : versionType.getMethods()){
					if(!ComparatorMethod.isDiffMethodByNameAndReturn(versionMethod, method)){
						methodVersionOld =  versionMethod;
					}
				}
			}
		}
		return methodVersionOld;
	}
	
	/**
	 * Retorna o método da classe que possui o mesmo nome e assinatura. Retorna nulo se o método não for encontrado.
	 * @param method
	 * @param type
	 * @return
	 */
	public MethodDeclaration findMethodByNameAndParameters(MethodDeclaration method, TypeDeclaration type){
		MethodDeclaration methodVersionOld = null;
		for (TypeDeclaration versionType : this.apiAccessibleTypes) {
			if(versionType.getName().toString().equals(type.getName().toString())){
				for(MethodDeclaration versionMethod : versionType.getMethods()){
					if(!ComparatorMethod.isDiffMethodByNameAndParameters(versionMethod, method)){
						methodVersionOld =  versionMethod;
					}
				}
			}
		}
		return methodVersionOld;
	}

	/**
	 * Retorna o método na versão corrente que possuem o mesmo nome do método recebido como parâmetro.
	 * Retorna nulo se o método não for encontrado.
	 * @param method
	 * @param type
	 * @return
	 */
	public MethodDeclaration getEqualVersionMethod(MethodDeclaration method, TypeDeclaration type){
		for(MethodDeclaration methodInThisVersion : this.getAllEqualMethodsByName(method, type)){
			if(UtilTools.isEqualMethod(method, methodInThisVersion)){
				return methodInThisVersion;
			}
		}
		return null;
	}
	
	public ArrayList<AnnotationTypeMemberDeclaration> getAllEqualAnnotationMemberByName(AnnotationTypeMemberDeclaration member, AnnotationTypeDeclaration annotation) {
		ArrayList<AnnotationTypeMemberDeclaration> result = new ArrayList<AnnotationTypeMemberDeclaration>();
		for (AnnotationTypeDeclaration annotationType : this.apiAccessibleAnnotation) {
			if(annotationType.getName().toString().equals(annotation.getName().toString())){
				List<AnnotationTypeMemberDeclaration> members =  annotationType.bodyDeclarations();
				for(int i=0; i< members.size(); i++){
					AnnotationTypeMemberDeclaration memberComparation = members.get(i);
					if(memberComparation.getName().toString().equals(member.getName().toString()))
						result.add(memberComparation);
				}
			}
		}
		return result;
	}
	
	/**
	 * Retorna o membro de uma anotação na versão corrente que possuem o mesmo nome do membro recebido como parâmetro.
	 * Retorna nulo se o membro não for encontrado.
	 * @param method
	 * @param type
	 * @return
	 */
	public AnnotationTypeMemberDeclaration getEqualVersionAnnotationTypeMember(AnnotationTypeMemberDeclaration annotationMember, AnnotationTypeDeclaration annotationType){
		for(AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration : this.getAllEqualAnnotationMemberByName(annotationMember, annotationType)){
			if(UtilTools.isEqualAnnotationMember(annotationMember, annotationTypeMemberDeclaration)){
				return annotationTypeMemberDeclaration;
			}
		}
		return null;
	}

	public EnumConstantDeclaration getEqualVersionConstant(EnumConstantDeclaration constant, EnumDeclaration enumReference) {
		EnumDeclaration thisVersionEnum = this.getVersionAccessibleEnum(enumReference);
		for(Object thisVersionConstant : thisVersionEnum.enumConstants()){
			if(((EnumConstantDeclaration)thisVersionConstant).getName().toString().equals(constant.getName().toString()))
				return ((EnumConstantDeclaration)thisVersionConstant);
		}

		return null;
	}
	
	/**
	 * Retorna a lista de todos os types de uma versão.
	 * @param version
	 * @return
	 */
	public List<TypeDeclaration> getAllTypes(){
		List<TypeDeclaration> listTypesVersion = new ArrayList<TypeDeclaration>();
		listTypesVersion.addAll(this.getApiNonAcessibleTypes());
		listTypesVersion.addAll(this.getApiAcessibleTypes());
		return listTypesVersion;
	}
	
	/**
	 * Retorna a lista de todos as anotações de uma versão.
	 * @param version
	 * @return
	 */
	public List<AnnotationTypeDeclaration> getAllAnnotationTypes(){
		List<AnnotationTypeDeclaration> listAnnotationTypesVersion = new ArrayList<AnnotationTypeDeclaration>();
		listAnnotationTypesVersion.addAll(this.getApiNonAccessibleAnnotation());
		listAnnotationTypesVersion.addAll(this.getApiAccessibleAnnotation());
		return listAnnotationTypesVersion;
	}
	
	/**
	 * Retorna a lista de todos os enums de uma versão.
	 * @param version
	 * @return
	 */
	public List<EnumDeclaration> getAllEnums(){
		List<EnumDeclaration> listTypesVersion = new ArrayList<EnumDeclaration>();
		listTypesVersion.addAll(this.getApiAccessibleEnums());
		listTypesVersion.addAll(this.getApiNonAccessibleEnums());
		return listTypesVersion;
	}

	public ArrayList<AnnotationTypeDeclaration> getApiAccessibleAnnotation() {
		return apiAccessibleAnnotation;
	}

	public void setApiAccessibleAnnotation(ArrayList<AnnotationTypeDeclaration> apiAccessibleAnnotation) {
		this.apiAccessibleAnnotation = apiAccessibleAnnotation;
	}

	public ArrayList<AnnotationTypeDeclaration> getApiNonAccessibleAnnotation() {
		return apiNonAccessibleAnnotation;
	}

	public void setApiNonAccessibleAnnotation(ArrayList<AnnotationTypeDeclaration> apiNonAccessibleAnnotation) {
		this.apiNonAccessibleAnnotation = apiNonAccessibleAnnotation;
	}

	
}
