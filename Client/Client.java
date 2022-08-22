package Client;

import java.io.*;
import java.net.*;
import java.util.Objects;

public class Client {
    static String HOST_DOMAIN = "localhost";
    static int PORT_NUM = 3000;

    public static void main(String[] args) throws Exception {
        String sentence = null;
        String modifiedSentence = null;

        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));

        Socket clientSocket = new Socket(HOST_DOMAIN, PORT_NUM);

        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        BufferedReader inFromServer =
                new BufferedReader(new
                        InputStreamReader(clientSocket.getInputStream()));

        sentence = inFromUser.readLine();

        outToServer.writeBytes(sentence + '\n');

        modifiedSentence = inFromServer.readLine();

        System.out.println("FROM SERVER: " + modifiedSentence);

        clientSocket.close();

    }
}
