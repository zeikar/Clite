// StaticTypeCheck.java

import java.util.*;

// Static type checking for Clite is defined by the functions 
// V and the auxiliary functions typing and typeOf.  These
// functions use the classes in the Abstract Syntax of Clite.


public class StaticTypeCheck
{
	
	// Declarations 으로부터 맵 생성
	public static TypeMap typing(Declarations d)
	{
		TypeMap map = new TypeMap();
		for (Declaration di : d)
		{
			map.put(di.v, di.t);
		}
		return map;
	}
	
	// test 가 false 이면 에러 메시지 출력 후 종료.
	public static void check(boolean test, String msg)
	{
		if (test)
		{
			return;
		}
		System.err.println(msg);
		// 추가 구현 과제!!!!!!!!
		// 바로 종료 안함.
		//System.exit(1);
	}
	
	// Declarations 체크, 중복 선언 있는지 확인
	public static void V(Declarations d)
	{
		for (int i = 0; i < d.size() - 1; i++)
		{
			for (int j = i + 1; j < d.size(); j++)
			{
				Declaration di = d.get(i);
				Declaration dj = d.get(j);
				check(!(di.v.equals(dj.v)),
						"duplicate declaration: " + dj.v);
			}
		}
	}
	
	// 전체 프로그램 체크.
	// Declarations, body 부분 체크.
	public static void V(Program p)
	{
		V(p.decpart);
		V(p.body, typing(p.decpart));
	}
	
	// Expression 의 타입을 tm(TypeMap)에서 가져온다.
	public static Type typeOf(Expression e, TypeMap tm)
	{
		// 값이면 그 값의 타입을 반환
		if (e instanceof Value)
		{
			return ((Value) e).type;
		}
		// 변수이면 변수의 타입을 반환.
		if (e instanceof Variable)
		{
			Variable v = (Variable) e;
			// 선언되지 않은 변수는 에러.
			check(tm.containsKey(v), "undefined variable: " + v);
			return (Type) tm.get(v);
		}
		// Binary 연산자
		if (e instanceof Binary)
		{
			Binary b = (Binary) e;
			// +, -, *, /
			if (b.op.ArithmeticOp())
			{
				if (typeOf(b.term1, tm) == Type.FLOAT)
				{
					return (Type.FLOAT);
				}
				else
				{
					return (Type.INT);
				}
			}
			// &&, ||, <, >, <=, >=, ==, != 등의 연산자는 불 타입.
			if (b.op.RelationalOp() || b.op.BooleanOp())
			{
				return (Type.BOOL);
			}
		}
		// Unary 연산자.
		if (e instanceof Unary)
		{
			Unary u = (Unary) e;
			// !
			if (u.op.NotOp())
			{
				return (Type.BOOL);
			}
			// -
			else if (u.op.NegateOp())
			{
				return typeOf(u.term, tm);
			}
			// int 캐스팅
			else if (u.op.intOp())
			{
				return (Type.INT);
			}
			// float 캐스팅
			else if (u.op.floatOp())
			{
				return (Type.FLOAT);
			}
			// char 캐스팅
			else if (u.op.charOp())
			{
				return (Type.CHAR);
			}
		}
		throw new IllegalArgumentException("should never reach here");
	}
	
	// Expression 을 체크.
	public static void V(Expression e, TypeMap tm)
	{
		// Value. 체크할 것이 없음.
		if (e instanceof Value)
		{
			return;
		}
		// 변수이면 선언 되었는지 체크
		if (e instanceof Variable)
		{
			Variable v = (Variable) e;
			check(tm.containsKey(v)
					, "undeclared variable: " + v);
			return;
		}
		// Binary 연산자.
		if (e instanceof Binary)
		{
			Binary b = (Binary) e;
			Type typ1 = typeOf(b.term1, tm);
			Type typ2 = typeOf(b.term2, tm);
			
			// 각각을 체크
			V(b.term1, tm);
			V(b.term2, tm);
			
			// +, -, *, /
			if (b.op.ArithmeticOp())
			{
				check(typ1 == typ2 &&
								(typ1 == Type.INT || typ1 == Type.FLOAT)
						, "type error for " + b.op);
			}
			// <, >, <=, >=, ==, !=
			else if (b.op.RelationalOp())
			{
				check(typ1 == typ2, "type error for " + b.op);
			}
			// &&, ||
			else if (b.op.BooleanOp())
			{
				check(typ1 == Type.BOOL && typ2 == Type.BOOL,
						b.op + ": non-bool operand");
			}
			else
			{
				throw new IllegalArgumentException("should never reach here");
			}
			return;
		}
		
		// student exercise
		// Unary 연산자.
		else if(e instanceof Unary)
		{
			Unary u = (Unary) e;
			
			Type t = typeOf(u.term, tm);
			
			V(u.term, tm);
			
			// ! 연산자
			// 불 타입이어야 함.
			if (u.op.NotOp())
			{
				check(t == Type.BOOL,
						u.op + ": non-bool operand");
			}
			// - 연산자
			// float, int 에만 사용 가능.
			else if (u.op.NegateOp())
			{
				check(t == Type.INT && t == Type.FLOAT,
						u.op + ": non-int or non-float operand");
			}
			// int 캐스팅 연산자.
			// float, char 에만 사용 가능.
			else if (u.op.intOp())
			{
				check(t == Type.FLOAT || t == Type.CHAR,
						u.op + ": non-float or non-char operand");
			}
			// float 캐스팅 연산자.
			// int 에만 사용 가능.
			else if (u.op.floatOp())
			{
				check(t == Type.INT,
						u.op + ": non-int operand");
			}
			// char 캐스팅 연산자.
			// int 에만 사용 가능.
			else if (u.op.charOp())
			{
				check(t == Type.INT,
						u.op + ": non-int operand");
			}
			else
			{
				throw new IllegalArgumentException("should never reach here");
			}
			return;
		}
		throw new IllegalArgumentException("should never reach here");
	}
	
	// Statement 검증
	public static void V(Statement s, TypeMap tm)
	{
		// null 이면 에러
		if (s == null)
		{
			throw new IllegalArgumentException("AST error: null statement");
		}
		// Skip (;)이면 ㅇㅋ
		if (s instanceof Skip)
		{
			return;
		}
		// Assignment
		if (s instanceof Assignment)
		{
			Assignment a = (Assignment) s;
			// 선언 되어있는지 체크
			check(tm.containsKey(a.target)
					, "undefined target in assignment: " + a.target);
			V(a.source, tm);
			Type ttype = (Type) tm.get(a.target);
			Type srctype = typeOf(a.source, tm);
			
			// target, source 서로 다를 경우
			if (ttype != srctype)
			{
				// int 를 float 에 대입
				if (ttype == Type.FLOAT)
				{
					check(srctype == Type.INT
							, "mixed mode assignment to " + a.target);
				}
				// char 를 int 에 대입
				else if (ttype == Type.INT)
				{
					check(srctype == Type.CHAR
							, "mixed mode assignment to " + a.target);
				}
				// 그 외는 에러. (float 를 int 에 대입 등)
				else
				{
					check(false
							, "mixed mode assignment to " + a.target);
				}
			}
			return;
		}
		
		// student exercise
		// Block 검증. (Statement 여러개)
		if (s instanceof Block)
		{
			Block b = (Block) s;
			
			// Block 내의 모든 Statement 검증
			for (Statement statement : b.members)
			{
				V(statement, tm);
			}
			
			return;
		}
		// Conditional
		if (s instanceof Conditional)
		{
			Conditional c = (Conditional) s;
			
			// 조건문, then, else 각각을 V
			V(c.test, tm);
			V(c.thenbranch, tm);
			V(c.elsebranch, tm);
			
			return;
		}
		// Loop
		if (s instanceof Loop)
		{
			Loop l = (Loop) s;
			
			// 조건문과 body 각각을 V
			V(l.test, tm);
			V(l.body, tm);
			
			return;
		}
		throw new IllegalArgumentException("should never reach here");
	}
	
	public static void main(String args[])
	{
		//Parser parser  = new Parser(new Lexer(args[0]));
		// 명령 인자방식이 아닌 직접 입력 방식 사용
		String fileName = "../Test Programs/clone.cpp";
		Parser parser = new Parser(new Lexer(fileName));
		
		// 프로그램 파싱
		Program prog = parser.program();
		prog.display();           // student exercise
		
		// Type Map 출력, 타입 체킹.
		System.out.println("\nBegin type checking...");
		System.out.println("Type map:");
		TypeMap map = typing(prog.decpart);
		map.display();   // student exercise
		V(prog);
	} //main
	
} // class StaticTypeCheck

