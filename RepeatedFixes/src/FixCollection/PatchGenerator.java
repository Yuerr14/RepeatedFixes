package FixCollection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import RepeatedFixAnalyzer.RepeatedFixAnalyzer;

public class PatchGenerator {
	String gitDiffCommand = "git diff";
	String hgDiffCommand = "hg diff -r";
	String patchFileSuffix = ".patch";
	String lineBreak = "\r\n";

	private void sortCommitIDsByBugID(String commitIDsPath, String orderedCommitIDsPath) {
		try {
			ArrayList<String> commitIDsAndLineList = new ArrayList<String>();
			File commitIDsFile = new File(commitIDsPath);
			BufferedReader br = new BufferedReader(new FileReader(commitIDsFile));
			String str = null;
			int lineCnt = 0;
			while ((str = br.readLine()) != null) {
				lineCnt++;
				str = str + " " + lineCnt;
				commitIDsAndLineList.add(str);
			}
			br.close();

			Collections.sort(commitIDsAndLineList, new SortCommitIDsByBugID());

			File orderedCommitIDsFile = new File(orderedCommitIDsPath);
			BufferedWriter bw = new BufferedWriter(new FileWriter(orderedCommitIDsFile));
			for (String oneLine : commitIDsAndLineList) {
				bw.write(oneLine.substring(0, oneLine.lastIndexOf(" ")) + lineBreak);
			}
			bw.flush();
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeBat(String commitIDsPath, String orderedCommitIDsPath, String batFilePath,
			RepeatedFixAnalyzer fixAna) {
		sortCommitIDsByBugID(commitIDsPath, orderedCommitIDsPath);

		try {
			File inputFile = new File(orderedCommitIDsPath);
			BufferedReader br;
			br = new BufferedReader(new FileReader(inputFile));

			File outputFile = new File(batFilePath);
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
			BufferedWriter bw;
			bw = new BufferedWriter(new FileWriter(outputFile));

			bw.write(fixAna.diskName + lineBreak);
			bw.write("mkdir " + fixAna.patchFolderPath + lineBreak);
			bw.write("cd " + fixAna.repoPath + lineBreak);
			String str = null, parentCommitID = null, curCommitID = null, bugID = null, preBugID = null;
			int sameBugIDCount = 0;
			while ((str = br.readLine()) != null) {
				String[] ss = str.split(" ");
				parentCommitID = ss[0];
				curCommitID = ss[1];
				bugID = ss[2];
				if (!bugID.equals(preBugID)) {
					sameBugIDCount = 1;
				} else {
					sameBugIDCount = sameBugIDCount + 1;
				}
				String postDiffCommand = ">" + fixAna.patchFolderPath + bugID + "_" + sameBugIDCount + patchFileSuffix;
				if (!fixAna.projectName.equals("MozillaFirefox"))
					bw.write(gitDiffCommand + " " + parentCommitID + " " + curCommitID + postDiffCommand + lineBreak);
				else
					bw.write(hgDiffCommand + " " + parentCommitID + ":" + curCommitID + postDiffCommand + lineBreak);

				preBugID = bugID;
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void runBat(String batFilePath) {
		Runtime runtime = Runtime.getRuntime();
		try {
			Process p = runtime.exec("cmd /c " + batFilePath);
			final InputStream is1 = p.getErrorStream();
			new Thread(new Runnable() {
				public void run() {
					BufferedReader br = new BufferedReader(new InputStreamReader(is1));
					try {
						while (br.readLine() != null)
							;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class SortCommitIDsByBugID implements Comparator {
		public int compare(Object o1, Object o2) {
			String s1 = (String) o1;
			String s2 = (String) o2;
			String[] tmp1 = s1.split(" ");// parentCommitID, commitID, bugID,
											// lineCnt
			String[] tmp2 = s2.split(" ");
			int bugID1 = Integer.valueOf(tmp1[2]);
			int line1 = Integer.valueOf(tmp1[3]);

			int bugID2 = Integer.valueOf(tmp2[2]);
			int line2 = Integer.valueOf(tmp2[3]);
			if (bugID1 > bugID2 || (bugID1 == bugID2 && line1 > line2))
				return 1;
			else
				return -1;
		}
	}

}
