package edu.ait.nlp.rest;

import edu.ait.nlp.classifier.SQLClassifierImpl;
import edu.ait.nlp.response.SQLResponse;
import edu.ait.nlp.search.lucene.LuceneSqlInfoIndexer;
import edu.ait.nlp.services.KaldiServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

@Path("/sql")
public class SQLBotResource {

    private KaldiServiceImpl kaldiService;

    public SQLBotResource() throws IOException {
        this.kaldiService = new KaldiServiceImpl();
    }

    @POST
    @Path("/ask")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(InputStream fileInputStream) throws URISyntaxException, IOException, ParseException {

        String result = kaldiService.decodeAudio(fileInputStream);
         result = result.replace(".", "").toUpperCase();
        SQLClassifierImpl sqlClassifier = new SQLClassifierImpl();
        List<String> resList = sqlClassifier.getNERFromText(result, null);
        StringBuilder res = new StringBuilder("");
        for(String s : resList){
            res.append(s).append(" ");
        }
        List<SQLResponse> results = kaldiService.getSearchResponse(res.toString());

        return Response.status(Response.Status.OK).entity(kaldiService.getBestMatch(results)).build();

    }

    @GET
    @Path("/ask")
    public Response getResponse(@QueryParam("query") String query) throws IOException, ParseException {
        if(StringUtils.isEmpty(query)){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        List<SQLResponse> results = kaldiService.getSearchResponse(query);
        return Response.status(Response.Status.OK).entity(kaldiService.getBestMatch(results)).build();
    }

}
