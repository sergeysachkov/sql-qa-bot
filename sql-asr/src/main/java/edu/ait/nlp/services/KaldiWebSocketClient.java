package edu.ait.nlp.services;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;


@ClientEndpoint
public class KaldiWebSocketClient {
    Session userSession = null;
    private MessageHandler messageHandler;

    public KaldiWebSocketClient(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider
                    .getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null)
            this.messageHandler.handleMessage(message);
    }

    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    public void sendMessage(InputStream message) throws IOException {
        if(this.userSession.isOpen()) {
            byte[] bytes = IOUtils.toByteArray(message);
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            this.userSession.getBasicRemote().sendBinary(byteBuffer);
            this.userSession.getBasicRemote().sendText("EOS");
        }

    }


    public static interface MessageHandler {
        public void handleMessage(String message);
    }
}