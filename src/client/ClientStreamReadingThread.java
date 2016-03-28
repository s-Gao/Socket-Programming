package client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Song on 3/21/16.
 */
public class ClientStreamReadingThread extends Thread{
    private Socket socket;
    private static boolean socketConnected;
    public ClientStreamReadingThread(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        socketConnected = true;
        try{
            BufferedReader is = new BufferedReader((new InputStreamReader(socket.getInputStream())));
            String readLine;
            while(socketConnected && (readLine = is.readLine())!=null){
                if(readLine.equals("logout")){
                    break;
                }
                System.out.println(readLine);
            }
            is.close();
            Client.disconnectServer();
        }
        catch (SocketTimeoutException e){
            Client.disconnectClient();
            System.out.println("Client is inactive for too long, please type enter to exit");
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
    public static void closeInputStream(){
        socketConnected = false;
    }
}
