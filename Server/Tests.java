package Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Tests {
    static String DIVIDER = "-----------------------------------";
    static String HOST_DOMAIN = "localhost";
    static int PORT_NUM = 3000;
    static Socket testClientSocket;

    static DataOutputStream outToServer;
    static BufferedReader inFromServer;

    int testsCount = 0;
    int failedTests = 0;

    public static void main(String[] args) throws IOException {
        Tests tests = new Tests();

        System.out.println("Starting tests...");
        tests.runUserTests();

        outToServer.close();
        inFromServer.close();

        System.out.println(DIVIDER);
        if (tests.failedTests == 0) {
            System.out.println("All tests passed!");
        }
        else {
            System.out.println((tests.testsCount - tests.failedTests) + " test(s) passed, " +
                    tests.failedTests + " test(s) failed.");
        }
        System.out.println(DIVIDER);
    }

    public void USER_userOnly() throws IOException {
        String[][] cmds = {{"USER user_only", "! Logged in as user_only"},
                {"DONE", "+Connection closed."}};
        runTest("USER: User with no password or account", cmds);
    }

    public void USER_userOnly_attemptedSignIn() throws IOException {
        String[][] cmds = {{"USER user_only", "! Logged in as user_only"},
                {"PASS somePassword", "-Already logged in."},
                {"ACCT someAcct", "-Already logged in. No accounts associated with this user."},
                {"DONE", "+Connection closed."} };
        runTest("USER: User with no password or account", cmds);
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
        String response = inFromServer.readLine();
        try {
            int numLines = Integer.parseInt(response);
        } catch (NumberFormatException e) {
            return response;
        }

        return inFromServer.readLine();
    }

    public void runUserTests() throws IOException {
        USER_userOnly();
        USER_userOnly_attemptedSignIn();
    }

    public void runTest(String testName, String[][] commands) throws IOException {
        newTestClient();

        System.out.println(DIVIDER);
        System.out.println(testName);
        System.out.println(DIVIDER);

        boolean isPassing = true;

        for (String[] command : commands) {
            isPassing = testCommand(command[0], command[1]);
            if (!isPassing) {
                break;
            }
        }

        if (isPassing) {
            System.out.println("PASSED");
        }
        else {
            System.out.println("FAILED");
            failedTests++;
        }
        testsCount++;
    }

    public boolean testCommand(String command,
                               String expected) throws IOException {

        String response = sendMessage(command);
        System.out.println("Expected: " + expected);
        System.out.println("Received: " + response);
        return response.equals(expected);
    }

}
