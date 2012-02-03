package ch.bind.philib.net;

import java.io.Closeable;
import java.io.IOException;

public interface Connection extends Closeable, Selectable {

    void send(byte[] data) throws IOException;

}
