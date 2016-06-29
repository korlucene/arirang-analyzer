package org.apache.lucene.ko;


import java.util.List;

import org.apache.lucene.analysis.ko.morph.CompoundEntry;
import org.apache.lucene.analysis.ko.morph.CompoundNounAnalyzer;

import junit.framework.TestCase;

public class TestCompoundSegment extends TestCase {
	
	public void testSegmentCompound() throws Exception {
		
		String[] inputs = new String[] {
				"학교R",
				"學校"
				};
		
		for(String input : inputs) {
			CompoundNounAnalyzer analyzer = new CompoundNounAnalyzer();
			
			List<CompoundEntry> entries = analyzer.analyze(input);
			if(entries==null){
				System.out.println("null");
				return;
			}
			for(CompoundEntry entry : entries) {
				System.out.println(entry.getWord());
			}
		}

		
	}

}
