// Abstract syntax for the language C++Lite,
// exactly as it appears in Appendix B.

import javax.swing.plaf.nimbus.State;
import java.util.*;

class Program
{
	// Program = Declarations decpart ; Block body
	// <<Declarations>>  <<Statements>>
	// 에러를 막기 위한 더미 코드
	Declarations decpart;
	Block body;
	
	Declarations globals;
	Functions functions;
	
	public Program(Declarations globals, Functions functions)
	{
		this.globals = globals;
		this.functions = functions;
	}
	
	// 에러를 막기 위한 더미 코드
	public Program(Declarations decpart, Block body)
	{
		this.decpart = decpart;
		this.body = body;
	}
	
	void display()
	{
		// student exercise
		Display.print(0, "Program (abstract syntax): ");
		
		// NullPointerException 발생하는지 체크
		// -> 뭔가 토큰이 제대로 입력이 안된 것.
		try
		{
			Display.print(1, "Globals: ");
			globals.display(2);
			functions.display(1);
		}
		catch (NullPointerException e)
		{
			System.err.println("Unexpected Syntax Errors!");
		}
	}
	
}

// 함수들
class Functions extends ArrayList<Function>
{
	// Functions = Function*
	
	// 해당 이름의 Function  반환
	public Function getFunction(String name)
	{
		// 함수 iteration
		for (Function function : this)
		{
			if(function.id.equals(name))
			{
				return function;
			}
		}
		return null;
	}
	
	// 모든 함수의 이름들(Declarations) 반환.
	public Declarations getAllFunctionNames()
	{
		Declarations declarations = new Declarations();
		
		// 함수 iteration
		for (Function function : this)
		{
			declarations.add(new Declaration(new Variable(function.id), function.t));
		}
		
		return declarations;
	}
	
	// 화면에 출력.
	public void display(int indent)
	{
		Display.print(indent, "Functions: ");
		
		// 함수 iteration
		for (Function function : this)
		{
			function.display(indent + 1);
		}
	}
}

// 함수
class Function
{
	// Function = Type t; String id, Declarations params, locals, Block body
	// 함수의 리턴 타입
	Type t;
	String id;
	Declarations params, locals;
	Block body;
	
	public Function(Type t, String id, Declarations params, Declarations locals, Block body)
	{
		this.t = t;
		this.id = id;
		this.params = params;
		this.locals = locals;
		this.body = body;
	}
	
	public void display(int indent)
	{
		Display.print(indent, "Function = " + id + "; Return Type = " + t.toString());
		
		Display.print(indent + 1, "params = ");
		params.display(indent + 2);
		
		Display.print(indent + 1, "locals = ");
		locals.display(indent + 2);
		
		body.display(indent + 1);
	}
}

class Declarations extends ArrayList<Declaration>
{
	// 화면에 출력.
	public void display(int indent)
	{
		String output = "{";
		
		// 변수 iteration
		for (Declaration d : this)
		{
			output += "<" + d.v + ", " + d.t + ">, ";
		}
		
		Display.print(indent, output.substring(0, Math.max(output.length() - 2, 1)) + "}");
	}
	// Declarations = Declaration*
	// (a list of declarations d1, d2, ..., dn)
	
}

class Declaration
{
	// Declaration = Variable v; Type t
	Variable v;
	Type t;
	
	Declaration(Variable var, Type type)
	{
		v = var;
		t = type;
	} // declaration */
	
}

class Type
{
	// Type = int | bool | char | float | void
	final static Type INT = new Type("int");
	final static Type BOOL = new Type("bool");
	final static Type CHAR = new Type("char");
	final static Type FLOAT = new Type("float");
	final static Type VOID = new Type("void");
	// final static Type UNDEFINED = new Type("undef");
	
	private String id;
	
	private Type(String t)
	{
		id = t;
	}
	
	public String toString()
	{
		return id;
	}
}

interface Statement
{
	void display(int indent);
	// Statement = Skip | Block | Assignment | Conditional | Loop | Call | Return
	
}

class Skip implements Statement
{
	@Override
	public void display(int indent)
	{
		Display.print(indent, "Skip:");
	}
}

class Block implements Statement
{
	// Block = Statement*
	//         (a Vector of members)
	public ArrayList<Statement> members = new ArrayList<Statement>();
	
	public void display(int indent)
	{
		Display.print(indent, "Block: ");
		
		// 순차적으로 출력
		for (Statement s : members)
		{
			s.display(indent + 1);
		}
	}
}

class Assignment implements Statement
{
	// Assignment = Variable target; Expression source
	Variable target;
	Expression source;
	
	Assignment(Variable t, Expression e)
	{
		target = t;
		source = e;
	}
	
	@Override
	public void display(int indent)
	{
		Display.print(indent, "Assignment:");
		
		target.display(indent + 1);
		source.display(indent + 1);
	}
}

class Conditional implements Statement
{
	// Conditional = Expression test; Statement thenbranch, elsebranch
	Expression test;
	Statement thenbranch, elsebranch;
	// elsebranch == null means "if... then"
	
	Conditional(Expression t, Statement tp)
	{
		test = t;
		thenbranch = tp;
		elsebranch = new Skip();
	}
	
	Conditional(Expression t, Statement tp, Statement ep)
	{
		test = t;
		thenbranch = tp;
		elsebranch = ep;
	}
	
	@Override
	public void display(int indent)
	{
		Display.print(indent, "Conditional: ");
		
		test.display(indent + 1);
		thenbranch.display(indent + 1);
		elsebranch.display(indent + 1);
	}
}

class Loop implements Statement
{
	// Loop = Expression test; Statement body
	Expression test;
	Statement body;
	
	Loop(Expression t, Statement b)
	{
		test = t;
		body = b;
	}
	
	@Override
	public void display(int indent)
	{
		Display.print(indent, "Loop: ");
		test.display(indent + 1);
		body.display(indent + 1);
	}
}

// Call
class Call implements Statement, Expression
{
	// Call = String name; Expressions args
	String name;
	ArrayList<Expression> args = new ArrayList<>();
	
	@Override
	public void display(int indent)
	{
		Display.print(indent, "Call: " + name);
		
		Display.print(indent + 1, "args = ");
		
		// 순차적으로 출력
		for (Expression expression : args)
		{
			expression.display(indent + 2);
		}
	}
	
	public Call(String name, ArrayList<Expression> args)
	{
		this.name = name;
		this.args = args;
	}
}

// Return
class Return implements Statement
{
	// Return = Variable target; Expression result
	Variable target;
	Expression result;
	
	@Override
	public void display(int indent)
	{
		Display.print(indent, "Return: ");
		
		target.display(indent + 1);
		result.display(indent + 1);
	}
	
	public Return(Variable target, Expression result)
	{
		this.target = target;
		this.result = result;
	}
}

interface Expression
{
	void display(int indent);
	// Expression = Variable | Value | Binary | Unary
	
}

class Variable implements Expression
{
	// Variable = String id
	private String id;
	
	Variable(String s)
	{
		id = s;
	}
	
	public String toString()
	{
		return id;
	}
	
	public boolean equals(Object obj)
	{
		String s = ((Variable) obj).id;
		return id.equals(s); // case-sensitive identifiers
	}
	
	public int hashCode()
	{
		return id.hashCode();
	}
	
	public void display(int indent)
	{
		Display.print(indent, "Variable: " + id);
	}
}

abstract class Value implements Expression
{
	// Value = IntValue | BoolValue |
	//         CharValue | FloatValue
	protected Type type;
	protected boolean undef = true;
	
	int intValue()
	{
		assert false : "should never reach here";
		return 0;
	}
	
	boolean boolValue()
	{
		assert false : "should never reach here";
		return false;
	}
	
	char charValue()
	{
		assert false : "should never reach here";
		return ' ';
	}
	
	float floatValue()
	{
		assert false : "should never reach here";
		return 0.0f;
	}
	
	boolean isUndef()
	{
		return undef;
	}
	
	Type type()
	{
		return type;
	}
	
	static Value mkValue(Type type)
	{
		if (type == Type.INT)
		{
			return new IntValue();
		}
		if (type == Type.BOOL)
		{
			return new BoolValue();
		}
		if (type == Type.CHAR)
		{
			return new CharValue();
		}
		if (type == Type.FLOAT)
		{
			return new FloatValue();
		}
		throw new IllegalArgumentException("Illegal type in mkValue");
	}
}

class IntValue extends Value
{
	private int value = 0;
	
	IntValue()
	{
		type = Type.INT;
	}
	
	IntValue(int v)
	{
		this();
		value = v;
		undef = false;
	}
	
	int intValue()
	{
		assert !undef : "reference to undefined int value";
		return value;
	}
	
	public String toString()
	{
		if (undef)
		{
			return "undef";
		}
		return "" + value;
	}
	
	@Override
	public void display(int indent)
	{
		Display.print(indent, "IntValue: " + value);
	}
}

class BoolValue extends Value
{
	private boolean value = false;
	
	BoolValue()
	{
		type = Type.BOOL;
	}
	
	BoolValue(boolean v)
	{
		this();
		value = v;
		undef = false;
	}
	
	boolean boolValue()
	{
		assert !undef : "reference to undefined bool value";
		return value;
	}
	
	int intValue()
	{
		assert !undef : "reference to undefined bool value";
		return value ? 1 : 0;
	}
	
	public String toString()
	{
		if (undef)
		{
			return "undef";
		}
		return "" + value;
	}
	
	@Override
	public void display(int indent)
	{
		Display.print(indent, "BoolValue: " + value);
	}
}

class CharValue extends Value
{
	private char value = ' ';
	
	CharValue()
	{
		type = Type.CHAR;
	}
	
	CharValue(char v)
	{
		this();
		value = v;
		undef = false;
	}
	
	char charValue()
	{
		assert !undef : "reference to undefined char value";
		return value;
	}
	
	public String toString()
	{
		if (undef)
		{
			return "undef";
		}
		return "" + value;
	}
	
	@Override
	public void display(int indent)
	{
		Display.print(indent, "CharValue: " + value);
	}
}

class FloatValue extends Value
{
	private float value = 0;
	
	FloatValue()
	{
		type = Type.FLOAT;
	}
	
	FloatValue(float v)
	{
		this();
		value = v;
		undef = false;
	}
	
	float floatValue()
	{
		assert !undef : "reference to undefined float value";
		return value;
	}
	
	public String toString()
	{
		if (undef)
		{
			return "undef";
		}
		return "" + value;
	}
	
	@Override
	public void display(int indent)
	{
		Display.print(indent, "FloatValue: " + value);
	}
}

class Binary implements Expression
{
	// Binary = Operator op; Expression term1, term2
	Operator op;
	Expression term1, term2;
	
	Binary(Operator o, Expression l, Expression r)
	{
		op = o;
		term1 = l;
		term2 = r;
	} // binary
	
	@Override
	public void display(int indent)
	{
		Display.print(indent, "Binary: ");
		
		op.display(indent + 1);
		term1.display(indent + 1);
		term2.display(indent + 1);
	}
}

class Unary implements Expression
{
	// Unary = Operator op; Expression term
	Operator op;
	Expression term;
	
	Unary(Operator o, Expression e)
	{
		op = o;
		term = e;
	} // unary
	
	@Override
	public void display(int indent)
	{
		Display.print(indent, "Unary: ");
		op.display(indent + 1);
		term.display(indent + 1);
	}
}

class Operator
{
	// Operator = BooleanOp | RelationalOp | ArithmeticOp | UnaryOp
	// BooleanOp = && | ||
	final static String AND = "&&";
	final static String OR = "||";
	// RelationalOp = < | <= | == | != | >= | >
	final static String LT = "<";
	final static String LE = "<=";
	final static String EQ = "==";
	final static String NE = "!=";
	final static String GT = ">";
	final static String GE = ">=";
	// ArithmeticOp = + | - | * | /
	final static String PLUS = "+";
	final static String MINUS = "-";
	final static String TIMES = "*";
	final static String DIV = "/";
	// UnaryOp = !
	final static String NOT = "!";
	final static String NEG = "-";
	// CastOp = int | float | char
	final static String INT = "int";
	final static String FLOAT = "float";
	final static String CHAR = "char";
	// Typed Operators
	// RelationalOp = < | <= | == | != | >= | >
	final static String INT_LT = "INT<";
	final static String INT_LE = "INT<=";
	final static String INT_EQ = "INT==";
	final static String INT_NE = "INT!=";
	final static String INT_GT = "INT>";
	final static String INT_GE = "INT>=";
	// ArithmeticOp = + | - | * | /
	final static String INT_PLUS = "INT+";
	final static String INT_MINUS = "INT-";
	final static String INT_TIMES = "INT*";
	final static String INT_DIV = "INT/";
	// UnaryOp = !
	final static String INT_NEG = "-";
	// RelationalOp = < | <= | == | != | >= | >
	final static String FLOAT_LT = "FLOAT<";
	final static String FLOAT_LE = "FLOAT<=";
	final static String FLOAT_EQ = "FLOAT==";
	final static String FLOAT_NE = "FLOAT!=";
	final static String FLOAT_GT = "FLOAT>";
	final static String FLOAT_GE = "FLOAT>=";
	// ArithmeticOp = + | - | * | /
	final static String FLOAT_PLUS = "FLOAT+";
	final static String FLOAT_MINUS = "FLOAT-";
	final static String FLOAT_TIMES = "FLOAT*";
	final static String FLOAT_DIV = "FLOAT/";
	// UnaryOp = !
	final static String FLOAT_NEG = "-";
	// RelationalOp = < | <= | == | != | >= | >
	final static String CHAR_LT = "CHAR<";
	final static String CHAR_LE = "CHAR<=";
	final static String CHAR_EQ = "CHAR==";
	final static String CHAR_NE = "CHAR!=";
	final static String CHAR_GT = "CHAR>";
	final static String CHAR_GE = "CHAR>=";
	// RelationalOp = < | <= | == | != | >= | >
	final static String BOOL_LT = "BOOL<";
	final static String BOOL_LE = "BOOL<=";
	final static String BOOL_EQ = "BOOL==";
	final static String BOOL_NE = "BOOL!=";
	final static String BOOL_GT = "BOOL>";
	final static String BOOL_GE = "BOOL>=";
	// Type specific cast
	final static String I2F = "I2F";
	final static String F2I = "F2I";
	final static String C2I = "C2I";
	final static String I2C = "I2C";
	
	String val;
	
	Operator(String s)
	{
		val = s;
	}
	
	public String toString()
	{
		return val;
	}
	
	public boolean equals(Object obj)
	{
		return val.equals(obj);
	}
	
	boolean BooleanOp()
	{
		return val.equals(AND) || val.equals(OR);
	}
	
	boolean RelationalOp()
	{
		return val.equals(LT) || val.equals(LE) || val.equals(EQ)
				|| val.equals(NE) || val.equals(GT) || val.equals(GE);
	}
	
	boolean ArithmeticOp()
	{
		return val.equals(PLUS) || val.equals(MINUS)
				|| val.equals(TIMES) || val.equals(DIV);
	}
	
	boolean NotOp()
	{
		return val.equals(NOT);
	}
	
	boolean NegateOp()
	{
		return val.equals(NEG);
	}
	
	boolean intOp()
	{
		return val.equals(INT);
	}
	
	boolean floatOp()
	{
		return val.equals(FLOAT);
	}
	
	boolean charOp()
	{
		return val.equals(CHAR);
	}
	
	final static String intMap[][] = {
			{PLUS, INT_PLUS}, {MINUS, INT_MINUS},
			{TIMES, INT_TIMES}, {DIV, INT_DIV},
			{EQ, INT_EQ}, {NE, INT_NE}, {LT, INT_LT},
			{LE, INT_LE}, {GT, INT_GT}, {GE, INT_GE},
			{NEG, INT_NEG}, {FLOAT, I2F}, {CHAR, I2C}
	};
	
	final static String floatMap[][] = {
			{PLUS, FLOAT_PLUS}, {MINUS, FLOAT_MINUS},
			{TIMES, FLOAT_TIMES}, {DIV, FLOAT_DIV},
			{EQ, FLOAT_EQ}, {NE, FLOAT_NE}, {LT, FLOAT_LT},
			{LE, FLOAT_LE}, {GT, FLOAT_GT}, {GE, FLOAT_GE},
			{NEG, FLOAT_NEG}, {INT, F2I}
	};
	
	final static String charMap[][] = {
			{EQ, CHAR_EQ}, {NE, CHAR_NE}, {LT, CHAR_LT},
			{LE, CHAR_LE}, {GT, CHAR_GT}, {GE, CHAR_GE},
			{INT, C2I}
	};
	
	final static String boolMap[][] = {
			{EQ, BOOL_EQ}, {NE, BOOL_NE}, {LT, BOOL_LT},
			{LE, BOOL_LE}, {GT, BOOL_GT}, {GE, BOOL_GE},
	};
	
	final static private Operator map(String[][] tmap, String op)
	{
		for (int i = 0; i < tmap.length; i++)
		{
			if (tmap[i][0].equals(op))
			{
				return new Operator(tmap[i][1]);
			}
		}
		assert false : "should never reach here";
		return null;
	}
	
	final static public Operator intMap(String op)
	{
		return map(intMap, op);
	}
	
	final static public Operator floatMap(String op)
	{
		return map(floatMap, op);
	}
	
	final static public Operator charMap(String op)
	{
		return map(charMap, op);
	}
	
	final static public Operator boolMap(String op)
	{
		return map(boolMap, op);
	}
	
	public void display(int indent)
	{
		Display.print(indent, "Operator: " + val);
	}
}
