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
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.ClassicFilter;

/**
 * A Korean Analyzer
 */
public class KoreanAnalyzer extends StopwordAnalyzerBase {
  
  /** An unmodifiable set containing some common English words that are not usually useful
  for searching.*/
  public static final CharArraySet ENGLISH_STOP_WORDS_SET;
  
  static {
    final List<String> stopWords = Arrays.asList(
      "a", "an", "and", "are", "as", "at", "be", "but", "by",
      "for", "if", "in", "into", "is", "it",
      "no", "not", "of", "on", "or", "such",
      "that", "the", "their", "then", "there", "these",
      "they", "this", "to", "was", "will", "with"
    );
    final CharArraySet stopSet = new CharArraySet(stopWords, false);
    ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet); 
  }

  private boolean bigrammable = false;
    
  private boolean hasOrigin = false;
    
  private boolean exactMatch = false;
  private boolean originCNoun = true;
  private boolean queryMode = false;
  private boolean wordSegment = false;
  private boolean decompound = true;
  
  public KoreanAnalyzer() {
	  this(ENGLISH_STOP_WORDS_SET);
  }
  
  /** Builds an analyzer with the stop words from the given set.
   * @param stopWords Set of stop words */
  public KoreanAnalyzer(CharArraySet stopWords) {
    super(stopWords);
  }
  
  @Override
  protected TokenStreamComponents createComponents(final String fieldName) {
    final KoreanTokenizer src = new KoreanTokenizer();
    TokenStream tok = new LowerCaseFilter(src);
    tok = new ClassicFilter(tok);
    tok = new KoreanFilter(tok, bigrammable, hasOrigin, exactMatch, originCNoun, queryMode, decompound);
    if(wordSegment) tok = new WordSegmentFilter(tok, hasOrigin);
    tok = new HanjaMappingFilter(tok);
    tok = new PunctuationDelimitFilter(tok);
    tok = new StopFilter(tok, stopwords);
    
    return new TokenStreamComponents(src, tok) {
      @Override
      protected void setReader(final Reader reader)  {
        super.setReader(reader);
      }
    };
	    
  }
    
  /**
   * determine whether the bigram index term is returned or not if a input word is failed to analysis
   * If true is set, the bigram index term is returned. If false is set, the bigram index term is not returned.
   */
  public void setBigrammable(boolean is) {
    bigrammable = is;
  }
  
  /**
   * determin whether the original term is returned or not if a input word is analyzed morphically.
   */
  public void setHasOrigin(boolean has) {
    hasOrigin = has;
  }

  /**
   * determin whether the original compound noun is returned or not if a input word is analyzed morphically.
   */
  public void setOriginCNoun(boolean cnoun) {
    originCNoun = cnoun;
  }
  
  /**
   * determin whether the original compound noun is returned or not if a input word is analyzed morphically.
   */
  public void setExactMatch(boolean exact) {
    exactMatch = exact;
  }
  
  /**
   * determin whether the analyzer is running for a query processing
   */
  public void setQueryMode(boolean mode) {
    queryMode = mode;
  }

  public void setDecompound(boolean is) {
	  this.decompound = is;
  }
  
  /**
   * determin whether word segment analyzer is processing
   */
	public boolean isWordSegment() {
		return wordSegment;
	}
	
	public void setWordSegment(boolean wordSegment) {
		this.wordSegment = wordSegment;
	}
  
}
