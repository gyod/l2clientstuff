package ee.l2.clientstuff.files.crypt;

import java.io.OutputStream;

/**
 * @author acmi
 */
public class L2Ver120OutputStream extends XOROutputStream {

    public L2Ver120OutputStream(OutputStream output) {
        super(output, new L2Ver120XORKeyGen());
    }
}
