package ee.l2.clientstuff.files.crypt;

import java.io.OutputStream;

/**
 * @author acmi
 */
public class L2Ver212OutputStream extends BlowfishOutputStream implements L2Ver212{

    public L2Ver212OutputStream(OutputStream output){
        super(output, BLOWFISH_KEY);
    }
}
