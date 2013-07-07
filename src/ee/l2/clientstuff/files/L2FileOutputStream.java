package ee.l2.clientstuff.files;

import ee.l2.clientstuff.files.crypt.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author acmi
 */
public class L2FileOutputStream extends OutputStream {
    private OutputStream output;
    private OutputStream stream;
    private boolean writeChecksum;

    public L2FileOutputStream(OutputStream output, int version, boolean writeChecksum) throws IOException {
        this.output = output;
        try {
            this.stream = getOutputStream(output, version);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.writeChecksum = writeChecksum;

        writeHeader(version);
    }

    private void writeHeader(int version) throws IOException{
        if (version < 100 || version > 999)
            throw new IOException("Invalid version "+version);

        output.write(("Lineage2Ver" + version).getBytes(Charset.forName("utf-16le")));
    }

    private OutputStream getOutputStream(OutputStream output, int version) throws Exception {
        switch (version) {
            //XOR
            case 111:
                return new L2Ver111OutputStream(output);
            case 120:
                return new L2Ver120OutputStream(output);
            case 121:
                return new L2Ver121OutputStream(output);
            //BLOWFISH
            case 211:
                return new L2Ver211OutputStream(output);
            case 212:
                return new L2Ver212OutputStream(output);
            //RSA
            case 411:
                throw new RuntimeException("Not supported yet");
            case 412:
                throw new RuntimeException("Not supported yet");
            case 413:
                throw new RuntimeException("Not supported yet");
            case 414:
                throw new RuntimeException("Not supported yet");
            default:
                throw new RuntimeException("Unsupported version: " + version);
        }
    }

    @Override
    public void write(int b) throws IOException {
        stream.write(b);
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void close() throws IOException {
        flush();
        if (writeChecksum){
            //TODO
            for (int i=0; i<20; i++)
                output.write(0);
        }
        output.close();
    }
}
