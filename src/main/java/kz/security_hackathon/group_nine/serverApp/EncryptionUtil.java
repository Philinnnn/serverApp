package kz.security_hackathon.group_nine.serverApp;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public static String encrypt(String data, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        byte[] ivAndEncryptedData = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, ivAndEncryptedData, 0, iv.length);
        System.arraycopy(encryptedData, 0, ivAndEncryptedData, iv.length, encryptedData.length);
        return Base64.getEncoder().encodeToString(ivAndEncryptedData);
    }

    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
        byte[] ivAndEncryptedData = Base64.getDecoder().decode(encryptedData);
        byte[] iv = new byte[16];
        byte[] encryptedBytes = new byte[ivAndEncryptedData.length - iv.length];
        System.arraycopy(ivAndEncryptedData, 0, iv, 0, iv.length);
        System.arraycopy(ivAndEncryptedData, iv.length, encryptedBytes, 0, encryptedBytes.length);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return new String(cipher.doFinal(encryptedBytes));
    }

    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        return keyGen.generateKey();
    }

    public static byte[] generateIV() {
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }
}
