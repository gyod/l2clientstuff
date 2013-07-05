package ee.l2.clientstuff.files;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

/**
 *
 * @author acmi
 */
public class L2FileInputStream extends InputStream {
    public final static BigInteger EXPONENT_ORIGINAL = new BigInteger("35", 16);
    public final static BigInteger MODULUS_ORIGINAL = new BigInteger(
            "97df398472ddf737ef0a0cd17e8d172f" +
            "0fef1661a38a8ae1d6e829bc1c6e4c3c" +
            "fc19292dda9ef90175e46e7394a18850" +
            "b6417d03be6eea274d3ed1dde5b5d7bd" +
            "e72cc0a0b71d03608655633881793a02" +
            "c9a67d9ef2b45eb7c08d4be329083ce4" +
            "50e68f7867b6749314d40511d09bc574" +
            "4551baa86a89dc38123dc1668fd72d83", 16);

    public final static BigInteger EXPONENT_L2ENCDEC = new BigInteger("1d", 16);
    public final static BigInteger MODULUS_L2ENCDEC = new BigInteger(
            "75b4d6de5c016544068a1acf125869f4"+
            "3d2e09fc55b8b1e289556daf9b875763"+
            "5593446288b3653da1ce91c87bb1a5c1"+
            "8f16323495c55d7d72c0890a83f69bfd"+
            "1fd9434eb1c02f3e4679edfa43309319"+
            "070129c267c85604d87bb65bae205de3"+
            "707af1d2108881abb567c3b3d069ae67"+
            "c3a4c6a3aa93d26413d4c66094ae2039", 16);

    private InflaterInputStream inflaterInputStream;
    private int size;
    private int got;

    public L2FileInputStream(InputStream input, BigInteger modulus, BigInteger exponent) throws IOException {
        readHeader(input);

        DecryptInputStream dis = new DecryptInputStream(input, modulus, exponent);

        for (int i=0; i<=24; i+=8){
            int t = dis.read();
            if (t < 0)
                throw new EOFException();

            size += t << i;
        }

        inflaterInputStream = new InflaterInputStream(dis);
    }

    public L2FileInputStream(InputStream input) throws IOException {
        this(input, MODULUS_ORIGINAL, EXPONENT_ORIGINAL);
    }

    private void readHeader(InputStream input) throws IOException {
        byte[] header = new byte[28];
        input.read(header);
        String headerStr = new String(header, Charset.forName("utf-16le"));
        if (!Pattern.compile("Lineage2Ver\\w{3}").matcher(headerStr).matches())
            throw new IOException("Not a Lineage 2 file");

        if (!headerStr.substring(11).equals("413"))
            throw new IOException("Unknown version " + headerStr.substring(11));
    }

    @Override
    public int read() throws IOException {
        if (got == size)
            throw new EOFException();

        got++;
        return inflaterInputStream.read();
    }

    @Override
    public int available() throws IOException {
        return size - got;
    }

    @Override
    public void close() throws IOException {
        inflaterInputStream.close();
    }

    public static class DecryptInputStream extends InputStream {
        private InputStream input;

        private BigInteger modulus, exponent;

        private ByteBuffer buffer = ByteBuffer.allocate(124);
        {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(buffer.limit());
        }

        public DecryptInputStream(InputStream input, BigInteger modulus, BigInteger exponent) {
            this.input = input;

            this.modulus = modulus;
            this.exponent = exponent;
        }

        @Override
        public int read() throws IOException {
            if (buffer.position() == buffer.limit()) {
                buffer.clear();
                fillBuffer();
                buffer.position(0);
            }

            return buffer.get() & 0xff;
        }

        private void fillBuffer() throws IOException {
            StringBuilder sb = new StringBuilder(256);
            for (int i = 0; i < 128; i++) {
                int b = input.read();
                if (b < 0x10)
                    sb.append("0");
                sb.append(Integer.toHexString(b));
            }

            ByteBuffer block = ByteBuffer.wrap(new BigInteger(sb.toString(), 16).modPow(exponent, modulus).toByteArray());

            if (block.array().length != 125)
                throw new IOException("block size invalid");

            int size = block.get() & 0xff;
            if (size > 124)
                throw new IOException("block data size too big");

            int p = block.capacity() - size;
            while (p > 1 && block.array()[p - 1] != '\0') p--;

            buffer.put(block.array(), p, size);
        }

        @Override
        public void close() throws IOException {
            input.close();
        }
    }
}
