package FixCollection.MethodLabel.JavaParser.symboltable;

public class MParameter 
{
	public String type = null;
	public String name = null;
	public int startPosition;
	public MParameter(String type, String name, int startPosition)
	{
		this.type = type;
		this.name = name;
		this.startPosition = startPosition;
	}
	public int getStartPosition()
	{
		return startPosition;
	}
	public void setStartPosition(int startPosition)
	{
		this.startPosition = startPosition;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getName()
	{
		return this.name;
	}
	
	public void setType(String type)
	{
		this.type = type;
	}
	public String getType()
	{
		return this.type;
	}
}
