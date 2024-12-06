package kz.security_hackathon.group_nine.serverApp;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class MessageController {

    @PostMapping("/sendMessage")
    public String receiveMessage(
            @RequestParam String clientIP,
            @RequestBody String message) {
        System.out.println("Клиент подключён: " + clientIP);
        if (message == null || message.isBlank()) {
            return "Ошибка: Пустое сообщение от клиента " + clientIP;
        }
        System.out.println("Сообщение от клиента [" + clientIP + "]: " + message);
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
