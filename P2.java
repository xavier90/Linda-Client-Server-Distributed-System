

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by yaojianwang on 4/28/17.
 */
public class P2 {
    private Server server;
    private Client client;
    private String localIp;
    private int localPort;

    private String hostName;
    private P1 p1;

    private String strForLocalHost;
    private String strForBackupHost;

    public static boolean turnOff = false;


    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        P2 linda  = new P2();
	linda.setHostName(args[0]);
        linda.start();
        linda.runCommand();
    }


    private void start() throws IOException {
        p1 = new P1();
        p1.preStart();

        server = p1.getServer();

        localIp = p1.getLocalIp();
        localPort = p1.getLocalPort();
        client = p1.getClient();

    }

    private void setHostName(String name) {
	this.hostName = name;
    }


    private void runCommand () throws IOException, NoSuchAlgorithmException {
        BufferedReader commandInput = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Welcome to Linda! Following is input format.\n" +
                "*******************************************************\n" +
                "Add a new host:           add(hostname,ip,port)        \n" +
                "Delete host:              delete(host1,host2)          \n" +
                "Make one host crash:      kill(host)                   \n" +
                "Reboot host been crashed: reboot(host)");
        System.out.println();

	System.out.print("Linda> ");
	System.out.println(localIp + " at port number: " + localPort);
 
       

        server.setPathForServer("/tmp/ywang3/linda/" + hostName + "/nets");
        server.setPathForTuple("/tmp/ywang3/linda/" + hostName + "/tuples");

       
        strForLocalHost = hostName + "," + localIp + "," + localPort;

        //for rearrange tuple
        server.setStrForLocalHost(strForLocalHost);
        server.createFile(hostName);
        p1.addNewServer("add("+ hostName + "," + localIp + "," + localPort +")");


        while (true) {

            System.out.print("Linda> ");

            String command = commandInput.readLine().replaceAll(" ", "");
               
            if (command.startsWith("add")) {

                //when get new command, we rearrange the tuple
                if (server.getRearrange() && strForBackupHost != null) {
                    rearrangeTuple(strForBackupHost);
                }

                //I do not need to deal with backup tuple, because when I deal with original tuple,
                //its backup will be handled
                if (server.getAllTuple().size() == 0) {
                    // if original tuple list and backup tuple list are empty, do not need to rearrange
                    p1.addNewServer(command);
                } else {

                    //firstly tell all servers to generate strForBackupHost
                    //after this for loop, all server get strForBackupHost

                    for (String h : server.getServerList()) {
                        String gIp = h.split(",")[1];
                        int gPort = Integer.parseInt(h.split(",")[2]);
                        client.connectNewServer(gIp, gPort);
                        client.sendMessage("generateStrForBackupHost");

                        client.closeSocket();

                    }


                    //wait until server generate the value because of multi-thread
                    while (strForBackupHost == null) {
                        strForBackupHost = server.getStrForBackupHost();
                    }


                    //add new host
                    p1.addNewServer(command);

                    //to do
                    //tell all other hosts to rearrange tuples except the latest host
                    for (int i = 0; i < server.getServerList().size()-1; i++) {
                        String h = server.getServerList().get(i);
                        String rIp = h.split(",")[1];
                        int rPort = Integer.parseInt(h.split(",")[2]);
                        client.connectNewServer(rIp, rPort);
                        client.sendMessage("rearrange");
                        client.closeSocket();
                    }

                    server.updateTupleFile();
                    server.updateServerFile();

                }


            } else if (command.startsWith("in")) {
                //before handle command, check whether need to rearrange
                if (server.getRearrange() && strForBackupHost != null) {
                    rearrangeTuple(strForBackupHost);
                }

                client.setFind(false);
                p1.findTuple(command.substring(3, command.length()-1));


                if (client.isFind()) {

                    client.connectNewServer(client.getIpForRemove(),client.getPortForRemove());
                    client.sendMessage("remove:" + p1.getResult());

                }
                client.closeSocket();

                server.updateTupleFile();
                server.updateServerFile();

            }else if (command.startsWith("out")) {
                //before handle command, check whether need to rearrange
                if (server.getRearrange() && strForBackupHost != null) {
                    rearrangeTuple(strForBackupHost);
                }

                storeTuple(command.substring(4, command.length()-1));

                server.updateTupleFile();
                server.updateServerFile();

            } else if (command.startsWith("rd")) {
                //before handle command, check whether need to rearrange
                if (server.getRearrange() && strForBackupHost != null) {
                    rearrangeTuple(strForBackupHost);
                }


                client.setFind(false);
                p1.findTuple(command.substring(3, command.length()-1));

                server.updateTupleFile();
                server.updateServerFile();

            } else if (command.startsWith("delete")) {
                //when get new command, we rearrange the tuple
                if (server.getRearrange() && strForBackupHost != null) {
                    rearrangeTuple(strForBackupHost);
                }

                deleteServer(command);

                server.updateTupleFile();
                server.updateServerFile();

            } else if (command.startsWith("kill")) {
                //when get new command, we rearrange the tuple
                if (server.getRearrange() && strForBackupHost != null) {
                    rearrangeTuple(strForBackupHost);
                }

                killServer(command);

            } else if (command.startsWith("reboot")) {
                //when get new command, we rearrange the tuple
                if (server.getRearrange() && strForBackupHost != null) {
                    rearrangeTuple(strForBackupHost);
                }

                rebootServer(command);

            } else if (command.startsWith("p")) { //print information
                //before handle command, check whether need to rearrange
                if (server.getRearrange() && strForBackupHost != null) {
                    rearrangeTuple(strForBackupHost);
                }

                if (command.charAt(1) == 's') {
                    for(int i = 0; i < server.getServerList().size(); i++) {
                        System.out.println(server.getServerList().get(i));

                    }
                    System.out.println(server.getTotalNumOfServer());
                } else if (command.charAt(1)=='t') {
                    for (int i = 0; i < server.getAllTuple().size(); i++) {
                        System.out.println(server.getAllTuple().get(i));
                    }
                    System.out.println("following is backup");
                    for (int i = 0; i < server.getAllBackupTuple().size(); i++) {
                        System.out.println(server.getAllBackupTuple().get(i));
                    }
                }

            } else {
                //before handle command, check whether need to rearrange
                if (server.getRearrange() && strForBackupHost != null) {
                    rearrangeTuple(strForBackupHost);
                }

                System.out.println("Invalid command!");
            }

        }
    }


    private void storeTuple(String command) throws NoSuchAlgorithmException, IOException {
        String[] subCommand = command.split(",");

        String strForStore = Util.parseTuple(subCommand, "store");
        String strForHash = Util.parseTuple(subCommand, "hash");

        ConsistentHash hash = new ConsistentHash(strForHash);

        int idForTuple = hash.getHostID(server.getTotalNumOfServer(), hash.getSlotID());

        int total = server.getTotalNumOfServer();
        int idForBackupTuple = (idForTuple + total/2) % total;


        //store tuple
        String t = server.getServerList().get(idForTuple);
        String tIp = t.split(",")[1];
        int tPort = Integer.parseInt(t.split(",")[2]);

        client.connectNewServer(tIp, tPort);
        client.sendMessage("out" + strForStore);

        String addr = client.receiveMessage();
        System.out.println("put tuple (" + command + ") on " + addr );

        client.closeSocket();

        //store backup tuple
        String bServer = server.getServerList().get(idForBackupTuple);
        String bIp = bServer.split(",")[1];
        int bPort = Integer.parseInt(bServer.split(",")[2]);
        client.connectNewServer(bIp, bPort);
        client.sendMessage("backup" + strForStore);
        String bRes = client.receiveMessage();
        System.out.println("back up on " + bRes);
        //after server backup success, client receive success message and print it
    }

    private void rearrangeTuple(String strForBackupHost) throws NoSuchAlgorithmException, IOException {
        ArrayList<String> tuples = server.getAllTuple();
        ArrayList<String> backupTuples = server.getAllBackupTuple();


        //rearrange tuple in tuples and its corresponding backup tuple
        for (int i = 0; i < tuples.size(); i++) {
            String tuple = tuples.get(i);
            String strForHash = Util.storeConvertToHash(tuple);

            ConsistentHash hash = new ConsistentHash(strForHash);

            //rearrange original tuple
            //new hostid for tuple
            int idForTuple = hash.getHostID(server.getTotalNumOfServer(), hash.getSlotID());
            //current hostid for tuple
            int curHostId = server.getHostId(strForLocalHost);
            if (idForTuple != curHostId) {

                //remove tuple on current host
                server.getAllTuple().remove(tuple);
                //store tuple on new host
                String ip = server.getServerList().get(idForTuple).split(",")[1];
                int port = Integer.parseInt(server.getServerList().get(idForTuple).split(",")[2]);
                client.connectNewServer(ip, port);
                client.sendMessage("out" + tuple);
                client.closeSocket();

                //rearrange backup tuple
                int total = server.getTotalNumOfServer();
                int idNewBackup = (idForTuple + total/2) % total;

                //delete backup tuple on current backup host
                String bIp = strForBackupHost.split(",")[1];
                int bPort = Integer.parseInt(strForBackupHost.split(",")[2]);
                client.connectNewServer(bIp, bPort);
                client.sendMessage("backremove" + tuple);
                client.closeSocket();

                //store backup tuple on new backup host
                String sbIp = server.getServerList().get(idNewBackup).split(",")[1];
                int sbPort = Integer.parseInt(server.getServerList().get(idNewBackup).split(",")[2]);
                client.connectNewServer(sbIp, sbPort);
                client.sendMessage("backup" + tuple);
                client.closeSocket();

            }


        }
    }


    private void deleteServer(String command) throws IOException, NoSuchAlgorithmException {
        ArrayList<String> collectTupleTS = new ArrayList<>();

        String tmp = command.replaceAll(" ", "");
        tmp = tmp.substring(7, tmp.length()-1);
        String[] tmpCommand = tmp.split(",");

        //collect all tuple into collectTupleTS
        for (String h : server.getServerList()) {
            String cIp = h.split(",")[1];
            int cPort = Integer.parseInt(h.split(",")[2]);
            client.connectNewServer(cIp, cPort);
            client.sendMessage("collect");
            String collect = client.receiveMessage();
            client.closeSocket();


            //parse collect and store into collectTupleTS
            String[] collectTuple = collect.split(";");
            for (int i = 0; i < collectTuple.length; i++) {
                if (!collectTuple[i].equals("")) {
                    collectTupleTS.add(collectTuple[i]);
                }

            }
        }


        //clear all tuple in TS
        for (String h : server.getServerList()) {
            String cIp = h.split(",")[1];
            int cPort = Integer.parseInt(h.split(",")[2]);
            client.connectNewServer(cIp, cPort);
            client.sendMessage("clearTuple");
            client.closeSocket();

        }


        boolean delself = false;
        //delete server
        for (int i = 0; i < tmpCommand.length; i++) {

            String del = "";
            for (String h : server.getServerList()) {
                if (h.startsWith(tmpCommand[i])) {
                    del = h;
                }
            }

            server.getServerList().remove(del);

            if (del.equals(strForLocalHost)){
                delself = true;

            } else {
                //delete other host

                //clear data on deleted host
                String dIp = del.split(",")[1];
                int dPort = Integer.parseInt(del.split(",")[2]);
                client.connectNewServer(dIp, dPort);
                client.sendMessage("clearTuple");
                client.closeSocket();

                client.connectNewServer(dIp, dPort);
                client.sendMessage("clearServer");
                client.closeSocket();
            }

        }


        //broadcast new server list and server file
        p1.updateServerListForAll();


        //restore all tuple into remain servers
        for (String t : collectTupleTS) {

            System.out.println(t);
            String tmpt = Util.strToTuple(t);
            storeTuple(tmpt.substring(1, tmpt.length()-1));
        }

        //if delete self
        if (delself) {
            server.getAllTuple().clear();
            server.getAllBackupTuple().clear();
            server.getServerList().clear();
        }

    }


    private void killServer(String command) throws IOException {

        String tmp = command.replaceAll(" ", "");
        tmp = tmp.substring(5, tmp.length()-1);


        //kill server
        String kill = "";
        for (String h : server.getServerList()) {
            if (h.startsWith(tmp)) {
                kill = h;
            }
        }



        //set target server to be killed
        String dIp = kill.split(",")[1];
        int dPort = Integer.parseInt(kill.split(",")[2]);
        client.connectNewServer(dIp, dPort);
        client.sendMessage("kill");
        client.closeSocket();


    }

    private void rebootServer(String command) throws IOException {

        String tmp = command.replaceAll(" ", "");
        tmp = tmp.substring(7, tmp.length()-1);


        //reboot server
        String reboot = "";
        for (String h : server.getServerList()) {
            if (h.startsWith(tmp)) {
                reboot = h;
            }
        }



        //set target server to be killed
        String rIp = reboot.split(",")[1];
        int rPort = Integer.parseInt(reboot.split(",")[2]);
        client.connectNewServer(rIp, rPort);
        client.sendMessage("reboot");
        client.closeSocket();


    }
 }
