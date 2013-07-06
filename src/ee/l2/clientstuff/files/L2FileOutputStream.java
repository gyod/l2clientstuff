package ee.l2.clientstuff.files;

import ee.l2.clientstuff.files.crypt.L2Ver111OutputStream;
import ee.l2.clientstuff.files.crypt.L2Ver121OutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author acmi
 */
public class L2FileOutputStream extends OutputStream {
    private OutputStream output;

    public L2FileOutputStream(OutputStream output, int version) throws IOException {
        try {
            this.output = getOutputStream(output, version);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        output.write(("Lineage2Ver" + version).getBytes(Charset.forName("utf-16le")));
    }

    private OutputStream getOutputStream(OutputStream output, int version) throws Exception {
        switch (version) {
            //XOR
            case 111:
                return new L2Ver111OutputStream(output);
            case 120:
                new RuntimeException("Not supported yet");
            case 121:
                return new L2Ver121OutputStream(output);
            //BLOWFISH
            case 211:
                new RuntimeException("Not supported yet");
            case 212:
                new RuntimeException("Not supported yet");
                //RSA
            case 411:
                new RuntimeException("Not supported yet");
            case 412:
                new RuntimeException("Not supported yet");
            case 413:
                new RuntimeException("Not supported yet");
            case 414:
                new RuntimeException("Not supported yet");
            default:
                throw new RuntimeException("Unsupported version: " + version);
        }
    }

    @Override
    public void write(int b) throws IOException {
        output.write(b);
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
