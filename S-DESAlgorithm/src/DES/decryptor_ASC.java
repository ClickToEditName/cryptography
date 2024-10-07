package DES;
import java.util.Scanner;

public class decryptor_ASC {
    
    // Initial Permutation (IP)
    private static final int[] IP = { 2, 6, 3, 1, 4, 8, 5, 7 };

    // Inverse Initial Permutation (IP^-1)
    private static final int[] IP_INV = { 4, 1, 3, 5, 7, 2, 8, 6 };

    // Expansion/Permutation (E/P)
    private static final int[] EP = { 4, 1, 2, 3, 2, 3, 4, 1 };

    // P4 permutation
    private static final int[] P4 = { 2, 4, 3, 1 };

    // P10 permutation
    private static final int[] P10 = { 3, 5, 2, 7, 4, 10, 1, 9, 8, 6 };

    // P8 permutation
    private static final int[] P8 = { 6, 3, 7, 4, 8, 5, 10, 9 };

    // S-boxes
    private static final int[][][] SBOX = {
        {
            { 1, 0, 3, 2 },
            { 3, 2, 1, 0 },
            { 0, 2, 1, 3 },
            { 3, 1, 0, 2 }
        },
        {
            { 0, 1, 2, 3 },
            { 2, 3, 1, 0 },
            { 3, 0, 1, 2 },
            { 2, 1, 0, 3 }
        }
    };

    public static String fromMixedASCII(String mixed) {
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

    public static String toStringASCII(String binary) {
        StringBuilder ascii = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 8) {
            String byteString = binary.substring(i, Math.min(i + 8, binary.length()));
            int charCode = Integer.parseInt(byteString, 2);
            // 可打印字符直接添加，不可打印字符转义
            if (charCode >= 32 && charCode <= 126) {
                ascii.append((char) charCode);
            } else {
                ascii.append("\\u").append(String.format("%04x", charCode));
            }
        }
        return ascii.toString();
    }

    public static String decrypt(String ciphertext, String[] keys) {
        String permuted = permute(ciphertext, IP);
        // System.out.println("Initial Permutation (IP): " + permuted);
        
        String left = permuted.substring(0, 4);
        String right = permuted.substring(4);

        // Round 1: Using k2
        // System.out.println("Left: " + left + ", Right: " + right);
        String result = fk(left, right, keys[1]);
        left = result.substring(0, 4);
        right = result.substring(4);
        // System.out.println("After Round 1 (using k2): Left: " + left + ", Right: " + right);

        // Swap
        String temp = left;
        left = right;
        right = temp;

        // Round 2: Using k1
        result = fk(left, right, keys[0]);
        // System.out.println("After Round 2 (using k1): Left: " + result.substring(0, 4) + ", Right: " + result.substring(4));

        String finalResult = permute(result, IP_INV);
        // System.out.println("Final Permutation (IP^-1): " + finalResult);
        return finalResult;
    }

    private static String fk(String left, String right, String key) {
        String epRight = permute(right, EP);
        // System.out.println("E/P: " + epRight);
        
        String xorResult = xor(epRight, key);
        // System.out.println("XOR with key: " + xorResult);
        
        String sboxOutput = sbox(xorResult.substring(0, 4), xorResult.substring(4));
        // System.out.println("S-Box output: " + sboxOutput);
        
        String p4Output = permute(sboxOutput, P4);
        // System.out.println("P4 Output: " + p4Output);
        
        String xorLeft = xor(left, p4Output);
        return xorLeft + right;
    }

    private static String sbox(String left, String right) {
        int row1 = Integer.parseInt("" + left.charAt(0) + left.charAt(3), 2);
        int col1 = Integer.parseInt("" + left.charAt(1) + left.charAt(2), 2);
        int row2 = Integer.parseInt("" + right.charAt(0) + right.charAt(3), 2);
        int col2 = Integer.parseInt("" + right.charAt(1) + right.charAt(2), 2);

        String leftSboxOutput = String.format("%2s", Integer.toBinaryString(SBOX[0][row1][col1])).replace(' ', '0');
        String rightSboxOutput = String.format("%2s", Integer.toBinaryString(SBOX[1][row2][col2])).replace(' ', '0');
        return leftSboxOutput + rightSboxOutput;
    }

    private static String[] generateKeys(String key) {
        // P10 permutation
        String p10Key = permute(key, P10);

        // Left and Right halves
        String left = p10Key.substring(0, 5);
        String right = p10Key.substring(5);

        // Shift left 1
        left = shiftLeft(left, 1);
        right = shiftLeft(right, 1);
        String k1 = permute(left + right, P8);

        // Shift left 2
        left = shiftLeft(left, 2);
        right = shiftLeft(right, 2);
        String k2 = permute(left + right, P8);

        return new String[] { k1, k2 };
    }

    private static String shiftLeft(String bits, int count) {
        return bits.substring(count) + bits.substring(0, count);
    }

    private static String xor(String a, String b) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            result.append(a.charAt(i) == b.charAt(i) ? '0' : '1');
        }
        return result.toString();
    }

    private static String permute(String input, int[] permutation) {
        StringBuilder result = new StringBuilder();
        for (int i : permutation) {
            result.append(input.charAt(i-1));
        }
        return result.toString();
    }
}
