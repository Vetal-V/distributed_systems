## Distributed Systems Seminars: 4. SOAP Web Services

1. To run server and client you need use `Java` version `1.8` or higher and `Maven` tool.

2. First of all you have to start the server. To do this, run the following command in console or terminal from folder with soap server. You can omit the argument, but also start server with your specify your port (for example):
    ```
    mvn clean compile exec:java -D exec.args="0.0.0.0 4321"
    ```
3. Then you can start the client. To do this, run the command below in console or terminal with url `WSDL` Schema of the server in folder with client (for example):
    ```
    mvn clean compile exec:java -D exec.args="http://localhost:4321/chat?wsdl"
    ```
4. You can send the command to the server after running the client. List and description of the available commands can be seen by running the `help` command. Following commands are available for the client:
    
    - `exit` - shutdown client (no parameters);
    - `ping` - testing connection (no parameters);
    - `echo` - testing sending message. After entering the command, the program will ask you to enter text of echo message:
        ```
        Enter the command (help - list of available commands):
        echo
         You choose the command echo.
         Enter the echo text: Hello world
        Received from server: I'm glad to echo your message "Hello world", sir!
        ```
    - `login` - log in on server or create new user. After entering the command, the program will ask you to enter login and password. New user will logged or user login his account:
        ```
        Enter the command (help - list of available commands):
        login
         You choose the command login.
         Enter the login: user1
         Enter the password: pass
         Successful login
        ```
    - `list` - array with active user names (no parameters);
    - `msg` - send message to user. After entering the command, the program will ask you to enter name of receiver and test of message. User with this name immediately receive notification about new message:
        ```
        Enter the command (help - list of available commands):
        msg
         You choose the command msg.
         Enter the login of receiver: user2
         Enter the message: Hi! How are you?
         Message sent successfully.
        ```
    - `file` - send file to user. After entering the command, the program will ask you to enter name of receiver, name of file and path to file (for example file.txt with the same relative path file.txt). User with this name immediately receive notification about waiting file:
        ```
        Enter the command (help - list of available commands):
        file
         You choose the command file.
         Enter the login of receiver: user2
         Enter the path to file: file.txt
         File sent successfully.
        ```
    - `recMsg` - receive message for login user (after logging in to the user2 account, I received a notification about the file and the messages received. Account user2 must be created):
        ```
        Enter the command (help - list of available commands):
        recMsg
         You choose the command receive message.
         You have messages:
          Sender: user1
          Message: Hi! How are you?
        ```
    - `recFile` - receive file for login user:
        ```
        Enter the command (help - list of available commands):
        You have waiting file: 1. To see waiting file enter the command recFile.
        You have unread message: 1. To see the message enter the command recMsg.
        recFile
         You choose the command receive file.
         You have waiting file:
          Sender of file: user1
          Filename: file.txt
         The content was written to a file file.txt at path ./receiverFile/file.txt
        ```
      
5. Application prints when some new user logs in to the server and logs out of the server:
    - To check it you need to log in to several accounts from different terminals:
    ``` 
    ~~~Terminal_1~~~
    Enter the command (help - list of available commands):
    login
     You choose the command login.
     Enter the login: user1
     Enter the password: pass
     Server answer:
    Successful login.
    ```
    ```
    ~~~Terminal_2~~~
    Enter the command (help - list of available commands):
    login
     You choose the command login.
     Enter the login: user2
     Enter the password: pass
     Server answer:
    Successful login.
    ```
    ```
    ~~~Terminal_1~~~
    Enter the command (help - list of available commands):
    The user user2 connected to the server.
    exit
    ```
    ```
    ~~~Terminal_2~~~
    Enter the command (help - list of available commands):
    The user user1 disconnected from server
    ```

6. Also application send a welcome message to every user that logs in:
    - To check it you need to log in to several accounts from different terminals:
    ```
    ~~~Terminal_1~~~
    Enter the command (help - list of available commands):
    login
     You choose the command login.
     Enter the login: user1
     Enter the password: pass
     Server answer:
    Successful login.
    ```
    ```
    ~~~Terminal_2~~~
    Enter the command (help - list of available commands):
    login
     You choose the command login.
     Enter the login: user2
     Enter the password: pass
     Server answer:
    Successful login.
    ```
    ```
    ~~~Terminal_1~~~
    Enter the command (help - list of available commands):
    The user user2 connected to the server. Welcome message sent successfully.
    ```
    ```
    ~~~Terminal_2~~~
    Enter the command (help - list of available commands):
    You have unread message: 1. To see the message enter the command recMsg.
    recMsg
    You choose the command receive message.
    You have the message:
     Sender: user2
     Message: Hello there, user1.
    ```