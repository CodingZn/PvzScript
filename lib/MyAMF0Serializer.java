package lib;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.exadel.flamingo.flex.amf.AMF0Body;
import com.exadel.flamingo.flex.messaging.amf.io.AMF0Serializer;

public class MyAMF0Serializer extends AMF0Serializer{

    public MyAMF0Serializer(DataOutputStream outputStream) {
        super(outputStream);
    }
    
    @Override
    protected void writeBody(AMF0Body body) throws IOException {
        if (body.getTarget() == null) {
            this.outputStream.writeUTF("null");
        } else {
            this.outputStream.writeUTF(body.getTarget());
        }

        if (body.getResponse() == null) {
            this.outputStream.writeUTF("null");
        } else {
            this.outputStream.writeUTF(body.getResponse());
        }
        // switch to tmpstream
        ByteArrayOutputStream tmpStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = this.outputStream;
        this.outputStream = new DataOutputStream(tmpStream);
        // measure byte len
        this.writeData(body.getValue());
        int bytelen = tmpStream.size();
        tmpStream.close();
        // switch back
        this.outputStream = outputStream;
        // write as normal
        this.outputStream.writeInt(bytelen);
        this.writeData(body.getValue());
    }
}
