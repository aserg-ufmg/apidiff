package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.labsoft.apidiff.UtilFile;
import br.ufmg.dcc.labsoft.apidiff.UtilTools;
import br.ufmg.dcc.labsoft.apidiff.detect.diff.service.git.GitFile;
import br.ufmg.dcc.labsoft.apidiff.detect.diff.service.git.GitService;
import br.ufmg.dcc.labsoft.apidiff.detect.diff.service.git.GitServiceImpl;
import br.ufmg.dcc.labsoft.apidiff.detect.parser.APIVersion;

public class APIDiff {
	

	private final String nameFile = "output.csv";
	
	private String nameProject;
	private String url;
	private APIVersion versionOld;
	private APIVersion versionNew;
	
	private Result resultType; 
	private Result resultFild;
	private Result resultMethod;
	private Result resultEnum;
	private Result resultEnumConstant;
	
	private Logger logger = LoggerFactory.getLogger(APIDiff.class);

	public APIDiff(final String nameProject, final String url) {
		this.url = url;
		this.nameProject = nameProject;
	}
	

	public void calculateDiff() {

		try {
			GitService service = new GitServiceImpl();
			File projectFolder = new File(UtilTools.getPathProjects() + "/" + this.nameProject);
			Repository repository = service.openRepositoryAndCloneIfNotExists(this.nameProject, this.url);
			RevWalk revWalk = service.fetchAndCreateNewRevsWalk(repository, null);
			
			//Itera sobre os commits.
			Iterator<RevCommit> i = revWalk.iterator();
			while(i.hasNext()){
				
				RevCommit currentCommit = i.next();
				
				//Busca a lista de arquivos modificados entre o commit atual e o commit pai.
				Map<ChangeType, List<GitFile>> mapModifications = service.fileTreeDiff(repository, currentCommit);
				
				//Informações do autor.
				PersonIdent personIdent = currentCommit.getAuthorIdent();
				
				//versao atual
				String commitId = currentCommit.getId().getName();
				service.checkout(repository, commitId);
				this.versionNew = new APIVersion(projectFolder, mapModifications);
				
				//versao antiga
				String parentCommit = currentCommit.getParent(0).getName();
				service.checkout(repository, parentCommit);
				this.versionOld = new APIVersion(projectFolder, mapModifications);
				
				this.logger.info("Processing Types ...");
				this.resultType = new TypeDiff().calculateDiff(this.versionOld, this.versionNew);
				
				this.logger.info("Processing Filds...");
				this.resultFild = new FieldDiff().calculateDiff(this.versionOld, this.versionNew);
				
				this.logger.info("Processing Methods...");
				this.resultMethod = new MethodDiff().calculateDiff(this.versionOld, this.versionNew);
				
				this.logger.info("Processing Method Enums...");
				this.resultEnum = new EnumDiff().calculateDiff(this.versionOld, this.versionNew);
				
				this.logger.info("Processing Method Enuns Constant...");
				this.resultEnumConstant = new EnumConstantDiff().calculateDiff(this.versionOld, this.versionNew);
				
				this.print(currentCommit);//Escreve saída em arquivo.
			}
		
		} catch (Exception e) {
			this.logger.error("Error in calculating commitn diff ", e);
		}
		
		System.out.println("Finished processing. Check the output file <" + this.nameFile + ">");
	}
	
	/**
	 * Imprime resultado em um arquivo CSV.
	 */
	private void print(final RevCommit currentCommit){
		List<String> result =  new ArrayList<String>();
		
		//Lista de Breaking Changes.
		//result.add("Author;E-mail;Library;ChangedType;StructureName;Category");
		result.addAll(this.printListBreakingChange(this.resultType, currentCommit));
		result.addAll(this.printListBreakingChange(this.resultFild, currentCommit));
		result.addAll(this.printListBreakingChange(this.resultMethod,currentCommit));
		result.addAll(this.printListBreakingChange(this.resultEnum,currentCommit));
		result.addAll(this.printListBreakingChange(this.resultEnumConstant, currentCommit));
		
		UtilFile.writeFile(this.nameFile, result);
	}
	
	/**
	 * Imprime lista de breaking change detectadas.
	 * @param r
	 */
	private List<String> printListBreakingChange(final Result r, final RevCommit currentCommit){
		PersonIdent personIdent = currentCommit.getAuthorIdent();
		List<String> list =  new ArrayList<String>();
		if(r != null){
			for(BreakingChange bc: r.getListBreakingChange()){
				list.add(personIdent.getName() + ";" + personIdent.getEmailAddress() + ";" + this.nameProject  + ";" + bc.getPath() + ";" + bc.getStruture() + ";" + bc.getCategory()
				+ currentCommit.getId().getName() + ";" +currentCommit.getFullMessage() + ";" +currentCommit.getFullMessage() + ";" + currentCommit.getCommitTime());
			}
		}
		return list;
	}
	
	private String printCount(final Result result, final String struture){
		if(result!=null){
			return struture + ";" + result.getElementAdd() + ";" + result.getElementRemoved() + ";" +result.getElementModified() + ";" +  result.getElementDeprecated();	
		}
		return "";
		
	}

}
