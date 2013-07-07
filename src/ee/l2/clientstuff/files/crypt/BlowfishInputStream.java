package ee.l2.clientstuff.files.crypt;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author acmi
 */
public class BlowfishInputStream extends InputStream {
    private InputStream input;

    private BlowfishEngine blowfish = new BlowfishEngine();

    private byte[] readBuffer = new byte[8];
    private byte[] dataBuffer = new byte[8];
    private int pos = dataBuffer.length - 1;

    public BlowfishInputStream(InputStream input, byte[] key){
        this.input = input;

        blowfish.init(false, key);
    }

    @Override
    public int read() throws IOException {
        if (pos == dataBuffer.length - 1) {
            int r = input.read(readBuffer);
            if (r != readBuffer.length)
                return -1;

            blowfish.processBlock(readBuffer, 0, dataBuffer, 0);
        }
        pos++;
        pos &= 0b111;
        return dataBuffer[pos];
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
