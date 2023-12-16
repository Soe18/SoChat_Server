package Server;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

// Singleton Pattern: https://www.javaboss.it/singleton-design-pattern/
public class ClientSockets {
    // Unica istanza della classe
    private static ClientSockets instance = null;
    public HashMap<String, ObjectOutputStream> hashMap;

    // Costruttore invisibile
    private ClientSockets() {
        hashMap = new HashMap<>();
    }

    public static ClientSockets getInstance() {
        // Crea l'oggetto solo se NON esiste:
        if (instance == null) {
            instance = new ClientSockets();
        }
        return instance;
    }
}