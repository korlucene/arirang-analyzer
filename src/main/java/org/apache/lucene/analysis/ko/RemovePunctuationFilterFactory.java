package org.apache.lucene.analysis.ko;

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class RemovePunctuationFilterFactory extends TokenFilterFactory {

	protected RemovePunctuationFilterFactory(Map<String, String> args) {
		super(args);
	}

	@Override
	public TokenStream create(TokenStream input) {
		return new RemovePunctuationFilter(input);
	}

}
