package Client;

import java.io.*;
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

        do {
            System.out.print("Enter command: ");
            message = inFromUser.readLine();
            if (message.equalsIgnoreCase("SEND")) {
                receiveFile(message);
            }
            sendMessage(message);

        } while (!message.equalsIgnoreCase("DONE"));
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

    public void receiveFile(String msg) throws IOException {
        outToServer.writeBytes(msg + "\n");

        String filename = inFromServer.readLine();
        String line;
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename));

        while (!"END".equals(line = inFromServer.readLine())) {
            bufferedWriter.write(line);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
    }

    public static void main(String[] args) throws IOException {
        Client c = new Client();
        c.start();
    }
}
