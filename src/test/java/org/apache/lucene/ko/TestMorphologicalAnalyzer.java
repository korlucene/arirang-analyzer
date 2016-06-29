package org.apache.lucene.ko;

import java.util.List;


import org.apache.lucene.analysis.ko.morph.AnalysisOutput;
import org.apache.lucene.analysis.ko.morph.CompoundEntry;
import org.apache.lucene.analysis.ko.morph.MorphAnalyzer;

import junit.framework.TestCase;

public class TestMorphologicalAnalyzer extends TestCase {

	public void testAnalyze() throws Exception {
		
		String[] terms = new String[]{
//			"아름다운", "푸미폰국왕은", "정교로운","냉방을",
//			"하고", 
//			"전기를",
			"만들고","공장을","가동하는",
			"진산세고",
			"김경은"
		};
		
		long start = System.currentTimeMillis();
		
		MorphAnalyzer morphAnalyzer = new MorphAnalyzer();
		for(String term : terms) {
			List<AnalysisOutput> results = morphAnalyzer.analyze(term);
			
			for(AnalysisOutput o : results) {
				
				List<CompoundEntry> entries = o.getCNounList();
				for(CompoundEntry entry : entries) {
					System.out.print(entry.getWord()+"/");
				}
				
				if(entries.size()==0) System.out.print(o.getStem());
				System.out.print("<"+o.getPatn()+">->"+o.getScore());
				
				System.out.println();
			}
		}
		
		System.out.println((System.currentTimeMillis()-start)+"ms");
		
	}

}
