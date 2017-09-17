package RQ2.Table6;

import java.io.*;
import java.util.*;
/*
 * RQ2-2
 * Goal: extract buggy snippets for FilterdPatches with repeated fixes
 * 
 */

public class BuggySnippetCollector {
	String filteredNewPatchFolderPath = null, buggySnippetFolderPath = null;
	int diffCount = 0, fixCount = 0, contextLineNum = 20;
	String hunkSuffix = null;
	String pathSep = File.separator;
	String lineBreak = System.getProperty("line.separator");

	public BuggySnippetCollector(String filteredNewPatchFolderPath, String buggySnippetFolderPath) {
		this.filteredNewPatchFolderPath = filteredNewPatchFolderPath;
		this.buggySnippetFolderPath = buggySnippetFolderPath;
		File buggySnippetDir = new File(buggySnippetFolderPath);
		if (!buggySnippetDir.exists() && !buggySnippetDir.isDirectory()) {
			buggySnippetDir.mkdir();
		}

		File filteredNewPatchFileDir = new File(filteredNewPatchFolderPath);
		for (File file : filteredNewPatchFileDir.listFiles()) {
			if (file.getName().equals(".DS_Store"))
				continue;
			patchToBuggySnippets(file.getAbsolutePath());
		}
	}

	public void patchToBuggySnippets(String filteredNewPatchfilePath) {
		try {
			File file = new File(filteredNewPatchfilePath);
			Scanner input = new Scanner(file);

			diffCount = 0;// diff count in current patch
			fixCount = 0;// continuous change(fix) count in current patch

			Vector<String> hunkContent = new Vector<String>();
			boolean isBelowDiffAndAboveAt = false;
			String sourceFileSuffix = null;
			while (input.hasNext()) {
				String text = input.nextLine();
				// System.out.println(text);
				if (text.startsWith("diff")) {
					if (!hunkContent.isEmpty()) {// last @@ of previous diff
						processSingleHunk(hunkContent, filteredNewPatchfilePath);
						hunkContent.clear();
					}
					hunkSuffix = text.substring(text.lastIndexOf("."));
					diffCount++;
					fixCount = 0;
					isBelowDiffAndAboveAt = true;

				} else if (text.startsWith("@@")) {
					if (!hunkContent.isEmpty()) {// previous @@
						processSingleHunk(hunkContent, filteredNewPatchfilePath);
						hunkContent.clear();
					}
					isBelowDiffAndAboveAt = false;
				} else if (!isBelowDiffAndAboveAt) {
					hunkContent.add(text);
				}
			}
			input.close();
			if (!hunkContent.isEmpty()) {
				processSingleHunk(hunkContent, filteredNewPatchfilePath);
				hunkContent.clear();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void processSingleHunk(Vector<String> hunkContent, String patchfilePath) {
		int firstIndex = 0, lastIndex = 0;
		int k = 0;
		for (int i = 0; i < hunkContent.size(); i = lastIndex + 1) {
			boolean isFirstEditedLineExist = false;
			for (k = i; k < hunkContent.size(); k++) {
				if (hunkContent.get(k).startsWith("+") || hunkContent.get(k).startsWith("-")) {
					if (!isFirstEditedLineExist) {
						firstIndex = k;
						isFirstEditedLineExist = true;
					}
				} else {
					if (isFirstEditedLineExist) {
						lastIndex = k - 1;
						break;
					}
				} // end of else
			}
			if (!isFirstEditedLineExist || k == hunkContent.size()) {
				break;
			}
			fixCount++;

			int countUp = 0;
			String buggySnippetStr = "";
			for (int j = firstIndex - 1; j >= 0 && countUp < contextLineNum; j--) {
				if (!hunkContent.get(j).startsWith("+")) {
					if (buggySnippetStr.equals("")) {
						buggySnippetStr = hunkContent.get(j);
					} else {
						buggySnippetStr = hunkContent.get(j) + lineBreak + buggySnippetStr;
					}
					countUp++;
				}
			}

			int addLineCount = 0;
			int deleteLineCount = 0;
			for (int j = firstIndex; j <= lastIndex; j++) {
				if (hunkContent.get(j).startsWith("-")) {
					if (buggySnippetStr.equals("")) {
						buggySnippetStr = hunkContent.get(j);
					} else {
						buggySnippetStr = buggySnippetStr + lineBreak + hunkContent.get(j);
					}
					deleteLineCount++;
				} else {
					addLineCount++;
				}
			}

			int countDown = 0;
			for (int j = lastIndex + 1; countDown < contextLineNum && j < hunkContent.size(); j++) {
				if (!hunkContent.get(j).startsWith("+")) {
					if (buggySnippetStr.equals("")) {
						buggySnippetStr = hunkContent.get(j);
					} else {
						buggySnippetStr = buggySnippetStr + lineBreak + hunkContent.get(j);
					}
					countDown++;
				}
			}

			String buggySnippetFilePath = buggySnippetFolderPath
					+ patchfilePath.substring(patchfilePath.lastIndexOf(pathSep) + 1, patchfilePath.lastIndexOf("."))
					+ "_" + diffCount + "_" + fixCount + "-" + countUp + "-" + countDown + "-" + addLineCount + "-"
					+ deleteLineCount + hunkSuffix;
			File outputFile = new File(buggySnippetFilePath);
			try {
				if (!outputFile.exists()) {
					outputFile.createNewFile();
				}
				FileWriter output = new FileWriter(outputFile);
				output.write(buggySnippetStr);
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
