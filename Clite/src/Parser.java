import java.util.*;

public class Parser
{
	// Recursive descent parser that inputs a C++Lite program and
	// generates its abstract syntax.  Each method corresponds to
	// a concrete syntax grammar rule, which appears as a comment
	// at the beginning of the method.
	
	Token token;          // current token from the input stream
	Variable currentFunction; // current function
	Lexer lexer;
	
	// 파서 생성. lexer 설정 후 next로 첫번째 읽어옴.
	public Parser(Lexer ts)
	{ // Open the C++Lite source program
		lexer = ts;                          // as a token stream, and
		token = lexer.next();            // retrieve its first Token
	}
	
	// 토큰 타입이 일치하는지 확인.
	// 일치하면 값을 리턴.
	private String match(TokenType t)
	{
		String value = token.value();
		if (token.type().equals(t))
		{
			token = lexer.next();
		}
		else
		{
			error(t);
		}
		return value;
	}
	
	// 에러 출력
	private void error(TokenType tok)
	{
		error(tok.toString());
	}
	
	// 에러 출력
	private void error(String tok)
	{
		// 패닉 모드 추가!
		// 세미 콜론 나올 때까지 계속 토큰을 읽고 무시.
		//System.exit(1);
		do
		{
			System.err.println("Syntax Error: expecting: " + tok + "; saw: " + token);
			token = lexer.next();
		}
		while (token != Token.eofTok && !token.type().equals(TokenType.Semicolon));
		token = lexer.next();
	}
	
	// 메인
	public Program program()
	{
		// Program --> { Type Identifier FunctionOrGlobal } MainFunction
		Declarations globals = new Declarations();
		Functions functions = new Functions();
		
		// 토큰이 타입일 경우 반복
		while (isType())
		{
			FunctionOrGlobal(globals, functions);
		}
		
		Function mainFunction = MainFunction();
		functions.add(mainFunction);
		
		return new Program(globals, functions);
	}
	
	// 함수 혹은 글로벌 변수 추가함.
	private void FunctionOrGlobal(Declarations globals, Functions functions)
	{
		// FunctionOrGlobal -> (Parameters) {Declarations Statements} | Global
		
		// Type
		Type t = type();
		// 메인 함수 체크
		if (t.equals(Type.INT) && isMain())
		{
			return;
		}
		// Identifier
		Variable v = new Variable(match(TokenType.Identifier));
		
		// ( 나오면 함수
		if (isLeftParen())
		{
			// 현재 함수 세팅
			currentFunction = v;
			
			// 다음 토큰
			token = lexer.next();
			
			// Parameters
			Declarations parameters = Parameters();
			// )
			match(TokenType.RightParen);
			// {
			match(TokenType.LeftBrace);
			// Declarations
			Declarations locals = declarations();
			// Statements
			Block body = statementsWithoutBrackets();
			// }
			match(TokenType.RightBrace);
			functions.add(new Function(t, v.toString(), parameters, locals, body));
		}
		else
		{
			// Global
			Global(t, globals);
		}
	}
	
	private Declarations Parameters()
	{
		// Parameters  --> [ Parameter {, Parameter } ]
		// Parameter --> Type, Identifier
		Declarations parameters = new Declarations();
		// <<Type>>
		Type t = type();
		// <<Identifier>>
		Variable v = new Variable(match(TokenType.Identifier));
		// <<Parameter>> 추가
		parameters.add(new Declaration(v, t));
		
		// , 나올 때마다 계속 추가
		while (isComma())
		{
			// 다음 토큰
			token = lexer.next();
			
			// <<Type>>
			t = type();
			// <<Identifier>>
			v = new Variable(match(TokenType.Identifier));
			// <<Parameter>> 추가
			parameters.add(new Declaration(v, t));
		}
		
		return parameters;
	}
	
	private void Global(Type globalType, Declarations globals)
	{
		// , 나올 때마다 계속 추가
		while (isComma())
		{
			// 다음 토큰
			token = lexer.next();
			
			// <<Identifier>>
			Variable v = new Variable(match(TokenType.Identifier));
			// <<Declaration>> 추가
			globals.add(new Declaration(v, globalType));
		}
		
		// 끝은 세미콜론
		match(TokenType.Semicolon);
	}
	
	// 메인 함수
	private Function MainFunction()
	{
		// MainFunction -> int main() { Declarations Statements }
		
		// int main( params )
		//match(TokenType.Int);
		match(TokenType.Main);
		match(TokenType.LeftParen);
		Declarations params = declarations();
		match(TokenType.RightParen);
		
		// { 열었는지 확인.
		match(TokenType.LeftBrace);
		// student exercise
		
		// <<Declarations>>  <<Statements>>
		Declarations locals = declarations();
		Block body = statementsWithoutBrackets();
		
		// } 닫았는지 확인.
		match(TokenType.RightBrace);
		
		return new Function(Type.INT, Token.mainTok.toString(), params, locals, body);
	}
	
	// <<Declarations>> -> {<<Declaration>>}
	private Declarations declarations()
	{
		// Declarations --> { Declaration }
		Declarations ds = new Declarations();
		
		// 토큰이 타입일 경우 반복
		while (isType())
		{
			// ds에 Declaration을 계속 추가.
			declaration(ds);
		}
		
		return ds;  // student exercise
	}
	
	// <<Declaration>>을 계속 추가함.
	private void declaration(Declarations ds)
	{
		// Declaration  --> Type Identifier { , Identifier } ;
		// student exercise
		
		// <<Type>>
		Type t = type();
		// <<Identifier>>
		Variable v = new Variable(match(TokenType.Identifier));
		// <<Declaration>> 추가
		ds.add(new Declaration(v, t));
		
		// , 나올 때마다 계속 추가
		while (isComma())
		{
			// 다음 토큰
			token = lexer.next();
			
			// <<Identifier>>
			v = new Variable(match(TokenType.Identifier));
			// <<Declaration>> 추가
			ds.add(new Declaration(v, t));
		}
		
		// 끝은 세미콜론
		match(TokenType.Semicolon);
	}
	
	// <<Type>> 타입 반환. int | bool | float | char
	private Type type()
	{
		// Type  -->  int | bool | float | char
		Type t = null;
		// student exercise
		// int
		if (token.type().equals(TokenType.Int))
		{
			t = Type.INT;
		}
		// bool
		else if (token.type().equals(TokenType.Bool))
		{
			t = Type.BOOL;
		}
		// float
		else if (token.type().equals(TokenType.Float))
		{
			t = Type.FLOAT;
		}
		// char
		else if (token.type().equals(TokenType.Char))
		{
			t = Type.CHAR;
		}
		// void
		else if (token.type().equals(TokenType.Void))
		{
			t = Type.VOID;
		}
		// 타입이 아님 -> 에러
		else
		{
			error("Type ( int | bool | float | char | void )");
		}
		token = lexer.next();
		
		return t;
	}
	
	// <<Statement>> 1개 반환. ;(Skip) | Block | Assignment | IfStatement | WhileStatement
	private Statement statement()
	{
		// Statement --> ;(Skip) | Block | Assignment | IfStatement | WhileStatement
		Statement s = new Skip();
		// student exercise
		
		// ; (Skip)
		if (token.type().equals(TokenType.Semicolon))
		{
			s = new Skip();
		}
		// <<Block>>
		else if (token.type().equals(TokenType.LeftBrace))
		{
			s = statements();
		}
		// <<Assignment>> OR CallStatement
		else if (token.type().equals(TokenType.Identifier))
		{
			Variable identifier = new Variable(token.value());
			match(TokenType.Identifier);
			
			// Assignment
			if(token.type().equals(TokenType.Assign))
			{
				s = assignment(identifier);
			}
			// CallStatement
			else if(token.type().equals(TokenType.LeftParen))
			{
				Call c = callStatement(identifier);
				match(TokenType.Semicolon);
				s = c;
			}
		}
		// <<IfStatement>>
		else if (token.type().equals(TokenType.If))
		{
			s = ifStatement();
		}
		// <<WhileStatement>>
		else if (token.type().equals(TokenType.While))
		{
			s = whileStatement();
		}
		// ReturnStatement
		else if(token.type().equals(TokenType.Return))
		{
			s = returnStatement();
		}
		// 에러.
		else
		{
			error("Statement (;(Skip) | Block | Assignment | IfStatement | WhileStatement)");
		}
		
		return s;
	}
	
	// <<Block>> 반환  '{' Statements '}'
	private Block statements()
	{
		// Block --> '{' Statements '}'
		// <<Statements>>
		Block b;
		// student exercise
		
		// { 으로 시작
		match(TokenType.LeftBrace);
		
		// <<Block>>에 <<Statements>> 추가.
		b = statementsWithoutBrackets();
		
		// } 으로 끝
		match(TokenType.RightBrace);
		
		
		return b;
	}
	
	// <<Statements>> 반환. ({} 없는 Block)
	private Block statementsWithoutBrackets()
	{
		Block b = new Block();
		while (isStatement())
		{
			// <<Block>>에 <<Statement>> 추가.
			b.members.add(statement());
		}
		return b;
	}
	
	// 대입 <<Identifier>> = <<Expression>>
	private Assignment assignment(Variable identifier)
	{
		// Assignment --> Identifier = Expression ;
		// <<Identifier>>
		//Variable v = new Variable(match(TokenType.Identifier));
		// =
		match(TokenType.Assign);
		// <<Expression>>
		Expression e = expression();
		// ; 으로 끝난다.
		match(TokenType.Semicolon);
		
		return new Assignment(identifier, e);  // student exercise
	}
	
	// if, if ( <<Expression>> ) <<Statement>> [ else <<Statement>> ]
	private Conditional ifStatement()
	{
		// IfStatement --> if ( Expression ) Statement [ else Statement ]
		Conditional c = null;
		
		// if
		match(TokenType.If);
		// (
		match(TokenType.LeftParen);
		// <<Expression>>
		Expression e = expression();
		// )
		match(TokenType.RightParen);
		// <<Statement>>
		Statement s = statement();
		
		// else가 나오면
		if (token.type().equals(TokenType.Else))
		{
			// else
			token = lexer.next();
			
			// <<Statement>>
			Statement elseState = statement();
			c = new Conditional(e, s, elseState);
		}
		else
		{
			c = new Conditional(e, s);
		}
		
		return c;  // student exercise
	}
	
	private Loop whileStatement()
	{
		// WhileStatement --> while ( Expression ) Statement
		// while
		match(TokenType.While);
		// (
		match(TokenType.LeftParen);
		// <<Expression>>
		Expression e = expression();
		// )
		match(TokenType.RightParen);
		// <<Statement>>
		Statement s = statement();
		
		return new Loop(e, s);  // student exercise
	}
	
	// Call Statement
	private Call callStatement(Variable identifier)
	{
		// CallStatement -> Call;
		// Call -> Identifier ( Arguments )
		
		// (
		match(TokenType.LeftParen);
		
		ArrayList<Expression> arguments = new ArrayList<>();
		// ) 괄호 닫는 것 까지 돌면서 Arguments 추가.
		while (!(token.type().equals(TokenType.RightParen)))
		{
			arguments.add(expression());
			if (isComma())
			{
				match(TokenType.Comma);
			}
		}
		// )
		match(TokenType.RightParen);
		
		return new Call(identifier.toString(), arguments);
	}
	
	// ReturnStatement
	private Return returnStatement()
	{
		// ReturnStatement -> return Expression;
		// return
		match(TokenType.Return);
		// expression
		Expression ret = expression();
		// ;
		match(TokenType.Semicolon);
		
		return new Return(currentFunction, ret);
	}
	
	// 식
	private Expression expression()
	{
		// Expression --> Conjunction { || Conjunction }
		// <<Conjunction>>
		Expression e = conjunction();
		
		// || 이면 반복해서 추가.
		while (isOr())
		{
			Operator op = new Operator(match(token.type()));
			Expression c2 = conjunction();
			// 앞과 뒤의 식을 연결
			e = new Binary(op, e, c2);
		}
		return e;  // student exercise
	}
	
	private Expression conjunction()
	{
		// Conjunction --> Equality { && Equality }
		// <<Equality>>
		Expression e = equality();
		
		// && 이면 반복해서 추가.
		while (isAnd())
		{
			Operator op = new Operator(match(token.type()));
			Expression e2 = equality();
			// 앞과 뒤의 식을 연결
			e = new Binary(op, e, e2);
		}
		return e;  // student exercise
	}
	
	// ==, != 연산자
	private Expression equality()
	{
		// Equality --> Relation [ EquOp Relation ]
		// <<Relation>>
		Expression e = relation();
		
		// ==, != 이면 반복해서 추가.
		while (isEqualityOp())
		{
			Operator op = new Operator(match(token.type()));
			Expression r2 = relation();
			// 앞과 뒤의 식을 연결
			e = new Binary(op, e, r2);
		}
		return e;  // student exercise
	}
	
	//  <, <=, >, >= 연산자.
	private Expression relation()
	{
		// Relation --> Addition [RelOp Addition]
		// <<Addition>>
		Expression e = addition();
		
		// <, <=, >, >= 이면 반복해서 추가.
		while (isRelationalOp())
		{
			Operator op = new Operator(match(token.type()));
			Expression a2 = addition();
			// 앞과 뒤의 식을 연결
			e = new Binary(op, e, a2);
		}
		return e;  // student exercise
	}
	
	// +, - 연산자
	private Expression addition()
	{
		// Addition --> Term { AddOp Term }
		Expression e = term();
		while (isAddOp())
		{
			Operator op = new Operator(match(token.type()));
			Expression term2 = term();
			e = new Binary(op, e, term2);
		}
		return e;
	}
	
	// *, /, % 연산자
	private Expression term()
	{
		// Term --> Factor { MultiplyOp Factor }
		Expression e = factor();
		while (isMultiplyOp())
		{
			Operator op = new Operator(match(token.type()));
			Expression term2 = factor();
			e = new Binary(op, e, term2);
		}
		return e;
	}
	
	// 단항 연산자 (!, -)
	private Expression factor()
	{
		// Factor --> [ UnaryOp ] Primary
		if (isUnaryOp())
		{
			Operator op = new Operator(match(token.type()));
			Expression term = primary();
			return new Unary(op, term);
		}
		else
		{
			return primary();
		}
	}
	
	// 우선순위 가장 높은 것, 리터럴, 식별자 등
	private Expression primary()
	{
		// Primary --> Identifier | Literal | ( Expression )
		//             | Type ( Expression ) | Identifier(Arguments)
		Expression e = null;
		// 식별자. 변수 혹은 함수 호출
		if (token.type().equals(TokenType.Identifier))
		{
			Variable v = new Variable(match(TokenType.Identifier));
			
			// 함수 호출
			if (token.type().equals(TokenType.LeftParen))
			{
				e = callStatement(v);
			}
			// variable
			else
			{
				e = v;
			}
		}
		// 리터럴
		else if (isLiteral())
		{
			e = literal();
		}
		// 소괄호
		else if (token.type().equals(TokenType.LeftParen))
		{
			token = lexer.next();
			e = expression();
			match(TokenType.RightParen);
		}
		// 형변환
		else if (isType())
		{
			Operator op = new Operator(match(token.type()));
			match(TokenType.LeftParen);
			Expression term = expression();
			match(TokenType.RightParen);
			e = new Unary(op, term);
		}
		// 에러
		else
		{
			error("Identifier | Literal | ( | Type");
		}
		return e;
	}
	
	// 리터럴 값 반환
	private Value literal()
	{
		Value v = null;
		// 정수형
		if (token.type().equals(TokenType.IntLiteral))
		{
			// 값 저장
			v = new IntValue(Integer.parseInt(token.value()));
			token = lexer.next();
		}
		// 불리언
		else if (isBooleanLiteral())
		{
			v = new BoolValue(Boolean.parseBoolean(token.value()));
			token = lexer.next();
		}
		// 실수형
		else if (token.type().equals(TokenType.FloatLiteral))
		{
			// 값 저장
			v = new FloatValue(Float.parseFloat(token.value()));
			token = lexer.next();
		}
		// 문자
		else if (token.type().equals(TokenType.CharLiteral))
		{
			// 값 저장
			v = new CharValue(token.value().charAt(0));
			token = lexer.next();
		}
		// 에러
		else
		{
			error("Literal value");
		}
		
		return v;
	}
	
	// +, - 연산자.
	private boolean isAddOp()
	{
		return token.type().equals(TokenType.Plus) ||
				token.type().equals(TokenType.Minus);
	}
	
	// *, / 연산자
	private boolean isMultiplyOp()
	{
		return token.type().equals(TokenType.Multiply) ||
				token.type().equals(TokenType.Divide);
	}
	
	// !, - 연산자
	private boolean isUnaryOp()
	{
		return token.type().equals(TokenType.Not) ||
				token.type().equals(TokenType.Minus);
	}
	
	// ==, != 연산자
	private boolean isEqualityOp()
	{
		return token.type().equals(TokenType.Equals) ||
				token.type().equals(TokenType.NotEqual);
	}
	
	// <. <=, >, >= 연산자
	private boolean isRelationalOp()
	{
		return token.type().equals(TokenType.Less) ||
				token.type().equals(TokenType.LessEqual) ||
				token.type().equals(TokenType.Greater) ||
				token.type().equals(TokenType.GreaterEqual);
	}
	
	// Type 선언부분.
	private boolean isType()
	{
		return token.type().equals(TokenType.Int)
				|| token.type().equals(TokenType.Bool)
				|| token.type().equals(TokenType.Float)
				|| token.type().equals(TokenType.Char)
				|| token.type().equals(TokenType.Void);
	}
	
	// Literal 체크
	private boolean isLiteral()
	{
		return token.type().equals(TokenType.IntLiteral) ||
				isBooleanLiteral() ||
				token.type().equals(TokenType.FloatLiteral) ||
				token.type().equals(TokenType.CharLiteral);
	}
	
	// Boolean 체크
	private boolean isBooleanLiteral()
	{
		return token.type().equals(TokenType.True) ||
				token.type().equals(TokenType.False);
	}
	
	
	// , 인가
	private boolean isComma()
	{
		return token.type().equals(TokenType.Comma);
	}
	
	// ( 인가 (왼쪽 소괄호)
	private boolean isLeftParen()
	{
		return token.type().equals(TokenType.LeftParen);
	}
	
	// main 인가
	private boolean isMain()
	{
		return token.type().equals(TokenType.Main);
	}
	
	// <<Statement>>인가. (;(Skip) | Block | Assignment | IfStatement | WhileStatement | CallStatement | ReturnStatement)
	private boolean isStatement()
	{
		return token.type().equals(TokenType.Semicolon)
				|| token.type().equals(TokenType.LeftBrace)
				|| token.type().equals(TokenType.Identifier)
				|| token.type().equals(TokenType.If)
				|| token.type().equals(TokenType.While)
				|| token.type().equals(TokenType.Return);
	}
	
	// || 인가
	private boolean isOr()
	{
		return token.type().equals(TokenType.Or);
	}
	
	// && 인가
	private boolean isAnd()
	{
		return token.type().equals(TokenType.And);
	}
	
	public static void main(String args[])
	{
		//Parser parser  = new Parser(new Lexer(args[0]));
		String fileName = "../Test Programs/recFib.cpp";
		Parser parser = new Parser(new Lexer(fileName));
		Display.print(0, "Begin parsing... " + fileName + "\n");
		Program prog = parser.program();
		prog.display();           // display abstract syntax tree
	} //main
	
} // Parser
