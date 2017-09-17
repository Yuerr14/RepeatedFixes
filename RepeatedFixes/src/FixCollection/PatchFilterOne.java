package FixCollection;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * InputFolder:Patches OutputFolder:PatchesAfterFilter1
 * 
 * FilterOne consist of: 1. remove total diffs to not MainSourceFiles 2. remove
 * total diffs that are adding or deleting files 3. remove total diffs that
 * contain no change 4. delete NullPatch after the above three steps
 * 
 */
public class PatchFilterOne {

	static ArrayList<String> mainSourceFileSuffixList = new ArrayList<String>();
	static String diffStarter = null;

	private String lineBreak = System.getProperty("line.separator");
	private String pathSep = File.separator;

	public void performFirstFilter(String projectName, String patchFolderPath, String patchAfterFilter1FolderPath) {
		
		mainSourceFileSuffixList.clear();
		if (projectName.equals("EclipseJDTCore")) {// .java(Java)
			mainSourceFileSuffixList.add(".java");
			diffStarter = "diff --git";
		} else if (projectName.equals("MozillaFirfox")) {// .c,.cpp,cxx,.cc(C和C++)
			mainSourceFileSuffixList.add(".c");
			mainSourceFileSuffixList.add(".cpp");
			mainSourceFileSuffixList.add(".cxx");
			mainSourceFileSuffixList.add(".cc");
			diffStarter = "diff -r";
		} else if (projectName.equals("LibreOffice")) {// .cpp,cxx,.cc(C++)
			mainSourceFileSuffixList.add(".cpp");
			mainSourceFileSuffixList.add(".cxx");
			mainSourceFileSuffixList.add(".cc");
			diffStarter = "diff --git";
		}
		removeNotJavaFileDiff(patchFolderPath, patchAfterFilter1FolderPath);
		deleteNullPatchFile(patchAfterFilter1FolderPath);
	}

	private void removeNotJavaFileDiff(String patchFolderPath, String patchAfterFilter1FolderPath) {
		// create patchAfterFilter1Folder
		File patchAfterFilter1Dir = new File(patchAfterFilter1FolderPath);
		if (!patchAfterFilter1Dir.exists())
			patchAfterFilter1Dir.mkdir();

		ArrayList<String> patchFilePathList = new ArrayList<String>();
		File dir = new File(patchFolderPath);
		File[] patchFiles = dir.listFiles();
		if (patchFiles == null) {
			System.err.println("No Patch Files Found!");
			return;
		}

		for (int i = 0; i < patchFiles.length; i++) {
			String patchFilePath = patchFiles[i].getAbsolutePath();
			patchFilePathList.add(patchFilePath);
		}

		BufferedReader br;
		BufferedWriter bw;
		String readStr;
		for (String patchFilePath : patchFilePathList) {
			if (patchFilePath.endsWith(".DS_Store"))
				continue;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(patchFilePath)));
				String patchFileName = patchFilePath.substring(patchFilePath.lastIndexOf(pathSep) + 1);

				File fileFilteredPatch = new File(patchAfterFilter1FolderPath + patchFileName);
				if (!fileFilteredPatch.exists())
					fileFilteredPatch.createNewFile();
				bw = new BufferedWriter(new FileWriter(fileFilteredPatch));

				readStr = null;
				Vector<String> oneDiffContent = new Vector<String>();
				while ((readStr = br.readLine()) != null) {
					if (readStr.startsWith(diffStarter)) {
						if (oneDiffContent.size() > 0) {// process the content
														// of previous diff
							processSingleDiff(oneDiffContent, bw);
							oneDiffContent.clear();
						}
						oneDiffContent.addElement(readStr);
					} else {
						oneDiffContent.addElement(readStr);
					}
				}
				// process the last diff
				if (oneDiffContent.size() > 0)
					processSingleDiff(oneDiffContent, bw);
				oneDiffContent.clear();

				bw.flush();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	private void processSingleDiff(Vector<String> diffContent, BufferedWriter bw) {
		Boolean isMainSourceFileDiff = false;
		Boolean isAddDeleteFile = false;
		Boolean containsModify = false;
		// check the changed file's suffix
		String diff = diffContent.elementAt(0);
		for (String mainSourceFileSuffix : mainSourceFileSuffixList) {
			if (diff.substring(diff.lastIndexOf(".")).equals(mainSourceFileSuffix)) {
				isMainSourceFileDiff = true;
			}
		}
		// check whether the change only contains adding or deleting a file,
		// whether the change contains modification
		for (int i = 0; i < diffContent.size(); i++) {
			if (diffContent.elementAt(i).contains("/dev/null") || diffContent.elementAt(i).contains("new file mode")
					|| diffContent.elementAt(i).contains("deleted file mode")) {
				isAddDeleteFile = true;
			}
			if (diffContent.elementAt(i).startsWith("+") || diffContent.elementAt(i).startsWith("-")) {
				if (!(diffContent.elementAt(i).startsWith("+++") || diffContent.elementAt(i).startsWith("---"))) {
					containsModify = true;
				}
			}
		}
		// if the change is made to main source files, and not adding or
		// deleting a file, and contains modification, it will be reserved.
		try {
			if (isMainSourceFileDiff && !isAddDeleteFile && containsModify) {
				for (int i = 0; i < diffContent.size(); i++) {
					bw.write(diffContent.elementAt(i) + lineBreak);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void deleteNullPatchFile(String patchFolderAfterFilter1) {
		File file = new File(patchFolderAfterFilter1);
		File[] fileList = file.listFiles();
		if (fileList == null)
			return;
		else {
			for (File tmp : fileList) {
				if (tmp.length() == 0) {
					tmp.delete();
				}
			}
		}
	}

}
