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

import ee.l2.clientstuff.files.streams.FinishableOutputStream;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;

/**
 * @author acmi
 */
public class RSAOutputStream extends FinishableOutputStream {
    private OutputStream output;

    private Cipher cipher;

    private ByteBuffer dataBuffer = ByteBuffer.allocate(124);

    private boolean finished;

    public RSAOutputStream(OutputStream output, BigInteger modulus, BigInteger exponent) throws GeneralSecurityException {
        this.output = output;

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
        cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(keySpec));
    }

    @Override
    public void write(int b) throws IOException {
        if (finished)
            throw new IOException("write beyond end of stream");

        dataBuffer.put((byte) b);
        if (dataBuffer.position() == dataBuffer.limit()) {
            writeData();

            dataBuffer.clear();
        }
    }

    @Override
    public void flush() throws IOException {
        output.flush();
    }

    @Override
    public void finish() throws IOException {
        if (finished)
            return;

        finished = true;
        writeData();
        flush();
    }

    @Override
    public void close() throws IOException {
        super.close();

        output.close();
    }

    private void writeData() throws IOException {
        int size = dataBuffer.position();
        if (size == 0)
            return;

        byte[] block = new byte[125];
        block[0] = (byte) (size & 0xff);
        System.arraycopy(dataBuffer.array(), 0, block, 125 - size - ((124 - size) % 4), size);

        try {
            output.write(cipher.doFinal(block));
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }
}
