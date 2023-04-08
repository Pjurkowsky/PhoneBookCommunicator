// Łukasz Wdowiak 264026

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    PhoneBookServer phoneBookServer;
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String clientUsername;

    public ClientHandler(PhoneBookServer phoneBookServer, Socket socket) {
        try {
            this.phoneBookServer = phoneBookServer;
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            phoneBookServer.addClient(this);

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String message;
        while (socket.isConnected()) {
            try {
                message = bufferedReader.readLine();
                interpreterOfCommands(message);
                phoneBookServer.printReceivedMessage(message);
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void interpreterOfCommands(String message) {
        if (message != null) {
            String[] command = message.split("\\s");
            String reply;
            try {
                switch (command[1]) {
                    case "LOAD":
                        reply = phoneBookServer.phoneBook.load(command[2]);
                        break;
                    case "SAVE":
                        reply = phoneBookServer.phoneBook.save(command[2]);
                        break;
                    case "GET":
                        reply = phoneBookServer.phoneBook.get(command[2]);
                        break;
                    case "PUT":
                        reply = phoneBookServer.phoneBook.put(command[2], command[3]);
                        break;
                    case "REPLACE":
                        reply = phoneBookServer.phoneBook.replace(command[2], command[3]);
                        break;
                    case "DELETE":
                        reply = phoneBookServer.phoneBook.delete(command[2]);
                        break;
                    case "LIST":
                        reply = "OK" + phoneBookServer.phoneBook.list();
                        break;
                    case "CLOSE":
                        phoneBookServer.closeServerSocket();
                        reply = "OK";
                        break;
                    case "BYE":
                        reply = "OK";
                        StringBuilder clients = new StringBuilder("SERVER: ");
                        sendMessage(reply);
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        for (ClientHandler client : phoneBookServer.clientHandlers)
                            clients.append(client);
                        for (ClientHandler client : phoneBookServer.clientHandlers) {
                            client.sendMessage("CLIENTS " + clients);
                            System.out.println(clients);
                        }

                        break;
                    case "TO":
                        for (ClientHandler client : phoneBookServer.clientHandlers) {
                            if ((client.toString()).equals(command[2] + " ")) {
                                StringBuilder messageFromUser = new StringBuilder();
                                for (int i = 3; i < command.length; i++)
                                    messageFromUser.append(" " + command[i]);
                                client.sendMessage(command[0], messageFromUser.toString());
                            }
                        }
                    default:
                        reply = null;
                }
                if (reply != null) {
                    sendMessage(reply);
                    //phoneBookServer.printReceivedMessage("SERVER: " + reply);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                sendMessage("ERROR bląd w komendzie");
            }
        }
    }

    public void sendMessage(String message) {
        try {
            this.bufferedWriter.write("SERVER: " + message);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage(String clientUsername, String message) {
        try {
            this.bufferedWriter.write(clientUsername + message);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        phoneBookServer.removeClient(this);
        try {
            if (bufferedReader != null)
                bufferedReader.close();
            if (bufferedWriter != null)
                bufferedWriter.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String toString() {
        return clientUsername;
    }
}
