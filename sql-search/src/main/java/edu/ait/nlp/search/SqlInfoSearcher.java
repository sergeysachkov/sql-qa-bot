package edu.ait.nlp.search;

import java.util.List;

public interface SqlInfoSearcher {
    List<String> getDocuments(String query);
}
