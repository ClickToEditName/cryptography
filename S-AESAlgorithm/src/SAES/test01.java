package SAES;

import java.math.*;

public class test01 {
    private String[][] S_BOX = {
            {"1001", "0100", "1010", "1011"},
            {"1101", "0001", "1000", "0101"},
            {"0110", "0010", "0000", "0011"},
            {"1100", "1110", "1111", "0111"}
    };

    private String[][] S_BOX_INV = {
            {"1010", "0101", "1001", "1011"},
            {"0001", "0111", "1000", "1111"},
            {"0110", "0000", "0010", "0011"},
            {"1100", "0100", "1101", "1110"}
    };

    private String[] RCON = {"10000000", "00110000"};
    private int[][] MixMatrix = {{1, 4}, {4, 1}};
    private int[][] MixMatrix_INV = {{9, 2}, {2, 9}};
    private String[] keys_list;
    private String key;

    public test01(String key) {
        this.key = key;
        if (key.length() % 16 != 0) {
            throw new IllegalArgumentException("Key length must be a multiple of 16!");
        }
        keys_list = new String[key.length() / 16];
        for (int i = 0; i < keys_list.length; i++) {
            keys_list[i] = key.substring(i * 16, (i + 1) * 16);
        }
    }

    public String[] keyExpansion() {
        String w0 = key.substring(0, 8);
        String w1 = key.substring(8, 16);
        String temp = xor(RCON[0], subNib(rotNib(w1)));
        String w2 = xor(w0, temp);
        String w3 = xor(w1, w2);
        temp = xor(RCON[1], subNib(rotNib(w3)));
        String w4 = xor(w2, temp);
        String w5 = xor(w3, w4);
        return new String[]{w0 + w1, w2 + w3, w4 + w5};
    }

    public String encrypt(String plaintext) {
        String ciphertext = plaintext;
        for (String k : keys_list) {
            key = k;
            ciphertext = singleEncrypt(ciphertext);
        }
        return ciphertext;
    }

    public String decrypt(String ciphertext) {
        String plaintext = ciphertext;
        for (int i = keys_list.length - 1; i >= 0; i--) {
            key = keys_list[i];
            plaintext = singleDecrypt(plaintext);
        }
        return plaintext;
    }

    public String singleEncrypt(String plaintext) {
        String[] keys = keyExpansion();
        String initialText = roundKeyAddition(plaintext, keys[0]);
        String afterSubByte1 = subByte(initialText, false);
        String afterRowShift1 = rowShift(afterSubByte1);
        String afterMixColumns1 = mixColumns(afterRowShift1, MixMatrix);
        String afterKeyAddition1 = roundKeyAddition(afterMixColumns1, keys[1]);
        String afterSubByte2 = subByte(afterKeyAddition1, false);
        String afterRowShift2 = rowShift(afterSubByte2);
        return roundKeyAddition(afterRowShift2, keys[2]);
    }

    public String singleDecrypt(String ciphertext) {
        String[] keys = keyExpansion();
        String initialText = roundKeyAddition(ciphertext, keys[2]);
        String afterInvRowShift1 = rowShift(initialText);
        String afterInvSubByte1 = subByte(afterInvRowShift1, true);
        String afterKeyAddition1 = roundKeyAddition(afterInvSubByte1, keys[1]);
        String afterInvMixColumns = mixColumns(afterKeyAddition1, MixMatrix_INV);
        String afterInvRowShift2 = rowShift(afterInvMixColumns);
        String afterInvSubByte2 = subByte(afterInvRowShift2, true);
        return roundKeyAddition(afterInvSubByte2, keys[0]);
    }

    private String xor(String a, String b) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            result.append(a.charAt(i) ^ b.charAt(i));
        }
        return result.toString();
    }

    private String rotNib(String text) {
        return text.substring(4) + text.substring(0, 4);
    }

    private String subNib(String text) {
        String leftNibble = text.substring(0, 4);
        String rightNibble = text.substring(4);
        return S_BOX[Integer.parseInt(leftNibble.substring(0, 2), 2)][Integer.parseInt(leftNibble.substring(2), 2)] +
               S_BOX[Integer.parseInt(rightNibble.substring(0, 2), 2)][Integer.parseInt(rightNibble.substring(2), 2)];
    }

    private String roundKeyAddition(String text, String key) {
        return xor(text, key);
    }

    private String subByte(String text, boolean useInverse) {
        String[][] sbox = useInverse ? S_BOX_INV : S_BOX;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i += 4) {
            String nibble = text.substring(i, i + 4);
            int row = Integer.parseInt(nibble.substring(0, 2), 2);
            int col = Integer.parseInt(nibble.substring(2), 2);
            result.append(sbox[row][col]);
        }
        return result.toString();
    }

    private String rowShift(String text) {
        return text.substring(0, 4) + text.substring(12) + text.substring(8, 12) + text.substring(4, 8);
    }

    private String mixColumns(String input, int[][] matrix) {
        int[] col1 = {Integer.parseInt(input.substring(0, 4), 2), Integer.parseInt(input.substring(4, 8), 2)};
        int[] col2 = {Integer.parseInt(input.substring(8, 12), 2), Integer.parseInt(input.substring(12), 2)};
        int[] mixedCol1 = mixColumn(col1, matrix);
        int[] mixedCol2 = mixColumn(col2, matrix);
        return String.format("%4s%4s%4s%4s", Integer.toBinaryString(mixedCol1[0]), Integer.toBinaryString(mixedCol1[1]),
                Integer.toBinaryString(mixedCol2[0]), Integer.toBinaryString(mixedCol2[1])).replace(' ', '0');
    }

    private int[] mixColumn(int[] column, int[][] matrix) {
        int[] result = new int[2];
        for (int i = 0; i < 2; i++) {
            result[i] = gfAdd(gfMul(matrix[i][0], column[0]), gfMul(matrix[i][1], column[1]));
        }
        return result;
    }

    private int gfAdd(int a, int b) {
        return a ^ b;
    }

    private int gfMul(int a, int b) {
        int result = 0;
        while (b > 0) {
            if ((b & 1) != 0) result ^= a;
            a <<= 1;
            b >>= 1;
        }
        int modulus = 0b10011;
        while (result >= 0b10000) {
            result ^= (modulus << (Integer.toBinaryString(result).length() - 5));
        }
        return result;
    }

    public void setKey(String newKey) {
        if (newKey.length() % 16 != 0) {
            throw new IllegalArgumentException("Key length must be a multiple of 16!");
        }
        this.key = newKey;
        keys_list = new String[newKey.length() / 16];
        for (int i = 0; i < keys_list.length; i++) {
            keys_list[i] = newKey.substring(i * 16, (i + 1) * 16);
        }
    }

    public static void main(String[] args) {
        String key = "0101001000000001";
        test01 saes = new test01(key);
        String plaintext = "0110101110100011";
        System.out.println("本次SAES加密明文为：" + plaintext);
        String encryptedCiphertext = saes.encrypt(plaintext);
        System.out.println("通过SAES加密后的密文为：" + encryptedCiphertext);
        String decryptedPlaintext = saes.decrypt(encryptedCiphertext);
        System.out.println("通过SAES解密后的明文为：" + decryptedPlaintext);
    }

    public String encryptASCII(String plaintext) {
        // 将ASCII字符串转换为二进制字符串
        StringBuilder binaryInput = new StringBuilder();
        for (char c : plaintext.toCharArray()) {
            binaryInput.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }

        // 添加填充
        while (binaryInput.length() % 16 != 0) {
            binaryInput.append("0");  // 采用简单的零填充
        }

        // 进行加密
        StringBuilder ciphertext = new StringBuilder();
        for (int i = 0; i < binaryInput.length(); i += 16) {
            String block = binaryInput.substring(i, i + 16);
            ciphertext.append(singleEncrypt(block));
        }

        // 返回十六进制字符串
        return new BigInteger(ciphertext.toString(), 2).toString(16);
    }

    // 新增：支持ASCII字符串解密
    public String decryptASCII(String ciphertext) {
        // 将十六进制字符串转换为二进制字符串
        String binaryInput = new BigInteger(ciphertext, 16).toString(2);

        // 确保输入长度是16的倍数
        while (binaryInput.length() % 16 != 0) {
            binaryInput = "0" + binaryInput;  // 前补零
        }

        // 进行解密
        StringBuilder binaryOutput = new StringBuilder();
        for (int i = 0; i < binaryInput.length(); i += 16) {
            String block = binaryInput.substring(i, i + 16);
            binaryOutput.append(singleDecrypt(block));
        }

        // 将二进制输出转换为ASCII字符串
        StringBuilder asciiOutput = new StringBuilder();
        for (int i = 0; i < binaryOutput.length(); i += 8) {
            int charCode = Integer.parseInt(binaryOutput.substring(i, Math.min(i + 8, binaryOutput.length())), 2);
            asciiOutput.append((char) charCode);
        }

        return asciiOutput.toString();
    }
}
