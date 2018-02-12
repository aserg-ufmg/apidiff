package apidiff.analysis.description;

import apidiff.enums.Category;
import refdiff.core.rm2.model.refactoring.SDRefactoring;

public class FieldDescription extends TemplateDescription {

	public String remove(final String nameField, final String nameClass){
		return super.messageRemoveTemplate("field", nameField, nameClass);
	}
	
	public String refactorField(final Category category, final SDRefactoring ref){
		
		String description = "";
		
		String[] entityBefore = ref.getEntityBefore().fullName().split("#");
		String[] entityAfter = ref.getEntityAfter().fullName().split("#");
		String nameClassBefore = entityBefore[0];
		String nameClassAfter = entityAfter[0];
		String nameMethodAfter = entityAfter[1];
		String nameMethodBefore = entityBefore[1];
		
		switch (category) {
			case FIELD_MOVE:
				description = this.move(nameMethodAfter, nameClassBefore, nameClassAfter);
				break;
	
			case FIELD_PULL_UP:
				description = "";
				break;
				
			case FIELD_PUSH_DOWN:
				description = "";
				break;
				
			default:
				description = "";
				break;
		}
		return description;
	}
	
	public String move(final String nameFieldAfter, final String nameClassBefore, final String nameClassAfter){
		return this.messageMoveTemplate("field", nameFieldAfter, nameClassBefore, nameClassAfter);
	}
	
	public String addition(final String nameField, final String nameClass){
		return this.messageAddition("field", nameField, nameClass);
	}
	
	public String deprecate(final String nameFieldAfter, final String nameClassAfter){
		return this.messageDeprecate("field", nameFieldAfter, nameClassAfter);
	}
	
	public String changeDefaultValue(final String nameField, final String nameClass){
		String message = "";
		message += "field <code>" + nameField + "<code>";
		message += "<br>changed default value";
		message += "<br>in <code>" + nameClass + "</code>";
		message += "<br>";
		return message;
	}
	
	public String returnType(final String nameField, final String nameClass){
		return super.messageReturnTypeTemplate("field", nameField, "type", nameClass);
	}
	
	public String visibility(final String nameField, final String nameClass, final String visibility1, final String visibility2){
		return super.messageVisibilityTemplate("field", nameField, "type", nameClass, visibility1, visibility2);
	}
	
	public String modifierFinal(final String nameFieldd, final String nameClass, final Boolean isGain){
		return this.messageFinalTemplate("field", nameFieldd, "type", nameClass, isGain);
	}
	
}
