package br.ufmg.dcc.labsoft.apidiff.detect.diff.service.git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.labsoft.apidiff.Main;

public class GitServiceImpl implements GitService {
	
	private static final String REMOTE_REFS_PREFIX = "refs/remotes/origin/";
	
	private DefaultCommitsFilter commitsFilter = new DefaultCommitsFilter();
	
	private Logger logger = LoggerFactory.getLogger(GitServiceImpl.class);
	
	private String path;
	
	public GitServiceImpl() {
		Properties prop = new Properties();
    	InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties");
    	try {
    		prop.load(input);
    		this.path = prop.getProperty("PATH_PROJECT");
		} catch (IOException e) {
			this.logger.error("Path project not found, check the properties file.");
		}
	}
	
	private class DefaultCommitsFilter extends RevFilter {
		@Override
		public final boolean include(final RevWalk walker, final RevCommit c) {
			return c.getParentCount() == 1 && !isCommitAnalyzed(c.getName());
		}

		@Override
		public final RevFilter clone() {
			return this;
		}

		@Override
		public final boolean requiresCommitBody() {
			return false;
		}

		@Override
		public String toString() {
			return "RegularCommitsFilter";
		}
	}
	
	public boolean isCommitAnalyzed(String sha1) {
		return false;
	}
	
	@Override
	public Repository openRepositoryAndCloneIfNotExists(String projectName, String cloneUrl) throws Exception {
		File folder = new File(this.path + "/" + projectName);
		Repository repository = null;
		//Se repositório existe, carrega as propriedades.
		if (folder.exists()) {
			this.logger.info(projectName + " exists. Reading properties ... (wait)");
			RepositoryBuilder builder = new RepositoryBuilder();
			repository = builder
					.setGitDir(new File(folder, ".git"))
					.readEnvironment()
					.findGitDir()
					.build();
			
		} else {
			this.logger.info("Cloning " + cloneUrl  + " in " + cloneUrl + " ... (wait)");
			Git git = Git.cloneRepository()
					.setDirectory(folder)
					.setURI(cloneUrl)
					.setCloneAllBranches(true)
					.call();
			repository = git.getRepository();
		}
		this.logger.info("Process " + projectName  + " finish.");
		return repository;
	}
	
	@Override
	public RevWalk fetchAndCreateNewRevsWalk(Repository repository, String branch) throws Exception {
		List<ObjectId> currentRemoteRefs = new ArrayList<ObjectId>(); 
		for (Ref ref : repository.getAllRefs().values()) {
			String refName = ref.getName();
			if (refName.startsWith(REMOTE_REFS_PREFIX)) {
				currentRemoteRefs.add(ref.getObjectId());
			}
		}
		
		List<TrackingRefUpdate> newRemoteRefs = this.fetch(repository);
		
		RevWalk walk = new RevWalk(repository);
		for (TrackingRefUpdate newRef : newRemoteRefs) {
			if (branch == null || newRef.getLocalName().endsWith("/" + branch)) {
				walk.markStart(walk.parseCommit(newRef.getNewObjectId()));
			}
		}
		for (ObjectId oldRef : currentRemoteRefs) {
			walk.markUninteresting(walk.parseCommit(oldRef));
		}
		walk.setRevFilter(commitsFilter);
		return walk;
	}
	
	private List<TrackingRefUpdate> fetch(Repository repository) throws Exception {
		this.logger.info("Fetching changes of repository " + repository.getDirectory().toString());
        try (Git git = new Git(repository)) {
    		FetchResult result = git.fetch().call();
    		
    		Collection<TrackingRefUpdate> updates = result.getTrackingRefUpdates();
    		List<TrackingRefUpdate> remoteRefsChanges = new ArrayList<TrackingRefUpdate>();
    		for (TrackingRefUpdate update : updates) {
    			String refName = update.getLocalName();
    			if (refName.startsWith(REMOTE_REFS_PREFIX)) {
    				ObjectId newObjectId = update.getNewObjectId();
    				this.logger.info(refName +" is now at " + newObjectId.getName());
    				remoteRefsChanges.add(update);
    			}
    		}
    		if (updates.isEmpty()) {
    			this.logger.info("Nothing changed");
    		}
    		return remoteRefsChanges;
        }
	}
	
	@Override
	public Integer countCommits(Repository repository, String branch) throws Exception {
		RevWalk walk = new RevWalk(repository);
		try {
			Ref ref = repository.getRef(REMOTE_REFS_PREFIX + branch);
			ObjectId objectId = ref.getObjectId();
			RevCommit start = walk.parseCommit(objectId);
			walk.setRevFilter(RevFilter.NO_MERGES);
			return RevWalkUtils.count(walk, start, null);
		} finally {
			walk.dispose();
		}
	}
	
	@Override
	public Map<ChangeType, List<GitFile>> fileTreeDiff(Repository repository, RevCommit commitNew) throws Exception {
	       
		ObjectId headOld = commitNew.getParent(0).getTree();
       
		ObjectId headNew = commitNew.getTree();

        // prepare the two iterators to compute the diff between
		ObjectReader reader = repository.newObjectReader();
		
		CanonicalTreeParser treeRepositoryOld = new CanonicalTreeParser();
		treeRepositoryOld.reset(reader, headOld);
		
		CanonicalTreeParser treeRepositoryNew = new CanonicalTreeParser();
		treeRepositoryNew.reset(reader, headNew);
		
		// finally get the list of changed files
		List<DiffEntry> diffs = new Git(repository).diff()
		                    .setNewTree(treeRepositoryNew)
		                    .setOldTree(treeRepositoryOld)
		                    .setShowNameAndStatusOnly(true)
		                    .call();
		Map<ChangeType, List<GitFile>> mapDiff = new HashMap<ChangeType, List<GitFile>>();
		mapDiff.put(ChangeType.ADD, new ArrayList<>());
		mapDiff.put(ChangeType.COPY, new ArrayList<>());
		mapDiff.put(ChangeType.DELETE, new ArrayList<>());
		mapDiff.put(ChangeType.MODIFY, new ArrayList<>());
		mapDiff.put(ChangeType.RENAME, new ArrayList<>());
		
        for (DiffEntry entry : diffs) {
        	//TODO: Remover interfaces instáveis da árvore (internas, testes, experimentais).
        	if(this.isJavafile(entry.getOldPath()) || this.isJavafile(entry.getNewPath())) {
        		GitFile file = new GitFile(entry.getOldPath(), entry.getNewPath(), entry.getChangeType());
        		mapDiff.get(entry.getChangeType()).add(file);
        	}
        }
        return mapDiff;
	}
	
	
	private boolean isJavafile(String path) {
		return path != null && path.endsWith(".java");
	}
	
	@Override
	public void checkout(Repository repository, String commitId) throws Exception {
	    this.logger.info("Checking out {} {} ...", repository.getDirectory().getParent().toString(), commitId);
	    try (Git git = new Git(repository)) {
	        CheckoutCommand checkout = git.checkout().setName(commitId);
	        checkout.call();
	    }
	}
}
