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
package ee.l2.clientstuff.files.streams.l2file;

import ee.l2.clientstuff.files.streams.l2file.crypt.blowfish.*;
import ee.l2.clientstuff.files.streams.l2file.crypt.rsa.*;
import ee.l2.clientstuff.files.streams.l2file.crypt.xor.*;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.regex.Pattern;

import static ee.l2.clientstuff.files.streams.l2file.crypt.blowfish.L2Ver21xInputStream.*;
import static ee.l2.clientstuff.files.streams.l2file.crypt.rsa.L2Ver41xInputStream.*;

/**
 * @author acmi
 */
public class L2FileInputStream extends FilterInputStream {

    public L2FileInputStream(InputStream input, String name, boolean l2encdec) throws IOException {
        super(getInputStream(Objects.requireNonNull(input), name, l2encdec));
    }

    public L2FileInputStream(InputStream input, String name) throws IOException {
        this(input, name, false);
    }

    public static InputStream getInputStream(InputStream input, String name, boolean l2encdec) throws IOException {
        int version = readVersion(input);

        switch (version) {
            //XOR
            case 111:
            case 121:
                return new L2Ver1x1InputStream(input, version == 111 ?
                        L2Ver1x1.XOR_KEY_111 :
                        L2Ver1x1.getXORKey121(name));
            case 120:
                return new L2Ver120InputStream(input);
            //BLOWFISH
            case 211:
            case 212:
                return new L2Ver21xInputStream(input, version == 211 ?
                        BLOWFISH_KEY_211 :
                        BLOWFISH_KEY_212);
            //RSA
            case 411:
            case 412:
            case 413:
            case 414:
                BigInteger modulus = l2encdec ? MODULUS_L2ENCDEC : RSA_KEYS[version-411][0];
                BigInteger exponent = l2encdec ? PRIVATE_EXPONENT_L2ENCDEC : RSA_KEYS[version-411][1];
                try {
                    return new L2Ver41xInputStream(input, modulus, exponent);
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            default:
                throw new RuntimeException("Unsupported version: " + version);
        }
    }

    public static int readVersion(InputStream input) throws IOException {
        byte[] header = new byte[28];
        input.read(header);
        String headerStr = new String(header, Charset.forName("utf-16le"));
        if (!Pattern.compile("Lineage2Ver\\d{3}").matcher(headerStr).matches())
            throw new IOException("Not a Lineage 2 file");

        return Integer.valueOf(headerStr.substring(11));
    }

    private static final BigInteger RSA_KEYS[][] = new BigInteger[][]{
            {MODULUS_411, PRIVATE_EXPONENT_411},
            {MODULUS_412, PRIVATE_EXPONENT_412},
            {MODULUS_413, PRIVATE_EXPONENT_413},
            {MODULUS_414, PRIVATE_EXPONENT_414}
    };
}
