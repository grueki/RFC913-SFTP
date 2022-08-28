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
    String expected;
    int numLines;

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

        // skip host info for tests
        inFromServer.readLine();
    }

    public String sendMessage(String msg) throws IOException {
        outToServer.writeBytes(msg + "\n");
        numLines = Integer.parseInt(inFromServer.readLine());
        return inFromServer.readLine();
    }

    public void USER_userOnly() throws IOException {
        newTestClient();

        message = "USER user_only";

        response = sendMessage(message);

        expected = "! Logged in as user_only";

        if (response.equals(expected)) {
            System.out.println("PASSED");
        }
        else {
            System.out.println("FAILED");
        }


        System.out.println("Expected: " + expected);
        System.out.println("Received: " + response);
    }

}
