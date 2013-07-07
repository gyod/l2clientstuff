package ee.l2.clientstuff.files.crypt;

import java.io.InputStream;

/**
 * @author acmi
 */
public class L2Ver211InputStream extends BlowfishInputStream implements L2Ver211{

    public L2Ver211InputStream(InputStream input) {
        super(input, BLOWFISH_KEY);
    }
}
