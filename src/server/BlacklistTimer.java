package server;

import java.util.TimerTask;

public class BlacklistTimer extends TimerTask {
    private String username;
    public BlacklistTimer(String username){
        this.username = username;
    }
    @Override
    public void run(){
        Server.removeIpBlacklist(username);
    }
}
