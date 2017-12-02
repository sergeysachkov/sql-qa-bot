package edu.ait.nlp.search;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.List;

public interface SqlInfoSearcher {
    List<String> getDocuments(String query) throws IOException, ParseException;
}
