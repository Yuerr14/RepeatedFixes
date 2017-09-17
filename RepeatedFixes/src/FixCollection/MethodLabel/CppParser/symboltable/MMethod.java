package FixCollection.MethodLabel.CppParser.symboltable;

import java.util.List;
import java.util.Vector;



public class MMethod 
{
	public int startOffset;
	public int endOffset;
	public int length;
	public int bodyStart;
	public List<String> declarator;
	public List<String> body;
	public MMethod(int start, int length, int bodyStart, List<String> decl, List<String> body)
	{
		this.startOffset = start;
		this.endOffset = start + length;
		this.bodyStart = bodyStart;
		this.length = length;
		this.declarator = decl;
		this.body = body;
	}
}
