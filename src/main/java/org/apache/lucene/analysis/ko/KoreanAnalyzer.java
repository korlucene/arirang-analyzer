package org.apache.lucene.analysis.ko;

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
  private boolean removePunctuation = false;
  private boolean hasVerb = false;
  
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
    tok = new KoreanFilter(tok, bigrammable, hasOrigin, exactMatch, originCNoun, queryMode, decompound, hasVerb);
    if(wordSegment) tok = new WordSegmentFilter(tok, hasOrigin);
    tok = new HanjaMappingFilter(tok);
    if(removePunctuation) tok = new RemovePunctuationFilter(tok);
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
  
	/**
	 * 
	 * @param wordSegment
	 */
	public void setRemovePunctuation(boolean remove) {
		this.removePunctuation = remove;
	}
	
	public void setHasVerb(boolean has) {
		this.hasVerb = has;
	}
}
