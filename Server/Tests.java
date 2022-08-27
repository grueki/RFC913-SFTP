package Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Tests {
    static String HOST_DOMAIN = "localhost";
    static int PORT_NUM = 3000;
    static Socket testClientSocket;

    String message;
    String response;

    static DataOutputStream outToServer;
    static BufferedReader inFromServer;

    public static void main(String[] args) throws IOException {
        Tests tests = new Tests();

        System.out.println("Beginning tests");
        System.out.println("-----------------------------------");
        tests.USER_userOnly();
        outToServer.close();
        inFromServer.close();
    }

    public void newTestClient() throws IOException {
        testClientSocket = new Socket(HOST_DOMAIN, PORT_NUM);

        outToServer = new DataOutputStream(testClientSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(testClientSocket.getInputStream()));

        System.out.println(inFromServer.readLine());
    }

    public String sendMessage(String msg) throws IOException {
        outToServer.writeBytes(msg + "\n");
        return inFromServer.readLine();
    }

    public void USER_userOnly() throws IOException {
        newTestClient();

        message = "USER user_only";

        response = sendMessage(message);

        System.out.println(response.contains("Logged in as user_only"));

    }

}
