package edu.ait.nlp.rest;

import edu.ait.nlp.classifier.SQLClassifierImpl;
import edu.ait.nlp.response.FinalQueryResponse;
import edu.ait.nlp.response.SQLResponse;
import edu.ait.nlp.search.lucene.DidYouMeanService;
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
    private DidYouMeanService service;

    public SQLBotResource() throws IOException {
        this.kaldiService = new KaldiServiceImpl();
        this.service = new DidYouMeanService();
    }

    @POST
    @Path("/ask")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(InputStream fileInputStream) throws IOException, ParseException {

        String result = kaldiService.decodeAudio(fileInputStream);
        result = result.replace(".", "").toUpperCase();
        if(StringUtils.isEmpty(result)){
            //avoid bad arg exception from getResponse(result) call
            result = "none";
        }
        return getResponse(result);


    }

    @GET
    @Path("/ask")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResponse(@QueryParam("query") String query) throws IOException, ParseException {
        if(StringUtils.isEmpty(query)){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        SQLClassifierImpl sqlClassifier = new SQLClassifierImpl();
        List<String> resList = sqlClassifier.getNERFromText(query.toUpperCase(), null);
        //todo remove this logic after model update, need uppercase in the model only
        if(resList == null || resList.isEmpty()){
            resList = sqlClassifier.getNERFromText(query, null);
        }
        StringBuilder res = new StringBuilder("");
        for(String s : resList){
            res.append(s).append(" ");
        }
        List<SQLResponse> results = kaldiService.getSearchResponse(res.toString());
        FinalQueryResponse response = new FinalQueryResponse();
        if(results == null || results.isEmpty()){
            response.setFound(false);
            response.setVariants(service.didYouMean(query));
        }else {
            response = kaldiService.getBestMatch(results);
        }

        if(StringUtils.isEmpty(response.getText()) && response.getVariants().isEmpty()){
            //response not found when did you mean turned off
            response.setFound(true);
            response.setText("Sorry, don't understand!");
        }

        return Response.status(Response.Status.OK).entity(response).build();
    }

}
