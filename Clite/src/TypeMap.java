import java.util.*;

public class TypeMap extends HashMap<Variable, Type> {

// TypeMap is implemented as a Java HashMap.  
// Plus a 'display' method to facilitate experimentation.
	
	// 출력
	public void display()
	{
		String output = "{ ";
		
		// 출력
		for (Map.Entry<Variable, Type> entry : entrySet()) {
			output += "<" + entry.getKey() + ", " + entry.getValue() + ">, ";
		}
		
	 	Display.print(0,output.substring(0, Math.max(output.length() - 2, 1)) + " }");
	}
}
