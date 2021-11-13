package com.group06.inversematrix;

import java.io.File;
import java.util.*;
import javax.print.event.PrintEvent;
import javax.sound.sampled.SourceDataLine;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.QRCodeDetector;
import org.opencv.videoio.VideoCapture;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


public class hill_Cipher {

    static int[][] keyMatrix = new int[3][3];
    static int[][] inverseMatrix = new int[3][3];
    static int[] msgMatrix = new int[3];
    static int[] encryptMatrix = new int[3];
    static int[] decryptMatrix = new int[3];

    static Map<Character, Integer> key = new HashMap<Character, Integer>();
    static Map<Integer, Character> itoc = new HashMap<Integer, Character>();
    static float determinant = 0;
    static char trail = '!';
    static String message;
    static String encryptedMessage = "";
    static String decrypedMessage = "";
    static String data = null;

    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);

        numberToChar();
        charToNumber();
        System.out.println("Select:");
        System.out.println("1.Encryption");
        System.out.println("2.Decryption");
        System.out.println("3.Exit");

        int choice = s.nextInt();

        if (choice == 1) {
            System.out.print("Enter message :: ");
            message = s.nextLine();
            message = s.nextLine();
            encrypt(message);
            System.out.println("Encrypted Message :: " + encryptedMessage);
        } else if (choice == 2) {
        	System.out.println();
        	System.out.println("Select:");
            System.out.println("1.Capture Through Camera");
            System.out.println("2.Through PNG file");
            System.out.println("3.Exit");
            
            int dr = s.nextInt();
            
            if(dr==1) {
            	captureQRCode();
            	message=data;
            }else if(dr==2) {
            	message=readQRCode();
            }
        	
            trail = message.charAt(message.length() - 1);
            message = message.substring(0, message.length() - 1);

            decrypt(message);

            System.out.println("Decrypted message :: " + decrypedMessage);
        }
    }

    static void charToNumber() {
        char a = 'A';
        for (int i = 0; i < 26; i++) {
            key.put(a, i);
            a++;
        }
        a = 'a';
        for (int i = 0; i < 26; i++) {
            key.put(a, i + 26);
            a++;
        }
        a = '0';
        for (int i = 0; i < 10; i++) {
            key.put(a, i + 52);
            a++;
        }
        key.put('+', 62);
        key.put('=', 63);
        key.put(' ', 64);
        key.put('.', 65);
        key.put('?', 66);
        key.put('!', 67);
        key.put('/', 68);
    }

    static void numberToChar() {
        char a = 'A';
        for (int i = 0; i < 26; i++) {
            itoc.put(i, a);
            a++;
        }
        a = 'a';
        for (int i = 0; i < 26; i++) {
            itoc.put(i + 26, a);
            a++;
        }
        a = '0';
        for (int i = 0; i < 10; i++) {
            itoc.put(i + 52, a);
            a++;
        }
        itoc.put(62, '+');
        itoc.put(63, '=');
        itoc.put(64, ' ');
        itoc.put(65, '.');
        itoc.put(66, '?');
        itoc.put(67, '!');
        itoc.put(68, '/');
    }

    static void group(String msg) {
        if (msg.length() % 3 == 1) {
            message += '!';
            message += '!';
            trail = '@';
        } else if (msg.length() % 3 == 2) {
            message += '!';
            trail = '!';
        } else {
            trail = ')';
        }
        System.out.println("Message After :: " + message);
    }

    static void createKeyMatrix() {
        keyMatrix[0][0] = 1;
        keyMatrix[0][1] = 3;
        keyMatrix[0][2] = 3;
        keyMatrix[1][0] = 1;
        keyMatrix[1][1] = 4;
        keyMatrix[1][2] = 3;
        keyMatrix[2][0] = 1;
        keyMatrix[2][1] = 3;
        keyMatrix[2][2] = 4;
        // cout<<"Enter key matrix :: "<<endl;
        // for(int i=0;i<3;i++){
        // for (int j=0;j<3;j++){
        // cin>>keyMatrix[i][j];
        // }
        // }
        // System.out.println("I'm Here too!");

    }

    static void inverse() {
        // finding determinant
        for (int i = 0; i < 3; i++) {
            determinant += (keyMatrix[0][i] * (keyMatrix[1][(i + 1) % 3] * keyMatrix[2][(i + 2) % 3]
                    - keyMatrix[1][(i + 2) % 3] * keyMatrix[2][(i + 1) % 3]));
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                inverseMatrix[i][j] = (int) (((keyMatrix[(j + 1) % 3][(i + 1) % 3]
                        * keyMatrix[(j + 2) % 3][(i + 2) % 3])
                        - (keyMatrix[(j + 1) % 3][(i + 2) % 3] * keyMatrix[(j + 2) % 3][(i + 1) % 3])) / determinant);
            }
        }
    }

    static void createMessageMatrix(Character[] msg, int[] matrix) {
        for (int i = 0; i < 3; i++) {
            matrix[i] = key.get(msg[i]);
        }
    }

    static void createEncryptMatrix(int[][] key, int[] msg) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                encryptMatrix[i] += (keyMatrix[i][j] * msgMatrix[j]);
            }
        }
        for (int i = 0; i < 3; i++) {
            encryptMatrix[i] %= 69;
        }
    }

    static void encrypt(String msg) {
        Character[] tempMsg = new Character[3];
        group(msg);
        createKeyMatrix();
        for (int i = 0; i < message.length(); i += 3) {
            tempMsg[0] = message.charAt(i);
            tempMsg[1] = message.charAt(i + 1);
            tempMsg[2] = message.charAt(i + 2);

            createMessageMatrix(tempMsg, msgMatrix);
            createEncryptMatrix(keyMatrix, msgMatrix);

            for (int j = 0; j < 3; j++) {
                encryptedMessage += itoc.get(encryptMatrix[j]);
            }
            encryptMatrix[0] = 0;
            encryptMatrix[1] = 0;
            encryptMatrix[2] = 0;
        }
        encryptedMessage += trail;
        createQRCode();
    }

    static void createDecryptMatrix(int[][] key, int[] msg) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                decryptMatrix[i] += (inverseMatrix[i][j] * msgMatrix[j]);
            }
            while (decryptMatrix[i] < 0) {
                decryptMatrix[i] += 69;
            }
        }
        for (int i = 0; i < 3; i++) {
            decryptMatrix[i] %= 69;
        }
    }

    static void decrypt(String msg) {
        Character[] tempMsg = new Character[3];
        createKeyMatrix();
        inverse();

        for (int i = 0; i < message.length(); i += 3) {
            tempMsg[0] = message.charAt(i);
            tempMsg[1] = message.charAt(i + 1);
            tempMsg[2] = message.charAt(i + 2);
            createMessageMatrix(tempMsg, msgMatrix);
            createDecryptMatrix(keyMatrix, msgMatrix);
            for (int j = 0; j < 3; j++) {
                decrypedMessage += itoc.get(decryptMatrix[j]);
            }
            decryptMatrix[0] = 0;
            decryptMatrix[1] = 0;
            decryptMatrix[2] = 0;
        }
        if (trail == '!') {
            decrypedMessage = decrypedMessage.substring(0, decrypedMessage.length() - 1);
        } else if (trail == '@') {
            decrypedMessage = decrypedMessage.substring(0, decrypedMessage.length() - 2);
        }
    }

    static void createQRCode() {
    	try {
            String qrCodeData = encryptedMessage;
            String filePath = "C:\\Users\\HP\\eclipse-workspace\\Encryption_Decryption\\resource\\qrCode.png";
            String charset = "UTF-8"; // or "ISO-8859-1"
            Map < EncodeHintType, ErrorCorrectionLevel > hintMap = new HashMap < EncodeHintType, ErrorCorrectionLevel > ();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            BitMatrix matrix = new MultiFormatWriter().encode(
                new String(qrCodeData.getBytes(charset), charset),
                BarcodeFormat.QR_CODE, 200, 200, hintMap);
            MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath
                .lastIndexOf('.') + 1), new File(filePath));
            System.out.println("QR Code image created successfully!");
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    static void captureQRCode() {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat img = Imgcodecs.imread("./resources/qrcode.jpeg");
		VideoCapture capture = new VideoCapture(0);

		String file = "./resources/sanpshot.jpg";

		Imgcodecs imageCodecs = new Imgcodecs();
		Mat matrix = new Mat();
		capture.read(matrix);
		// Saving it again
		imageCodecs.imwrite(file, matrix);

		QRCodeDetector decoder = new QRCodeDetector();
		Mat points = new Mat();
//		String data = null;
		while (capture.isOpened()) {
			capture.read(matrix);
			HighGui.imshow("Detected QR code", matrix);

			data = decoder.detectAndDecode(matrix, points);

			if (!points.empty()) {
//				System.out.println("Decoded data: " + data);

				for (int i = 0; i < points.cols(); i++) {
					Point pt1 = new Point(points.get(0, i));
					Point pt2 = new Point(points.get(0, (i + 1) % 4));
					Imgproc.line(matrix, pt1, pt2, new Scalar(0, 255, 0), 3);
				}

				if (!data.equals("")) {
					HighGui.destroyAllWindows();
					break;
				}

			}

			if (HighGui.waitKey(30) != -1) {
				HighGui.destroyAllWindows();
				break;
			}
		}	
		System.out.println("Decoded data: " + data);
    }
    
    static String readQRCode() {
    	try {
            String filePath = "C:\\Users\\HP\\eclipse-workspace\\Encryption_Decryption\\resource\\qrCode.png";
            String charset = "UTF-8";
            Map < EncodeHintType, ErrorCorrectionLevel > hintMap = new HashMap < EncodeHintType, ErrorCorrectionLevel > ();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            System.out.println("Data read from QR Code: " + readQRCode(filePath, charset, hintMap));
           return readQRCode(filePath, charset, hintMap);
        } catch (Exception e) {
            // TODO: handle exception
        }
    	return null;
    }
    
    public static String readQRCode(String filePath, String charset, Map hintMap)
    	    throws FileNotFoundException, IOException, NotFoundException {
    	        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
    	            new BufferedImageLuminanceSource(
    	                ImageIO.read(new FileInputStream(filePath)))));
    	        Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
    	        return qrCodeResult.getText();
    	    }
    
}