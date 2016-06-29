package org.apache.lucene.analysis.ko;

import org.apache.lucene.analysis.ko.morph.AnalysisOutput;

import java.util.List;

/**
 * Created by SooMyung(soomyung.lee@gmail.com) on 2014. 7. 28.
 */

public class KoreanToken {

    private String term;

    private int offset;

    private int length;

    private int posInc = 1;

    private int posLen = 1;

    private boolean endWithPunctuation = false;

    private List<AnalysisOutput> outputs = null;

    public KoreanToken(String term, int offset) {
        this.term = term;
        this.offset = offset;
        this.length = this.term.length();
    }

    public KoreanToken(String term, int offset, int posInc) {
        this(term,offset);
        this.posInc = posInc;
    }

    public KoreanToken(String term, int offset, int posInc, List<AnalysisOutput> outputs) {
        this(term, offset, posInc);
        this.outputs = outputs;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public int getPosInc() {
        return posInc;
    }

    public int getPosLen() {
        return posLen;
    }

    public void setPosLen(int posLen) {
        this.posLen = posLen;
    }

    public void setPosInc(int posInc) {
        this.posInc = posInc;
    }

    public boolean isEndWithPunctuation() {
        return endWithPunctuation;
    }

    public void setEndWithPunctuation(boolean endWithPunctuation) {
        this.endWithPunctuation = endWithPunctuation;
    }

    public List<AnalysisOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<AnalysisOutput> outputs) {
        this.outputs = outputs;
    }
}
