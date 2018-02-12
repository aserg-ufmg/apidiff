package apidiff.analysis.description;

import java.util.List;

import org.eclipse.jdt.core.dom.SimpleType;

import apidiff.enums.Category;
import apidiff.util.UtilTools;
import refdiff.core.rm2.model.refactoring.SDRefactoring;

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
	
	public String exception(final String nameMethodBefore, final List<SimpleType> listExceptionBefore, final List<SimpleType> listExceptionAfter, final String nameClassBefore){
		String message = "";
		message += "<br><code>" + nameMethodBefore +"</code>";
		
		String listBefore = (listExceptionBefore == null || listExceptionBefore.isEmpty()) ? "" : listExceptionBefore.toString();
		String listAfter = (listExceptionAfter == null || listExceptionAfter.isEmpty())? "" : listExceptionAfter.toString();
		
		if(!UtilTools.isNullOrEmpty(listBefore) && !UtilTools.isNullOrEmpty(listAfter)){
			message += "<br>changed the list exception";
			message += "<br>from <code>" + listBefore + "</code>";
			message += "<br>to <code>" + listAfter + "</code>";
		}
		
		if(UtilTools.isNullOrEmpty(listBefore) && !UtilTools.isNullOrEmpty(listAfter)){
			message += "<br>added list exception " + listAfter + "</code>";
		}
		
		if(!UtilTools.isNullOrEmpty(listBefore) && UtilTools.isNullOrEmpty(listAfter)){
			message += "<br>removed list exception " + listBefore + "</code>";
		}

		message += "<br>in <code>" + nameClassBefore +"</code>";
		message += "<br>";
		return message;
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
	
	public String refactorMethod(final Category category, final SDRefactoring ref){
		
		String description = "";
		
		String[] entityBefore = ref.getEntityBefore().fullName().split("#");
		String[] entityAfter = ref.getEntityAfter().fullName().split("#");
		String nameClassBefore = entityBefore[0];
		String nameClassAfter = entityAfter[0];
		String nameMethodAfter = entityAfter[1];
		String nameMethodBefore = entityBefore[1];
		
		switch (category) {
			case METHOD_MOVE:
				description = this.move(nameMethodAfter, nameClassBefore, nameClassAfter);
				break;
	
			case METHOD_PULL_UP:
				description = this.pullUp(nameMethodAfter, nameClassBefore, nameClassAfter);
				break;
				
			case METHOD_PUSH_DOWN:
				description = this.pushDown(nameMethodAfter, nameClassBefore, nameClassAfter);
				break;
				
			case METHOD_INLINE:
				description = this.inline(nameMethodBefore, nameMethodAfter, nameClassBefore, nameClassAfter);
				break;
				
			case METHOD_RENAME:
				description = this.rename(nameMethodBefore, nameMethodAfter, nameClassAfter);
				break;
				
			default:
				description = "";
				break;
		}
		return description;
	}
	
	public String rename(final String nameMethodBefore, final String nameMethodAfter, final String nameClass){
		return this.messageRenameTemplate("method", nameMethodBefore, nameMethodAfter, nameClass);
	}
	
	public String pullUp(final String nameMethod, final String nameClassBefore, final String nameClassAfter){
		return this.messagePullUpTemplate("method", nameMethod, nameClassBefore, nameClassAfter);
	}
	
	public String pushDown(final String nameMethod, final String nameClassBefore, final String nameClassAfter){
		return this.messagePushDownTemplate("method", nameMethod, nameClassBefore, nameClassAfter);
	}
	
	public String addition(final String nameMethod, final String nameClass){
		String message = "";
		message += "<br>Method <code>" + nameMethod +"</code>";
		message += "<br>added in <code>" + nameClass +"</code>";
		message += "<br>";
		return message;
	}
	
	public String inline(final String nameMethodBefore, final String nameMethodAfter,  final String nameClassBefore, final String nameClassAfter){
		String message = "";
		message += "<br>Method <code>" + nameMethodBefore +"</code>";
		message += "<br>from <code>" + nameClassBefore +"</code>";
		message += "<br>inlined to  <code>" + nameMethodAfter +"</code>";
		message += "<br>in <code>" + nameClassAfter +"</code>";
		message += "<br>";
		return message;
	}
	
	
	public String deprecate(final String nameMethodBefore, final String nameClassBefore){
		return this.messageDeprecate("method", nameMethodBefore, nameClassBefore);
	}
	
	public String move(final String nameMethodAfter, final String nameClassBefore, final String nameClassAfter){
		return this.messageMoveTemplate("method", nameMethodAfter, nameClassBefore, nameClassAfter);
	}
	
}
