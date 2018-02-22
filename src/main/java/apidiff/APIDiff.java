package apidiff;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apidiff.enums.Classifier;
import apidiff.internal.analysis.DiffProcessor;
import apidiff.internal.analysis.DiffProcessorImpl;
import apidiff.internal.analysis.Result;
import apidiff.internal.service.git.GitFile;
import apidiff.internal.service.git.GitService;
import apidiff.internal.service.git.GitServiceImpl;
import apidiff.internal.visitor.APIVersion;
import apidiff.util.UtilTools;

public class APIDiff implements DiffDetector{
	
	private String nameProject;
	
	private String path;
	
	private String url;
	
	private Logger logger = LoggerFactory.getLogger(APIDiff.class);

	public APIDiff(final String nameProject, final String url) {
		this.url = url;
		this.nameProject = nameProject;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public Result detectChangeAtCommit(String commitId, Classifier classifierAPI) {
		Result result = new Result();
		try {
			GitService service = new GitServiceImpl();
			Repository repository = service.openRepositoryAndCloneIfNotExists(this.path, this.nameProject, this.url);
			RevCommit commit = service.createRevCommitByCommitId(repository, commitId);
			Result resultByClassifier = this.diffCommit(commit, repository, this.nameProject, classifierAPI);
			result.getChangeType().addAll(resultByClassifier.getChangeType());
			result.getChangeMethod().addAll(resultByClassifier.getChangeMethod());
			result.getChangeField().addAll(resultByClassifier.getChangeField());
		} catch (Exception e) {
			this.logger.error("Error in calculating commitn diff ", e);
		}
		this.logger.info("Finished processing.");
		return result;
	}
	
	@Override
	public Result detectChangeAllHistory(String branch, List<Classifier> classifiers) throws Exception {
		Result result = new Result();
		GitService service = new GitServiceImpl();
		Repository repository = service.openRepositoryAndCloneIfNotExists(this.path, this.nameProject, this.url);
		RevWalk revWalk = service.createAllRevsWalk(repository, branch);
		//Itera sobre os commits.
		Iterator<RevCommit> i = revWalk.iterator();
		while(i.hasNext()){
			RevCommit currentCommit = i.next();
			for(Classifier classifierAPI: classifiers){
				Result resultByClassifier = this.diffCommit(currentCommit, repository, this.nameProject, classifierAPI);
				result.getChangeType().addAll(resultByClassifier.getChangeType());
				result.getChangeMethod().addAll(resultByClassifier.getChangeMethod());
				result.getChangeField().addAll(resultByClassifier.getChangeField());
			}
		}
		this.logger.info("Finished processing.");
		return result;
	}
	
	@Override
	public Result detectChangeAllHistory(List<Classifier> classifiers) throws Exception {
		return this.detectChangeAllHistory(null, classifiers);
	}
	
	@Override
	public Result fetchAndDetectChange(List<Classifier> classifiers) {
		Result result = new Result();
		try {
			GitService service = new GitServiceImpl();
			Repository repository = service.openRepositoryAndCloneIfNotExists(this.path, this.nameProject, this.url);
			RevWalk revWalk = service.fetchAndCreateNewRevsWalk(repository, null);
			//Itera sobre os commits.
			Iterator<RevCommit> i = revWalk.iterator();
			while(i.hasNext()){
				RevCommit currentCommit = i.next();
				for(Classifier classifierAPI : classifiers){
					Result resultByClassifier = this.diffCommit(currentCommit, repository, this.nameProject, classifierAPI);
					result.getChangeType().addAll(resultByClassifier.getChangeType());
					result.getChangeMethod().addAll(resultByClassifier.getChangeMethod());
					result.getChangeField().addAll(resultByClassifier.getChangeField());
				}
			}
		} catch (Exception e) {
			this.logger.error("Error in calculating commit diff ", e);
		}

		this.logger.info("Finished processing.");
		return result;
	}
	
	@Override
	public Result detectChangeAllHistory(String branch, Classifier classifier) throws Exception {
		return this.detectChangeAllHistory(branch, Arrays.asList(classifier));
	}

	@Override
	public Result detectChangeAllHistory(Classifier classifier) throws Exception {
		return this.detectChangeAllHistory(Arrays.asList(classifier));
	}

	@Override
	public Result fetchAndDetectChange(Classifier classifier) throws Exception {
		return this.fetchAndDetectChange(Arrays.asList(classifier));
	}
	
	private Result diffCommit(final RevCommit currentCommit, final Repository repository, String nameProject, Classifier classifierAPI) throws Exception{
		File projectFolder = new File(UtilTools.getPathProject(this.path, nameProject));
		if(currentCommit.getParentCount() != 0){//Se commit tem pelo menos um pai.
			try {
				APIVersion version1 = this.getAPIVersionByCommit(currentCommit.getParent(0).getName(), projectFolder, repository, currentCommit, classifierAPI);//versao antiga
				APIVersion version2 = this.getAPIVersionByCommit(currentCommit.getId().getName(), projectFolder, repository, currentCommit, classifierAPI); //versao atual
				DiffProcessor diff = new DiffProcessorImpl();
				return diff.detectChange(version1, version2, repository, currentCommit);
			} catch (Exception e) {
				this.logger.error("Ocorreu um errro durante o checkout commit=[" + currentCommit + "]");
			}
		}
		return new Result();
	}
	
	private APIVersion getAPIVersionByCommit(String commit, File projectFolder, Repository repository, RevCommit currentCommit, Classifier classifierAPI) throws Exception{
		
		GitService service = new GitServiceImpl();
		
		//Busca a lista de arquivos modificados entre o commit atual e o commit pai.
		Map<ChangeType, List<GitFile>> mapModifications = service.fileTreeDiff(repository, currentCommit);
		
		service.checkout(repository, commit);
		return new APIVersion(this.path, projectFolder, mapModifications, classifierAPI);
	}

}
