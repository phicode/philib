package ch.bind.philib.net;

import java.io.Closeable;

public interface Connection extends Closeable, Selectable {

    void send(byte[] data);

}
