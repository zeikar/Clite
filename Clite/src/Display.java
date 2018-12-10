/**
 * Created by Zeikar Park. 2013111995
 */
public class Display
{
	// 화면에 예쁘게 들여쓰기 후 출력.
	public static void print(int indent, String content)
	{
		// 들여쓰기
		while(indent-->0)
		{
			System.out.print('\t');
		}
		System.out.println(content);
	}
}
