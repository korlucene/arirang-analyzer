package org.apache.lucene.ko;

import junit.framework.TestCase;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public class TestKoreanAnalyzer extends TestCase {

	public void testKoreanAnalyzer() throws Exception {
		
		String input = "정식시호는 명헌숙경예인정목홍성장순정휘장소단희수현의헌강수유령자온공안효정왕후(明憲淑敬睿仁正穆弘聖章純貞徽莊昭端禧粹顯懿獻康綏裕寧慈溫恭安孝定王后)이며 돈령부영사(敦寧府領事) 홍재룡(洪在龍)의 딸이다. 1844년, 헌종의 정비(正妃)인 효현왕후가 승하하자 헌종의 계비로써 중궁에 책봉되었으나 5년 뒤인 1849년에 남편 헌종이 승하하고 철종이 즉위하자 19세의 어린 나이로 대비가 되었다. 1857년 시조모 대왕대비 순원왕후가 승하하자 왕대비가 되었다.";
//		input = "정식시호는 明憲淑敬睿";
//		input = "정보화용역사업";
//		input = "空間의";
//		input =  "di" + '\u000B' + "erent";
//		input = "찾아서- C# 달리기";
//		input = "ab.cd";
//		input = "절차서";
//		input = "입찰안내서";
//		input = "9호기";
//		input = "입찰평가보고서";
//		input = "입찰평가보고서";
//		input = "배수로";
//		input = "기성고사정기준";
//		input = "기준(정식)은";
//		input = "사업  기계 보일러";
//		input = "사업((기계)보일러)";
//		input = "사업((기계)보일러)가";
//		input = "(기계)에";
//		input = "되었다";
		input="홍재룡(洪在龍)이며";
//		input="[2015/12/12] 일일감리보고서";
		input="총·균·쇠";
		
		KoreanAnalyzer a = new KoreanAnalyzer();
		a.setHasOrigin(false);
		a.setQueryMode(false);
		a.setOriginCNoun(true);
		a.setDecompound(true);
		a.setRemovePunctuation(true);
		a.setHasVerb(true);
		
		StringBuilder actual = new StringBuilder();
		
	     TokenStream ts = a.tokenStream("bogus", input);
	     CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
	     OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
	     PositionIncrementAttribute posIncrAtt = ts.addAttribute(PositionIncrementAttribute.class);
	     
	      ts.reset();
	      while (ts.incrementToken()) {
	        if(posIncrAtt.getPositionIncrement()==1) 
	        	actual.append('\n');
	        actual.append(termAtt.toString()).append(":"+offsetAtt.startOffset()+","+offsetAtt.endOffset()+"/"+posIncrAtt.getPositionIncrement());
	        actual.append(' ');
	      }
	      System.out.println(actual);
	     
//	      assertEquals("for line " + line + " input: " + input, expected, actual.toString());
	      ts.end();
	      ts.close();
	}
	
	public void testConvertUnicode() throws Exception {
		char c = 0x772C;
		System.out.println(c);
		
		int code = '領';
		System.out.println(code);
		
		System.out.println(Character.getType('&'));
	}
	
	public void testCharacter() throws Exception {
		String str = "領)(az01가-'.";
		for(int i=0;i<str.length();i++) {
			char c = str.charAt(i);
			System.out.println(c+":"+Character.isLetterOrDigit(c));
		}
	}
}
