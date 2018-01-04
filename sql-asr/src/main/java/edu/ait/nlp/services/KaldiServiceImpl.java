package edu.ait.nlp.services;

import edu.ait.nlp.response.KaldiResponse;
import edu.ait.nlp.response.SQLResponse;
import edu.ait.nlp.search.SqlInfoSearcher;
import edu.ait.nlp.search.lucene.LuceneSqlInfoSearcher;
import org.apache.lucene.queryparser.classic.ParseException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

public class KaldiServiceImpl implements AudioRecognitionService {

    private SqlInfoSearcher sqlInfoSearcher;
    private Properties props;

    public KaldiServiceImpl() throws IOException {
        props = new Properties();
        props.load(KaldiServiceImpl.class.getClassLoader().getResourceAsStream("sql-bot.properties"));
        File f = new File(props.getProperty("lucene.index.location"));
        this.sqlInfoSearcher = new LuceneSqlInfoSearcher(f.getAbsolutePath());
    }

    public String decodeAudio(InputStream inputStream) {

        final KaldiWebSocketClient clientEndPoint;
        final KaldiResponse response = new KaldiResponse();
        try {
            clientEndPoint = new KaldiWebSocketClient(
                    new URI(props.getProperty("web.socket.address")));
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
