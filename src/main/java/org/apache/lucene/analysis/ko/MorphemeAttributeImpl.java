package org.apache.lucene.analysis.ko;

import org.apache.lucene.util.AttributeImpl;

/**
 * Created by SooMyung(soomyung.lee@gmail.com) on 2014. 7. 29.
 */

public class MorphemeAttributeImpl extends AttributeImpl implements MorphemeAttribute, Cloneable {

    private KoreanToken koreanToken;

    @Override
    public void clear() {
    	koreanToken = null;
    }

    @Override
    public void copyTo(AttributeImpl target) {
    	MorphemeAttribute t = (MorphemeAttribute) target;
        t.setToken(koreanToken);
    }

    public void setToken(KoreanToken token) {
        this.koreanToken = token;
    }

    public KoreanToken getToken() {
        return this.koreanToken;
    }

    
}
