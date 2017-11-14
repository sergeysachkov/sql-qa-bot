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

public class KaldiService {


    public String decodeAudio(InputStream inputStream) throws URISyntaxException {

        final KaldiWebSocketClient clientEndPoint = new KaldiWebSocketClient(
                new URI("ws://192.168.1.6:8888/client/ws/speech?content-type=audio/x-raw,+layout=(string)interleaved,+rate=(int)16000,+format=(string)S16LE,+channels=(int)1"));

        final KaldiResponse response = new KaldiResponse();
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
        try {
            clientEndPoint.sendMessage(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!response.isInitialized()) {
            //wait to get final transcript
        }

        return response.getResponse();
    }
}
