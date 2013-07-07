package ee.l2.clientstuff.files.crypt;

import java.io.InputStream;

/**
 * @author acmi
 */
public class L2Ver212InputStream extends BlowfishInputStream implements L2Ver212{

    public L2Ver212InputStream(InputStream input) {
        super(input, BLOWFISH_KEY);
    }
}
