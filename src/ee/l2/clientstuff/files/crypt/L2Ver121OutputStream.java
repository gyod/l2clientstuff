package ee.l2.clientstuff.files.crypt;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author acmi
 */
public class L2Ver121OutputStream extends OutputStream {
    private OutputStream output;

    public L2Ver121OutputStream(OutputStream output) {
        this.output = output;
    }

    @Override
    public void write(int b) throws IOException {
        output.write(b ^ 0x22);
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
