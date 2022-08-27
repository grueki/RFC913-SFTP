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

        System.out.println(inFromServer.readLine());

        while (true) {
            System.out.print("Enter command: ");
            message = inFromUser.readLine();
            sendMessage(message);
//            System.out.println(response);

            if (message.equalsIgnoreCase("DONE")){
                break;
            }
        }
    }

    public void sendMessage(String msg) throws IOException {
        outToServer.writeBytes(msg + "\n");
        String response = inFromServer.readLine();
        try {
            int numLines = Integer.parseInt(response);
            for (int i = 0; i < numLines; i++) {
                System.out.println(inFromServer.readLine());
            }
        } catch (NumberFormatException e) {
            System.out.println(response);
        }
    }

    public void stop() throws IOException {
        inFromServer.close();
        outToServer.close();
        inFromUser.close();
        clientSocket.close();
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        Client c = new Client();
        c.start();
    }
}
