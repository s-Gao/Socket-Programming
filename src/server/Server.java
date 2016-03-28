package server;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    private static Map<String,String> ipBlacklist = new HashMap<String, String>();
    private static Map<String,Socket> loggedInUsers = new HashMap<String, Socket>();
    private static Set<ServerThread> runningThread = new HashSet<ServerThread>();
    private static Map<String,Long> userRecentConnectedTime = new HashMap<String, Long>();
    private static Map<String,ArrayList<String>> offlineMessage = new HashMap<String, ArrayList<String>>();
    //add shutdown hook
    public void start(){
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run(){
                System.out.println("Execute control-c and first close the client socket and then close the server socket");
                try{
                    for(Map.Entry<String,Socket> entry : loggedInUsers.entrySet()){
                        PrintStream printStream = new PrintStream(entry.getValue().getOutputStream());
                        printStream.println("The server is terminated. Type enter to exit");
                        printStream.close();
                        //close all the server side sockets
                        entry.getValue().close();
                    }
                    logOutAllUsers();
                    setAllLoginUsersRecentConnectedTime();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }));
    }

    public static void main(String[] args) throws IOException{

        //begin shutdown hook
        new Server().start();
        int port;
        if(args.length == 1){
            port = Integer.parseInt(args[0]);
        }
        else{
            System.out.println("The port number is needed to be passed as argument");
            return;
        }
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is running.");

        Map<String,String> usrPasswdMapping = new HashMap<String, String>();
        FileReader fileReader = new FileReader(new File("users.txt"));
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while((line = bufferedReader.readLine())!=null){
            String[] strPair = line.split(" ");
            usrPasswdMapping.put(strPair[0], strPair[1]);
            userRecentConnectedTime.put(strPair[0],(long)0);
            offlineMessage.put(strPair[0],new ArrayList<String>());
        }
        while(true){
            Socket client = serverSocket.accept();
            ServerThread serverThread = new ServerThread(client,usrPasswdMapping);
            serverThread.start();
            runningThread.add(serverThread);
        }
    }

    //method dealing with username and Ip pair blacklist
    public static void setIpBlacklist(String username, String ip){
        ipBlacklist.put(username,ip);
    }
    public static void removeIpBlacklist(String username){
        ipBlacklist.remove(username);
    }
    public static String getIpBlacklist(String username){
        if(ipBlacklist.containsKey(username)){
            return ipBlacklist.get(username);
        }
        else{
            return "-1";
        }
    }

    //methods dealing with logged in users
    public static void logInUser(String username, Socket socket){
        System.out.println("User '"+username+"' logs in.");
        loggedInUsers.put(username,socket);
    }
    public static void logOutUser(String username, ServerThread serverThread){
        System.out.println("User '"+username+"' logs out.");
        loggedInUsers.remove(username);
        runningThread.remove(serverThread);
    }
    public static void logOutAllUsers() throws Exception{
        for(ServerThread serverThread : runningThread){
            if(serverThread.isAlive()){
                serverThread.interrupt();
            }
        }
        for(ServerThread serverThread : runningThread){
            PrintStream printStream = new PrintStream(serverThread.getThreadSocket().getOutputStream());
            printStream.println("Server is terminated. Please type enter to exit");
            printStream.println("logout");
            printStream.close();
        }
        for(Map.Entry<String,Socket> entry : loggedInUsers.entrySet()){
            entry.getValue().close();
        }

    }
    public static boolean alreadyLogin(String username){
        return loggedInUsers.containsKey(username);
    }
    public static Set<Map.Entry<String,Socket>> getLoginUserEntry(){
        return loggedInUsers.entrySet();
    }
    public static Socket getLoginUser(String username){
        return loggedInUsers.get(username);
    }

    //methods dealing with
    public static void setUserRecentConnectedTime(String username){
        userRecentConnectedTime.put(username,System.currentTimeMillis());
    }
    public static void setAllLoginUsersRecentConnectedTime(){
        for(Map.Entry<String,Socket> entry : loggedInUsers.entrySet()){
            userRecentConnectedTime.put(entry.getKey(),System.currentTimeMillis());
        }
    }
    public static long getUserRecentConnectedTime(String username){
        return (System.currentTimeMillis()-userRecentConnectedTime.get(username))/60000;
    }
    public static Set<Map.Entry<String,Long>> getUserRecentConnectedTimeEntry(){
        return userRecentConnectedTime.entrySet();
    }

    //methods dealing with offline message
    public static void addOfflineMessage(String username, String message){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String str = dateFormat.format(date);
        offlineMessage.get(username).add(str+" "+username+" sent to you:\n"+message+"\n");
    }
    public static ArrayList<String> getAllOfflineMessage(String username){
        return offlineMessage.get(username);
    }
}
