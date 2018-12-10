import java.io.*;

public class Lexer
{
	private boolean isEof = false;
	private char ch = ' ';
	private BufferedReader input;
	private String line = "";
	private int lineno = 0;
	private int col = 1;
	private final String letters = "abcdefghijklmnopqrstuvwxyz"
			+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private final String digits = "0123456789";
	private final char eolnCh = '\n';
	private final char eofCh = '\004';
	
	// 파일을 읽음.
	public Lexer(String fileName)
	{ // source filename
		try
		{
			input = new BufferedReader(new FileReader(fileName));
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File not found: " + fileName);
			System.exit(1);
		}
	}
	
	// 한 글자씩 읽기.
	private char nextChar()
	{ // Return next char
		if (ch == eofCh)
		{
			error("Attempt to read past end of file");
		}
		col++;
		
		// 한 줄 다 읽었을 때
		if (col >= line.length())
		{
			try
			{
				line = input.readLine();
			}
			catch (IOException e)
			{
				System.err.println(e);
				System.exit(1);
			} // try
			if (line == null) // at end of file
			{
				line = "" + eofCh;
			}
			// 새로운 라인.
			else
			{
				// System.out.println(lineno + ":\t" + line);
				lineno++;
				line += eolnCh;
			} // if line
			col = 0;
		} // if col
		return line.charAt(col);
	}
	
	// 다음 토큰 읽기.
	public Token next()
	{ // Return next token
		do
		{
			// 문자인가. (알파벳)
			if (isLetter(ch))
			{ // ident or keyword
				String spelling = concat(letters + digits);
				return Token.keyword(spelling);
			}
			// 숫자인가.
			else if (isDigit(ch))
			{ // int or float literal
				String number = concat(digits);
				// 점이 아니면 정수형
				if (ch != '.')  // int Literal
				{
					return Token.mkIntLiteral(number);
				}
				// 점 뒤의 숫자 읽은 후 뒤에 붙이고 실수형 생성.
				number += concat(digits);
				return Token.mkFloatLiteral(number);
			}
			else
			{
				switch (ch)
				{
					// whitespace (공백)
					// 그냥 다음 글자 읽기.
					case ' ':
					case '\t':
					case '\r':
					case eolnCh:
						ch = nextChar();
						break;
					
					case '/':  // divide or comment
						ch = nextChar();
						// 다음 글자가 / 가 아니면 나눗셈.
						if (ch != '/')
						{
							return Token.divideTok;
						}
						// 아니면 주석임.
						// 다음 줄까지 계속 읽고 주석으로 처리 -> 무시함.
						// comment
						do
						{
							ch = nextChar();
						}
						while (ch != eolnCh);
						// 개행 문자 처리.
						ch = nextChar();
						break;
					
					// 작은 따옴표
					case '\'':  // char literal
						// ch1: 다음 글자 (문자)
						char ch1 = nextChar();
						
						
						// 만약 ' 이면 (없음) char 리터럴 타입이 아니다 -> 에러.
						if (ch1 == '\'')
						{
							error("Empty character literal");
						}
						
						ch = nextChar(); // get '
						
						// 만약 ' 가 아니면 (두 글자 이상) char 리터럴 타입이 아니다 -> 에러.
						if (ch != '\'')
						{
							error("Too many characters in character literal");
						}
						ch = nextChar();
						return Token.mkCharLiteral("" + ch1);
					
					// end of file
					case eofCh:
						return Token.eofTok;
					
					// +는 더하기 기호
					case '+':
						ch = nextChar();
						return Token.plusTok;
					
					// - * ( ) { } ; ,  student exercise
					// -는 빼기
					case '-':
						ch = nextChar();
						return Token.minusTok;
					
					// *은 곱하기
					case '*':
						ch = nextChar();
						return Token.multiplyTok;
					
					// (
					case '(':
						ch = nextChar();
						return Token.leftParenTok;
					// )
					case ')':
						ch = nextChar();
						return Token.rightParenTok;
					
					// {
					case '{':
						ch = nextChar();
						return Token.leftBraceTok;
					
					// }
					case '}':
						ch = nextChar();
						return Token.rightBraceTok;
					
					// [
					case '[':
						ch = nextChar();
						return Token.leftBracketTok;
					
					// ]
					case ']':
						ch = nextChar();
						return Token.rightBracketTok;
					
					// ;
					case ';':
						ch = nextChar();
						return Token.semicolonTok;
					
					// ,
					case ',':
						ch = nextChar();
						return Token.commaTok;
					
					// &
					case '&':
						// &가 두번 연속인지 체크.
						check('&');
						return Token.andTok;
					
					// |
					case '|':
						// |가 두번 연속인지 체크.
						check('|');
						return Token.orTok;
					
					// =
					case '=':
						// =이 2개인지 1개인지 체크.
						return chkOpt('=', Token.assignTok,
								Token.eqeqTok);
					
					
					// < > !  student exercise
					// <, <=
					case '<':
						return chkOpt('=', Token.ltTok,
								Token.lteqTok);
					
					// >, >=
					case '>':
						return chkOpt('=', Token.gtTok,
								Token.gteqTok);
					
					// !, !=
					case '!':
						return chkOpt('=', Token.notTok,
								Token.noteqTok);
					
					default:
						error("Illegal character " + ch);
				} // switch
			}
		}
		while (true);
	} // next
	
	// 글자인가 리턴.
	private boolean isLetter(char c)
	{
		return (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
	}
	
	// 숫자인가 리턴.
	private boolean isDigit(char c)
	{
		return (c >= '0' && c <= '9');  // student exercise
	}
	
	// 연속한 두 글자가 같은지 체크.
	private void check(char c)
	{
		ch = nextChar();
		if (ch != c)
		{
			error("Illegal character, expecting " + c);
		}
		ch = nextChar();
	}
	
	// c가 들어가는지 안들어가는지 체크.
	private Token chkOpt(char c, Token one, Token two)
	{
		ch = nextChar();
		
		// c가 안들어감
		if (ch != c)
		{
			return one;
		}
		
		// c가 들어감.
		ch = nextChar();
		return two;  // student exercise
	}
	
	// set 안의 문자의 경우 계속 한글자씩 뒤에 붙인다.
	private String concat(String set)
	{
		String r = "";
		do
		{
			r += ch;
			ch = nextChar();
		}
		while (set.indexOf(ch) >= 0);
		return r;
	}
	
	// 에러 출력 후 종료.
	public void error(String msg)
	{
		System.err.print(line);
		System.err.println("Lexical Error: line " + lineno + ", column " + col + " " + msg);
		
		// 패닉 모드 추가!
		// 에러 캐릭터는 무시.
		//System.exit(1);
		if (ch != eofCh)
		{
			ch = nextChar();
		}
	}
	
	static public void main(String[] argv)
	{
		// 렉서 스타트.
		//Lexer lexer = new Lexer(argv[0]);
		Lexer lexer = new Lexer("../Test Programs/fib.cpp");
		
		// 다음 토큰 읽기.
		Token tok = lexer.next();
		// 끝날 때까지 읽기.
		while (tok != Token.eofTok)
		{
			System.out.println(tok.toString());
			tok = lexer.next();
		}
	} // main
	
}

