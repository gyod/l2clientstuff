package ee.l2.clientstuff.files;

import javax.crypto.Cipher;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.RSAPrivateKeySpec;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

/**
 * Thanks Hint for algorithm and keys.
 *
 * @author acmi
 */
public class L2FileInputStream extends InputStream {
    public final static BigInteger PRIVATE_EXPONENT_ORIGINAL = new BigInteger("35", 16);
    public final static BigInteger MODULUS_ORIGINAL = new BigInteger(
            "97df398472ddf737ef0a0cd17e8d172f" +
                    "0fef1661a38a8ae1d6e829bc1c6e4c3c" +
                    "fc19292dda9ef90175e46e7394a18850" +
                    "b6417d03be6eea274d3ed1dde5b5d7bd" +
                    "e72cc0a0b71d03608655633881793a02" +
                    "c9a67d9ef2b45eb7c08d4be329083ce4" +
                    "50e68f7867b6749314d40511d09bc574" +
                    "4551baa86a89dc38123dc1668fd72d83", 16);

    public final static BigInteger PRIVATE_EXPONENT_L2ENCDEC = new BigInteger("1d", 16);
    public final static BigInteger MODULUS_L2ENCDEC = new BigInteger(
            "75b4d6de5c016544068a1acf125869f4" +
                    "3d2e09fc55b8b1e289556daf9b875763" +
                    "5593446288b3653da1ce91c87bb1a5c1" +
                    "8f16323495c55d7d72c0890a83f69bfd" +
                    "1fd9434eb1c02f3e4679edfa43309319" +
                    "070129c267c85604d87bb65bae205de3" +
                    "707af1d2108881abb567c3b3d069ae67" +
                    "c3a4c6a3aa93d26413d4c66094ae2039", 16);

    private InputStream stream;
    private int size;
    private int got;

    public L2FileInputStream(InputStream input, BigInteger modulus, BigInteger exponent) throws IOException, GeneralSecurityException {
        readHeader(input);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, exponent);
        Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keyFactory.generatePrivate(keySpec));

        CipherInputStream cipherInputStream = new CipherInputStream(input, cipher);

        DataInputStream dataInputStream = new DataInputStream(cipherInputStream);
        size = Integer.reverseBytes(dataInputStream.readInt());  //Little endian

        stream = new InflaterInputStream(cipherInputStream);
    }

    public L2FileInputStream(InputStream input) throws IOException, GeneralSecurityException {
        this(input, MODULUS_ORIGINAL, PRIVATE_EXPONENT_ORIGINAL);
    }

    private void readHeader(InputStream input) throws IOException {
        byte[] header = new byte[28];
        input.read(header);
        String headerStr = new String(header, Charset.forName("utf-16le"));
        if (!Pattern.compile("Lineage2Ver\\w{3}").matcher(headerStr).matches())
            throw new IOException("Not a Lineage 2 file");

        String ver = headerStr.substring(11);
        if (!Pattern.compile("41\\d").matcher(ver).matches())
            throw new IOException("Unknown version " + ver);
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

    public static class CipherInputStream extends InputStream {
        private InputStream input;

        private Cipher cipher;

        private byte[] readBuffer = new byte[128];
        private ByteBuffer dataBuffer = ByteBuffer.allocate(124);

        {
            dataBuffer.position(dataBuffer.limit());
        }

        public CipherInputStream(InputStream input, Cipher cipher) {
            this.input = input;

            this.cipher = cipher;
        }

        @Override
        public int read() throws IOException {
            if (dataBuffer.position() == dataBuffer.limit()) {
                dataBuffer.clear();
                fillBuffer();
                dataBuffer.position(0);
            }

            return dataBuffer.get() & 0xff;
        }

        private void fillBuffer() throws IOException {
            input.read(readBuffer);
            ByteBuffer block;
            try {
                block = ByteBuffer.wrap(cipher.doFinal(readBuffer), 3, 125);
            } catch (GeneralSecurityException | IndexOutOfBoundsException e) {
                throw new IOException(e);
            }

            int size = block.get() & 0xff;
            if (size > 124)
                throw new IOException("block data size too large");

            int p = block.capacity() - size;
            while (p > 4 && block.array()[p - 1] != '\0') p--;

            dataBuffer.put(block.array(), p, size);
        }

        @Override
        public void close() throws IOException {
            input.close();
        }
    }
}
