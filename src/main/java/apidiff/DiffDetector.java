package apidiff;

import java.util.List;

import apidiff.enums.Classifier;

public interface DiffDetector {
	
	/**
	 * Analyzing changes performed in specific commit
	 * @param commitId - SHA key
	 * @param classifier - Classifier for packages
	 * @return Result - Detected changes
	 * @throws Exception - Exception during process
	 */
	public Result detectChangeAtCommit(String commitId, Classifier classifier) throws Exception;
	
	/**
	 * Analyzing changes performed in several commits
	 * @param branch - branch name (i.e., "master")
	 * @param classifiers - Classifier for packages
	 * @return Result - Detected changes
	 * @throws Exception - Exception during process
	 */
	public Result detectChangeAllHistory(String branch, List<Classifier> classifiers) throws Exception;
	
	/**
	 * Analyzing changes performed in several commits
	 * @param branch - branch name (i.e., "master")
	 * @param classifier - Classifier for packages
	 * @return - Detected changes
	 * @throws Exception - Exception during process
	 */
	public Result detectChangeAllHistory(String branch, Classifier classifier) throws Exception;
	
	/**
	 * AAnalyzing changes performed in several commits
	 * @param classifiers
	 * @return - Detected changes
	 * @throws Exception - Exception during process
	 */
	public Result detectChangeAllHistory(List<Classifier> classifiers) throws Exception;
	
	/**
	 * Analyzing changes performed in several commits
	 * @param classifier - Classifier for packages
	 * @return Result - Detected changes
	 * @throws Exception
	 */
	public Result detectChangeAllHistory(Classifier classifier) throws Exception;
	
	/**
	 * Fetching new commits from a repository
	 * @param classifiers - Classifier for packages
	 * @return Result - Detected changes
	 * @throws Exception - Exception during process
	 */
	public Result fetchAndDetectChange(List<Classifier> classifiers) throws Exception;
	
	/**
	 * Fetching new commits from a repository
	 * @param classifier - Classifier for packages
	 * @return Result - Detected changes
	 * @throws Exception - Exception during process
	 */
	public Result fetchAndDetectChange(Classifier classifier) throws Exception;
	
}
