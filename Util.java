

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by yaojianwang on 4/11/17.
 */
public class Util {


    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    public static boolean isFloat(String str) {
        try {
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    public static int hash(String strForHash) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(strForHash.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        return bigInt.intValue();
    }



    public static String parseTuple(String[] subCommand, String type) {
        String res = "";


        if (type.equals("hash")) {
            for (int i = 0; i < subCommand.length; i++) {
                if (subCommand[i].startsWith("\"")) {
                    res += subCommand[i].substring(1, subCommand[i].length()-1);
                } else {
                    res += subCommand[i];
                }
            }
        } else if (type.equals("store")) {
            // there is , on the end the string
            for (int i = 0; i < subCommand.length; i++) {
                if (isInteger(subCommand[i])) {
                    res += "int:" + subCommand[i] + ",";

                } else if (isFloat(subCommand[i])) {
                    res += "float:" + subCommand[i] + ",";

                } else if (subCommand[i].startsWith("?")) {
                    if (subCommand[i].split(":")[1].equals("int")) {
                        res += "int:?,";
                    } else if (subCommand[i].split(":")[1].equals("float")) {
                        res += "float:?,";

                    } else if (subCommand[i].split(":")[1].equals("string")) {
                        res += "string:?,";

                    }

                } else if (subCommand[i].startsWith("\"")){//before hashing, convert "abc" int abc
                    res += "string:" + subCommand[i].substring(1, subCommand[i].length()-1) + ",";

                }
            }
            res = res.substring(0, res.length()-1);
        }
        return res;
    }

    public static String strToTuple(String str) {
        String res = "(";

        String[] tmp = str.split(",");
        for (int i = 0; i < tmp.length; i++) {

            String type = tmp[i].split(":")[0];
            String value = tmp[i].split(":")[1];
            if (type.equals("string")) {
                res += "\"" + value + "\"";
            } else if (type.equals("float") || type.equals("int")) {
                res += value;
            }

            if (i < tmp.length - 1) res += ",";
        }
        res += ")";
        return res;
    }


    public static boolean variableMatch(String command, String tuple) {

        //to do
        //implement if there is ?

        String[] subCommand = command.split(",");

        String[] aryTuple = tuple.split(",");

        for (int i = 0; i < aryTuple.length; i++) {
            if (i >= subCommand.length) return false;
            String[] c = subCommand[i].split(":");
            String[] t = aryTuple[i].split(":");

            if (c[0].equals(t[0]) && c[1].equals(t[1])) {
                continue;
            } else if (c[0].equals(t[0]) && c[1].equals("?")) {
                continue;
            } else {
                return false;
            }

        }
        return true;

    }

    public static void chmod(File dirName) {
        if (!dirName.exists()) {
            try{
                dirName.mkdir();
                dirName.setExecutable(true, false);
                dirName.setWritable(true, false);
                dirName.setReadable(true, false);
            } catch(SecurityException se){
                System.out.println("Error: cannot create a direcotry!");
            }
        }
    }

    public static String storeConvertToHash(String store) {
        String res = "";
        String[] tmp = store.split(",");
        for (int i = 0; i < tmp.length; i++) {
            res += tmp[i].split(":")[1];
        }
        return res;
    }
}
