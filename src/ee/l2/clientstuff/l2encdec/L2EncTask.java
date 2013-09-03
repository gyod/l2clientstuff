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

import ee.l2.clientstuff.files.streams.l2file.L2FileOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

/**
 * @author acmi
 */
public class L2EncTask implements Callable<Void> {
    private InputStream input;
    private OutputStream output;
    private int version;
    private String key;
    private boolean writeTail;

    public L2EncTask() {
    }

    public L2EncTask(InputStream input, OutputStream output, int version) {
        this.input = input;
        this.output = output;
        this.version = version;
    }

    public L2EncTask(InputStream input, OutputStream output, int version, String key) {
        this.input = input;
        this.output = output;
        this.version = version;
        this.key = key;
    }

    public L2EncTask(InputStream input, OutputStream output, int version, boolean writeTail) {
        this.input = input;
        this.output = output;
        this.version = version;
        this.writeTail = writeTail;
    }

    public L2EncTask(InputStream input, OutputStream output, int version, String key, boolean writeTail) {
        this.input = input;
        this.output = output;
        this.version = version;
        this.key = key;
        this.writeTail = writeTail;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isWriteTail() {
        return writeTail;
    }

    public void setWriteTail(boolean writeTail) {
        this.writeTail = writeTail;
    }

    @Override
    public Void call() throws Exception {
        try (InputStream is = new BufferedInputStream(getInput());
             L2FileOutputStream os = new L2FileOutputStream(
                     new BufferedOutputStream(getOutput()),
                     getKey(), getVersion(), isWriteTail())) {
            int read;
            while ((read = is.read()) >= 0) {
                os.write(read);
            }
        }
        return null;
    }
}
