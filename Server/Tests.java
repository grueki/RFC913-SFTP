package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Tests {
    static String HOST_DOMAIN = "localhost";
    static int PORT_NUM = 3000;
    static Server testServer;
    static Socket testClientSocket;

    String message;
    String response;

    static DataOutputStream outToServer;
    static BufferedReader inFromServer;
    static BufferedReader inFromUser;

    public static void main(String[] args) throws IOException {
        Tests tests = new Tests();

        testServer = new Server();
        testServer.start();

        System.out.println("Beginning tests");
        System.out.println("-----------------------------------");
        tests.USER_userOnly();

        outToServer.close();
        inFromServer.close();
        inFromUser.close();
    }

    public void newTestClient() throws IOException {
        testClientSocket = new Socket(HOST_DOMAIN, PORT_NUM);

        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        outToServer = new DataOutputStream(testClientSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(testClientSocket.getInputStream()));

        ServerClientThread testServerClientThread = new ServerClientThread(testClientSocket);
        testServerClientThread.start();

        System.out.println(inFromServer.readLine());
    }

    public String sendMessage(String msg) throws IOException {
        outToServer.writeBytes(msg + "\n");
        return inFromServer.readLine();
    }

    public void USER_userOnly() throws IOException {
        newTestClient();

        System.setIn(new ByteArrayInputStream("USER user_only".getBytes()));
        Scanner sc = new Scanner(System.in);

        message = sc.nextLine();

        response = sendMessage(message);

        System.out.println(response);
        boolean passes = response.contains("Logged in as user_only");
        System.out.println(passes);
    }

}
