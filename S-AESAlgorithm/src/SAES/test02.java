package SAES;

import java.math.BigInteger;

public class test02 {

    
    public static String cbcEncrypt(String plaintext, String key, String iv) {
        test01 saes = new test01(key);
        String previousCipherBlock = iv;
        StringBuilder ciphertext = new StringBuilder();

        // 将输入转换为二进制
        StringBuilder binaryInput = new StringBuilder();
        for (char c : plaintext.toCharArray()) {
            binaryInput.append(String.format("%8s", Integer.toBinaryString(c)).replace(" ", "0"));
        }

        // 检查字符数并进行填充
        if (plaintext.length() % 2 == 1) {  // 奇数字符，添加00000001
            binaryInput.append("00000001");
        } else {  // 偶数字符，添加00000010 00000010
            binaryInput.append("00000010").append("00000010");
        }

        // 使用CBC模式进行加密
        for (int i = 0; i < binaryInput.length(); i += 16) {
            String block = binaryInput.substring(i, Math.min(i + 16, binaryInput.length()));

            // 明文块与前一个密文块异或
            StringBuilder xorBlock = new StringBuilder();
            for (int j = 0; j < block.length(); j++) {
                xorBlock.append(block.charAt(j) ^ previousCipherBlock.charAt(j));
            }

            String cipherBlock = saes.encrypt(xorBlock.toString());
            ciphertext.append(cipherBlock);

            previousCipherBlock = cipherBlock;
        }

        // 将结果转换为十六进制字符串
        BigInteger binaryCipherText = new BigInteger(ciphertext.toString(), 2);
        return binaryCipherText.toString(16);  // 转换为16进制字符串
    }

    public static String cbcDecrypt(String ciphertext, String key, String iv) {
        test01 saes = new test01(key);
        String previousCipherBlock = iv;
        StringBuilder binaryOutput = new StringBuilder();

        // 从十六进制字符串转换为二进制
        String binaryInput = new BigInteger(ciphertext, 16).toString(2);

        // 使用CBC模式进行解密
        for (int i = 0; i < binaryInput.length(); i += 16) {
            String block = binaryInput.substring(i, Math.min(i + 16, binaryInput.length()));

            String decryptedBlock = saes.decrypt(block);

            // 解密块与前一个密文块异或
            StringBuilder xorBlock = new StringBuilder();
            for (int j = 0; j < decryptedBlock.length(); j++) {
                xorBlock.append(decryptedBlock.charAt(j) ^ previousCipherBlock.charAt(j));
            }
            binaryOutput.append(xorBlock);

            previousCipherBlock = block;
        }

        // 移除填充
        if (binaryOutput.toString().endsWith("00000001")) {
            binaryOutput.setLength(binaryOutput.length() - 8);
        } else if (binaryOutput.toString().endsWith("0000001000000010")) {
            binaryOutput.setLength(binaryOutput.length() - 16);
        }

        // 将二进制转换回ASCII
        StringBuilder asciiOutput = new StringBuilder();
        for (int i = 0; i < binaryOutput.length(); i += 8) {
            int charCode = Integer.parseInt(binaryOutput.substring(i, i + 8), 2);
            asciiOutput.append((char) charCode);
        }

        return asciiOutput.toString();
    }

    public static void main(String[] args) {
        String plaintext = "Hello World!";
        System.out.println("明文是: " + plaintext);
        String key = "0101001000000001";
        String iv = "1111001110001011";
        String ciphertext = cbcEncrypt(plaintext, key, iv);
        System.out.println("密文是: " + ciphertext);
        String decryptedText = cbcDecrypt(ciphertext, key, iv);
        System.out.println("解密后的明文是: " + decryptedText);
        assert plaintext.equals(decryptedText);
    }
}