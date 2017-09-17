package FixCollection.MethodLabel;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class CommitIDs 
{
	public Map<Integer, List<Node>> map = new HashMap<Integer, List<Node>>();
	public CommitIDs(String path)
	{
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(fs));
		String line;  
        try {
        	int count = 0;
			while ((line = reader.readLine()) != null) 
			{  
				String[] s = line.split("[ \t]+");
				if(s.length != 3)
				{
					count++;
					continue;
				}
				int bugid = Integer.parseInt(s[2]);
				String p = s[0];
				String h = s[1];
				Node n = new Node(p,h);
				if(!map.containsKey(bugid))
					map.put(bugid, new ArrayList<Node>());
				map.get(bugid).add(n);
			}
			fs.close();
			//System.out.println(count);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
}
