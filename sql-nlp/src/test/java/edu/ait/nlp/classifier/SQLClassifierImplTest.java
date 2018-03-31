package edu.ait.nlp.classifier;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SQLClassifierImplTest {

    @Test
    public void getNERFromTextDefaultModelTest() throws IOException {
        SQLClassifier classifier = new SQLClassifierImpl();
        List<String> control = new ArrayList<>();
        control.add("CREATE");
        control.add("TABLE");
        List<String> results = classifier.getNERFromText("CREATE TABLE", null);
        assertEquals(results, control);
    }

    @Test
    public void getNERFromTextWithModelTest() throws IOException {
        SQLClassifier classifier = new SQLClassifierImpl();
        List<String> control = new ArrayList<>();
        control.add("CREATE");
        control.add("TABLE");
        List<String> results = classifier.getNERFromText("CREATE TABLE", "target/resources/nlp/ner-model.ser.gz");
        assertEquals(results, control);
    }
}
