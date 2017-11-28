package edu.ait.nlp.rest;

import edu.ait.nlp.services.KaldiServiceImpl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URISyntaxException;

@Path("/sql")
public class SQLBotResource {

    private KaldiServiceImpl kaldiService = new KaldiServiceImpl();
    @POST
    @Path("/ask")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(InputStream fileInputStream) throws URISyntaxException {

        String result = kaldiService.decodeAudio(fileInputStream);
        return Response.status(200).entity(result).build();

    }

    @GET
    @Path("/ask")
    public Response uploadFile() {
        String output = "";
        return Response.status(200).entity(output).build();

    }

}
