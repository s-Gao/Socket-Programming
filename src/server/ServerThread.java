package server;

import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerThread extends Thread{
    private static int BLOCK_TIME = 60;

    private Socket socket = null;
    private Map<String,String> usrPasswdMapping = new HashMap<String, String>();
    private String ipAddress;
    public ServerThread(Socket socket, Map<String,String> usrPasswdMapping){
        this.socket = socket;
        this.usrPasswdMapping = usrPasswdMapping;
    }


    @Override
    public void run(){
        String username = "";
        String password = "";

        try{
            if(System.getenv().containsKey("BLOCK_TIME")){
                BLOCK_TIME = Integer.parseInt(System.getenv().get("BLOCK_TIME"));
            }
            ipAddress = socket.getInetAddress().getHostAddress();
            PrintStream os = new PrintStream(socket.getOutputStream());
            BufferedReader  is = new BufferedReader((new InputStreamReader(socket.getInputStream())));
            String readLine;
            os.println("Verification is needed.");
            boolean validUser = false;
            for(int i=0;i<3;i++){
                //username
                os.println("Please type the username:");
                username = is.readLine();
                if(username.equals("logout")){
                    os.close();
                    is.close();
                    socket.close();
                    return;
                }
                while(!usrPasswdMapping.containsKey(username)){
                    os.println("This username doesn't exist.");
                    os.println("Please retype the username:");
                    username = is.readLine();
                }
                //password
                os.println("please type the password:");
                String passwordplain = is.readLine();
                if(passwordplain.equals("logout")){
                    os.close();
                    is.close();
                    socket.close();
                    return;
                }
                password = sha1(passwordplain);
                //if the username and ip address pair is blocked
                if(Server.getIpBlacklist(username).equals(ipAddress)){
                    os.println("Your ip and username is blocked, " +
                            "please reconnect after "+BLOCK_TIME+" seconds or try another username.");
                    os.println("Type enter to exit the program.");

                    //disconnect the socket
                    os.println("logout");
                    os.close();
                    is.close();
                    socket.close();
                    return;
                }

                //if the username and password are not matched
                if(!usrPasswdMapping.containsKey(username) || !usrPasswdMapping.get(username).equals(password)){
                    os.println("The password is wrong.");
                    os.println("Please try again.");
                }
                else{
                    os.println("Log in successfully, welcome!\n");
                    validUser = true;
                    break;
                }
            }
            if(!validUser){
                os.println("Incorrect information for too many times.");
                os.println("Your username and ip address pair is blocked");
                os.println("Type enter to exit the program.");
                blockIp(username,ipAddress);

                //Not login successfully, disconnect the socket
                os.println("logout");
                os.close();
                is.close();
                socket.close();

                return;
            }

            //If user has already been logged in
            //First check the offline message
            if(!Server.getAllOfflineMessage(username).isEmpty()){
                os.println("Here are all the offline messages for you:\n");
                while(!Server.getAllOfflineMessage(username).isEmpty()){
                    String str = Server.getAllOfflineMessage(username).remove(0);
                    os.println(str);
                }
                os.println("End of offline message.\n");
            }
            os.println("Please type a command.\n");
            if(Server.alreadyLogin(username)){
                os.println("Your account has been logged in somewhere else");
                os.println("Type enter to exit the program.");
                os.println("logout");
                os.close();
                is.close();
                socket.close();
                return;
            }

            //Right here the client has successfully login
            Server.logInUser(username,socket);
            //read and parse messages from the client
            while(true){
                if((readLine = is.readLine())!=null){
                    System.out.println(username+": "+readLine);
                    os.println("Server receive " + readLine);
                    if(readLine.equals("logout")){
                        break;
                    }
                    Pattern pattern1 = Pattern.compile("\\s*who\\s*");
                    Pattern pattern2 = Pattern.compile("^\\s*last\\s+[1-5]?[0-9]\\s*$|^\\s*last\\s+60\\s*$");
                    Pattern pattern3 = Pattern.compile("broadcast\\s+.{1,255}");
                    Pattern pattern4 = Pattern.compile("send\\s+\\(.{1,255}\\)\\s+.{1,255}");
                    Pattern pattern5 = Pattern.compile("send\\s+.{1,255}\\s+.{1,255}");
                    Matcher matcher1 = pattern1.matcher(readLine);
                    Matcher matcher2 = pattern2.matcher(readLine);
                    Matcher matcher3 = pattern3.matcher(readLine);
                    Matcher matcher4 = pattern4.matcher(readLine);
                    Matcher matcher5 = pattern5.matcher(readLine);

                    if(matcher1.matches()){
                        // "who"
                        if(Server.getLoginUserEntry().size() == 1){
                            //The size 1 is the minimum since when there is a command, there is
                            //at least one user online
                            os.println("Unfortunately, you are the only person online.");
                            os.println("Don't be sad, you can still send offline message to one or more users.");
                            os.println("As soon as they logged in, they would see your message");
                        }
                        else{
                            os.println("The names of other connected users are:");
                            for(Map.Entry<String,Socket> entry : Server.getLoginUserEntry()){
                                if(!(entry.getKey().equals(username))){
                                    os.println(entry.getKey());
                                }
                            }
                        }

                    }
                    else if(matcher2.matches()){
                        //last <number>
                        Pattern patternForSplit = Pattern.compile("\\s+");
                        String[] strArr = patternForSplit.split(readLine);
                        long period = Long.parseLong(strArr[1]);
                        os.println("The name of those users connected within the last "+period+" minutes are:");
                        for(Map.Entry<String,Long> entry : Server.getUserRecentConnectedTimeEntry()){
                            if(entry.getKey().equals(username)){
                                continue;
                            }
                            if(Server.alreadyLogin(entry.getKey())){
                                os.println(entry.getKey());
                            }
                            else if(Server.getUserRecentConnectedTime(entry.getKey()) <= period){
                                os.println(entry.getKey());
                            }
                        }
                    }
                    else if(matcher3.matches()){
                        //broadcast <message>
                        String message = readLine.split("\\s+",2)[1];
                        for (Map.Entry<String,Socket> entry : Server.getLoginUserEntry()){
                            if(!entry.getKey().equals(username)){
                                PrintStream tempStream = new PrintStream(entry.getValue().getOutputStream());
                                tempStream.println(sendMessage(username,message));
                            }
                        }
                    }
                    else if(matcher4.matches()){
                        //send (<user> <user>...<user>) <message>
                        String userStr = readLine.split("[\\(\\)]",3)[1];
                        String message = readLine.split("\\)\\s+",2)[1];
                        String[] users = userStr.split("\\s+");
                        for(String user : users){
                            if(!usrPasswdMapping.containsKey(user)){
                                os.println("user '"+user+"' doesn't exit");
                                os.println("Please retype your command.");
                                break;
                            }
                            if(Server.alreadyLogin(user)){
                                //instant message
                                PrintStream tempStream = new PrintStream(Server.getLoginUser(user).getOutputStream());
                                tempStream.println(sendMessage(username,message));
                            }
                            else{
                                //offline message
                                Server.addOfflineMessage(user,message);
                            }

                        }
                    }
                    else if(matcher5.matches()){
                        //send <user> <message>
                        String user = readLine.split("\\s+",3)[1];
                        String message = readLine.split("\\s+",3)[2];
                        if(!usrPasswdMapping.containsKey(user)){
                            os.println("user '"+user+"' doesn't exit");
                            os.println("Please retype your command.");
                            continue;
                        }
                        if(Server.alreadyLogin(user)){
                            //instant message
                            PrintStream tempStream = new PrintStream(Server.getLoginUser(user).getOutputStream());
                            tempStream.println(sendMessage(username,message));
                        }
                        else{
                            //offline message
                            Server.addOfflineMessage(user,message);
                        }

                    }
                    else{
                        os.println("Couldn't recognize your request: "+readLine );
                        os.println("For your reference, the supported commands are: ");
                        os.println("1. 'who'");
                        os.println("2. 'last <number>'  <number> should be integer from 1 to 60");
                        os.println("3. 'broadcast <message>'");
                        os.println("4. 'send (<user> <user>...<user>) <message>'");
                        os.println("5. 'send <user> <message>'");
                        os.println("6. 'logout'");
                    }


                }
            }
            os.close();
            is.close();
            socket.close();
            Server.setUserRecentConnectedTime(username);
            System.out.println("Server socket with '"+username+"' is closed");
            Server.logOutUser(username,this);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void blockIp(String username, String ipAddress){
        Server.setIpBlacklist(username, ipAddress);
        Timer timer = new Timer();
        timer.schedule(new BlacklistTimer(username),BLOCK_TIME*1000);
    }

    private String sendMessage(String username,String content){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String str = dateFormat.format(date);
        return "\n"+str+" "+username+" sent to you:\n"+content+"\n";
    }
    public Socket getThreadSocket(){
        return socket;
    }

    //This function is from http://www.sha1-online.com/sha1-java/
    private String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
