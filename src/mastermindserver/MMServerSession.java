
package mastermindserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * MMServerSession class that handles game session logic.
 * 
 * @author Rafia Anwar, Evan Glicakis, and Seaim Khan
 */
public class MMServerSession {

    private boolean playAgain = true;
    private boolean gameOver = false;
    private int[] colours;
    MMPacket mmPacket;
    
    /**
     * Constructor initializes an integer array for colours and an MMPacket
     * object.
     * 
     * @param mmp An MMPacket object
     */
    public MMServerSession(MMPacket mmp) {
        
        colours = new int[]{2,3,4,5,6,7,8,9};
        mmPacket = mmp;
    }
    
    /**
     * Determines whether to continue or end play and writes packets
     * accordingly.
     * 
     * @return A boolean value representing continuing or ending play.
     * @throws IOException if unable to read/write packets.
     */
    private boolean setPlayAgainValue() throws IOException{
        System.out.println("Waiting for play again value...");
        byte[] packet= mmPacket.readPacket();
        //System.out.println("Packet: " + Arrays.toString(packet));
        boolean play = false;
        
        //Continue play
        if(packet[0] == 0x00000011){
            play = true;
            gameOver = false;
            System.out.println("Sent start message");
            mmPacket.writePacket(new byte[]{0x0000000A,0,0,0});
            return play;
        }
        //End play   
        else {
            play = false;
            boolean validColour=false;
            //Check if incoming array is array of colors
            for(int i=0;i<4;i++){
                if(setColour(packet[i]) == -1)
                        validColour=false;
                    
                else 
                    validColour=true;
            }
            byte[] testAnswerSet=new byte[4];
            for(int i=0;i<4;i++)
                testAnswerSet[i]=packet[i];
                
            mmPacket.writePacket(testAnswerSet);  
       }     
       return play;
    }
    
    /**
     * Contains the game logic for the server and reading and writing of 
     * packets accordingly.
     * 
     * @throws IOException if unable to read/write packets concerning game.
     */
    public void action() throws IOException{        
        while(playAgain & !mmPacket.getSocket().isClosed()){
            int counter=0;
            setPlayAgainValue();
            int[] answerSet = createAnswerSet(); //Generate answer set
            while(!gameOver & !mmPacket.getSocket().isClosed()){
                // read packet from user.
                byte[] colorMessage = mmPacket.readPacket();
                //Checks if socket was closed and closes it's own.
                if(colorMessage[0] == 0x25){ //closed socket code
                    System.out.println("Good-bye!");
                    mmPacket.closeSocket();
                }
                System.out.println("received packet: "
                        + Arrays.toString(colorMessage));
                //check if msg is color 
                // checks if user sent the newgame/endgame message
                if(colorMessage[0] == 0x22){
                    System.out.println("new game -- server");
                    gameOver = true;
                    break;
                }

                int colorRange = setColour(colorMessage[0]);
                if(colorRange != -1)
                {
                    System.out.println("getting here");

                    //get user answer
                    int[] clientGuesses=new int[4];
                    for(int i=0;i<4;i++)
                        clientGuesses[i]=setColour(colorMessage[i]);
                 
                    //compare the answers
                    //reply with clues
                   int[] clues= clueGenerator(clientGuesses,answerSet);
                    
                   System.out.println("user guesses: "
                           +Arrays.toString(clientGuesses));
                   System.out.println("answer set: "
                           +Arrays.toString(answerSet));
                   //send it to the client
                   byte[] replyClientClues=convertIntCluesArrayToBytes(clues);
                   
                    if(check_win(clues)){
                        byte[] bytes = new byte[4];
                        byte[] answer_set_bytes = new byte[4];
                        for(int i = 0; i < answerSet.length; i++)
                           answer_set_bytes[i] = convertIntToByte(answerSet[i]);
                       
                        System.out.println("sent answer set in bytes");
                        mmPacket.writePacket(replyClientClues);
                        mmPacket.writePacket(answer_set_bytes);
                        gameOver = true;
                        break;
                    }
                   
                    mmPacket.writePacket(replyClientClues);
                    System.out.println("clues: "+Arrays.toString(clues));
                    System.out.println("clues in bytes: " 
                           + Arrays.toString(replyClientClues));
                   
                    counter++;
                    System.out.println("turn: " + counter);
                   
                    //if 10th submission then 0xFFFFFFFF
                    if(counter == 10){
                        System.out.println("sent game over");
                        gameOver = true;
                        mmPacket.writePacket(new byte[]{0xFFFFFFFF, 0 , 0 ,0 });
                        byte[] resp = new byte[4];
                        for(int i = 0 ; i < answerSet.length; i++){
                            resp[i] = convertIntToByte(answerSet[i]);
                        }
                        //Send the answer set to the client
                        System.out.println("Sent answer set to client");
                        mmPacket.writePacket(resp);
                    }
                    
                }     
            }
        }
       
        
    }
    
    /**
     * Determines win if clues match.
     * 
     * @param clues Clue set to compare
     * @return Boolean representing win or no win.
     */
    private boolean check_win(int[] clues){
        for(int i : clues){
            if(i != 1)
                return false;
        }
        return true;
    }
    
    /**
     * Converts the int array of clues to a byte array.
     * 
     * @param clues The int array of clues.
     * @return The byte array of clues.
     */
    private byte[] convertIntCluesArrayToBytes(int[] clues){
        byte[] byteClues = new byte[clues.length];
        for (int i=0;i < clues.length;i++)
        {
           byteClues[i]= convertIntCluesToBytes(clues[i]);
        }
        return byteClues;
    }
    
    /**
     * Converts a single clue to a byte.
     * 
     * @param clue A clue
     * @return A byte representation of a clue.
     */
    private byte convertIntCluesToBytes(int clue){
        switch(clue){
            case 0: 
                return 0x00000000; // inplace   --change
            case 1: 
                return 0x00000001; // outplace -- change
            
            default: 
                return 20;
        }
    }

    /**
     * Generates clues depending on the client's guesses.
     * 
     * @param clientGuesses The set of client's guesses
     * @param answerSet The actual set of answers
     * @return An array of clues depending on the accuracy of guesses.
     */
    private int[] clueGenerator(int[] clientGuesses,int[] answerSet){
        
        List<Integer> clueList = new ArrayList<>();
        int[] cloneAnswerSet = new int[4];
    
        for(int i=0;i <4;i++)
            cloneAnswerSet[i]=clientGuesses[i];
        
        //check for in-place clues
        for(int i=0;i < 4;i++)
        {
            if(answerSet[i] == cloneAnswerSet[i])
            {
                cloneAnswerSet[i]=-2; //so it will not be matched twice
                clueList.add(1);
            }
        }

        for(int guess=0;guess < 4;guess++)
        {
            for(int ans=0;ans<4;ans++)
            {                            
                if(answerSet[guess] == cloneAnswerSet[ans] &&
                            cloneAnswerSet[guess] != -2)
                {
                    if ( cloneAnswerSet[ans]!= -1){
                        cloneAnswerSet[ans]=-1;
                        clueList.add(0);
                        //exit loop
                        ans = 4;
                        System.out.println("ans in IF " + ans);
                    }
                }
            }
        }

        while(clueList.size() < 4){
            clueList.add(11);
        }
        int[] clues=new int[clueList.size()];
        for(int i=0;i<clueList.size();i++)
        {
            clues[i]=clueList.get(i);
        }
        
        return clues;
    }
  
    /**
     * Randomly generates an answer set.
     * 
     * @return An set of randomly generated answers.
     */
    private int[] createAnswerSet(){
        int[] randomSet = new int[4];
        Random random = new Random();
        for(int i = 0; i < randomSet.length; i++){
            int randomInt = random.nextInt(colours.length - 0);
            
            randomSet[i] = colours[randomInt];
        }
        //call convertIntToByte()
        //byte[] randomByteSet=colourBytes(randomSet);
        return randomSet;
    }
    
    /**
     * Generates a byte array of colours.
     * 
     * @param array An int array of colours
     * @return A byte array of colours
     */
    private byte[] colourBytes(int[] array){
        byte[] colours = new byte[array.length];
        for(int i = 0; i < colours.length; i++){
           colours[i]= convertIntToByte(i);
        }
        return colours;
    }
    
    /**
     * Converts int representing colour to a byte.
     * 
     * @param i A colour integer
     * @return A colour byte
     */
    private byte convertIntToByte(int i){
        switch(i){
            case 2: 
                return 0x00000002; // red
            case 3: 
                return 0x00000003; // yellow
            case 4: 
                return 0x00000004; // green
            case 5:
                return 0x00000005; // blue
            case 6:
                return 0x00000006; //purple
            case 7:
                return 0x00000007; // pink
            case 8:
                return 0x00000008; // light-green
            case 9:
                return 0x00000009; // brown
            default: 
                return -1;
        }
    }
    
    /**
     * Sets the colour representation of a byte.
     * 
     * @param value A byte value
     * @return A colour integer
     */
    private int setColour(byte value){
        switch(value){
            case 0x00000002: 
                return 2; // red
            case 0x00000003: 
                return 3; // yellow
            case 0x00000004: 
                return 4; // green
            case 0x00000005:
                return 5; // blue
            case 0x00000006:
                return 6; //purple
            case 0x00000007:
                return 7; // pink
            case 0x00000008:
                return 8; // light-green
            case 0x00000009:
                return 9; // brown
            default: 
                return -1;
        }
    }   
}
