package edu.ait.nlp.servlet;

import edu.ait.nlp.search.SqlInfoIndexer;
import edu.ait.nlp.search.lucene.LuceneSqlInfoIndexer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class InitServlet extends HttpServlet {

       private static final long serialVersionUID = 1L;

        public void init(ServletConfig config) throws ServletException {
            String luceneLocation = config.getInitParameter("lucene-properties-location");
            ServletContext sc = config.getServletContext();
            String webAppPath = sc.getRealPath("/");
            String luceneProp = webAppPath + luceneLocation;
            File lucene = new File(luceneProp);
            FileWriter fileWriter = null;
            if (lucene.exists()) {

                SqlInfoIndexer sqlInfoIndexer;
                try {
                    Properties props = new Properties();
                    props.load(new FileReader(lucene));
                    File indexLocation = new File(props.getProperty("lucene.index.location"));
                    if(!indexLocation.exists()){
                        throw new RuntimeException("failed to create suggestion list, directory doesn't exists!");
                    }
                    if(!indexLocation.isDirectory()){
                        throw new RuntimeException("failed to create suggestion list, vocabulary is not a directory!");
                    }

                    //todo check, if this part needed on add document
                    File vocabularyLocation = new File(props.getProperty("lucene.vocabulary.location"));
                    sqlInfoIndexer = new LuceneSqlInfoIndexer(indexLocation.getAbsolutePath());
                    sqlInfoIndexer.addDocuments(indexLocation.listFiles());
                    sqlInfoIndexer.close();
                    vocabularyLocation.mkdir();
                    fileWriter = new FileWriter(vocabularyLocation + "/dictionary.txt");
                    Set<String> vocabularySet = new HashSet<>();
                    for(File f : indexLocation.listFiles()){
                        String fileName = f.getName();
                        if(fileName.endsWith(".txt")){
                            fileName = fileName.replace(".txt", "").replace("-", " ");
                            vocabularySet.addAll(Arrays.asList(fileName.split(" ")));
                        }
                    }
                    for(String voc : vocabularySet){
                        fileWriter.write(voc);
                        fileWriter.write("\n");
                    }

                } catch (IOException e) {
                    throw new RuntimeException("Lucene failed to initialize!");
                }finally {
                    if(fileWriter != null){
                        try {
                            fileWriter.close();
                        } catch (IOException e) {
                            throw new RuntimeException("failed to close FileWriter!");
                        }
                    }
                }
            } else {
                throw new RuntimeException("Property file not found!");
            }

            super.init(config);
        }


}
