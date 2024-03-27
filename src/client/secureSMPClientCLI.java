package client;

import handler.connectionHandlerCLI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class secureSMPClientCLI {
    private static connectionHandlerCLI connectionHandler = new connectionHandlerCLI();
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static boolean isConnected = false;

    public static void main(String[] args) {
        System.out.println("Welcome to Secure Messaging Client CMD Version!");

        while (true) {
            if (!isConnected) {
                connectToServer();
            } else {
                runSMPCli();
            }
        }
    }

    //This code sets the CLI for the application, and allows the user to input a chice of what they wish to do.
    private static void runSMPCli() {
        System.out.println("\n*************************************");
        System.out.println("\n**Secure Message Client Version 1.0**");

        System.out.println("\n*************************************");
        System.out.println("\n1. Send Message");
        System.out.println("\n2. Retrieve Message");
        System.out.println("\n3. Retrieve All Messages");
        System.out.println("\n4. Log Out");
        System.out.println("\n*************************************");

        try {
            System.out.print("\nEnter your choice (1-4): ");
            String choice = reader.readLine();
            switch (choice) {
                case "1":
                    sendMessage();
                    break;
                case "2":
                    retrieveMessage();
                    break;
                case "3":
                    retrieveAllMessages();
                    break;
                case "4":
                    logOut();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 4.");
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }
    }


    // This method asks the user for the host, port, a username and password also, it then uses the connectionHandler
    // method connectToServer with those inputs to try establish a connection to the server, once successful, it says
    // connected to the server, with the host and port, if an error happens, it shows what happened.
        private static void connectToServer() {
            try {
                System.out.print("Enter Server Host: ");
                String host = reader.readLine();
                System.out.print("Enter Server Port: ");
                int port = Integer.parseInt(reader.readLine());
                System.out.print("Enter Username: ");
                String username = reader.readLine();
                System.out.print("Enter Password: ");
                String password = reader.readLine();
                connectionHandler.connectToServer(host, port, username, password);
                isConnected = true;
                System.out.println("Connected to server at " + host + ":" + port);
            } catch (IOException e) {
                System.err.println("Error connecting to server: " + e.getMessage());
            }
        }


        // This method allows the user to send a message to the server. It uses the connectionHandler class again
        // to attempt to send the message, it shows a success or error message depending on the status of message being sent
    private static void sendMessage() {
        try {
            System.out.print("Enter message to send: ");
            String message = reader.readLine();
            connectionHandler.sendMessage(message);
            System.out.println("Message sent successfully!");
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }


        // This method is used to try and retrieve a message from the server, it asks the user to input the
        // 'number' of the message to be received. It works off the connectionHandler class to see if the message
        // exists, and if it does, it will return the text of the message.
    private static void retrieveMessage() {
        try {
            System.out.print("Enter message number to retrieve: ");
            String msgNumber = reader.readLine();
            connectionHandler.sendMessage("retrieve_message " + msgNumber);
            String response = connectionHandler.readMessage();
            System.out.println("Message received: " + response);
        } catch (IOException e) {
            System.err.println("Error retrieving message: " + e.getMessage());
        }
    }


        // This method is used to try and retrieve all messages from the server, it uses the connectioNHandler class
        // to go through all the messages stored, it loops through til it receives the 'end of messages' indicator.
        // it simply prints the messages then onto the CLI
    private static void retrieveAllMessages() {
        try {
            connectionHandler.sendMessage("retrieve_all_messages");
            System.out.println("All messages:");
            String response;
            while (!(response = connectionHandler.readMessage()).equals("End of messages")) {
                System.out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error retrieving all messages: " + e.getMessage());
        }
    }


        // This method simply sends the logout command to the server using the connectionHandler, and then closes the connection
    private static void logOut() {
        try {
            connectionHandler.sendMessage("logout");
            connectionHandler.closeConnection();
            System.out.println("Logged out and disconnected from server.");
        } catch (IOException e) {
            System.err.println("Error during logging out: " + e.getMessage());
        }
    }
}
