package edu.ait.nlp.search;

import java.io.File;
import java.io.IOException;

public interface SqlInfoIndexer {
    void addDocument(File file) throws IOException;
    void addDocuments(File[] files) throws IOException;
}
