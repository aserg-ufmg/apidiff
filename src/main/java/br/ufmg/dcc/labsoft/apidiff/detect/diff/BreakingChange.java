package br.ufmg.dcc.labsoft.apidiff.detect.diff;

/**
 * Classe para armazenar a saída
 * @author aline
 *
 */
public class BreakingChange {
	
	public BreakingChange(final String path, final String struture, final String category, final Boolean isBreakingChange) {
		this.path = path;
		this.struture = struture;
		this.category = category;
		this.breakingChange = isBreakingChange;
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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getStruture() {
		return struture;
	}

	public void setStruture(String struture) {
		this.struture = struture;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Boolean isBreakingChange() {
		return breakingChange;
	}

	public void setBreakingChange(Boolean breakingChange) {
		this.breakingChange = breakingChange;
	}
	
}
