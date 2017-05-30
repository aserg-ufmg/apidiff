package br.ufmg.dcc.labsoft.apidiff.detect.diff.comparator;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;

/**
 * Classes com funções utilitárias para comparar propriedades de métodos.
 * @author aline
 */
public class ComparatorMethod {
	
	/**
	 * Retorna verdadeiro se dois métodos tem assinaturas diferentes, incluindo a ordem dos parâmetros.
	 * Exemplo:
	 * 
	 * int, String -- int, String : iguais.
	 * String, Integer -- Integer, String: Diferentes.
	 * float, boolean -- boolean: diferentes.
	 * 
	 * @return
	 */
	public static Boolean isDiffByParameters(MethodDeclaration methodVersion1, MethodDeclaration methodVersion2){
		//Se a lista de parâmetros tem tamanhos diferentes.
		if(methodVersion1.parameters().size() != methodVersion2.parameters().size()){
			return true;
		}
		else{
			//Se tem o mesma quantidade de parâmetros, mas eles são diferentes.
			for(int i = 0; i < methodVersion1.parameters().size(); i++){
				
				String parameterVersion1 = methodVersion1.parameters().get(i).toString();
				String parameterVersion2 = methodVersion2.parameters().get(i).toString();
				
				int indexVersion1 = parameterVersion1.lastIndexOf(" "); //índice para o início do nome da variável.
				int indexVersion2 = parameterVersion2.lastIndexOf(" "); //índice para o início do nome da variável.
				
				parameterVersion1 = parameterVersion1.substring(0, indexVersion1);
				parameterVersion2 = parameterVersion2.substring(0, indexVersion2);
				
				if(!parameterVersion1.equals(parameterVersion2)){
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Retorna verdadeiro se os métodos tem tipos de retornos diferentes.
	 * Falso se os métodos são iguais.
	 * @param methodVersion1
	 * @param methodVersion2
	 * @return
	 */
	public static Boolean isDiffByReturn(MethodDeclaration methodVersion1, MethodDeclaration methodVersion2){
		Type returnType1 = methodVersion1.getReturnType2();
		Type returnType2 = methodVersion2.getReturnType2();
		
		if(returnType1 != null && returnType2 != null &&  !returnType1.toString().equals(returnType2.toString())){
			return true;
		}
		return false;
	}
	
	/**
	 * Retorna verdadeiro se os nomes dos métodos são diferentes. 
	 * Falso caso contrário.
	 * @param methodVersion1
	 * @param methodVersion2
	 * @return
	 */
	public static Boolean isDiffByName(MethodDeclaration methodVersion1, MethodDeclaration methodVersion2){
		return methodVersion1.getName().toString().equals(methodVersion2.getName().toString())?false:true;
	}
	
	
	/**
	 * Retorna verdadeiro se os metodos são diferentes. Ou seja, possuem nomes diferentes ou lista de parâmetros ou tipo retorno.
	 * @param methodVersion1
	 * @param methodVersion2
	 * @return
	 */
	 public static Boolean isDiffMethodByNameAndParametersAndReturn(MethodDeclaration methodVersion1, MethodDeclaration methodVersion2){
		if(ComparatorMethod.isDiffByName(methodVersion1, methodVersion2) ||
				ComparatorMethod.isDiffByParameters(methodVersion1, methodVersion2) ||
				ComparatorMethod.isDiffByReturn(methodVersion1, methodVersion2)){
			return true;
		}
		return false;
	}
	 
	/**
	 * Retorna verdadeiro se os metodos são diferentes. Ou seja, possuem nomes diferentes ou lista de parâmetros.
	 * @param methodVersion1
	 * @param methodVersion2
	 * @return
	 */
	 public static Boolean isDiffMethodByNameAndParameters(MethodDeclaration methodVersion1, MethodDeclaration methodVersion2){
		if(ComparatorMethod.isDiffByName(methodVersion1, methodVersion2) ||
				ComparatorMethod.isDiffByParameters(methodVersion1, methodVersion2)){
			return true;
		}
		return false;
	}
	 
	/**
	 * Retorna verdadeiro se os metodos são diferentes. Ou seja, possuem nomes diferentes ou tipo de retorno.
	 * @param methodVersion1
	 * @param methodVersion2
	 * @return
	 */
	 public static Boolean isDiffMethodByNameAndReturn(MethodDeclaration methodVersion1, MethodDeclaration methodVersion2){
		if(ComparatorMethod.isDiffByName(methodVersion1, methodVersion2) ||
				ComparatorMethod.isDiffByReturn(methodVersion1, methodVersion2)){
			return true;
		}
		return false;
	}

}
