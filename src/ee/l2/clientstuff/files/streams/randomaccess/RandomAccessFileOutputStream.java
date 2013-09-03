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
package ee.l2.clientstuff.files.streams.randomaccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

/**
 * @author acmi
 */
public class RandomAccessFileOutputStream extends RandomAccessOutputStream {
    private RandomAccessFile file;

    public RandomAccessFileOutputStream(File file) throws FileNotFoundException {
        super(null);

        this.file = new RandomAccessFile(Objects.requireNonNull(file), "rw");
    }

    public RandomAccessFileOutputStream(String name) throws FileNotFoundException {
        this(new File(Objects.requireNonNull(name)));
    }

    @Override
    public int position() throws IOException {
        return (int) file.getFilePointer();
    }

    @Override
    public void seek(int pos) throws IOException {
        file.seek(pos);
    }

    @Override
    public void write(int b) throws IOException {
        file.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        file.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
        super.close();

        file.close();
    }


}
