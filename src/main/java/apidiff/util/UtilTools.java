package apidiff.util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import apidiff.enums.Classifier;
import apidiff.internal.visitor.APIVersion;

public class UtilTools {
	
	private static Properties properties = null;
	
	public static boolean isEqualAnnotationMember(AnnotationTypeMemberDeclaration member1, AnnotationTypeMemberDeclaration member2){
		if(!member1.getName().toString().equals(member2.getName().toString())){
			return false;
		}
		return true;
	}
	
	public static boolean isEqualMethod(MethodDeclaration method1, MethodDeclaration method2){
		if(!method1.getName().toString().equals(method2.getName().toString()))
			return false;
		if(method1.parameters().size() != method2.parameters().size())
			return false;
		for(int i = 0; i < method1.parameters().size(); i++){
			String typeP1 = ((SingleVariableDeclaration) method1.parameters().get(i)).getType().toString();
			String typeP2 = ((SingleVariableDeclaration) method2.parameters().get(i)).getType().toString();
			if(!typeP1.equals(typeP2))
				return false;
		}
		
		return true;
	}
	
	public static String getFieldName(FieldDeclaration field){
		String name = null;
		List<VariableDeclarationFragment> variableFragments = field.fragments();
		for (VariableDeclarationFragment variableDeclarationFragment : variableFragments) {
			if(variableDeclarationFragment.resolveBinding() != null){
				name = variableDeclarationFragment.resolveBinding().getName();
			}
		}
		return name;
	}
	
	public static String getTypeName(TypeDeclaration type){
		return type.resolveBinding() == null ? "" : type.resolveBinding().getQualifiedName();
	}
	
	public static boolean isVisibilityPrivate(BodyDeclaration node){
		return getVisibility(node).equals("private");
	}
	
	public static boolean isVisibilityPublic(BodyDeclaration node){
		return getVisibility(node).equals("public");
	}
	
	public static boolean isVisibilityDefault(BodyDeclaration node){
		return getVisibility(node).equals("default");
	}
	
	public static boolean isVisibilityProtected(BodyDeclaration node){
		return getVisibility(node).equals("protected");
	}
	
	
	public static Boolean isFinal(BodyDeclaration node){
		return containsModifier(node, "final");
	}
	
	public static Boolean isStatic(BodyDeclaration node){
		return containsModifier(node, "static");
	}
	
	/**
	 * Busca modificador na lista de modificadores do nó.
	 * @param node
	 * @param modifier
	 * @return
	 */
	public static Boolean containsModifier(BodyDeclaration node, String modifier){
		for (Object m : node.modifiers()) {
			if(m.toString().equals(modifier)){
				return true;
			}
		}
		return false;
	}
	
	public static String getVisibility(BodyDeclaration node){
		for (Object modifier : node.modifiers()) {
			if(modifier.toString().equals("public") || modifier.toString().equals("private")
					|| modifier.toString().equals("protected")){
				return modifier.toString();
			}
		}
		
		return "default";
	}
	
	public static String readFileToString(String filePath) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));

		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}

		reader.close();

		return fileData.toString();
	}
	
	public static void addChangeToTypeMap(TypeDeclaration type, BodyDeclaration change, 
			HashMap<TypeDeclaration, ArrayList<BodyDeclaration>> changeMap) {
			
			if(changeMap.containsKey(type)){
				changeMap.get(type).add(change);
			}else{
				changeMap.put(type, new ArrayList<BodyDeclaration>());
				changeMap.get(type).add(change);
			}	
	}
	
	public static void addChangeToEnumMap(EnumDeclaration type, BodyDeclaration change, 
			HashMap<EnumDeclaration, ArrayList<BodyDeclaration>> changeMap) {
			
			if(changeMap.containsKey(type)){
				changeMap.get(type).add(change);
			}else{
				changeMap.put(type, new ArrayList<BodyDeclaration>());
				changeMap.get(type).add(change);
			}	
	}
	
	/**
	 * 
	 * @return Retorna o  path da classe/nó. Exemplo: io.reactivex.annotations.BackpressureKind
	 * 		   String vazia se não foi possível ler o binding.
	 */
	public static  String getPath(final AbstractTypeDeclaration node){
		return ((node == null) || (node.resolveBinding() == null) || (node.resolveBinding().getQualifiedName() == null))? "" : node.resolveBinding().getQualifiedName();
	}
	
	
	public static Boolean isAPIByClassifier(String pathLibrary, Classifier classifierAPI) throws IOException{
		Boolean isAPI = false;
		switch (classifierAPI){
			case EXAMPLE:
				isAPI = isNonAPIExample(pathLibrary)?true:false;
				break;
			case INTERNAL:
				isAPI = isNonAPIInternal(pathLibrary)?true:false;
				break;
			case TEST:
				isAPI = isNonAPITest(pathLibrary)?true:false;
				break;
			case EXPERIMENTAL:
				isAPI = isNonAPIExperimental(pathLibrary)?true:false;
				break;
			case API:
				isAPI = isInterfaceStable(pathLibrary)?true:false;
				break;
			default:
				break;
	      }
		return isAPI;
	}
	
	public static Boolean isNonAPITest(String pathAPI){
		String regexTest =  "(?i)(\\/test)|(test\\/)|(tests\\/)|(test\\.java$)|(tests\\.java$)";
		return (pathAPI !=null  && !"".equals(pathAPI) && checkCountainsByRegex(regexTest, pathAPI))?true:false;
	}
	
	/**
	 * Remove possíveis pacotes com o nome da biblioteca. O nome muitas vezes pode ser iterpretado como um pacote de teste, internal, etc.
	 * @param path - path da API analisada.
	 * @param nameProject - nome do projeto.
	 * @return
	 * @throws IOException 
	 */
	public static String getSimpleNameFileWithouPackageWithNameLibrary(String path, String absolutePath, final String nameProject) throws IOException{
		String simpleNameFile = absolutePath.replaceAll(path + "/" + nameProject, "");
		String[] names = nameProject.split("/");
		for(int i=0; i<names.length; i++){
			simpleNameFile = simpleNameFile.replaceAll(names[i] + "/", "");
		}
		return simpleNameFile;
	}
	
	public static Boolean isNonAPIInternal(String pathAPI){
		return (pathAPI !=null  && !"".equals(pathAPI)  && pathAPI.toLowerCase().contains("/internal/"))?true:false;
	}
	
	public static Boolean isNonAPIExperimental(String pathAPI){
		return (pathAPI !=null  && !"".equals(pathAPI)  && pathAPI.toLowerCase().contains("/experimental/"))?true:false;
	}
	
	public static Boolean isNonAPIDemo(String pathAPI){
		String regexDemo = "(?i)(\\/demo)|(demo\\/)";
		return (pathAPI !=null  && !"".equals(pathAPI)  &&  checkCountainsByRegex(regexDemo, pathAPI))?true:false;
	}
	
	public static Boolean isNonAPISample(String pathAPI){
		String regexSample = "(?i)(sample\\/)|(samples\\/)";
		return (pathAPI !=null  && !"".equals(pathAPI)  && checkCountainsByRegex(regexSample, pathAPI))?true:false;
	}
	
	public static Boolean isNonAPIExample(String pathAPI){
		String regexExample = "(?i)(\\/example)|(example\\/)|(examples\\/)";
		Boolean isNonAPIExample = checkCountainsByRegex(regexExample, pathAPI) || isNonAPIDemo(pathAPI) || isNonAPISample(pathAPI);
		return (pathAPI !=null  && !"".equals(pathAPI) && isNonAPIExample)?true:false;
	}
	
	public static Boolean checkCountainsByRegex(String regex, String pathAPI){
		String path = pathAPI.toLowerCase();
		Pattern r = Pattern.compile(regex);
		Matcher m = r.matcher(path);
		return m.find();
	}
	
	/**
	 * Verifica pelo caminho completo do arquivo, se ele está dentro de pacotes que indicam interfaces instáveis.
	 * Exemplo: /project/tests/Util.java ou /project/internal/Util.java
	 * @param pathCompleteFile - Caminho completo do arquivo no sistema. Exemplo: /home/user/projectsAPIBreakingChange/nameProject/src/main/java/br/com/api/Util.java
	 * @return
	 * @throws IOException
	 */
	public static Boolean isInterfaceStable(String pathLibrary) throws IOException{
	  if((!"".equals(pathLibrary) && !isNonAPIExample(pathLibrary) && !isNonAPIExperimental(pathLibrary) && !isNonAPIInternal(pathLibrary) && !isNonAPITest(pathLibrary))){
		return true;
	  }
	  return false;
	}
	
	/**
	 * Retorna verdadeiro se o arquivo termina com ".java". Falso caso contrário.
	 * @param nameFile
	 * @return
	 */
	public static Boolean isJavaFile(final String nameFile){
		return (nameFile!=null && nameFile.endsWith(".java"))?true:false;
	}
	
//	public static Properties getProperties() throws IOException{
//		try {
//			if(properties == null){
//				properties = new Properties();
//		    	InputStream input = APIDiff.class.getClassLoader().getResourceAsStream("config.properties");
//		    	properties.load(input);
//			}
//    		return properties;
//		} catch (Exception e) {
//			throw new IOException("Path project not found, check the properties file.", e);
//		}
//	}
	
//	public static String getPathProjects() throws IOException{
//		return UtilTools.getProperties().getProperty("PATH_PROJECT");
//	}
	
	public static String getPathProject(final String path, final String nameProject) throws IOException{
		String pathComplete = isNullOrEmpty(path) ? "./" : path + "/";
		pathComplete += isNullOrEmpty(nameProject) ? "" : nameProject;
		return pathComplete;
	}
	
	public static Boolean isNullOrEmpty(final String text){
		return (text == null || "".equals(text));
	}
	
	/**
	 * Retorna a lista dos types acessíveis de uma versão. No contexto de clientes externos,
	 * apenas os métodos public e protected são acessíveis. Métodos default não são considerados.
	 * @param version
	 * @return
	 */
	public static List<AbstractTypeDeclaration> getAcessibleTypes(APIVersion version){
		List<AbstractTypeDeclaration> list = new ArrayList<AbstractTypeDeclaration>();
		for(AbstractTypeDeclaration type: version.getTypesPublicAndProtected()){
			if(UtilTools.isVisibilityPublic(type) || UtilTools.isVisibilityProtected(type)){
				list.add(type);
			}
		}
		return list;
	}
	
	/**
	 * Retorna a lista de types que estão nas duas versões, ou seja, a interseção das duas listas.
	 * Os types retornados são aqueles contidos na última versão (listVersion2).
	 * @param listVersion1
	 * @param listVersion2
	 * @return
	 */
	public static  List<TypeDeclaration> getIntersectionListTypes(List<TypeDeclaration> listVersion1, List<TypeDeclaration> listVersion2){
		List<TypeDeclaration> list = new ArrayList<TypeDeclaration>();
		for(TypeDeclaration type: listVersion2){
			if(listVersion1.contains(type)){
				list.add(type);
			}
		}
		return list;
	}
	

	public static Boolean containsJavadoc(final AbstractTypeDeclaration node){
		return ((node != null) && (node.getJavadoc() != null) && (!node.getJavadoc().equals("")))? true : false;
	} 
	
	/**
	 * Se o nó não possui javadoc, retorna prefixo.
	 * @param node
	 * @return
	 */
	public static String getSufixJavadoc(final AbstractTypeDeclaration node){
		return ((node != null) && (node.getJavadoc() != null) && (!node.getJavadoc().equals("")))? "" : " WITHOUT JAVADOC";
	}
	
	public static Boolean containsJavadoc(final AbstractTypeDeclaration node, final MethodDeclaration methodDeclaration){
		Boolean typeContainsJavadoc = containsJavadoc(node);
		return (typeContainsJavadoc && (methodDeclaration != null) && (methodDeclaration.getJavadoc() != null) && (!methodDeclaration.getJavadoc().equals("")))? true : false;
	}
	
	public static Boolean containsJavadoc(final AbstractTypeDeclaration node, final FieldDeclaration fieldInVersion){
		return containsJavadoc(node) && containsJavadoc(fieldInVersion);
	}
	
	public static Boolean containsJavadoc(final FieldDeclaration fieldInVersion){
		return ((fieldInVersion != null) && (fieldInVersion.getJavadoc() != null) && (!fieldInVersion.getJavadoc().equals("")))? true : false;
	}
	
	/**
	 * Se o método não possui javadoc, retorna prefixo.
	 * @param node
	 * @return
	 */
	public static String getSufixJavadoc(final MethodDeclaration methodDeclaration){
		return ((methodDeclaration != null) && (methodDeclaration.getJavadoc() != null) && (!methodDeclaration.getJavadoc().equals("")))? "" : " WITHOUT JAVADOC";
	}
	
	/**
	 * Se o membro da anotação não possui javadoc, retorna prefixo.
	 * @param node
	 * @return
	 */
	public static String getSufixJavadoc(final AnnotationTypeMemberDeclaration annotationMember){
		return ((annotationMember != null) && (annotationMember.getJavadoc() != null) && (!annotationMember.getJavadoc().equals("")))? "" : " WITHOUT JAVADOC";
	}

	/**
	 * Se o fild não possui javadoc, retorna prefixo.
	 * @param node
	 * @return
	 */
	public static String getSufixJavadoc(final FieldDeclaration fieldInVersion){
		return ((fieldInVersion != null) && (fieldInVersion.getJavadoc() != null) && (!fieldInVersion.getJavadoc().equals("")))? "" : " WITHOUT JAVADOC";
	}
	
	/**
	 * Muda a primeira letra da palavra para maiúsculo.
	 * @param str
	 * @return
	 */
	public static String upperCaseFirstLetter(String str){
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
	
	/**
	 * Muda a primeira letra da palavra para minúsculo.
	 * @param str
	 * @return
	 */
	public static String downCaseFirstLetter(String str){
		return str.substring(0, 1).toLowerCase() + str.substring(1);
	}
	
}