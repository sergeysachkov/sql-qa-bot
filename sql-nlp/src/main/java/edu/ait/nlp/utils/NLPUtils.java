package edu.ait.nlp.utils;

import java.io.IOException;

public interface NLPUtils {
    void tokenizeModel(String source) throws IOException;
    void trainModel(String propFile) throws Exception;
}
