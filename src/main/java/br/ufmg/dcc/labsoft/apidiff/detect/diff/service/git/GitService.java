package br.ufmg.dcc.labsoft.apidiff.detect.diff.service.git;

import java.util.List;
import java.util.Map;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public interface GitService {

	/**
	 * Clona um repositório se ele não existe. Se existe, apenas lê as propriedades do repositório.
	 * @param projectPath	- Caminho do repositório.
	 * @param cloneUrl		- url GitHub.
	 * @return
	 * @throws Exception
	 */
	Repository openRepositoryAndCloneIfNotExists(String projectName, String cloneUrl) throws Exception;
	
	RevWalk fetchAndCreateNewRevsWalk(Repository repository, String branch) throws Exception;
	
	Map<ChangeType, List<GitFile>> fileTreeDiff(Repository repository, RevCommit commitNew) throws Exception;
	
	Integer countCommits(Repository repository, String branch) throws Exception ;
	
	void checkout(Repository repository, String commitId) throws Exception;
	
	RevCommit createRevCommitByCommitId(final Repository repository, final String commitId) throws Exception;
}
