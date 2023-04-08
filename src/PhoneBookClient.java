// Łukasz Wdowiak 264026

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class PhoneBookClient extends JFrame implements Runnable, ActionListener {
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String username;

    private HashMap<String, String> chats = new HashMap<>();

    JComboBox<String> clientMenu = new JComboBox<>();

    JPanel panel = new JPanel();
    JLabel write = new JLabel("Napisz:");
    JTextField writeField = new JTextField(20);

    JTextArea readTextArea = new JTextArea(15, 25);
    JScrollPane scroll_bars = new JScrollPane(readTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    String selectedUser;
    boolean newUserConnected = false;

    public static void main(String[] args) {
        try {
            String host = JOptionPane.showInputDialog("Podaj adres serwera");
            host = host.equals("") ? "localhost" : host;
            String username = JOptionPane.showInputDialog("Podaj nazwe uzytkownika");
            if (username.contains(" ")) {
                JOptionPane.showMessageDialog(null, "Nazwa uzytkownika nie moze posiadac spacji", "Blad", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (username != null && !username.equals(""))
                new PhoneBookClient(new Socket(host, 25000), username);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Nie mozna polaczyc z serverem", "Blad", JOptionPane.ERROR_MESSAGE);
        }
    }

    public PhoneBookClient(Socket socket, String username) {
        super(username);
        this.socket = socket;
        this.username = username;

        panel.add(clientMenu);
        panel.add(scroll_bars);
        panel.add(write);
        panel.add(writeField);
        writeField.addActionListener(this);
        clientMenu.addActionListener(this);
        readTextArea.setEditable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                sendMessage("BYE");
                closeEverything();
            }

            @Override
            public void windowClosed(WindowEvent event) {
                windowClosing(event);
            }
        });
        setSize(300, 350);
        setLayout(new GridLayout(2, 1));
        setContentPane(panel);
        setResizable(false);
        setVisible(true);

        try {
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            closeEverything();
        }
        new Thread(this).start();
    }

    public void sendMessage(String message) {
        try {
            bufferedWriter.write(username + ": " + message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    public void printMessage(String message) {
        String text = readTextArea.getText();
        readTextArea.setText(text + message + "\n");
    }

    private void closeEverything() {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null)
                bufferedWriter.close();
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String message;
        sendMessage(""); // zeby pozwolic odczytac nazwe uzytkownika serwerowi
        while (!socket.isClosed()) {
            try {
                message = bufferedReader.readLine();
                if (message != null) {
                    String[] command = message.split("\\s");
                    if (command[1].equals("CLIENTS")) {
                        knowUsers(command);
                    } else {
                        if (command[0].equals(selectedUser))
                            printMessage(message);
                        else {
                            String convertedMessage = chats.get(command[0]) + message + "\n";
//                                    : chats.get(command[0]) + "\n" + message;
                            chats.replace(command[0], convertedMessage);
                        }
                    }
                }
            } catch (IOException e) {
                closeEverything();
            }
        }
    }

    private void knowUsers(String[] command) {
        newUserConnected = true;
        clientMenu.removeAllItems();

        for (int i = 2; i < command.length; i++)
            if (!command[i].equals(username + ":")) {
                boolean found = false;
                for (int j = 0; j < clientMenu.getItemCount(); j++) {
                    if (clientMenu.getItemAt(j).equals(command[i]))
                        found = true;

                }
                if (!found) {
                    chats.put(command[i], "");
                }
            }


        for (int i = 2; i < command.length; i++) {
            if (!command[i].equals(username + ":")) {
                clientMenu.addItem(command[i]);
            }
            if (command[i].equals(selectedUser)) {
                clientMenu.setSelectedItem(selectedUser);
            } else {
                selectedUser = clientMenu.getItemAt(0);
                clientMenu.setSelectedItem(selectedUser);
            }
        }
        newUserConnected = false;
    }


    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == writeField) {
            if (!writeField.getText().equals("")) {
                if (clientMenu.getSelectedItem().equals("SERVER:"))
                    sendMessage(writeField.getText());
                else
                    sendMessage("TO " + clientMenu.getSelectedItem() + " " + writeField.getText());
                printMessage(username + ": " + writeField.getText());
            }
            writeField.setText("");
        }
        repaint();
        if (event.getSource() == clientMenu) {
            if (clientMenu.getSelectedItem() != null && !newUserConnected) {
                String currentUser = clientMenu.getSelectedItem().toString();
                chats.replace(selectedUser, readTextArea.getText());
                readTextArea.setText(chats.get(currentUser));
                selectedUser = currentUser;
            }
        }
    }
}
