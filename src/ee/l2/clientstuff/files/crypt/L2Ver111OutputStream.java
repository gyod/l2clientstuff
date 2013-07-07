package ee.l2.clientstuff.files.crypt;

import java.io.OutputStream;

/**
 * @author acmi
 */
public class L2Ver111OutputStream extends XOROutputStream {

    public L2Ver111OutputStream(OutputStream output) {
        super(output, new L2Ver111XORKeyGen());
    }
}
