package org.apache.lucene.analysis.ko;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

/**
 * 
 * @author isumyeong
 * extracting term removed a punctuation from a term with a punctuation
 * example: 고소/고발 => 고소고발
 */
public class RemovePunctuationFilter extends TokenFilter {


    private final LinkedList<Token> outQueue = new LinkedList<Token>();

    private State currentState = null;
	
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    
    /**
     * Construct a token stream filtering the given input.
     *
     * @param input
     */
    public RemovePunctuationFilter(TokenStream input) {
        super(input);
    }
    
    @Override
    public final boolean incrementToken() throws IOException {

        if (!outQueue.isEmpty()) {
            restoreState(currentState);
            setAttributesFromQueue(false);
            return true;
        }

        while (input.incrementToken()) {
        	
        	if (keywordAtt.isKeyword()) 
        		return true;
        	
        	String term = termAtt.toString();
        	
        	if(term.length()==1 && isPunctuation(term.charAt(0))) 
        		continue;
        	
            if(!containPunctuation(term)) 
            	return true;
            
            extractTermWithoutPunctuation(term);

            if (!outQueue.isEmpty()) {
                setAttributesFromQueue(true);
                return true;
            }
        }

        return false;
    }

    private void setAttributesFromQueue(boolean isFirst) {
        final Token iw = outQueue.removeFirst();

        if (isFirst && !outQueue.isEmpty()) {
            currentState = captureState();
        }

        termAtt.setEmpty().append(iw.getTerm());
        offsetAtt.setOffset(iw.getOffset(), iw.getEndOffset());
        posIncrAtt.setPositionIncrement(iw.getIncrement());
    }

    private void extractTermWithoutPunctuation(String term) {
        if(term.length()<2) return;
        StringBuffer sb = new StringBuffer();

        for(int i=0;i<term.length();i++) {
            if(!isPunctuation(term.charAt(i))) {
            	sb.append(term.charAt(i));
            } 
        }

        // add original text
        outQueue.add(new Token(termAtt.toString(), offsetAtt.startOffset(), posIncrAtt.getPositionIncrement()));
        
        if(sb.length()>0) 
        	outQueue.add(new Token(sb.toString(), offsetAtt.startOffset(), offsetAtt.endOffset(), 0));

    }

    private boolean containPunctuation(String term) {
        for(int i=0;i<term.length()-1;i++) {
            if(isPunctuation(term.charAt(i))) return true;
        }
        return false;
    }

    private static boolean isPunctuation(char ch) {
        switch(Character.getType(ch)) {
            case Character.SPACE_SEPARATOR:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.CONTROL:
            case Character.FORMAT:
            case Character.DASH_PUNCTUATION:
            case Character.START_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.CONNECTOR_PUNCTUATION:
            case Character.OTHER_PUNCTUATION:
            case Character.MATH_SYMBOL:
            case Character.CURRENCY_SYMBOL:
            case Character.MODIFIER_SYMBOL:
            case Character.OTHER_SYMBOL:
            case Character.INITIAL_QUOTE_PUNCTUATION:
            case Character.FINAL_QUOTE_PUNCTUATION:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        outQueue.clear();
        currentState = null;
    }

    private class Token {
        int offset;
        
        int endoffset;

        int increment = 1;

        String term;

        public Token(String term, int offset) {
        	this(term,offset,1);
        }

        public Token(String term, int offset, int inc) {
            this(term,offset,offset+term.length(),inc);
        }

        public Token(String term, int offset, int endoffset, int inc) {
            this.term=term;
            this.offset=offset;
            this.endoffset=endoffset;
            this.increment =inc;
        }
        
        public int getOffset() {
            return offset;
        }

        @SuppressWarnings("unused")
		public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getEndOffset() {
            return endoffset;
        }

        @SuppressWarnings("unused")
		public void setEndOffset(int offset) {
            this.endoffset = offset;
        }
        
        public int getIncrement() {
            return increment;
        }

        @SuppressWarnings("unused")
		public void setIncrement(int increment) {
            this.increment = increment;
        }

        public String getTerm() {
            return term;
        }

        @SuppressWarnings("unused")
		public void setTerm(String term) {
            this.term = term;
        }
        
        public boolean equal(Token another) {
        	
        	return (this.offset==another.offset 
        			&& this.term.equals(another.getTerm()));
        }
    }

}
