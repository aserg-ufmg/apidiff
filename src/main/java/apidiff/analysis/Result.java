package apidiff.analysis;

import java.util.List;
import java.util.ArrayList;

/**
 * Classe para armazenar informações de breaking change e non-breaking change encontradas.
 * @author aline
 *
 */
public class Result {

	//Lista de Breaking Change encontradas.
	private List<BreakingChange> listBreakingChange = new ArrayList<BreakingChange>();
	
	//Quantidade de Breaking Change encontradas.
	private int breakingChange;
	
	//Quantidade de non-Breaking Change encontradas.
	private int nonBreakingChange;
	
	private int elementAdd;
	
	private int elementRemoved;
	
	private int elementModified;
	
	private int elementDeprecated;
	
	private String library;

	public List<BreakingChange> getListBreakingChange() {
		return listBreakingChange;
	}

	public void setListBreakingChange(List<BreakingChange> listBreakingChange) {
		this.listBreakingChange = listBreakingChange;
	}

	public int getBreakingChange() {
		return breakingChange;
	}

	public void setBreakingChange(int breakingChange) {
		this.breakingChange = breakingChange;
	}

	public int getNonBreakingChange() {
		return nonBreakingChange;
	}

	public void setNonBreakingChange(int nonBreakingChange) {
		this.nonBreakingChange = nonBreakingChange;
	}

	public int getElementAdd() {
		return elementAdd;
	}

	public void setElementAdd(int elementAdd) {
		this.elementAdd = elementAdd;
	}

	public int getElementRemoved() {
		return elementRemoved;
	}

	public void setElementRemoved(int elementRemoved) {
		this.elementRemoved = elementRemoved;
	}

	public int getElementModified() {
		return elementModified;
	}

	public void setElementModified(int elementModified) {
		this.elementModified = elementModified;
	}

	public int getElementDeprecated() {
		return elementDeprecated;
	}

	public void setElementDeprecated(int elementDeprecated) {
		this.elementDeprecated = elementDeprecated;
	}

	public String getLibrary() {
		return library;
	}

	public void setLibrary(String library) {
		this.library = library;
	}
}
