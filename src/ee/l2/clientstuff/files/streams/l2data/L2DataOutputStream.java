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
package ee.l2.clientstuff.files.streams.l2data;

import java.io.DataOutput;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * @author acmi
 */
public class L2DataOutputStream extends FilterOutputStream implements DataOutput {

    public L2DataOutputStream(OutputStream out) {
        super(Objects.requireNonNull(out));
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        writeInt(v ? 1 : 0);
    }

    @Override
    public void writeByte(int v) throws IOException {
        write(v & 0xFF);
    }

    @Override
    public void writeShort(int v) throws IOException {
        write(v & 0xFF);
        write((v >>> 8) & 0xFF);
    }

    @Override
    public void writeChar(int v) throws IOException {
        write(v & 0xFF);
        write((v >>> 8) & 0xFF);
    }

    @Override
    public void writeInt(int v) throws IOException {
        write(v & 0xFF);
        write((v >>> 8) & 0xFF);
        write((v >>> 16) & 0xFF);
        write((v >>> 24) & 0xFF);
    }

    public void writeCompactInt(int v) throws IOException {
        boolean negative = v < 0;
        v = Math.abs(v);
        int[] bytes = new int[]{
                (v) & 0b00111111,
                (v >> 6) & 0b01111111,
                (v >> 6 + 7) & 0b01111111,
                (v >> 6 + 7 + 7) & 0b01111111,
                (v >> 6 + 7 + 7 + 7) & 0b01111111
        };

        if (negative) bytes[0] |= 0b10000000;

        for (int i = 0; i < 5; i++) {
            boolean hasMore = false;
            for (int j = i + 1; j < 5; j++)
                if (bytes[j] != 0)
                    hasMore = true;
            if (hasMore) {
                bytes[i] |= i == 0 ? 0b01000000 : 0b10000000;
                write(bytes[i]);
            } else {
                write(bytes[i]);
                return;
            }
        }
    }

    @Override
    public void writeLong(long v) throws IOException {
        write((int) (v) & 0xFF);
        write((int) (v >>> 8) & 0xFF);
        write((int) (v >>> 16) & 0xFF);
        write((int) (v >>> 24) & 0xFF);
        write((int) (v >>> 32) & 0xFF);
        write((int) (v >>> 40) & 0xFF);
        write((int) (v >>> 48) & 0xFF);
        write((int) (v >>> 56) & 0xFF);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    @Override
    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    /**
     * Writes a ASCII string to the output stream.
     * First <code>s.lenght</code> is written as compact int.
     * Use <code>writeLine</code> instead.
     *
     * @param s the string of bytes to be written.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void writeBytes(String s) throws IOException {
        byte[] strBytes = (s + '\0').getBytes("ascii");
        writeCompactInt(strBytes.length);
        write(strBytes);
    }

    /**
     * Writes a Unicode string to the output stream.
     * First <code>-s.lenght</code> is written as compact int.<br>
     * Use <code>writeLine</code> instead.
     *
     * @param s the string of bytes to be written.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void writeChars(String s) throws IOException {
        byte[] strBytes = (s + '\0').getBytes("utf-16le");
        writeCompactInt(-strBytes.length);
        write(strBytes);
    }

    /**
     * Writes a string to the output stream.
     * First <code>s.lenght</code> is written as compact int(positive or negative depending on charset).
     *
     * @param s the string of bytes to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void writeLine(String s) throws IOException {
        if (isASCII(s))
            writeBytes(s);
        else
            writeChars(s);
    }

    public static boolean isASCII(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 0x7F)
                return false;
        }
        return true;
    }

    /**
     * Writes a Unicode string to the output stream. First <code>s.lenght</code> is written as int.
     *
     * @param s the string of bytes to be written.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void writeUTF(String s) throws IOException {
        byte[] strBytes = s.getBytes("utf-16le");
        writeInt(strBytes.length);
        write(strBytes);
    }

    public void writeObject(Object obj) throws IOException, ReflectiveOperationException {
        Class<?> clazz = obj.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isSynthetic())
                continue;

            int mod = field.getModifiers();
            if (Modifier.isTransient(mod) ||
                    Modifier.isStatic(mod))
                continue;

            field.setAccessible(true);

            if (field.getType().isArray()) {
                Object array = field.get(obj);
                if (array == null)
                    array = Array.newInstance(field.getType().getComponentType(), 0);

                int len = Array.getLength(array);

                Length lenAnn = field.getAnnotation(Length.class);
                if (lenAnn != null) {
                    if (lenAnn.value() == 0) {
                        if (!lenAnn.field().equals("")) {
                            Field fieldLen = clazz.getDeclaredField(lenAnn.field());
                            fieldLen.setAccessible(true);
                            int lenExpected;
                            if (fieldLen.getType().isArray()) {
                                lenExpected = Array.getLength(fieldLen.get(obj)) + lenAnn.add();
                            } else if (fieldLen.getType() == Integer.class) {
                                lenExpected = ((Number) fieldLen.get(obj)).intValue();
                            } else if (fieldLen.getType() == Integer.TYPE) {
                                lenExpected = fieldLen.getInt(obj);
                            } else
                                throw new IOException(clazz.getSimpleName() + "." + field.getName() +
                                        ": couldn't extract length from field " + fieldLen.getName());

                            if (lenExpected != len)
                                throw new IOException(clazz.getSimpleName() + "." + field.getName() +
                                        ": length expected " + lenExpected +
                                        ", got" + len);
                        } else
                            len += lenAnn.add();

                        LengthType lenType = lenAnn.lengthType();
                        switch (lenType) {
                            case COMPACT:
                                writeCompactInt(len);
                                break;
                            default:
                                writeInt(len);
                        }
                    }
                } else
                    writeInt(len);

                for (int i = 0; i < len; i++) {
                    if (field.getType().getComponentType() == String.class) {
                        if (field.isAnnotationPresent(Unicode.class))
                            writeUTF(Array.get(array, i).toString());
                        else
                            writeLine(Array.get(array, i).toString());
                    } else if (field.isAnnotationPresent(Compact.class) &&
                            (field.getType().getComponentType() == Integer.class ||
                                    field.getType().getComponentType() == Integer.TYPE)) {
                        writeCompactInt((Integer) Array.get(array, i));
                    } else
                        writeObject(Array.get(array, i));
                }
            } else {
                if (field.getType() == Boolean.TYPE) {
                    writeBoolean(field.getBoolean(obj));
                } else if (field.getType() == Byte.class || field.getType() == Byte.TYPE) {
                    int val = 0;
                    if (field.getType() == Byte.class) {
                        Object valObj = field.get(obj);
                        if (valObj != null)
                            val = (Byte) valObj;
                    } else
                        val = field.getByte(obj);
                    val &= 0xff;

                    IntConst intConst = field.getAnnotation(IntConst.class);
                    if (intConst != null && val != intConst.value())
                        throw new IOException(clazz.getSimpleName() + "." + field.getName() +
                                ": IntConst expected " + intConst.value() +
                                ", got" + val);

                    writeByte(val);
                } else if (field.getType() == Short.class || field.getType() == Short.TYPE) {
                    int val = 0;
                    if (field.getType() == Short.class) {
                        Object valObj = field.get(obj);
                        if (valObj != null)
                            val = (Short) valObj;
                    } else
                        val = field.getShort(obj);
                    val &= 0xffff;

                    IntConst intConst = field.getAnnotation(IntConst.class);
                    if (intConst != null && val != intConst.value())
                        throw new IOException(clazz.getSimpleName() + "." + field.getName() +
                                ": IntConst expected " + intConst.value() +
                                ", got" + val);

                    writeShort(val);
                } else if (field.getType() == Character.TYPE) {
                    writeChar(field.getChar(obj));
                } else if (field.getType() == Integer.class || field.getType() == Integer.TYPE) {
                    int val = 0;
                    if (field.getType() == Integer.class) {
                        Object valObj = field.get(obj);
                        if (valObj != null)
                            val = (Integer) valObj;
                    } else
                        val = field.getInt(obj);

                    IntConst intConst = field.getAnnotation(IntConst.class);
                    if (intConst != null && val != intConst.value())
                        throw new IOException(clazz.getSimpleName() + "." + field.getName() +
                                ": IntConst expected " + intConst.value() +
                                ", got" + val);

                    if (field.isAnnotationPresent(Compact.class))
                        writeCompactInt(val);
                    else
                        writeInt(val);
                } else if (field.getType() == Long.TYPE) {
                    writeLong(field.getLong(obj));
                } else if (field.getType() == Float.TYPE) {
                    writeFloat(field.getFloat(obj));
                } else if (field.getType() == Double.TYPE) {
                    writeDouble(field.getDouble(obj));
                } else if (field.getType() == String.class) {
                    Object valObj = field.get(obj);
                    String val = valObj != null ? valObj.toString() : "";

                    StringConst stringConst = field.getAnnotation(StringConst.class);
                    if (stringConst != null && !val.equals(stringConst.value()))
                        throw new IOException(clazz.getSimpleName() + "." + field.getName() +
                                ": StringConst expected " + stringConst.value() +
                                ", got" + val);

                    if (field.isAnnotationPresent(Unicode.class))
                        writeUTF(field.get(obj).toString());
                    else
                        writeLine(field.get(obj).toString());
                } else {
                    writeObject(field.get(obj));
                }
            }
        }
    }
}
