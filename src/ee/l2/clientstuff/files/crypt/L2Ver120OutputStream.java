package ee.l2.clientstuff.files.crypt;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author acmi
 */
public class L2Ver120OutputStream extends OutputStream {
    private OutputStream output;

    private int ind = 0xE6;

    public L2Ver120OutputStream(OutputStream output) {
        this.output = output;
    }

    @Override
    public void write(int b) throws IOException {
        output.write(b ^ L2Ver120.getXORByte(ind));
        ind++;
        ind &= 0xffff;
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }

    @Override
    public void close() throws IOException {
        output.close();
    }
}
