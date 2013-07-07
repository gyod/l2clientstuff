package ee.l2.clientstuff.files;

import ee.l2.clientstuff.files.crypt.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * @author acmi
 */
public class L2FileInputStream extends InputStream {
    private volatile InputStream stream;

    public L2FileInputStream(InputStream input, boolean l2encdec) throws IOException {
        try {
            stream = getInputStream(input, l2encdec);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public L2FileInputStream(InputStream input) throws IOException{
        this(input, false);
    }

    private InputStream getInputStream(InputStream input, boolean l2encdec) throws Exception {
        int version = readVersion(input);

        switch (version) {
            //XOR
            case 111:
                return new L2Ver111InputStream(input);
            case 120:
                return new L2Ver120InputStream(input);
            case 121:
                return new L2Ver121InputStream(input);
            //BLOWFISH
            case 211:
                return new L2Ver211InputStream(input);
            case 212:
                return new L2Ver212InputStream(input);
            //RSA
            case 411:
                throw new RuntimeException("Not supported yet");
            case 412:
                throw new RuntimeException("Not supported yet");
            case 413:
                return new L2Ver413InputStream(input, l2encdec);
            case 414:
                throw new RuntimeException("Not supported yet");
            default:
                throw new RuntimeException("Unsupported version: " + version);
        }
    }

    private int readVersion(InputStream input) throws IOException {
        byte[] header = new byte[28];
        input.read(header);
        String headerStr = new String(header, Charset.forName("utf-16le"));
        if (!Pattern.compile("Lineage2Ver\\w{3}").matcher(headerStr).matches())
            throw new IOException("Not a Lineage 2 file");

        try {
            return Integer.valueOf(headerStr.substring(11));
        } catch (NumberFormatException nfe) {
            throw new IOException("Version must be an 3-digit integer.");
        }
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        stream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        stream.reset();
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }
}
