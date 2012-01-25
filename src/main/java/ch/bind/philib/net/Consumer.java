package ch.bind.philib.net;

public interface Consumer {

    void receive(byte[] data);
    
    void closed();
    
}
