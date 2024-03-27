package handler;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class connectionHandler {
    private SSLSocket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected;



    // This method establishes the connection to the server using SSL sockets. It takes the inputs given from the
    // secureSMPClientgui class.
    public void connectToServer(String host, int port) throws IOException {
        System.setProperty("javax.net.ssl.trustStore", "C:/Users/The Oracle/Desktop/DSC24/DC/smpclienttruststore.p12");// If error coms up running make sure this directory is correct, at the moment it's set to my machine
        System.setProperty("javax.net.ssl.trustStorePassword", "admin12345");

        SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = (SSLSocket) ssf.createSocket(host, port);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        isConnected = true;
    }


    // This method is what sends a message to the server, it ensures that the user is logged in first, and if they are
    // it will check for an empty message, it'll then send the message once these are checked, if some other error happens
    // after this, it will show a failed to send mesage message.
    public boolean sendMessage(String message) {
        if (!isConnected) {
            System.err.println("Error make sure user is logged in.");
            return false;
        }
        if (message == null || message.trim().isEmpty()) {
            System.err.println("Cannot send empty message.");
            return false;
        }
        try {
            out.println(message);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
            return false;
        }
    }


    // This method reads a message from the server. It checks if the user is connected to the server, then
    // reads a line of text from the input stream, and returns the received message. If the client is not connected,
    // it prints an error message and returns null.
    public String readMessage() throws IOException {
        if (!isConnected) {
            System.err.println("Client is not connected to the server.");
            return null;
        }
        return in.readLine();
    }


    // This method facilitates the logout method in the GUI, and closes the connection between the client and server.
    // if the client isn't connected yet, itll show an error message.
    public void closeConnection() throws IOException {
        if (!isConnected) {
            System.err.println("Client isn't connected to the server.");
            return;
        }
        out.close();
        in.close();
        socket.close();
        isConnected = false;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
