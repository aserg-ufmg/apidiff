package br.ufmg.dcc.labsoft.apidiff;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import br.ufmg.dcc.labsoft.apidiff.detect.exception.BindingException;

public class UtilTools {
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
	
	public static String getFieldName(FieldDeclaration field) throws BindingException{
		String name = null;
		List<VariableDeclarationFragment> variableFragments = field.fragments();
		for (VariableDeclarationFragment variableDeclarationFragment : variableFragments) {
			if(variableDeclarationFragment.resolveBinding() == null){
				throw new BindingException();
			}
			name = variableDeclarationFragment.resolveBinding().getName();
		}
		return name;
	}
	
	public static boolean isDeprecatedField(FieldDeclaration field){
		List<VariableDeclarationFragment> variableFragments = field.fragments();
		for (VariableDeclarationFragment variableDeclarationFragment : variableFragments) {
			if(variableDeclarationFragment.resolveBinding() != null &&
					variableDeclarationFragment.resolveBinding().isDeprecated()){
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isPrivate(BodyDeclaration node){
		return getVisibility(node).equals("private");
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
	public static  String getNameNode(final AbstractTypeDeclaration node){
		return ((node == null) || (node.resolveBinding().getQualifiedName() == null))?"":node.resolveBinding().getQualifiedName();
	}
	
	/**
	 * Verifica se a classe é interface estável. 
	 * Interfaces instáveis podem ser APIs internas, experimentais, ou pacotes de testes.
	 * @param node
	 * @return
	 */
	public static Boolean isInterfaceStable(AbstractTypeDeclaration node){
		String nameNode = UtilTools.getNameNode(node).toLowerCase();
		
		if("".equals(nameNode) || nameNode.contains("test") || nameNode.contains("example") || nameNode.contains(".internal.") || nameNode.contains(".experimental.") ){
			return false;
		}

		return true;
	}

}
