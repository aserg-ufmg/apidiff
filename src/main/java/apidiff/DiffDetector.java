package apidiff;

import java.util.List;

import apidiff.enums.Classifier;

public interface DiffDetector {
	
	/**
	 * Analyzing changes performed in specific commit
	 * @param commitId - SHA key
	 * @param classifier
	 * @return Result
	 * @throws Exception
	 */
	public Result detectChangeAtCommit(String commitId, Classifier classifier) throws Exception;
	
	/**
	 * Analyzing changes performed in several commits
	 * @param branch - branch name (i.e., "master")
	 * @param classifiers
	 * @return Result
	 * @throws Exception
	 */
	public Result detectChangeAllHistory(String branch, List<Classifier> classifiers) throws Exception;
	
	/**
	 * Analyzing changes performed in several commits
	 * @param branch - branch name (i.e., "master")
	 * @param classifier
	 * @return
	 * @throws Exception
	 */
	public Result detectChangeAllHistory(String branch, Classifier classifier) throws Exception;
	
	/**
	 * AAnalyzing changes performed in several commits
	 * @param classifiers
	 * @return
	 * @throws Exception
	 */
	public Result detectChangeAllHistory(List<Classifier> classifiers) throws Exception;
	
	/**
	 * Analyzing changes performed in several commits
	 * @param classifier
	 * @return Result
	 * @throws Exception
	 */
	public Result detectChangeAllHistory(Classifier classifier) throws Exception;
	
	/**
	 * Fetching new commits from a repository
	 * @param classifiers
	 * @return Result
	 * @throws Exception
	 */
	public Result fetchAndDetectChange(List<Classifier> classifiers) throws Exception;
	
	/**
	 * Fetching new commits from a repository
	 * @param classifier
	 * @return Result
	 * @throws Exception
	 */
	public Result fetchAndDetectChange(Classifier classifier) throws Exception;
	
}
