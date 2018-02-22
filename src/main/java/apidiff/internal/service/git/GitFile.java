package apidiff.internal.service.git;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;

public class GitFile {
	
	private String pathOld;
	
	private String pathNew;

	private ChangeType chageType;
	
	public GitFile(final String pathOld, final String pathNew, final ChangeType chageType) {
		this.pathNew = pathNew;
		this.pathOld = pathOld;
		this.chageType = chageType;
	}

	public String getPathOld() {
		return pathOld;
	}

	public void setPathOld(String pathOld) {
		this.pathOld = pathOld;
	}

	public String getPathNew() {
		return pathNew;
	}

	public void setPathNew(String pathNew) {
		this.pathNew = pathNew;
	}

	public ChangeType getChageType() {
		return chageType;
	}

	public void setChageType(ChangeType chageType) {
		this.chageType = chageType;
	}
	
	public void print(){
		System.out.println("["+this.chageType+"]["+this.pathOld+"]["+this.pathNew+"]");
	}
	
	
}

