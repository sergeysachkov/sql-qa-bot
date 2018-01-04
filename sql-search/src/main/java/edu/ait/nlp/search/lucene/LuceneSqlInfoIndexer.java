package edu.ait.nlp.search.lucene;

import edu.ait.nlp.search.SqlInfoIndexer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class LuceneSqlInfoIndexer implements SqlInfoIndexer{

    private IndexWriter writer;

    public LuceneSqlInfoIndexer(String indexDirectoryPath) throws IOException {
        FSDirectory dir = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        this.writer = new IndexWriter(dir, config);
    }

    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void addDocument(File file) throws IOException {
        Document document = new Document();
        document.add(new StringField("id", file.getName() , Field.Store.YES));
        document.add(new TextField("content", getFileContent(file) , Field.Store.YES));
        writer.addDocument(document);
    }

    @Override
    public void addDocuments(File[] files) throws IOException {
        for(File f : files){
            if(f.getName().endsWith(".txt")){
                addDocument(f);
            }
        }
    }

    private String getFileContent(File file) throws IOException {
        return java.nio.file.Files.lines(Paths.get(file.getAbsolutePath())).collect(
                Collectors.joining());
    }
}
