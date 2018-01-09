package apidiff.analysis.description;

public class MethodDescription extends TemplateDescription {

	public String remove(final String nameMethod, final String nameClass){
		return super.messageRemoveTemplate("method", nameMethod, "class", nameClass);
	}
	
	public String visibility(final String nameMethod, final String nameClass, final String visibility1, final String visibility2){
		return super.messageVisibilityTemplate("method", nameMethod, "class", nameClass, visibility1, visibility2);
	}
	
	public String parameter(final String nameMethodAfter, final String nameMethodBefore, final String nameClass){
		return super.messageParameterTemplate("method", nameMethodAfter, nameMethodBefore, "class", nameClass);
	}
	
	public String exception(final String nameMethod, final String nameClass){
		return super.messageExceptionTemplate("method", nameMethod, "class", nameClass);
	}
	
	public String returnType(final String nameMethod, final String nameClass){
		return super.messageReturnTypeTemplate("method", nameMethod, "class", nameClass);
	}
	
	public String modifierStatic(final String nameMethod, final String nameClass, final Boolean isGain){
		return this.messageStaticTemplate("method", nameMethod, "class", nameClass, isGain);
	}
	
	public String modifierFinal(final String nameMethod, final String nameClass, final Boolean isGain){
		return this.messageFinalTemplate("method", nameMethod, "class", nameClass, isGain);
	}
	
	public String move(final String operation, final String fullName, final String nameClassBefore, final String nameClassAfter){
		return this.messageMoveTemplate(operation, fullName, nameClassBefore, nameClassAfter);
	}
	
}
