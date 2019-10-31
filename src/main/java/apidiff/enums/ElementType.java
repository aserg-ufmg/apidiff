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

	@Override
	public String toString() {
		return type;
	}
	
}
