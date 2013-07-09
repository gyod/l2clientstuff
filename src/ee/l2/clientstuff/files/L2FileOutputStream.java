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

import static ee.l2.clientstuff.files.crypt.blowfish.L2Ver21xOutputStream.*;
import static ee.l2.clientstuff.files.crypt.rsa.L2Ver41xOutputStream.*;

/**
 * @author acmi
 */
public class L2FileOutputStream extends FinishableOutputStream {
    private OutputStream output;
    private FinishableOutputStream stream;
    private boolean writeTail;

    private boolean finished;

    public L2FileOutputStream(OutputStream output, String name, int version, boolean writeTail) throws IOException {
        this.output = output;
        this.stream = getOutputStream(output, name, version);

        this.writeTail = writeTail;

        writeHeader(version);
    }

    private void writeHeader(int version) throws IOException {
        output.write(("Lineage2Ver" + version).getBytes(Charset.forName("utf-16le")));
    }

    private FinishableOutputStream getOutputStream(OutputStream output, String name, int version) {
        switch (version) {
            //XOR
            case 111:
                return new L2Ver111OutputStream(output);
            case 120:
                return new L2Ver120OutputStream(output);
            case 121:
                return new L2Ver121OutputStream(output, L2Ver121.getXORKey(name));
            //BLOWFISH
            case 211:
            case 212:
                return new L2Ver21xOutputStream(output, version == 211 ?
                        BLOWFISH_KEY_211 :
                        BLOWFISH_KEY_212);
            //RSA
            case 411:
            case 412:
            case 413:
            case 414:
                return new L2Ver41xOutputStream(output, MODULUS_L2ENCDEC, PUBLIC_EXPONENT_L2ENCDEC);
            default:
                throw new RuntimeException("Unsupported version: " + version);
        }
    }

    @Override
    public void write(int b) throws IOException {
        stream.write(b);
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void finish() throws IOException {
        if (finished)
            return;

        finished = true;

        stream.finish();

        if (writeTail) {
            //TODO
            for (int i = 0; i < 20; i++)
                output.write(0);
        }
        output.flush();
    }

    @Override
    public void close() throws IOException {
        finish();
        output.close();
    }
}
