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

/**
 * @author acmi
 */
public class L2DataOutputStream extends FilterOutputStream implements DataOutput {

    public L2DataOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
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
        write(compactIntToByteArray(v));
    }

    public static byte[] compactIntToByteArray(int index) {
        boolean signed = false;
        if (index < 0) {
            signed = true;
            index = -index;
        }

        byte[] bytes = new byte[]{
                (byte) (index & 0x3F),
                (byte) ((index >> 6) & 0x7F),
                (byte) ((index >> 13) & 0x7F),
                (byte) ((index >> 20) & 0x7F),
                (byte) ((index >> 27) & 0x7F)
        };

        if (signed)
            bytes[0] |= 0x80;

        byte[] res = null;
        for (int i = 0; i < 4; i++) {
            boolean f = false;
            for (int j = i + 1; j < 4; j++)
                if (bytes[j] != 0)
                    f = true;
            if (f) {
                bytes[i] |= i == 0 ? 0x40 : 0x80;
            } else {
                res = new byte[i + 1];
                System.arraycopy(bytes, 0, res, 0, res.length);
                break;
            }
        }
        return res;
    }

    @Override
    public void writeLong(long v) throws IOException {
        write((int) (v >>> 0) & 0xFF);
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
        writeDouble(Double.doubleToLongBits(v));
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
                    Modifier.isStatic(mod) ||
                    Modifier.isFinal(mod))
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
