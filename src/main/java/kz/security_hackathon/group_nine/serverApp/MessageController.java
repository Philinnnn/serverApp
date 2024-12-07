package kz.security_hackathon.group_nine.serverApp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/chat")
public class MessageController {

    @Autowired
    private WebSocketMessageHandler webSocketMessageHandler;

    @PostMapping("/sendMessage")
    public String receiveMessage(
            @RequestParam String clientIP,
            @RequestBody String encryptedMessage) {
        System.out.println("Сообщение от клиента [" + clientIP + "]: " + encryptedMessage);
        try {
            webSocketMessageHandler.broadcastMessage(encryptedMessage);
        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка при отправке сообщения";
        }
        return "Сообщение принято от " + clientIP;
    }

    @PostMapping("/connect")
    public String connectClient(@RequestParam String clientIP) {
        System.out.println("Клиент подключён: " + clientIP);
        return "Подключение успешно: " + clientIP;
    }

    @PostMapping("/disconnect")
    public String disconnectClient(@RequestParam String clientIP) {
        System.out.println("Клиент отключён: " + clientIP);
        return "Отключение успешно: " + clientIP;
    }
}