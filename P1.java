

import java.io.*;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


/**
 * Created by yaojianwang on 4/9/17.
 */
public class P1 {

    private Server server;
    private Client client;
    private String localIp;
    private int localPort;

    private String hostName;
    private String result;

    public P1() throws IOException {
        server = new Server();
        server.start();

    }

    public void preStart() throws IOException {
        //get local ip and local port number
        localIp = InetAddress.getLocalHost().toString().split("/")[1];
        localPort = server.getAvailablePort();

        //start client, and based on ip and port to connect the local server
        client = new Client();
//        client.connectNewServer(localIp, localPort);

    }

    public void setHostName (String name) {
        this.hostName = name;
    }

    public Server getServer () { return server; }
    public String getLocalIp() { return localIp; }
    public int getLocalPort() { return localPort; }
    public Client getClient() { return client; }

    //result means tuple we find from server
    public String getResult() { return result; }

    public void runCommand() throws IOException, NoSuchAlgorithmException {
        BufferedReader commandInput = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("Linda> ");
            String command = commandInput.readLine().replaceAll(" ", "");

            if (command.startsWith("P1")) {//get local ip address and port number
                System.out.println(localIp + " at port number: " + localPort);
                command.replaceAll(" ", "");
                hostName = command.substring(2);

                server.setPathForServer("/tmp/ywang3/linda/" + hostName + "/nets");
                server.setPathForTuple("/tmp/ywang3/linda/" + hostName + "/tuples");


//                server.createFile(hostName);

            } else if (command.startsWith("add")) {
                addNewServer(command);

            } else if (command.startsWith("in")) {
                client.setFind(false);//set find to false

                findTuple(command.substring(3, command.length()-1));

                if (client.isFind()) {
                    client.connectNewServer(client.getIpForRemove(),client.getPortForRemove());
                    client.sendMessage("remove:" + result);
                }
                client.closeSocket();

            } else if (command.startsWith("out")) {
                storeTuple(command.substring(4, command.length()-1));

            } else if (command.startsWith("rd")) {

                findTuple(command.substring(3, command.length()-1));
            }  else {
                System.out.println("Invalid command!");
            }


        }
    }




    public void addNewServer(String command) throws IOException {
        command = command.replaceAll(" ", "");
        String[] tmpCommand = command.split("\\(");
        for (int i = 1; i < tmpCommand.length; i++) {
            String[] tmpAddress = tmpCommand[i].substring(0, tmpCommand[i].length()-1).split(",");

            hostName = tmpAddress[0];
            String targetIp = tmpAddress[1];
            int targetPort = Integer.parseInt(tmpAddress[2]);

            //connect new server
            client.connectNewServer(targetIp, targetPort);
            client.sendMessage("add");

            //add new ip to serverList
            server.addNewServerToServerList(hostName, targetIp, targetPort);

            //update server list of all server
            updateServerListForAll();
            client.closeSocket();
        }
    }

    //to do
    //update server list
    public void updateServerListForAll() throws IOException {
        ArrayList<String> tmpList = server.getServerList();

        //generate server list then send to server
        String list = "";
        for (String s : tmpList) {
            list += s + ";";
        }
        list = list.substring(0, list.length()-1);

        for (String server : tmpList) {
            String tmpIp = server.split(",")[1];
            int tmpPort = Integer.parseInt(server.split(",")[2]);
            client.connectNewServer(tmpIp, tmpPort);

            client.sendMessage("update;" + list);
            client.closeSocket();
        }
    }


    //read and remove tuple from tuple space
    //
    public void findTuple(String command) throws IOException, NoSuchAlgorithmException {

        String[] subCommand = command.split(",");

        boolean isBroadcast = false;
        String strForSearch = Util.parseTuple(subCommand, "store");
        String strForHash = Util.parseTuple(subCommand, "hash");

        // decide whether client need to broadcast the command
        for (int i = 0; i < subCommand.length; i++) {
            if (subCommand[i].startsWith("?")) {
                isBroadcast = true;
            }
        }

        if (isBroadcast) {
            //from all the server list find what we need
            for (String address : server.getServerList()) {
                if (client.isFind()) break;// some server has found tuple before other server start

                int idForTuple = server.getServerList().indexOf(address);
                int total = server.getTotalNumOfServer();
                int idForBackup = (idForTuple + total/2) % total;


                String i = address.split(",")[1];
                int p = Integer.parseInt(address.split(",")[2]);
                client.connectNewServer(i, p);
                client.sendMessage("in?" + strForSearch);

                String str = client.receiveMessage();

                result = str.split(";")[0];
                if (result.equals("killed")) {

                    //if server is killed, look up its backup server
                    String backupAddr = server.getServerList().get(idForBackup);
                    boolean flag = findTupleOnBackupServer("fb?" + strForSearch, backupAddr);
                    //if find tuple, break loop
                    if (flag) break;


                } else if (!result.equals("failed")) {
                    String ip = str.split(";")[1];

                    System.out.println("get tuple " + Util.strToTuple(result) + " on " + ip);
                    break;
                }

            }
            if (result.equals("failed")) {
                System.out.println("No target tuple in server");
            }


        } else {
            //use hash function to find what we need

//            int idForTuple = Util.hash(strForHash) % server.getTotalNumOfServer();

            ConsistentHash hash = new ConsistentHash(strForHash);
            int idForTuple = hash.getHostID(server.getTotalNumOfServer(), hash.getSlotID());
            int total = server.getTotalNumOfServer();
            int idForBackup = (idForTuple + total/2) % total;


            String t = server.getServerList().get(idForTuple);
            String tIp = t.split(",")[1];
            int tPort = Integer.parseInt(t.split(",")[2]);
            client.connectNewServer(tIp, tPort);
            client.sendMessage("in" + strForSearch);

            String str = client.receiveMessage();
            result = str.split(";")[0];

            if (result.equals("killed")) {

                String backupAddr = server.getServerList().get(idForBackup);
                //if server is killed, look up its backup server
                boolean flag = findTupleOnBackupServer("fb" + strForSearch, backupAddr);
                if (!flag) {
                    System.out.println("No target tuple in server");
                }

            } else if (result.equals("failed")) {
                System.out.println("No target tuple in server");

            } else {
                String ip = str.split(";")[1];
                client.closeSocket();
                System.out.println("get tuple " + Util.strToTuple(result) + " on " + ip);
            }


        }
    }


    public void storeTuple(String command) throws NoSuchAlgorithmException, IOException {
        String[] subCommand = command.split(",");

        String strForStore = Util.parseTuple(subCommand, "store");
        String strForHash = Util.parseTuple(subCommand, "hash");

        int idForTuple = Util.hash(strForHash) % server.getTotalNumOfServer();

        String t = server.getServerList().get(idForTuple);
        String tIp = t.split(",")[1];
        int tPort = Integer.parseInt(t.split(",")[2]);
        client.connectNewServer(tIp, tPort);

        client.sendMessage("out" + strForStore);

        //print result of out
        String addr = client.receiveMessage();
        System.out.println("put tuple (" + command + ") on " + addr );
        client.closeSocket();

    }


    private boolean findTupleOnBackupServer(String message, String address) throws IOException {
        boolean find = false;

        String backIp = address.split(",")[1];
        int backPort = Integer.parseInt(address.split(",")[2]);
        client.connectNewServer(backIp, backPort);
        client.sendMessage(message);
        String res = client.receiveMessage().split(";")[0];
        //if find on backup server


        if (!res.equals("failed")) {

            //if after kill also need to delete tuple
//            client.setFind(true);
//            client.setIpForRemove(backIp);
//            client.setPortForRemove(backPort);

            find = true;
            System.out.println("get tuple " + Util.strToTuple(res) + " on backup server " + backIp);

        }

        return find;
    }
}
