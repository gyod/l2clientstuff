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
package ee.l2.clientstuff.l2encdec;

import ee.l2.clientstuff.files.streams.l2file.L2FileInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

/**
 * @author acmi
 */
public class L2DecTask implements Callable<Void> {
    private InputStream input;
    private OutputStream output;
    private String key;
    private boolean l2encdecKey;

    public L2DecTask() {
    }

    public L2DecTask(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    public L2DecTask(InputStream input, OutputStream output, String key) {
        this.input = input;
        this.output = output;
        this.key = key;
    }

    public L2DecTask(InputStream input, OutputStream output, boolean l2encdecKey) {
        this.input = input;
        this.output = output;
        this.l2encdecKey = l2encdecKey;
    }

    public L2DecTask(InputStream input, OutputStream output, String key, boolean l2encdecKey) {
        this.input = input;
        this.output = output;
        this.key = key;
        this.l2encdecKey = l2encdecKey;
    }

    public InputStream getInput() {
        return input;
    }

    public void setInput(InputStream input) {
        this.input = input;
    }

    public OutputStream getOutput() {
        return output;
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isL2encdecKey() {
        return l2encdecKey;
    }

    public void setL2encdecKey(boolean l2encdecKey) {
        this.l2encdecKey = l2encdecKey;
    }

    @Override
    public Void call() throws Exception {
        try (L2FileInputStream is = new L2FileInputStream(
                new BufferedInputStream(getInput()),
                getKey(), isL2encdecKey());
             OutputStream os = new BufferedOutputStream(getOutput())) {
            int read;
            while ((read = is.read()) >= 0) {
                os.write(read);
            }
        }
        return null;
    }
}
