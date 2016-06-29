package org.apache.lucene.ko;

import junit.framework.TestCase;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Test;

public class WordSegmentFilterTest extends TestCase {

	@Test
	public void testAnalyze() throws Exception {
		String input = "긴하루";
//		input = "明憲淑敬睿仁正穆弘聖章純貞徽莊昭端禧粹顯懿獻康綏裕寧慈溫恭安孝定王后";
		input = "무죄다라고 말할수";
		input = "김다은";
		input = "예수란 할렐루야";
		
		KoreanAnalyzer a = new KoreanAnalyzer();
		a.setHasOrigin(true);
		a.setOriginCNoun(true);
		a.setBigrammable(false);
		a.setQueryMode(true);
		a.setWordSegment(true);
		
		StringBuilder actual = new StringBuilder();
		
	     TokenStream ts = a.tokenStream("bogus", input);
          CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
          PositionIncrementAttribute posIncrAtt = ts.addAttribute(PositionIncrementAttribute.class);
          OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
          ts.reset();

	      while (ts.incrementToken()) {
              System.out.println(termAtt.toString()+":"+posIncrAtt.getPositionIncrement()+"("+offsetAtt.startOffset()+","+offsetAtt.endOffset()+")");
	      }
	      System.out.println(actual);
	     
	      ts.end();
	      ts.close();
	}
}
