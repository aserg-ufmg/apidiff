package br.ufmg.dcc.labsoft.apidiff.detect.parser;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import br.ufmg.dcc.labsoft.apidiff.UtilTools;

public class TypeDeclarationVisitor extends ASTVisitor{
	private ArrayList<TypeDeclaration> acessibleTypes = new ArrayList<TypeDeclaration>();
	private ArrayList<TypeDeclaration> nonAcessibleTypes = new ArrayList<TypeDeclaration>();

	public ArrayList<TypeDeclaration> getAcessibleTypes() {
		return this.acessibleTypes;
	}

	public ArrayList<TypeDeclaration> getNonAcessibleTypes() {
		return this.nonAcessibleTypes;
	}

	public void addAcessibleType(TypeDeclaration type) {
		this.acessibleTypes.add(type);
	}

	public void addNonAcessibleTypes(TypeDeclaration type) {
		this.nonAcessibleTypes.add(type);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if(UtilTools.isInterfaceStable(node)){
			if(UtilTools.isPrivate(node)){
				this.addNonAcessibleTypes(node);
			} else {
				this.addAcessibleType(node);
			}
		}

		return super.visit(node);
	}


}
