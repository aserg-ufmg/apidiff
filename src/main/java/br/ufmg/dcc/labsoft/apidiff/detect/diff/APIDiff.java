package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import br.ufmg.dcc.labsoft.apidiff.enums.ClassifierAPI;

public class APIDiff {
	
	private final String nameFile = "output.csv";
	
	private String nameProject;
	private String url;
	
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
			Repository repository = service.openRepositoryAndCloneIfNotExists(this.nameProject, this.url);
			RevWalk revWalk = service.fetchAndCreateNewRevsWalk(repository, null);
			
			//Itera sobre os commits.
			Iterator<RevCommit> i = revWalk.iterator();
			while(i.hasNext()){
				RevCommit currentCommit = i.next();
				this.diffCommit(currentCommit, repository, this.nameProject, ClassifierAPI.API);
				this.diffCommit(currentCommit, repository, this.nameProject, ClassifierAPI.NON_API_EXAMPLE);
				this.diffCommit(currentCommit, repository, this.nameProject, ClassifierAPI.NON_API_EXPERIMENTAL);
				this.diffCommit(currentCommit, repository, this.nameProject, ClassifierAPI.NON_API_INTERNAL);
				this.diffCommit(currentCommit, repository, this.nameProject, ClassifierAPI.NON_API_TEST);
			}
		
		} catch (Exception e) {
			this.logger.error("Error in calculating commitn diff ", e);
		}
		this.logger.info("Finished processing. Check the output file <" + this.nameFile + ">");
	}
	
	/**
	 * Calcula um diff entre um commit e a versão anterior dos seus arquivos.
	 * @param currentCommit
	 * @param repository
	 * @param nameProject
	 * @throws Exception
	 */
	private void diffCommit(final RevCommit currentCommit, final Repository repository, String nameProject, ClassifierAPI classifierAPI) throws Exception{
		
		File projectFolder = new File(UtilTools.getPathProjects() + "/" + nameProject);
		APIVersion versionNew = this.getAPIVersionByCommit(currentCommit.getId().getName(), projectFolder, repository, currentCommit, classifierAPI); //versao atual
		APIVersion versionOld = this.getAPIVersionByCommit(currentCommit.getParent(0).getName(), projectFolder, repository, currentCommit,classifierAPI);////versao antiga
		this.diff(versionOld, versionNew);
		this.print(currentCommit, classifierAPI);//Escreve saída em arquivo.
	}
	
	/**
	 * Retorna uma APIVersion conforme o commit corrente.
	 * @param commit - is do commit de referencia
	 * @param projectFolder - File do projeto analisado
	 * @param repository
	 * @param currentCommit -- commit de referencia no grafo.
	 * @return
	 * @throws Exception
	 */
	private APIVersion getAPIVersionByCommit(String commit, File projectFolder, Repository repository, RevCommit currentCommit, ClassifierAPI classifierAPI) throws Exception{
		
		GitService service = new GitServiceImpl();
		
		//Busca a lista de arquivos modificados entre o commit atual e o commit pai.
		Map<ChangeType, List<GitFile>> mapModifications = service.fileTreeDiff(repository, currentCommit);
		
		service.checkout(repository, commit);
		return new APIVersion(projectFolder, mapModifications, classifierAPI);
	}
	
	/**
	 * Procura breaking changes e non-breaking changes entre duas versões do código-fonte.
	 * @param version1
	 * @param version2
	 */
	private void diff(APIVersion version1, APIVersion version2){
		this.resultType = new TypeDiff().calculateDiff(version1, version2);
		this.resultFild = new FieldDiff().calculateDiff(version1, version2);
		this.resultMethod = new MethodDiff().calculateDiff(version1, version2);
		this.resultEnum = new EnumDiff().calculateDiff(version1, version2);
		this.resultEnumConstant = new EnumConstantDiff().calculateDiff(version1, version2);
	}
	
	/**
	 * Imprime resultado em um arquivo CSV.
	 */
	private void print(final RevCommit currentCommit, ClassifierAPI classifierAPI){
		List<String> result =  new ArrayList<String>();
		//Lista de Breaking Changes.
		Date date = new Date();
		result.addAll(this.printListBreakingChange(this.resultType, currentCommit, date, classifierAPI));
		result.addAll(this.printListBreakingChange(this.resultFild, currentCommit, date, classifierAPI));
		result.addAll(this.printListBreakingChange(this.resultMethod,currentCommit, date, classifierAPI));
		result.addAll(this.printListBreakingChange(this.resultEnum,currentCommit, date, classifierAPI));
		result.addAll(this.printListBreakingChange(this.resultEnumConstant, currentCommit, date, classifierAPI));
		
		UtilFile.writeFile(this.nameFile, result);
	}
	
	/**
	 * Imprime lista de breaking change detectadas.
	 * @param r
	 */
	private List<String> printListBreakingChange(final Result r, final RevCommit currentCommit, Date date, ClassifierAPI classifierAPI){
		
		//name developer; e-mail; project; path; struture; category; id commit; message commit; timestamp commit (milliseconds); timestamp process (milliseconds); formatted date process; is breaking change (boolean)
		PersonIdent personIdent = currentCommit.getAuthorIdent();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		List<String> list =  new ArrayList<String>();
		if(r != null){
			for(BreakingChange bc: r.getListBreakingChange()){
				list.add(personIdent.getName() + ";" + personIdent.getEmailAddress() + ";" + this.nameProject  + ";" + bc.getPath() + ";" + bc.getStruture() + ";" + bc.getCategory()
				+ ";" + currentCommit.getId().getName() + ";" + this.formatMessage(currentCommit.getFullMessage()) + ";" + currentCommit.getCommitTime() + "000" + ";" + date.getTime() + ";" + sdf.format(date) +  ";" + bc.isBreakingChange()
				+ ";" + classifierAPI);
			}
		}
		return list;
	}
	
	private String formatMessage(String message){
		String messageFormat = message.replaceAll("\\r?\\n", "--NEW_LINE_APIDIFF_PARSER--");
		messageFormat = messageFormat.replace(";", "--SEMICOLON_APIDIFF_PARSER--");
		return messageFormat;
	}

}
