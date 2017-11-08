package apidiff.visitor;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;

import apidiff.util.UtilTools;

public class AnnotationTypeDeclarationVisitor extends ASTVisitor{
	
	private ArrayList<AnnotationTypeDeclaration> acessibleAnnotation = new ArrayList<AnnotationTypeDeclaration>();
	private ArrayList<AnnotationTypeDeclaration> nonAcessibleAnnotation = new ArrayList<AnnotationTypeDeclaration>();

	public ArrayList<AnnotationTypeDeclaration> getAcessibleAnnotation() {
		return acessibleAnnotation;
	}

	public void setAcessibleAnnotation(ArrayList<AnnotationTypeDeclaration> acessibleAnnotation) {
		this.acessibleAnnotation = acessibleAnnotation;
	}


	public ArrayList<AnnotationTypeDeclaration> getNonAcessibleAnnotation() {
		return nonAcessibleAnnotation;
	}


	public void setNonAcessibleAnnotation(ArrayList<AnnotationTypeDeclaration> nonAcessibleAnnotation) {
		this.nonAcessibleAnnotation = nonAcessibleAnnotation;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		if(UtilTools.isVisibilityProtected(node) || UtilTools.isVisibilityPublic(node)){
			this.acessibleAnnotation.add(node);
		}
		else{
			this.nonAcessibleAnnotation.add(node);
		}
		return super.visit(node);
	}


}
