package ee.l2.clientstuff.files.crypt;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author acmi
 */
public class XORInputStream extends InputStream{
    protected InputStream input;
    private XORKeyGen keyGen;
    private int ind;

    public XORInputStream(InputStream input, XORKeyGen keyGen) {
        this.input = input;
        this.keyGen = keyGen;
    }

    @Override
    public int read() throws IOException {
        int b = input.read();
        return b < 0 ? b : b ^ keyGen.getKey(ind++);
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
