package lpi.client.mq;

import javax.jms.*;
import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import lpi.server.mq.FileInfo;

public class CommandHandler implements Closeable {

    private boolean loginCheck = false; //boolean variable for checking log in

    private javax.jms.Session session; //session for current user
    private javax.jms.Session sessionListener; //session for check file and msg
    private javax.jms.Connection connection; //current connection
    private MessageConsumer messageConsumer; //MessageConsumer for get msg
    private MessageConsumer fileConsumer; //MessageConsumer for get file
    private String currentUsername;  //username of current user

    private Instant lastAction; //instant for get time last action

    CommandHandler(javax.jms.Session session, javax.jms.Connection connection, javax.jms.Session sessionListener){ //constructor of class CommandHandler
        this.session = session;
        this.connection = connection;
        this.sessionListener = sessionListener;
    }

    private synchronized javax.jms.Message getResponse(javax.jms.Message msg, String queueName) throws JMSException { //method for sending and receiving response
        // create an object specifying the Destination to which the message will be sent:
        javax.jms.Destination targetQueue = session.createQueue(queueName);
        // create an object specifying the Destination where the response will be received:
        javax.jms.Destination replyQueue = this.session.createTemporaryQueue();
        // create a producer on targetQueue, which will allow to send the request message:
        javax.jms.MessageProducer producer = session.createProducer(targetQueue);
        // create a consumer on replyQueue, which will allow to receive the answer:
        javax.jms.MessageConsumer consumer = session.createConsumer(replyQueue);
        msg.setJMSReplyTo(replyQueue);  // specify JMSReplyTo attribute:
        producer.send(msg); // send the message using producer:
        javax.jms.Message replyMsg = consumer.receive(1500); // await the reply using consumer:

        consumer.close(); //close MessageProducer consumer
        producer.close(); //close MessageProducer producer

        return replyMsg;
    }

    public static void responseError(Message responseError) throws JMSException { //method to handle error response
        System.out.println("Unexpected error!");
        if (responseError instanceof TextMessage) { //check the text message as the content
            String content = ((TextMessage) responseError).getText(); //get content
            System.out.println(content);
        }

        if (responseError instanceof MapMessage) { //check the text message as the content
            String content = ((MapMessage) responseError).getString("message"); //get content
            System.out.println(content);
        }
        System.out.println();
    }

    boolean checkLogin(boolean loginCheck){ //method to check the logging of user
        if (loginCheck)
            return true;
        else {
            System.out.println("This command requires login first.");
            return false;
        }
    }

    public void checkMsg() throws JMSException { //check new messages
        Destination queue = sessionListener.createQueue("chat.messages");
        messageConsumer = sessionListener.createConsumer(queue);
        messageConsumer.setMessageListener(new MessageReceiver()); //implement MessageListener of class MessageReceiver
    }

    public void chechFile() throws JMSException {
        Destination queue = sessionListener.createQueue("chat.files");
        fileConsumer = sessionListener.createConsumer(queue);
        fileConsumer.setMessageListener(new FileReceiver()); //implement MessageListener of class FileReceiver
    }

    public void checkTimeOut(){
        Thread threadAFK = new Thread(() -> {
            while (true) { //wait 5 min
                try {
                    Thread.sleep(20 *  1000);

                    Instant instantNow = Instant.now();
                    Duration timeElapsed = Duration.between(lastAction, instantNow); //calculate the difference between instant

                    if (timeElapsed.toMinutes() >= 5) { //set time to begin AFK mode
                        break;
                    }

                    Message msg = session.createMessage(); // an empty message
                    getResponse(msg, "chat.diag.ping"); //ping to server for threatAFK
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            System.out.println("You are AFK. Message about is successfully send to other user.");
            try {
                //get list of active user
                String[] users = new String[0];
                Message msgList = session.createMessage();
                Message response = getResponse(msgList, "chat.listUsers");

                if (response instanceof ObjectMessage) { //handle response of list users
                    Serializable obj = ((ObjectMessage) response).getObject();
                    if (obj instanceof String[]) {
                        users = (String[]) obj;
                    }
                }

                for (String user : users) { //send message to all active user
                    if (user.equals(currentUsername))
                        continue;

                    while (true) {
                        String messageContent = "Sorry, I'm AFK, will answer ASAP";

                        MapMessage msg = session.createMapMessage();
                        msg.setString("receiver", user);
                        msg.setString("message", messageContent);

                        response = getResponse(msg, "chat.sendMessage");
                        if (response instanceof MapMessage) {
                            // when the message is successfully sent
                            if (((MapMessage) response).getBoolean("success"))
                                break;
                        }
                    }
                }

                loginCheck = false;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        threadAFK.setDaemon(true); //create a daemon thread
        threadAFK.start(); //start thread
    }

    void run() throws IOException, JMSException { //method run of class CommandHandler
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  //input buffer stream from user

        boolean ifLoop = true;
        while (ifLoop) { //loop for entering command
            System.out.println("\nEnter the command (help - list of available commands): ");
            String command = br.readLine(); //read command of user
            lastAction = Instant.now();
            switch (command) {
                case "exit": //complete client execution
                    ifLoop = false;
                    if (checkLogin(loginCheck)) { //log out user on server
                        Message msg = session.createMessage(); // an empty message
                        Message response = getResponse(msg, "chat.exit");

                        if (!(response instanceof TextMessage)) {
                            System.out.println("Successful logout.");
                        } else {
                            responseError(response);
                        }

                        if (messageConsumer != null)
                            messageConsumer.close();

                        if (fileConsumer != null)
                            fileConsumer.close();
                    }
                    loginCheck = false;
                    close(); // closing a connection
                    break;

                case "ping": //command ping
                    System.out.println(" You choose the command ping.");

                    javax.jms.Message msg = null; // an empty message
                    try {
                        msg = session.createMessage();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }

                    assert msg != null;
                    javax.jms.Message pingResponse = getResponse(msg, "chat.diag.ping"); //ping to server

                    if (!(pingResponse instanceof TextMessage)){ //successfully ping
                        System.out.println("Successfully ping.");
                    } else { //handle error
                        responseError(pingResponse);
                    }
                    break;

                case "echo": //command echo
                    System.out.println(" You choose the command echo.");
                    System.out.print(" Enter the echo text: ");
                    String echoText = br.readLine(); //read echo text from user

                    TextMessage msgEcho = session.createTextMessage(echoText); //message that contains a string echo

                    javax.jms.Message echoResponse = getResponse(msgEcho, "chat.diag.echo");

                    if (echoResponse instanceof TextMessage) { //check the text message as the content
                        String content = ((TextMessage) echoResponse).getText(); //get content
                        System.out.println("Received from server: " + content);
                    } else { //unexpected error
                        System.out.println("Unexpected message type: " + echoResponse.getClass() );
                    }
                    break;

                case "login": //command login
                    System.out.println(" You choose the command login.");
                    System.out.print(" Enter the login: ");
                    String loginName = br.readLine(); //read login from user
                    System.out.print(" Enter the password: ");
                    String loginPass = br.readLine(); //read password from user

                    javax.jms.MapMessage loginRequest = session.createMapMessage();
                    loginRequest.setString("login", loginName);
                    loginRequest.setString("password", loginPass);

                    javax.jms.Message loginResponse = getResponse(loginRequest, "chat.login"); //login on server
                    if (loginResponse instanceof MapMessage) {
                        if (((javax.jms.MapMessage) loginResponse).getBoolean("success")) {// successfully login
                            System.out.println(((javax.jms.MapMessage) loginResponse).getString("message"));
                            loginCheck = true;
                            currentUsername = loginName;
                            checkMsg(); //start checking msg
                            chechFile(); //start checking files
                            checkTimeOut(); //start checking AFK
                        } else { // user failed to login, check the "message" for details.
                            loginCheck = false;
                            System.out.println("Failed to login: " + ((javax.jms.MapMessage) loginResponse).getString("message"));
                        }
                    } else //handle error
                        responseError(loginResponse);
                    break;

                case "list": //command list
                    System.out.println("You choose the command list.");
                    if (checkLogin(loginCheck)) {
                        javax.jms.Message msgList = null; // an empty message
                        try {
                            msgList = session.createMessage();
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }

                        assert msgList != null;
                        javax.jms.Message listResponse = getResponse(msgList, "chat.listUsers"); //list command to server
                        Serializable objList = ((ObjectMessage)listResponse).getObject(); //serialize response
                        if (objList != null && objList instanceof String[]) {
                            String[] users = (String[])objList;
                            System.out.print("List of user names: ");
                            for (String s : users) System.out.print(s + " ");
                        } else { //handle error.
                            System.out.println("Unexpected content: " + objList);
                        }
                    }
                    break;

                case "msg": //command msg
                    System.out.println("You choose the command msg.");
                    if (checkLogin(loginCheck)) {
                        System.out.print("Enter the login of receiver: ");
                        String msgLogin = br.readLine(); //read login of receiver from user
                        System.out.print("Enter the message: ");
                        String msgText = br.readLine(); //read message from user

                        javax.jms.MapMessage msgUser = session.createMapMessage();
                        msgUser.setString("receiver", msgLogin);
                        msgUser.setString("message", msgText);

                        javax.jms.Message responseMsg = getResponse(msgUser, "chat.sendMessage"); //send message to server

                        if (responseMsg instanceof MapMessage){
                            System.out.println(((javax.jms.MapMessage) responseMsg).getString("message")); //print response message
                            if (!((javax.jms.MapMessage) responseMsg).getBoolean("success")) { //check existence of error
                                System.out.println("Please retry!");
                            }
                            System.out.println();
                        } else { //handle error
                            responseError(responseMsg);
                        }
                    }
                    break;

                case "file": //command file
                    if (checkLogin(loginCheck)) {
                        System.out.println("You choose the command file.");
                        System.out.print("Enter the login of receiver: ");
                        String fileReceiver = br.readLine(); //read login of receiver from user
                        System.out.print("Enter the name of file: ");
                        String fileName = br.readLine(); //read login of receiver from user
                        System.out.print("Enter the path to file: ");
                        String filePath = br.readLine(); //read the path to file from user

                        File file = new File(filePath);
                        if (!file.isFile()) { //check existence of file
                            System.out.println("Incorrect file path or it is not a file.");
                            return;
                        }

                        javax.jms.ObjectMessage fileObj = session.createObjectMessage(); //create object for file

                        byte[] fileContent = Files.readAllBytes(file.toPath()); //content of file to byte array

                        //set field of FileInfo
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.setReceiver(fileReceiver);
                        fileInfo.setFilename(fileName);
                        fileInfo.setFileContent(fileContent);

                        fileObj.setObject(fileInfo); //set object for file

                        javax.jms.Message response = getResponse(fileObj, "chat.sendFile"); //send file to server

                        if (response instanceof javax.jms.MapMessage){
                            System.out.println(((javax.jms.MapMessage) response).getString("message")); //print response message
                            if (!((javax.jms.MapMessage) response).getBoolean("success")) { //check existence of error
                                System.out.println("Unexpected error sending file.");
                            }
                            System.out.println();
                        } else {
                            responseError(response); //handle error
                        }
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
                    break;

                default: //execute when wrong command
                    System.out.println("\nWrong command. Please, try again.");
            }
        }
    }

    @Override
    public void close() { //method of Closeable
        if (session != null){
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        if (connection != null){
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Connection close.");
        System.exit(0);
    }
}

class MessageReceiver implements MessageListener { //class of MessageListener for MessageReceiver
    @Override public void onMessage(javax.jms.Message message) {
        try { //time out 1 second
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if(message instanceof javax.jms.MapMessage) { //message exist
                javax.jms.MapMessage mapMsg = (MapMessage) message;
                String messageText = null;
                String sender = null;

                sender = mapMsg.getString("sender");
                messageText = mapMsg.getString("message");

                System.out.println("You have a new message:");
                System.out.println(" Sender: " + sender);
                System.out.println(" Message: " + messageText);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

class FileReceiver implements MessageListener { //class of MessageListener for FileReceiver
    @Override
    public void onMessage(javax.jms.Message message) {
        String folderPath = "./receiveFile";

        try { //time out 1 second
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (message instanceof ObjectMessage) { //file exist
            javax.jms.ObjectMessage objMsg = (javax.jms.ObjectMessage)message;
            try {
                FileInfo fileInfo = (FileInfo) objMsg.getObject();

                System.out.println("You have a new file:");
                System.out.println(" File sender: " + fileInfo.getSender() + "");
                System.out.println(" Name of file: " + fileInfo.getFilename());

                // check if there is a folder to save the files
                File folder = new File(folderPath);
                if (!folder.exists()) { //create folder if it don't exist
                    folder.mkdir();
                }

                fileInfo.saveFileTo(folder); //save file to folder
                System.out.println(" File " + fileInfo.getFilename() + " was saved at path " + folderPath + "/" + fileInfo.getFilename());

            } catch (JMSException | IOException error) {
                System.out.println(error.getMessage());
            }
        }
    }
}

