

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by yaojianwang on 4/9/17.
 */
public class Server extends Thread {
    private ServerSocket listener;

    private String pathForServer;
    private String pathForTuple;

    private ArrayList<String> allTuple;
    private ArrayList<String> allBackupTuple;
    private ArrayList<String> serverList;

    private int totalNumOfServer;
    private String strForBackupHost;
    private String strForLocalHost;
    private boolean rearrange = false; //flag to tell whether need to rearrange now
    private boolean killed;

    public Server() throws IOException {
        listener = new ServerSocket(0);
//        listener.setReuseAddress(true);
        serverList = new ArrayList<>();
        allTuple = new ArrayList<>();
        allBackupTuple = new ArrayList<>();
        totalNumOfServer = 0;
    }


    public void run() {
        try {


           while (true) {

               Socket socket = listener.accept();

               if (socket != null) {

                   new Handler(socket);
               }

           }


        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                listener.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public int getAvailablePort() {
        return listener.getLocalPort();
    }

    public void createFile(String hostName) throws IOException{

        File dirName = new File("/tmp");
        Util.chmod(dirName);

        dirName = new File("/tmp/ywang3");
        Util.chmod(dirName);

        dirName = new File ("/tmp/ywang3/linda");
        Util.chmod(dirName);
 
        dirName = new File ("/tmp/ywang3/linda/" + hostName);
        Util.chmod(dirName);

        //create nets file
        //String text = localhost + " " +  portNumber + " " + hostName;
        BufferedWriter output = null;
        try {
            File file = new File("/tmp/ywang3/linda/" + hostName + "/nets");
            file.createNewFile();
            file.setExecutable(false, false);
            file.setReadable(true, false);
            file.setWritable(true,false);
            output = new BufferedWriter(new FileWriter(file));
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( output != null ) {
                output.close();
            }
        }

	//delete one line code near here

        //create tuples file
        try {
            File file = new File("/tmp/ywang3/linda/" + hostName + "/tuples");
            file.createNewFile();
            file.setExecutable(false, false);
            file.setReadable(true, false);
            file.setWritable(true,false);
            output = new BufferedWriter(new FileWriter(file));
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
            if ( output != null ) {
                output.close();
            }
        }
    }



    public void addNewServerToServerList(String hostName, String newIp, int newPort) throws IOException {
        String str = hostName + "," + newIp + "," + newPort;
        if (!serverList.contains(str)) {
            serverList.add(str);

            //to do
            //after debugging, need to uncommit these lines

        BufferedWriter bw = new BufferedWriter(new FileWriter(pathForServer));
        for (String server : serverList) {
            bw.write(server);
        }
            bw.close();
            totalNumOfServer++;
        } else {
            System.out.println("host is already in Linda");
        }

    }
    public ArrayList<String> getServerList() {
        return serverList;
    }

    public void setPathForServer(String path) { this.pathForServer = path; }

    public String getPathForServer() { return pathForServer; }

    public void setPathForTuple(String path) { this.pathForTuple = path; }

    public String getPathForTuple() { return pathForTuple; }

    public void storeTuple(String tuple) throws IOException {

        allTuple.add(tuple);
        updateTupleFile();
    }

    public void storeBackupTuple(String tuple) throws IOException {
        allBackupTuple.add(tuple);
        updateTupleFile();
    }

    public ArrayList<String> getAllTuple() {
        return allTuple;
    }

    public ArrayList<String> getAllBackupTuple() {
        return allBackupTuple;
    }

    public int getTotalNumOfServer() {
        totalNumOfServer = serverList.size();
        return totalNumOfServer;
    }

    public int getHostId(String host) {
        return serverList.indexOf(host);
    }

    //to do
    //after debugging, need to uncommit these lines
    public void updateTupleFile() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(pathForTuple));
        for (String t : allTuple) {
            bw.write(t);
        }
        bw.write("following are backup tuple");
        for (String t : allBackupTuple) {
            bw.write(t);
        }
        bw.close();
    }

    //to do
    //after debugging, need to uncommit these lines
    public void updateServerFile() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(pathForServer));
        for (String i : serverList) {
            bw.write(i);
        }
        bw.close();
    }



    public class Handler implements Runnable {
        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;
        private String message;

        public Handler (Socket socket) throws IOException {

            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            message = in.readLine();

            if(message != null) {
                new Thread(this).start();
            }

        }

        public void run() {
            if (message.startsWith("update")) {//update server file
                try {
                    serverList = new ArrayList<>();
                    String[] lists = message.split(";");
                    for (int i = 1; i < lists.length; i++) {
                        serverList.add(lists[i]);
                    }
                    updateServerFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (message.startsWith("in?")) {// deal with the command in with variable match
                if (!killed) {
                    String tmp = message.substring(3);


                    boolean f = false;//if find the target f will be true
                    for (String s : allTuple) {

                        if (Util.variableMatch(tmp, s)) {//if tuple match the search condition


                            out.println("in?;" + s + ";" + socket.getInetAddress().toString().substring(1)+ ";" + socket.getLocalPort());
                            f = true;
                            break;
                        }
                    }
                    if (!f) out.println("failed");//if cannot find target, return failed

                } else {
                    out.println("killed");
                }
            } else if (message.startsWith("fb?")) {
                String tmp = message.substring(3);


                boolean f = false;//if find the target f will be true
                for (String s : allBackupTuple) {

                    if (Util.variableMatch(tmp, s)) {//if tuple match the search condition


                        out.println("in?;" + s + ";" + socket.getInetAddress().toString().substring(1)+ ";" + socket.getLocalPort());
                        f = true;
                        break;
                    }
                }
                if (!f) out.println("failed");//if cannot find target, return failed

            } else if (message.startsWith("in")) {
                if (!killed) {
                    String tmp = message.substring(2);

                    if (allTuple.contains(tmp)) {


                        try {
                            out.println("in;" + tmp +";" +socket.getInetAddress().toString().substring(1)+ ";" + socket.getLocalPort());
                            updateTupleFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    } else {
                        out.println("failed");
                    }
                } else {
                    out.println("killed");
                }


            } else if (message.startsWith("fb")) {

                String tmp = message.substring(2);

                if (allBackupTuple.contains(tmp)) {


                    try {
                        out.println("in;" + tmp +";" +socket.getInetAddress().toString().substring(1)+ ";" + socket.getLocalPort());
                        updateTupleFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else {
                    out.println("failed");
                }
            } else if (message.startsWith("out")) {
                String tmp = message.substring(3);

                try {
                    storeTuple(tmp);
                    out.println("out:" + socket.getInetAddress().toString().substring(1));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (message.startsWith("remove")) { 
		//delete one line code here 
                if (!killed) {
                    allTuple.remove(message.substring(7));
                    out.println("stop");
                } else {
                    out.println("killed");
                }

            } else if (message.equals("add")) {

            } else if (message.startsWith("backup")) {//store backup tuple
                String tmp = message.substring(6);

                try {
                    storeBackupTuple(tmp);
                    out.println("backup:"+ socket.getInetAddress().toString().substring(1));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (message.startsWith("backremove")) {
                String tmp = message.substring(10);
                allBackupTuple.remove(tmp);
            } else if (message.equals("generateStrForBackupHost")) {

                generateStrForBackupHost(strForLocalHost);

            } else if (message.equals("rearrange")) {
                rearrange = true;
            } else if (message.equals("collect")) {
                String res = "collect;";
                for (String t : allTuple) {
                    res = res + t + ";";
                }
                if (allTuple.isEmpty()) {
                    out.println(res);
                } else {
                    out.println(res.substring(0, res.length()-1));
                }

            } else if (message.equals("clearTuple")) {
                allTuple = new ArrayList<>();
                allBackupTuple = new ArrayList<>();
            } else if (message.equals("clearServer")) {
                serverList = new ArrayList<>();

            } else if (message.equals("kill")) {
                killed = true;
            } else if (message.equals("reboot")) {
                killed = false;
            }

        }


    }

    //for rearrange tuple
    private void generateStrForBackupHost(String strForLocalHost) {
        int curHostId = getHostId(strForLocalHost);
        int total = getTotalNumOfServer();
        int oldBackupHostId = (curHostId + total/2) % total;
        strForBackupHost = getServerList().get(oldBackupHostId);
    }

    public String getStrForBackupHost() { return strForBackupHost; }

    public void setStrForLocalHost(String strForLocalHost) {
        this.strForLocalHost = strForLocalHost;
    }

    public boolean getRearrange() { return rearrange; }

}
