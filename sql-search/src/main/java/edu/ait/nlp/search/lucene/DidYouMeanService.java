package edu.ait.nlp.search.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DidYouMeanService {

    private Properties props;
    private SpellChecker spellChecker;
    private String location;

    public DidYouMeanService() throws IOException {
        props = new Properties();
        //props.load(DidYouMeanService.class.getClassLoader().getResourceAsStream("sql-bot.properties"));
        props.load(new FileReader("C:\\Users\\root\\projects\\sql-qa-bot\\sql-asr\\src\\main\\resources\\sql-bot.properties"));

        location  = props.getProperty("lucene.vocabulary.location") + "/dictionary.txt";
        File vocabularyDir = new File(props.getProperty("lucene.vocabulary.location"));
        Directory directory = FSDirectory.open(vocabularyDir.toPath());
        spellChecker = new SpellChecker(directory);
    }


    public Set<String> didYouMean(String search) throws IOException {
        Set<String> suggestionList = new HashSet<>();
        spellChecker.indexDictionary(new PlainTextDictionary(new File(location).toPath()), new IndexWriterConfig(new StandardAnalyzer()), true);
        final int suggestionNumber = Integer.parseInt(props.getProperty("number.suggestion", "5"));
        for(String value : search.split(" ")) {
            String[] suggestions = spellChecker.suggestSimilar(value, suggestionNumber);
            suggestionList.addAll(Arrays.asList(suggestions));
        }
        boolean didYouMeanDefaultOn = Boolean.parseBoolean(props.getProperty("did.you.mean.default.on", "false"));
        if(suggestionList.isEmpty() && didYouMeanDefaultOn){
            suggestionList = getDefaultSuggestion();
        }
        return suggestionList;
    }

    public Set<String> getDefaultSuggestion(){
        Set<String> suggestionList = new HashSet<>();
        //todo here we need to take predefined list of suggestions
        suggestionList.add("select record");
        suggestionList.add("update record");
        suggestionList.add("delete record");
        return suggestionList;
    }
    public static void main(String []  args) throws IOException {
        DidYouMeanService service = new DidYouMeanService();
        System.out.println(service.didYouMean("coddect sedect foll"));
    }

}
