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

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.Charset;

/**
 * @author acmi
 */
public class L2DataInputStream extends FilterInputStream implements DataInput {

    public L2DataInputStream(InputStream input) {
        super(input);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int total = 0;
        int cur;

        while ((total < n) && ((cur = (int) in.skip(n - total)) > 0)) {
            total += cur;
        }

        return total;
    }

    @Override
    public boolean readBoolean() throws IOException {
        int val = readInt();

        if (val != 0 && val != 1)
            throw new IOException("Not boolean: " + val);

        return val == 1;
    }

    @Override
    public byte readByte() throws IOException {
        int ch = in.read();
        if (ch < 0)
            throw new EOFException();
        return (byte) (ch);
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return readByte() & 0xff;
    }

    @Override
    public short readShort() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short) (ch1 + (ch2 << 8));
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return readShort() & 0xffff;
    }

    @Override
    public char readChar() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char) (ch1 + (ch2 << 8));
    }

    @Override
    public int readInt() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return (ch1 + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
    }

    public int readCompactInt() throws IOException {
        int output = 0;
        boolean signed = false;
        for (int i = 0; i < 5; i++) {
            int x = in.read();
            if (x < 0)
                throw new EOFException();
            if (i == 0) {
                if ((x & 0x80) > 0)
                    signed = true;
                output |= (x & 0x3F);
                if ((x & 0x40) == 0)
                    break;
            } else if (i == 4) {
                output |= (x & 0x1F) << (6 + (3 * 7));
            } else {
                output |= (x & 0x7F) << (6 + ((i - 1) * 7));
                if ((x & 0x80) == 0)
                    break;
            }
        }
        if (signed)
            output *= -1;
        return output;
    }

    @Override
    public long readLong() throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();
        int ch5 = in.read();
        int ch6 = in.read();
        int ch7 = in.read();
        int ch8 = in.read();
        if ((ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7 | ch8) < 0)
            throw new EOFException();

        return (((long) ch1)) |
                (((long) ch2) << 8) |
                (((long) ch3) << 16) |
                (((long) ch4) << 24) |
                (((long) ch5) << 32) |
                (((long) ch6) << 40) |
                (((long) ch7) << 48) |
                (((long) ch8) << 56);
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public String readLine() throws IOException {
        int len = readCompactInt();
        if (len == 0)
            return "";

        byte[] bytes = new byte[len > 0 ? len : -2 * len];
        if (in.read(bytes) == -1)
            throw new EOFException();
        return new String(bytes, 0, bytes.length - (len > 0 ? 1 : 2), Charset.forName(len > 0 ? "ascii" : "utf-16le"));
    }

    @Override
    public String readUTF() throws IOException {
        int len = readInt();
        if (len == 0)
            return "";

        byte[] bytes = new byte[len];
        if (in.read(bytes) == -1)
            throw new EOFException();
        return new String(bytes, Charset.forName("utf-16le"));
    }

    @SuppressWarnings("unchecked")
    public <T> T readObject(Class<T> clazz) throws IOException, ReflectiveOperationException {
        if (clazz == Boolean.TYPE || clazz == Boolean.class) {
            return (T) Boolean.valueOf(readBoolean());
        } else if (clazz == Byte.TYPE || clazz == Byte.class) {
            return (T) Byte.valueOf(readByte());
        } else if (clazz == Short.TYPE || clazz == Short.class) {
            return (T) Short.valueOf(readShort());
        } else if (clazz == Character.TYPE || clazz == Character.class) {
            return (T) Character.valueOf(readChar());
        } else if (clazz == Integer.TYPE || clazz == Integer.class) {
            return (T) Integer.valueOf(readInt());
        } else if (clazz == Long.TYPE || clazz == Long.class) {
            return (T) Long.valueOf(readLong());
        } else if (clazz == Float.TYPE || clazz == Float.class) {
            return (T) Float.valueOf(readFloat());
        } else if (clazz == Double.TYPE || clazz == Double.class) {
            return (T) Double.valueOf(readDouble());
        } else {
            T obj = clazz.newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isSynthetic())
                    continue;

                int mod = field.getModifiers();
                if (Modifier.isTransient(mod) ||
                        Modifier.isStatic(mod) ||
                        Modifier.isFinal(mod))
                    continue;

                field.setAccessible(true);

                if (field.getType().isArray()) {
                    int len;
                    Length lenAnn = field.getAnnotation(Length.class);
                    if (lenAnn != null) {
                        if (lenAnn.value() != 0)
                            len = lenAnn.value();
                        else if (!lenAnn.field().equals("")) {
                            Field fieldLen = clazz.getDeclaredField(lenAnn.field());
                            fieldLen.setAccessible(true);
                            if (fieldLen.getType().isArray()) {
                                len = Array.getLength(fieldLen.get(obj)) + lenAnn.add();
                            } else if (fieldLen.getType() == Integer.class) {
                                len = ((Number) fieldLen.get(obj)).intValue();
                            } else if (fieldLen.getType() == Integer.TYPE) {
                                len = fieldLen.getInt(obj);
                            } else
                                throw new IOException(clazz.getSimpleName() + "." + field.getName() +
                                        ": couldn't extract length from field " + fieldLen.getName());
                        } else {
                            switch (lenAnn.lengthType()) {
                                case COMPACT:
                                    len = readCompactInt();
                                    break;
                                default:
                                    len = readInt();
                            }
                            len += lenAnn.add();
                        }
                    } else
                        len = readInt();

                    Object arr = Array.newInstance(field.getType().getComponentType(), len);
                    for (int i = 0; i < len; i++) {
                        Object el;

                        if (field.getType().getComponentType() == String.class) {
                            if (field.isAnnotationPresent(Unicode.class))
                                el = readUTF();
                            else
                                el = readLine();
                        } else if (field.isAnnotationPresent(Compact.class) &&
                                (field.getType().getComponentType() == Integer.class ||
                                        field.getType().getComponentType() == Integer.TYPE))
                            el = readCompactInt();
                        else
                            el = readObject(field.getType().getComponentType());

                        Array.set(arr, i, el);
                    }
                    field.set(obj, arr);
                } else {
                    if (field.getType() == Byte.class ||
                            field.getType() == Byte.TYPE) {
                        byte val = readByte();

                        IntConst intConst = field.getAnnotation(IntConst.class);
                        if (intConst != null && (val & 0xff) != intConst.value())
                            throw new IOException(clazz.getSimpleName() + "." + field.getName() +
                                    ": IntConst expected " + Integer.toHexString(intConst.value()) +
                                    ", got " + Integer.toHexString(val));

                        if (field.getType() == Byte.TYPE)
                            field.setByte(obj, val);
                        else
                            field.set(obj, val);
                    } else if (field.getType() == Short.class ||
                            field.getType() == Short.TYPE) {
                        short val = readByte();

                        IntConst intConst = field.getAnnotation(IntConst.class);
                        if (intConst != null && (val & 0xffff) != intConst.value())
                            throw new IOException(clazz.getSimpleName() + "." + field.getName() +
                                    ": IntConst expected " + Integer.toHexString(intConst.value()) +
                                    ", got " + Integer.toHexString(val));

                        if (field.getType() == Short.TYPE)
                            field.setShort(obj, val);
                        else
                            field.set(obj, val);
                    } else if (field.getType() == Integer.class ||
                            field.getType() == Integer.TYPE) {
                        int val = field.isAnnotationPresent(Compact.class) ?
                                readCompactInt() :
                                readInt();

                        IntConst intConst = field.getAnnotation(IntConst.class);
                        if (intConst != null && val != intConst.value())
                            throw new IOException(clazz.getSimpleName() + "." + field.getName() +
                                    ": IntConst expected " + Integer.toHexString(intConst.value()) +
                                    ", got " + Integer.toHexString(val));

                        if (field.getType() == Integer.TYPE)
                            field.setInt(obj, val);
                        else
                            field.set(obj, val);
                    } else if (field.getType() == String.class) {
                        String val = field.isAnnotationPresent(Unicode.class) ?
                                readUTF() :
                                readLine();

                        StringConst stringConst = field.getAnnotation(StringConst.class);
                        if (stringConst != null && !val.equals(stringConst.value()))
                            throw new IOException(clazz.getSimpleName() + "." + field.getName() +
                                    ": StringConst expected " + stringConst.value() +
                                    ", got " + val);

                        field.set(obj, val);
                    } else {
                        field.set(obj, readObject(field.getType()));
                    }
                }
            }

            return obj;
        }
    }
}
