package RQ2.Table6;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
//Goal:
//(1)filter buggy snippets which do not correspond to repeated fixes
//(2)preprocess buggy snippets for clone detection(enclose buggy snippet with "{}", remove minus sign)

public class BuggySnippetProcessor {
	String buggySnippetFolderPath = null, buggySnippetWithRepeatedFixFolderPath = null;
	String filteredXmlFolderPath = null, tmpBuggySnippetFolderPath = null;

	String pathSep = File.separator;
	String lineBreak = System.getProperty("line.separator");
	String plusSign = "+", minusSign = "-";

	public BuggySnippetProcessor(String buggySnippetFolderPath, String buggySnippetWithRepeatedFixFolderPath,
			String filteredRepeatedFixXmlFolderPath) {
		this.buggySnippetFolderPath = buggySnippetFolderPath;
		this.buggySnippetWithRepeatedFixFolderPath = buggySnippetWithRepeatedFixFolderPath;
		this.filteredXmlFolderPath = filteredRepeatedFixXmlFolderPath;
		
		String tmpStr=buggySnippetFolderPath.substring(0, buggySnippetFolderPath.lastIndexOf(pathSep));
		tmpBuggySnippetFolderPath = tmpStr.substring(0, tmpStr.lastIndexOf(pathSep))
				+ pathSep + "BuggySnippetsTmp"+pathSep;

		File dir = new File(buggySnippetWithRepeatedFixFolderPath);
		if (!dir.exists() && !dir.isDirectory()) {
			dir.mkdir();
		}
		File tmpDir = new File(tmpBuggySnippetFolderPath);
		if (!tmpDir.exists() && !tmpDir.isDirectory()) {
			tmpDir.mkdir();
		}
		this.filterBuggySnippetsWithNoRepeatedFix();
	}

	private void filterBuggySnippetsWithNoRepeatedFix() {
		File xmlDir = new File(filteredXmlFolderPath);
		ArrayList<String> fixNameList = new ArrayList<String>();
		for (File xmlfile : xmlDir.listFiles()) {
			if (!xmlfile.getName().endsWith(".xml"))
				continue;
			Scanner input;
			try {
				input = new Scanner(xmlfile);
				String tmp = null;
				while (input.hasNextLine()) {
					tmp = input.nextLine();
					if (tmp.contains("<path>")) {
						String fixName = tmp.substring(tmp.lastIndexOf(pathSep) + 1, tmp.lastIndexOf("."));
						if (!fixNameList.contains(fixName))
							fixNameList.add(fixName);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		File buggySnippetDir = new File(buggySnippetFolderPath);
		for (File buggySnippetFile : buggySnippetDir.listFiles()) {
			String buggySnippetFileName = buggySnippetFile.getName();
			String appliedFixName = buggySnippetFileName.substring(0, buggySnippetFileName.indexOf("-"));
			if (fixNameList.contains(appliedFixName)) {
				File tmpBuggySnippetFile=new File(tmpBuggySnippetFolderPath + buggySnippetFileName);
				File buggySnippetFileWithRepeatFix=new File(buggySnippetWithRepeatedFixFolderPath+buggySnippetFileName);
				if (buggySnippetFile.renameTo(tmpBuggySnippetFile)) {
					System.out.println("BuggySnippet File is moved successful!");
					this.rewriteOneBuggySnippet(tmpBuggySnippetFile, buggySnippetFileWithRepeatFix);
				}
			}
		}

	}

	private void rewriteOneBuggySnippet(File inputFile, File outputFile) {
		BufferedReader br;
		BufferedWriter bw;
		try {
			br = new BufferedReader(new FileReader(inputFile));

			bw = new BufferedWriter(new FileWriter(outputFile));
			String str = null;
			str = br.readLine();
			bw.write("{" + removePlusOrMinusSign(str));// insert { before the
														// first line
			while ((str = br.readLine()) != null) {
				bw.write(lineBreak);// insert lineBreak for the previous line
				bw.write(removePlusOrMinusSign(str));
			}
			bw.write("}");// insert { for the last line(can't insert lineBreak,
							// or the line number will become different)
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String removePlusOrMinusSign(String str) {
		String removedStr = null;
		if (str.startsWith(plusSign))
			removedStr = str.substring(str.indexOf(plusSign) + 1);
		else if (str.startsWith(minusSign))
			removedStr = str.substring(str.indexOf(minusSign) + 1);
		else
			removedStr = str;
		return removedStr;

	}

}
