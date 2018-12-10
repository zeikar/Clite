public class Token
{
	// 키워드 개수.
	private static final int KEYWORDS = TokenType.Eof.ordinal();
	
	private static final String[] reserved = new String[KEYWORDS];
	private static Token[] token = new Token[KEYWORDS];
	
	// 토큰들
	public static final Token eofTok = new Token(TokenType.Eof, "<<EOF>>");
	public static final Token boolTok = new Token(TokenType.Bool, "bool");
	public static final Token charTok = new Token(TokenType.Char, "char");
	public static final Token elseTok = new Token(TokenType.Else, "else");
	public static final Token falseTok = new Token(TokenType.False, "false");
	public static final Token floatTok = new Token(TokenType.Float, "float");
	public static final Token voidTok = new Token(TokenType.Void, "void");
	public static final Token ifTok = new Token(TokenType.If, "if");
	public static final Token intTok = new Token(TokenType.Int, "int");
	public static final Token mainTok = new Token(TokenType.Main, "main");
	public static final Token trueTok = new Token(TokenType.True, "true");
	public static final Token whileTok = new Token(TokenType.While, "while");
	public static final Token leftBraceTok = new Token(TokenType.LeftBrace, "{");
	public static final Token rightBraceTok = new Token(TokenType.RightBrace, "}");
	public static final Token leftBracketTok = new Token(TokenType.LeftBracket, "[");
	public static final Token rightBracketTok = new Token(TokenType.RightBracket, "]");
	public static final Token leftParenTok = new Token(TokenType.LeftParen, "(");
	public static final Token rightParenTok = new Token(TokenType.RightParen, ")");
	public static final Token semicolonTok = new Token(TokenType.Semicolon, ";");
	public static final Token commaTok = new Token(TokenType.Comma, ",");
	public static final Token assignTok = new Token(TokenType.Assign, "=");
	public static final Token eqeqTok = new Token(TokenType.Equals, "==");
	public static final Token ltTok = new Token(TokenType.Less, "<");
	public static final Token lteqTok = new Token(TokenType.LessEqual, "<=");
	public static final Token gtTok = new Token(TokenType.Greater, ">");
	public static final Token gteqTok = new Token(TokenType.GreaterEqual, ">=");
	public static final Token notTok = new Token(TokenType.Not, "!");
	public static final Token noteqTok = new Token(TokenType.NotEqual, "!=");
	public static final Token plusTok = new Token(TokenType.Plus, "+");
	public static final Token minusTok = new Token(TokenType.Minus, "-");
	public static final Token multiplyTok = new Token(TokenType.Multiply, "*");
	public static final Token divideTok = new Token(TokenType.Divide, "/");
	public static final Token andTok = new Token(TokenType.And, "&&");
	public static final Token orTok = new Token(TokenType.Or, "||");
	
	// 토큰 타입
	private TokenType type;
	// 토큰 값
	private String value = "";
	
	// 생성자.
	private Token(TokenType t, String v)
	{
		type = t;
		value = v;
		// 식별자는 따로 저장.
		if (t.compareTo(TokenType.Eof) < 0)
		{
			int ti = t.ordinal();
			reserved[ti] = v;
			token[ti] = this;
		}
	}
	
	public TokenType type()
	{
		return type;
	}
	
	public String value()
	{
		return value;
	}
	
	// name 에 해당하는 토큰 리턴.
	// => 대문자로 시작하면 식별자.
	// => 예약어 있으면 해당 리턴.
	// => 나머지는 식별자.
	public static Token keyword(String name)
	{
		char ch = name.charAt(0);
		if (ch >= 'A' && ch <= 'Z')
		{
			return mkIdentTok(name);
		}
		for (int i = 0; i < KEYWORDS; i++)
		{
			if (name.equals(reserved[i]))
			{
				return token[i];
			}
		}
		return mkIdentTok(name);
	} // keyword
	
	// 식별자 생성
	public static Token mkIdentTok(String name)
	{
		return new Token(TokenType.Identifier, name);
	}
	
	// 정수 리터럴 생성
	public static Token mkIntLiteral(String name)
	{
		return new Token(TokenType.IntLiteral, name);
	}
	
	// 실수 리터럴 생성
	public static Token mkFloatLiteral(String name)
	{
		return new Token(TokenType.FloatLiteral, name);
	}
	
	// 문자 리터럴 생성
	public static Token mkCharLiteral(String name)
	{
		return new Token(TokenType.CharLiteral, name);
	}
	
	public String toString()
	{
		// 리터럴, 식별자 아닌 것들.
		if (type.compareTo(TokenType.Identifier) < 0)
		{
			return value;
		}
		return type + "\t" + value;
	} // toString
	
	public static void main(String[] args)
	{
		System.out.println(eofTok);
		System.out.println(whileTok);
	}
} // Token
