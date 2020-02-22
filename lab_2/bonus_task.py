import os
import platform
import subprocess

if platform.system() == "Windows":
    while True:
        print("Welcome to the command interpreter that understands and properly reacts to the following commands:")
        print("\t a) ping - used to test the ability of the source computer to reach a specified destination computer;")
        print("\t b) echo - used to display line of text/string that are passed as an argument;")
        print("\t c) login - view user account information;")
        print("\t d) list - lists all files and subdirectories contained in a specific directory;")
        print("\t e) msg - used to send a message to one or more users on the network")
        print("\t f) file - displayed the contents of a text file. ")
        print("\t g) exit - close the command interpreter and end the program.")

        command = input("Enter chosen command: ")
        if command == "ping":
            print("You choose the command ping.")
            parameters = input("Enter the parameter for this command (for example 127.0.0.1): ")
            subprocess.call(["ping", parameters])
            break

        elif command == "echo":
            print("You choose the command echo.")
            parameters = input("Enter the parameter for this command (for example Hello world): ")
            subprocess.call(["echo", parameters])
            break

        elif command == "login":
            print("You choose the command login.")
            print("List of available users:")
            subprocess.run('net user', check=True, shell=True)
            parameters = input("Enter the name of user: ")
            command = "net user " + parameters
            try:
                subprocess.run(command, check=True, shell=True)
            except:
                print("Wrong user name. Please, try again.")
            break

        elif command == "list":
            print("You choose the command list.")
            print("The contents of the current directory: ")
            subprocess.run('dir', check=True, shell=True)
            break

        elif command == "msg":
            print("You choose the command msg.")
            parameters = input("Enter ip of destination address of user (for example 127.0.0.1): ")
            command = "msg * /server:" + parameters
            parameters = input("Enter the message (for example Hello world): ")
            command = command + ' "' + parameters + '"'
            try:
                subprocess.run(command, check=True, shell=True)
            except:
                print("Wrong destination address of user. Please, try again.")
            break

        elif command == "file":
            print("You choose the command file.")
            parameters = input("Enter the name of text file (for example README.md): ")
            command = "type " + parameters
            try:
                subprocess.run(command, check=True, shell=True)
            except:
                print("Wrong name of file. Please, try again.")
            break

        elif command == "exit":
            subprocess.run('exit', check=True, shell=True)
            break
        else:
            print("Wrong command. Please, try again. Enter the command exit to close the application.")


