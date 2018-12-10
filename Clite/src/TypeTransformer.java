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
		Block body = (Block) T(p.body, tm);
		return new Program(p.decpart, body);
	}
	
	// Transform
	public static Expression T(Expression e, TypeMap tm)
	{
		// 값은 그대로
		if (e instanceof Value)
		{
			return e;
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
			Type typ1 = StaticTypeCheck.typeOf(b.term1, tm);
			Type typ2 = StaticTypeCheck.typeOf(b.term2, tm);
			Expression t1 = T(b.term1, tm);
			Expression t2 = T(b.term2, tm);
			
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
			Type type = StaticTypeCheck.typeOf(u.term, tm);
			Expression t = T(u.term, tm);
			
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
	public static Statement T(Statement s, TypeMap tm)
	{
		// Skip 은 안함.
		if (s instanceof Skip)
		{
			return s;
		}
		// Assignment 일 경우.
		if (s instanceof Assignment)
		{
			Assignment a = (Assignment) s;
			
			Variable target = a.target;
			Expression src = T(a.source, tm);
			Type ttype = (Type) tm.get(a.target);
			Type srctype = StaticTypeCheck.typeOf(a.source, tm);
			
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
			Expression test = T(c.test, tm);
			Statement tbr = T(c.thenbranch, tm);
			Statement ebr = T(c.elsebranch, tm);
			
			return new Conditional(test, tbr, ebr);
		}
		// Loop
		if (s instanceof Loop)
		{
			Loop l = (Loop) s;
			Expression test = T(l.test, tm);
			Statement body = T(l.body, tm);
			
			return new Loop(test, body);
		}
		// Block
		if (s instanceof Block)
		{
			Block b = (Block) s;
			Block out = new Block();
			for (Statement stmt : b.members)
			{
				out.members.add(T(stmt, tm));
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
		String fileName = "../Test Programs/clone.cpp";
		Parser parser = new Parser(new Lexer(fileName));
		//Parser parser  = new Parser(new Lexer(args[0]));
		
		Program prog = parser.program();
		prog.display();           // student exercise
		
		// 타입 체킹
		System.out.println("\nBegin type checking...");
		System.out.println("Type map:");
		TypeMap map = StaticTypeCheck.typing(prog.decpart);
		map.display();    // student exercise
		
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

    
