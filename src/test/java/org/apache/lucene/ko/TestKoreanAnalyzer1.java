package org.apache.lucene.ko;

import junit.framework.TestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.ko.MorphemeAttribute;
import org.apache.lucene.analysis.tokenattributes.*;

import java.io.StringReader;

/**
 * Created by SooMyung(soomyung.lee@gmail.com) on 2014. 7. 30.
 */

public class TestKoreanAnalyzer1 extends TestCase {

    public void testKoreanAnalzer() throws Exception {

        String[] sources = new String[] {
//                "고려 때 중랑장(中郞將) 이돈수(李敦守)의 12대손이며",
//                "이돈수(李敦守)의",
//                "K·N의 비극",
//                "金靜子敎授",
//                "天國의",
//                  "기술천이",
//                "12대손이며",
//        		"明憲淑敬睿仁正穆弘聖章純貞徽莊昭端禧粹顯懿獻康綏裕寧慈溫恭安孝定王后",
//        		"홍재룡(洪在龍)의",
                "정식시호는 명헌숙경예인정목홍성장순정휘장소단희수현의헌강수유령자온공안효정왕후(明憲淑敬睿仁正穆弘聖章純貞徽莊昭端禧粹顯懿獻康綏裕寧慈溫恭安孝定王后)이며 돈령부영사(敦寧府領事) 홍재룡(洪在龍)의 딸이다. 1844년, 헌종의 정비(正妃)인 효현왕후가 승하하자 헌종의 계비로써 중궁에 책봉되었으나 5년 뒤인 1849년에 남편 헌종이 승하하고 철종이 즉위하자 19세의 어린 나이로 대비가 되었다. 1857년 시조모 대왕대비 순원왕후가 승하하자 왕대비가 되었다."
        };

        KoreanAnalyzer analyzer = new KoreanAnalyzer();

        for(String source : sources) {
            TokenStream stream = analyzer.tokenStream("dummy", new StringReader(source));

            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            PositionIncrementAttribute posIncrAtt = stream.addAttribute(PositionIncrementAttribute.class);
            PositionLengthAttribute posLenAtt = stream.addAttribute(PositionLengthAttribute.class);
            TypeAttribute typeAtt = stream.addAttribute(TypeAttribute.class);
            OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
            MorphemeAttribute morphAtt = stream.addAttribute(MorphemeAttribute.class);
            stream.reset();

            while(stream.incrementToken()) {
                System.out.println(termAtt.toString()+":"+posIncrAtt.getPositionIncrement()+"("+offsetAtt.startOffset()+","+offsetAtt.endOffset()+")");
            }
            stream.close();
        }

    }
}
