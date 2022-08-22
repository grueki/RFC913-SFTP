package Server;

import java.io.*;
import java.net.*;

class Server {
    static String HOST_DOMAIN = "localhost";
    static int PORT_NUM = 3000;

    public static void main(String[] args)
    {
        try {
            String clientSentence;
            String capitalizedSentence;

            ServerSocket welcomeSocket = new ServerSocket(PORT_NUM);

            while (true) {

                Socket connectionSocket = welcomeSocket.accept();

                BufferedReader inFromClient =
                        new BufferedReader(new
                                InputStreamReader(connectionSocket.getInputStream()));

                DataOutputStream outToClient =
                        new DataOutputStream(connectionSocket.getOutputStream());

                clientSentence = inFromClient.readLine();

                capitalizedSentence = clientSentence.toUpperCase() + '\n';

                outToClient.writeBytes(capitalizedSentence);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
