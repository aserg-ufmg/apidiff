package apidiff.internal.refactor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import refdiff.core.RefDiff;
import refdiff.core.api.RefactoringType;
import refdiff.core.rm2.model.refactoring.SDRefactoring;

public class RefactoringProcessorImpl implements RefactorProcessor {
	
	private Logger logger = LoggerFactory.getLogger(RefactoringProcessorImpl.class);
	
	private Map<RefactoringType, List<SDRefactoring>> format(final List<SDRefactoring> refactorings){
		Map<RefactoringType, List<SDRefactoring>> result = new HashMap<RefactoringType, List<SDRefactoring>>();
		for(SDRefactoring ref : refactorings){
			RefactoringType refactoringName =  ref.getRefactoringType();
			if(result.containsKey(refactoringName)){
				result.get(refactoringName).add(ref);
			}
			else{
				List<SDRefactoring> listRefactorings = new ArrayList<SDRefactoring>();
				listRefactorings.add(ref);
				result.put(refactoringName, listRefactorings);
			}
		}
		return result;
	}
	
	@Override
	public Map<RefactoringType, List<SDRefactoring>> detectRefactoringAtCommit(final Repository repository, final String commit){
		Map<RefactoringType, List<SDRefactoring>> result = new HashMap<RefactoringType, List<SDRefactoring>>();
		try{
			RefDiff refDiff = new RefDiff();
			result = this.format(refDiff.detectAtCommit(repository, commit));
		} catch (Exception e) {
			this.logger.error("Erro in refactoring process [repository=" + repository + "][commit=" + commit + "]", e);
		}
		return result;
	}

}
