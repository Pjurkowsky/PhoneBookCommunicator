// Łukasz Wdowiak 264026
// Aby przetestować 1 zadanie (PHONE BOOKA) należy pisać do servera
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;

public class PhoneBookServer extends JFrame implements Runnable, ActionListener {
    static final int SERVER_PORT = 25000;
    private ServerSocket serverSocket;
    public Vector<ClientHandler> clientHandlers = new Vector<>();
    public PhoneBook phoneBook = new PhoneBook();
    JPanel panel = new JPanel();
    JLabel write = new JLabel("Napisz:");
    JTextField writeField = new JTextField(20);
    JTextArea readTextArea = new JTextArea(15, 25);
    JScrollPane scroll_bars = new JScrollPane(readTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);


    public static void main(String[] args){
        new PhoneBookServer();
    }
    public PhoneBookServer() {
        super("PhoneBookServer");

        panel.add(scroll_bars);
      //  panel.add(write);
      //  panel.add(writeField);

        readTextArea.setEditable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(300, 350);
        setLayout(new GridLayout(2, 1));
        setContentPane(panel);
        setResizable(false);
        setVisible(true);

        new Thread(this).start();
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(this, socket);

                StringBuilder clients = new StringBuilder("SERVER: ");
                for (ClientHandler client : clientHandlers)
                    clients.append(client);
                for (ClientHandler client : clientHandlers)
                    client.sendMessage("CLIENTS " + clients);

                printReceivedMessage("SERVER: " + clientHandler.toString().substring(0, clientHandler.toString().length() - 2) + " has connected to server");
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            closeServerSocket();
        }
    }

    @Override
    public void run() {
        startServer();
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public void addClient(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
    }

    synchronized public void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    synchronized public void printReceivedMessage(String message) {
        String text = readTextArea.getText();
        readTextArea.setText(text + message + "\n");
        readTextArea.setCaretPosition(readTextArea.getDocument().getLength());
    }
}
