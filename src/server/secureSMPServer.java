package server;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.HashMap;




// This is the server class, it has ssl properties to enable the use of SSL by creating an SSL socket. It also
// creates a thread so that clients can connect to it

public class secureSMPServer {

    private static HashMap<Integer, String> messageStorage = new HashMap<>();
    private static int nextMessageNumber = 1;

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.keyStore", "C:/Users/The Oracle/Desktop/DSC24/DC/smpserver.p12");// If error coms up running make sure this directory is correct, at the moment it's set to my machine
        System.setProperty("javax.net.ssl.keyStorePassword", "admin12345");

        try {
            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            int port = 8989;
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
            System.out.println("Server is running and running on port: " + port);

            while (true) {
                try {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    System.out.println("A client connected to the server");

                    Thread clientThread = new Thread(new ClientHandler(clientSocket));
                    clientThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private SSLSocket clientSocket;

        public ClientHandler(SSLSocket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                System.out.println("SSL connection has established with client.");

                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    if ("logout".equals(clientMessage.trim())) {
                        System.out.println("Client has logged off. Bye bye client...");
                        break;
                    }
                    System.out.println("Received message from client: " + clientMessage);
                    boolean isSuccess = processMessage(clientMessage, out);
                    System.out.println("Message Processed, Status Code: " + (isSuccess ? "200" : "400") + ", Command Used: " + getClientCommand(clientMessage));
                }
            } catch (SocketException e) {
                System.out.println("Connection was abandoned by user.");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // This method processes the users messages. If the message is a request to retrieve
    // a specific message or all messages, it retrieves and sends the requested messages. Otherwise it'll store the
    // message and sends a confirmation back to the client.
    private static boolean processMessage(String clientMessage, PrintWriter out) {
        if (clientMessage.startsWith("retrieve_message")) {
            int messageNumber = Integer.parseInt(clientMessage.split(" ")[1]);
            String message = messageStorage.getOrDefault(messageNumber, "Message not found.");
            out.println(message);
            return !message.equals("Message not found.");
        } else if (clientMessage.equals("retrieve_all_messages")) {
            retrieveAllMessages(out);
            return true; // Assuming always succeeds
        } else {
            messageStorage.put(nextMessageNumber, clientMessage);
            out.println("Message Sent! Your message number is: " + nextMessageNumber);
            nextMessageNumber++;
            return true; // Success
        }
    }


    // This method extracts and returns the users command from the received message.
    private static String getClientCommand(String clientMessage) {
        if (clientMessage.startsWith("retrieve_message") || clientMessage.equals("retrieve_all_messages")) {
            return clientMessage.split(" ")[0];
        } else {
            return "upload_message";
        }
    }


    // This method sends all stored messages to the user. If no messages are available, it will send a message
    // saying that theres no messages that have been sent to the server yet
    private static void retrieveAllMessages(PrintWriter out) {
        if (messageStorage.isEmpty()) {
            out.println("No messages have been sent yet");
        } else {
            messageStorage.forEach((number, message) -> out.println(number + ": " + message));
        }
        out.println("End of messages");
    }
}
