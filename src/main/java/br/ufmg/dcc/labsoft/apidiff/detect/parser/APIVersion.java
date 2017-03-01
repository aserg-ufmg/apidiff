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
import br.ufmg.dcc.labsoft.apidiff.detect.diff.service.git.GitFile;
import br.ufmg.dcc.labsoft.apidiff.detect.exception.BindingException;

public class APIVersion {

	private ArrayList<TypeDeclaration> apiAccessibleTypes = new ArrayList<TypeDeclaration>();;
	private ArrayList<TypeDeclaration> apiNonAccessibleTypes = new ArrayList<TypeDeclaration>();;
	private ArrayList<EnumDeclaration> apiAccessibleEnums = new ArrayList<EnumDeclaration>();
	private ArrayList<EnumDeclaration> apiNonAccessibleEnums = new ArrayList<EnumDeclaration>();
	private Map<ChangeType, List<GitFile>> mapModifications = new HashMap<ChangeType, List<GitFile>>();
	private List<String> listFilesMofify = new ArrayList<String>();
	
	private Logger logger = LoggerFactory.getLogger(APIVersion.class);

	public APIVersion(final File path, final Map<ChangeType, List<GitFile>> mapModifications) {
		
		try {
			this.mapModifications = mapModifications;
			
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
			this.parseFilesInDir(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void parseFilesInDir(File file) throws IOException {
		if (file.isFile()) {
			if (UtilTools.isJavaFile(file.getName()) && this.isFileModification(file)) {
				this.parse(UtilTools.readFileToString(file.getAbsolutePath()), file);		
			}
		} else {
			for (File f : file.listFiles()) {
				this.parseFilesInDir(f);
			}
		}
	}

	public void parse(String str, File source) throws IOException {
		
		if(this.mapModifications.size() > 0 && !this.isFileModification(source)){
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
		compilationUnit.accept(visitorType);
		compilationUnit.accept(visitorEnum);
		this.apiAccessibleTypes.addAll(visitorType.getAcessibleTypes());
		this.apiNonAccessibleTypes.addAll(visitorType.getNonAcessibleTypes());
		this.apiAccessibleEnums.addAll(visitorEnum.getAcessibleEnums());
		this.apiNonAccessibleEnums.addAll(visitorEnum.getNonAcessibleEnums());
	}
	
	/**
	 * Retorna verdadeiro se o arquivo está na lista de arquivos modificados na última versão.
	 * Falso caso contrário.
	 * @param source
	 * @return
	 */
	private Boolean isFileModification(final File source){
		String path = source.getAbsolutePath();
		if(this.listFilesMofify.contains(path)){
			return true;
		}
		return false;
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

	public boolean contaisAccessibleType(TypeDeclaration type){
		return this.getVersionAccessibleType(type) != null;
	}

	public boolean contaisNonAccessibleType(TypeDeclaration type){
		return this.getVersionNonAccessibleType(type) != null;
	}

	public boolean contaisAccessibleEnum(EnumDeclaration type){
		return this.getVersionAccessibleEnum(type) != null;
	}

	public boolean contaisNonAccessibleEnum(EnumDeclaration type){
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

	public MethodDeclaration getEqualVersionMethod(MethodDeclaration method, TypeDeclaration type){
		for(MethodDeclaration methodInThisVersion : this.getAllEqualMethodsByName(method, type)){
			if(UtilTools.isEqualMethod(method, methodInThisVersion))
				return methodInThisVersion;
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

}
