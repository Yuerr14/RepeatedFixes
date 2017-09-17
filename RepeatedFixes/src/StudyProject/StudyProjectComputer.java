package StudyProject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;

public class StudyProjectComputer {
	String projectPath;

	public StudyProjectComputer(String projectPath) {
		this.projectPath=projectPath;	
		
	}
	/*
	 * Raw data
	 */
	public void computeTable1(String patchFolderPath, PrintWriter pw) {
		File patchDir = new File(patchFolderPath);
		File[] patchFileList = patchDir.listFiles();
		HashMap<String, Long> FixNumForEachPatch = new HashMap<String, Long>();
		for (File patchFile : patchFileList) {
			if (patchFile.getName().equals(".DS_Store"))
				continue;
			Long fixNumForOnePatch = computeSinglePatch(patchFile);
			FixNumForEachPatch.put(patchFile.getName(), fixNumForOnePatch);
		}
		long totalPatchNum=patchFileList.length;
		long totalFixNum = 0;
		for (String key : FixNumForEachPatch.keySet()) {
			totalFixNum = totalFixNum + FixNumForEachPatch.get(key);
		}

		try {		
			pw.println("StudyProject:" + this.projectPath);
			pw.println("Num of Fixing Patches:" + totalPatchNum);
			pw.println("Num of Fixes:" + totalFixNum);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private long computeSinglePatch(File patchFile) {
		long fixNumForOnePatch = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(patchFile));
			Vector<String> hunkContent = new Vector<String>();//hunk content(except @@ line)
			Vector<String> headContentForOneDiff = new Vector<String>();//head content(except diff line and @@ line)
			String text = null;
			boolean isBelowDiffAndAboveAt = false;
			while ((text = reader.readLine()) != null) {
				if (text.startsWith("diff")) {
					if (!headContentForOneDiff.isEmpty()&&isAddOrDeleteFile(headContentForOneDiff)) {
							fixNumForOnePatch++;
					} else if (!hunkContent.isEmpty()) {//last @@ of the previous diff
						fixNumForOnePatch = fixNumForOnePatch + processSingleHunk(hunkContent);					
					}
					headContentForOneDiff.clear();
					hunkContent.clear();
					isBelowDiffAndAboveAt = true;
				} else if (text.startsWith("@@")) {
					if (!headContentForOneDiff.isEmpty()&&isAddOrDeleteFile(headContentForOneDiff)) {
						//not update fixNumForOnePatch		
					}	else if (!hunkContent.isEmpty()) {//process the previous @@
						fixNumForOnePatch = fixNumForOnePatch + processSingleHunk(hunkContent);
						hunkContent.clear();
					}
					isBelowDiffAndAboveAt = false;
				} else if (isBelowDiffAndAboveAt) {
					headContentForOneDiff.addElement(text);
				} else {
					hunkContent.addElement(text);
				}

			}
			reader.close();
			if (!hunkContent.isEmpty()) {//last @@ of last diff
				fixNumForOnePatch = fixNumForOnePatch + processSingleHunk(hunkContent);
				hunkContent.clear();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return fixNumForOnePatch;

	}

	private boolean isAddOrDeleteFile(Vector<String> headContent) {
		for (int i = 0; i < headContent.size(); i++) {
			if (headContent.elementAt(i).contains("new file mode")
					|| headContent.elementAt(i).contains("deleted file mode")
					|| headContent.elementAt(i).contains("/dev/null"))
				return true;
		}

		return false;
	}

	private long processSingleHunk(Vector<String> hunkContent) throws IOException {
		long fixNumForOneHunk = 0;
		
		int firstEditIndex = -1;
		int lastEditIndex = -1;
		for (int i = 0; i < hunkContent.size(); i++) {
			if (hunkContent.elementAt(i).startsWith("+") || hunkContent.elementAt(i).startsWith("-")) {
				firstEditIndex = i;
				break;
			}
		}
		for (int i = hunkContent.size() - 1; i >= 0; i--) {
			if (hunkContent.elementAt(i).startsWith("+") || hunkContent.elementAt(i).startsWith("-")) {
				lastEditIndex = i;
				break;
			}
		}
		
		// the number of fixes=1+the number of continuous contexts between firstEditIndex and lastEditIndex
		if (firstEditIndex >= 0 && lastEditIndex >= 0 && lastEditIndex >= firstEditIndex) {
			boolean haveReachedContinuousContext = false;
			fixNumForOneHunk++;
			for (int i = firstEditIndex; i <= lastEditIndex; i++) {
				// System.out.println(hunkContent.elementAt(i));
				if (hunkContent.elementAt(i).startsWith("+") || hunkContent.elementAt(i).startsWith("-")) {// enter a continuous edit
					haveReachedContinuousContext = false;
				} else {//enter a continuous context
					if (!haveReachedContinuousContext) {
						fixNumForOneHunk++;//update the number of continuous context
						haveReachedContinuousContext = true;
					}
				}
			}
		}

		return fixNumForOneHunk;
	}
	
		
	/*
	 * Refined data
	 */
	public void computeTable2(String fixFolderPath, PrintWriter pw) {
		long fixCount = 0;
		Vector<String> patchList = new Vector<String>();
		Vector<String> bugList = new Vector<String>();
		Vector<String> hunkList = new Vector<String>();

		File fileDir = new File(fixFolderPath);
		File[] fileList = fileDir.listFiles();
		if (fileList == null) {
			System.out.println("No Fix Files!");
		}

		for (File file : fileList) {
			if (file.getName().equals(".DS_Store"))
				continue;
			fixCount++;
			String fixFileName = file.getName();
			// System.out.println(fixFileName);
			String[] tmp = fixFileName.split("_");
			String patchName = tmp[0] + "_" + tmp[1];
			String bugName = tmp[0];
			String hunkName = tmp[0] + "_" + tmp[1] + "_" + tmp[2];
			// System.out.println(patchName + " " + bugName + " " + hunkName);

			if (!patchList.contains(patchName))
				patchList.addElement(patchName);
			if (!bugList.contains(bugName))
				bugList.addElement(bugName);
			if (!hunkList.contains(hunkName))
				hunkList.addElement(hunkName);
		}

		try {
			pw.println();
			pw.println("Num of Refined Fixes:" + fixCount);
			pw.println("Num of Refined Bugs:" + bugList.size());
			pw.println("Num of Refined Patches:" + patchList.size());
			pw.println("Num of Refined Hunks:" + hunkList.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
