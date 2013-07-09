/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ee.l2.clientstuff.files.streams.l2file.crypt.rsa;

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

    private boolean closed;

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
        if (closed)
            throw new IOException("Stream closed");

        if (dataBuffer.position() == dataBuffer.limit()) {
            if (input.read(readBuffer) != readBuffer.length)
                return -1;

            ByteBuffer block;
            try {
                block = ByteBuffer.wrap(cipher.doFinal(readBuffer), 3, 125);
            } catch (GeneralSecurityException | IndexOutOfBoundsException e) {
                throw new IOException(e);
            }

            int size = block.get() & 0xff;
            if (size > 124)
                throw new IOException("block data size too large");

            dataBuffer.clear();
            dataBuffer.put(block.array(), 128 - size - ((124 - size) % 4), size);
            dataBuffer.flip();
        }

        return dataBuffer.get() & 0xff;
    }

    @Override
    public void close() throws IOException {
        if (closed)
            return;

        closed = true;
        input.close();
    }
}
