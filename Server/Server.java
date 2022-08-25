package Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

class Server {
    static String HOST_DOMAIN = "localhost";
    static int PORT_NUM = 3000;

    private ServerSocket serverSocket;
    private Socket clientSocket;

    private BufferedReader inFromClient;
    private DataOutputStream outToClient;

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT_NUM);
        System.out.println("Server listening on port " + PORT_NUM + "!");

        clientSocket = serverSocket.accept();
        System.out.println("Client connected!");

        inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outToClient = new DataOutputStream(clientSocket.getOutputStream());

        InetAddress ip = InetAddress.getLocalHost();
        String hostname = ip.getHostName();
        outToClient.writeBytes("+" + hostname + ": Welcome to the server! You're now connected on port " + PORT_NUM + ".\n");

        boolean done = false;

        while (!done) {
            done = loop();
        }

        stop();
    }

    public boolean loop() throws IOException {
        String inputLine;
        if ((inputLine = inFromClient.readLine()) != null) {
            if ("DONE".equals(inputLine)) {
                return true;
            }
            else {
                outToClient.writeBytes("Recieved your message, \"" + inputLine + "\"! :)\n");
            }
        }
        return false;
    }

    public void stop() throws IOException {
        inFromClient.close();
        outToClient.close();
        clientSocket.close();
        serverSocket.close();
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        Server myServer = new Server();
        myServer.start();
    }
}
