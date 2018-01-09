package apidiff;

import java.util.List;

import apidiff.analysis.Result;
import apidiff.enums.Classifier;

public interface DiffDetector {
	
	public Result detectChangeAtCommit(String commitId, Classifier classifier) throws Exception;;
	
	public Result detectChangeAllHistory(String branch, List<Classifier> classifiers) throws Exception;
	
	public Result detectChangeAllHistory(String branch, Classifier classifier) throws Exception;
	
	public Result detectChangeAllHistory(List<Classifier> classifiers) throws Exception;
	
	public Result detectChangeAllHistory(Classifier classifier) throws Exception;
	
	public Result fetchAndDetectChange(List<Classifier> classifiers) throws Exception;
	
	public Result fetchAndDetectChange(Classifier classifier) throws Exception;
	
}
