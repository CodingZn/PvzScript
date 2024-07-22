package src.api;

import java.io.IOException;
import java.io.OutputStream;

public class MyTeeOutput extends OutputStream {
    private OutputStream oStream1;
    private OutputStream oStream2;

    public MyTeeOutput(OutputStream o1, OutputStream o2){
        oStream1 = o1;
        oStream2 = o2;
    }

    @Override
    public void write(int b) throws IOException {
        oStream1.write(b);
        oStream2.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        oStream1.write(b);
        oStream2.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        oStream1.write(b, off, len);
        oStream2.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        oStream1.flush();
        oStream2.flush();
    }

    @Override
    public void close() throws IOException {
        oStream1.close();
        oStream2.close();
    }
    
}
