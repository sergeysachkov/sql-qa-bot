package edu.ait.nlp.services;

import edu.ait.nlp.model.KaldiResponse;
import edu.ait.nlp.response.SQLResponse;
import edu.ait.nlp.search.SqlInfoSearcher;
import edu.ait.nlp.search.lucene.LuceneSqlInfoSearcher;
import org.apache.lucene.queryparser.classic.ParseException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class KaldiServiceImpl implements AudioRecognitionService {

    private SqlInfoSearcher sqlInfoSearcher;

    public KaldiServiceImpl() throws IOException {
        //todo init from props
        this.sqlInfoSearcher = new LuceneSqlInfoSearcher("C:\\Users\\root\\projects\\sql-qa-bot\\sql-search\\src\\main\\resources");
    }

    public String decodeAudio(InputStream inputStream) {

        final KaldiWebSocketClient clientEndPoint;
        final KaldiResponse response = new KaldiResponse();
        try {
            clientEndPoint = new KaldiWebSocketClient(
                    //todo init from props
                    new URI("ws://172.17.0.2:8888/client/ws/speech?content-type=audio/x-raw,+layout=(string)interleaved,+rate=(int)16000,+format=(string)S16LE,+channels=(int)1"));
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

    public List<SQLResponse> getSearchResponse(String query) throws IOException, ParseException {
        return sqlInfoSearcher.getDocuments(query);
    }

    public String getBestMatch(List<SQLResponse> responses){
        if(responses == null || responses.isEmpty()){
            return "Sorry, don't understand!";
        }
        if(responses.size() == 1){
            return responses.get(0).getText();
        }
        SQLResponse bestResponse = new SQLResponse();
        for(SQLResponse r : responses){
            if(bestResponse.getScore() < r.getScore()){
                bestResponse = r;
            }
        }
        return bestResponse.getText();
    }
}
