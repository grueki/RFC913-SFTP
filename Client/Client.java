package Client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    static String HOST_DOMAIN = "localhost";
    static int PORT_NUM = 3000;

    private static Socket clientSocket;

    private BufferedReader inFromServer;
    private DataOutputStream outToServer;

    private BufferedReader inFromUser;

    public void start() throws IOException {
        String message;
        String response;

        clientSocket = new Socket(HOST_DOMAIN, PORT_NUM);
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        while (inFromUser != null) {
            message = inFromUser.readLine();
            if ("DONE".equals(message)) {
                stop();
            }
            else {
                response = sendMessage(message);
                System.out.println("FROM SERVER: " + response);
            }
        }
    }

    public String sendMessage(String msg) throws IOException {
        outToServer.writeBytes(msg + "\n");
        return inFromServer.readLine();
    }

    public void stop() throws IOException {
        inFromServer.close();
        outToServer.close();
        inFromUser.close();
        clientSocket.close();
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        Client myClient = new Client();
        myClient.start();
    }
}
