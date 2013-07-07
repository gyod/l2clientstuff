package ee.l2.clientstuff.files.crypt;

import java.io.OutputStream;

/**
 * @author acmi
 */
public class L2Ver121OutputStream extends XOROutputStream {

    public L2Ver121OutputStream(OutputStream output) {
        super(output, new L2Ver121XORKeyGen());
    }
}
