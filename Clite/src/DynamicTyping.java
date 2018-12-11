// Following is the semantics class
// for a dynamically typed language.
// The meaning M of a Statement is a State.
// The meaning M of a Expression is a Value.

public class DynamicTyping extends Semantics
{
	
	// M 함수. 전체 프로그램의 M(Meaning) 반환.
	State M(Program p)
	{
		return M(p.functions.getFunction(Token.mainTok.value()).body, initialState(p.globals), p.functions);
	}
	
	// Binary 연산자 적용.
	Value applyBinary(Operator op, Value v1, Value v2)
	{
		StaticTypeCheck.check(v1.type() == v2.type(),
				"mismatched types");
		if (op.ArithmeticOp())
		{
			if (v1.type() == Type.INT)
			{
				if (op.val.equals(Operator.PLUS))
				{
					return new IntValue(v1.intValue() + v2.intValue());
				}
				if (op.val.equals(Operator.MINUS))
				{
					return new IntValue(v1.intValue() - v2.intValue());
				}
				if (op.val.equals(Operator.TIMES))
				{
					return new IntValue(v1.intValue() * v2.intValue());
				}
				if (op.val.equals(Operator.DIV))
				{
					return new IntValue(v1.intValue() / v2.intValue());
				}
			}
			if(v1.type() == Type.FLOAT)
			{
				if (op.val.equals(Operator.PLUS))
				{
					return new FloatValue(v1.floatValue() + v2.floatValue());
				}
				if (op.val.equals(Operator.MINUS))
				{
					return new FloatValue(v1.floatValue() - v2.floatValue());
				}
				if (op.val.equals(Operator.TIMES))
				{
					return new FloatValue(v1.floatValue() * v2.floatValue());
				}
				if (op.val.equals(Operator.DIV))
				{
					return new FloatValue(v1.floatValue() / v2.floatValue());
				}
			}
			// student exercise
		}
		// 비교 연산자
		if(op.RelationalOp())
		{
			if (v1.type() == Type.INT)
			{
				if (op.val.equals(Operator.LT))
				{
					return new BoolValue(v1.intValue() < v2.intValue());
				}
				if (op.val.equals(Operator.LE))
				{
					return new BoolValue(v1.intValue() <= v2.intValue());
				}
				if (op.val.equals(Operator.EQ))
				{
					return new BoolValue(v1.intValue() == v2.intValue());
				}
				if (op.val.equals(Operator.NE))
				{
					return new BoolValue(v1.intValue() != v2.intValue());
				}
				if (op.val.equals(Operator.GT))
				{
					return new BoolValue(v1.intValue() > v2.intValue());
				}
				if (op.val.equals(Operator.GE))
				{
					return new BoolValue(v1.intValue() >= v2.intValue());
				}
			}
			if (v1.type() == Type.FLOAT)
			{
				if (op.val.equals(Operator.LT))
				{
					return new BoolValue(v1.floatValue() < v2.floatValue());
				}
				if (op.val.equals(Operator.LE))
				{
					return new BoolValue(v1.floatValue() <= v2.floatValue());
				}
				if (op.val.equals(Operator.EQ))
				{
					return new BoolValue(v1.floatValue() == v2.floatValue());
				}
				if (op.val.equals(Operator.NE))
				{
					return new BoolValue(v1.floatValue() != v2.floatValue());
				}
				if (op.val.equals(Operator.GT))
				{
					return new BoolValue(v1.floatValue() > v2.floatValue());
				}
				if (op.val.equals(Operator.GE))
				{
					return new BoolValue(v1.floatValue() >= v2.floatValue());
				}
			}
			if (v1.type() == Type.CHAR)
			{
				if (op.val.equals(Operator.LT))
				{
					return new BoolValue(v1.charValue() < v2.charValue());
				}
				if (op.val.equals(Operator.LE))
				{
					return new BoolValue(v1.charValue() <= v2.charValue());
				}
				if (op.val.equals(Operator.EQ))
				{
					return new BoolValue(v1.charValue() == v2.charValue());
				}
				if (op.val.equals(Operator.NE))
				{
					return new BoolValue(v1.charValue() != v2.charValue());
				}
				if (op.val.equals(Operator.GT))
				{
					return new BoolValue(v1.charValue() > v2.charValue());
				}
				if (op.val.equals(Operator.GE))
				{
					return new BoolValue(v1.charValue() >= v2.charValue());
				}
			}
			if (v1.type() == Type.BOOL)
			{
				if (op.val.equals(Operator.EQ))
				{
					return new BoolValue(v1.boolValue() == v2.boolValue());
				}
				if (op.val.equals(Operator.NE))
				{
					return new BoolValue(v1.boolValue() != v2.boolValue());
				}
			}
		}
		// student exercise
		throw new IllegalArgumentException("should never reach here");
	}
	
	// Unary 연산자 적용.
	Value applyUnary(Operator op, Value v)
	{
		if (op.val.equals(Operator.NOT))
		{
			return new BoolValue(!v.boolValue());
		}
		else if (op.val.equals(Operator.NEG))
		{
			return new IntValue(-v.intValue());
		}
		else if (op.val.equals(Operator.NEG))
		{
			return new FloatValue(-v.floatValue());
		}
		else if (op.val.equals(Operator.FLOAT))
		{
			return new FloatValue((float) (v.intValue()));
		}
		else if (op.val.equals(Operator.INT))
		{
			return new IntValue((int) (v.floatValue()));
		}
		else if (op.val.equals(Operator.INT))
		{
			return new IntValue((int) (v.charValue()));
		}
		else if (op.val.equals(Operator.CHAR))
		{
			return new CharValue((char) (v.intValue()));
		}
		throw new IllegalArgumentException("should never reach here");
	}
	
	// Expression 에 M 적용
	Value M(Expression e, State sigma)
	{
		if (e instanceof Value)
		{
			return (Value) e;
		}
		// 변수일 경우 state 에 존재하는지 확인.
		if (e instanceof Variable)
		{
			StaticTypeCheck.check(sigma.containsKey(e),
					"reference to undefined variable");
			return (Value) (sigma.get(e));
		}
		if (e instanceof Binary)
		{
			Binary b = (Binary) e;
			return applyBinary(b.op,
					M(b.term1, sigma), M(b.term2, sigma));
		}
		if (e instanceof Unary)
		{
			Unary u = (Unary) e;
			return applyUnary(u.op, M(u.term, sigma));
		}
		throw new IllegalArgumentException("should never reach here");
	}
	
	public static void main(String args[])
	{
		// Parser parser  = new Parser(new Lexer(args[0]));
		// 명령 인자방식이 아닌 직접 입력 방식 사용
		String fileName = "../Test Programs/dyn-fact.cpp";
		Parser parser = new Parser(new Lexer(fileName));
		
		Program prog = parser.program();
		prog.display();    // student exercise
		DynamicTyping dynamic = new DynamicTyping();
		State state = dynamic.M(prog);
		System.out.println("Final State");
		state.display();   // student exercise
	}
}
