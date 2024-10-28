package SAES;

import java.util.Arrays;
import java.util.Random;

public class CBCMode {

	private static String generateIV() {
		Random random = new Random();
		StringBuilder iv = new StringBuilder();
		for (int i = 0; i < 16; i++) {
			iv.append(random.nextBoolean() ? '1' : '0');
		}
		return iv.toString();
	}

	private static String xor(String a, String b) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < a.length(); i++) {
			result.append(a.charAt(i) == b.charAt(i) ? '0' : '1');
		}
		return result.toString();
	}

	public static String[] cbcEncrypt(String plaintext, String key) {
		test01 saes = new test01(key);  // 创建SAES实例并设置密钥
		String iv = generateIV();  // 生成初始向量
		System.out.println("Generated IV: " + iv);

		// 将明文分为16位的块
		int length = plaintext.length();
		int paddingLength = 16 - (length % 16);
		StringBuilder paddedPlaintext = new StringBuilder(plaintext);
		for (int i = 0; i < paddingLength; i++) {
			paddedPlaintext.append('0');  // 使用 '0' 填充
		}

		String[] ciphertextBlocks = new String[paddedPlaintext.length() / 16];
		String previousCiphertext = iv;

		// CBC 加密
		for (int i = 0; i < paddedPlaintext.length(); i += 16) {
			String block = paddedPlaintext.substring(i, i + 16);
			String xorResult = xor(block, previousCiphertext);  // 与前一个密文块进行异或
			String ciphertext = saes.singleEncrypt(xorResult);  // 加密
			ciphertextBlocks[i / 16] = ciphertext;  // 保存密文块
			previousCiphertext = ciphertext;  // 更新上一个密文块
		}

		return new String[]{iv, Arrays.toString(ciphertextBlocks)};
	}

	public static String cbcDecrypt(String[] ciphertextBlocks, String key) {
		test01 saes = new test01(key);  // 创建SAES实例并设置密钥
		String iv = ciphertextBlocks[0];  // 第一个块是IV
		StringBuilder decryptedPlaintext = new StringBuilder();

		// CBC 解密
		for (int i = 1; i < ciphertextBlocks.length; i++) {
			String ciphertext = ciphertextBlocks[i];
			String decryptedBlock = saes.singleDecrypt(ciphertext);  // 解密密文块
			String xorResult = xor(decryptedBlock, iv);  // 与IV或前一个密文块进行异或
			decryptedPlaintext.append(xorResult);  // 追加到解密的明文
			iv = ciphertext;  // 更新IV为当前密文块
		}

		return decryptedPlaintext.toString().replaceAll("0*$", "");  // 去除填充的零
	}

	public static void main(String[] args) {
		String key = "0000000000000001";  // 示例密钥
		String plaintext = "11010111001011001101011100101100";  // 示例明文，注意需为16的倍数

		// CBC加密
		System.out.println("--- CBC 加密 ---");
		String[] encryptedData = cbcEncrypt(plaintext, key);
		String iv = encryptedData[0];
		String[] ciphertextBlocks = encryptedData[1].replaceAll("[\\[\\]\\s]", "").split(","); // 解析密文块
		System.out.println("IV: " + iv);
		System.out.println("Ciphertext: " + Arrays.toString(ciphertextBlocks));

		// CBC解密
		System.out.println("--- CBC 解密 ---");
		String decryptedPlaintext = cbcDecrypt(encryptedData, key);
		System.out.println("Decrypted Plaintext: " + decryptedPlaintext);
	}
}
