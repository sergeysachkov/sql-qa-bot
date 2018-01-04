package edu.ait.nlp.servlet;

import edu.ait.nlp.search.SqlInfoIndexer;
import edu.ait.nlp.search.lucene.LuceneSqlInfoIndexer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class InitServlet extends HttpServlet {

       private static final long serialVersionUID = 1L;

        public void init(ServletConfig config) throws ServletException {
            String luceneLocation = config.getInitParameter("lucene-properties-location");
            ServletContext sc = config.getServletContext();
            String webAppPath = sc.getRealPath("/");
            String luceneProp = webAppPath + luceneLocation;
            File lucene = new File(luceneProp);
            if (lucene.exists()) {

                SqlInfoIndexer sqlInfoIndexer;
                try {
                    Properties props = new Properties();
                    props.load(new FileReader(lucene));
                    File indexLocation = new File(props.getProperty("lucene.index.location"));
                    sqlInfoIndexer = new LuceneSqlInfoIndexer(indexLocation.getAbsolutePath());
                    sqlInfoIndexer.addDocuments(indexLocation.listFiles());
                    sqlInfoIndexer.close();
                } catch (IOException e) {
                    throw new RuntimeException("Lucene failed to initialize!");
                }
            } else {
                throw new RuntimeException("Property file not found!");
            }

            super.init(config);
        }


}
