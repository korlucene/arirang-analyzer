package org.apache.lucene.analysis.ko;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.morph.CompoundEntry;
import org.apache.lucene.analysis.ko.morph.CompoundNounAnalyzer;
import org.apache.lucene.analysis.ko.morph.MorphException;
import org.apache.lucene.analysis.ko.morph.WordEntry;
import org.apache.lucene.analysis.ko.utils.DictionaryUtil;
import org.apache.lucene.analysis.ko.utils.HanjaUtils;
import org.apache.lucene.analysis.tokenattributes.*;

import java.io.IOException;
import java.util.*;

/**
 * Created by SooMyung(soomyung.lee@gmail.com) on 2014. 7. 29.
 */

public final class HanjaMappingFilter extends TokenFilter {

    private final LinkedList<KoreanToken> outQueue = new LinkedList<KoreanToken>();

    private State currentState = null;
    
    private static int maxCandidateSize = 5;

    private final CompoundNounAnalyzer cnAnalyzer = new CompoundNounAnalyzer();

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    /**
     * Construct a token stream filtering the given input.
     *
     * @param input
     */
    protected HanjaMappingFilter(TokenStream input) {
        super(input);
        cnAnalyzer.setExactMach(false);
    }

    @Override
    public boolean incrementToken() throws IOException {

        if (!outQueue.isEmpty()) {
            restoreState(currentState);
            setAttributesFromQueue(false);
            return true;
        }

        while (input.incrementToken()) {
            if(posIncrAtt.getPositionIncrement()==0 ||
            		!hasHanja(termAtt.toString())) return true;

            int startOffset = offsetAtt.startOffset();

            List<StringBuffer> terms = splitByHanja(termAtt.toString());
            int offset = 0;
            for(StringBuffer sb : terms) {
                if(sb.length()==0) continue;
                if(isHanja(sb.charAt(0)))
                    try {
                        mapHanjaToHangul(sb);
                    } catch (MorphException e) {
                        throw new RuntimeException(e);
                    }
                else
                    outQueue.add(new KoreanToken(sb.toString(), offset + startOffset, 1));
                offset += sb.length();
            }

            if (!outQueue.isEmpty()) {
                setAttributesFromQueue(true);
                return true;
            }
        }

        return false;
    }

    private void setAttributesFromQueue(boolean isFirst) {
        final KoreanToken iw = outQueue.removeFirst();

        if (isFirst && !outQueue.isEmpty()) {
            termAtt.setEmpty();
            currentState = captureState();
        }

        termAtt.setEmpty().append(iw.getTerm());
        offsetAtt.setOffset(iw.getOffset(), iw.getOffset() + iw.getLength());
        posIncrAtt.setPositionIncrement(iw.getPosInc());

    }

    private boolean hasHanja(String term) {
        for (int i=0; i<term.length();i++) {
            if(isHanja(term.charAt(i))) return true;
        }
        return false;
    }

    private List<StringBuffer> splitByHanja(String term) {
        List<StringBuffer> result = new ArrayList<StringBuffer>();

        // TODO : need to modify code
        if(true) {
            result.add(new StringBuffer(term));
            return result;
        }

        @SuppressWarnings("unused")
		StringBuffer sb = null;
        boolean wasHanja = false;
        for (int i=0; i<term.length();i++) {
            if(isHanja(term.charAt(i)))  {
                if(!wasHanja || sb==null) {
                    sb = new StringBuffer();
                    result.add(sb);
                }
                sb.append(term.charAt(i));
                wasHanja=true;
                continue;
            }
            if(wasHanja || sb==null) {
                sb = new StringBuffer();
                result.add(sb);
            }
            sb.append(term.charAt(i));
            wasHanja = false;
        }
        return result;
    }

    private void mapHanjaToHangul(StringBuffer term) throws MorphException {

        outQueue.add(new KoreanToken(term.toString(),offsetAtt.startOffset()));
        if(term.length()<2) return; // 1글자 한자는 색인어로 한글을 추출하지 않는다.

        List<StringBuffer> candiList = new ArrayList<StringBuffer>();
        candiList.add(new StringBuffer());

        List<StringBuffer> removeList = new ArrayList<StringBuffer>(); // 제거될 후보를 저장
        
        for(int i=0;i<term.length();i++) {

            char[] chs = HanjaUtils.convertToHangul(term.charAt(i));
            if(chs==null) continue;

           int caniSize = candiList.size();

            for(int j=0;j<caniSize;j++) {
                String origin = candiList.get(j).toString();

                for(int k=0;k<chs.length;k++) { // 추가로 생성된 음에 대해서 새로운 텍스트를 생성한다.

                    if(k==4) break; // 4개 이상의 음을 가지고 있는 경우 첫번째 음으로만 처리를 한다.

                    StringBuffer sb = candiList.get(j);
                    if(k>0) sb = new StringBuffer(origin);

                    sb.append(chs[k]);
                    if(k>0)  candiList.add(sb);
                    
                	Iterator<String[]> iter = DictionaryUtil.findWithPrefix(sb.toString());
                    if(!iter.hasNext() && !removeList.contains(sb)) removeList.add(sb); // 사전에 없으면 삭제 후보
                }
            }
            
            // 후보가 정해진 갯수보다 크면 이후는 삭제하여 지나친 메모리 사용을 방지한다.
            if(candiList.size()>maxCandidateSize) {
            	removeLast(candiList,removeList, maxCandidateSize);
            }
        }

	    if(removeList.size()!=candiList.size()) { // 사전에서 찾은 단어가 하나도 없다면..
			for(StringBuffer rsb : removeList) {
			    candiList.remove(rsb);
			}
			removeList.clear();
	    }
	        
        int noCandidate = candiList.size();
        int maxDecompounds = 0;
        List<List<CompoundEntry>> compoundList = new ArrayList<List<CompoundEntry>>();
        for(int i=0;i<noCandidate;i++) {
            outQueue.add(new KoreanToken(candiList.get(i).toString(),offsetAtt.startOffset(), 0));
            List<CompoundEntry> results = confirmCNoun(candiList.get(i).toString());
            compoundList.add(results);
            if(maxDecompounds<results.size()) maxDecompounds = results.size();
        }

        // 추출된 명사가 복합명사인 경우 분리한다.
        if(maxDecompounds>1) {
            int[] pos = new int[noCandidate];
            int[] offset =new int[noCandidate];
            int[] index =new int[noCandidate];
            int minOffset = term.length();
            
            for(int i=0;i<maxDecompounds;i++) {
            	Map<String, CompoundEntry> dupcheck = new HashMap<String, CompoundEntry>();
            	int min = term.length();
            	boolean done = false;
            	for(int j=0;j<noCandidate;j++){
            		if(offset[j]>minOffset || compoundList.get(j).size()<=index[j]) continue;
            		int posInc = i!=0 && !done ? 1 : 0;
            		CompoundEntry entry = compoundList.get(j).get(index[j]);
            		pos[j] += entry.getWord().length();
            		if(!done && pos[j]<=term.length())
            			outQueue.add(new KoreanToken(term.substring(offset[j],pos[j]),offsetAtt.startOffset() + offset[j], posInc));
            		if(dupcheck.get(entry.getWord())==null) {
            			outQueue.add(new KoreanToken(entry.getWord(),offsetAtt.startOffset() + offset[j], 0));
            			dupcheck.put(entry.getWord(), entry);
            		}
            		offset[j] = pos[j];
            		index[j]++;
            		if(min>offset[j]) min = offset[j];
            		done = true;
            	}
            	minOffset = min;
            }
        }
    }

    private boolean isHanja(char c) {
        if((c>=0x3400&&c<=0x4dbf) || (c>=0x4e00&&c<=0x9fff) || (c>=0xf900&&c<=0xfaff))
            return true;
        return  false;
    }

    private List<CompoundEntry> confirmCNoun(String input) throws MorphException {

        WordEntry cnoun = DictionaryUtil.getAllNoun(input);
        if(cnoun!=null && cnoun.getFeature(WordEntry.IDX_NOUN)=='2') {
            return cnoun.getCompounds();
        }

        return cnAnalyzer.analyze(input);
    }

    private void removeLast(List<StringBuffer> list, List<StringBuffer> removeCandi, int start) {
    	List<StringBuffer> removed = new ArrayList<StringBuffer>();
    	for(int i=start;i<list.size();i++) {
    		removed.add(list.get(i));
    	}
    	
    	for(Object o : removed) {
    		list.remove(o);
    		removeCandi.remove(o);
    	}
    	
    	removed=null;
    }
    
    @Override
    public void reset() throws IOException {
        super.reset();
        outQueue.clear();
        currentState = null;
    }
}
