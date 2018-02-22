package apidiff;

import java.util.List;

import apidiff.enums.Classifier;

public interface DiffDetector {
	
	/**
	 * Analyzing changes performed in specific commit.
	 * @param commitId - SHA key
	 * @param classifier
	 * @return Result
	 * @throws Exception
	 */
	public Result detectChangeAtCommit(String commitId, Classifier classifier) throws Exception;
	
	/**
	 * Analyzing changes performed in whole history.
	 * @param branch - branch name (i.e., "master")
	 * @param classifiers
	 * @return Result
	 * @throws Exception
	 */
	public Result detectChangeAllHistory(String branch, List<Classifier> classifiers) throws Exception;
	
	/**
	 * Analyzing changes performed in whole history.
	 * @param branch - branch name (i.e., "master")
	 * @param classifier
	 * @return
	 * @throws Exception
	 */
	public Result detectChangeAllHistory(String branch, Classifier classifier) throws Exception;
	
	/**
	 * Analyzing changes performed in whole history.
	 * @param classifiers
	 * @return
	 * @throws Exception
	 */
	public Result detectChangeAllHistory(List<Classifier> classifiers) throws Exception;
	
	/**
	 * Analyzing changes performed in whole history.
	 * @param classifier
	 * @return Result
	 * @throws Exception
	 */
	public Result detectChangeAllHistory(Classifier classifier) throws Exception;
	
	/**
	 * Analyzing changes performed in new commits.
	 * @param classifiers
	 * @return Result
	 * @throws Exception
	 */
	public Result fetchAndDetectChange(List<Classifier> classifiers) throws Exception;
	
	/**
	 * Analyzing changes performed in new commits.
	 * @param classifier
	 * @return Result
	 * @throws Exception
	 */
	public Result fetchAndDetectChange(Classifier classifier) throws Exception;
	
}
