package edu.ait.nlp.response;

public class KaldiResponse {
    private String response;
    private boolean finalResponse;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isFinalResponse() {
        return finalResponse;
    }

    public void setFinalResponse(boolean finalResponse) {
        this.finalResponse = finalResponse;
    }
}
