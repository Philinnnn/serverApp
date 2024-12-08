package kz.security_hackathon.group_nine.serverApp;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.security.SecureRandom;

public class WebSocketMessageHandler extends TextWebSocketHandler {
    private static final Set<WebSocketSession> connectedClients = new HashSet<>();
    private static final SecretKey secretKey;

    static {
        secretKey = generateSecretKey();
        System.out.println("Общий ключ: " + Base64.getEncoder().encodeToString(secretKey.getEncoded()));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String keyBase64 = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        String message = "KEY:" + keyBase64;

        sendMessageToClient(session, message);
        connectedClients.add(session);
        System.out.println("Клиент подключён: " + session.getRemoteAddress());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String receivedMessage = message.getPayload();
        System.out.println("Получено сообщение: " + receivedMessage);

        String[] parts = receivedMessage.split(":");
        if (parts.length == 4 && parts[0].equals("IV") && parts[2].equals("MSG")) {
            String ivBase64 = parts[1];
            String encryptedMessage = parts[3];

            for (WebSocketSession client : connectedClients) {
                if (!client.equals(session)) {
                    sendMessageToClient(client, receivedMessage);
                }
            }
        }
    }

    private static SecretKey generateSecretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessageToClient(WebSocketSession client, String message) throws IOException {
        if (client.isOpen()) {
            client.sendMessage(new TextMessage(message));
            System.out.println("Отправлено сообщение клиенту [" + client.getRemoteAddress() + "]: " + message);
        }
    }
}
