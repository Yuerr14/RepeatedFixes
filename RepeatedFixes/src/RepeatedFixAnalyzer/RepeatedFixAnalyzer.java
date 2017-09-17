package RepeatedFixAnalyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import FixCollection.CommitAnalyzer;
import FixCollection.CommitInfoReverse;
import FixCollection.FixFilterTwo;
import FixCollection.PatchFilterOne;
import FixCollection.PatchGenerator;
import FixCollection.MethodLabel.MethodLabelInvoker;
import RQ1.RQ1Invoker;
import RQ2.RQ2Invoker;
import RQ2.Table6.BuggySnippetCollector;
import RQ2.Table6.BuggySnippetProcessor;
import RQ2.Table6.BuggySnippetsOfOneGroupCloneDetection;
import RQ2.Table6.NewPatchGen;
import RQ2.Table6.Table6Computer;
import RepeatedFixDetection.FixFormatter;
import RepeatedFixDetection.RepeatDetectionBatGenerator;
import RepeatedFixDetection.RepeatedFixCollector;
import RepeatedFixDetection.RepeatedFixFilter;
import StudyProject.StudyProjectComputer;

public class RepeatedFixAnalyzer {
	public String diskName = "E:";
	String pathSep = File.separator;
	String lineBreak = System.getProperty("line.separator");

	public String projectName;
	String fixedBugReportName;
	String repoName;
	// BasicPaths
	public String projectPath;
	public String repoPath;
	String patchCollectionPath;
	String fixedBugReportsPath;
	// For getDateAscCommits()
	String commitInfoPath;
	String dateAscCommitInfoPath;
	// For getPatches()
	String commitIDsForDiffPath;
	String table1Path;
	String orderedCommitIDsForDiffPath;
	String patchGenBatPath;
	public String patchFolderPath;
	// For filterFixes()
	String patchAfterFilter1FolderPath;
	String fixFolderPath;
	String methodLabelFolderPath;
	// For generateDetectionBat()
	String formattedFixFolderPath;
	String editSeqFolderPath;
	String repeatedFixFolderPath;
	int token = 15;
	// For getFilteredRepeatedFixes()
	String cloneInfoFolderPath;
	String cloneInfoXmlFolderPath;
	String repeatedFixXmlFolderPath;
	String filteredRepeatedFixXmlFolderPath;
	// For computeStudyProjectInfo()
	// String studyProjectInfoFolderPath;
	// For computeRQ1()
	String rq1FolderPath;
	// For computeRQ2()
	String rq2FolderPath;
	String buggySnippetInfoPath;
	String newPatchFolderPath, filteredNewPathFolderPath;
	String buggySnippetFolderPath, buggySnippetWithRepeatedFixFolderPath;
	String buggySnippetCloneInfoPath;

	RepeatedFixAnalyzer(String proName, String fixedBugFileName, String repoName) {
		this.projectName = proName;
		this.fixedBugReportName = fixedBugFileName;
		this.repoName = repoName;
		setBasicPaths();
		setOtherPaths();
	}

	private void setBasicPaths() {
		projectPath = diskName + pathSep + projectName + pathSep;
		repoPath = projectPath + repoName + pathSep;
		patchCollectionPath = projectPath + "PatchCollection" + pathSep;
		fixedBugReportsPath = patchCollectionPath + fixedBugReportName;
	}

	private void setOtherPaths() {
		// For getDateAscCommits()
		commitInfoPath = patchCollectionPath + "commitInfoOneParentBefore20160101.txt";
		dateAscCommitInfoPath = patchCollectionPath + "dateAscCommitInfo.txt";
		// For getPatches()
		commitIDsForDiffPath = patchCollectionPath + "commitIDsForDiff.txt";
		table1Path = projectPath + "Table1.txt";
		orderedCommitIDsForDiffPath = patchCollectionPath + "commitIDsForDiffOrdered.txt";
		patchGenBatPath = patchCollectionPath + "patchGen.bat";
		patchFolderPath = projectPath + "Patches" + pathSep;
		// For filterFixes()
		patchAfterFilter1FolderPath = projectPath + "PatchesAfterFilter1" + pathSep;
		fixFolderPath = projectPath + "Fixes" + pathSep;
		methodLabelFolderPath = projectPath + "MethodLabels" + pathSep;
		// For getRepeatedFixes()
		formattedFixFolderPath = projectPath + "FormattedFixes" + pathSep;
		editSeqFolderPath = projectPath + "EditSeqs" + pathSep;
		repeatedFixFolderPath = projectPath + "RepeatedFixes" + pathSep;
		// For getFilteredRepeatedFixes()
		cloneInfoFolderPath = repeatedFixFolderPath + "CloneInfo" + token + pathSep;
		cloneInfoXmlFolderPath = cloneInfoFolderPath + "cloneInfoXml" + pathSep;
		repeatedFixXmlFolderPath = repeatedFixFolderPath + "RepeatedFixXmls" + pathSep;
		filteredRepeatedFixXmlFolderPath = repeatedFixFolderPath + "FilteredRepeatedFixXmls" + pathSep;
		// For computeStudyProjectInfo()
		// studyProjectInfoFolderPath = projectPath + "StudyProjectInfo" +
		// pathBreak;
		// For computeRQ1()
		rq1FolderPath = projectPath + "RQ1" + pathSep;
		// For computeRQ2()
		rq2FolderPath = projectPath + "RQ2" + pathSep;
		buggySnippetInfoPath = rq2FolderPath + "BuggySnippetInfo" + pathSep;
		newPatchFolderPath = buggySnippetInfoPath + "NewPatches" + pathSep;
		filteredNewPathFolderPath = buggySnippetInfoPath + "NewPatchesAfterFilter1" + pathSep;
		buggySnippetFolderPath = buggySnippetInfoPath + "BuggySnippets" + pathSep;
		buggySnippetWithRepeatedFixFolderPath = buggySnippetInfoPath + "BuggySnippetsWithRepeatedFix" + pathSep;
		buggySnippetCloneInfoPath = buggySnippetInfoPath + "BuggySnippetCloneInfo" + pathSep;
	}

	private void getDateAscCommits() {
		CommitInfoReverse reverse = new CommitInfoReverse();
		reverse.readReverseOrder(commitInfoPath, "UTF-8", dateAscCommitInfoPath, lineBreak);
	}

	private void getPatches() {
		CommitAnalyzer commitAna = new CommitAnalyzer();
		commitAna.getCommitIDsForDiff(fixedBugReportsPath, dateAscCommitInfoPath, commitIDsForDiffPath);
		commitAna.writeStudyProjectInfo(table1Path);

		PatchGenerator patchGen = new PatchGenerator();
		patchGen.writeBat(commitIDsForDiffPath, orderedCommitIDsForDiffPath, patchGenBatPath, this);
	}

	private void performFirstFilterForFixes() {
		PatchFilterOne filterOne = new PatchFilterOne();
		filterOne.performFirstFilter(projectName, patchFolderPath, patchAfterFilter1FolderPath);
	}

	private void labelMethodForFixes() {
		MethodLabelInvoker methodLabelInvoker = new MethodLabelInvoker();
		methodLabelInvoker.labelMethodForPatches(projectName, repoPath, patchAfterFilter1FolderPath,
				orderedCommitIDsForDiffPath, methodLabelFolderPath);
	}

	private void performSecondFilterForFixes() {
		FixFilterTwo filterTwo = new FixFilterTwo();
		filterTwo.performSecondFilter(patchAfterFilter1FolderPath, fixFolderPath, methodLabelFolderPath);
	}

	private void generateDetectionBat() {
		// Format Fixes
		FixFormatter fixFormatter = new FixFormatter();
		fixFormatter.getFormattedFixes(fixFolderPath, formattedFixFolderPath, editSeqFolderPath);
		// Detection Clones For Formatted Fixes
		RepeatDetectionBatGenerator detector = new RepeatDetectionBatGenerator(projectName, token,
				formattedFixFolderPath, repeatedFixFolderPath);
		detector.generateDetectionBat();
	}

	private void getFilteredRepeatedFixes() {
		// get Real Repeated Fixes
		RepeatedFixCollector collector = new RepeatedFixCollector(cloneInfoFolderPath, cloneInfoXmlFolderPath,
				editSeqFolderPath, repeatedFixXmlFolderPath);
		collector.getRepeatedFixesBasedOnEditSeqs();
		// Filter Real Repeated Fixes
		RepeatedFixFilter filter = new RepeatedFixFilter();
		filter.getFilterdRepeatedFixes(repeatedFixXmlFolderPath, filteredRepeatedFixXmlFolderPath, fixFolderPath);
	}

	private void computeStudyProjectInfo() {
		StudyProjectComputer projectInfoComputer = new StudyProjectComputer(projectPath);
		String table2FilePath = projectPath + "Table2.txt";
		File table2File = new File(table2FilePath);
		try {
			if (!table2File.exists())
				table2File.createNewFile();
			PrintWriter pwTable2 = new PrintWriter(new FileWriter(table2File));
			projectInfoComputer.computeTable1(patchFolderPath, pwTable2);
			projectInfoComputer.computeTable2(fixFolderPath, pwTable2);
			pwTable2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void computeRQ1() {
		File rq1Folder = new File(rq1FolderPath);
		if (!rq1Folder.exists())
			rq1Folder.mkdirs();
		RQ1Invoker rq1Invoker = new RQ1Invoker(rq1FolderPath, filteredRepeatedFixXmlFolderPath);
		rq1Invoker.computeTable3();
		rq1Invoker.computeFigure3And4();
		rq1Invoker.computeTable4(token, projectName, formattedFixFolderPath, editSeqFolderPath, fixFolderPath);
	}

	private void computeFirstRQOfRQ2() {
		File rq2Folder = new File(rq2FolderPath);
		if (!rq2Folder.exists())
			rq2Folder.mkdirs();
		RQ2Invoker rq2Invoker = new RQ2Invoker(rq2FolderPath, filteredRepeatedFixXmlFolderPath, methodLabelFolderPath);
		rq2Invoker.computeTable5();
	}

	private void genNewPatchesBat() {
		NewPatchGen patchGen = new NewPatchGen(projectPath, projectName, repoPath, filteredRepeatedFixXmlFolderPath,
				orderedCommitIDsForDiffPath, buggySnippetInfoPath, newPatchFolderPath);
	}

	private void getBuggySnippets() {
		PatchFilterOne filterOne = new PatchFilterOne();
		filterOne.performFirstFilter(projectName, newPatchFolderPath, filteredNewPathFolderPath);
		BuggySnippetCollector collector = new BuggySnippetCollector(filteredNewPathFolderPath, buggySnippetFolderPath);
		BuggySnippetProcessor processor = new BuggySnippetProcessor(buggySnippetFolderPath,
				buggySnippetWithRepeatedFixFolderPath, filteredRepeatedFixXmlFolderPath);
	}

	private void genBuggySnippetCloneDetectionBat() {
		BuggySnippetsOfOneGroupCloneDetection batGen = new BuggySnippetsOfOneGroupCloneDetection(
				buggySnippetWithRepeatedFixFolderPath, filteredRepeatedFixXmlFolderPath, projectName,
				buggySnippetCloneInfoPath);
	}

	private void computeTable6() {
		Table6Computer table6Computer=new Table6Computer(filteredRepeatedFixXmlFolderPath,buggySnippetInfoPath, rq2FolderPath);
		table6Computer.computeTable6();

	}

	public static void main(String[] args) {
		RepeatedFixAnalyzer analyzer = new RepeatedFixAnalyzer("EclipseJDTCore", "bugs-2005.csv", "eclipse.jdt.core");
		// analyzer.getDateAscCommits();
		// analyzer.getPatches();
		// Manually run patchGen.bat

		// analyzer.performFirstFilterForFixes();
		// analyzer.labelMethodForFixes();
		// analyzer.performSecondFilterForFixes();

		// analyzer.generateDetectionBat();
		// Manually run cloneDetFormattedFixes15.bat
		// analyzer.getFilteredRepeatedFixes();

		// analyzer.computeStudyProjectInfo();
		// analyzer.computeRQ1();
		// analyzer.computeFirstRQOfRQ2();

		// ComputingTable6
		// analyzer.genNewPatchesBat();
		// Manually run patchesWithRepeatedFixesGen.bat
		// analyzer.getBuggySnippets();
		// analyzer.genBuggySnippetCloneDetectionBat();
		// Manually run buggySnippetCloneDetection.bat(token from 5 to 50)
		//analyzer.computeTable6();

	}
}
