package edu.ait.nlp.search;

import java.io.File;

public interface SqlInfoIndexer {
    void addDocument(File file);
    void addDocuments(File[] files);
}
