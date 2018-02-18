package edu.ait.nlp.search.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class DidYouMeanService {

    private Properties props;
    private SpellChecker spellChecker;
    private String location;
    private Set<String> suggestionSet;
    public DidYouMeanService() throws IOException {
        props = new Properties();
        props.load(DidYouMeanService.class.getClassLoader().getResourceAsStream("sql-bot.properties"));
        //props.load(new FileReader("C:\\Users\\root\\projects\\sql-qa-bot\\sql-asr\\src\\main\\resources\\sql-bot.properties"));

        location  = props.getProperty("lucene.vocabulary.location") + "/dictionary.txt";
        File vocabularyDir = new File(props.getProperty("lucene.vocabulary.location"));
        Directory directory = FSDirectory.open(vocabularyDir.toPath());
        spellChecker = new SpellChecker(directory);
        suggestionSet = new HashSet<>();
        Scanner scanner = new Scanner(Paths.get(props.getProperty("lucene.vocabulary.location") + "/didyoumeandefault.txt"));
        while (scanner.hasNext()){
            suggestionSet.add(scanner.nextLine());
        }
    }


    public Set<String> didYouMean(String search) throws IOException {
        Set<String> suggestionSet = new HashSet<>();
        spellChecker.indexDictionary(new PlainTextDictionary(new File(location).toPath()), new IndexWriterConfig(new StandardAnalyzer()), true);
        final int suggestionNumber = Integer.parseInt(props.getProperty("number.suggestion", "5"));
        for(String value : search.split(" ")) {
            String[] suggestions = spellChecker.suggestSimilar(value, suggestionNumber);
            for(String suggestion : suggestions){
                String sug = getFromSuggestionSet(suggestion);
                if(sug != null){
                    suggestionSet.add(sug);
                }
            }
        }
        boolean didYouMeanDefaultOn = Boolean.parseBoolean(props.getProperty("did.you.mean.default.on", "false"));
        if(suggestionSet.isEmpty() && didYouMeanDefaultOn){
            suggestionSet = getDefaultSuggestion();
        }
        return suggestionSet;
    }

    private String getFromSuggestionSet(String suggestion) {
        for(String sug : suggestionSet){
            if(sug.contains(suggestion)){
                return sug;
            }
        }
        return null;
    }

    public Set<String> getDefaultSuggestion(){
        return suggestionSet;
    }
    public static void main(String []  args) throws IOException {
        DidYouMeanService service = new DidYouMeanService();
        System.out.println(service.didYouMean("coddect sedect foll"));
    }

}
