package Server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerClientThread extends Thread {
    BufferedReader inFromClient;
    DataOutputStream outToClient;
    String STATUS_SUCCESS = "+";
    String STATUS_ERROR = "-";
    String STATUS_LOGGEDIN = "! ";
    String LOGIN_DB = "login.txt";


    Socket clientSocket;
    String status;
    String serverMsg;
    String userId;
    String userAcc;
    String transmissionType = "B";
    File rootDir = new File(System.getProperty("user.dir"));
    String currentDir = "";
    boolean isLoggedIn = false;
    int linesToRead = 1;

    public ServerClientThread(Socket inSocket) throws IOException {
        clientSocket = inSocket;
    }

    public void run() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    inFromClient.close();
                    outToClient.close();
                    clientSocket.close();
                    this.interrupt();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));

            inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            outToClient = new DataOutputStream(clientSocket.getOutputStream());

            InetAddress ip = InetAddress.getLocalHost();
            String hostname = ip.getHostName();
            outToClient.writeBytes("+" + hostname + " RFC-913 SFTP\n");

            loop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loop() throws IOException {
        String clientMsg;
        String[] clientCmd;

        while ((clientMsg = inFromClient.readLine()) != null) {
            clientCmd = clientMsg.split("\\s+");

            try {
                switch (clientCmd[0].toUpperCase()) {
                    case "USER":
                        USER(clientCmd[1]);
                        break;
                    case "ACCT":
                        ACCT(clientCmd[1]);
                        break;
                    case "PASS":
                        PASS(clientCmd[1]);
                        break;
                    case "TYPE":
                        TYPE(clientCmd[1]);
                        break;
                    case "LIST":
                        LIST(clientCmd[1]);
                        break;
                    case "CDIR":
                        CDIR(clientCmd[1]);
                        break;
                    case "KILL":
                        KILL();
                        break;
                    case "NAME":
                        NAME();
                        break;
                    case "DONE":
                        DONE();
                        return;
                    case "RETR":
                        RETR();
                        break;
                    case "STOR":
                        STOR();
                        break;
                    default:
                        status = STATUS_ERROR;
                        serverMsg = "Invalid command";
                        break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                status = STATUS_ERROR;
                serverMsg = "Valid command, but insufficient arguments given. Please try again.";
            }
            String response = linesToRead + "\n" + status + serverMsg + "\n";
            outToClient.writeBytes(response);
            linesToRead = 1;
        }
    }

    public void USER(String user_input) throws IOException {
        List<String> users = Files.readAllLines(Paths.get(LOGIN_DB));
        boolean foundUser = false;

        String[] userInfo = new String[0];

        for (String user : users) {
            userInfo = user.split(",");
            if (user_input.equals(userInfo[0])) {
                foundUser = true;
                userId = userInfo[0];
                break;
            }
        }

        if (foundUser) {
            if (userInfo.length < 2) {
                status = STATUS_LOGGEDIN;
                isLoggedIn = true;
                serverMsg = "Logged in as " + userId;
            }
            else {
                status = STATUS_SUCCESS;
                serverMsg = "User-id valid, send account and/or password.";
            }
        }
        else {
            status = STATUS_ERROR;
            serverMsg = user_input + " is not a valid user-id. Please try again.";
        }
    }

    public void ACCT(String acc_input) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(LOGIN_DB));
        String[] userInfo = getUserInfo();

        boolean foundAcc = false;

        if (userInfo.length > 1) {
            for (String account : userInfo[1].split("\\|")) {
                if (acc_input.equals(account)) {
                    foundAcc = true;
                    userAcc = account;
                    break;
                }
            }

            if (foundAcc || userInfo[1].equals("")) {
                if (userInfo.length < 3) {
                    status = STATUS_LOGGEDIN;
                    isLoggedIn = true;
                    serverMsg = "Account valid (or not needed), logged in.";
                } else {
                    status = STATUS_SUCCESS;
                    serverMsg = "Account valid (or not needed), send password.";
                }
            } else {
                status = STATUS_ERROR;
                serverMsg = "Invalid account, try again.";
            }
        }
        else {
            status = STATUS_ERROR;
            serverMsg = "Already logged in. No accounts associated with this user.";
        }
    }

    public void PASS(String pass_input) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(LOGIN_DB));
        String[] userInfo = getUserInfo();

        boolean passwordSatisfied = false;

        if (userInfo.length > 2) {
            for (String password : userInfo[2].split("\\|")) {
                if (pass_input.equals(password)) {
                    passwordSatisfied = true;
                    break;
                }
            }

            if (passwordSatisfied) {
                if (userInfo[1].equals("") || userAcc != null) {
                    status = STATUS_LOGGEDIN;
                    isLoggedIn = true;
                    serverMsg = "Password is correct (or not needed), logged in.";
                } else {
                    status = STATUS_SUCCESS;
                    serverMsg = "Password valid (or not needed), send account.";
                }
            } else {
                status = STATUS_ERROR;
                serverMsg = "Wrong password, try again.";
            }
        }
        else {
            status = STATUS_ERROR;
            if (isLoggedIn) {
                serverMsg = "Already logged in.";
            }
            else {
                serverMsg = "No passwords associated with this user. Please send account.";
            }
        }
    }

    public void TYPE(String mode) {
        if (isLoggedIn) {
            switch (mode.toUpperCase()) {
                case "A":
                    transmissionType = mode.toUpperCase();
                    status = STATUS_SUCCESS;
                    serverMsg = "Using ASCII mode.";
                    break;
                case "B":
                    transmissionType = "B";
                    status = STATUS_SUCCESS;
                    serverMsg = "Using binary mode.";
                    break;
                case "C":
                    transmissionType = "C";
                    status = STATUS_SUCCESS;
                    serverMsg = "Using continuous mode.";
                    break;
                default:
                    status = STATUS_ERROR;
                    serverMsg = "Invalid type. Please follow the command 'TYPE' with 'A' (ASCII), 'B' (Byte) or 'C' (Continuous) to select transmission mode.";
                    break;
            }
        }
        else {
            status = STATUS_ERROR;
            serverMsg = "You are not logged in. Please log in using the USER command.";
        }
    }

    public void LIST(String list_cmd) throws IOException {
        if (isLoggedIn) {
            String[] list_args = list_cmd.split("\\s+");

            File dirToList;

            if (list_args.length > 1) {
                if (Paths.get(list_args[1]).isAbsolute()) {
                    dirToList = new File(list_args[1]);
                }
                else {
                    dirToList = new File(rootDir, currentDir + File.separator + list_args[1]);
                }

            } else {
                dirToList = new File(rootDir, currentDir);
            }

            switch (list_args[0].toUpperCase()) {
                case "F":
                    Set<String> listedFilenames = Stream.of(Objects.requireNonNull(dirToList.listFiles()))
                            .filter(file -> !file.isDirectory())
                            .map(File::getName)
                            .collect(Collectors.toSet());
                    status = STATUS_SUCCESS;
                    serverMsg = dirToList + "\n" + String.join("\n", listedFilenames);
                    linesToRead = listedFilenames.size() + 1;
                    break;
                case "V":
                    DateFormat formatter =  new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    Set<File> listedFiles = Stream.of(Objects.requireNonNull(dirToList.listFiles()))
                            .filter(file -> !file.isDirectory())
                            .collect(Collectors.toSet());
                    status = STATUS_SUCCESS;
                    StringBuilder msg = new StringBuilder(dirToList.getCanonicalPath());

                    for (File file : listedFiles) {
                        msg.append("\n");
                        msg.append(file.getName());
                        msg.append(" ");
                        msg.append("(size: ");
                        msg.append(file.length());
                        msg.append(" bytes, last modified: ");
                        msg.append(formatter.format(file.lastModified()));
                        msg.append(")");
                    }

                    serverMsg = msg.toString();
                    linesToRead = listedFiles.size() + 1;
                    break;
                default:
                    status = STATUS_ERROR;
                    serverMsg = "Specify a listing format. Please follow the command 'LIST' with 'F' (standard formatting) or 'V' (verbose formatting).";
                    break;
            }
        }
        else {
            status = STATUS_ERROR;
            serverMsg = "You are not logged in. Please log in using the USER command.";
        }
    }

    public void CDIR(String new_dir) throws IOException {
        if (isLoggedIn) {
            String enteredDir;

            if (Paths.get(new_dir).isAbsolute()) {
                currentDir = "";
                enteredDir = "";
                rootDir = new File(new_dir);
            }
            else {
                enteredDir = currentDir + new_dir;
            }

            File completePath = new File(rootDir, enteredDir);
            if (completePath.exists()) {
                //TODO: auth
                currentDir += new_dir + File.separator;

                status = STATUS_LOGGEDIN;
                serverMsg = "Changed working directory to " + completePath.getCanonicalPath();
            }
            else {
                status = STATUS_ERROR;
                System.out.println("DIR STRING: " + completePath);
                serverMsg = "Directory does not exist.";
            }

        }
        else {
            status = STATUS_ERROR;
            serverMsg = "You are not logged in. Please log in using the USER command.";
        }
    }

    public void KILL() {
        if (isLoggedIn) {

        }
        else {
            status = STATUS_ERROR;
            serverMsg = "You are not logged in. Please log in using the USER command.";
        }
    }

    public void NAME() {
        if (isLoggedIn) {
            System.out.println("Rename command");
            status = STATUS_SUCCESS;
            serverMsg = "Rename command sent to server!";
        }
        else {
            status = STATUS_ERROR;
            serverMsg = "You are not logged in. Please log in using the USER command.";
        }
    }

    public void DONE() throws IOException {
        status = STATUS_SUCCESS;
        serverMsg = "Connection closed.";
        outToClient.writeBytes(status + serverMsg + "\n");
        inFromClient.close();
        outToClient.close();
        clientSocket.close();
    }

    public void RETR() {
        if (isLoggedIn) {
            System.out.println("Request command");
            status = STATUS_SUCCESS;
            serverMsg = "Request command sent to server!";
        }
        else {
            status = STATUS_ERROR;
            serverMsg = "You are not logged in. Please log in using the USER command.";
        }
    }

    public void STOR() {
        if (isLoggedIn) {
            System.out.println("Store command");
            status = STATUS_SUCCESS;
            serverMsg = "Store command sent to server!";
        }
        else {
            status = STATUS_ERROR;
            serverMsg = "You are not logged in. Please log in using the USER command.";
        }
    }

    public String[] getUserInfo() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(LOGIN_DB));
        String[] userInfo;

        do {
            userInfo = br.readLine().split(",");
        } while (!userInfo[0].equals(userId));

        return userInfo;
    }

}