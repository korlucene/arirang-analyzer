package org.apache.lucene.ko;

import java.io.File;

import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class KoreanNTokenizerTest extends TestCase {

    public void testKoreanPattern() throws Exception {
        String[] texts = new String[] {
            "나는 자랑스러운 태극기(태극기) 앞에"
        };

        Pattern ptn = Pattern.compile("[\"'\\[\\(\\{]+([^\"'\\]\\)\\}]+)[\"'\\]\\)\\}]+\"");
        ptn = Pattern.compile("[\\(]+([^\\)]+)[\\)]+");

        for(String text : texts) {
           String[] fragments = text.split("[ ]+");
           for(String fragment : fragments) {
               Matcher m = ptn.matcher(fragment);
               while (m.find()) {
                   System.out.println(m.group(1));
               }

           }

        }
    }

    private String[] sources = new String[] {
            "\"동작하는 가장 단순한 온라인 데이터베이스\"[1]라고",
            "위키위키웹(WikiWikiWeb)을 첫 위키 소프트웨어인 위키위키웹(WikiWikiWeb)을 만든 워드 커닝엄은 위키를 \"동작하는 가장 단순한 온라인 데이터베이스\"[1]라고 설명했다.",
            "위키 웹사이트의 한 문서는 \"위키 문서\"라 부르며, 하이퍼링크로 서로 연결된 전체 문서를 \"위키\"라 한다.",
            "(예를 들어 \"wiki\"라는 문서를 \"WiKi\"로 표기한다거나 한다.)"
    };

    public void testKoreanNTokenizer() throws Exception {

        List<String> lines = IOUtils.readLines(new FileInputStream(new File("arirang.lucene-analyzer-4.6/resources/tokenizer/tokensample.txt")));

//        lines = Arrays.asList(new String[]{"ショッピングセンター（英: shopping center）は、複数の小売店舗やフード・サービス業、美容院・旅行代理店などの第4次産業も入居する商業施設である。==>ショッピングセンター/英/shopping/centerは/複数の小売店舗やフード・サービス業/美容院・旅行代理店などの第4次産業も入居する商業施設である"});

        for(String line : lines) {
            System.out.println(line);

            String[] sample = StringUtils.split(line,"==>");
            if(sample.length!=2) continue;

            StringReader reader = new StringReader(sample[0]);
            KoreanTokenizer tokenizer = new KoreanTokenizer();
            tokenizer.reset();
            CharTermAttribute termAtt = tokenizer.addAttribute(CharTermAttribute.class);

            StringBuffer sb = new StringBuffer();
            while(tokenizer.incrementToken()) {
                if(sb.length()>0) sb.append("/");
                sb.append(termAtt.toString());
            }

            TestCase.assertEquals(sample[1], sb.toString());

        }

    }
    
    public void testUtils() throws Exception {
    	List<Integer> list = new ArrayList<Integer>();
    	for(int i=0;i<100;i++) {
    		list.add(i);
    	}
    	
    	removeLast(list, 50);
    	
    	System.out.println(list.size());
    	
    	for(int i=0;i<100;i++) {
    		list.add(i);
    	}
    	
    	removeLast(list, 50);
    	
    	System.out.println(list.size());
    	
//    	for(int i=0;i<subList.size();i++) {
//    		list.remove(subList.get(i));
//    	}

    }
    
    private void removeLast(List<Integer> list, int start) {
    	List<Integer> removed = new ArrayList();
    	for(int i=start;i<list.size();i++) {
    		removed.add(list.get(i));
    	}
    	
    	for(Integer o : removed) {
    		list.remove(0);
    	}
    	
    	removed = null;
    }

}