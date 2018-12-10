import java.util.*;

public class State extends HashMap<Variable, Value> { 
    // Defines the set of variables and their associated values 
    // that are active during interpretation
    
    public State( ) { }
    
    public State(Variable key, Value val) {
        put(key, val);
    }
    
    public State onion(Variable key, Value val) {
        put(key, val);
        return this;
    }
    
    public State onion (State t) {
        for (Variable key : t.keySet( ))
            put(key, t.get(key));
        return this;
    }
	
	
	public void display()
	{
		String output = "{ ";
		
		// 출력
		for (Map.Entry<Variable, Value> entry : entrySet()) {
			output += "<" + entry.getKey() + ", " + entry.getValue() + ">, ";
		}
		
		Display.print(0,output.substring(0, Math.max(output.length() - 2, 1)) + " }");
	}
}
