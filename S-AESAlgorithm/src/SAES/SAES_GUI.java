package SAES;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SAES_GUI extends JFrame {

	private JTextField inputTextField;
	private JTextField keyTextField;
	private JTextField outputTextField;
	private JComboBox<String> modeComboBox;
	private test01 saes;

	public SAES_GUI() {
		setTitle("S-AES 加解密");
		setSize(400, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout());

		inputTextField = new JTextField(20);
		keyTextField = new JTextField(16);
		outputTextField = new JTextField(20);
		outputTextField.setEditable(false);

		modeComboBox = new JComboBox<>(new String[]{"二进制", "ASCII"});

		JButton encryptButton = new JButton("加密");
		JButton decryptButton = new JButton("解密");

		encryptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String plaintext = inputTextField.getText();
				String key = keyTextField.getText();
				saes = new test01(key);

				String mode = (String) modeComboBox.getSelectedItem();
				String ciphertext;

				if ("ASCII".equals(mode)) {
					ciphertext = saes.encryptASCII(plaintext);
				} else {
					ciphertext = saes.encrypt(plaintext); // 使用现有的二进制加密方法
				}

				outputTextField.setText(ciphertext);
			}
		});

		decryptButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ciphertext = inputTextField.getText();
				String key = keyTextField.getText();
				saes = new test01(key);

				String mode = (String) modeComboBox.getSelectedItem();
				String decryptedText;

				if ("ASCII".equals(mode)) {
					decryptedText = saes.decryptASCII(ciphertext);
				} else {
					decryptedText = saes.decrypt(ciphertext); // 使用现有的二进制解密方法
				}

				outputTextField.setText(decryptedText);
			}
		});

		add(new JLabel("输入:"));
		add(inputTextField);
		add(new JLabel("密钥:"));
		add(keyTextField);
		add(modeComboBox);
		add(encryptButton);
		add(decryptButton);
		add(new JLabel("输出:"));
		add(outputTextField);

		setVisible(true);
	}

	public static void main(String[] args) {
		new SAES_GUI();
	}
}
