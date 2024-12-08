package kz.security_hackathon.group_nine.serverApp;

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
    private static final byte[] iv;

    static {
        // Генерируем общий секретный ключ и IV при запуске сервера
        secretKey = generateSecretKey();
        iv = generateIV();
        System.out.println("Общий IV: " + Base64.getEncoder().encodeToString(iv));
        System.out.println("Общий ключ: " + Base64.getEncoder().encodeToString(secretKey.getEncoded()));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Отправляем клиенту общий IV и ключ
        String ivBase64 = Base64.getEncoder().encodeToString(iv);
        String keyBase64 = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        String message = "IV:" + ivBase64 + ":KEY:" + keyBase64;
        sendMessageToClient(session, message);

        connectedClients.add(session);
        System.out.println("Клиент подключён: " + session.getRemoteAddress());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String encryptedMessage = message.getPayload();
        System.out.println("Получено зашифрованное сообщение: " + encryptedMessage);

        // Передаём сообщение всем клиентам (включая отправителя)
        for (WebSocketSession client : connectedClients) {
            sendMessageToClient(client, encryptedMessage);
        }
    }

    private static SecretKey generateSecretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // Генерируем 128-битный ключ
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[16]; // IV для AES 128 (16 байт)
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private void sendMessageToClient(WebSocketSession client, String message) throws IOException {
        if (client.isOpen()) {
            client.sendMessage(new TextMessage(message));
            System.out.println("Отправлено сообщение клиенту [" + client.getRemoteAddress() + "]: " + message);
        }
    }
}
