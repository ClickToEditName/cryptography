package DES;
import java.util.Scanner;

public class breakKey {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String ciphertext = "";
		String knownPlaintext = "";

		// 输入密文
		while (true) {
			System.out.print("请输入密文 (8位二进制): ");
			ciphertext = scanner.nextLine();
			if (isValidBinary(ciphertext)) break;
			System.out.println("输入无效，请输入8位二进制字符串。");
		}

		// 输入已知明文
		while (true) {
			System.out.print("请输入已知明文 (8位二进制): ");
			knownPlaintext = scanner.nextLine();
			if (isValidBinary(knownPlaintext)) break;
			System.out.println("输入无效，请输入8位二进制字符串。");
		}

		// 暴力破解
		for (int i = 0; i < 1024; i++) {
			String key = String.format("%10s", Integer.toBinaryString(i)).replace(' ', '0');
			String[] keys = Encryptor.generateKeys(key);
			String decryptedText = decryptor.decrypt(ciphertext, keys);
			if (decryptedText.equals(knownPlaintext)) {
				System.out.println("找到密钥: " + key);
				System.out.println("解密结果: " + decryptedText);
				scanner.close();
				return;
			}
		}
		System.out.println("未找到有效密钥。");
		scanner.close();
	}

	private static boolean isValidBinary(String input) {
		return input.length() == 8 && input.matches("[01]+");
	}
}