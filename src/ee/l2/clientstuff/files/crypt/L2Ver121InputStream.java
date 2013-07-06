package ee.l2.clientstuff.files.crypt;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author acmi
 */
public class L2Ver121InputStream extends InputStream {
    private InputStream input;

    public L2Ver121InputStream(InputStream input) {
        this.input = input;
    }

    @Override
    public int read() throws IOException {
        int b = input.read();
        if (b < 0)
            return b;

        return b ^ 0x22;
    }

    @Override
    public int available() throws IOException {
        return input.available();
    }

    @Override
    public void close() throws IOException {
        input.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        input.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        input.reset();
    }

    @Override
    public boolean markSupported() {
        return input.markSupported();
    }
}
