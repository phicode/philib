package ch.bind.philib.io;

public interface DoubleSidedBuffer {

    int available();

    void read(byte[] data);
    
    void read(byte[] data, int off, int len);

    void readBack(byte[] data);
    
    void readBack(byte[] data, int off, int len);

    void write(byte[] data);
    
    void write(byte[] data, int off, int len);

    void writeFront(byte[] data);
    
    void writeFront(byte[] data, int off, int len);
}
