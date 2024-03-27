package handler;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class connectionHandlerCLI {
    private SSLSocket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected;



    // This method establishes the connection to the server using SSL sockets. It takes the inputs given from the
    // secureSMPClientCLI class.

    public void connectToServer(String host, int port, String username, String password) throws IOException {
        System.setProperty("javax.net.ssl.trustStore", "C:/Users/The Oracle/Desktop/DSC24/DC/smpclienttruststore.p12"); // If error coms up running make sure this directory is correct, at the moment it's set to my machine
        System.setProperty("javax.net.ssl.trustStorePassword", "admin12345");

        SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = (SSLSocket) ssf.createSocket(host, port);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        isConnected = true;

        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
    }


    // This method is what sends a message to the server, it ensures that the user is connected first, and if they are
    // it will check for an empty message, it'll then send the message once these are checked
    public void sendMessage(String message) throws IOException {
        if (!isConnected) {
            throw new IllegalStateException("Not connected to the server.");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }
        out.println(message);
    }


    // This method reads a message from the server. It checks if the user is connected to the server,
    // and then reads a line of text from the input stream, and returns the received message.

    public String readMessage() throws IOException {
        if (!isConnected) {
            throw new IllegalStateException("Not connected to the server.");
        }
        return in.readLine();
    }


    // This method closes the connection to the server, it facilitates the logout method in secureSMPClientCLI. It checks
    // if the client is connected, closes the output and input streams, closes the socket, and updates the connection status
    public void closeConnection() throws IOException {
        if (!isConnected) {
            return;
        }
        out.close();
        in.close();
        socket.close();
        isConnected = false;
    }


    // This is just a boolean value indicating whether the client is currently connected to the server
    public boolean isConnected() {
        return isConnected;
    }
}

