import java.util.*;

class StateFrame
{
	Stack<State> stateStack = new Stack<>();
	
	public StateFrame()
	{
	}
	
	// State 추가
	public StateFrame pushState(State state)
	{
		stateStack.push(state);
		return this;
	}
	
	// State pop
	public StateFrame popState()
	{
		stateStack.pop();
		return this;
	}
	
	public State peekState()
	{
		return stateStack.peek();
	}
	
	public StateFrame put(Variable key, Value val)
	{
		State top = stateStack.peek();
		top.put(key, val);
		return this;
	}
	
	public StateFrame remove(Variable key)
	{
		State top = stateStack.peek();
		top.remove(key);
		return this;
	}
	
	public Value get(Variable key)
	{
		// 탑에 없으면 글로벌
		State top = stateStack.peek();
		Iterator<State> stateIterator = stateStack.listIterator();
		State global = stateIterator.next();
		
		if(top.get(key) != null)
		{
			return top.get(key);
		}
		else
		{
			return global.get(key);
		}
	}
	
	public StateFrame onion(Variable key, Value val)
	{
		// top 에서 찾아보고 없으면 글로벌 변수
		State top = stateStack.peek();
		Iterator<State> stateIterator = stateStack.listIterator();
		State global = stateIterator.next();
		
		if(top.get(key) != null)
		{
			top.put(key, val);
		}
		else
		{
			global.put(key, val);
		}
		
		return this;
	}
	
	public StateFrame onion(State t)
	{
		State top = stateStack.peek();
		for (Variable key : t.keySet())
		{
			top.put(key, t.get(key));
		}
		return this;
	}
	
	
	public void display()
	{
		Display.print(1, "Globals and top frame:");
		Display.print(1, "---------------------------------------");
		
		Iterator<State> stateIterator = stateStack.iterator();
		while (stateIterator.hasNext())
		{
			stateIterator.next().display();
			Display.print(2, "---------------------------------------");
		}
		
		Display.print(1, "---------------------------------------");
	}
}

public class State extends HashMap<Variable, Value>
{
	// Defines the set of variables and their associated values
	// that are active during interpretation
	
	public State()
	{
	}
	
	public State(Variable key, Value val)
	{
		put(key, val);
	}
	
	public State onion(Variable key, Value val)
	{
		put(key, val);
		return this;
	}
	
	public State onion(State t)
	{
		for (Variable key : t.keySet())
		{
			put(key, t.get(key));
		}
		return this;
	}
	
	
	public void display()
	{
		// 출력
		for (Map.Entry<Variable, Value> entry : entrySet())
		{
			Display.print(2, "<" + entry.getKey() + ", " + entry.getValue() + ">");
		}
	}
}
