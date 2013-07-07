package ee.l2.clientstuff.files.crypt;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author acmi
 */
public class L2Ver111InputStream extends XORInputStream {

    public L2Ver111InputStream(InputStream input) {
        super(input, new L2Ver111XORKeyGen());
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
