import java.util.*;

public class TypeMap extends HashMap<Variable, Type>
{

// TypeMap is implemented as a Java HashMap.  
// Plus a 'display' method to facilitate experimentation.
	
	// 출력
	public void display()
	{
		String output = "{ ";
		
		// 출력
		for (Map.Entry<Variable, Type> entry : entrySet())
		{
			output += "<" + entry.getKey() + ", " + entry.getValue() + ">, ";
		}
		
		Display.print(0, output.substring(0, Math.max(output.length() - 2, 1)) + " }");
	}
	
	// 함수와 같이 출력하는 용
	public void display(Functions functions)
	{
		Display.print(0, "{");
		
		// 출력
		for (Map.Entry<Variable, Type> entry : entrySet())
		{
			Display.print(1, "<" + entry.getKey() + ", " + entry.getValue() + ">,");
		}
		
		// 함수
		for (Function function : functions)
		{
			Display.print(1, "<" + function.id + ", " + function.t + ", " + function.params.toString() + ">");
		}
		
		Display.print(0, "}");
	}
}
