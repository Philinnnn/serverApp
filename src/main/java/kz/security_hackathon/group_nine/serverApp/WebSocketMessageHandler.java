package kz.security_hackathon.group_nine.serverApp;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Component
public class WebSocketMessageHandler extends TextWebSocketHandler {
    private static final Set<WebSocketSession> connectedClients = new HashSet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        connectedClients.add(session);
        System.out.println("Клиент подключён: " + session.getRemoteAddress());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String clientIP = Objects.requireNonNull(session.getRemoteAddress()).toString();
        String encryptedMessage = message.getPayload();
        System.out.println("Сообщение от клиента [" + clientIP + "]: " + encryptedMessage);
        for (WebSocketSession client : connectedClients) {
            sendMessageToClient(client, encryptedMessage);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        connectedClients.remove(session);
        System.out.println("Клиент отключён: " + session.getRemoteAddress());
    }

    public void broadcastMessage(String message) throws IOException {
        for (WebSocketSession client : connectedClients) {
            sendMessageToClient(client, message);
        }
        // System.out.println(connectedClients.size());
    }

    private void sendMessageToClient(WebSocketSession client, String encryptedMessage) throws IOException {
        if (client.isOpen()) {
            client.sendMessage(new TextMessage(encryptedMessage));
            System.out.println("Отправка сообщения клиенту [" + client.getRemoteAddress() + "]: " + encryptedMessage);
        } else {
            System.out.println("Не удалось отправить сообщение. Клиент [" + client.getRemoteAddress() + "] отключён.");
        }
    }
}