/**
 * 
 * Student Name: Megan Palczak
 * Student Number: 300301072
 * CSI4118 Assignment 2 - Client
 * Date: October 27, 2025
 *
**/

import java.io.*;
import java.net.*;
import java.util.*;

public class myClient {
    public static final String CLASS_NAME = "CSI4118";
    public static final String STUDENT_NUMBER = "300301072";

    // Default Caesar shift
    private static int caesarShift = 3;
	
	// Store relevant connection information in class (also automatically closes sockets and streams)
    public static class ConnectionResult implements AutoCloseable {
        Socket socket;
        BufferedReader reader;
        BufferedWriter writer;

        public ConnectionResult(Socket socket, BufferedReader reader, BufferedWriter writer) {
            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
        }

        @Override
        public void close() throws IOException {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        }
    }

    public static void main(String[] args) {
        String serverAddress = null;
        int portNumber = 0;

        // ----- Validate command line arguments -----
        if (args.length != 2) {
            System.err.println("Usage: java myClient <server address> <port number>");
            System.exit(1);
        }

        // Validate IPv4 address
        serverAddress = args[0];
        String[] ipFields = serverAddress.split("\\.");
        if (ipFields.length != 4) {
            System.err.println("Invalid IPv4 server address");
            System.exit(1);
        }
        for (String field : ipFields) {
            try {
                int num = Integer.parseInt(field);
                if (num < 0 || num > 255) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.err.println("Invalid IPv4 server address");
                System.exit(1);
            }
        }

        // Validate port
        try {
            portNumber = Integer.parseInt(args[1]);
            if (portNumber < 0 || portNumber > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.err.println("Error: Port number must be an integer in [0-65535]");
            System.exit(1);
        }
		// ------------------------------------------------------------
		
        ConnectionResult connection = null;
        try {
            connection = attemptConnection(serverAddress, portNumber);
            System.out.println("Session has been established.");

            Scanner scanner = new Scanner(System.in);
            String input;
			
			// Command loop (requirement 10)
            while (true) {
                System.out.print("> ");
                input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Empty command. Try again.");
                    continue;
                }

                String response = processCommand(input, connection.reader, connection.writer);
                System.out.println("Server: " + response);
				
				// Exit only after server sends "BYE" (requirement 4)
                if (response.equals("BYE")) {
                    break;
                }
            }

            System.out.println("Session is terminated.");

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    
    // Parses and processes the full command string entered by the user (requirement 3).
    public static String processCommand(String input, BufferedReader reader, BufferedWriter writer) throws IOException {
        String[] parts = input.trim().split("\\s+");

        // Must start with CSI4118
        if (parts.length < 3 || !parts[0].equals(CLASS_NAME)) {
            return "ERROR: Command must start with CSI4118";
        }

        // Must have typeX
        if (!parts[1].matches("type[0-4]")) {
            return "ERROR: Second field must be type0 to type4";
        }

        // Must include correct student number
        if (!parts[2].equals(STUDENT_NUMBER)) {
            return "ERROR: Student number must be " + STUDENT_NUMBER;
        }

        int commandType = Character.getNumericValue(parts[1].charAt(4)); // typeX -> X

        String param = (parts.length > 3) ? parts[3] : null;
        String commandToSend = input; // Use exactly what user typed (except validation)

        // Special handling for type3: encode message
        if (commandType == 3 && param != null) {
            String encoded = caesarEncode(param, caesarShift);
            commandToSend = CLASS_NAME + " type3 " + STUDENT_NUMBER + " " + encoded;
        }

        // Special handling for type0: update shift
        if (commandType == 0 && param != null) {
            try {
                int shift = Integer.parseInt(param);
                if (shift >= 1 && shift <= 25) {
                    caesarShift = shift;
                } else {
                    return "ERROR: Shift must be 1-25";
                }
            } catch (NumberFormatException e) {
                return "ERROR: Shift must be integer 1-25";
            }
        }

        // Send command
        writer.write(commandToSend);
        writer.newLine();
        writer.flush();

        // Read response
        String response = reader.readLine();
        return (response != null) ? response : "No response";
    }


    // Caesar cipher encoding (only letters, preserves case)
    public static String caesarEncode(String text, int shift) {
        StringBuilder result = new StringBuilder();
        shift = shift % 26;

        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append((char) ((c - 'A' + shift) % 26 + 'A'));
            } else if (Character.isLowerCase(c)) {
                result.append((char) ((c - 'a' + shift) % 26 + 'a'));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // Connect to server
    public static ConnectionResult attemptConnection(String serverAddress, int portNumber) throws IOException {
        Socket socket = new Socket(serverAddress, portNumber);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        return new ConnectionResult(socket, reader, writer);
    }
}