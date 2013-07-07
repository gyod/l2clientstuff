package ee.l2.clientstuff.files.crypt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * @author acmi
 */
public class BlowfishOutputStream extends OutputStream {
    private OutputStream output;

    private BlowfishEngine blowfish = new BlowfishEngine();

    private byte[] writeBuffer = new byte[8];
    private byte[] dataBuffer = new byte[8];
    private int pos;

    public BlowfishOutputStream(OutputStream output, byte[] key) {
        this.output = output;

        blowfish.init(true, key);
    }

    @Override
    public void write(int b) throws IOException {
        dataBuffer[pos++] = (byte) b;
        if (pos == dataBuffer.length)
            flush();
    }

    @Override
    public void flush() throws IOException {
        if (pos == 0)
            return;

        blowfish.processBlock(dataBuffer, 0, writeBuffer, 0);
        output.write(writeBuffer);
        Arrays.fill(writeBuffer, (byte)0);
        pos = 0;
    }

    @Override
    public void close() throws IOException {
        output.close();
    }
}
