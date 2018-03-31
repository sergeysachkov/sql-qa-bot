package edu.ait.nlp.classifier;

import edu.ait.nlp.utils.NLPUtils;
import edu.ait.nlp.utils.NLPUtilsImpl;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SQLClassifierImplTest {

    @Test
    public void getNERFromTextDefaultModelTest(){
        try {
            Properties props = new Properties();
            props.load(NLPUtilsImpl.class.getClassLoader().getResourceAsStream("nlp.properties"));
            String source = props.getProperty("nlp.train.property.file");
            NLPUtils nlpUtils = new NLPUtilsImpl();
            nlpUtils.trainModel(source);
            SQLClassifier classifier = new SQLClassifierImpl();
            List<String> control = new ArrayList<>();
            control.add("CREATE");
            control.add("TABLE");
            List<String> results = classifier.getNERFromText("CREATE TABLE", null);
            assertEquals(results, control);
            File file1 = new File(props.getProperty("nlp.model"));
            assertTrue(file1.exists());
            assertTrue(file1.delete());
        }catch (Exception e) {
            fail();
        }
    }

    @Test
    public void getNERFromTextWithModelTest() throws IOException {
        try {
            SQLClassifier classifier = new SQLClassifierImpl();
            List<String> control = new ArrayList<>();
            control.add("CREATE");
            control.add("TABLE");
            List<String> results = classifier.getNERFromText("CREATE TABLE", "target/resources/nlp/ner-model.ser.gz");
            assertEquals(results, control);
        }catch (IOException e){
            fail();
        }
    }
}
