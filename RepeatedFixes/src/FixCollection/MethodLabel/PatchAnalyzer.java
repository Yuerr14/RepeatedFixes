package FixCollection.MethodLabel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

class DiffInfo {
	String prefilename;
	String filename;
	//String superClass;
	Vector<HunkInfo> hunks;

	public DiffInfo(String pf, String f) {
		hunks = new Vector<HunkInfo>();
		prefilename = pf;
		filename = f;
	}

	public void add(HunkInfo h) {
		hunks.add(h);
	}

	public HunkInfo get(int i) {
		return hunks.get(i);
	}
}

class HunkInfo {
	int preline;
	int prelast;
	int line;
	int last;

	public HunkInfo(int pl, int pla, int l, int la) {
		preline = pl;
		prelast = pla;
		line = l;
		last = la;
	}
}

public class PatchAnalyzer {
	public void anayHunk(String hunk, DiffInfo diffInf) {
		// System.out.println(hunk);
		String numInf = hunk.substring(0, hunk.indexOf(" @@"));
		String[] nums = numInf.split("[^0-9]");
		int pl, pla, l, la;

		pl = Integer.valueOf(nums[0]);
		pla = Integer.valueOf(nums[1]);
		l = Integer.valueOf(nums[3]);
		la = Integer.valueOf(nums[4]);

		String[] lines = hunk.split("\n");
		int plus_count = 0;
		int minus_count = 0;
		boolean inEdit = false;
		int temppluscount = 0;
		int tempminuscount = 0;
		int editprefirst = 0;
		int editprelast = 0;
		int editafterfirst = 0;
		int editafterlast = 0;
		for (int i = 1; i < lines.length; i++) {
			if (!inEdit) {
				editprefirst = pl + i - 1 - plus_count;
				editprelast = pl + i - 1 - plus_count;
				editafterfirst = l + i - 1 - minus_count;
				editafterlast = l + i - 1 - minus_count;
			}
			String line = lines[i];
			if (line.startsWith("+") || line.startsWith("-"))
				inEdit = true;
			else
				inEdit = false;
			if (line.startsWith("+")) {
				if (inEdit && temppluscount == 0) {
					editafterfirst = l + i - 1 - minus_count;
				}
				plus_count++;
				temppluscount++;
				editafterlast = l + i - 1 - minus_count;
			}
			if (line.startsWith("-")) {
				if (inEdit && tempminuscount == 0) {
					editprefirst = pl + i - 1 - plus_count;
				}
				minus_count++;
				tempminuscount++;
				editprelast = pl + i - 1 - plus_count;
			}
			if ((inEdit == false && ((tempminuscount > 0) || (temppluscount > 0)))) {
				editprelast = editprelast - editprefirst + 1;
				editafterlast = editafterlast - editafterfirst + 1;
				if (temppluscount == 0)
					editafterlast = 0;
				if (tempminuscount == 0)
					editprelast = 0;
				HunkInfo hunkInf = new HunkInfo(editprefirst, editprelast, editafterfirst, editafterlast);
				diffInf.add(hunkInf);
				tempminuscount = 0;
				temppluscount = 0;
			}
		}

	}

	public DiffInfo anayDiff(String diff) {
		String tdiff = diff;
		String before = tdiff.substring(tdiff.indexOf("--- ") + 6, tdiff.indexOf("+++ ") - 1);
		String after = tdiff.substring(tdiff.indexOf("+++ ") + 6, tdiff.indexOf("\n@@"));
		DiffInfo diffInf = new DiffInfo(before, after);
		String[] hunks = tdiff.split("@@ -");
		for (int i = 1; i < hunks.length; i++) {
			try {
				anayHunk(hunks[i], diffInf);
			} catch (Exception e) {
				diffInf.add(new HunkInfo(-1, -1, -1, -1));
			}
			// diffInf.add(hunk);
		}
		return diffInf;
	}

	public Vector<DiffInfo> anayPatch(File patch) {
		Vector<DiffInfo> patchInf = new Vector<DiffInfo>();
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(patch);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(fs));
		StringBuffer sb = new StringBuffer();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] diffs = sb.toString().split("diff --git ");
		for (String diff : diffs) {
			if (diff.indexOf("+++ ") == -1)
				continue;
			patchInf.add(anayDiff(diff));
		}
		return patchInf;
	}

}
