package br.ufmg.dcc.labsoft.apidiff;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

public class UtilFile {
	
	/**
	 * Imprime a lista em uma arquivo. Cada item em uma linha.
	 * @param nameFile
	 * @param listMsg
	 */
	public static void writeFile(final String nameFile, final List<String> listMsg){
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter (new FileOutputStream(nameFile), "utf-8"));
			for(String msg: listMsg){
				writer.write(msg + "\n");
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("Erro ao escrever resultado no arquivo de saida. " + e);
		} 
	}

}
