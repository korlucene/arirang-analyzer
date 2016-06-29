package org.apache.lucene.analysis.ko;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.morph.AnalysisOutput;
import org.apache.lucene.analysis.ko.morph.CompoundEntry;
import org.apache.lucene.analysis.ko.morph.MorphAnalyzer;
import org.apache.lucene.analysis.ko.morph.MorphException;
import org.apache.lucene.analysis.ko.morph.PatternConstants;
import org.apache.lucene.analysis.ko.morph.WordEntry;
import org.apache.lucene.analysis.ko.utils.DictionaryUtil;
import org.apache.lucene.analysis.ko.utils.MorphUtil;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public final class KoreanFilter extends TokenFilter {

  private final LinkedList<KoreanToken> morphQueue = new LinkedList<KoreanToken>();
  private final MorphAnalyzer morph;
  
  private State currentState = null;
  
	private final boolean bigrammable;
	private final boolean hasOrigin;
	private final boolean originCNoun;
	private final boolean queryMode;
	private final boolean decompound;

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final MorphemeAttribute morphAtt = addAttribute(MorphemeAttribute.class);

  private static final String KOREAN_TYPE = KoreanTokenizer.TYPE_KOREAN;
    
  public KoreanFilter(TokenStream input) {
    this(input, true);
  }

  /**
   * 
   * @param input  input token stream
   * @param bigram  Whether the bigram index term return or not.
   */
  public KoreanFilter(TokenStream input, boolean bigram) {
    this(input, bigram, false);
  }
  
  public KoreanFilter(TokenStream input, boolean bigram, boolean has) {
    this(input, bigram, has, false);
  }
  
  public KoreanFilter(TokenStream input, boolean bigram, boolean has, boolean exactMatch) {
    this(input, bigram, has, exactMatch, true);
  }

  public KoreanFilter(TokenStream input, boolean bigram, boolean has, boolean exactMatch, boolean cnoun) {
	  this(input, bigram, has,exactMatch,cnoun, false);
  }
  
  public KoreanFilter(TokenStream input, boolean bigram, boolean has, boolean exactMatch, 
		  boolean cnoun, boolean isQuery) {
	  this(input, bigram, has,exactMatch,cnoun, false, true);
  }
  
  public KoreanFilter(TokenStream input, boolean bigram, boolean has, boolean exactMatch, 
		  boolean cnoun, boolean isQuery, boolean decompound) {
	  
    super(input);
    this.bigrammable = bigram;
    this.hasOrigin = has;
    this.originCNoun = cnoun;
    this.morph = new MorphAnalyzer();
    this.morph.setExactCompound(exactMatch);
    this.queryMode = isQuery;
    this.decompound = decompound;
    
//    if(this.queryMode)
//    	this.morph.setDivisibleOne(false);
  }
  
  public boolean incrementToken() throws IOException {
    if (!morphQueue.isEmpty()) {
      restoreState(currentState);
      setAttributesFromQueue(false);
      return true;
    }

    while (input.incrementToken()) {
      final String type = typeAtt.type();
      String term = termAtt.toString();
      if (KOREAN_TYPE.equals(type)) {
        try {
			analysisKorean(term);
		} catch (MorphException e) {
			throw new RuntimeException(e);
		}
      } else {
        return true; // pass anything else thru
      }        
  
      if (!morphQueue.isEmpty()) {
        setAttributesFromQueue(true);
        return true;
      }
    }

    return false;
  }
  

  private void setAttributesFromQueue(boolean isFirst) {
    final KoreanToken iw = morphQueue.removeFirst();
    if (isFirst && !morphQueue.isEmpty()) {
      // our queue has more elements remaining (e.g. we decompounded)
      // capture state for those. We set the term attribute to be empty
      // so we save lots of array copying later.
      termAtt.setEmpty();
      currentState = captureState();
    }
 
    termAtt.setEmpty().append(iw.getTerm());
    offsetAtt.setOffset(iw.getOffset(), iw.getOffset() + iw.getLength());
    morphAtt.setToken(iw);

    // on the first Token we preserve incoming increment:
    if (!isFirst) {
      posIncrAtt.setPositionIncrement(iw.getPosInc());
    }
    
    String type = TokenUtilities.getType(iw.getTerm().toCharArray(), iw.getTerm().length());
    typeAtt.setType(type);
    
    // TODO: How to handle PositionLengthAttribute correctly?
  }
  
  /**
   * Analyze korean text
 * @throws MorphException 
   */
  private void analysisKorean(String input) throws MorphException {

//	input = trimHangul(input);
    List<AnalysisOutput> outputs = morph.analyze(input);
    if(outputs.size()==0) return;
    
    Map<String,KoreanToken> map = new LinkedHashMap<String,KoreanToken>();
    if(hasOrigin) map.put("0:"+input, new KoreanToken(input,offsetAtt.startOffset()));

    extractKeyword(outputs,offsetAtt.startOffset(), map, 0); 

    Collection<KoreanToken> values = map.values();
    for(KoreanToken kt : values) {
       kt.setOutputs(outputs);
    }

      morphQueue.addAll(map.values());
  }
  
  /**
   * remove the preserved punctuation character followed by the input hangul word. (ex. 찾아서-)
   * @param input
   * @return
   */
  private String trimHangul(String input) {
	  
	  int minpos = input.length();
	  for(int i=input.length()-1 ; i>=0 ; i--) {
		  if(MorphUtil.isHanSyllable(input.charAt(i))) break;
		  minpos = i;
	  }
	  
	  if(minpos == input.length()) return input;
	  
	  return input.substring(0, minpos);
  }
  
  private String removeSymbol(String input) {
	  
	  int minpos = input.length();
	  for(int i=input.length()-1 ; i>=0 ; i--) {
		  if(Character.isLetterOrDigit(input.charAt(i))) break;
		  minpos = i;
	  }
	  
	  if(minpos == input.length()) return input;
	  
	  return input.substring(0, minpos);
  }
  
	private void extractKeyword(List<AnalysisOutput> outputs, int startoffset,
			Map<String, KoreanToken> map, int position) {

		int maxDecompounds = 0;
		int maxStem = 0;

		for (AnalysisOutput output : outputs) {
			if (queryMode && hasOrigin
					&& output.getScore() == AnalysisOutput.SCORE_ANALYSIS
					&& output.getCNounList().size() < 2)
				break;

			if (output.getPos() == PatternConstants.POS_VERB)
				continue; // extract keywords from only noun

			if (output.getCNounList().size() > maxDecompounds)
				maxDecompounds = output.getCNounList().size();
			if (!originCNoun && output.getCNounList().size() > 0)
				continue; // except compound nound

			int inc = map.size() > 0 ? 0 : 1;

			if (queryMode
					&& invalidAnalysis(output)
					&& output.getSource().length() - 1 == output.getStem()
							.length()) {
				String source = removeSymbol(output.getSource());
				map.put(position + ":" + source, new KoreanToken(source,
						startoffset, inc));
				// map.put(position+":"+output.getStem(), new
				// KoreanToken(output.getStem(),startoffset,0));
			} else {
				String stem = removeSymbol(output.getStem());
				map.put(position + ":" + stem, new KoreanToken(stem,
						startoffset, inc));
			}

			if (output.getStem().length() > maxStem)
				maxStem = output.getStem().length();

			// extract the first stem as the keyword for the query processing
			if (queryMode)
				break;
		}

		if (!decompound)
			return;

		if (maxDecompounds > 1) {
			for (int i = 0; i < maxDecompounds; i++) {
				position += i;

				int cPosition = position;
				for (AnalysisOutput output : outputs) {
					if (output.getPos() == PatternConstants.POS_VERB
							|| output.getCNounList().size() <= i)
						continue;

					CompoundEntry cEntry = output.getCNounList().get(i);
					int cStartoffset = getStartOffset(output, i) + startoffset;
					int inc = i == 0 ? 0 : 1;
					map.put((cPosition) + ":" + cEntry.getWord(),
							new KoreanToken(cEntry.getWord(), cStartoffset, inc));

					if (bigrammable && !cEntry.isExist())
						cPosition = addBiagramToMap(cEntry.getWord(),
								cStartoffset, map, cPosition);

					// extract the words derived from the first stem as the
					// keyword for the query processing
					if (queryMode)
						break;
				}
			}
		} else {
			for (AnalysisOutput output : outputs) {
				if (output.getPos() == PatternConstants.POS_VERB)
					continue;

				if (bigrammable
						&& output.getScore() < AnalysisOutput.SCORE_COMPOUNDS)
					addBiagramToMap(output.getStem(), startoffset, map,
							position);
			}
		}
	}
  
	private boolean invalidAnalysis(AnalysisOutput output) {

		if(output.getScore()<=AnalysisOutput.SCORE_ANALYSIS) {
			String vsfx = output.getVsfx();
			if("하".equals(vsfx) || "되".equals(vsfx)) return true; // if doesn't exist in the dictionary
		}
		
		if (output.getJosa() == null) return false;
		
		if (!("로".equals(output.getJosa()) || "도".equals(output.getJosa())))
			return false;

		try {
			WordEntry entry = DictionaryUtil.getNoun(output.getStem());

			if (entry!=null && (entry.getFeature(WordEntry.IDX_BEV) == '1'
					|| entry.getFeature(WordEntry.IDX_DOV) == '1'))
				return true;

		} catch (MorphException e) {
			throw new RuntimeException(e);
		}

		return false;
	}
  
	private int addBiagramToMap(String input, int startoffset,
			Map<String, KoreanToken> map, int position) {
		int offset = 0;
		int strlen = input.length();
		if (strlen < 2)
			return position;

		while (offset < strlen - 1) {

			int inc = offset == 0 ? 0 : 1;

			if (isAlphaNumChar(input.charAt(offset))) {
				String text = findAlphaNumeric(input.substring(offset));
				map.put(position + ":" + text, new KoreanToken(text,
						startoffset + offset, inc));
				offset += text.length();
			} else {
				String text = input.substring(offset,
						offset + 2 > strlen ? strlen : offset + 2);
				map.put(position + ":" + text, new KoreanToken(text,
						startoffset + offset, inc));
				offset++;
			}

			position += 1;
		}

		return position - 1;
	}
  
  /**
   * return the start offset of current decompounds entry.
   * @param output  morphlogical analysis output
   * @param index     the index of current decompounds entry
   * @return        the start offset of current decoumpounds entry
   */
  private int getStartOffset(AnalysisOutput output, int index) {    
    int sOffset = 0;
    for(int i=0; i<index;i++) {
      sOffset += output.getCNounList().get(i).getWord().length();
    }
    return sOffset;
  }
  
  private String findAlphaNumeric(String text) {
    int pos = 0;
    for(int i=0;i<text.length();i++) {
      if(!isAlphaNumChar(text.charAt(i))) break;
      pos++;
    }    
    if(pos<text.length()) pos += 1;
    
    return text.substring(0,pos);
  }
  
  private boolean isAlphaNumChar(int c) {
    if((c>=48&&c<=57)||(c>=65&&c<=122)) return true;    
    return false;
  }
  
  @Override
  public void reset() throws IOException {
    super.reset();
    morphQueue.clear();
    currentState = null;
  }

}
