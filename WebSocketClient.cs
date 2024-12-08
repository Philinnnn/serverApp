using System;
using System.Net.WebSockets;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

class WebSocketClient
{
    private static ClientWebSocket _client;
    private static Aes _aes;
    private static bool _isKeyReceived = false;

    static async Task Main(string[] args)
    {
        Console.WriteLine("Введите адрес WebSocket сервера (например, wss://localhost:8443/ws):");
        string serverUrl = Console.ReadLine();

        _client = new ClientWebSocket();
        _client.Options.RemoteCertificateValidationCallback = (sender, cert, chain, sslPolicyErrors) => true;

        await _client.ConnectAsync(new Uri(serverUrl), CancellationToken.None);
        Console.WriteLine("Соединение установлено!");

        await ReceiveKey();

        _ = Task.Run(ListenForMessages);

        while (true)
        {
            if (!_isKeyReceived)
            {
                await Task.Delay(1000);
                continue;
            }

            Console.WriteLine("Введите сообщение для отправки (или 'exit' для выхода):");
            string message = Console.ReadLine();
            if (message.Equals("exit", StringComparison.OrdinalIgnoreCase)) break;

            string encryptedMessage = EncryptMessage(message, out string ivBase64);
            string finalMessage = $"IV:{ivBase64}:MSG:{encryptedMessage}";
            await SendMessage(finalMessage);
        }

        await _client.CloseAsync(WebSocketCloseStatus.NormalClosure, "Закрытие соединения", CancellationToken.None);
    }

    private static async Task ReceiveKey()
    {
        byte[] buffer = new byte[1024];
        var result = await _client.ReceiveAsync(buffer, CancellationToken.None);
        string keyMessage = Encoding.UTF8.GetString(buffer, 0, result.Count);

        if (keyMessage.StartsWith("KEY:"))
        {
            string keyBase64 = keyMessage.Substring(4);
            byte[] keyBytes = Convert.FromBase64String(keyBase64);

            _aes = Aes.Create();
            _aes.Key = keyBytes;

            _isKeyReceived = true;
            Console.WriteLine("Ключ получен.");
        }
    }

    private static async Task ListenForMessages()
    {
        byte[] buffer = new byte[1024];
        while (_client.State == WebSocketState.Open)
        {
            try
            {
                var result = await _client.ReceiveAsync(buffer, CancellationToken.None);
                if (result.MessageType == WebSocketMessageType.Close)
                {
                    Console.WriteLine("Соединение закрыто сервером.");
                    break;
                }

                string receivedMessage = Encoding.UTF8.GetString(buffer, 0, result.Count);
                Console.WriteLine("Получено сообщение: " + receivedMessage);

                string[] parts = receivedMessage.Split(":");
                if (parts.Length == 4 && parts[0] == "IV" && parts[2] == "MSG")
                {
                    string ivBase64 = parts[1];
                    string encryptedMessage = parts[3];
                    string decryptedMessage = DecryptMessage(encryptedMessage, ivBase64);
                    Console.WriteLine("Расшифрованное сообщение: " + decryptedMessage);
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("Ошибка при обработке сообщения: " + ex.Message);
            }
        }
    }

    private static string EncryptMessage(string message, out string ivBase64)
    {
        _aes.GenerateIV();
        ivBase64 = Convert.ToBase64String(_aes.IV);

        using var encryptor = _aes.CreateEncryptor();
        byte[] plainBytes = Encoding.UTF8.GetBytes(message);
        byte[] encryptedBytes = encryptor.TransformFinalBlock(plainBytes, 0, plainBytes.Length);

        return Convert.ToBase64String(encryptedBytes);
    }

    private static string DecryptMessage(string encryptedMessage, string ivBase64)
    {
        byte[] ivBytes = Convert.FromBase64String(ivBase64);
        byte[] encryptedBytes = Convert.FromBase64String(encryptedMessage);

        using var decryptor = _aes.CreateDecryptor(_aes.Key, ivBytes);
        byte[] decryptedBytes = decryptor.TransformFinalBlock(encryptedBytes, 0, encryptedBytes.Length);

        return Encoding.UTF8.GetString(decryptedBytes);
    }

    private static async Task SendMessage(string message)
    {
        byte[] buffer = Encoding.UTF8.GetBytes(message);
        await _client.SendAsync(buffer, WebSocketMessageType.Text, true, CancellationToken.None);
    }
}
