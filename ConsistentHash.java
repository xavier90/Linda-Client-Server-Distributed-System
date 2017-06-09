

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by yaojianwang on 4/29/17.
 */
public class ConsistentHash {
    private String target;
    private int totalId;
    public ConsistentHash(String target) {
        this.target = target;
        totalId = (int) Math.pow(2, 16);
    }


    public int getSlotID() throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(target.getBytes());
        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        return Math.abs(bigInt.intValue() % totalId);

    }

    public int getHostID(int hostNum, int slotId) {

        return slotId/(totalId/hostNum);
    }



//    public static void main(String[] args) throws NoSuchAlgorithmException {
//        int i = 1;
//        while (i < 100) {
//            ConsistentHash test = new ConsistentHash("int:" + i);
//
//            System.out.println(test.getHostID(10, test.getSlotID()));
//            i++;
//        }
//    }

}
