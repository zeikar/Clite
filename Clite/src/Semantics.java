// Following is the semantics class:
// The meaning M of a Statement is a State
// The meaning M of a Expression is a Value

import java.util.ArrayList;
import java.util.Iterator;

public class Semantics
{
	// M 함수. 전체 프로그램의 M(Meaning) 반환.
	StateFrame M(Program p)
	{
		StateFrame stateFrame = new StateFrame();
		try
		{
			// main 함수부터 인터프리터 시작.
			stateFrame.pushState(initialState(p.globals));
			
			stateFrame = M(new Call("main", new ArrayList<>()), stateFrame, p.functions);
			// 마지막 State pop
			stateFrame.popState();
			
			return stateFrame;
		}
		// 뭔가 에러 발생
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("There are some errors. Please fix them!");
		}
		return stateFrame;
	}
	
	// 초기 상태.
	// 일단은 undef 로 초기화
	State initialState(Declarations d)
	{
		State state = new State();
		Value intUndef = new IntValue();
		for (Declaration decl : d)
		{
			state.put(decl.v, Value.mkValue(decl.t));
		}
		return state;
	}
	
	// 함수 Call interpret
	StateFrame M(Call call, StateFrame stateFrame, Functions functions)
	{
		// Call 하는 함수 가져옴.
		Function function = functions.getFunction(call.name);
		
		// 새로운 State
		State newState = new State();
		
		// 로컬 변수 추가.
		for (Declaration declaration : function.locals)
		{
			newState.put(declaration.v, Value.mkValue(declaration.t));
		}
		
		// 매개변수와 파라미터의 이터레이터.
		// 각각을 매핑
		Iterator<Expression> argIt = call.args.iterator();
		Iterator<Declaration> funcIt = function.params.iterator();
		while (argIt.hasNext())
		{
			Expression expression = argIt.next();
			Declaration declaration = funcIt.next();
			// 매개변수 값 계산.
			Value value = M(expression, stateFrame, functions);
			// 파라미터에 넣음.
			newState.put(declaration.v, value);
		}
		
		// 추가
		stateFrame.pushState(newState);
		
		// 현재 함수도 State 에 넣음
		// main 의 경우는 넣지 않는다.
		if (!call.name.equals(Token.mainTok.value()))
		{
			stateFrame.put(new Variable(call.name), Value.mkValue(functions.getFunction(call.name).t));
		}
		
		// Call Display
		Display.print(0, "Calling " + call.name);
		stateFrame.display();
		
		// 함수 body 의 모든 Statement 계산.
		Iterator<Statement> members = function.body.members.iterator();
		while (members.hasNext())
		{
			Statement statement = members.next();
			
			// 다른 Statement 에서 리턴했으면 함수 이름이 있음
			if(stateFrame.get(new Variable(call.name)) != null && !stateFrame.get(new Variable(call.name)).isUndef())
			{
				Display.print(0, "Returning " + call.name);
				stateFrame.display();
				
				return stateFrame;
			}
			
			// 리턴이면 함수 종료.
			if (statement instanceof Return)
			{
				Return r = (Return) statement;
				// 리턴할 값 계산.
				Value returnValue = M(r.result, stateFrame, functions);
				// 삽입
				stateFrame.put(r.target, returnValue);
				
				Display.print(0, "Returning " + call.name);
				stateFrame.display();
				
				return stateFrame;
			}
			// 아니면 Statement 계산
			else
			{
				stateFrame = M(statement, stateFrame, functions);
			}
		}
		
		// Display
		Display.print(0, "Returning " + call.name);
		stateFrame.display();
		
		return stateFrame;
	}
	
	// M 함수. 타입에 따라 호출.
	StateFrame M(Statement s, StateFrame state, Functions functions)
	{
		if (s instanceof Skip)
		{
			return M((Skip) s, state, functions);
		}
		if (s instanceof Assignment)
		{
			return M((Assignment) s, state, functions);
		}
		if (s instanceof Conditional)
		{
			return M((Conditional) s, state, functions);
		}
		if (s instanceof Loop)
		{
			return M((Loop) s, state, functions);
		}
		if (s instanceof Block)
		{
			return M((Block) s, state, functions);
		}
		// Statement Call
		// -> void function
		if (s instanceof Call)
		{
			Call call = (Call) s;
			// 함수 이름 State 제거
			state = M(call, state, functions);
			state.popState();
			return state;
		}
		// Return!!!
		if (s instanceof Return)
		{
			Return r = (Return) s;
			
			// 이미 리턴했으면 무시
			if(state.get(r.target) != null && !state.get(r.target).isUndef())
			{
				return state;
			}
			// 리턴할 값 계산.
			Value returnValue = M(r.result, state, functions);
			// 삽입
			state.put(r.target, returnValue);
			
			return state;
		}
		throw new IllegalArgumentException("should never reach here");
	}
	
	// skip 일 경우는 그냥 state 리턴
	StateFrame M(Skip s, StateFrame state, Functions functions)
	{
		return state;
	}
	
	// 대입일 경우는 source 의 결과를 target 으로 넣음.
	StateFrame M(Assignment a, StateFrame state, Functions functions)
	{
		return state.onion(a.target, M(a.source, state, functions));
	}
	
	// 블록의 경우는 모든 statement 에 대해 M 호출.
	StateFrame M(Block b, StateFrame state, Functions functions)
	{
		for (Statement s : b.members)
		{
			state = M(s, state, functions);
		}
		return state;
	}
	
	// 조건문의 경우는 조건문의 결과에 따라 조건문이 true 이면 then, 아니면 else.
	StateFrame M(Conditional c, StateFrame state, Functions functions)
	{
		if (M(c.test, state, functions).boolValue())
		{
			return M(c.thenbranch, state, functions);
		}
		else
		{
			return M(c.elsebranch, state, functions);
		}
	}
	
	// 루프이면 조건문(test)가 true 일 경우 계속해서 body 를 계산.
	StateFrame M(Loop l, StateFrame state, Functions functions)
	{
		if (M(l.test, state, functions).boolValue())
		{
			return M(l, M(l.body, state, functions), functions);
		}
		else
		{
			return state;
		}
	}
	
	// Binary 연산자 적용.
	Value applyBinary(Operator op, Value v1, Value v2)
	{
		StaticTypeCheck.check(!v1.isUndef() && !v2.isUndef(),
				"reference to undef value");
		if (op.val.equals(Operator.INT_PLUS))
		{
			return new IntValue(v1.intValue() + v2.intValue());
		}
		if (op.val.equals(Operator.INT_MINUS))
		{
			return new IntValue(v1.intValue() - v2.intValue());
		}
		if (op.val.equals(Operator.INT_TIMES))
		{
			return new IntValue(v1.intValue() * v2.intValue());
		}
		if (op.val.equals(Operator.INT_DIV))
		{
			return new IntValue(v1.intValue() / v2.intValue());
		}
		
		// float 연산자.
		if (op.val.equals(Operator.FLOAT_PLUS))
		{
			return new FloatValue(v1.floatValue() + v2.floatValue());
		}
		if (op.val.equals(Operator.FLOAT_MINUS))
		{
			return new FloatValue(v1.floatValue() - v2.floatValue());
		}
		if (op.val.equals(Operator.FLOAT_TIMES))
		{
			return new FloatValue(v1.floatValue() * v2.floatValue());
		}
		if (op.val.equals(Operator.FLOAT_DIV))
		{
			return new FloatValue(v1.floatValue() / v2.floatValue());
		}
		
		// bool &&, || 연산자
		if (op.val.equals(Operator.AND))
		{
			return new BoolValue(v1.boolValue() && v2.boolValue());
		}
		if (op.val.equals(Operator.OR))
		{
			return new BoolValue(v1.boolValue() || v2.boolValue());
		}
		
		// int 비교 연산자
		if (op.val.equals(Operator.INT_LT))
		{
			return new BoolValue(v1.intValue() < v2.intValue());
		}
		if (op.val.equals(Operator.INT_LE))
		{
			return new BoolValue(v1.intValue() <= v2.intValue());
		}
		if (op.val.equals(Operator.INT_EQ))
		{
			return new BoolValue(v1.intValue() == v2.intValue());
		}
		if (op.val.equals(Operator.INT_NE))
		{
			return new BoolValue(v1.intValue() != v2.intValue());
		}
		if (op.val.equals(Operator.INT_GT))
		{
			return new BoolValue(v1.intValue() > v2.intValue());
		}
		if (op.val.equals(Operator.INT_GE))
		{
			return new BoolValue(v1.intValue() >= v2.intValue());
		}
		
		// float 비교 연산자
		if (op.val.equals(Operator.FLOAT_LT))
		{
			return new BoolValue(v1.floatValue() < v2.floatValue());
		}
		if (op.val.equals(Operator.FLOAT_LE))
		{
			return new BoolValue(v1.floatValue() <= v2.floatValue());
		}
		if (op.val.equals(Operator.FLOAT_EQ))
		{
			return new BoolValue(v1.floatValue() == v2.floatValue());
		}
		if (op.val.equals(Operator.FLOAT_NE))
		{
			return new BoolValue(v1.floatValue() != v2.floatValue());
		}
		if (op.val.equals(Operator.FLOAT_GT))
		{
			return new BoolValue(v1.floatValue() > v2.floatValue());
		}
		if (op.val.equals(Operator.FLOAT_GE))
		{
			return new BoolValue(v1.floatValue() >= v2.floatValue());
		}
		
		// char 비교 연산자
		if (op.val.equals(Operator.CHAR_LT))
		{
			return new BoolValue(v1.charValue() < v2.charValue());
		}
		if (op.val.equals(Operator.CHAR_LE))
		{
			return new BoolValue(v1.charValue() <= v2.charValue());
		}
		if (op.val.equals(Operator.CHAR_EQ))
		{
			return new BoolValue(v1.charValue() == v2.charValue());
		}
		if (op.val.equals(Operator.CHAR_NE))
		{
			return new BoolValue(v1.charValue() != v2.charValue());
		}
		if (op.val.equals(Operator.CHAR_GT))
		{
			return new BoolValue(v1.charValue() > v2.charValue());
		}
		if (op.val.equals(Operator.CHAR_GE))
		{
			return new BoolValue(v1.charValue() >= v2.charValue());
		}
		
		// bool 비교 연산자
		if (op.val.equals(Operator.BOOL_LT))
		{
			//return new BoolValue(v1.boolValue() < v2.boolValue());
		}
		if (op.val.equals(Operator.BOOL_LE))
		{
			//return new BoolValue(v1.boolValue() <= v2.boolValue());
		}
		if (op.val.equals(Operator.BOOL_EQ))
		{
			return new BoolValue(v1.boolValue() == v2.boolValue());
		}
		if (op.val.equals(Operator.BOOL_NE))
		{
			return new BoolValue(v1.boolValue() != v2.boolValue());
		}
		if (op.val.equals(Operator.BOOL_GT))
		{
			//return new BoolValue(v1.boolValue() > v2.boolValue());
		}
		if (op.val.equals(Operator.BOOL_GE))
		{
			//return new BoolValue(v1.boolValue() >= v2.boolValue());
		}
		
		throw new IllegalArgumentException("should never reach here");
	}
	
	// Unary 연산자 적용.
	Value applyUnary(Operator op, Value v)
	{
		StaticTypeCheck.check(!v.isUndef(),
				"reference to undef value");
		if (op.val.equals(Operator.NOT))
		{
			return new BoolValue(!v.boolValue());
		}
		else if (op.val.equals(Operator.INT_NEG))
		{
			return new IntValue(-v.intValue());
		}
		else if (op.val.equals(Operator.FLOAT_NEG))
		{
			return new FloatValue(-v.floatValue());
		}
		else if (op.val.equals(Operator.I2F))
		{
			return new FloatValue((float) (v.intValue()));
		}
		else if (op.val.equals(Operator.F2I))
		{
			return new IntValue((int) (v.floatValue()));
		}
		else if (op.val.equals(Operator.C2I))
		{
			return new IntValue((int) (v.charValue()));
		}
		else if (op.val.equals(Operator.I2C))
		{
			return new CharValue((char) (v.intValue()));
		}
		throw new IllegalArgumentException("should never reach here");
	}
	
	// Expression 계산. 각각에 대해 M 함수 호출.
	Value M(Expression e, StateFrame state, Functions functions)
	{
		if (e instanceof Value)
		{
			return (Value) e;
		}
		if (e instanceof Variable)
		{
			return (Value) (state.get((Variable) e));
		}
		if (e instanceof Binary)
		{
			Binary b = (Binary) e;
			return applyBinary(b.op,
					M(b.term1, state, functions), M(b.term2, state, functions));
		}
		if (e instanceof Unary)
		{
			Unary u = (Unary) e;
			return applyUnary(u.op, M(u.term, state, functions));
		}
		// Call
		if (e instanceof Call)
		{
			Call c = (Call) e;
			// Call 함수 실행
			state = M(c, state, functions);
			// 리턴 받고 삭제
			Value returnValue = state.get(new Variable(c.name));
			state.popState();
			return returnValue;
		}
		throw new IllegalArgumentException("should never reach here");
	}
	
	public static void main(String args[])
	{
		// Parser parser  = new Parser(new Lexer(args[0]));
		// 명령 인자방식이 아닌 직접 입력 방식 사용
		String fileName = "../Test Programs/recFib.cpp";
		Parser parser = new Parser(new Lexer(fileName));
		
		Program prog = parser.program();
		prog.display();    // student exercise
		System.out.println("\nBegin type checking...");
		System.out.println("Type map:");
		
		TypeMap map = StaticTypeCheck.typing(prog.globals);
		map.display();    // student exercise
		
		StaticTypeCheck.V(prog);
		Program out = TypeTransformer.T(prog, map);
		System.out.println("Output AST");
		out.display();    // student exercise
		
		Semantics semantics = new Semantics();
		StateFrame state = semantics.M(out);
		System.out.println("Final State");
		state.display();  // student exercise
	}
}
