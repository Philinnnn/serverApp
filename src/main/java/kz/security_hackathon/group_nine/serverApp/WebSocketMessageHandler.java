package kz.security_hackathon.group_nine.serverApp;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

public class WebSocketMessageHandler extends TextWebSocketHandler {

    // Список всех подключенных клиентов
    private static final Set<WebSocketSession> connectedClients = new HashSet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        connectedClients.add(session); // Добавляем клиента в список подключений
        System.out.println("Клиент подключён: " + session.getRemoteAddress());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String clientIP = session.getRemoteAddress().toString();
        String encryptedMessage = message.getPayload();

        System.out.println("Сообщение от клиента [" + clientIP + "]: " + encryptedMessage);

        // Отправляем сообщение всем клиентам, кроме отправителя
        for (WebSocketSession client : connectedClients) {
            if (!client.equals(session)) {
                sendMessageToClient(client, encryptedMessage);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        connectedClients.remove(session); // Убираем клиента из списка подключений
        System.out.println("Клиент отключён: " + session.getRemoteAddress());
    }

    private void sendMessageToClient(WebSocketSession client, String encryptedMessage) throws IOException {
        client.sendMessage(new TextMessage(encryptedMessage)); // Отправляем сообщение клиенту
        System.out.println("Отправка сообщения клиенту [" + client.getRemoteAddress() + "]: " + encryptedMessage);
    }
}
