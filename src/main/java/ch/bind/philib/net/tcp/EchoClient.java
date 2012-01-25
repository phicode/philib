package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

import ch.bind.philib.net.SocketAddresses;

public class EchoClient {
    public static void main(String[] args) throws Exception {
        TcpConnection con = new TcpConnection();

        con.open(SocketAddresses.fromIp("10.0.0.67", 1234));

        byte[] buf = new byte[16 * 1024];
        new Random().nextBytes(buf);
        byte[] bufC = buf.clone();

        while (true) {
            if (con.write(buf) != buf.length) {
                System.out.println("write length failed");
                return;
            }
            int len = con.read(buf, 0, buf.length);
            while (len != buf.length) {
//                System.out.println("not a whole read");
                len += con.read(buf, len, buf.length - len);
            }
            if (!Arrays.equals(buf, bufC)) {
                System.out.println("buffers dont match!");
            } else {
                System.arraycopy(bufC, 0, buf, 0, buf.length);
            }
        }
    }
}
