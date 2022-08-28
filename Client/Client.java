package Client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

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

            if (message.toUpperCase().contains("STOR")) {
                String[] args = message.split("\\s+");
                if (Files.exists(Paths.get(args[2]))) {
                    outToServer.writeBytes(message + "\n");
                    sendFile(args[2]);
                    recieveMsg();
                }
                else {
                    System.out.println("File does not exist. Aborting command.");
                }
            }
            else {
                outToServer.writeBytes(message + "\n");

                if (message.equalsIgnoreCase("SEND") && recieveReady) {
                    receiveFile();
                }
                recieveMsg();
            }

        } while (!message.equalsIgnoreCase("DONE"));
    }

    public void receiveFile() throws IOException {
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

    public void sendFile(String fileName) throws IOException {
        String line;
        outToServer.writeBytes(Paths.get(fileName).getFileName().toString()+"\n");

        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));

        while ((line = bufferedReader.readLine()) != null) {
            outToServer.writeBytes(line + "\n");
        }

        outToServer.writeBytes("END\n");

        bufferedReader.close();
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
