package ee.l2.clientstuff.files.crypt;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.zip.InflaterInputStream;

/**
 * @author acmi
 */
public class RSADeflateInputStream extends InputStream {
    private InputStream stream;

    private int size;
    private int got;

    public RSADeflateInputStream(InputStream input, BigInteger modulus, BigInteger exponent) throws GeneralSecurityException, IOException {
        RSAInputStream rsaInputStream = new RSAInputStream(input, modulus, exponent);

        DataInputStream dataInputStream = new DataInputStream(rsaInputStream);
        size = Integer.reverseBytes(dataInputStream.readInt());  //Little endian

        stream = new InflaterInputStream(rsaInputStream);
    }

    @Override
    public int read() throws IOException {
        int b = stream.read();
        if (got < size) got++;

        return b;
    }

    @Override
    public int available() throws IOException {
        return size - got;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
