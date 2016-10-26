
package mastermindserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * MMServer class that runs the server for the game.
 * 
 * @author Rafia Anwar, Evan Glicakis, and Seaim Khan
 */
public class MMServer {

    private static int portNumber = 50000;
    private ServerSocket socket;
    private MMServerSession Session;
    
    public static void main(String[] args) throws IOException{
        
        ServerSocket ss = new ServerSocket(50000);
        new MMServer().createServerSocket(ss);
    }
    
    /**
     * No-parameter default constructor.
     */
    public MMServer() throws IOException {}
    
    /**
     * Creates the server socket that will run 24/7 and wait for a client.
     * 
     * @param socket The server socket
     * @throws IOException if unable to create socket.
     */
    public void createServerSocket(ServerSocket socket) throws IOException {
        for(;;){ 
            System.out.println("Waiting for client...");
            Socket client_socket = socket.accept();
            System.out.println("Connected to client at: " + client_socket.getLocalAddress().toString());
            if(client_socket != null){
                MMPacket mmp = new MMPacket(client_socket);
                //send an OK message to draw board
                Session = new MMServerSession(mmp);
                Session.action();
                client_socket.close();
            }          
        }
    }
    
    
}
