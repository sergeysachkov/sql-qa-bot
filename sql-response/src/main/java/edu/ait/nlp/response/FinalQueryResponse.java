package edu.ait.nlp.response;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FinalQueryResponse {
    private String text;
    private boolean found;
    private Set<String> variants;

    public FinalQueryResponse() {
        variants = new HashSet<>();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public Set<String> getVariants() {
        if(variants == null){
            variants = new HashSet<>();
        }
        return variants;
    }

    public void setVariants(Set<String> variants) {
        this.variants = variants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FinalQueryResponse that = (FinalQueryResponse) o;
        return found == that.found &&
                Objects.equals(text, that.text) &&
                Objects.equals(variants, that.variants);
    }

    @Override
    public int hashCode() {

        return Objects.hash(text, found, variants);
    }

    @Override
    public String toString() {
        return "FinalQueryResponse{" +
                "text='" + text + '\'' +
                ", found=" + found +
                ", variants=" + variants +
                '}';
    }
}
