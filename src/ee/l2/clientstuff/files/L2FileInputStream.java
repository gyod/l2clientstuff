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
package ee.l2.clientstuff.files;

import ee.l2.clientstuff.files.crypt.blowfish.*;
import ee.l2.clientstuff.files.crypt.rsa.*;
import ee.l2.clientstuff.files.crypt.xor.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import static ee.l2.clientstuff.files.crypt.blowfish.L2Ver21xInputStream.*;
import static ee.l2.clientstuff.files.crypt.rsa.L2Ver41xInputStream.*;

/**
 * @author acmi
 */
public class L2FileInputStream extends InputStream {
    private InputStream stream;

    public L2FileInputStream(InputStream input, String name, boolean l2encdec) throws IOException {
        stream = getInputStream(input, name, l2encdec);
    }

    public L2FileInputStream(InputStream input, String name) throws IOException {
        this(input, name, false);
    }

    private InputStream getInputStream(InputStream input, String name, boolean l2encdec) throws IOException {
        int version = readVersion(input);

        switch (version) {
            //XOR
            case 111:
                return new L2Ver111InputStream(input);
            case 120:
                return new L2Ver120InputStream(input);
            case 121:
                return new L2Ver121InputStream(input, L2Ver121.getXORKey(name));
            //BLOWFISH
            case 211:
            case 212:
                return new L2Ver21xInputStream(input, version == 211 ?
                        BLOWFISH_KEY_211 :
                        BLOWFISH_KEY_212);
            //RSA
            case 411:
                return new L2Ver41xInputStream(input,
                        l2encdec ? MODULUS_L2ENCDEC : MODULUS_411,
                        l2encdec ? PRIVATE_EXPONENT_L2ENCDEC : PRIVATE_EXPONENT_411);
            case 412:
                return new L2Ver41xInputStream(input,
                        l2encdec ? MODULUS_L2ENCDEC : MODULUS_412,
                        l2encdec ? PRIVATE_EXPONENT_L2ENCDEC : PRIVATE_EXPONENT_412);
            case 413:
                return new L2Ver41xInputStream(input,
                        l2encdec ? MODULUS_L2ENCDEC : MODULUS_413,
                        l2encdec ? PRIVATE_EXPONENT_L2ENCDEC : PRIVATE_EXPONENT_413);
            case 414:
                return new L2Ver41xInputStream(input,
                        l2encdec ? MODULUS_L2ENCDEC : MODULUS_414,
                        l2encdec ? PRIVATE_EXPONENT_L2ENCDEC : PRIVATE_EXPONENT_414);
            default:
                throw new RuntimeException("Unsupported version: " + version);
        }
    }

    private int readVersion(InputStream input) throws IOException {
        byte[] header = new byte[28];
        input.read(header);
        String headerStr = new String(header, Charset.forName("utf-16le"));
        if (!Pattern.compile("Lineage2Ver\\d{3}").matcher(headerStr).matches())
            throw new IOException("Not a Lineage 2 file");

        return Integer.valueOf(headerStr.substring(11));
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        stream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        stream.reset();
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }
}
