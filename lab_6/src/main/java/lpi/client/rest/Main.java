package lpi.client.rest;

public class Main{
    public static void main(String[] args) {
        try {
            Connect client = new Connect(args); //create object of class Connect
            client.run(); //run method of class Connect
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
