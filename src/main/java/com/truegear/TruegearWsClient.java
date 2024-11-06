package com.truegear;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class TruegearWsClient extends WebSocketClient {
    public interface ConnectCallback {
        void onConnectionChange(boolean connected);
    }

    public interface MessageReviCallback{
        void onMessageRevi(String msg);
    }
    private ConnectCallback _callback;
    private MessageReviCallback _msgCallback;
    public TruegearWsClient(URI serverURI) {
        super(serverURI);
    }
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        _callback.onConnectionChange(true);
        System.out.println("opened connection");
    }

    public void addPlayerConnectedCallback(ConnectCallback callback) {
        _callback = callback;
    }

    public void addPlayerMessageReviCallback(MessageReviCallback callback) {
        _msgCallback = callback;
    }
    @Override
    public void onMessage(String message) {
        System.out.println("received: " + message);
        _msgCallback.onMessageRevi(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        _callback.onConnectionChange(false);
        System.out.println(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: "
                        + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println(ex.toString());
    }
}
