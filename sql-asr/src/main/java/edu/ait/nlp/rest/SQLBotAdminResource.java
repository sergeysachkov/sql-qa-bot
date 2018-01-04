package edu.ait.nlp.rest;

import edu.ait.nlp.search.SqlInfoIndexer;
import edu.ait.nlp.search.lucene.LuceneSqlInfoIndexer;
import edu.ait.nlp.utils.NLPUtils;
import edu.ait.nlp.utils.NLPUtilsImpl;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

@Path("/admin")
public class SQLBotAdminResource {

    private NLPUtils nlpUtils;

    public SQLBotAdminResource() throws IOException {
        nlpUtils = new NLPUtilsImpl();


    }

    @POST
    @Path("/train")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response trainModel(InputStream fileInputStream) {
        try {
            File file = saveFileToDisk(fileInputStream);
            nlpUtils.trainModel(file.getAbsolutePath());
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/tokenize")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response tokenizeModel(InputStream fileInputStream) {
        try {
            File file = saveFileToDisk(fileInputStream);
            nlpUtils.tokenizeModel(file.getAbsolutePath());
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/addDocument")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addDocument(InputStream fileInputStream) {
        try {
            Properties props = new Properties();
            props.load(SQLBotAdminResource.class.getClassLoader().getResourceAsStream("sql-bot.properties"));
            SqlInfoIndexer sqlInfoIndexer = new LuceneSqlInfoIndexer(props.getProperty("lucene.index.location"));
            File file = saveFileToDisk(fileInputStream);
            sqlInfoIndexer.addDocument(file);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    private File saveFileToDisk(InputStream fileInputStream) throws IOException {
        File file = File.createTempFile("train", ".prop");
        Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return file;
    }

}
