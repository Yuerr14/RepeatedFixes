package FixCollection;

import java.awt.geom.Area;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;
import javax.xml.soap.SAAJMetaFactory;

/*
 * Input: commitInfo.txt((before 2016/01/01, one parent)
 * {EclipseJDTCore:git log  --pretty=format:"%H,%cd,%P,%s" --before="2016-01-01" --no-merges>commitInfoOneParentBefore20160101.txt
 * 
 * Output: dateAscCommitInfo.txt 
 */
public class CommitInfoReverse {
	/*
	 * Write the content of inputfile to outputfile in reverse order line by
	 * line
	 * 
	 * @param inputFilePath
	 * 
	 * @param inputFileCharset(to avoid garbage characters)
	 */
	public void readReverseOrder(String inputFilePath, String inputFileCharset, String outputFilePath,
			String lineBreak) {
		try {
			File fileOutput = new File(outputFilePath);
			if (!fileOutput.exists()) {
				fileOutput.createNewFile();
			}
			OutputStreamWriter osw = null;
			// Can give the charSet when initializing OutputStream
			osw = new OutputStreamWriter(new FileOutputStream(fileOutput), inputFileCharset);
			RandomAccessFile rf = null;
			try {
				rf = new RandomAccessFile(inputFilePath, "r");
				long len = rf.length();
				long start = rf.getFilePointer();
				long nextend = start + len - 1;
				// System.out.println("Start:"+start+" End:"+nextend);
				String line;
				rf.seek(nextend);
				int c = -1;
				while (nextend > start) {
					c = rf.read();
					if (c == '\n' || c == '\r') {
						// The char set is changed to ISO-8859-1 when RandomAccessFile is calling readLine
						line = rf.readLine();
						if (line != null) {	
							osw.write(new String(line.getBytes("ISO-8859-1"), inputFileCharset) + lineBreak);
							// osw.write(line+lineBreak);
						}
						// nextend--;//Add this code line or not is okay
					}
					nextend--;
					rf.seek(nextend);
					if (nextend == 0) {//When the file pointer is at the beginning of file, output the first line
						line = rf.readLine();
						osw.write(new String(line.getBytes("ISO-8859-1"), inputFileCharset) + lineBreak);
						// osw.write(line+lineBreak);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (rf != null)
						rf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			osw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
