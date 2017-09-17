package RQ2.Table6;

import java.io.*;
import java.util.*;

import FixCollection.MethodLabel.CommitIDs;
import FixCollection.MethodLabel.Node;

/*
 * RQ2-2:How effectively clone detection tools can suggest edit locations for repeated fixes 
 * Step 1: generate patches with repeated fixes
 * 
 */
public class NewPatchGen {
	String diskName = "E:";
	String projectPath = null, projectName = null, repoPath = null;
	String filteredXmlPath = null, commitIDsPath = null;
	String buggySnippetInfoPath=null,newPatchFolderPath=null;

	String lineBreak = System.getProperty("line.separator");
	String gitDiffCommand = "git diff", hgDiffCommand = "hg diff -r", patchFileSuffix = ".patch";
	int contextLineNum = 20;

	public NewPatchGen(String projectPath, String projectName, String repoPath, String filteredXmlPath,
			String commitIDsPath, String buggySnippetInfoPath,String newPatchFolderPath) {
		this.projectPath = projectPath;
		this.projectName = projectName;
		this.repoPath = repoPath;
		this.filteredXmlPath = filteredXmlPath;
		this.commitIDsPath = commitIDsPath;
		this.buggySnippetInfoPath=buggySnippetInfoPath;
		this.newPatchFolderPath=newPatchFolderPath;
		this.genBatForPatchesWithRepeatedFixes();
		
	}

	public void genBatForPatchesWithRepeatedFixes() {
		File buggySnippetInfoDir = new File(buggySnippetInfoPath);
		if (!buggySnippetInfoDir.exists() && !buggySnippetInfoDir.isDirectory()) {
			buggySnippetInfoDir.mkdir();
		}
		
		File newPatchDir = new File(newPatchFolderPath);
		if (!newPatchDir.exists() && !newPatchDir.isDirectory()) {
			newPatchDir.mkdir();
		}
		String newGeneratePathBatPath = buggySnippetInfoPath + "patchesWithRepeatedFixesGen.bat";
		File newBatFile = new File(newGeneratePathBatPath);
		try {
			if (!newBatFile.exists()) {
				newBatFile.createNewFile();
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(newBatFile));
			bw.write(diskName + lineBreak);
			bw.write("cd " + repoPath + lineBreak);

			CommitIDs ids = new CommitIDs(commitIDsPath);
			File xmlDir = new File(filteredXmlPath);
			File[] xmlFileList = xmlDir.listFiles();
			for (File xmlFile : xmlFileList) {
				if (xmlFile.getName().equals(".DS_Store"))
					continue;
				processSingleXml(xmlFile, ids, bw, newPatchFolderPath);
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void processSingleXml(File xmlFile, CommitIDs ids, BufferedWriter bw, String patchFolder) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(xmlFile));

			String strLine = null;
			ArrayList<String> patchNameListInXml = new ArrayList<String>();
			while ((strLine = br.readLine()) != null) {
				if (strLine.contains("<path>")) {
					String fixName = strLine.substring(strLine.lastIndexOf("\\") + 1, strLine.lastIndexOf("."));
					String[] tmp = fixName.split("_");
					String patchName = tmp[0] + "_" + tmp[1];
					if (!patchNameListInXml.contains(patchName))
						patchNameListInXml.add(patchName);
				}
			}

			for (String patchName : patchNameListInXml) {
				String[] tmp = patchName.split("_");
				int bugID = Integer.parseInt(tmp[0]);
				int index = Integer.parseInt(tmp[1]);
				Node n = ids.map.get(bugID).get(index - 1);
				String parentCommitID = n.preHash;
				String curCommitID = n.Hash;
				String postDiffCommand = ">" + patchFolder + bugID + "_" + index + patchFileSuffix;
				if (!projectName.equals("MozillaFirefox"))
					bw.write(gitDiffCommand + " " + "--unified=" + contextLineNum + " " + parentCommitID + " "
							+ curCommitID + postDiffCommand + lineBreak);
				else
					bw.write(hgDiffCommand + " " + parentCommitID + ":" + curCommitID + " " + "--unified="
							+ contextLineNum + postDiffCommand + lineBreak);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}