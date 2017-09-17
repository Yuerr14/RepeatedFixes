package FixCollection.MethodLabel;

import java.io.*;
import java.util.*;

import org.eclipse.cdt.core.parser.ParserLanguage;

import FixCollection.MethodLabel.JavaParser.JavaParser;
import FixCollection.MethodLabel.CppParser.CPPParser;

public class LabelProcessor {
	String filename;
	String preHash;
	String Hash;
	
	String lineBreak= System.getProperty("line.separator");

	String projectName, repoPath, patchesPath, resultPath;
	JavaParser javaParser = new JavaParser();
	CPPParser cppParser = new CPPParser();

	String cmd;
	ArrayList<String> mainSourceFileSuffixList;

	public LabelProcessor(String _filename, String _preHash, String _Hash, String projectName, String repoPath,
			String patchAfterFilter1FolderPath, String methodLabelFolderPath) {
		filename = _filename;
		preHash = _preHash;
		Hash = _Hash;

		this.projectName = projectName;
		this.repoPath = repoPath;
		patchesPath = patchAfterFilter1FolderPath;
		resultPath = methodLabelFolderPath;

		cmd = "E:"+lineBreak+"cd " + repoPath + lineBreak+"git reset --hard ";

		mainSourceFileSuffixList = new ArrayList<String>();
		mainSourceFileSuffixList.clear();
		if (projectName.equals("EclipseJDTCore")) {// .java(Java)
			mainSourceFileSuffixList.add(".java");
		} else if (projectName.equals("MozillaFirfox")) {// .c,.cpp,cxx,.cc(C和C++)
			mainSourceFileSuffixList.add(".c");
			mainSourceFileSuffixList.add(".cpp");
			mainSourceFileSuffixList.add(".cxx");
			mainSourceFileSuffixList.add(".cc");
		} else if (projectName.equals("LibreOffice")) {// .cpp,cxx,.cc(C++)
			mainSourceFileSuffixList.add(".cpp");
			mainSourceFileSuffixList.add(".cxx");
			mainSourceFileSuffixList.add(".cc");
		}
	}

	public void run() {
		PatchAnalyzer patchAnalyzer = new PatchAnalyzer();
		try {
			File file = new File(resultPath + "/" + filename + ".out");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(filename.getBytes());
			fos.write("\n".getBytes());
			File patch = new File(patchesPath + "/" + filename);
			Vector<DiffInfo> patchInf = null;
			try {
				patchInf = patchAnalyzer.anayPatch(patch);
				System.out.println("diff num: " + patchInf.size());
				/*
				 * if (patchInf.size() >= 10) { fos.write("too many diff"
				 * .getBytes()); fos.write("\n".getBytes()); fos.close();
				 * return; }
				 */
			} catch (Exception e) {
				e.printStackTrace();
				fos.write("exception occur!\n".getBytes());
				fos.close();
				return;
			}
			System.out.println("patch anaylz done!");
			// Get label result for parent version
			int status = resetRepo(preHash);
			if (status == -1) {
				fos.write(("can't reset to " + preHash + "\n").getBytes());
				fos.close();
				return;
			}
			Vector<String> preresult = getAnalyzeResult(patchInf, true);
			// Get label result for current version
			status = resetRepo(Hash);
			if (status == -1) {
				fos.write(("can't reset to" + Hash + "\n").getBytes());
				fos.close();
				return;
			}
			Vector<String> result = getAnalyzeResult(patchInf, false);
			// Compare label results for parent and current version
			int i = 0;
			for (DiffInfo diff : patchInf) {
				fos.write("diff --git ".getBytes());
				if (!diff.filename.equals("ev/null"))
					fos.write(diff.filename.getBytes());
				else if (!diff.prefilename.equals("ev/null"))
					fos.write(diff.prefilename.getBytes());
				else
					fos.write("dev/null".getBytes());
				/*
				 * if (diff.filename.endsWith(".java")) {
				 * fos.write("##".getBytes());
				 * fos.write(diff.superClass.getBytes()); }
				 */
				fos.write("\n".getBytes());
				int p = i;
				for (; i < p + diff.hunks.size(); i++) {
					fos.write("@@ ".getBytes());
					if (result.get(i).equals("no modify")) {
						fos.write(preresult.get(i).getBytes());
						fos.write("\n".getBytes());
					} else if (preresult.get(i).equals("no modify")) {
						fos.write(result.get(i).getBytes());
						fos.write("\n".getBytes());
					} else if (result.get(i).equals("in many methods") || preresult.get(i).equals("in many methods")) {
						fos.write("in many methods\n".getBytes());
					} else if (result.get(i).equals("not in a method") || preresult.get(i).equals("not in a method")) {
						fos.write("not in a method\n".getBytes());
					} else if (result.get(i).equals("diff exception occur!")
							|| preresult.get(i).equals("diff exception occur!")) {
						fos.write("diff exception occur!\n".getBytes());
					} else if (result.get(i).equals("hunk exception occur!")
							|| preresult.get(i).equals("hunk exception occur!")) {
						fos.write("hunk exception occur!\n".getBytes());
					} else if (result.get(i).equals(preresult.get(i))) {
						fos.write(result.get(i).getBytes());
						fos.write("\n".getBytes());
					} else if (result.get(i).equals("no a java file")) {
						fos.write(preresult.get(i).getBytes());
						fos.write("\n".getBytes());
					} else if (preresult.get(i).equals("no a java file")) {
						fos.write(result.get(i).getBytes());
						fos.write("\n".getBytes());
					} else {
						fos.write("not in the same method\n".getBytes());
					}
				}
			}
			fos.write("\n\n".getBytes());
			fos.close();
			javaParser.release();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Vector<String> getAnalyzeResult(Vector<DiffInfo> patchInfo, boolean forPreHash) {
		Vector<String> result = new Vector<String>();
		int hi = 0;
		System.out.println("parser make!");
		for (DiffInfo diff : patchInfo) {

			if (isDiffToMainSourceFile(diff.filename)) {
				try {
					if (projectName.equals("EclipseJDTCore")) {
						javaParser.init(repoPath + diff.filename);
					} else
						cppParser.init(repoPath + diff.prefilename,ParserLanguage.CPP);
					/*
					 * diff.superClass = jp.getSuperClass(); if (diff.superClass
					 * == null || diff.superClass.equals("java.lang.Object"))
					 * diff.superClass = "";
					 */
				} catch (Exception e) {
					e.printStackTrace();
					for (HunkInfo hunk : diff.hunks)
						result.add("diff exception occur!");
					continue;
				}
			}
			for (HunkInfo hunk : diff.hunks) {
				System.out.println(hi);
				hi++;
				if (hunk.line == -1) {
					result.add("hunk exception occur!");
					continue;
				}
				if (!isDiffToMainSourceFile(diff.filename)) {
					result.add("not main source file");
					continue;
				}
				String j = null;
				if (projectName.equals("EclipseJDTCore")) {
					if (forPreHash)
						j = javaParser.judge(hunk.preline, hunk.prelast);
					else
						j = javaParser.judge(hunk.line, hunk.last);
				} else {
					if (forPreHash)
						j = cppParser.judge(hunk.preline, hunk.prelast);
					else
						j = cppParser.judge(hunk.line, hunk.last);

				}

				result.add(j);
			}
		}
		return result;
	}

	private boolean isDiffToMainSourceFile(String changedFileName) {
		boolean res;
		String fileSuffix = changedFileName.substring(changedFileName.lastIndexOf("."));
		for (String mainFileSuffix : mainSourceFileSuffixList)
			if (mainFileSuffix.equals(fileSuffix))
				return true;
		return false;

	}

	private int resetRepo(String versionHash) {
		System.out.println("reset to " + versionHash + "\n");
		try {
			FileOutputStream fos = new FileOutputStream("reset.bat");
			String w = this.cmd + versionHash;
			fos.write(w.getBytes());
			fos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Runtime runtime = Runtime.getRuntime();
		try {
			Process p = runtime.exec("cmd /c reset.bat");

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
			InputStream is2 = p.getInputStream();
			BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
			StringBuilder buf = new StringBuilder();
			String line = null;
			while ((line = br2.readLine()) != null)
				buf.append(line); //
			System.out.println("reset to " + versionHash + "done!\n");
			if (buf.indexOf("HEAD is now at ") == -1)
				return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	public static void main(String[] args) {
		// Filter m = new Filter(args[0], args[1], args[2]);
		// Filter m = new Filter("23669_1.txt", "e6c002b", "d0bcc7f");
		// m.run();
		System.out.println("success");
	}
}
