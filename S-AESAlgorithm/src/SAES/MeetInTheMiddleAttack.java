package SAES;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MeetInTheMiddleAttack {

    public static List<String[]> meetInTheMiddleAttack(String[][] pairs) {
        test01 saes = new test01("0000000000000000");  // 创建一个默认密钥的SAES实例
        List<String[]> possibleKeysSet = null;  // 存储所有明密文对的交集

        for (String[] pair : pairs) {
            String plaintext = pair[0];
            String ciphertext = pair[1];

            String[][] encryptArray = new String[(int) Math.pow(2, 16)][2];  // 存储加密后的中间值和对应的 key1
            List<String[]> currentPossibleKeys = new ArrayList<>();

            // Step 1: 从明文开始加密，直到中间值
            for (int key1 = 0; key1 < Math.pow(2, 16); key1++) {
                String key1Str = String.format("%016d", Long.parseLong(Integer.toBinaryString(key1)));
                saes.setKey(key1Str);
                String intermediateCiphertext = saes.singleEncrypt(plaintext);
                encryptArray[key1][0] = intermediateCiphertext;
                encryptArray[key1][1] = key1Str;
            }

            // Step 2: 从密文开始解密，直到中间值
            for (int key2 = 0; key2 < Math.pow(2, 16); key2++) {
                String key2Str = String.format("%016d", Long.parseLong(Integer.toBinaryString(key2)));
                saes.setKey(key2Str);
                String intermediatePlaintext = saes.singleDecrypt(ciphertext);

                // 查找是否有匹配的中间值
                for (int i = 0; i < encryptArray.length; i++) {
                    if (encryptArray[i][0].equals(intermediatePlaintext)) {
                        currentPossibleKeys.add(new String[]{encryptArray[i][1], key2Str});
                    }
                }
            }

            if (possibleKeysSet == null) {
                possibleKeysSet = currentPossibleKeys;
            } else {
                possibleKeysSet.retainAll(currentPossibleKeys);  // 取交集
            }
        }

        return possibleKeysSet;
    }

    public static String doubleEncrypt(String plaintext, String key1, String key2) {
        test01 saes = new test01(key1);  // 创建SAES实例并设置密钥
        String intermediateCiphertext = saes.singleEncrypt(plaintext); // 第一次加密
        return saes.singleEncrypt(intermediateCiphertext); // 第二次加密
    }

    public static String tripleEncrypt(String plaintext, String key1, String key2, String key3) {
        test01 saes = new test01(key1);  // 创建SAES实例并设置密钥
        String intermediateCiphertext1 = saes.singleEncrypt(plaintext); // 第一次加密
        String intermediateCiphertext2 = saes.singleEncrypt(intermediateCiphertext1); // 第二次加密
        return saes.singleEncrypt(intermediateCiphertext2); // 第三次加密
    }

    public static List<String[]> doubleEncryptAttack(String[][] pairs) {
        List<String[]> possibleKeysSet = new ArrayList<>();

        for (String[] pair : pairs) {
            String plaintext = pair[0];
            String ciphertext = pair[1];

            // Step 1: 使用所有可能的 key1 和 key2
            for (int key1 = 0; key1 < Math.pow(2, 16); key1++) {
                for (int key2 = 0; key2 < Math.pow(2, 16); key2++) {
                    String key1Str = String.format("%016d", Long.parseLong(Integer.toBinaryString(key1)));
                    String key2Str = String.format("%016d", Long.parseLong(Integer.toBinaryString(key2)));

                    // 使用双重加密
                    String intermediateCiphertext = doubleEncrypt(plaintext, key1Str, key2Str);

                    // Step 2: 解密到中间值
                    String decryptedIntermediate = doubleEncrypt(ciphertext, key1Str, key2Str);

                    // 检查中间值
                    if (intermediateCiphertext.equals(decryptedIntermediate)) {
                        possibleKeysSet.add(new String[]{key1Str, key2Str});
                    }
                }
            }
        }

        return possibleKeysSet;
    }

    public static List<String[]> tripleEncryptAttack(String[][] pairs) {
        List<String[]> possibleKeysSet = new ArrayList<>();

        for (String[] pair : pairs) {
            String plaintext = pair[0];
            String ciphertext = pair[1];

            // Step 1: 使用所有可能的 key1、key2 和 key3
            for (int key1 = 0; key1 < Math.pow(2, 16); key1++) {
                for (int key2 = 0; key2 < Math.pow(2, 16); key2++) {
                    for (int key3 = 0; key3 < Math.pow(2, 16); key3++) {
                        String key1Str = String.format("%016d", Long.parseLong(Integer.toBinaryString(key1)));
                        String key2Str = String.format("%016d", Long.parseLong(Integer.toBinaryString(key2)));
                        String key3Str = String.format("%016d", Long.parseLong(Integer.toBinaryString(key3)));

                        // 使用三重加密
                        String intermediateCiphertext = tripleEncrypt(plaintext, key1Str, key2Str, key3Str);

                        // Step 2: 解密到中间值
                        String decryptedIntermediate = tripleEncrypt(ciphertext, key1Str, key2Str, key3Str);

                        // 检查中间值
                        if (intermediateCiphertext.equals(decryptedIntermediate)) {
                            possibleKeysSet.add(new String[]{key1Str, key2Str, key3Str});
                        }
                    }
                }
            }
        }

        return possibleKeysSet;
    }

    public static void main(String[] args) {
        // 1. 只使用一对明密文对
        System.out.println("1. 只使用一对明密文对");
        String[][] pairs1 = {{"1101011100101100", "0110110100111010"}};
        List<String[]> potentialKeys1 = meetInTheMiddleAttack(pairs1);
        System.out.println("Number of potential key pairs: " + potentialKeys1.size());

        System.out.println("--------------------");

        // 2. 使用两对明密文对
        System.out.println("2. 使用两对明密文对");
        String[][] pairs2 = {
                {"1101011100101100", "0110110100111010"},
                {"1111111100000000", "0111011011110111"}
        };
        List<String[]> potentialKeys2 = meetInTheMiddleAttack(pairs2);
        System.out.println("Number of potential key pairs: " + potentialKeys2.size());

        // 打印所有可能的密钥对
        for (String[] keyPair : potentialKeys2) {
            System.out.println("Key1: " + keyPair[0] + ", Key2: " + keyPair[1]);
        }

        // 测试双重加密
        System.out.println("\n--- 测试双重加密 ---");
        String[][] pairs3 = {{"1101011100101100", "0110110100111010"}};
        List<String[]> potentialKeys3 = doubleEncryptAttack(pairs3);
        System.out.println("双重加密潜在密钥对数量: " + potentialKeys3.size());
        for (String[] keyPair : potentialKeys3) {
            System.out.println("Key1: " + keyPair[0] + ", Key2: " + keyPair[1]);
        }

        // 测试三重加密
        System.out.println("\n--- 测试三重加密 ---");
        List<String[]> potentialKeys4 = tripleEncryptAttack(pairs3);
        System.out.println("三重加密潜在密钥对数量: " + potentialKeys4.size());
        for (String[] keyPair : potentialKeys4) {
            System.out.println("Key1: " + keyPair[0] + ", Key2: " + keyPair[1] + ", Key3: " + keyPair[2]);
        }
    }
}
