## Distributed Systems Seminars: 6. MQ

1. To run server and client you need use `Java` version `1.8` or higher and `Maven` tool.

2. First of all you have to start the server. To do this, run the following command in console or terminal from folder with soap server. You can omit the argument, but also start server with your specify your ip address and port (for example):
    ```
    mvn clean compile exec:java -D exec.args="0.0.0.0 61616"
    ```
3. Then you can start the client. To do this, run the command below in console or terminal with ip and port of the server in folder with client (in this case):
    ```
    mvn clean compile exec:java -D exec.args="localhost 61616"
    ```
4. You can send the command to the server after running the client. List and description of the available commands can be seen by running the `help` command. Following commands are available for the client:
    
    - `exit` - shutdown client (no parameters);
    - `ping` - testing connection (no parameters):
        ```
        Enter the command (help - list of available commands):
        ping
         You choose the command ping.
        Received from server: Ping successful.
        ```
    - `echo` - testing sending message. After entering the command, the program will ask you to enter text of echo message:
        ```
        Enter the command (help - list of available commands):
        echo
         You choose the command echo.
         Enter the echo text: Hello world
        Received from server: ECHO: Hello world
        ```
    - `login` - log in on server or create new user. After entering the command, the program will ask you to enter login and password. New user will logged or user login his account:
        ```
        Enter the command (help - list of available commands):
        login
         You choose the command login.
         Enter the login: user1
         Enter the password: pass
         Registered successfully
        ```
    - `list` - array with active user names (no parameters);
    - `msg` - send message to user. After entering the command, the program will ask you to enter name of receiver and test of message. User with this name immediately receive notification about new message (this user must exist):
        ```
        Enter the command (help - list of available commands):
        msg
         You choose the command msg.
         Enter the login of receiver: user1
         Enter the message: Hi! How are you?
         The message was sent successfully.
      
         Enter the command (help - list of available commands):
          You have a new message:
           Sender: user1
           Message: Hi! How are you?
        ```
    - `file` - send file to user. After entering the command, the program will ask you to enter name of receiver, name of file and path to file (for example file.txt with the same relative path file.txt). User with this name immediately receive notification about waiting file:
        ```
        Enter the command (help - list of available commands):
        file
         You choose the command file.
         Enter the login of receiver: user2
         Enter the name of file: file.txt
         Enter the path to file: ./file.txt
         The file was sent successfully.
      
        Enter the command (help - list of available commands):
         You have a new file:
          File sender: user1
          Name of file: file.txt
          File file.txt was saved at path ./receiveFile/file.txt
        ```
      
5. Application answers that user is away to an incoming message when the user is not specifying any commands longer than for 5 minutes and a new message arrives:
    - To check it you need to log in to several accounts from different terminals:
    ``` 
    ~~~Terminal_1~~~
    Enter the command (help - list of available commands):
    login
     You choose the command login.
     Enter the login: user1
     Enter the password: pass
     Server answer:
    Registered successfully
    ```
    ```
    ~~~Terminal_2~~~
    Enter the command (help - list of available commands):
    login
     You choose the command login.
     Enter the login: user2
     Enter the password: pass
     Server answer:
    Registered successfully
    ```
   - Wait more five minutes is terminal 1, while execute command in terminar 2:
    ```
    ~~~Terminal_1~~~
    Enter the command (help - list of available commands):
    You are AFK. Message about is successfully send to other user.
    ```
    ```
    ~~~Terminal_2~~~
    Enter the command (help - list of available commands):
    You have a new message:
     Sender: user1
     Message: Sorry, I'm AFK, will answer ASAP
    ```