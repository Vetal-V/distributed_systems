import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.Socket;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    public static String defaultIp = "127.0.0.1"; //default ip address of server
    public static int defaultPort = 4321; //default port of server
    public static Socket clientSocket; // socket for communication with the server
    public static DataInputStream input; // socket reading stream
    public static DataOutputStream output; // socket writing stream

    public static byte[] serialize(Object obj) throws IOException { // method for object serialization
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    // method for byte array deserialization
    public static <T> T deserialize(byte[] data, int offset, Class<T> clazz) throws ClassNotFoundException, IOException {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(data, offset, data.length - offset);
             ObjectInputStream objectStream = new ObjectInputStream(stream)) {
            return (T) objectStream.readObject();
        }
    }

    public static void sleeper (int timeSl){ //method for implementing delays
        try {
            Thread.sleep(timeSl);
        } catch (Exception ignored) {}
    }

    public static void errorHandler (byte errCode){ //method for handling errors
        switch (errCode){
            case 100:
                System.out.println("Internal Server Error occurred. Usually that means that it’s not your fault (or you just sent something THAT bad that server was not expecting).");
                break;
            case 101:
                System.out.println("The transfer size is below 0 or above 10MB.");
                break;
            case 102:
                System.out.println("Server failed to deserialize content.");
                break;
            case 103:
                System.out.println("Server did not understand the command.");
                break;
            case 104:
                System.out.println("Incorrect number or content of parameters. Server expected something different (usually the stuff described above)");
                break;
            case 110:
                System.out.println("The specified password is incorrect (this login was used before with another password).");
                break;
            case 112:
                System.out.println("This command requires login first.");
                break;
            case 113:
                System.out.println("Failed to send the message or file. Either receiver does not exist or his pending message quota exceeded.");
                break;
            default:
                System.out.println("Unknown error: " + errCode);
        }
    }

    public static void main(String[] args) { //main method
        try {
            try {
                try { //try initialize socket connection from default ip and port
                    clientSocket = new Socket(defaultIp, defaultPort); //initializing the socket connection
                } catch (Exception ignored) {}

                try { //try initialize socket connection from user argument
                    defaultIp = args[0];
                    defaultPort = Integer.parseInt(args[1]);
                    clientSocket = new Socket(defaultIp, defaultPort); //initializing the socket connection
                } catch (Exception ignored) {}

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); //input buffer stream from user
                input = new DataInputStream(clientSocket.getInputStream()); //initialize socket reading stream
                output = new DataOutputStream(clientSocket.getOutputStream()); //initialize socket writing stream

                int ifLoop = 1;
                while (ifLoop == 1) { //loop for entering command
                    System.out.println("\nEnter the command (help - list of available commands): ");
                    String command = br.readLine(); //read command of user

                    switch (command){
                        case "exit": //complete client execution
                            ifLoop = 0;
                            break;
                        case "ping": //command ping
                            System.out.println("You choose the command ping.");
                            output.writeInt(1); //sent size of buffer
                            output.writeByte(1); //sent ID
                            output.flush();  //providing write data to a buffered stream

                            sleeper(500); //delay

                            int pingBuffer = input.readInt(); //receive size of buffer
                            byte pingAns = input.readByte(); //receive answer
                            System.out.println("Server answer: " + pingAns);
                            if (pingAns == 2) //successful ping
                                System.out.println("Ping successful.");
                            else
                                errorHandler(pingAns); //error handler
                            break;
                        case "echo": //command echo
                            System.out.println("You choose the command echo.");
                            System.out.print("Enter the echo text: ");
                            String echoText = br.readLine(); //read echo text from user
                            byte[] echoBytes = echoText.getBytes();

                            output.writeInt(echoText.length() + 1);  //sent size of buffer
                            output.writeByte(3); //sent ID
                            output.write(echoBytes); //sent byte array
                            output.flush(); //providing write data to a buffered stream

                            sleeper(500); //delay

                            int echoBuffer = input.readInt(); //receive size of buffer
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[echoBuffer];
                            baos.write(buffer, 0 , input.read(buffer)); //receive echo text
                            byte[] result = baos.toByteArray();

                            String res = new String(result, StandardCharsets.US_ASCII);
                            System.out.println("Received from server: " + res);
                            break;

                        case "login": //command login
                            System.out.println("You choose the command login.");
                            String[] loginAr = new String[2]; //string array for login
                            System.out.print("Enter the login: ");
                            loginAr[0] = br.readLine(); //read login from user
                            System.out.print("Enter the password: ");
                            loginAr[1] = br.readLine(); //read password from user

                            byte[] loginSerial = serialize(loginAr); //serialize string array

                            output.writeInt(loginSerial.length + 1); //sent size of buffer
                            output.writeByte(5); //sent ID
                            output.write(loginSerial); //sent serialized byte array
                            output.flush(); //providing write data to a buffered stream

                            sleeper(500); //delay

                            int loginBuffer = input.readInt(); //receive size of buffer
                            byte loginAns = input.readByte(); //receive answer
                            System.out.println("Server answer: ");
                            if (loginAns == 6) //check whether new user
                                System.out.println("New user. Successful registration.");
                            else if (loginAns == 7) //check whether just login
                                System.out.println("Successful login.");
                            else
                                errorHandler(loginAns); //error handler
                            if (loginAns == 6 || loginAns == 7){
                                NewThread newTread = new NewThread(clientSocket);
                                newTread.start();
                            }
                            break;

                        case "list": //command list
                            System.out.println("You choose the command list.");
                            output.writeInt(1); //sent size of buffer
                            output.writeByte(10); //sent ID
                            output.flush(); //providing write data to a buffered stream

                            sleeper(500); //delay

                            int listBuffer = input.readInt(); //receive size of buffer
                            ByteArrayOutputStream baoss = new ByteArrayOutputStream();
                            byte[] bufferl = new byte[listBuffer];
                            baoss.write(bufferl, 0 , input.read(bufferl)); //receive list of active users

                            if (listBuffer == 1) { //error handler
                                byte errorCode = input.readByte();
                                errorHandler(errorCode);
                            } else { //process receive message
                                String[] ress = null;
                                try { // try to deserialize list
                                    ress = deserialize(bufferl, 0, String[].class);
                                } catch (Exception ex) {
                                    System.out.println("Deserialize error. Please, try again.");
                                }
                                System.out.print("List of active user name: ");
                                assert ress != null;
                                for (String s : ress) System.out.print(s + " "); //print active devices
                            }
                            break;

                        case "msg": //command msg
                            System.out.println("You choose the command msg.");
                            String[] msgAr = new String[2]; //string array for message
                            System.out.print("Enter the login of receiver: ");
                            msgAr[0] = br.readLine(); //read login of receiver from user
                            System.out.print("Enter the message: ");
                            msgAr[1] = br.readLine(); //read message from user

                            byte[] msgSerial = serialize(msgAr); //serialize  string array

                            output.writeInt(msgSerial.length + 1); //sent size of buffer
                            output.writeByte(15); //sent ID
                            output.write(msgSerial); //sent array for message
                            output.flush(); //providing write data to a buffered stream

                            sleeper(500); //delay

                            int msgBuffer = input.readInt(); //receive size of buffer
                            byte msgAns = input.readByte(); //receive answer
                            if (msgAns == 16 || msgAns == 26) //successful sending
                                System.out.println("Message sent successfully.");
                            else
                                errorHandler(msgAns); //error handler
                            break;

                        case "recMsg": //command receive message
                            System.out.println("You choose the command receive message.");
                            if (NewThread.countMsg == 0){ //no message
                                System.out.println("You don't have message.");
                            } else if(NewThread.countMsg == 1){ //one message
                                System.out.println("You have the message:");
                                System.out.println(" Sender: " + NewThread.senderMsg[0]);
                                System.out.println(" Message: " + NewThread.senderMsg[0]);
                                NewThread.countMsg = 0; //reset the counter
                            } else { //more then one message
                                System.out.println("You have messages:");
                                for(int i = 0; i < NewThread.countMsg; i++ ) {
                                    System.out.println(" Sender: " + NewThread.senderMsg[i]);
                                    System.out.println(" Message: " + NewThread.messageMsg[i] + "\n");
                                }
                                NewThread.countMsg = 0; //reset the counter
                            }
                            break;

                        case "file": //command file
                            System.out.println("You choose the command file.");
                            Object[] fileAr = new Object[3]; //object array for file sent
                            System.out.print("Enter the login of receiver: ");
                            fileAr[0] = br.readLine(); //read login of receiver from user
                            System.out.print("Enter the filename: ");
                            fileAr[1] = br.readLine(); //read filename from user
                            System.out.print("Enter the path to file: ");
                            String filePath = br.readLine(); //read the path to file from user
                            try{
                                byte[] b = Files.readAllBytes(Paths.get(filePath)); //transfer content of file to byte array
                                fileAr[2] = b;
                            } catch (IOException ignored) {}

                            byte[] fileSerial = serialize(fileAr); //serialize object array
                            try{
                                output.writeInt(fileSerial.length + 1); //sent size of buffer
                                output.writeByte(20); //sent ID
                                output.write(fileSerial); //sent object array to server
                                output.flush(); //providing write data to a buffered stream
                            } catch (IOException ignored) {}

                            sleeper(500); //delay

                            int fileBuffer = input.readInt(); //receive size of buffer
                            byte fileAns = input.readByte(); //receive answer
                            if (fileAns == 21) //successful sending
                                System.out.println("File sent successfully.");
                            else
                                errorHandler(fileAns); //error handler
                            break;

                        case "recFile": //command receive file
                            System.out.println("You choose the command receive file.");
                            if(NewThread.countFile == 0) //no file
                                System.out.println("You don't have waiting file.");
                            else if (NewThread.countFile == 1){ // one waiting file
                                System.out.println("You have waiting file:");
                                System.out.println("Sender of file: " + NewThread.senderFile[0]);
                                System.out.println("Filename: " + NewThread.nameFile[0]);
                                byte[] fileContByte = (byte[]) NewThread.contentFile[0];
                                String fileContStr = new String(fileContByte);
                                try (FileWriter writer = new FileWriter("receiveFile/" + NewThread.nameFile[0], false)) //try to write content to file
                                {
                                    writer.write(fileContStr); //print content to file
                                    writer.flush(); //providing write data to a buffered stream
                                }
                                catch(IOException ex) { //catch error
                                    System.out.println(ex.getMessage());
                                }
                                System.out.println("The content was written to a file " + NewThread.nameFile[0] + " at path /receiverFile/" + NewThread.nameFile[0]);
                                NewThread.countFile = 0; //reset the counter
                            } else { //more than one file
                                System.out.println("You have waiting files:");
                                for (int i = 0; i < NewThread.countFile; i++) {
                                    System.out.println("Sender of file: " + NewThread.senderFile[i]);
                                    System.out.println("Filename: " + NewThread.nameFile[i]);
                                    byte[] fileContByte = (byte[]) NewThread.contentFile[i];
                                    String fileContStr = new String(fileContByte);
                                    try ( FileWriter writer = new FileWriter("receiveFile/" + NewThread.nameFile[i], false)) //try to write content to file
                                    {
                                        writer.write(fileContStr); //print content
                                        writer.flush(); //providing write data to a buffered stream
                                    }
                                    catch(IOException ex) { //catch error
                                        System.out.println(ex.getMessage());
                                    }
                                    System.out.println("The content was written to a file " + NewThread.nameFile[i] + " at path /receiveFile/" + NewThread.nameFile[i]+".");
                                }
                                NewThread.countFile = 0;
                            }
                            break;

                        case "help": //help command
                            System.out.println("Available command:");
                            System.out.println(" exit - shutdown client;");
                            System.out.println(" ping - testing connection;");
                            System.out.println(" echo - testing sending message;");
                            System.out.println(" login - log in on server or create new user;");
                            System.out.println(" list - array with active user names;");
                            System.out.println(" msg - send message to user;");
                            System.out.println(" file - send file to user;");
                            System.out.println(" recMsg - receive message for login user;");
                            System.out.println(" recFile - receive file for login user.");
                            break;
                        default: //execute when wrong command
                            System.out.println("\nWrong command. Please, try again.");
                    }
                }
            } finally { //close open socket and streams
                System.out.println("Connection closed. Main thread.");
                clientSocket.close(); //close socket
                output.close(); //close socket writing stream
                input.close(); //close socket reading stream
            }
        } catch (IOException ignored) {}
    }
}


class NewThread extends Thread { //class for initialization new thread
    public static Socket clientSocket; // socket for communication with the server
    public static DataInputStream input; // socket reading stream
    public static DataOutputStream output; // socket writing stream

    public static String[] senderMsg = new String[50]; //string array for senders of message
    public static String[] messageMsg = new String[50]; //string array for send message
    public static String[] senderFile = new String[50]; //string array for senders of file
    public static String[] nameFile = new String[50]; //string array for names of files
    public static Object[] contentFile = new Object[50]; //object array content of files

    ArrayList <String> current = new ArrayList<>(); //array list of current user
    ArrayList <String> old = new ArrayList<>(); //array list of old user
    int count = 0;

    public static int countMsg = 0; //counter for receive message
    public static int countFile = 0;  //counter for receive file

    NewThread(Socket socketMain){ //constructor
        clientSocket = socketMain;
    }

    @Override
    public void run() { //method that called after initialization thread
        try {
            try {
                input = new DataInputStream(clientSocket.getInputStream()); //initialize socket reading stream
                output = new DataOutputStream(clientSocket.getOutputStream()); //initialize socket writing stream

                while (true) { //loop for checking messages, files and new users
                    //check new message
                    output.writeInt(1); //sent size of buffer
                    output.writeByte(25); //sent ID
                    output.flush(); //providing write data to a buffered stream

                    Main.sleeper(500); //delay

                    int recMsgBuffer = input.readInt(); //receive size of buffer
                    if (recMsgBuffer == 1) { //error or no message
                        byte recMsgAns = input.readByte(); //receive answer
                        if (recMsgAns != 26 && recMsgAns != 16) //no message
                            Main.errorHandler(recMsgAns); //error handler
                    } else { //message exist
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[recMsgBuffer];
                        baos.write(buffer, 0, input.read(buffer)); //receive message

                        String[] msgResult = null;
                        try { //try deserialize message
                            msgResult = Main.deserialize(buffer, 0, String[].class);
                        } catch (Exception ignored) {}
                        //print message about new message
                        countMsg++;
                        System.out.println("You have unread message: " + countMsg + ". To see the message enter the command recMsg.");
                        assert msgResult != null;
                        senderMsg[countMsg - 1] = msgResult[0]; //record to array
                        messageMsg[countMsg - 1] = msgResult[1];
                    }

                    //check new file
                    output.writeInt(1); //sent size of buffer
                    output.writeByte(30); //sent ID
                    output.flush(); //providing write data to a buffered stream

                    Main.sleeper(500); //delay

                    int recFileBuffer = input.readInt(); //receive size of buffer
                    if (recFileBuffer == 1) { //error or no waiting file
                        byte recFileAns = input.readByte(); //receive answer
                        if (recFileAns != 31) //no waiting file
                            Main.errorHandler(recFileAns); //error handler
                    } else { //waiting file exist
                        ByteArrayOutputStream baoss = new ByteArrayOutputStream();
                        byte[] fbuffer = new byte[recFileBuffer];
                        baoss.write(fbuffer, 0 , input.read(fbuffer)); //receive message

                        Object[] fileResult = null;
                        try { //try deserialize receiver object
                            fileResult = Main.deserialize(fbuffer, 0, Object[].class);
                        } catch (Exception ignored1) {}

                        countFile++;
                        System.out.println("You have waiting file: " + countFile + ". To see waiting file enter the command recFile.");
                        assert fileResult != null;
                        senderFile[countFile-1] = (String)fileResult[0];
                        nameFile[countFile-1] = (String)fileResult[1];
                        contentFile[countFile-1] = fileResult[2];
                    }

                    //check new or log out user
                    output.writeInt(1); //sent size of buffer
                    output.writeByte(10); //sent ID
                    output.flush(); //providing write data to a buffered stream

                    Main.sleeper(500); //delay

                    int sizeBuffer = input.readInt(); //receive size of buffer
                    ByteArrayOutputStream baoss = new ByteArrayOutputStream();
                    byte[] listBuffer = new byte[sizeBuffer];
                    baoss.write(listBuffer, 0 , input.read(listBuffer)); //receive list of active users

                    if (sizeBuffer == 1) { //error handler
                        byte errorCode = input.readByte();
                        Main.errorHandler(errorCode);
                    } else { //process receive message
                        String[] ress = null;
                        try { // try to deserialize list
                            ress = Main.deserialize(listBuffer, 0, String[].class);
                        } catch (Exception ignored) {}
                        //record users to array list on first iteration
                        assert ress != null;
                        if (count == 0){
                            for(String i: ress){
                                current.add(i);
                                old.add(i);
                            }
                        } else { //second and subsequent iterations
                            current.clear();
                            Collections.addAll(current, ress);

                            if(current.size() > old.size()){ //new user
                                current.removeAll(old);
                                System.out.println("The user " + current.get(0) + " connected to the server.");
                                current.clear(); //clear array list
                                old.clear();
                                for(String i: ress){
                                    current.add(i);
                                    old.add(i);
                                }

                            } else if (current.size() < old.size()){ //logout user
                                old.removeAll(current);
                                System.out.println("The user " + old.get(0) + " disconnected from server ");
                                old.clear(); //clear array list
                                current.clear();
                                for(String i: ress) {
                                    current.add(i);
                                    old.add(i);
                                }
                            }
                        }
                    }
                    if (count == 0) //detect first iteration
                        count++;
                }
            } finally { //close open socket and streams
                clientSocket.close(); //close socket
                output.close(); //close socket writing stream
                input.close(); //close socket reading stream
            }
        } catch (IOException ignored) {}
    }
}