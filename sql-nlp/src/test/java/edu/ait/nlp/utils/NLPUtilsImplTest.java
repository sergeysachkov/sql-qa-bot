package edu.ait.nlp.utils;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class NLPUtilsImplTest {

    @Test
    public void tokenizeModelTest() throws IOException {
        String source = "target/resources/nlp/model.txt";
        NLPUtils nlpUtils = new NLPUtilsImpl();
        nlpUtils.tokenizeModel(source);
        String testSource = "target/resources/nlp/model_orig.tok";
        Properties props = new Properties();
        props.load(NLPUtilsImpl.class.getClassLoader().getResourceAsStream("nlp.properties"));
        File file1 = new File(props.getProperty("nlp.train.model.file"));
        assertTrue(file1.exists());
        File file2 = new File(testSource);
        assertTrue(file2.exists());
        assertTrue(FileUtils.contentEquals(file1, file2));
        assertTrue(file1.delete());
    }

    @Test
    public void trainModelTest() throws Exception {
        Properties props = new Properties();
        props.load(NLPUtilsImpl.class.getClassLoader().getResourceAsStream("nlp.properties"));
        String source = props.getProperty("nlp.train.property.file");
        NLPUtils nlpUtils = new NLPUtilsImpl();
        nlpUtils.trainModel(source);
        File file1 = new File(props.getProperty("nlp.model"));
        assertTrue(file1.exists());
        assertTrue(file1.delete());
    }
}
