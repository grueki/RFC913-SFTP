package Server;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
    File fileToRename;
    File fileToSend;
    boolean renameReady = false;
    boolean retrieveReady = false;
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
        String[] clientCmd = new String[2];

        while ((clientMsg = inFromClient.readLine()) != null) {
            int index =  clientMsg.contains(" ") ? clientMsg.indexOf(" ") : clientMsg.length();
            clientCmd[0] = clientMsg.substring(0, index);
            clientCmd[1] = clientMsg.substring(clientMsg.indexOf(" ") + 1);

            try {
                switch (clientCmd[0].toUpperCase()) {
                    case "USER":
                        if (!retrieveReady) {
                            USER(clientCmd[1]);
                            break;
                        }
                    case "ACCT":
                        if (!retrieveReady) {
                            ACCT(clientCmd[1]);
                            break;
                        }
                    case "PASS":
                        if (!retrieveReady) {
                            PASS(clientCmd[1]);
                            break;
                        }
                    case "TYPE":
                        if (!retrieveReady) {
                            TYPE(clientCmd[1]);
                            break;
                        }
                    case "LIST":
                        if (!retrieveReady) {
                            LIST(clientCmd[1]);
                            break;
                        }
                    case "CDIR":
                        if (!retrieveReady) {
                            CDIR(clientCmd[1]);
                            break;
                        }
                    case "KILL":
                        if (!retrieveReady) {
                            KILL(clientCmd[1]);
                            break;
                        }
                    case "NAME":
                        if (!retrieveReady) {
                            NAME(clientCmd[1]);
                            break;
                        }
                    case "TOBE":
                        if (renameReady) {
                            TOBE(clientCmd[1]);
                            break;
                        }
                    case "DONE":
                        DONE();
                        return;
                    case "RETR":
                        if (!retrieveReady) {
                            RETR(clientCmd[1]);
                            break;
                        }
                    case "SEND":
                        if (retrieveReady) {
                            SEND();
                            break;
                        }
                    case "STOP":
                        if (retrieveReady) {
                            STOP();
                            break;
                        }
                    case "STOR":
                        if (!retrieveReady) {
                            STOR(clientCmd[1]);
                            break;
                        }
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

            if (dirToList.exists()) {
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
                        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
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
                serverMsg = dirToList + " does not exist. Please try again.";
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

    public void KILL(String file_spec) {
        if (isLoggedIn) {
            File fileToDelete;
            if (Paths.get(file_spec).isAbsolute()) {
                fileToDelete = new File(file_spec);
            }
            else {
                fileToDelete = new File(rootDir, currentDir + File.separator + file_spec);
            }

            if(fileToDelete.exists()) {
                boolean gotDeleted = fileToDelete.delete();

                if (gotDeleted) {
                    status = STATUS_SUCCESS;
                    serverMsg = file_spec + " deleted";
                }
                else {
                    status = STATUS_ERROR;
                    serverMsg = "File not deleted";
                }
            }
            else {
                status = STATUS_ERROR;
                serverMsg = "File not deleted because it doesn't exist.";
            }
        }
        else {
            status = STATUS_ERROR;
            serverMsg = "You are not logged in. Please log in using the USER command.";
        }
    }

    public void NAME(String file_spec) {
        if (isLoggedIn) {
            File fullPath;
            if (Paths.get(file_spec).isAbsolute()) {
                fullPath = new File(file_spec);
            }
            else {
                fullPath = new File(rootDir, currentDir + File.separator + file_spec);
            }

            if(fullPath.exists()) {
                fileToRename = fullPath;
                status = STATUS_SUCCESS;
                serverMsg = file_spec + "File exists. Use the TOBE command next to rename " + file_spec;
                renameReady = true;
            }
            else {
                status = STATUS_ERROR;
                serverMsg = "Can't find file " + file_spec;
            }

        }
        else {
            status = STATUS_ERROR;
            serverMsg = "You are not logged in. Please log in using the USER command.";
        }
    }

    public void TOBE(String new_name) {
        if (isLoggedIn) {
            if (renameReady) {
                File fullPath;
                if (Paths.get(new_name).isAbsolute()) {
                    fullPath = new File(new_name);
                } else {
                    fullPath = new File(rootDir, currentDir + File.separator + new_name);
                }
                boolean gotRenamed = fileToRename.renameTo(fullPath);

                if (gotRenamed) {
                    status = STATUS_SUCCESS;
                    serverMsg = fileToRename + " got renamed to " + new_name;
                    renameReady = false;
                } else {
                    status = STATUS_ERROR;
                    serverMsg = "File didn't get renamed.";
                }
            }
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

    public void RETR(String retrieve_path) {
        if (isLoggedIn) {
            File fullPath;
            if (Paths.get(retrieve_path).isAbsolute()) {
                fullPath = new File(retrieve_path);
            } else {
                fullPath = new File(rootDir, currentDir + File.separator + retrieve_path);
            }
            if(fullPath.exists()) {
                fileToSend = fullPath;
                retrieveReady = true;
                status = String.valueOf(fullPath.length());
                serverMsg = " bytes will be sent. Respond with SEND command to proceed with retrieving " + retrieve_path + ", or STOP command to cancel transfer.";
            }
            else {
                status = STATUS_ERROR;
                serverMsg = retrieve_path + " doesn't exist. Please try again.";
            }
        }
        else {
            status = STATUS_ERROR;
            serverMsg = "You are not logged in. Please log in using the USER command.";
        }
    }

    public void STOP() {
        status = STATUS_SUCCESS;
        serverMsg = "RETR of file " + fileToSend + " has been aborted.";
        retrieveReady = false;
    }

    public void SEND() throws IOException {
        String line;
        outToClient.writeBytes(fileToSend.toPath().getFileName().toString()+"\n");

        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileToSend));

        while ((line = bufferedReader.readLine()) != null) {
            outToClient.writeBytes(line + "\n");
        }

        outToClient.writeBytes("END\n");

        bufferedReader.close();

        status = STATUS_SUCCESS;
        serverMsg = fileToSend.toPath().getFileName().toString() + " has been saved to the client!";

        retrieveReady = false;
    }

    public void STOR(String stor_cmd) throws IOException {
        if (isLoggedIn) {
            String[] stor_args = stor_cmd.split("\\s+");

            if (stor_args.length >= 2 ) {
                String filename = null;
                String methodName = null;
                BufferedWriter bufferedWriter = null;
                boolean badInput = false;

                switch (stor_args[0].toUpperCase()) {
                    case "NEW":
                        methodName = "new";
                        filename = inFromClient.readLine();
                        if (Files.exists(Paths.get(filename))) {
                            int index = 0;

                            while (Files.exists(Paths.get(filename + "_" + index))) {
                                index++;
                            }
                            filename = filename + "_" + index;
                        }
                        bufferedWriter = new BufferedWriter(new FileWriter(filename));
                        break;
                    case "OLD":
                        methodName = "overwrite";
                        filename = inFromClient.readLine();
                        bufferedWriter = new BufferedWriter(new FileWriter(filename));
                        break;
                    case "APP":
                        methodName = "append";
                        filename = inFromClient.readLine();
                        bufferedWriter = new BufferedWriter(new FileWriter(filename, true));
                        break;
                    default:
                        badInput = true;
                        status = STATUS_ERROR;
                        serverMsg = "Please write your STOR command in the format 'STOR { NEW | OLD | APP } filename' (e.g. STOR NEW myfile.txt)";
                        break;
                }
                if (!badInput) {
                    String line;
                    while (!"END".equals(line = inFromClient.readLine())) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                    }

                    bufferedWriter.close();

                    status = STATUS_SUCCESS;
                    serverMsg = filename + " has been stored in the server using the " + methodName + " method.";
                }

            } else {
                status = STATUS_ERROR;
                serverMsg = "Please write your STOR command in the format 'STOR { NEW | OLD | APP } filename' (e.g. STOR NEW myfile.txt)";
            }
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