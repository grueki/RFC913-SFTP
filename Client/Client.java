package Client;

import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client {
    static String HOST_DOMAIN = "localhost";
    static int PORT_NUM = 3000;

    private static Socket clientSocket;

    private BufferedReader inFromServer;
    private DataOutputStream outToServer;

    private BufferedReader inFromUser;
    boolean recieveReady = false;

    public void start() throws IOException {
        String message;

        clientSocket = new Socket(HOST_DOMAIN, PORT_NUM);
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        System.out.println(inFromServer.readLine());

        do {
            System.out.print("Enter command: ");
            message = inFromUser.readLine();

            outToServer.writeBytes(message + "\n");

            if (message.equalsIgnoreCase("SEND") && recieveReady) {
                receiveFile(message);
            }
            recieveMsg();

        } while (!message.equalsIgnoreCase("DONE"));
    }

    public void receiveFile(String msg) throws IOException {

        String filename = inFromServer.readLine();
        String line;
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename));

        while (!"END".equals(line = inFromServer.readLine())) {
            bufferedWriter.write(line);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
        recieveReady = false;
    }

    private void recieveMsg() throws IOException {
        String response = inFromServer.readLine();
        try {
            int numLines = Integer.parseInt(response);
            for (int i = 0; i < numLines; i++) {
                String delayedResponse = inFromServer.readLine();
                System.out.println(delayedResponse);
                if (delayedResponse.contains("bytes will be sent. Respond with SEND command to proceed with retrieving")) {
                    recieveReady = true;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(response);
        }
    }

    public static void main(String[] args) throws IOException {
        Client c = new Client();
        c.start();
    }
}
