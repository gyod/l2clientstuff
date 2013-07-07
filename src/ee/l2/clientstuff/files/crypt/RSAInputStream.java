package ee.l2.clientstuff.files.crypt;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.RSAPrivateKeySpec;

/**
 * @author acmi
 */
public class RSAInputStream extends InputStream {
    private InputStream input;

    private Cipher cipher;

    private byte[] readBuffer = new byte[128];
    private ByteBuffer dataBuffer = ByteBuffer.allocate(124);
    {
        dataBuffer.position(dataBuffer.limit());
    }

    public RSAInputStream(InputStream input, BigInteger modulus, BigInteger exponent) throws GeneralSecurityException{
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, exponent);
        Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, keyFactory.generatePrivate(keySpec));

        this.input = input;
        this.cipher = cipher;
    }

    @Override
    public int read() throws IOException {
        if (dataBuffer.position() == dataBuffer.limit()) {
            if (input.read(readBuffer) != readBuffer.length)
                return -1;

            ByteBuffer block;
            try {
                block = ByteBuffer.wrap(cipher.doFinal(readBuffer), 3, 125);
            } catch (GeneralSecurityException | IndexOutOfBoundsException e) {
                throw new IOException(e);
            }

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 128; i++) {
                int b = block.array()[i] & 0xff;
                if (b < 0x10)
                    sb.append("0");
                sb.append(Integer.toHexString(b));
            }
            System.out.println(sb);

            int size = block.get() & 0xff;
            if (size > 124)
                throw new IOException("block data size too large");

            int p = block.capacity() - size;
            while (p > 4 && block.array()[p - 1] != '\0') p--;

            dataBuffer.clear();
            dataBuffer.put(block.array(), p, size);
            dataBuffer.flip();
        }

        return dataBuffer.get() & 0xff;
    }

    @Override
    public void close() throws IOException {
        input.close();
    }
}
