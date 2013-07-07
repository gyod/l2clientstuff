package ee.l2.clientstuff.files.crypt;

import java.io.OutputStream;

/**
 * @author acmi
 */
public class L2Ver211OutputStream extends BlowfishOutputStream implements L2Ver211{

    public L2Ver211OutputStream(OutputStream output){
        super(output, BLOWFISH_KEY);
    }
}
