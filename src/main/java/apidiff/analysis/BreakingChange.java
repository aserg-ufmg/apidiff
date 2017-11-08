package apidiff.analysis;

/**
 * Classe para armazenar a saída
 * @author aline
 *
 */
public class BreakingChange {
	
	public BreakingChange(){
		
	}
	
	public BreakingChange(final String path, final String struture, final String category, final Boolean isBreakingChange, final String description){
		this.path = path;
		this.struture = struture;
		this.category = category;
		this.description = description;
		this.breakingChange = isBreakingChange;
	}
	
	public BreakingChange(final String path, final String struture, final String category, final Boolean isBreakingChange){
		this.path = path;
		this.struture = struture;
		this.category = category;
		this.breakingChange = isBreakingChange;
		this.description = "";
	}
	
	public BreakingChange(final String path, final String struture, final String category) {
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
	private String category;
	
	private Boolean breakingChange;
	
	private String description;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public BreakingChange path(String path) {
		this.path = path;
		return this;
	}

	public String getStruture() {
		return struture;
	}

	public void setStruture(String struture) {
		this.struture = struture;
	}
	
	public BreakingChange struture(String struture) {
		this.struture = struture;
		return this;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	public BreakingChange category(String category) {
		this.category = category;
		return this;
	}

	public Boolean isBreakingChange() {
		return breakingChange;
	}

	public void setBreakingChange(Boolean breakingChange) {
		this.breakingChange = breakingChange;
	}
	
	public BreakingChange isBreakingChange(Boolean breakingChange) {
		this.breakingChange = breakingChange;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public BreakingChange description(String description) {
		this.description = description;
		return this;
	}
	
}
