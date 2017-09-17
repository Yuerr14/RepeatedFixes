package FixCollection.MethodLabel.JavaParser.symboltable;


import java.util.*;

import org.eclipse.jdt.core.dom.ASTNode;

public class MMethod 
{
	public List<String> modifiers = new ArrayList<String>();
	public List<MParameter> parameters;
	public String return_value;
	public String name;
	public int startPosition;
	public int bodystartPosition;
	public int endPosition;
	public MMethod(List<ASTNode> v_modifiers, String v_return, String v_name, List<ASTNode> v_parameters, int v_startPosition, int v_bodystartPosition, int endPosition)
	{
		modifiers = new ArrayList<String>();
		for(ASTNode node: v_modifiers)
		{
			modifiers.add(node.toString());
		}
		return_value = v_return;
		name = v_name;
		parameters = new ArrayList<MParameter>();
		for(ASTNode node: v_parameters)
		{
			String para = node.toString();
			String type = para.substring(0,para.indexOf(" "));
			String name = para.substring(para.indexOf(" ")+1);
			int startPosition = node.getStartPosition();
			parameters.add(new MParameter(type,name,startPosition));
		}
		startPosition = v_startPosition;
		bodystartPosition = v_bodystartPosition;
		this.endPosition = endPosition;
	}
}
