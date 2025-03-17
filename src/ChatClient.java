package src;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * The ChatClient class connects to the ChatServer class and allows users to send and receive messages in real-time.
 * It prompts the user to enter a unique username before connecting.
 * The client can send messages to the server, which will display them to all connected clients.
 * Each client can also receive messages from other clients.
 * 
 * @author Shauna Lachelier
 */

public class ChatClient {

  private static Socket socket;
  private static String localHost = "localhost";
  private static final int PORT = 7000;
  private static BufferedWriter bufferedWriter;
  private static BufferedReader bufferedReader;

    /**
   * This is the main method of the ChatClient class.
   * It connects to the server, prompts the user for a unique username, and starts the multi-thread for receiving messages.
   * The user/client can send messages to the server, which will send them to all of the connected clients.
   * The user can exit the chat by typing "/EXIT".
   * 
   * (No parameters are used for this function)
   */

  public static void main(String[] args) {

    try {
      // Connecting to server.
      socket = new Socket(localHost, PORT);
      System.out.println("Connected to server.");

      bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      Scanner scanner = new Scanner(System.in);
      String clientID;

      System.out.print("Enter your username: "); 
      clientID = scanner.nextLine();
      bufferedWriter.write(clientID + "\n");
      bufferedWriter.flush();

      String serverResponse = bufferedReader.readLine();
      if (!serverResponse.equals("Enter your username:")) {
        System.out.println(serverResponse);
      }

      // Starting the multi-thread for receiving messages.
      new Thread(new MsgReceiver()).start();

      // Enables functionality for reading and sending messages.
      while (true) {
        String message = scanner.nextLine();
        bufferedWriter.write(message + "\n");
        bufferedWriter.flush();
        
        if (message.equalsIgnoreCase("/EXIT")){
          System.out.println("You have left the chat. Thanks for joining us!");
          scanner.close();
          break;
        }
      } //Exception handling and closing.
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (bufferedReader != null) {
          bufferedReader.close();
        }
        if (bufferedWriter != null) {
          bufferedWriter.close();
        }
        if (socket != null) {
          socket.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * The MsgReceiver class implements Runnable because it is used to create each new thread. 
   * It continuously reads messages from the server and prints them to the console.
   * Not sure if I should have done this differntly. Trying to limit how often I repeat any code.
   */

  static class MsgReceiver implements Runnable {
    @Override
    public void run() {
      String message;
      try {
        while ((message = bufferedReader.readLine()) != null) {
          System.out.println(message);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}