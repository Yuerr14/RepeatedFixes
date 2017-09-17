package FixCollection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

/**
 * Input：dateAscCommitInfo.txt, sampledFixedBugs.csv
 * Output:commitIDsForDiff.txt
 */
public class CommitAnalyzer {
	ArrayList<Long> sampledBugIDs;
	int sampledBugCount, diffCommitIDCount;
	int distinctCommitCountWithBugID,commitCountWithBugID,totalCommitCount;
	String lineBreak= System.getProperty("line.separator");

	public CommitAnalyzer() {
		sampledBugIDs = new ArrayList<Long>();
		sampledBugCount = 0;
		diffCommitIDCount = 0;
	}

	private void getSampledBugIDs(String fixedBugReportsPath) {
		try {
			sampledBugIDs.clear();
			File fileFixedBugs = new File(fixedBugReportsPath);
			BufferedReader br;
			br = new BufferedReader(new FileReader(fileFixedBugs));
			String str = null;
			while ((str = br.readLine()) != null) {
				if (str.startsWith("\""))
					continue;// table header
				sampledBugIDs.add(Long.parseLong(str.substring(0, str.indexOf(","))));
				sampledBugCount++;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Process each commit in dateAscCommits.txt as the following: 
	 * 1. get the log message 
	 * 2. extract all numbers in the log message and compare the numbers with sampled BugIDs 
	 * 3. if a number is the same as one sampled BugID, record the current commit and its parent commit info to commitIDsForDiff.txt
	 */
	public void getCommitIDsForDiff(String fixedBugReportsPath, String dateAscCommitInfoPath, String diffCommitIDsPath) {
		getSampledBugIDs(fixedBugReportsPath);

		try {
			File fileDateAscCommits = new File(dateAscCommitInfoPath);
			BufferedReader br;
			br = new BufferedReader(new FileReader(fileDateAscCommits));
			
			PrintWriter pw=new PrintWriter(new FileWriter(diffCommitIDsPath));
			String str = null, parentCommitID = null, curCommitID = null, logMessage = null;
			
			while ((str = br.readLine()) != null) {
				// one commit usually contains at least three
				// ","(commitID,date,parentCommitID,log message)
				if (!str.contains(",")) {// str contains no ","
					continue;// there are multiple lines in the log message of
								// last commit
				} else {
					String tmp1 = str.substring(str.indexOf(",") + 1);
					if (!tmp1.contains(",")) {// str contains only one ","
						continue;
					} else {
						String tmp2 = tmp1.substring(tmp1.indexOf(",") + 1);
						if (!tmp2.contains(",")) {// str contains only two ","
							continue;
						}
					}
				}
				curCommitID = str.substring(0, str.indexOf(","));	
				// date,parentCommitID,log message
				String strWithoutCommitID = str.substring(str.indexOf(",") + 1);
				// parentCommitID,log message
				String strWithoutCommitIDAndDate = strWithoutCommitID.substring(strWithoutCommitID.indexOf(",") + 1);
				// parentCommitID
				parentCommitID = strWithoutCommitIDAndDate.substring(0, strWithoutCommitIDAndDate.indexOf(","));
				// log message
				logMessage = strWithoutCommitIDAndDate.substring(strWithoutCommitIDAndDate.indexOf(",") + 1);
				
				totalCommitCount++;
				long matchedBugID=getMatchedBugID(logMessage);
				if(matchedBugID==0) continue;
					pw.println(parentCommitID+" "+curCommitID+" "+matchedBugID);	
					 diffCommitIDCount++;	
			}
			br.close();
			pw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	private long getMatchedBugID(String logMessage){
		long matchedBugID=0;
		String[] ss = logMessage.split("\\D+");// Splited by NonNum
		for (int i = 0; i < ss.length; i++) {
			long bugIDForCheck=0;
			if (ss[i].length() > 0)
				bugIDForCheck = Long.parseLong(ss[i]);
			for (long sampledBugID : sampledBugIDs) {
				if (bugIDForCheck == sampledBugID) {		
					matchedBugID=bugIDForCheck;
					commitCountWithBugID++;
				}
			}
		}
		if(matchedBugID!=0)
			distinctCommitCountWithBugID++;	
		return matchedBugID;
	}

	public void writeStudyProjectInfo(String studyProjectTablePath) {
		try {
			File fileStudyProjectTable = new File(studyProjectTablePath);
			if (!fileStudyProjectTable.exists()) {
				fileStudyProjectTable.createNewFile();
			}
			BufferedWriter bwTable;
			bwTable = new BufferedWriter(new FileWriter(fileStudyProjectTable));
			bwTable.write("SampledBugCount," + sampledBugCount + lineBreak);
			bwTable.write("DiffCommitIDCount(=DiffCommitIDCount(for rqs))," + diffCommitIDCount + lineBreak);
			bwTable.write("TotalCommitCount,"+totalCommitCount+lineBreak);
			bwTable.write("DistinctCommitCountWithBugID,"+distinctCommitCountWithBugID+lineBreak);
			bwTable.write("CommitCountWithBugID,"+commitCountWithBugID+lineBreak);
			bwTable.flush();
			bwTable.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
