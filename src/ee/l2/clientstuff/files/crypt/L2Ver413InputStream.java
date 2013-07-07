package ee.l2.clientstuff.files.crypt;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

/**
 * @author acmi
 */
public class L2Ver413InputStream extends RSADeflateInputStream{
    public L2Ver413InputStream(InputStream input, boolean l2encdec) throws GeneralSecurityException, IOException {
        super(input,
                l2encdec ? MODULUS_L2ENCDEC : MODULUS_ORIGINAL,
                l2encdec ? PRIVATE_EXPONENT_L2ENCDEC : PRIVATE_EXPONENT_ORIGINAL);
    }

    public final static BigInteger PRIVATE_EXPONENT_ORIGINAL = new BigInteger("35", 16);
    public final static BigInteger MODULUS_ORIGINAL = new BigInteger(
            "97df398472ddf737ef0a0cd17e8d172f" +
                    "0fef1661a38a8ae1d6e829bc1c6e4c3c" +
                    "fc19292dda9ef90175e46e7394a18850" +
                    "b6417d03be6eea274d3ed1dde5b5d7bd" +
                    "e72cc0a0b71d03608655633881793a02" +
                    "c9a67d9ef2b45eb7c08d4be329083ce4" +
                    "50e68f7867b6749314d40511d09bc574" +
                    "4551baa86a89dc38123dc1668fd72d83", 16);

    public final static BigInteger PRIVATE_EXPONENT_L2ENCDEC = new BigInteger("1d", 16);
    public final static BigInteger MODULUS_L2ENCDEC = new BigInteger(
            "75b4d6de5c016544068a1acf125869f4" +
                    "3d2e09fc55b8b1e289556daf9b875763" +
                    "5593446288b3653da1ce91c87bb1a5c1" +
                    "8f16323495c55d7d72c0890a83f69bfd" +
                    "1fd9434eb1c02f3e4679edfa43309319" +
                    "070129c267c85604d87bb65bae205de3" +
                    "707af1d2108881abb567c3b3d069ae67" +
                    "c3a4c6a3aa93d26413d4c66094ae2039", 16);
}
