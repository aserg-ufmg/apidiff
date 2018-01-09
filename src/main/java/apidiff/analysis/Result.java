package apidiff.analysis;

import java.util.List;
import java.util.ArrayList;

public class Result {

	private List<Change> changeType = new ArrayList<Change>();
	
	private List<Change> changeMethod = new ArrayList<Change>();
	
	private List<Change> changeField = new ArrayList<Change>();

	public List<Change> getChangeType() {
		return changeType;
	}

	public void setChangeType(List<Change> changeType) {
		this.changeType = changeType;
	}

	public List<Change> getChangeMethod() {
		return changeMethod;
	}

	public void setChangeMethod(List<Change> changeMethod) {
		this.changeMethod = changeMethod;
	}

	public List<Change> getChangeField() {
		return changeField;
	}

	public void setChangeField(List<Change> changeField) {
		this.changeField = changeField;
	}
	
}
