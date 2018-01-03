package edu.ait.nlp.classifier;

import java.util.List;

public interface SQLClassifier {
    List<String> getNERFromText(String text, String model);
}
