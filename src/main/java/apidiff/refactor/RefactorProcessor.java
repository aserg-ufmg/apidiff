package apidiff.refactor;

import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.Repository;

import refdiff.core.api.RefactoringType;
import refdiff.core.rm2.model.refactoring.SDRefactoring;

public interface RefactorProcessor {
	
	public Map<RefactoringType, List<SDRefactoring>> detectRefactoringAtCommit (final Repository repository, final String commit);

}
