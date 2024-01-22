package src;

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
    
}
