package edu.ait.nlp.search;

import edu.ait.nlp.response.SQLResponse;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.List;

public interface SqlInfoSearcher {
    List<SQLResponse> getDocuments(String query) throws IOException, ParseException;
}
