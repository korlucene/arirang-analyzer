package org.apache.lucene.analysis.ko;

import org.apache.lucene.util.Attribute;

/**
 * Created by SooMyung(soomyung.lee@gmail.com) on 2014. 7. 29.
 */
public interface MorphemeAttribute extends Attribute {
    public void setToken(KoreanToken token);
    public KoreanToken getToken();
}
