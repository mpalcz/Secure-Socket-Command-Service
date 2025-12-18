Project Overview

This project implements a Java-based client/server application using TCP sockets. The system follows a predefined command protocol that allows a client to interact with a server 
by sending structured command strings. The server parses and executes supported commands, returns appropriate responses to the client, and maintains logs of all client interactions.

The project demonstrates fundamental concepts of network programming, client/server architectures, message parsing, basic cryptography (Caesar cipher), and session-based data handling.

System Architecture

- Client (`myClient`)
  - Command-line application.
  - Prompts the user for an operation number and any required parameters.
  - Sends formatted command strings to the server.
  - Displays responses received from the server.
  - Terminates only after issuing the `Bye` command and receiving confirmation.

- Server (`myServer`)
  - TCP socket server that listens for client connections.
  - Processes one client at a time while remaining active for future connections.
  - Executes valid commands and ignores unsupported ones.
  - Logs all operations with client IP addresses and student numbers.
  - Decodes and stores encrypted messages for the duration of a client session.

Command Protocol

All commands sent from the client to the server must follow this format:
  CSI4118 <commandType> <studentNumber> [parameters]

Where:
- `<commandType>` is `typeI`, with `I` representing the command number.
- `<studentNumber>` is a 7-digit University of Ottawa student identifier.
- `[parameters]` is an optional argument required for certain commands.

Supported Commands

| Command No. | Name              | Parameters | Server Behavior |
|------------|-------------------|------------|-----------------|
| 0 | Set Password | Integer string in range [1–25] (default: 3) | Sets the Caesar cipher shift value and replies `OK` |
| 1 | Who Am I? | None | Replies with the client’s IP address |
| 2 | Count to 10 | None | Replies with numbers 1 to 10 separated by spaces |
| 3 | Send Message | Caesar-encoded string | Decodes and stores the message, then replies `GOT IT` |
| 4 | Bye | None | Replies `BYE` and closes the client connection |

Caesar Cipher Handling

The server maintains a Caesar cipher password (shift value) per client session.  
- Messages received using the Send Message command are decoded using the current password.
- Decoded messages are appended and stored for the duration of the client connection.
- The password can be set or reset using the Set Password command.

Functional Requirements

- Java sockets must be used (Java 1.4 or higher).
- The programs must be named:
  - `myClient` (client)
  - `myServer` (server)
- The server must ignore unsupported commands.
- The server must continue running after a client disconnects.
- The client must disconnect only after a successful `Bye` command.
- Server address and port must be provided via command-line arguments (no hardcoding).
- The server must reject connections originating from `localhost`.
- The server must maintain a log file recording:
  - Client IP address
  - Requested operation
  - Student number associated with each request

Project Structure
.
├── myClient.java
├── myServer.java
├── Instructions for Users.txt
├── Session_Results/
│   └── myClient_terminal.png
│   └── myServer_terminal.png
│   └── server_log.txt
└── README.md

Usage Instructions

A separate file titled `Instructions for Users` is provided with this project.  
Users should consult this file for:
- Compilation instructions
- How to run the client and server
- Required command-line arguments
- Testing procedures
- An example of expected program output

Additionally, sample execution output is stored in the `Session results` subfolder for reference.
