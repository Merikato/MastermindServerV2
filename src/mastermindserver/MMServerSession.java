/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mastermindserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author 1430626
 */
public class MMServerSession {

    private boolean playAgain = true;
    private boolean gameOver = false;
    private int[] colours;
    MMPacket mmPacket;
    
    public MMServerSession(MMPacket mmp) {
        
        colours = new int[]{2,3,4,5,6,7,8,9};
        mmPacket = mmp;
    }
    
    private boolean setPlayAgainValue() throws IOException{
       System.out.println("Waiting for play again value...");
       byte[] packet= mmPacket.readPacket();
       System.out.println("Packet: " + Arrays.toString(packet));
              boolean play = false;
       
       
           if(packet[0] == 0x00000011){
                play = true;
                gameOver = false;
                System.out.println("Sent start message");
                mmPacket.writePacket(new byte[]{0x0000000A,0,0,0});
                return play;
           }
           
           else {
                play = false;
                boolean validColour=false;
                //check if incoming array is array of colors
                for(int i=0;i<4;i++){
                    if(setColour(packet[i]) == -1)
                    {
                        validColour=false;
                    }
                    else validColour=true;
                }
                byte[] testAnswerSet=new byte[4];
                for(int i=0;i<4;i++){
                    testAnswerSet[i]=packet[i];
                }
                mmPacket.writePacket(testAnswerSet);  
       }     
       return play;
    }
    public void action() throws IOException{
        
        
        while(playAgain && !mmPacket.getSocket().isClosed()){
            int counter=0;
            setPlayAgainValue();
            int[] answerSet = createAnswerSet(); //Generate answer set
            while(!gameOver & !mmPacket.getSocket().isClosed()){
                
                // read packet from user.
                byte[] colorMessage = mmPacket.readPacket();
                System.out.println("received packet: "+ Arrays.toString(colorMessage));
                //check if msg is color 
                if(colorMessage[0] == 0x22){ // checks if user sent the newgame/endgame message
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
                    {
                        clientGuesses[i]=setColour(colorMessage[i]);
                    }
                    //compare the answers
                    //reply with clues
                   int[] clues= clueGenerator(clientGuesses,answerSet);
                   
                   // TODO: check if clues are all equal to 1
                   
                   
                   System.out.println("user guesses: "+Arrays.toString(clientGuesses));
                   System.out.println("answer set: "+Arrays.toString(answerSet));
                   //send it to the client
                   //byte[] replyClientClues=colourBytes(clues);
                   byte[] replyClientClues=convertIntCluesArrayToBytes(clues);
                   
                   if(check_win(clues)){
                       byte[] bytes = new byte[4];
                       byte[] answer_set_bytes = new byte[4];
                       for(int i = 0; i < answerSet.length; i++){
                           answer_set_bytes[i] = convertIntToByte(answerSet[i]);
                       }
                       System.out.println("sent answer set in bytes");
                       mmPacket.writePacket(replyClientClues);
                       mmPacket.writePacket(answer_set_bytes);
                       gameOver = true;
                       break;
                   }
                   
                   mmPacket.writePacket(replyClientClues);
                   System.out.println("clues: "+Arrays.toString(clues));
                   System.out.println("clues in bytes: " + Arrays.toString(replyClientClues));
                   
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
    
    private boolean check_win(int[] clues){
        for(int i : clues){
            if(i != 1)
                return false;
        }
        return true;
    }
    
    private byte[] convertIntCluesArrayToBytes(int[] clues){
        byte[] byteClues = new byte[clues.length];
        for (int i=0;i < clues.length;i++)
        {
           byteClues[i]= convertIntCluesToBytes(clues[i]);
        }
        return byteClues;
    }
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

  private int[] clueGenerator(int[] clientGuesses,int[] answerSet){
        
    List<Integer> clueList = new ArrayList<Integer>();
    int[] cloneAnswerSet = new int[4];
    
    for(int i=0;i <4;i++)
    {
        cloneAnswerSet[i]=clientGuesses[i];
    }
        //check for in-place clues
        for(int i=0;i < 4;i++)
        {
            if(answerSet[i] == cloneAnswerSet[i])
            {
                cloneAnswerSet[i]=-2; //so it will not be matched twice
                clueList.add(1);
            }
        }
        //System.out.println("ARRAY   "+Arrays.toString(cloneAnswerSet));
        //System.out.println("LIST\t" + clueList);
        for(int guess=0;guess < 4;guess++)
        {
            for(int ans=0;ans<4;ans++)
            {                            
                //System.out.println("guess  " + guess + " ans  " + ans);

                    if(answerSet[guess] == cloneAnswerSet[ans] && cloneAnswerSet[guess] != -2)
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
                        //System.out.println("array " + Arrays.toString(cloneAnswerSet) );

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
    
    private byte[] colourBytes(int[] array){
        byte[] colours = new byte[array.length];
        for(int i = 0; i < colours.length; i++){
           colours[i]= convertIntToByte(i);
        }
        return colours;
    }
    
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
                return 0x00000008; // white
            case 9:
                return 0x00000009; // brown
            default: 
                return -1;
        }
    }
    
    
    private int setColour(byte value){
      //  colours = new int[]{0x01, 0x00000002, 0x00000003, 
      //  0x00000004, 0x00000005, 0x00000006, 0x00000007, 0x00000008};
       // for(int i = )
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
                return 8; // white
            case 0x00000009:
                return 9; // brown
            default: 
                return -1;
        }
    }
    
    
    /*private void checkAnswers(int[] clientAnswers, int[] answerSet){
        for(int i = 0; i < clientAnswers.length; i++){
            
        }
        
    } Will do after clue generation if needed*/
    
}
