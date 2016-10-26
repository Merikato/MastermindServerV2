/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mastermindserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author 1432581
 */
public class MMServer {

    private static int portNumber = 50000;
    private ServerSocket socket;
    private MMServerSession Session;
    
    public static void main(String[] args) throws IOException{
        
        ServerSocket ss = new ServerSocket(50000);
        new MMServer().createServerSocket(ss);
    }
    
    public MMServer() throws IOException {
//        socket = new ServerSocket(50000);
//        createServerSocket(socket);
    }
    
    public void createServerSocket(ServerSocket socket) throws IOException {
        for(;;){
        //    System.out.println(InetAddress.getLocalHost());
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
