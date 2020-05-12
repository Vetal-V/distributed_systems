package lpi.client.mq;

import javax.jms.*;
import java.io.*;
import java.nio.file.Files;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import lpi.server.mq.FileInfo;

public class CommandHandler implements Closeable {

    private boolean loginCheck = false; //boolean variable for checking log in

    private javax.jms.Session session;
    private javax.jms.Session sessionListener;
    private javax.jms.Connection connection;
    private MessageConsumer messageConsumer;
    private MessageConsumer fileConsumer;

    CommandHandler(javax.jms.Session session, javax.jms.Connection connection, javax.jms.Session sessionListener){ //constructor of class CommandHandler
        this.session = session;
        this.connection = connection;
        this.sessionListener = sessionListener;
    }

    private synchronized javax.jms.Message getResponse(javax.jms.Message msg, String queueName) throws JMSException {
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

        consumer.close();
        producer.close();

        return replyMsg;
    }

    public static void responseError(Message responseError) throws JMSException { //method to handle error response
        System.out.println("Unexpected error!");
        if (responseError instanceof TextMessage) { // expect the text message as the content
            String content = ((TextMessage) responseError).getText(); // obtaining content
            System.out.println(content);
        }

        if (responseError instanceof MapMessage) { // expect the text message as the content
            String content = ((MapMessage) responseError).getString("message"); // obtaining content
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

    public void checkMsg() throws JMSException {
        Destination queue = sessionListener.createQueue("chat.messages");
        messageConsumer = sessionListener.createConsumer(queue);
        messageConsumer.setMessageListener(new MessageReceiver());
    }

    public void chechFile() throws JMSException {
        Destination queue = sessionListener.createQueue("chat.files");
        fileConsumer = sessionListener.createConsumer(queue);
        fileConsumer.setMessageListener(new FileReceiver());
    }


    void run() throws IOException, JMSException { //method run of class CommandHandler
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  //input buffer stream from user

        boolean ifLoop = true;
        while (ifLoop) { //loop for entering command
            System.out.println("\nEnter the command (help - list of available commands): ");
            String command = br.readLine(); //read command of user

            switch (command) {
                case "exit": //complete client execution
                    ifLoop = false;
                    if (checkLogin(loginCheck)) {
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
                    close(); // closing a session
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
                    javax.jms.Message pingResponse = getResponse(msg, "chat.diag.ping");

                    if (!(pingResponse instanceof TextMessage)){
                        System.out.println("Successfully ping.\n");
                    } else {
                        responseError(pingResponse);
                    }
                    break;

                case "echo": //command echo
                    System.out.println(" You choose the command echo.");
                    System.out.print(" Enter the echo text: ");
                    String echoText = br.readLine(); //read echo text from user

                    TextMessage msgEcho = session.createTextMessage(echoText); // a message that contains a string

                    javax.jms.Message echoResponse = getResponse(msgEcho, "chat.diag.echo");

                    if (echoResponse instanceof TextMessage) {
                        // expect the text message as the content
                        String content = ((TextMessage) echoResponse).getText(); // obtaining content
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

                    javax.jms.Message loginResponse = getResponse(loginRequest, "chat.login");
                    if (loginResponse instanceof MapMessage) {
                        if (((javax.jms.MapMessage) loginResponse).getBoolean("success")) {// successfully logged in
                            System.out.println(((javax.jms.MapMessage) loginResponse).getString("message"));
                            loginCheck = true;

                            checkMsg();
                            chechFile();

                        } else { // user failed to login, check the "message" for details.
                            loginCheck = false;
                            System.out.println("Failed to login: " + ((javax.jms.MapMessage) loginResponse).getString("message"));
                        }
                    } else
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
                        javax.jms.Message listResponse = getResponse(msgList, "chat.listUsers");
                        Serializable objList = ((ObjectMessage)listResponse).getObject();
                        if (objList != null && objList instanceof String[]) {
                            String[] users = (String[])objList;
                            System.out.print("List of user names: ");
                            for (String s : users) System.out.print(s + " ");
                        } else { // no, there’s not a list of objects, handle error.
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

                        javax.jms.Message responseMsg = getResponse(msgUser, "chat.sendMessage");

                        if (responseMsg instanceof MapMessage){
                            // print success message or error
                            System.out.println(((javax.jms.MapMessage) responseMsg).getString("message"));
                            if (!((javax.jms.MapMessage) responseMsg).getBoolean("success")) {
                                // when error
                                System.out.println("Please retry!");
                            }
                            System.out.println();
                        } else {
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
                        if (!file.isFile()) {
                            System.out.println("Incorrect file path or it is not a file.\n");
                            return;
                        }

                        javax.jms.ObjectMessage fileObj = session.createObjectMessage();

                        // convert a file to an byte array
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        
                        // form a FileInfo to send
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.setReceiver(fileReceiver);
                        fileInfo.setFilename(fileName);
                        fileInfo.setFileContent(fileContent);

                        fileObj.setObject(fileInfo);

                        javax.jms.Message response = getResponse(fileObj, "chat.sendFile");

                        if (response instanceof javax.jms.MapMessage){
                            // print success message or error
                            System.out.println(((javax.jms.MapMessage) response).getString("message"));
                            if (!((javax.jms.MapMessage) response).getBoolean("success")) {
                                // when error
                                System.out.println("Unexpected error sending file.");
                            }
                            System.out.println();
                        } else {
                            responseError(response);
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
    public void close() {
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

class MessageReceiver implements MessageListener {
    @Override public void onMessage(javax.jms.Message message) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if(message instanceof javax.jms.MapMessage) {
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

class FileReceiver implements MessageListener {
    @Override
    public void onMessage(javax.jms.Message message) {
        String folderPath = "./receiveFile";

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (message instanceof ObjectMessage) {
            javax.jms.ObjectMessage objMsg = (javax.jms.ObjectMessage)message;
            try {
                FileInfo fileInfo = (FileInfo) objMsg.getObject();

                System.out.println("You have a new file:");
                System.out.println(" File sender: " + fileInfo.getSender() + "");
                System.out.println(" Name of file: " + fileInfo.getFilename());

                // check if there is a folder to save the files
                File folder = new File(folderPath);
                if (!folder.exists()) {
                    folder.mkdir();
                }

                fileInfo.saveFileTo(folder);
                System.out.println(" File " + fileInfo.getFilename() + " was saved at path " + folderPath + "/" + fileInfo.getFilename());

            } catch (JMSException | IOException error) {
                System.out.println(error.getMessage());
            }
        }
    }
}

