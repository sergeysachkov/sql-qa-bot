package edu.ait.nlp.classifier;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;

import java.util.ArrayList;
import java.util.List;

public class SQLClassifierImpl implements SQLClassifier{

    public List<String> getNERFromText(String text, String model){
        if(text == null || text.isEmpty()){
            throw new IllegalArgumentException("Input Text cannot be empty");
        }
        List<String> result = new ArrayList<>();
        if(model == null || model.isEmpty()){
            //todo init from props
            model = "C:\\Users\\root\\projects\\sql-qa-bot\\sql-nlp\\src\\main\\resources\\nlp\\ner-model.ser.gz";
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
