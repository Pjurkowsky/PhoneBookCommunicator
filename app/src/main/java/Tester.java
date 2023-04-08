// Łukasz Wdowiak 264026

import java.io.IOException;
import java.net.Socket;

public class Tester {
    public static void main(String[] args) throws IOException {
        new PhoneBookServer();
        new PhoneBookClient(new Socket("localhost", 25000),"esa");
        new PhoneBookClient(new Socket("localhost", 25000),"elo");
    }
}
