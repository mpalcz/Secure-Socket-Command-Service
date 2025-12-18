/**
 * 
 * Student Name: Megan Palczak
 * Student Number: 300301072
 * CSI4118 Assignment 2 - Server
 * Date: October 27, 2025
 *
**/

import java.io.*;
import java.net.*;
import java.util.*;

public class myServer {
	// Constants (ensuring validity of commands or for missing information)
    public static final String CLASS_NAME = "CSI4118";
    public static final int DEFAULT_SHIFT = 3;

    // Log file parameters
    private static final String LOG_FILE = "server_log.txt";
    private static PrintWriter logWriter;

    public static void main(String[] args) {
        int portNumber = 0;

        // ----- Validate port from command line -----
        if (args.length != 1) {
            System.err.println("Usage: java myServer <port number>");
            System.exit(1);
        }

        try {
            portNumber = Integer.parseInt(args[0]);
            if (portNumber < 0 || portNumber > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Port number must be an integer in [0-65535]");
            System.exit(1);
        }
        // ------------------------------------------------------------

        // Initialize log file to store client interactions
        try {
            logWriter = new PrintWriter(new FileWriter(LOG_FILE, true)); // append mode
            log("Server started on port " + portNumber);
        } catch (IOException e) {
            System.err.println("Could not open log file: " + e.getMessage());
            System.exit(1);
        }

        ServerSocket serverSocket = null;
        try {
			// Creating server socket
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server is running on port " + portNumber + "...");

			// Listening for clients
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    String clientIP = clientSocket.getInetAddress().getHostAddress();

                    // Requirement 8: Reject localhost (127.0.0.1 or ::1)
                    if (clientIP.equals("127.0.0.1") || clientIP.equals("0:0:0:0:0:0:0:1")) {
                        System.out.println("Rejected connection from localhost: " + clientIP);
                        log("REJECTED localhost connection from " + clientIP);
                        clientSocket.close();
                        continue;
                    }

                    System.out.println("Accepted connection from: " + clientIP);
                    log("CONNECTED from " + clientIP);

                    // Handle client in a separate thread (so server keeps accepting clients)
                    new Thread(new ClientHandler(clientSocket, clientIP)).start();

                } catch (IOException e) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Could not start server on port " + portNumber);
            e.printStackTrace();
        } finally {
			// Ensure all relevant sockets are closed
            if (serverSocket != null && !serverSocket.isClosed()) {
                try { serverSocket.close(); } catch (IOException e) {}
            }
            if (logWriter != null) logWriter.close();
        }
    }

    // Thread-safe log
    public static synchronized void log(String message) {
        String timestamp = new Date().toString();
        String logEntry = "[" + timestamp + "] " + message;
        System.out.println(logEntry);
        logWriter.println(logEntry);
        logWriter.flush();
    }
}

// Handles one client session
class ClientHandler implements Runnable {
    private final Socket socket;
    private final String clientIP;
    private BufferedReader reader;
    private BufferedWriter writer;
    private int caesarShift = myServer.DEFAULT_SHIFT; // default
    private StringBuilder messageBuffer = new StringBuilder(); // stores decoded messages (requirement 9)

    public ClientHandler(Socket socket, String clientIP) {
        this.socket = socket;
        this.clientIP = clientIP;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String inputLine;
			// Reads commands until "BYE"
            while ((inputLine = reader.readLine()) != null) {
                String response = processCommand(inputLine.trim());
                writer.write(response);
                writer.newLine();
                writer.flush();

                myServer.log("CLIENT " + clientIP + " -> " + inputLine + " | Response: " + response);

                if (response.equals("BYE")) { // client requests termination
                    break;
                }
            }

        } catch (IOException e) {
            myServer.log("Client " + clientIP + " disconnected unexpectedly: " + e.getMessage());
        } finally {
			// Ensure relevant sockets are clsed
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {}
            myServer.log("DISCONNECTED from " + clientIP);
            if (messageBuffer.length() > 0) {
                myServer.log("Stored message for " + clientIP + ": " + messageBuffer.toString());
            }
        }
    }

	// Validates commands and dispatches results
    private String processCommand(String command) {
        String[] parts = command.split("\\s+");

        // Must have at least 3 parts: CSI4118 typeX studentNumber
        if (parts.length < 3 || !parts[0].equals("CSI4118")) {
            return "ERROR: Invalid command format";
        }

        if (!parts[1].matches("type[0-4]")) {
            return "ERROR: Command must be type0 to type4";
        }

        int cmd = Character.getNumericValue(parts[1].charAt(4));
        String studentNumber = parts[2];

        // Log the request
        myServer.log("REQUEST from " + clientIP + " | Student: " + studentNumber + " | Command: type" + cmd);

        switch (cmd) {
            case 0: // Set password
                if (parts.length != 4) {
                    return "ERROR: type0 requires shift value";
                }
                try {
                    int shift = Integer.parseInt(parts[3]);
                    if (shift < 1 || shift > 25) {
                        return "ERROR: Shift must be 1-25";
                    }
                    caesarShift = shift;
                    return "OK";
                } catch (NumberFormatException e) {
                    return "ERROR: Shift must be integer";
                }

            case 1: // Who am I?
                if (parts.length != 3) return "ERROR: type1 takes no parameters";
                return clientIP;

            case 2: // Count to 10
                if (parts.length != 3) return "ERROR: type2 takes no parameters";
                return "1 2 3 4 5 6 7 8 9 10";

            case 3: // Send Message (decode and store)
                if (parts.length != 4) return "ERROR: type3 requires a message";
                String encodedMsg = parts[3];
                String decoded = caesarDecode(encodedMsg, caesarShift);
                messageBuffer.append(decoded).append(" ");
                return "GOT IT";

            case 4: // Bye
                if (parts.length != 3) return "ERROR: type4 takes no parameters";
                return "BYE";

            default:
                return "ERROR: Unknown command";
        }
    }

    // Caesar decode (reverse of encode)
    private String caesarDecode(String text, int shift) {
        return caesarEncode(text, (26 - shift) % 26); // decode = encode with negative shift
    }

    // Caesar encode (same as client, for consistency)
    private String caesarEncode(String text, int shift) {
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
}