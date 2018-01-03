package edu.ait.nlp;

import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

        public static void main(String[] args) throws IOException {

            String serializedClassifier = "C:\\Users\\root\\projects\\sql-qa-bot\\sql-nlp\\src\\main\\resources\\nlp\\ner-model.ser.gz";

            if (args.length > 0) {
                serializedClassifier = args[0];
            }

            AbstractSequenceClassifier classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);

      /* For either a file to annotate or for the hardcoded text example,
         this demo file shows two ways to process the output, for teaching
         purposes.  For the file, it shows both how to run NER on a String
         and how to run it on a whole file.  For the hard-coded String,
         it shows how to run it on a single sentence, and how to do this
         and produce an inline XML output format.
      */
                String s1 = "How to select record?";
                String s2 = "How to insert record into table.";
                System.out.println(Arrays.toString(classifier.classifyToString(s2).split(" ")));
                System.out.println(classifier.classifyWithInlineXML(s2));
                System.out.println(classifier.classifyToString(s2, "xml", true));
                String res = classifier.classifyWithInlineXML(s2);

                s2 = s2.replaceAll("[.!?;:]", "");
            String classifiedStrings [] = classifier.classifyToString(s2).split(" ");
            for(String s : classifiedStrings){
                if(!s.endsWith("/O")){
                    s = s.substring(0, s.indexOf("/"));
                    System.out.println(s);
                }
            }

        }


}
