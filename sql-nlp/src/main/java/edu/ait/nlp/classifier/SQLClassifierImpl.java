package edu.ait.nlp.classifier;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SQLClassifierImpl implements SQLClassifier{

    private Properties props;

    public SQLClassifierImpl() throws IOException {
        this.props = new Properties();
        props.load(SQLClassifierImpl.class.getClassLoader().getResourceAsStream("nlp.properties"));
    }

    public List<String> getNERFromText(String text, String model){
        if(text == null || text.isEmpty()){
            throw new IllegalArgumentException("Input Text cannot be empty");
        }
        List<String> result = new ArrayList<>();
        if(model == null || model.isEmpty()){
            model = props.getProperty("nlp.model");
        }
        text = text.replaceAll("[.!?;:]", "");
        AbstractSequenceClassifier classifier = CRFClassifier.getClassifierNoExceptions(model);
        String [] classifiedStrings = classifier.classifyToString(text).split(" ");
        for(String s : classifiedStrings){
            if(!s.endsWith("/O")){
                s = s.substring(0, s.indexOf("/"));
                result.add(s);
            }
        }
        return result;
    }
}
