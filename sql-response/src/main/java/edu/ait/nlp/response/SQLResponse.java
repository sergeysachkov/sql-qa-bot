package edu.ait.nlp.response;

import java.util.Objects;

public class SQLResponse {
    private String text;
    private float score;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SQLResponse that = (SQLResponse) o;
        return Float.compare(that.score, score) == 0 &&
                Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {

        return Objects.hash(text, score);
    }

    @Override
    public String toString() {
        return "SQLResponse{" +
                "text='" + text + '\'' +
                ", score=" + score +
                '}';
    }
}
