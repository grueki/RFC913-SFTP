package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread{
    static int PORT_NUM = 3000;
    public static ServerSocket serverSocket;

    public void run() {
        try {
            serverSocket = new ServerSocket(PORT_NUM);
            System.out.println("Server listening on port " + PORT_NUM + "!");

            int counter = 1;
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client number " + counter + " connected!");
                ServerClientThread newClientThread = new ServerClientThread(clientSocket);
                newClientThread.start();
                counter++;
            }
        } catch (IOException e) {
            System.out.println("Connection closed.");
        }

    }

    public static void main(String[] args) {
        Server s = new Server();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        s.start();

    }
}
