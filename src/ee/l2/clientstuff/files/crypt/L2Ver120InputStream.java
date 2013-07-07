package ee.l2.clientstuff.files.crypt;

import java.io.InputStream;

/**
 * @author acmi
 */
public class L2Ver120InputStream extends XORInputStream {

    public L2Ver120InputStream(InputStream input) {
        super(input, new L2Ver120XORKeyGen());
    }
}
