package DES;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends JFrame {
	private JTextField inputField;
	private JTextField keyField;
	private JTextArea outputArea;
	private JButton processButton;
	private JComboBox<String> modeSelector;
	private JComboBox<String> operationSelector;

	public GUI() {
		setTitle("SDES 加密/解密");
		setSize(400, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new GridLayout(8, 1));

		// 操作选择面板
		JPanel operationPanel = new JPanel();
		operationPanel.add(new JLabel("选择操作:"));
		operationSelector = new JComboBox<>(new String[]{"加密", "解密"});
		operationPanel.add(operationSelector);
		add(operationPanel);

		// 模式选择面板
		JPanel modePanel = new JPanel();
		modePanel.add(new JLabel("选择模式:"));
		modeSelector = new JComboBox<>(new String[]{"二进制", "ASCII"});
		modePanel.add(modeSelector);
		add(modePanel);

		// 输入面板
		JPanel inputPanel = new JPanel();
		inputPanel.add(new JLabel("输入:"));
		inputField = new JTextField(10);
		inputPanel.add(inputField);
		add(inputPanel);

		// 密钥面板
		JPanel keyPanel = new JPanel();
		keyPanel.add(new JLabel("密钥 (10位二进制):"));
		keyField = new JTextField(10);
		keyPanel.add(keyField);
		add(keyPanel);

		// 处理按钮
		processButton = new JButton("处理");
		processButton.setPreferredSize(new Dimension(100, 30));
		processButton.addActionListener(new ProcessAction());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(processButton);
		add(buttonPanel);

		// 输出区域
		outputArea = new JTextArea();
		outputArea.setEditable(false);
		add(new JScrollPane(outputArea));

		// 模式选择监听
		modeSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateInputField();
			}
		});

		setVisible(true);
	}

	private void updateInputField() {
		String selectedMode = (String) modeSelector.getSelectedItem();
		if ("二进制".equals(selectedMode)) {
			inputField.setToolTipText("输入 (8位二进制)");
			inputField.setText(""); // 清空输入框
		} else {
			inputField.setToolTipText("输入 (ASCII 字符)");
			inputField.setText(""); // 清空输入框
		}
		outputArea.setText(""); // 清空输出区域
	}

	private class ProcessAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String input = inputField.getText();
			String key = keyField.getText();
			String selectedMode = (String) modeSelector.getSelectedItem();
			String selectedOperation = (String) operationSelector.getSelectedItem();

			if (selectedMode.equals("二进制")) {
				if (validateBinaryInput(input, key)) {
					String[] keys = Encryptor_ASC.generateKeys(key);
					if (selectedOperation.equals("加密")) {
						String ciphertext = Encryptor_ASC.encrypt(input, keys);
						outputArea.setText("密文: " + ciphertext);
					} else {
						String decryptedText = decryptor.decrypt(input, keys); // 使用二进制解密
						outputArea.setText("明文: " + decryptedText);
					}
				} else {
					outputArea.setText("输入格式错误。请确保输入为8位二进制，密钥为10位二进制。");
				}
			} else {
				if (validateAsciiInput(input, key)) {
					String[] keys = Encryptor_ASC.generateKeys(key);
					StringBuilder finalCiphertext = new StringBuilder();

					// 加密过程
					for (char c : input.toCharArray()) {
						String binaryChar = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
						String ciphertext = Encryptor_ASC.encrypt(binaryChar, keys);
						finalCiphertext.append(ciphertext);
					}

					// 输出密文
					StringBuilder escapedOutput = new StringBuilder();
					for (int i = 0; i < finalCiphertext.length(); i += 8) {
						String byteString = finalCiphertext.substring(i, Math.min(i + 8, finalCiphertext.length()));
						int charCode = Integer.parseInt(byteString, 2);
						escapedOutput.append("\\u").append(String.format("%04x", charCode));
					}

					if (selectedOperation.equals("加密")) {
						outputArea.setText("密文: " + escapedOutput.toString());
					} else {
						// 解密过程
						String binaryInput = fromMixedASCII(input); // 将转义字符转换为二进制
						StringBuilder decryptedOutput = new StringBuilder();
						for (int i = 0; i < binaryInput.length(); i += 8) {
							String byteString = binaryInput.substring(i, Math.min(i + 8, binaryInput.length()));
							String decryptedText = decryptor.decrypt(byteString, keys); // 使用二进制解密
							decryptedOutput.append((char) Integer.parseInt(decryptedText, 2));
						}
						outputArea.setText("明文: " + decryptedOutput.toString());
					}
				} else {
					outputArea.setText("输入格式错误。请确保密钥为10位二进制。");
				}
			}
		}

		private boolean validateBinaryInput(String input, String key) {
			return input.matches("[01]{8}") && key.matches("[01]{10}");
		}

		private boolean validateAsciiInput(String input, String key) {
			return key.matches("[01]{10}"); // 只验证密钥
		}

		private String fromMixedASCII(String mixed) {
			StringBuilder binary = new StringBuilder();
			for (int i = 0; i < mixed.length(); i++) {
				char c = mixed.charAt(i);
				if (c == '\\' && i + 1 < mixed.length() && mixed.charAt(i + 1) == 'u') {
					String hex = mixed.substring(i + 2, i + 6);
					int charCode = Integer.parseInt(hex, 16);
					String binaryChar = String.format("%8s", Integer.toBinaryString(charCode)).replace(' ', '0');
					binary.append(binaryChar);
					i += 5; // Skip the escaped character
				} else {
					String binaryChar = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
					binary.append(binaryChar);
				}
			}
			return binary.toString();
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(GUI::new);
	}
}