package apidiff.analysis.description;

import apidiff.util.UtilTools;

public class TemplateDescription {
	
	/**
	 * Template para mensagem de recursos removido.
	 * @param typeStruture
	 * @param nameStruture
	 * @param typePath
	 * @param path
	 * @return
	 */
	protected String messageRemoveTemplate(final String typeStruture, final String nameStruture, final String typePath, final String path){
		String message = "";
		message += "<b>Remove " + UtilTools.upperCaseFirstLetter(typeStruture) + ":</b>";
		message += "<br>" + typeStruture.toLowerCase() + " <code>" + nameStruture +"</code>";
		message += "<br>removed  from " + typePath + " <code>" + path + "</code>";
		message += "<br>";
		return message;
	}

	/**
	 * Template para mensagem de perda de visibilidade.
	 * @param typeStruture
	 * @param nameStruture
	 * @param typePath
	 * @param path
	 * @param visibility1
	 * @param visibility2
	 * @return
	 */
	protected String messageVisibilityTemplate(final String typeStruture, final String nameStruture, final String typePath, final String path,  final String visibility1, final String visibility2){
		String message = "";
		message += "<b>Loss of " + UtilTools.upperCaseFirstLetter(typeStruture) + " Visibility:</b>";
		message += "<br><code>" + nameStruture +"</code>";
		message += "<br> changed visibility from <code>" + visibility1  + "</code> to <code>"  + visibility2 + "</code>";
		message += "<br>in " + typePath + " <code>" + path + "</code>";
		message += "<br>";
		return message;
	}
	
	/**
	 * Template para mensagem de mudança de valor default.
	 * @param typeStruture
	 * @param nameStruture
	 * @param typePath
	 * @param path
	 * @param value1
	 * @param value2
	 * @return
	 */
	protected String messageChangeDefaultValueTemplate(final String typeStruture, final String nameStruture, final String typePath, final String path,  final String value1, final String value2){
		String message = "";
		message += "<b>Change Default Value:</b>";
		message += "<br>" + UtilTools.downCaseFirstLetter(typeStruture) + "<code>" + nameStruture +"</code>";
		message += "<br>changed default value from " + value1  + " to "  + value2;
		message += "<br>in " + typePath + " <code>" + path + "</code>";
		message += "<br>";
		return message;
	}


	/**
	 * Template para mensagem de mudança no modificador final.
	 * @param typeStruture
	 * @param nameStruture
	 * @param typePath
	 * @param path
	 * @param gain
	 * @return
	 */
	protected String messageFinalTemplate(final String typeStruture, final String nameStruture, final String typePath, final String path, final Boolean gain){
		String message = "";
		message += "<b>Change <code>final</code> modifier</b>:";
		message += "<br>" + UtilTools.downCaseFirstLetter(typeStruture) + " <code>" + nameStruture +"</code>";
		message += gain ? "<br>received the modifier <code>final</code>" : "<br>lost the modifier <code>final</code>";
		message += "<br>in " + typePath + " <code>" + path + "</code>";
		message += "<br>";
		return message;
	}
	
	/**
	 * Template para mensagem de mudança no modificador static.
	 * @param typeStruture
	 * @param nameStruture
	 * @param typePath
	 * @param path
	 * @param gain
	 * @return
	 */
	protected String messageStaticTemplate(final String typeStruture, final String nameStruture, final String typePath, final String path, final Boolean gain){
		String message = "";
		message += "<b>Change <code>static</code> modifier</b>:";
		message += "<br>" + UtilTools.downCaseFirstLetter(typeStruture) + " <code>" + nameStruture +"</code>";
		message += gain ? "<br>received the modifier <code>static</code>" : "<br>lost the modifier <code>static</code>";
		message += "<br>in " + typePath + " <code>" + path + "</code>";
		message += "<br>";
		return message;
	}
	
	/**
	 * Template para mensagem de mudança do tipo de retorno.
	 * @param typeStruture
	 * @param nameStruture
	 * @param struture
	 * @param typePath
	 * @param path
	 * @return
	 */
	protected String messageReturnTypeTemplate(final String typeStruture,  final String nameStruture, final String typePath, final String path){
		String message = "";
		message += "<b>Change Return Type of " + UtilTools.upperCaseFirstLetter(typeStruture) + "</b>:";
		message += "<br>" + UtilTools.downCaseFirstLetter(typeStruture) + " <code>" + nameStruture +"</code>";
		message += "<br>changed the return type";
		message += "<br>in " + typePath + " <code>" + path + "</code>";
		message += "<br>";
		return message;
	}
	
	/**
	 * Template para mensagem de mudança da lista de parâmetros.
	 * @param typeStruture
	 * @param nameStruture
	 * @param typePath
	 * @param path
	 * @return
	 */
	protected String messageParameterTemplate(final String typeStruture, final String nameStruture, final String typePath, final String path){
		String message = "";
		message += "<b>Change " + UtilTools.upperCaseFirstLetter(typeStruture) + " Parameters</b>:";
		message += "<br><code>" + nameStruture +"</code>";
		message += "<br>changed the list parameters";
		message += "<br>in " + typePath + " <code>" + path + "</code>";
		message += "<br>";
		return message;
	}
	
	/**
	 * Template para mensagem de mudança da lista de exceções.
	 * @param typeStruture
	 * @param nameStruture
	 * @param typePath
	 * @param path
	 * @return
	 */
	protected String messageExceptionTemplate(final String typeStruture, final String nameStruture, final String typePath, final String path){
		String message = "";
		message += "<b>Change " + UtilTools.upperCaseFirstLetter(typeStruture) + " Exceptions</b>:";
		message += "<br><code>" + nameStruture +"</code>";
		message += "<br>changed the list exception";
		message += "<br>in " + typePath + " <code>" + path + "</code>";
		message += "<br>";
		return message;
	}

}
