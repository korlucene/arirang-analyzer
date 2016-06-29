package org.apache.lucene.analysis.ko;

public class TokenUtilities {

    /**
     * if more than a character exist, the token is considered as a korean token
     * @return
     */
    public static String getType(char[] buffer, int leng) {
        boolean isSimbol=true;
        for(int i=0;i<leng;i++) {
            if(buffer[i]=='\u0000') {
            	isSimbol=false;
            	break;
            }
            if(buffer[i]>='\uAC00' && buffer[i]<='\uD7A3') 
            	return KoreanTokenizer.TYPE_KOREAN;
            if(Character.isLetterOrDigit(buffer[i])) 
            	isSimbol=false;
        }
        if(isSimbol) return KoreanTokenizer.TYPE_SIMBOL;
        
        return KoreanTokenizer.TYPE_WORD;
    }
    
}
