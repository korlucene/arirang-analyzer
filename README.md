# arirang-analyzer-6.x


[add field type to schema.xml of collection/conf/]

        <fieldType name="text_ko" class="solr.TextField">
                <analyzer type="index">
                        <tokenizer class="org.apache.lucene.analysis.ko.KoreanTokenizerFactory"/>
                        <filter class="solr.LowerCaseFilterFactory"/>
                        <filter class="solr.ClassicFilterFactory"/>
                        <filter class="org.apache.lucene.analysis.ko.KoreanFilterFactory" hasOrigin="true" hasCNoun="true"  bigrammable="false" queryMode="false"/>
                        <filter class="solr.SynonymFilterFactory" synonyms="synonyms.txt" ignoreCase="true" expand="false" />
                        <filter class="org.apache.lucene.analysis.ko.WordSegmentFilterFactory" hasOrijin="true"/>
                        <!--filter class="org.apache.lucene.analysis.ko.HanjaMappingFilterFactory"/>
                        <filter class="org.apache.lucene.analysis.ko.PunctuationDelimitFilterFactory"/-->
                        <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt"/>
                </analyzer>
                <analyzer type="query">
                        <tokenizer class="org.apache.lucene.analysis.ko.KoreanTokenizerFactory"/>
                        <filter class="solr.LowerCaseFilterFactory"/>
                        <filter class="solr.ClassicFilterFactory"/>
                        <filter class="org.apache.lucene.analysis.ko.KoreanFilterFactory" hasOrigin="true" hasCNoun="true" bigrammable="false" queryMode="false"/>
                        <filter class="solr.SynonymFilterFactory" synonyms="synonyms.txt" ignoreCase="true" expand="false" />
                        <filter class="org.apache.lucene.analysis.ko.WordSegmentFilterFactory" hasOrijin="true"/>
                        <filter class="org.apache.lucene.analysis.ko.HanjaMappingFilterFactory"/>
                        <filter class="org.apache.lucene.analysis.ko.PunctuationDelimitFilterFactory"/>
                        <filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt"/>
                </analyzer>
        </fieldType>
