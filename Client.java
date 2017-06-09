

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by yaojianwang on 4/9/17.
 */
public class Client {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String ip;
    private int port;
    private boolean stop;
    private boolean find = false;//if server

    private String ipForRemove;
    private int portForRemove;


    public void connectNewServer(String targetIp, int targetPort) throws IOException {
        socket = new Socket(targetIp, targetPort);

//        System.out.println("this" +socket.getLocalAddress().toString()+ ":" + socket.getLocalPort());
//        System.out.println("remote" + socket.getRemoteSocketAddress().toString());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    }

    //send message to server
    public void sendMessage(String message) throws IOException {
        out = new PrintWriter(socket.getOutputStream(), true);

        out.println(message);

//        out.close();
    }

    public void closeSocket() throws IOException {
        socket.close();
    }

    public String receiveMessage() throws IOException {

        while (true) {
            String str = in.readLine();

            if (str != null) {
                if (str.equals("stop")) { //client stop waiting
                    in.close();
                    socket.close();
                    break;
                } else if (str.startsWith("in?")) {
                    String[] tmp = str.split(";");
                    setIpForRemove(tmp[2]);
                    setPortForRemove(Integer.parseInt(tmp[3]));
                    in.close();
                    socket.close();
                    setFind(true);
//                    System.out.println("get tuple on" + str.substring(3));
                    return str.substring(4);

                } else if (str.startsWith("in")) {
                    String[] tmp = str.split(";");
                    setIpForRemove(tmp[2]);
                    setPortForRemove(Integer.parseInt(tmp[3]));
                    in.close();
                    socket.close();
                    setFind(true);
//                    System.out.println("get tuple on" + str.substring(3));

                    return str.substring(3);

                    //to do
                    //after tuple has been found
                    //client stop waiting for message from server
                    //if there is ? client should stop other servers looking for tuple
                } else if (str.startsWith("out")) {
                    in.close();
                    socket.close();
//                    System.out.println("store tuple on" + str.substring(4));

                    return str.substring(4);

                } else if (str.equals("failed")) {
                    in.close();
                    socket.close();
                    return str;
                } else if (str.startsWith("backup")) {
                    in.close();
                    socket.close();
                    return str.substring(7);
                } else if (str.startsWith("collect")) {
                    in.close();
                    socket.close();

                    return str.substring(8);
                } else if (str.equals("killed")) {
                    in.close();
                    socket.close();
                    return str;
                }
            }
        }
        return "client stop waiting";
    }

    //set ip address

    public void setIp(String ip) {
        this.ip = ip;
    }

    //set port number

    public void setPort(int port) {
        this.port = port;
    }

    public void setIpForRemove(String ipForRemove) {

        this.ipForRemove = ipForRemove;

    }

    public String getIpForRemove() {
        return ipForRemove;
    }
    public void setPortForRemove(int portForRemove) {

        this.portForRemove = portForRemove;

    }

    public int getPortForRemove() {
        return portForRemove;
    }


    public boolean isFind() { return find; }

    public void setFind(boolean value) {
        find = value;
    }

//    public void broadcastRequest(String bIp, int bPort, String target) {
//
//        new BroadcastHandler(bIp, bPort, target).start();
//
//    }


//    public class BroadcastHandler extends Thread {
//        private String ipForBroadcast;
//        private int portForBroadcast;
//        private String target;
//
//        private Socket bSocket;
//        private BufferedReader bIn;
//        private PrintWriter bOut;
//        private String bRes;
//
//        public BroadcastHandler(String ip, int port, String target) {
//
//            ipForBroadcast = ip;
//            portForBroadcast = port;
//            this.target = target;
//        }
//
//        public void run() {
//            try {
//                System.out.println(socket.getLocalPort());
//                bSocket = new Socket(ipForBroadcast, portForBroadcast);
//                System.out.println(bSocket.getLocalPort());
//                // there is a problem for this sentence, maybe cannot connect to same port number?
//                bIn = new BufferedReader(new InputStreamReader(bSocket.getInputStream()));
//                bOut = new PrintWriter(bSocket.getOutputStream());
//
//                //broadcast to find the target
//                bOut.println("in?" + target);
//
//                System.out.println("i send to find ?");
//                //how do I synchronized this process
//                while (!find) {
//
//                    if ((bRes = bIn.readLine()) != null) {
//                        if (bRes.equals("failed")) {
//                            bOut.println("in?" + target);
//
//                        } else if (bRes.startsWith("in?")) {//server has found the tuple
//                            String[] tmp = bRes.split(";");
//                            String tpl = Util.strToTuple(tmp[1]);
//
//                            System.out.println("get tuple" + tpl + "on " + tmp[2]);
//                            find = true;
//                            setIpForRemove(tmp[2]);
//                            setPortForRemove(Integer.parseInt(tmp[3]));
//                            break;
//                        }
//                    }
//
//                }
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            finally {
//                try {
//
//                    bIn.close();
//                    bOut.close();
//                    bSocket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//        }
//
//
//    }
}
