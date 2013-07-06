package ee.l2.clientstuff.files.crypt;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author acmi
 */
public class L2Ver120InputStream extends InputStream {
    private InputStream input;

    private int ind = 0xE6;

    public L2Ver120InputStream(InputStream input) {
        this.input = input;
    }

    @Override
    public int read() throws IOException {
        int b = input.read();
        if (b < 0)
            return b;

        b ^= L2Ver120.getXORByte(ind);
        ind++;
        ind &= 0xffff;

        return b;
    }

    @Override
    public int available() throws IOException {
        return input.available();
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
