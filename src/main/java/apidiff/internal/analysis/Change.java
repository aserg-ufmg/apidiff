package apidiff.internal.analysis;

import org.eclipse.jgit.revwalk.RevCommit;

import apidiff.enums.Category;

/**
 * Classe para armazenar a saída
 * @author aline
 *
 */
public class Change {
	
	public Change(){
		
	}
	
	public Change(final String path, final String struture, final Category category, final Boolean isBreakingChange, final String description){
		this.path = path;
		this.struture = struture;
		this.category = category;
		this.description = description;
		this.breakingChange = isBreakingChange;
	}
	
	public Change(final String path, final String struture, final Category category, final Boolean isBreakingChange){
		this.path = path;
		this.struture = struture;
		this.category = category;
		this.breakingChange = isBreakingChange;
		this.description = "";
	}
	
	public Change(final String path, final String struture, final Category category) {
		this.path = path;
		this.struture = struture;
		this.category = category;
	}
	
	/**
	 * Caminho completo da classe/elemento.
	 */
	private String path;
	
	/**
	 * Estrutura onde a breaking change foi detectada.
	 */
	private String struture;
	
	/**
	 * Categoria da breaking change. 
	 */
	private Category category;
	
	private Boolean breakingChange;
	
	private String description;
	
	private Boolean javadoc;
	
	private Boolean depreciated;
	
	private RevCommit revCommit;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public Change path(String path) {
		this.path = path;
		return this;
	}

	public String getStruture() {
		return struture;
	}

	public void setStruture(String struture) {
		this.struture = struture;
	}
	
	public Change struture(String struture) {
		this.struture = struture;
		return this;
	}

	public Boolean isBreakingChange() {
		return breakingChange;
	}

	public void setBreakingChange(Boolean breakingChange) {
		this.breakingChange = breakingChange;
	}
	
	public Change isBreakingChange(Boolean breakingChange) {
		this.breakingChange = breakingChange;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Change description(String description) {
		this.description = description;
		return this;
	}

	public Boolean getJavadoc() {
		return this.javadoc;
	}

	public void setJavadoc(Boolean javadoc) {
		this.javadoc = javadoc;
	}

	public Boolean getDepreciated() {
		return this.depreciated;
	}

	public void setDepreciated(Boolean depreciated) {
		this.depreciated = depreciated;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Boolean getBreakingChange() {
		return breakingChange;
	}

	public RevCommit getRevCommit() {
		return revCommit;
	}

	public void setRevCommit(RevCommit revCommit) {
		this.revCommit = revCommit;
	}
	
}
