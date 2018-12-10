// Following is the semantics class:
// The meaning M of a Statement is a State
// The meaning M of a Expression is a Value

public class Semantics
{
	// M 함수. 전체 프로그램의 M(Meaning) 반환.
	State M(Program p)
	{
		return M(p.body, initialState(p.decpart));
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
	
	// M 함수. 타입에 따라 호출.
	State M(Statement s, State state)
	{
		if (s instanceof Skip)
		{
			return M((Skip) s, state);
		}
		if (s instanceof Assignment)
		{
			return M((Assignment) s, state);
		}
		if (s instanceof Conditional)
		{
			return M((Conditional) s, state);
		}
		if (s instanceof Loop)
		{
			return M((Loop) s, state);
		}
		if (s instanceof Block)
		{
			return M((Block) s, state);
		}
		throw new IllegalArgumentException("should never reach here");
	}
	
	// skip 일 경우는 그냥 state 리턴
	State M(Skip s, State state)
	{
		return state;
	}
	
	// 대입일 경우는 source 의 결과를 target 으로 넣음.
	State M(Assignment a, State state)
	{
		return state.onion(a.target, M(a.source, state));
	}
	
	// 블록의 경우는 모든 statement 에 대해 M 호출.
	State M(Block b, State state)
	{
		for (Statement s : b.members)
		{
			state = M(s, state);
		}
		return state;
	}
	
	// 조건문의 경우는 조건문의 결과에 따라 조건문이 true 이면 then, 아니면 else.
	State M(Conditional c, State state)
	{
		if (M(c.test, state).boolValue())
		{
			return M(c.thenbranch, state);
		}
		else
		{
			return M(c.elsebranch, state);
		}
	}
	
	// 루프이면 조건문(test)가 true 일 경우 계속해서 body 를 계산.
	State M(Loop l, State state)
	{
		if (M(l.test, state).boolValue())
		{
			return M(l, M(l.body, state));
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
	Value M(Expression e, State state)
	{
		if (e instanceof Value)
		{
			return (Value) e;
		}
		if (e instanceof Variable)
		{
			return (Value) (state.get(e));
		}
		if (e instanceof Binary)
		{
			Binary b = (Binary) e;
			return applyBinary(b.op,
					M(b.term1, state), M(b.term2, state));
		}
		if (e instanceof Unary)
		{
			Unary u = (Unary) e;
			return applyUnary(u.op, M(u.term, state));
		}
		throw new IllegalArgumentException("should never reach here");
	}
	
	public static void main(String args[])
	{
		// Parser parser  = new Parser(new Lexer(args[0]));
		// 명령 인자방식이 아닌 직접 입력 방식 사용
		String fileName = "Test Programs/factorial.cpp";
		Parser parser = new Parser(new Lexer(fileName));
		
		Program prog = parser.program();
		prog.display();    // student exercise
		System.out.println("\nBegin type checking...");
		System.out.println("Type map:");
		
		TypeMap map = StaticTypeCheck.typing(prog.decpart);
		map.display();    // student exercise
		
		StaticTypeCheck.V(prog);
		Program out = TypeTransformer.T(prog, map);
		System.out.println("Output AST");
		out.display();    // student exercise
		
		Semantics semantics = new Semantics();
		State state = semantics.M(out);
		System.out.println("Final State");
		state.display();  // student exercise
	}
}
