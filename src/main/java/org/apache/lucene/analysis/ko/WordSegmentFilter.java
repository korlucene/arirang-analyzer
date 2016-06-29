package org.apache.lucene.analysis.ko;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.morph.AnalysisOutput;
import org.apache.lucene.analysis.ko.morph.CompoundEntry;
import org.apache.lucene.analysis.ko.morph.MorphException;
import org.apache.lucene.analysis.ko.morph.PatternConstants;
import org.apache.lucene.analysis.ko.morph.WordSegmentAnalyzer;
import org.apache.lucene.analysis.ko.utils.DictionaryUtil;
import org.apache.lucene.analysis.ko.utils.MorphUtil;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public final class WordSegmentFilter extends TokenFilter {

    private final LinkedList<KoreanToken> outQueue = new LinkedList<KoreanToken>();
    
    private boolean modeQueue = false;
    
    private WordSegmentAnalyzer segmentAnalyzer;
    
    private boolean hasOrigin = true;
    
    // used to check whether if incomming token is produced by the same text as the previous token at korean filter
    private List<AnalysisOutput> morphOutputs;
    
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final MorphemeAttribute morphAtt = addAttribute(MorphemeAttribute.class);
    
	protected WordSegmentFilter(TokenStream input) {
		super(input);
		segmentAnalyzer = new WordSegmentAnalyzer();
	}

	protected WordSegmentFilter(TokenStream input, boolean hasOrigin) {
		this(input);
		this.hasOrigin = hasOrigin;
	}
	
	@Override
	public boolean incrementToken() throws IOException {
		
    	if(modeQueue && outQueue.size()>0) {
    		setAttributesFromQueue();
    		return true;
    	}

    	modeQueue = false;
    	
        while (input.incrementToken()) {
        	
        	KoreanToken kToken = morphAtt.getToken();
        	
        	if(kToken==null) return true;

        	if(morphOutputs!=null) {
        		if(morphOutputs==kToken.getOutputs()) {
        			continue; // current incomming token is removed because of it is duplicated to Word Segment output.
        		}else {
        			morphOutputs = null;
        		}
        	}
        	
        	assert kToken.getOutputs().size()>0;
        	
        	if(posIncrAtt.getPositionIncrement()==0 || 
        			kToken==null || kToken.getOutputs().size()==0 
        			|| kToken.getOutputs().get(0).getScore()>AnalysisOutput.SCORE_COMPOUNDS ||
        					(kToken.getOutputs().get(0).getScore()==AnalysisOutput.SCORE_COMPOUNDS && 
        					!(containJosa(kToken) || MorphUtil.hasVerbOnly(kToken.getOutputs().get(0).getStem()))))
        		return true;
        	
        	String term = termAtt.toString();
        	try {
        		
				if(hasOrigin) outQueue.add(new KoreanToken(termAtt.toString(), offsetAtt.startOffset(), posIncrAtt.getPositionIncrement()));
				
				List<List<AnalysisOutput>> segments = segmentAnalyzer.analyze(term);
				if(segments.size()<2) {
					if(hasOrigin) outQueue.removeFirst();
					return true;
				}
				
				int offset = 0;
				for(int i=0;i<segments.size();i++)
				{
					assert segments.get(i).size()>0;
					
					String word = segments.get(i).get(0).getSource();
					List<CompoundEntry> entries = segments.get(i).get(0).getCNounList();
					int posInc = i==0 ? 0 : 1;
					
					if(hasOrigin) outQueue.add(new KoreanToken(word,offsetAtt.startOffset()+offset, posInc));
					
					if(entries.size()>1) {
						if(!hasOrigin || !word.equals(segments.get(i).get(0).getStem()))
							outQueue.add(new KoreanToken(segments.get(i).get(0).getStem(),offsetAtt.startOffset()+offset, hasOrigin ? 0 : posInc));
						
						int innerOffset = offset;
						for(int k=0;k<entries.size();k++) {
							CompoundEntry ce = entries.get(k);
							int innerPosInc = k==0 ? 0 : 1;
							outQueue.add(new KoreanToken(ce.getWord(),offsetAtt.startOffset()+innerOffset, innerPosInc));
							innerOffset += ce.getWord().length();
						}
					} else {
						if(segments.get(i).get(0).getPatn()>=PatternConstants.PTN_VM && segments.get(i).get(0).getPatn()<PatternConstants.PTN_ZZZ) {
							if(!hasOrigin) outQueue.add(new KoreanToken(word,offsetAtt.startOffset()+offset, posInc));
						} else {
							if(!hasOrigin || !word.equals(segments.get(i).get(0).getStem()))
								outQueue.add(new KoreanToken(segments.get(i).get(0).getStem(),offsetAtt.startOffset()+offset, hasOrigin ? 0 : posInc));
						}
					}
					
					offset += word.length();
				}
				
				captureState();
				modeQueue = true;
				morphOutputs = kToken.getOutputs();
				
	            if (!outQueue.isEmpty()) {
					setAttributesFromQueue();
					return true;
	            }
	            
			} catch (MorphException e) {
				throw new RuntimeException(e);
			}
        }
        
    	if(outQueue.size()>0) {
    		setAttributesFromQueue();
    		return true;
    	}
    	
        return false;
	}

	private boolean containJosa(KoreanToken kToken)  {
		
		List<AnalysisOutput> outputs = kToken.getOutputs();
		if(outputs.size()==0 || outputs.get(0).getCNounList().size()==0) return false;
		
		try {
			List<CompoundEntry> entries = outputs.get(0).getCNounList();
			for(int i=0;i<entries.size();i++) {
				if(DictionaryUtil.existJosa(entries.get(i).getWord())) return true;
			}
		}catch(MorphException e) {
			throw new RuntimeException(e);
		}

		return false;
	}
	
    private void setAttributesFromQueue() {
        final KoreanToken iw = outQueue.removeFirst();

        termAtt.setEmpty().append(iw.getTerm());
        offsetAtt.setOffset(iw.getOffset(), iw.getOffset() + iw.getLength());
        posIncrAtt.setPositionIncrement(iw.getPosInc());
    }
    
    @Override
    public void reset() throws IOException {
        super.reset();
        outQueue.clear();
    }
    
}
