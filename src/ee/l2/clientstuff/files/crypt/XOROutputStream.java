package ee.l2.clientstuff.files.crypt;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author acmi
 */
public class XOROutputStream extends OutputStream {
    protected OutputStream output;
    private XORKeyGen keyGen;
    private int ind;

    public XOROutputStream(OutputStream output, XORKeyGen keyGen) {
        this.output = output;
        this.keyGen = keyGen;
    }

    @Override
    public void write(int b) throws IOException {
        output.write(b ^ keyGen.getKey(ind++));
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
