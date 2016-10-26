
package mastermindserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * MMPacket class that handles the sending and receiving of packets.
 * 
 * @author Rafia Anwar, Evan Glicakis, and Seaim Khan
 */
public class MMPacket {
    private final int BUFSIZE = 4;
    private Socket clientSocket;
    private InputStream in;
    private OutputStream out;
    private int msg_size;
    
    /**
     * Constructor initializes the client socket and streams for reading and 
     * writing of packets.
     * 
     * @param socket The client socket
     * @throws IOException if stream(s) cannot be initialized.
     */
    public MMPacket(Socket socket)throws IOException{
        this.clientSocket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    /**
     * Gets the socket.
     * 
     * @return A client socket.
     */
    public Socket getSocket() {
        return clientSocket;
    }

    /**
     * Sets the socket.
     * 
     * @param socket The client's socket
     */
    public void setSocket(Socket socket) {
        this.clientSocket = socket;
    }
    
    /**
     * Reads packet from the MMPacket's socket and returns a byte array of 
     * the data.
     * 
     * @return A byte array of data
     * @throws IOException if packet cannot be read.
     */
    public byte[] readPacket()throws IOException{
        
        byte[] byteBuffer = new byte[BUFSIZE];
        int total_bytes = 0;
        int bytes;
        while(total_bytes < BUFSIZE){
            
            if((bytes = in.read(byteBuffer, total_bytes, 
                    BUFSIZE - total_bytes))== -1){
                System.out.println("Packet: \n" + (bytes));
                throw new SocketException("Connection closed...");
            }
            total_bytes += bytes;
        }
        
        return byteBuffer;
    }
    
    /**
     * Takes in data to be written to the out of the packet.
     * 
     * @param bytes The byte array.
     * @throws IOException if packet cannot be written.
     */
    public void writePacket(byte[] bytes)throws IOException{
        out.write(bytes);
    }
    
    /**
     * Closes the client socket.
     * 
     * @throws IOException if unable to close socket.
     */
    public void closeSocket() throws IOException{
        this.clientSocket.close();
    }   
}

