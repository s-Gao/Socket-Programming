package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;

/**
 * Created by Song on 3/19/16.
 */
public class Client {
    private static String IPADDRESS;
    private static int PORT;
    private static int TIMEOUT = 30*60;//0 would represent infinite time
    private static boolean serverConnected=true;
    private static boolean clientConnected=true;
    private static Socket clientSocket;
    public void start(){
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run(){
                try{
                    if(!clientConnected){
                        //this is the case when the Client socket has a time out, no need to execute the logic for control-c
                        return;
                    }
                    ClientStreamReadingThread.interrupted();
                    if(serverConnected){
                        PrintStream printStream = new PrintStream(clientSocket.getOutputStream());
                        printStream.println("logout");
                        printStream.close();
                    }
                    clientSocket.close();
                    System.out.println("Execute control-c and first close the server socket and then the client socket");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }));
    }

    public static void main(String[] args){

        new Client().start();
        if(System.getenv().containsKey("TIME_OUT")){
            TIMEOUT = Integer.parseInt(System.getenv().get("TIME_OUT"));
        }
        //set a new TCP connection to IPADDRESS port PORT
        try{

            if(args.length == 2){
                IPADDRESS = args[0];
                PORT = Integer.parseInt(args[1]);
            }
            else{
                System.out.println("Server ip address and port number should be passed as arguments");
                return;
            }
            System.out.println(IPADDRESS);
            System.out.println(PORT);

            Socket socket = new Socket();
            clientSocket = socket;
            socket.connect(new InetSocketAddress(IPADDRESS,PORT));
            socket.setSoTimeout(TIMEOUT*1000);
            //key board stream and the output stream of the socket, the input of the socket would be
            //handled by a separate Thread of Client ClientStreamReadingThread
            BufferedReader keyBoardReader = new BufferedReader(new InputStreamReader(System.in));
            PrintStream os = new PrintStream(socket.getOutputStream());
            ClientStreamReadingThread clientStreamReadingThread = new ClientStreamReadingThread(socket);
            clientStreamReadingThread.start();
            String readLine;
            while(clientConnected && serverConnected && !(readLine = keyBoardReader.readLine()).equals("logout")){
                os.println(readLine);
            }
            if(serverConnected){
                os.println("logout");
                //wait for the Server to close its socket
                Thread.sleep(1000);
                ClientStreamReadingThread.closeInputStream();
            }
            keyBoardReader.close();
            os.close();
            socket.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }

    public static void disconnectServer(){
        serverConnected = false;
    }
    public static void disconnectClient(){
        clientConnected = false;
    }
}

