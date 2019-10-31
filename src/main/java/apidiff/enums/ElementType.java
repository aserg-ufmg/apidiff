package apidiff.enums;

public enum ElementType {

	ENUM("enum"),
	INTERFACE("interface"),
	CLASS("class"),
	METHOD("method"),
	CONSTRUCTOR("constructor");
	
	private String type;
	
	private ElementType(final String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
}
