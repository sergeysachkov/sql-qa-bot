package edu.ait.nlp.services;

import java.io.InputStream;

public interface AudioRecognitionService {
    String decodeAudio(InputStream inputStream);
}
