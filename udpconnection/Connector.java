package udpconnection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;

public class Connector extends Thread{
    
    static enum Mode{SERVERSETUP, CLIENTSETUP};
    
    private final static int SERVER_PORT = 8888;
    private final static int CLIENT_PORT = 8887;
    private final static String LISTEN_TO_ALL = "0.0.0.0";
    private final static String SEND_TO_ALL = "255.255.255.255";
    private DatagramSocket socket = null;
    private final static String check = "DISCOVER"; //"red3xemAn5#1:";
    private final static String checkBack = "FOUND"; //"red3xemAn5#1:";
    private String myName;
    private String theirName;
    private InetAddress theirAddress;
    private Mode mode;
    FXMLDocumentController controller;
    
    Connector(){
    }
    
    public void setConnector (String myName, Mode mode, FXMLDocumentController controller){
        this.myName = myName;
        this.mode = mode;
        this.controller = controller;
    }
    
        @Override
    public void run(){
        switch (mode){
            case SERVERSETUP: serverSetup();
                 break;
            case CLIENTSETUP: clientSetup();
                break;
        }
    }
    
    private void serverSetup(){
        System.out.println("serverSetup");
        try{
            socket = new DatagramSocket(SERVER_PORT, InetAddress.getByName(LISTEN_TO_ALL));
            socket.setBroadcast(true);
            System.out.println("Socket ready");
            byte[] receiveBuffer = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            String incomingMessage = new String(receivePacket.getData()).trim();
            if(incomingMessage.equals(check)){
                theirAddress = receivePacket.getAddress();
                System.out.println("Check-message ok");
                byte[] sendData = checkBack.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, theirAddress, CLIENT_PORT);
                socket.send(sendPacket);
                System.out.println("Check-message sent");
                receiveBuffer = new byte[15000];
                receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                incomingMessage = new String(receivePacket.getData()).trim();
                theirName = incomingMessage;
                System.out.println("name-packet received");
                sendData = myName.getBytes();
                sendPacket = new DatagramPacket(sendData, sendData.length, theirAddress, CLIENT_PORT);
                socket.send(sendPacket);
                System.out.println("name-packet sent");
                System.out.println(theirName);
                showMessageLine("Connected.", Color.RED);
                showMessageLine(theirName +" joined the conversation.", Color.RED);
                controller.watchConnection(true);
                flow();
            }
        } catch(IOException e){
            showMessageLine("Server setup faliure!", Color.RED);
            e.printStackTrace();
        } finally {
            if(socket != null && socket.isClosed()==false){
                socket.close();
                controller.watchConnection(false);
                showMessageLine("Connection closed", Color.RED);
            }
        }
    }
    private void clientSetup(){
            // Find the server using UDP broadcast
            System.out.println("clientSetup");
        try {
            //Open a random port to send the package
            socket = new DatagramSocket(CLIENT_PORT);
            socket.setBroadcast(true);
            System.out.println("Socket ready");
            byte[] sendData = check.getBytes();
            //Try the 255.255.255.255 first... But why first?????
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(SEND_TO_ALL), SERVER_PORT);
            socket.send(sendPacket);
            System.out.println("Check-message sent " + new String(sendPacket.getData()) );
            byte[] receiveBuffer;
            DatagramPacket receivePacket;
            String incomingMessage;
            do{
                receiveBuffer = new byte[15000];
                receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                incomingMessage = new String(receivePacket.getData()).trim();
                System.out.println(incomingMessage);
            }while(! incomingMessage.equals(checkBack));
   
            theirAddress = receivePacket.getAddress();
            System.out.println("Check-message ok");
            sendData = myName.getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, theirAddress, SERVER_PORT);
            socket.send(sendPacket);
            System.out.println("name-packet sent");
            receiveBuffer = new byte[15000];
            receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            incomingMessage = new String(receivePacket.getData()).trim();
            theirName = incomingMessage;
            System.out.println("name-packet received");
            System.out.println(theirName);
            showMessageLine("Connected.", Color.RED);
            showMessageLine(theirName +" is ready to chat.", Color.RED);
            controller.watchConnection(true);
            flow();
        } catch (IOException ex) {
            showMessageLine("Client setup faliure!", Color.RED);
            ex.printStackTrace();
        } finally {
            if(socket != null && socket.isClosed()==false){
                socket.close();
                controller.watchConnection(false);
                showMessageLine("Connection closed", Color.RED);
            }
        }
    }
    private void flow()throws IOException{
        byte[] receiveBuffer;
        DatagramPacket receivePacket;
        String incomingMessage;
        while(true){
                receiveBuffer = new byte[15000];
                receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                incomingMessage = new String(receivePacket.getData()).trim();
                System.out.println(theirName + ": " + incomingMessage);
                showMessageLine(theirName + ": " + incomingMessage, Color.BLUE);
        }
    }
    
    private void showMessageLine(String message, Color c){
        Text text = new Text(message);
        text.setFill(c);
        text.setFont(Font.font("System", FontPosture.REGULAR, 12));
        Platform.runLater(() -> {
            controller.showMessage(text);
        });
//    Platform.runLater(new Runnable() {
//        @Override
//        public void run() {
//        //code
//        }
//    });
    
    }
    
    public void send(String message){
        if (socket!=null){
            int port = 0;
            switch (mode){
                case SERVERSETUP: port = CLIENT_PORT;
                    break;
                case CLIENTSETUP: port = SERVER_PORT;
                    break;
            }
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, theirAddress, port);
            try{
                socket.send(sendPacket);
            }catch(IOException e){
                showMessageLine("DatagramPacket sending faliure!", Color.RED);
            }
        }
    }
    
    public void close(){
        if(socket != null && socket.isClosed() == false){
            socket.close();
            socket = null;
            controller.watchConnection(false);
            System.out.println("Connection closed");
            this.close();
        }

    }
}
