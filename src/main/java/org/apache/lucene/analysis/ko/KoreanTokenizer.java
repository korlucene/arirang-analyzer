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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.CharacterUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ko.morph.MorphException;
import org.apache.lucene.analysis.ko.utils.DictionaryUtil;
import org.apache.lucene.analysis.ko.utils.SyllableUtil;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeFactory;

public final class KoreanTokenizer extends Tokenizer {

    private int offset = 0, bufferIndex = 0, dataLen = 0, finalOffset = 0;
    private static final int MAX_WORD_LEN = 255;
    private static final int IO_BUFFER_SIZE = 4096;

    //MGK_DEL [
    //private final CharacterUtils charUtils;
    //MGK_DEL ]
    private final CharacterUtils.CharacterBuffer ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);

    private static Map<Integer,Integer> pairmap = new HashMap<Integer,Integer>();

    static {
        pairmap.put(34,34);// ""
        pairmap.put(39,39);// ''
        pairmap.put(40,41);// ()
        pairmap.put(60,62);// <>
        pairmap.put(91,93);// []
        pairmap.put(123,125);// {}
        pairmap.put(65288,65289);// ‘’
        pairmap.put(8216,8217);// ‘’
        pairmap.put(8220,8221);// “”
    }

    private List<Integer> pairstack = new ArrayList<Integer>();

    public static final String TYPE_KOREAN = "korean";
    public static final String TYPE_WORD = "word";
    public static final String TYPE_SIMBOL = "symbol";

    // this tokenizer generates three attributes:
    // term offset, positionIncrement and type
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    public KoreanTokenizer() {
    	
    	//MGK_DEL [
        //charUtils = CharacterUtils.getInstance();
        //MGK_DEL ]
    }

    public KoreanTokenizer(AttributeFactory factory) {
        super(factory);
        //MGK_CHG [
        //charUtils = CharacterUtils.getInstance();
        //MGK_CHG ]
    }

    @Override
    public final boolean incrementToken() throws IOException {

        clearAttributes();
        char[] buffer = termAtt.buffer();

        int length = 0;
        int start = -1; // this variable is always initialized
        int end = -1;
        int pos = posIncrAtt.getPositionIncrement();
        
        while (true) {
            if (bufferIndex >= dataLen) {
                offset += dataLen;
                //MGK_CHG [
                //charUtils.fill(ioBuffer, input); // read supplementary char aware with CharacterUtils
                CharacterUtils.fill(ioBuffer, input); // read supplementary char aware with CharacterUtils
                //MGK_CHG ]
                
                if (ioBuffer.getLength() == 0) {
                    dataLen = 0; // so next offset += dataLen won't decrement offset
                    if (length > 0) {
                        break;
                    } else {
                        finalOffset = correctOffset(offset);
                        return false;
                    }
                }
                dataLen = ioBuffer.getLength();
                bufferIndex = 0;
            }

            // use CharacterUtils here to support < 3.1 UTF-16 code unit behavior if the char based methods are gone
            //MGK_CHG [
            //final int c = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex, ioBuffer.getLength());
            final int c = Character.codePointAt(ioBuffer.getBuffer(), bufferIndex, ioBuffer.getLength());
            //MGK_CHG ]
            final int charCount = Character.charCount(c);
            bufferIndex += charCount;

            char inspect_c = (char)c;
            
            int closechar = getPairChar(c);
           
            if(closechar!=0 && 
            		(pairstack.isEmpty() || 
            				(!pairstack.isEmpty() && pairstack.get(0)!=c))) {
            	if(start==-1) {
            		start=offset + bufferIndex - charCount;
            		end=start;
            	}
            	end += charCount;
                length += Character.toChars(c, buffer, length); // buffer it
                pairstack.add(0,closechar);
                
                break; 
            } else if (isTokenChar(c) || 
            		(pairstack.size()>0 && pairstack.get(0)==c)) {               // if it's a token char
                if (length == 0) {                // start of token
                    assert start == -1;
                    start = offset + bufferIndex - charCount;
                    end = start;
                } else if (length >= buffer.length - 1) { // check if a supplementary could run out of bounds
                    buffer = termAtt.resizeBuffer(2 + length); // make sure a supplementary fits in the buffer
                }
                end += charCount;
                length += Character.toChars(c, buffer, length); // buffer it
                
                // delimited close character

                
//                // check if next token is parenthesis.
                if(isDelimitPosition(length, c)) {
                    if(!pairstack.isEmpty() && pairstack.get(0)==c) {
                    	pairstack.remove(0);
                    }
                	break;
                }
                
                if(!pairstack.isEmpty() && pairstack.get(0)==c) {
                	pairstack.remove(0);
                }
                
                if (length >= MAX_WORD_LEN)
                    break; // buffer overflow! make sure to check for >= surrogate pair could break == test
            } else if (length > 0) {           // at non-Letter w/ chars
                break;
            }// return 'em
            
        }

        String type = TokenUtilities.getType(buffer, length);

        termAtt.setLength(length);
        assert start != -1;
        offsetAtt.setOffset(correctOffset(start), finalOffset = correctOffset(end));
        typeAtt.setType(type);
        return true;
    }

	/**
	 * @return
	 */
	private boolean isDelimitPosition(int length, int c) {
		if(bufferIndex>=dataLen ||
				(length==1 && !pairstack.isEmpty() && pairstack.get(0)==c)) return true;
		//MGK_CHG [
		//int next_c = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex, ioBuffer.getLength());
		int next_c = Character.codePointAt(ioBuffer.getBuffer(), bufferIndex, ioBuffer.getLength());
		//MGK_CHG ]
		if(isTokenChar(next_c)) return false;
		
		if(pairstack.size()==0) return true;
		
		int next_closechar = getPairChar(next_c);
		if(next_closechar!=0 && pairstack.get(0)!=next_closechar) 
			return true;
		
		int size = pairstack.size();
		if((ioBuffer.getLength()-bufferIndex)<size) size = ioBuffer.getLength()-bufferIndex;
		
		if(next_c!=pairstack.get(0)) return false; // if next character is not close parenthesis
		
		for(int i=1;i<size;i++) {
			//MGK_CHG [
			//next_c = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex+i, ioBuffer.getLength());
			next_c = Character.codePointAt(ioBuffer.getBuffer(), bufferIndex+i, ioBuffer.getLength());
			//MGK_CHG ]
			if(next_c!=pairstack.get(i)) return true;
		}

		
		try {

			
			int start = bufferIndex+size;
			int end = Math.min(ioBuffer.getLength(), start + 2);
			
			boolean hasParticle = false;
			for(int i=start;i<end;i++) {
				//MGK_CHG [
				//int space_c = charUtils.codePointAt(ioBuffer.getBuffer(), i, ioBuffer.getLength());
				int space_c = Character.codePointAt(ioBuffer.getBuffer(), i, ioBuffer.getLength());
				//MGK_CHG ]
				
				if(space_c==32) { // 32 is space ascii code
					if(i==start)
						return true;
					else
						return false;
				}
				
				char[] feature =  SyllableUtil.getFeature((char)space_c);
				
				if(i==start && !(feature[SyllableUtil.IDX_JOSA1]=='1' || feature[SyllableUtil.IDX_EOMI1]=='1')) {
					return true;
				} else if(i==start+1 && !(feature[SyllableUtil.IDX_JOSA2]=='1' || feature[SyllableUtil.IDX_EOMI2]=='1')) {
					return true;
				} 
				
				hasParticle = true;
			}

			return !hasParticle;
			
		} catch (MorphException e) {
			throw new RuntimeException("Error occured while reading a josa");
		}

	}
    
    private boolean isTokenChar(int c) {
        if(Character.isLetterOrDigit(c) || isPreserveSymbol((char)c)) return true;
        return false;
    }

    private int getPairChar(int c) {
        Integer p = pairmap.get(c);
        return p==null ? 0 : p;
    }


    private boolean isPreserveSymbol(char c) {
        return (c=='#' || c=='+' || c=='-' || c=='/' || c=='·' || c == '&' || c == '_');
    }

    @Override
    public final void end() throws IOException {
        super.end();
        // set final offset
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        bufferIndex = 0;
        offset = 0;
        dataLen = 0;
        finalOffset = 0;
        ioBuffer.reset(); // make sure to reset the IO buffer!!
    }

}
