import com.sun.org.apache.regexp.internal.RE;

import java.util.*;

public class TypeTransformer
{
	static boolean errorOccurred = false;
	
	// 에러 출력
	public static void error(String msg)
	{
		errorOccurred = true;
		System.err.println(msg);
	}
	
	// 전체 프로그램 Transform
	public static Program T(Program p, TypeMap tm)
	{
		// 전역 변수 TypeMap
		TypeMap globalMap = StaticTypeCheck.typing(p.globals);
		Functions functions = p.functions;
		
		// Transform 후의 Functions
		Functions transformedFunctions = new Functions();
		
		// 모든 함수에 대해 transform 진행.
		for (Function f : p.functions)
		{
			// 매개변수, 로컬변수 파라미터
			TypeMap newMap = new TypeMap();
			newMap.putAll(globalMap);
			newMap.putAll(StaticTypeCheck.typing(f.params));
			newMap.putAll(StaticTypeCheck.typing(f.locals));
			
			// 함수의 body Transform
			Block transformedBody = (Block) T(f.body, newMap, functions);
			
			// Function Transform
			transformedFunctions.add(new Function(f.t, f.id, f.params, f.locals, transformedBody));
		}
		
		return new Program(p.globals, transformedFunctions);
	}
	
	// Transform
	public static Expression T(Expression e, TypeMap tm, Functions functions)
	{
		// 값, Call 은 그대로
		if (e instanceof Value)
		{
			return e;
		}
		// Call 일 경우
		if (e instanceof Call)
		{
			Call c = (Call) e;
			ArrayList<Expression> transformedExpression = new ArrayList<>();
			
			// args 모두 순회하며 Transform
			for (Expression expression : c.args)
			{
				transformedExpression.add(T(expression, tm, functions));
			}
			
			return new Call(c.name, transformedExpression);
		}
		// 변수도 그대로
		if (e instanceof Variable)
		{
			return e;
		}
		// Binary 연산자
		if (e instanceof Binary)
		{
			Binary b = (Binary) e;
			Type typ1 = StaticTypeCheck.typeOf(b.term1, tm, functions);
			Type typ2 = StaticTypeCheck.typeOf(b.term2, tm, functions);
			Expression t1 = T(b.term1, tm, functions);
			Expression t2 = T(b.term2, tm, functions);
			
			// 타입에 따라 다른 연산자 매핑.
			if (typ1 == Type.INT)
			{
				return new Binary(b.op.intMap(b.op.val), t1, t2);
			}
			else if (typ1 == Type.FLOAT)
			{
				return new Binary(b.op.floatMap(b.op.val), t1, t2);
			}
			else if (typ1 == Type.CHAR)
			{
				return new Binary(b.op.charMap(b.op.val), t1, t2);
			}
			else if (typ1 == Type.BOOL)
			{
				return new Binary(b.op.boolMap(b.op.val), t1, t2);
			}
			// 에러 발생. 그대로 리턴.
			error("Binary type transforming error");
			return e;
		}
		
		// student exercise
		// Unary 연산자.
		if (e instanceof Unary)
		{
			Unary u = (Unary) e;
			Type type = StaticTypeCheck.typeOf(u.term, tm, functions);
			Expression t = T(u.term, tm, functions);
			
			// 타입에 따라 다른 연산자 매핑
			if (type == Type.BOOL)
			{
				return new Unary(u.op.boolMap(u.op.val), t);
			}
			else if (type == Type.FLOAT)
			{
				return new Unary(u.op.floatMap(u.op.val), t);
			}
			else if (type == Type.INT)
			{
				return new Unary(u.op.intMap(u.op.val), t);
			}
			else if (type == Type.CHAR)
			{
				return new Unary(u.op.charMap(u.op.val), t);
			}
			
			// 에러 발생. 그대로 리턴.
			error("Unary type transforming error");
			return e;
		}
		
		// 에러 발생. 그대로 리턴.
		error("type transforming error");
		return e;
	}
	
	// Transform
	public static Statement T(Statement s, TypeMap tm, Functions functions)
	{
		// Skip 은 안함.
		if (s instanceof Skip)
		{
			return s;
		}
		// Call 일 경우
		if (s instanceof Call)
		{
			Call c = (Call) s;
			ArrayList<Expression> transformedExpression = new ArrayList<>();
			
			// args 모두 순회하며 Transform
			for (Expression expression : c.args)
			{
				transformedExpression.add(T(expression, tm, functions));
			}
			
			return new Call(c.name, transformedExpression);
		}
		// Return 일 경우
		if (s instanceof Return)
		{
			Return r = (Return) s;
			
			return new Return(r.target, T(r.result, tm, functions));
		}
		// Assignment 일 경우.
		if (s instanceof Assignment)
		{
			Assignment a = (Assignment) s;
			
			Variable target = a.target;
			Expression src = T(a.source, tm, functions);
			Type ttype = (Type) tm.get(a.target);
			Type srctype = StaticTypeCheck.typeOf(a.source, tm, functions);
			
			// src 가 INT 이면 int to float 삽입.
			if (ttype == Type.FLOAT)
			{
				if (srctype == Type.INT)
				{
					src = new Unary(new Operator(Operator.I2F), src);
					srctype = Type.FLOAT;
				}
			}
			// src 가 CHAR 이면 char to int 삽입.
			else if (ttype == Type.INT)
			{
				if (srctype == Type.CHAR)
				{
					src = new Unary(new Operator(Operator.C2I), src);
					srctype = Type.INT;
				}
			}
			StaticTypeCheck.check(ttype == srctype,
					"bug in assignment to " + target);
			return new Assignment(target, src);
		}
		// Conditional
		if (s instanceof Conditional)
		{
			Conditional c = (Conditional) s;
			Expression test = T(c.test, tm, functions);
			Statement tbr = T(c.thenbranch, tm, functions);
			Statement ebr = T(c.elsebranch, tm, functions);
			
			return new Conditional(test, tbr, ebr);
		}
		// Loop
		if (s instanceof Loop)
		{
			Loop l = (Loop) s;
			Expression test = T(l.test, tm, functions);
			Statement body = T(l.body, tm, functions);
			
			return new Loop(test, body);
		}
		// Block
		if (s instanceof Block)
		{
			Block b = (Block) s;
			Block out = new Block();
			for (Statement stmt : b.members)
			{
				out.members.add(T(stmt, tm, functions));
			}
			return out;
		}
		
		// 에러 발생. 그대로 리턴.
		error("statement transforming error");
		return s;
	}
	
	public static void main(String args[])
	{
		// 명령 인자방식이 아닌 직접 입력 방식 사용
		String fileName = "../Test Programs/functions.cpp";
		Parser parser = new Parser(new Lexer(fileName));
		//Parser parser  = new Parser(new Lexer(args[0]));
		
		Program prog = parser.program();
		prog.display();           // student exercise
		
		// 타입 체킹
		System.out.println("Globals = ");
		TypeMap map = StaticTypeCheck.typing(prog.globals);
		map.display();
		
		StaticTypeCheck.V(prog);
		Program out = T(prog, map);
		System.out.println("Output AST");
		// 에러 발생 시 에러 메시지 출력.
		if (errorOccurred)
		{
			error("Errors occurred when transforming AST.");
		}
		else
		{
			out.display();    // student exercise
		}
	} //main
	
} // class TypeTransformer

    
