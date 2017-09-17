package FixCollection.MethodLabel;

import java.io.*;
import java.util.Properties;

public class MethodLabelInvoker {

	public void labelMethodForPatches(String projectName, String repoPath, String patchAfterFilter1FolderPath,
			String commitIDsForDiffPath, String methodLabelFolderPath) {
		File methodLabelDir=new File(methodLabelFolderPath);
		if(!methodLabelDir.exists())
			methodLabelDir.mkdirs();
			
		CommitIDs ids = new CommitIDs(commitIDsForDiffPath);
		File patches = new File(patchAfterFilter1FolderPath);
		String[] files = patches.list();
		for (int i = 0; i < files.length; i++) {
			String filename = files[i];
			System.out.println(i + " : " + filename);
			String[] commitInfo = filename.split("_");
			int bugid = Integer.parseInt(commitInfo[0]);
			int index = Integer.parseInt(commitInfo[1].substring(0, commitInfo[1].indexOf(".")));
			Node n = ids.map.get(bugid).get(index - 1);
			
			LabelProcessor labelProcessor=new LabelProcessor(filename, n.preHash, n.Hash,projectName,repoPath,patchAfterFilter1FolderPath,methodLabelFolderPath);
			labelProcessor.run();

		}
	}

}
