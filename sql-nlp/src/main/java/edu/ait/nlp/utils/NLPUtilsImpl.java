package edu.ait.nlp.utils;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class NLPUtilsImpl implements NLPUtils{

    private Properties props;

    public NLPUtilsImpl() throws IOException {
        this.props = new Properties();
        props.load(NLPUtilsImpl.class.getClassLoader().getResourceAsStream("nlp.properties"));
    }

    public void trainModel(String propFile) throws Exception {
        if(propFile == null || propFile.isEmpty()){
            propFile = props.getProperty("nlp.train.property.file");
        }
        String [] args = new String [2];
        args[0] = "-prop";
        args[1] = propFile;
        CRFClassifier.main(args);
    }

    public void tokenizeModel(String source) throws IOException {
        String dest  = props.getProperty("nlp.train.model.file");
        PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new FileReader(source),
                new CoreLabelTokenFactory(), "");
        FileWriter f = new FileWriter(dest);
        while (ptbt.hasNext()) {
            CoreLabel label = ptbt.next();
            f.write(label.toString() + "\n");
        }
        f.close();

    }

}
