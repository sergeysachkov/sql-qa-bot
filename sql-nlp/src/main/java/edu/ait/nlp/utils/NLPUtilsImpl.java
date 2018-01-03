package edu.ait.nlp.utils;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class NLPUtilsImpl implements NLPUtils{

    public void trainModel(String propFile) throws Exception {
        if(propFile == null || propFile.isEmpty()){
            //todo init from props
            propFile = "C:\\Users\\root\\projects\\sql-qa-bot\\sql-nlp\\src\\main\\resources\\nlp\\train.prop";
        }
        String [] args = new String [2];
        args[0] = "-prop";
        args[1] = propFile;
        CRFClassifier.main(args);
    }

    public void tokenizeModel(String source) throws IOException {
        //todo init from props
        String dest  = "nlp/model1.tok";
        PTBTokenizer<CoreLabel> ptbt = new PTBTokenizer<>(new FileReader(source),
                new CoreLabelTokenFactory(), "");
        FileWriter f = new FileWriter(dest);
        while (ptbt.hasNext()) {
            CoreLabel label = ptbt.next();
            //System.out.println(label);
            f.write(label.toString() + "\n");
        }
        f.close();

    }

    public static void main(String [] args) throws Exception {
        //NLPUtils trainer = new NLPUtilsImpl();
        //trainer.trainModel(null);
        //trainer.tokenizeModel("C:\\Users\\root\\projects\\sql-qa-bot\\sql-nlp\\src\\main\\resources\\nlp\\model.txt");
    }
}
