package src;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;


/**
 * The ChatServer class manages multi-threaded connections from clients using sockets.
 * A unique user ID is assigned to each connected client per each user input.
 * It maintains a list of connected users.
 * The server takes messages from each client and displays them to all other connected clients.
 * 
 * @author Shauna Lachelier
 */

public class ChatServer {

  private static ServerSocket server;
  private static int PORT = 7000;
  //The array that will contain the list of connected clients
  private static List<ClientHandler> clients = new ArrayList<>();

/*
 * This is the main method of the ChatServer class.
 * It starts the server on the specified port and listens for incoming connections.
 * 7000 is used as an example here.
 * User-specified unique client IDs AKA usernames are assigned to each client and read by the server.
 * 
 * (No parameters are used for this function)
 */

  public static void main(String[] args) {
      try {
      // Start the server on the specified port
        server = new ServerSocket(PORT);
        System.out.println("\nThe Server has started on port " + PORT + ".");

        // Continuously accept client connections while the server is open and running.
        while (true) {
          Socket socket = server.accept();

          BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
          BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
          String clientID;

          // Provides instructions and prompts the user to enter a unique username.
          while(true){
            bufferedWriter.newLine();
            bufferedWriter.flush();

            clientID = bufferedReader.readLine();

            if(isClientIDUnique(clientID)){
              bufferedWriter.write("Welcome to the live chat! You may exit the chat at any time by typing /EXIT.");
              bufferedWriter.newLine();
              bufferedWriter.flush();
              break;
            } else {
              bufferedWriter.write("Username is already taken. Please try a different username: ");
              bufferedWriter.newLine();
              bufferedWriter.flush();
            }
          }
          // Create a new client handler and start a new thread for each connected client/user.
          ClientHandler clientHandler = new ClientHandler(socket, clientID);
          clients.add(clientHandler);
          new Thread(clientHandler).start();

          System.out.println(clientID + " has connected.");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

    /**
   * This purpose of this method is to check if the user-given client ID 
   * is unique among all other connected clients.
   * 
   * @param clientID Is the client ID/username to check.
   * @return true if the client ID is unique, otherwise, it is false.
   */

  private static boolean isClientIDUnique(String clientID) {
    for (ClientHandler client : clients) {
      if (client.getClientID().equals(clientID)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Sends a message to all connected clients except the sender 
   * so that the sent message does not display twice for the sender.
   * 
   * @param message The message that is being sent.
   * @param sender The ID/username of the client/user sending the message.
   */

  public static void sendMessage(String message, String sender) {
    for (ClientHandler client : clients) {
      if (!client.getClientID().equals(sender)) {
        client.createMessage(message);
      }
    }
  }

  /**
   * Removes a client from the list of connected clients when a user disconnects.
   * 
   * @param clientHandler This handles the removal of the client/user to be disconnected.
   */

  public static void removeClient(ClientHandler clientHandler) {
    clients.remove(clientHandler);
    // This will display to the server to show that the specified client has disconnected.
    System.out.println(clientHandler.getClientID() + " has disconnected.");
  }
}

/**
 * I implemented this additional class here to handle the communication for each client.
 * It receives messages from the client and forwards them to the server for all users to see.
 * I implemented this for better organization and reusability so that the main class is not cluttered.
 * 
 */

class ClientHandler implements Runnable {
  private Socket socket;
  public String clientID;
  private BufferedWriter bufferedWriter;
  private BufferedReader bufferedReader;

    /**
   * A constructor to create a new ClientHandler when a client connects to the server.
   * 
   * @param socket The socket for communicating with each individual client that connects.
   * @param clientID The unique ID of the client that the user specifies.
   */

  public ClientHandler(Socket socket, String clientID) {
    this.socket = socket;
    this.clientID = clientID;

    try{
      this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

    /**
   * Returns the unique ID of the client that the user has entered.
   * I found this better to use in my workflow so that I can call it in any context.
   * 
   * @return The unique ID of the client.
   */
  public String getClientID() {
    return clientID;
  }


    /**
   * Sends a message to the client so that they receive any messages sent to the server from other clients.
   * 
   * @param message The message being sent.
   */

  public void createMessage(String message) {
    try {
      bufferedWriter.write(message + "\n");
      bufferedWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** 
   * This runs the multi-thread for each client that connects to the server.
   * It continuously reads messages from the client and forwards them to the server to read.
   * Is there possibly a better way to do this?
   * 
   * (No parameters are used for this function)
   */

  @Override
  public void run() {
    String message;
    
    try {
      while ((message = bufferedReader.readLine()) != null) {
        System.out.println(clientID + ": " + message);
        ChatServer.sendMessage(clientID + ": " + message, clientID);
        if (message.equalsIgnoreCase("QUIT")) {
          break;
        }
      } //Exception handling and closing everything out, then removing the individual client from the server.
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
    ChatServer.removeClient(this);
  }

}

