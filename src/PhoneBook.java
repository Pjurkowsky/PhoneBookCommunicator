// Łukasz Wdowiak 264026

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class PhoneBook {
    private ConcurrentHashMap<String, String> record = new ConcurrentHashMap<>();

    public String load(String fileName) {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
            record = (ConcurrentHashMap<String, String>) inputStream.readObject();
            return "OK";
        } catch (IOException | ClassNotFoundException e) {
            return "ERROR Wystąpił błąd podczas odczytu danych z pliku.";
        }
    }

    public String save(String fileName) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            outputStream.writeObject(record);
        } catch (IOException e) {
            return "ERROR nie mozna zapisac do pliku";
        }
        return "OK";
    }

    public String get(String name) {
        return "OK " + record.get(name);
    }

    public String put(String name, String number) {
        try {
            record.put(name, number);
            return "OK";
        } catch (NullPointerException e) {
            return "ERROR nazwa lub numer nie istnieje";
        }
    }

    public String replace(String name, String number) {
        try {
            record.replace(name, number);
            return "OK";
        } catch (NullPointerException e) {
            return "ERROR nazwa lub numer nie istnieje";
        }
    }

    public String delete(String name) {
        try {
            record.remove(name);
            return "OK";
        } catch (NullPointerException e) {
            return "ERROR nazwa nie istnieje";
        }
    }

    public String list() {
        StringBuilder names = new StringBuilder(" ");
        for (String key : record.keySet()) {
            names.append(' ').append(key);
        }
        return names.toString();
    }
}
