package br.ufmg.dcc.labsoft.apidiff.detect.diff.description;

public class TypeDescription extends TemplateDescription {

	public String remove(final String nameClass, final String namePackage){
		return super.messageRemoveTemplate("class", nameClass, "package", namePackage);
	}
	
	public String visibility(final String nameClass, final String namePackage, final String visibility1, final String visibility2){
		return super.messageVisibilityTemplate("class", nameClass, "package", namePackage, visibility1, visibility2);
	}
	
	public String modifierStatic(final String nameClass, final String namePackage, final Boolean isGain){
		return this.messageStaticTemplate("class", nameClass, "package", namePackage, isGain);
	}
	
	public String modifierFinal(final String nameClass, final String namePackage, final Boolean isGain){
		return this.messageFinalTemplate("class", nameClass, "package", namePackage, isGain);
	}
	
	public String changeSuperClass(final String nameClassComplete, final String listSuperClass1, final String listSuperClass2){
		String message = "";
		message += "<b>Change Superclass</b>:";
		message += "<br>class <code>" + nameClassComplete + "</code>";
		message += "<br>changed the superclass list";
		message += "<br>from <code>" + listSuperClass1 +"</code>";
		message += "<br>to <code>" + listSuperClass2 +"</code>";
		message += "<br>";
		return message;
	}
	
	public String removeSuperClass(final String nameClassComplete, final String listSuperClassRemoved){
		String message = "";
		message += "<b>Remove Superclass</b>:";
		message += "<br>superclass <code>" + listSuperClassRemoved +"</code>";
		message += "<br>removed from class <code>" + nameClassComplete + "</code>";
		message += "<br>";
		return message;
	}
}
