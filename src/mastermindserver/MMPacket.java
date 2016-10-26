/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mastermindserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

/**
 *
 * @author 1432581
 */
public class MMPacket {
    private final int BUFSIZE = 4;
    private Socket clientSocket;
    private InputStream in;
    private OutputStream out;
    private int msg_size;
    
    public MMPacket(Socket socket)throws IOException{
        this.clientSocket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    public Socket getSocket() {
        return clientSocket;
    }

    public void setSocket(Socket socket) {
        this.clientSocket = socket;
    }
    
    
    /**
     * reads packet from the MMPacket's socket and returns a byte array of the data
     * @return
     * @throws IOException 
     */
    public byte[] readPacket()throws IOException{
        
        byte[] byteBuffer = new byte[BUFSIZE];
        int total_bytes = 0;
        int bytes;
        while(total_bytes < BUFSIZE){
            
            if((bytes = in.read(byteBuffer, total_bytes, BUFSIZE - total_bytes))== -1){
                System.out.println("Packet: \n" + (bytes));
                //throw new SocketException("Connection Closed");
            }
            total_bytes += bytes;
        }
        
        return byteBuffer;
    }
    /**
     * takes in data to be written to the out of the packet.
     * @param bytes
     * @throws IOException 
     */
    public void writePacket(byte[] bytes)throws IOException{
        out.write(bytes);
    }
    /**
     * closes the client socket.
     * @throws IOException 
     */
    public void closeSocket() throws IOException{
        this.clientSocket.close();
    }
    
    
}

