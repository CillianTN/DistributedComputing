package client;

import handler.connectionHandler;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class secureSMPClientGUI extends JFrame {
    private final connectionHandler networkHandler = new connectionHandler();
    private JTextArea messageBox;
    private JTextField inputField;
    private JTextField retrieveField;
    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;
    private JPasswordField userPass;

    public secureSMPClientGUI() {
        setTitle("Secure Messaging Client");
        setSize(1200, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        messageBox = new JTextArea(10, 40);
        messageBox.setEditable(false);
        inputField = new JTextField(30);
        retrieveField = new JTextField(5);
        hostField = new JTextField(10);
        portField = new JTextField(5);
        userField = new JTextField(10);
        userPass = new JPasswordField(10);

        JButton connectButton = new JButton("Connect to Server");
        JButton sendButton = new JButton("Send Message");
        JButton retrieveButton = new JButton("Retrieve Message");
        JButton retrieveAllButton = new JButton("Retrieve All Messages");
        JButton logOutButton = new JButton("Log Off");

        JPanel panel = new JPanel(new BorderLayout());

        JPanel topLayout = new JPanel(new FlowLayout());
        topLayout.add(new JLabel("Server Host: "));
        topLayout.add(hostField);
        topLayout.add(new JLabel("Server Port: "));
        topLayout.add(portField);
        topLayout.add(new JLabel("Username: "));
        topLayout.add(userField);
        topLayout.add(new JLabel("Password: "));
        topLayout.add(userPass);
        topLayout.add(connectButton);
        panel.add(topLayout, BorderLayout.NORTH);

        panel.add(new JScrollPane(messageBox), BorderLayout.CENTER);

        JPanel bottomLayout = new JPanel(new FlowLayout());
        bottomLayout.add(inputField);
        bottomLayout.add(sendButton);
        bottomLayout.add(new JLabel("Message Number Search: "));
        bottomLayout.add(retrieveField);
        bottomLayout.add(retrieveButton);
        bottomLayout.add(retrieveAllButton);
        bottomLayout.add(logOutButton);
        panel.add(bottomLayout, BorderLayout.SOUTH);

        add(panel);

        connectButton.addActionListener(e -> connectToServer());
        sendButton.addActionListener(e -> sendMessage());
        retrieveButton.addActionListener(e -> retrieveMessage());
        retrieveAllButton.addActionListener(e -> retrieveAllMessages());
        logOutButton.addActionListener(e -> logOut());
    }



    // This method will attempt to connect to the server by taking in the host, port, username and password.
    // It also validates to ensure that they're all entered correctly. If successful - message will appear saying
    // connected, and logged in, if it fails, it will show an error message
    private void connectToServer() {
        String host = hostField.getText().trim();
        String portStr = portField.getText().trim();
        String username = userField.getText().trim();
        String password = new String(userPass.getPassword());

        if (host.isEmpty() || portStr.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Host, port, username orr password is empty. Please fill them in", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int port = Integer.parseInt(portStr);

        try {
            networkHandler.connectToServer(host, port);
            JOptionPane.showMessageDialog(this, "Connected to server at " + host + ":" + port);
            login();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error connecting to server: " + e.getMessage() + "\n\n\nPlease run and try again", "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    // This method displays a success message showing that the user has logged in successfully.
    private void login() {
        JOptionPane.showMessageDialog(this, "Successfully logged in!", "Login Success", JOptionPane.INFORMATION_MESSAGE);
    }




    //  This method sends a message to the server using the network/client handler.
    //  It ensures the user is connected to the server, sends the message, and updates the message box with
    //  the server's response. If there is an error during the process, it displays an error message.
    private void sendMessage() {
        if (!networkHandler.isConnected()) {
            JOptionPane.showMessageDialog(this, "Error: Not connected to the server. Please connect first.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = inputField.getText();
        boolean messageSent = networkHandler.sendMessage(message);
        if (!messageSent) {
            JOptionPane.showMessageDialog(this, "Failed to send message.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        inputField.setText("");
        try {
            String response = networkHandler.readMessage();
            messageBox.append("Server: " + response + "\n");
        } catch (IOException e) {
            messageBox.append("Failed to read response from server.\n");
        }
    }

    // This method retrieves a specific message from the server based on the 'message number' provided by the user.
    // It validates the message number, sends a request to the server to retrieve the message, and updates the
    // message box with the retrieved message. If there is an error during this, it;ll displays an error message.

    private void retrieveMessage() {
        if (!networkHandler.isConnected()) {
            JOptionPane.showMessageDialog(this, "Error: Not connected to the server. Please connect first.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String msgNumber = retrieveField.getText().trim();
        if (msgNumber.isEmpty() || !msgNumber.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid message number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        networkHandler.sendMessage("retrieve_message " + msgNumber);
        try {
            String response = networkHandler.readMessage();
            messageBox.append("Message " + msgNumber + " received: " + response + "\n");
        } catch (IOException e) {
            messageBox.append("Failed to retrieve message.\n");
        }
    }



    // This method retrieves all message(s) from the server, it will update the message box with the retrieved message(s).
    // If there's an error, it'll show the error message.

    private void retrieveAllMessages() {
        if (!networkHandler.isConnected()) {
            JOptionPane.showMessageDialog(this, "Error: Not connected to the server. Please connect first.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        networkHandler.sendMessage("retrieve_all_messages");
        try {
            String response;
            while (!(response = networkHandler.readMessage()).equals("End of messages")) {
                messageBox.append(response + "\n");
            }
        } catch (IOException e) {
            messageBox.append("Failed to retrieve all messages.\n");
        }
    }


    // This sends a logout request to the server, then closes the connection, and displays a logout confirmation message.
    // If there is an error during the logout process, it displays an error message. If the user isnt connected to the server,
    // it exits the program.
    private void logOut() {
        if (!networkHandler.isConnected()) {
            JOptionPane.showMessageDialog(this, "Exiting the program.", "Good luck", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0); // Exit the program if not connected
            return;
        }

        try {
            networkHandler.sendMessage("logout");
            networkHandler.closeConnection();
            JOptionPane.showMessageDialog(this, "Logged off and disconnected from server.", "Disconnected", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0); // Or hide and dispose the window if you prefer
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error during logging off: " + e.getMessage(), "Log Off Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            secureSMPClientGUI clientGUI = new secureSMPClientGUI();
            clientGUI.setVisible(true);
        });
    }
}
