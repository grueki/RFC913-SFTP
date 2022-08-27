package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class Server {
    static String HOST_DOMAIN = "localhost";
    static int PORT_NUM = 3000;

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT_NUM)) {
            System.out.println("Server listening on port " + PORT_NUM + "!");

            int counter = 1;
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client number " + counter + " connected!");
                ServerClientThread newClientThread = new ServerClientThread(clientSocket, counter);
                newClientThread.start();
                counter++;
            }
        }
    }
}
