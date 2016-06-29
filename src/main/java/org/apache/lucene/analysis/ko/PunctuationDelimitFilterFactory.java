package org.apache.lucene.analysis.ko;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.util.Map;

/**
 * Created by SooMyung(soomyung.lee@gmail.com) on 2014. 7. 30.
 */

public class PunctuationDelimitFilterFactory extends TokenFilterFactory {
	
	public static final String SplitedTerm_PARAM = "hasSplitedTerm";

	public static final String Punctation_PARAM = "hasPunctation";
	
	public static final String ConcatedTerm_PARAM = "hasConcatedTerm";
	
	public static final String MIN_TERM_SIZE_PARAM = "minTermSize";
	
	private boolean hasSplitedTerm = true;
	
	private boolean hasPunctation = true;
	
	private boolean hasConcatedTerm = true;
	
	private int minTermSize = 1;
	
    /**
     * Initialize this factory via a set of key-value pairs.
     *
     * @param args
     */
    public PunctuationDelimitFilterFactory(Map<String, String> args) {
        super(args);
        
        hasSplitedTerm = getBoolean(args, SplitedTerm_PARAM, true);
        hasPunctation = getBoolean(args, Punctation_PARAM, true);
        hasConcatedTerm = getBoolean(args, ConcatedTerm_PARAM, true);
        minTermSize = getInt(args, MIN_TERM_SIZE_PARAM, 1);
        
        if (!args.isEmpty()) {
          throw new IllegalArgumentException("Unknown parameters: " + args);
        }
        
    }

    @Override
    public TokenStream create(TokenStream input) {
    	PunctuationDelimitFilter filter = new PunctuationDelimitFilter(input);
    	filter.setHasConcatedTerm(hasConcatedTerm);
    	filter.setHasSplitedTerm(hasSplitedTerm);
    	filter.setHasPunctation(hasPunctation);
        return filter;
    }
}
