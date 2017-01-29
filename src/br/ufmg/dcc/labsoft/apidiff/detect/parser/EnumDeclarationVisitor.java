package br.ufmg.dcc.labsoft.apidiff.detect.parser;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnumDeclaration;

import br.ufmg.dcc.labsoft.apidiff.Utils;

public class EnumDeclarationVisitor extends ASTVisitor{
	private ArrayList<EnumDeclaration> acessibleEnums = new ArrayList<EnumDeclaration>();
	private ArrayList<EnumDeclaration> nonAcessibleEnums = new ArrayList<EnumDeclaration>();

	public ArrayList<EnumDeclaration> getAcessibleEnums() {
		return acessibleEnums;
	}

	public ArrayList<EnumDeclaration> getNonAcessibleEnums() {
		return nonAcessibleEnums;
	}

	private boolean isConsiderable(EnumDeclaration node){
		if (node.resolveBinding().getQualifiedName() != null){
			boolean notTestFilterLower = !node.resolveBinding().getQualifiedName().contains("test");
			boolean notTestFilterUpper = !node.resolveBinding().getQualifiedName().contains("Test");
			boolean notExampleFilterLower = !node.resolveBinding().getQualifiedName().contains("example");
			boolean notExampleFilterUpper = !node.resolveBinding().getQualifiedName().contains("Example");

			return notExampleFilterLower && notExampleFilterUpper && notTestFilterLower && notTestFilterUpper;
		}
		
		return false;
	}

	@Override
	public boolean visit(EnumDeclaration node){
		if(isConsiderable(node)){
			if(Utils.isPrivate(node)){
				this.nonAcessibleEnums.add(node);
			} else {
				this.acessibleEnums.add(node);
			}
		}
		return super.visit(node);
	}

}
