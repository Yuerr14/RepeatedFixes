package FixCollection.MethodLabel.JavaParser;


import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import FixCollection.MethodLabel.JavaParser.symboltable.MMethod;
import FixCollection.MethodLabel.JavaParser.symboltable.MTable;


public class BuildSymbolTableVisitor extends ASTVisitor
{
	public MTable table;
	public BuildSymbolTableVisitor(){
		table=new MTable();
	}

	public boolean visit(MethodDeclaration node) 
	{
		List<ASTNode> modifiers = node.modifiers();
		String return_value = null; 
		try{
			return_value = node.getReturnType2().toString();
		}catch(NullPointerException e){}
		String name = node.getName().toString();
		List<ASTNode> parameters = node.parameters();
		int endPosition = node.getStartPosition() + node.getLength();
		int bodyPosition = node.getStartPosition();
		try
		{
			bodyPosition = node.getBody().getStartPosition();
		}
		catch(Exception e)
		{
			
		}
		System.out.println("MMethod:"+modifiers+" "+return_value+" "+name+" "+parameters+" "+node.getStartPosition()+" "+bodyPosition+" "+endPosition);
		MMethod method=new MMethod(modifiers, return_value,  name, parameters, node.getStartPosition(), bodyPosition, endPosition);
		table.methods.add(method);
		return true;
	}
	
}
