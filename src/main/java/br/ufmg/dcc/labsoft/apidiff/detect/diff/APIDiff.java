package br.ufmg.dcc.labsoft.apidiff.detect.diff;

import java.io.File;
import java.io.IOException;
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
	private Result resultAnnotationType; 
	private Result resultAnnotationTypeMember; 
	private Result resultFild;
	private Result resultMethod;
	private Result resultEnum;
	private Result resultEnumConstant;
	
	private Logger logger = LoggerFactory.getLogger(APIDiff.class);

	
	public String getNameProject() {
		return nameProject;
	}

	public void setNameProject(String nameProject) {
		this.nameProject = nameProject;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public APIDiff(){}
	
	public APIDiff(final String nameProject, final String url) {
		this.url = url;
		this.nameProject = nameProject;
	}
	
	public APIDiff(final String nameProject) {
		this.nameProject = nameProject;
	}

	/**
	 * Detecta breaking changes nos novos commits do projeto.
	 */
	public void calculateDiffCommit() {
		try {
			GitService service = new GitServiceImpl();
			Repository repository = service.openRepositoryAndCloneIfNotExists(this.nameProject, this.url);
			RevWalk revWalk = service.fetchAndCreateNewRevsWalk(repository, null);
			
			//Itera sobre os commits.
			Iterator<RevCommit> i = revWalk.iterator();
			while(i.hasNext()){
				RevCommit currentCommit = i.next();
				this.diffCommit(currentCommit, repository, this.nameProject, ClassifierAPI.API);
				this.diffCommit(currentCommit, repository, this.nameProject, ClassifierAPI.NON_API_EXPERIMENTAL);
				this.diffCommit(currentCommit, repository, this.nameProject, ClassifierAPI.NON_API_INTERNAL);
			}
		
		} catch (Exception e) {
			this.logger.error("Error in calculating commitn diff ", e);
		}
		this.logger.info("Finished processing. Check the output file <" + this.nameFile + ">");
	}
	
	/**
	 * Detecta breaking changes e non-breaking changes no commit recebido como parâmetro.
	 * @param commitId - sha do commit.
	 */ 
	public void calculateDiffAtCommit(String commitId, ClassifierAPI classifierAPI) {
		try {
			GitService service = new GitServiceImpl();
			Repository repository = service.openRepositoryAndCloneIfNotExists(this.nameProject, this.url);
			RevCommit commit = service.createRevCommitByCommitId(repository, commitId);
			this.diffCommit(commit, repository, this.nameProject, classifierAPI);
			this.logger.info("Finished processing. Check the output file <" + this.nameFile + ">");
		} catch (Exception e) {
			this.logger.error("Error in calculating commitn diff ", e);
		}
	}
	
	
	/**
	 * Calcula a diferença entre dois projetos.
	 * @param nameProjectVersion1
	 * @param nameProjectVersion2
	 */
	public void calculateDiffProject(final String nameProjectVersion1, final String nameProjectVersion2, ClassifierAPI classifierAPI){
		APIVersion version1 = new APIVersion(nameProjectVersion1, classifierAPI);
		APIVersion version2 = new APIVersion(nameProjectVersion2, classifierAPI);
		this.diff(version1, version2);
		this.printAll(null, classifierAPI);//Escreve saída em arquivo.
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
		APIVersion versionOld = this.getAPIVersionByCommit(currentCommit.getParent(0).getName(), projectFolder, repository, currentCommit,classifierAPI);//versao antiga
		this.diff(versionOld, versionNew);
		this.printAll(currentCommit, classifierAPI);//Escreve saída em arquivo.
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
		this.resultAnnotationType = new AnnotationTypeDiff().calculateDiff(version1, version2);
		this.resultAnnotationTypeMember = new AnnotationTypeMemberDiff().calculateDiff(version1, version2);
	}
	
	/**
	 * Imprime resultado em um arquivo CSV.
	 */
	private void printAll(final RevCommit currentCommit, ClassifierAPI classifierAPI){
		List<String> result =  new ArrayList<String>();
		//Lista de Breaking Changes.
		Date date = new Date();
		result.addAll(this.print(this.resultType, currentCommit, date, classifierAPI));
		result.addAll(this.print(this.resultFild, currentCommit, date, classifierAPI));
		result.addAll(this.print(this.resultMethod,currentCommit, date, classifierAPI));
		result.addAll(this.print(this.resultEnum,currentCommit, date, classifierAPI));
		result.addAll(this.print(this.resultEnumConstant, currentCommit, date, classifierAPI));
		result.addAll(this.print(this.resultAnnotationType, currentCommit, date, classifierAPI));
		result.addAll(this.print(this.resultAnnotationTypeMember, currentCommit, date, classifierAPI));
		
		UtilFile.writeFile(this.nameFile, result);
	}
	
	private List<String> print(final Result r, final RevCommit currentCommit, Date date, ClassifierAPI classifierAPI){
		if(currentCommit != null){
			return this.printListBreakingChange(r, currentCommit, date, classifierAPI);
		}
		else{
			return this.printListBreakingChange(r, date, classifierAPI);
		}
	}
	
	/**
	 * Imprime lista de breaking change detectadas no commit.
	 * @param r - Resultados do diff.
	 * @param currentCommit - Commit corrente, o nulo se a análise não verifica a árvore de commits.
	 * @param date - data do processamento.
	 * @param classifierAPI - classificação das APIs analisadas.
	 * @return
	 */
	private List<String> printListBreakingChange(final Result r, final RevCommit currentCommit, Date date, ClassifierAPI classifierAPI){
		
		//name developer; e-mail; project; path; struture; category; id commit; message commit; timestamp commit (milliseconds); timestamp process (milliseconds); formatted date process; is breaking change (boolean)
		PersonIdent personIdent = currentCommit.getAuthorIdent();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		List<String> list =  new ArrayList<String>();
		if(r != null){
			for(BreakingChange bc: r.getListBreakingChange()){
				list.add(personIdent.getName() + ";" + personIdent.getEmailAddress() + ";" + this.nameProject  + ";" + bc.getPath() + ";" + bc.getStruture() + ";" + bc.getCategory()
				+ ";" + currentCommit.getId().getName() + ";" + this.formatMessage(currentCommit.getFullMessage()) + ";" + personIdent.getWhen().getTime() + ";" + date.getTime() + ";" + sdf.format(date) +  ";" + bc.isBreakingChange()
				+ ";" + classifierAPI + ";" + bc.getDescription());
			}
		}
		return list;
	}
	
	/**
	 * Imprime lista de breaking changes detectadas.
	 * @param  r - Resultados do diff.
	 * @param date - data do processamento.
	 * @param classifierAPI
	 * @return
	 */
	private List<String> printListBreakingChange(final Result r, Date date, ClassifierAPI classifierAPI){
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		List<String> list =  new ArrayList<String>();
		if(r != null){
			for(BreakingChange bc: r.getListBreakingChange()){
				list.add(this.nameProject  + ";" + bc.getPath() + ";" + bc.getStruture() + ";" + bc.getCategory()
				+ ";" + date.getTime() + ";" + sdf.format(date) +  ";" + bc.isBreakingChange()
				+ ";" + classifierAPI + ";" + bc.getDescription());
			}
		}
		return list;
	}
	
	/**
	 * Substitui quebras de linha e ponto e vírgula das mensagens. Ajuste necessário para a saida CSV.
	 * @param message
	 * @return
	 */
	private String formatMessage(String message){
		String messageFormat = message.replaceAll("\\r?\\n", "--NEW_LINE_APIDIFF_PARSER--");
		messageFormat = messageFormat.replace(";", "--SEMICOLON_APIDIFF_PARSER--");
		return messageFormat;
	}

}
