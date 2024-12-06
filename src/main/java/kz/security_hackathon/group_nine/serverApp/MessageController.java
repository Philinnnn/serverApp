package kz.security_hackathon.group_nine.serverApp;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/chat")
public class MessageController {

    // Список всех подключенных клиентов
    private static final Set<String> connectedClients = new HashSet<>();

    @PostMapping("/sendMessage")
    public String receiveMessage(
            @RequestParam String clientIP,
            @RequestBody String encryptedMessage) {
        System.out.println("Клиент подключён: " + clientIP);

        // Просто выводим зашифрованное сообщение и отправляем его другим клиентам
        System.out.println("Сообщение от клиента [" + clientIP + "]: " + encryptedMessage);

        // Отправляем зашифрованное сообщение всем клиентам, кроме отправителя
        for (String client : connectedClients) {
            if (!client.equals(clientIP)) {
                sendMessageToClient(client, encryptedMessage);
            }
        }

        return "Сообщение принято от " + clientIP;
    }

    @PostMapping("/connect")
    public String connectClient(@RequestParam String clientIP) {
        connectedClients.add(clientIP); // Добавляем клиента в список подключений
        System.out.println("Клиент подключён: " + clientIP);
        return "Подключение успешно: " + clientIP;
    }

    @PostMapping("/disconnect")
    public String disconnectClient(@RequestParam String clientIP) {
        connectedClients.remove(clientIP); // Убираем клиента из списка подключений
        System.out.println("Клиент отключён: " + clientIP);
        return "Отключение успешно: " + clientIP;
    }

    private void sendMessageToClient(String clientIP, String encryptedMessage) {
        System.out.println("Отправка сообщения клиенту [" + clientIP + "]: " + encryptedMessage);
    }
}
