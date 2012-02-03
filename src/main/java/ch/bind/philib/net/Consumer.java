package ch.bind.philib.net;

import java.io.IOException;

public interface Consumer {

    void receive(byte[] data) throws IOException;
    
    void closed();
    
}
