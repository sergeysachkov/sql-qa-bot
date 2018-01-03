package edu.ait.nlp.search.lucene;

import edu.ait.nlp.response.SQLResponse;
import edu.ait.nlp.search.SqlInfoSearcher;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LuceneSqlInfoSearcher implements SqlInfoSearcher{

    IndexSearcher searcher;

    public LuceneSqlInfoSearcher(String indexDirectoryPath) throws IOException {
        Directory dir = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexReader reader = DirectoryReader.open(dir);
        this.searcher = new IndexSearcher(reader);
    }

    @Override
    public List<SQLResponse> getDocuments(String query) throws IOException, ParseException {
        List<SQLResponse> contents = new ArrayList<>();
        QueryParser qp = new QueryParser("content", new StandardAnalyzer());
        Query idQuery = qp.parse(query);
        TopDocs hits = searcher.search(idQuery, 10);
        for (ScoreDoc sd : hits.scoreDocs)
        {
            SQLResponse response = new SQLResponse();
            Document d = searcher.doc(sd.doc);
            response.setScore(sd.score);
            response.setText(String.format(d.get("content")));
            contents.add(response);
        }
        return contents;
    }
}
