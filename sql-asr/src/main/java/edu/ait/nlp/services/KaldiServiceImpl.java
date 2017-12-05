package edu.ait.nlp.services;

import edu.ait.nlp.model.KaldiResponse;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

public class KaldiServiceImpl implements AudioRecognitionService {


    public String decodeAudio(InputStream inputStream) {

        final KaldiWebSocketClient clientEndPoint;
        final KaldiResponse response = new KaldiResponse();
        try {
            clientEndPoint = new KaldiWebSocketClient(
                    new URI("ws://localhost:8888/client/ws/speech?content-type=audio/x-raw,+layout=(string)interleaved,+rate=(int)16000,+format=(string)S16LE,+channels=(int)1"));
            clientEndPoint.addMessageHandler(new KaldiWebSocketClient.MessageHandler() {
                public void handleMessage(String message) {
                    JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();
                    if (jsonObject.getInt("status") == 0) {
                        JsonObject obj = jsonObject.getJsonObject("result");
                        if (obj != null) {
                            if (obj.getBoolean("final"))
                                for (JsonValue value : obj.getJsonArray("hypotheses")) {
                                    response.setResponse(((JsonObject) value).getString("transcript"));
                                    response.setInitialized(true);
                                    System.out.println(((JsonObject) value).getString("transcript"));
                                }
                        }
                    }
                }
            });
            clientEndPoint.sendMessage(inputStream);


            while (!response.isInitialized()) {
                //wait to get final transcript
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        return response.getResponse();
    }
}
