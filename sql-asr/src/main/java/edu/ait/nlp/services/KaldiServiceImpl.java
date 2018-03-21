package edu.ait.nlp.services;

import edu.ait.nlp.response.FinalQueryResponse;
import edu.ait.nlp.response.KaldiResponse;
import edu.ait.nlp.response.SQLResponse;
import edu.ait.nlp.search.SqlInfoSearcher;
import edu.ait.nlp.search.lucene.LuceneSqlInfoSearcher;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class KaldiServiceImpl implements AudioRecognitionService {

    private static final Logger logger = LoggerFactory.getLogger(KaldiServiceImpl.class);

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
        KaldiResponse response = new KaldiResponse();
        List<KaldiResponse> responses = new ArrayList<>();
        boolean useFinal = Boolean.parseBoolean(props.getProperty("use.final", "true"));
        //array to control finishing of kaldi processing
        boolean[]finish = new boolean [] {false};
        int numberOfHypothesis = Integer.parseInt(props.getProperty("max.kaldi.waitingloops", "10"));
        try {
            clientEndPoint = new KaldiWebSocketClient(
                    new URI(props.getProperty("web.socket.address")));
            clientEndPoint.addMessageHandler(new KaldiWebSocketClient.MessageHandler() {
                public void handleMessage(String message) {
                    JsonObject jsonObject = Json.createReader(new StringReader(message)).readObject();
                    if (jsonObject.getInt("status") == 0) {
                        JsonObject obj = jsonObject.getJsonObject("result");
                        if (obj != null) {
                            //if (obj.getBoolean("final"))
                                for (JsonValue value : obj.getJsonArray("hypotheses")) {
                                    KaldiResponse response = new KaldiResponse();
                                    response.setResponse(((JsonObject) value).getString("transcript"));
                                    if (obj.getBoolean("final")){
                                        finish[0] = true;
                                        response.setFinalResponse(true);
                                    }else {
                                        response.setFinalResponse(false);
                                    }
                                    responses.add(response);
                                    logger.debug(((JsonObject) value).getString("transcript"));
                                }
                        }
                    }
                }
            });
            clientEndPoint.sendMessage(inputStream);

            int attempts = 0;
            while (!finish[0]) {
                logger.debug("Do I need to stop {}", finish[0]);
                attempts++;
                //sometimes kaldi error occurs and it won't send final hypothesis need extra logig to avoid infinite loop
                if(numberOfHypothesis < attempts){
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //wait to get final transcript
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }

        if(responses.isEmpty()){
            return " ";
        }
        if(useFinal){
            response = getFinalResponse(responses);
        }else {
            response = getLongestResponse(responses);
        }
        return response.getResponse();
    }

    private KaldiResponse getLongestResponse(List<KaldiResponse> responses) {
        KaldiResponse result = new KaldiResponse();
        for(KaldiResponse response : responses){
            if(result.getResponse() == null){
                result = response;
                continue;
            }
            if(result.getResponse().length() < response.getResponse().length()){
                result = response;
            }
        }
        return result;
    }

    private KaldiResponse getFinalResponse(List<KaldiResponse> responses) {
        for(KaldiResponse response : responses){
            if(response.isFinalResponse()){
                return response;
            }
        }
        return getLongestResponse(responses);
    }

    public List<SQLResponse> getSearchResponse(String query) throws IOException, ParseException {
        return sqlInfoSearcher.getDocuments(query);
    }

    public FinalQueryResponse getBestMatch(List<SQLResponse> responses){
        FinalQueryResponse response = new FinalQueryResponse();
        if(responses == null || responses.isEmpty()){
            return response;
        }
        if(responses.size() == 1){
            response.setText(responses.get(0).getText());
            response.setFound(true);
            return response;
        }
        SQLResponse bestResponse = new SQLResponse();
        for(SQLResponse r : responses){
            if(bestResponse.getScore() < r.getScore()){
                bestResponse = r;
            }
        }
        response.setText(bestResponse.getText());
        response.setFound(true);
        return response;
    }
}
