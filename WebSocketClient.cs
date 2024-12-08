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

        await ReceiveKeyAndIV();

        // Запуск асинхронного прослушивания сообщений от сервера
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

            string encryptedMessage = EncryptMessage(message);
            await SendMessage(encryptedMessage);
        }

        await _client.CloseAsync(WebSocketCloseStatus.NormalClosure, "Закрытие соединения", CancellationToken.None);
    }

    /// <summary>
    /// Метод для получения ключа и IV от сервера.
    /// </summary>
    private static async Task ReceiveKeyAndIV()
    {
        byte[] buffer = new byte[1024];
        var result = await _client.ReceiveAsync(buffer, CancellationToken.None);
        string keyMessage = Encoding.UTF8.GetString(buffer, 0, result.Count);

        if (keyMessage.StartsWith("IV:"))
        {
            string[] parts = keyMessage.Split(':');
            byte[] ivBytes = Convert.FromBase64String(parts[1]);
            byte[] keyBytes = Convert.FromBase64String(parts[3]);

            _aes = Aes.Create();
            _aes.Key = keyBytes;
            _aes.IV = ivBytes;

            _isKeyReceived = true;
        }
    }

    /// <summary>
    /// Асинхронный метод для прослушивания сообщений от сервера.
    /// </summary>
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

                string encryptedMessage = Encoding.UTF8.GetString(buffer, 0, result.Count);
                Console.WriteLine("Получено зашифрованное сообщение от сервера: " + encryptedMessage);

                if (_isKeyReceived)
                {
                    try
                    {
                        string decryptedMessage = DecryptMessage(encryptedMessage);
                        Console.WriteLine("Расшифрованное сообщение: " + decryptedMessage);
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine("Ошибка расшифровки: " + ex.Message);
                    }
                }
            }
            catch (WebSocketException ex)
            {
                Console.WriteLine("Ошибка WebSocket: " + ex.Message);
                break;
            }
        }
    }

    /// <summary>
    /// Метод для шифрования сообщений.
    /// </summary>
    private static string EncryptMessage(string message)
    {
        using var encryptor = _aes.CreateEncryptor();
        byte[] plainBytes = Encoding.UTF8.GetBytes(message);
        byte[] encryptedBytes = encryptor.TransformFinalBlock(plainBytes, 0, plainBytes.Length);
        return Convert.ToBase64String(encryptedBytes);
    }

    /// <summary>
    /// Метод для расшифровки сообщений.
    /// </summary>
    private static string DecryptMessage(string encryptedMessage)
    {
        byte[] encryptedBytes = Convert.FromBase64String(encryptedMessage);
        using var decryptor = _aes.CreateDecryptor();
        byte[] decryptedBytes = decryptor.TransformFinalBlock(encryptedBytes, 0, encryptedBytes.Length);
        return Encoding.UTF8.GetString(decryptedBytes);
    }

    /// <summary>
    /// Метод для отправки сообщений серверу.
    /// </summary>
    private static async Task SendMessage(string message)
    {
        byte[] buffer = Encoding.UTF8.GetBytes(message);
        await _client.SendAsync(buffer, WebSocketMessageType.Text, true, CancellationToken.None);
    }
}
