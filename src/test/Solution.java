package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Song on 3/27/16.
 */
public class Solution {

    public static void main(String args[]){
        try{
//            FileReader fileReader = new FileReader(new File("graph.txt"));
//            BufferedReader bufferedReader = new BufferedReader(fileReader);
//            List<String> list  = new ArrayList<String>();
//            String str;
//            while((str = bufferedReader.readLine())!=null){
//                list.add(str);
//                System.out.println(str);
//            }
//
//            List<String> res;
//            res = routes(list);
//            for(String element:res){
//                System.out.println(element);
//            }
//
//            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
//            Date date = dateFormat.parse("03/01/2016-00:00:01");
//            Date date1 = dateFormat.parse("02/28/2016-23:59:59");
//            //Date date1 = dateFormat.parse("03/02/2016-23:59:59");
//            System.out.println(date.getTime());
//            System.out.println(date1.getTime());
//            long temp = date.getTime()-date1.getTime();
//            System.out.println(temp);

            FileReader fileReader = new FileReader(new File("time.txt"));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> list = new ArrayList<String>();
            String curStr;
            while((curStr = bufferedReader.readLine())!=null){
                list.add(curStr);
            }
            long connect = 0;
            long total = 0;
            long lastConnect = 0;
            long lastStart = 0;
            long curTime = 0;
            Pattern pattern1 = Pattern.compile("\\(\\d{2}/\\d{2}/\\d{4}\\-\\d{2}\\:\\d{2}\\:\\d{2}\\)\\s+[:]{2}\\s+START");
            Pattern pattern2 = Pattern.compile("\\(\\d{2}/\\d{2}/\\d{4}\\-\\d{2}\\:\\d{2}\\:\\d{2}\\)\\s+[:]{2}\\s+CONNECTED");
            Pattern pattern3 = Pattern.compile("\\(\\d{2}/\\d{2}/\\d{4}\\-\\d{2}\\:\\d{2}\\:\\d{2}\\)\\s+[:]{2}\\s+DISCONNECTED");
            Pattern pattern4 = Pattern.compile("\\(\\d{2}/\\d{2}/\\d{4}\\-\\d{2}\\:\\d{2}\\:\\d{2}\\)\\s+[:]{2}\\s+SHUTDOWN");
            for(String str : list){
                if(pattern1.matcher(str).matches()){
                    //START
                    System.out.println("start");
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
                    curTime = dateFormat.parse(str.split("[\\(\\)]",3)[1]).getTime();
                    lastConnect = 0;
                    lastStart = curTime;
                }
                else if(pattern2.matcher(str).matches()){
                    //connected
                    System.out.println("connected");
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
                    curTime = dateFormat.parse(str.split("[\\(\\)]",3)[1]).getTime();
                    lastConnect = curTime;
                    System.out.println(lastConnect);
                }
                else if(pattern3.matcher(str).matches()){
                    //disconnected
                    System.out.println("disconnected");
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
                    curTime = dateFormat.parse(str.split("[\\(\\)]",3)[1]).getTime();
                    connect += (curTime-lastConnect);
                    System.out.println(connect);
                    lastConnect = 0;
                }
                else if(pattern4.matcher(str).matches()){
                    //shutdown
                    System.out.println("shutdown");
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
                    curTime = dateFormat.parse(str.split("[\\(\\)]",3)[1]).getTime();
                    if(lastConnect != 0){
                        connect += (curTime-lastConnect);
                        lastConnect = 0;
                    }
                    total += (curTime-lastStart);
                    System.out.println(total);
                    lastStart = 0;
                }
                else{
                    System.out.println("The file format is invalid");
                }
            }
            double abc = ((double)connect/(double)total)*100;
            System.out.println((int)Math.floor(abc));


//            Matcher matcher1 = pattern1.matcher("(02/03/2002-14:00:00) :: START");



        }

        catch (IOException e){
            e.printStackTrace();
        }
        catch (ParseException e){
            e.printStackTrace();
        }
    }

    private static ArrayList<String> routes(List<String > graph){
        String start = graph.get(0).trim().split("\\s+",2)[0];
        String end = graph.get(0).trim().split("\\s+",2)[1];
        ArrayList<String> res = new ArrayList<String>();
        Map<String,ArrayList<String>> adjcencyList = new HashMap<String, ArrayList<String>>();
        Set<String> visitedNodes = new HashSet<String>();
        int i=0;
        for(String str : graph){
            if(i == 0){
                i++;
                continue;
            }

            String head = (str.trim().split("\\s?:\\s?",2))[0];
            adjcencyList.put(head,new ArrayList<String>());

            String[] body = (str.trim().split("\\s?:\\s?",2))[1].split("\\s+");
            for(String element : body){
                adjcencyList.get(head).add(element);
            }
        }
        dfsHelper(start,end,adjcencyList,res,"",new HashSet<String>());
        return res;
    }

    private static void dfsHelper(String start, String end, Map<String,ArrayList<String>> adjcencyList, ArrayList<String> res, String curPath, Set<String> visitedNodes){
        curPath = curPath+start;
        if(visitedNodes.contains(start)){
            //this node has already visited in the current route, indicating a loop here
            return;
        }
        visitedNodes.add(start);
        if(!adjcencyList.containsKey(start)){
            //the case when this node doesn't have any out-coming edge
            return;
        }
        if(adjcencyList.get(start).isEmpty()){
            return;
        }
        for(String str : adjcencyList.get(start)){
            if(str.equals(end)){
                res.add((curPath+end));
            }
            else{
                dfsHelper(str,end,adjcencyList,res,curPath,visitedNodes);
            }
        }
        visitedNodes.remove(start);
    }


}
