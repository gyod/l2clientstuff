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
package ee.l2.clientstuff.files.up;

import ee.l2.clientstuff.files.streams.l2file.crypt.xor.L2Ver1x1;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.*;

/**
 * L2UnrealPackage based on {@link RandomAccessFile}. Only utility information is stored in memory.
 *
 * @author acmi
 */
public class L2UnrealPackageRAF implements L2UnrealPackage {
    private final RandomAccessFile file;

    private final int startOffset;
    private final int xorKey;

    public L2UnrealPackageRAF(String name, String key) throws IOException {
        this(new File(name), key);
    }

    public L2UnrealPackageRAF(File file, String key) throws IOException {
        this.file = new RandomAccessFile(file, "rw");

        byte[] l2CryptHeaderBytes = new byte[28];
        this.file.readFully(l2CryptHeaderBytes);
        String l2CryptHeader = new String(l2CryptHeaderBytes, "UTF-16LE");
        if (l2CryptHeader.startsWith("Lineage2Ver")) {
            int xorByte;
            switch (l2CryptHeader) {
                case "Lineage2Ver111":
                    xorByte = L2Ver1x1.XOR_KEY_111;
                    break;
                case "Lineage2Ver121":
                    xorByte = L2Ver1x1.getXORKey121(key);
                    break;
                default:
                    throw new IOException(l2CryptHeader + " is not supported.");
            }
            startOffset = 28;
            xorKey = xorByte + (xorByte << 8) + (xorByte << 16) + (xorByte << 24);
        } else {
            startOffset = 0;
            xorKey = -1;
        }

        seek(0);
        if (readInt() != L2_UNREAL_PACKAGE_MAGIC)
            throw new IOException("Not a L2 package file.");
    }

    @Override
    public int getVersion() {
        try {
            seek(4);
            return readShort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setVersion(int version) {
        try {
            seek(4);
            writeShort(version);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getLicense() {
        try {
            seek(6);
            return readShort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLicense(int license) {
        try {
            seek(6);
            writeShort(license);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getFlags() {
        try {
            seek(8);
            return readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFlags(int flags) {
        try {
            seek(8);
            writeInt(flags);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<NameEntry> names;

    @Override
    public List<NameEntry> getNameTable() {
        if (names == null) {
            try {
                List<NameEntry> tmp = new ArrayList<>();

                seek(12);
                int count = readInt();
                int offset = readInt();
                seek(offset);
                for (int i = 0; i < count; i++) {
                    tmp.add(new NameEntry(readLine(), readInt()));
                }

                names = Collections.unmodifiableList(tmp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return names;
    }

    private List<ExportEntry> exports;

    @Override
    public List<ExportEntry> getExportTable() {
        if (exports == null) {
            try {
                List<ExportEntry> tmp = new ArrayList<>();

                seek(20);
                int count = readInt();
                int offset = readInt();
                seek(offset);
                for (int i = 0; i < count; i++) {
                    tmp.add(new ExportEntry(readCompactInt(), readCompactInt(), readInt(), readCompactInt(), readInt(), readCompactInt(), readCompactInt()));
                }

                exports = Collections.unmodifiableList(tmp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return exports;
    }

    private List<ImportEntry> imports;

    @Override
    public List<ImportEntry> getImportTable() {
        if (imports == null) {
            try {
                List<ImportEntry> tmp = new ArrayList<>();

                seek(28);
                int count = readInt();
                int offset = readInt();
                seek(offset);
                for (int i = 0; i < count; i++) {
                    tmp.add(new ImportEntry(readCompactInt(), readCompactInt(), readInt(), readCompactInt()));
                }

                imports = Collections.unmodifiableList(tmp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return imports;
    }

    private UUID uuid;

    @Override
    public UUID getUUID() {
        if (uuid == null) {
            try {
                seek(36);
                byte[] uuidBytes = new byte[16];
                readFully(uuidBytes);
                uuid = UUID.fromString(String.format(
                        "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
                        uuidBytes[3], uuidBytes[2], uuidBytes[1], uuidBytes[0],
                        uuidBytes[5], uuidBytes[4],
                        uuidBytes[7], uuidBytes[6],
                        uuidBytes[8], uuidBytes[9],
                        uuidBytes[10], uuidBytes[11], uuidBytes[12], uuidBytes[13], uuidBytes[14], uuidBytes[15]
                ));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return uuid;
    }

    private List<ee.l2.clientstuff.files.up.Generation> generations;

    @Override
    public List<? extends ee.l2.clientstuff.files.up.Generation> getGenerations() {
        if (generations == null) {
            try {
                List<ee.l2.clientstuff.files.up.Generation> tmp = new ArrayList<>();

                seek(52);
                int count = readInt();
                for (int i = 0; i < count; i++)
                    tmp.add(new Generation(readInt(), readInt()));

                generations = Collections.unmodifiableList(tmp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return generations;
    }

    protected IEEntry objectReference(int index) {
        if (index > 0)
            return getExportTable().get(index - 1);
        else if (index < 0)
            return getImportTable().get(-index - 1);

        return null;
    }

    protected void seek(long pos) throws IOException {
        file.seek(pos + startOffset);
    }

    protected int read() throws IOException {
        return xorKey != -1 ? (file.read() ^ xorKey) & 0xff : file.read();
    }

    protected short readShort() throws IOException {
        return Short.reverseBytes(xorKey != -1 ? (short) (file.readShort() ^ xorKey) : file.readShort());
    }

    protected int readInt() throws IOException {
        return Integer.reverseBytes(xorKey != -1 ? file.readInt() ^ xorKey : file.readInt());
    }

    protected int readCompactInt() throws IOException {
        int output = 0;
        boolean signed = false;
        for (int i = 0; i < 5; i++) {
            int x = read();
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

    protected String readLine() throws IOException {
        int len = readCompactInt();
        if (len == 0)
            return "";

        byte[] bytes = new byte[len > 0 ? len : -2 * len];
        readFully(bytes);
        return new String(bytes, 0, bytes.length - (len > 0 ? 1 : 2), Charset.forName(len > 0 ? "ascii" : "utf-16le"));
    }

    protected void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    protected void readFully(byte[] b, int off, int len) throws IOException {
        file.readFully(b, off, len);

        if (xorKey != -1)
            for (int i = off; i < off + len; i++)
                b[i] ^= xorKey;
    }

    protected void write(int b) throws IOException {
        file.write(xorKey != -1 ? b ^ xorKey : b);
    }

    protected void writeShort(int v) throws IOException {
        file.writeShort(Short.reverseBytes(xorKey != -1 ? (short) (v ^ xorKey) : (short) v));
    }

    protected void writeInt(int v) throws IOException {
        file.writeInt(Integer.reverseBytes(xorKey != -1 ? v ^ xorKey : v));
    }

    protected void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    protected void write(byte[] b, int off, int len) throws IOException {
        if (xorKey != -1) {
            byte[] copy = Arrays.copyOfRange(b, off, off + len);
            for (int i = 0; i < copy.length; i++)
                copy[i] ^= xorKey;
            file.write(copy);
        } else {
            file.write(b, off, len);
        }
    }

    private static class Generation implements ee.l2.clientstuff.files.up.Generation {
        private final int exportCount;
        private final int importCount;

        public Generation(int exportCount, int importCount) {
            this.exportCount = exportCount;
            this.importCount = importCount;
        }

        @Override
        public int getExportCount() {
            return exportCount;
        }

        @Override
        public int getImportCount() {
            return importCount;
        }

        @Override
        public String toString() {
            return "Generation[" +
                    "exportCount=" + exportCount +
                    ", importCount=" + importCount +
                    ']';
        }
    }

    private static class NameEntry implements ee.l2.clientstuff.files.up.NameEntry {
        private final String name;
        private int flags;

        public NameEntry(String name) {
            this.name = Objects.requireNonNull(name);
        }

        private NameEntry(String name, int flags) {
            this(name);
            this.flags = flags;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getFlags() {
            return flags;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NameEntry nameEntry = (NameEntry) o;

            return name.equals(nameEntry.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    private abstract class IEEntry implements Entry {
        protected int objectPackage;
        protected int objectName;

        protected IEEntry(int objectPackage, int objectName) {
            this.objectPackage = objectPackage;
            this.objectName = objectName;
        }

        @Override
        public IEEntry getObjectPackage() {
            return objectReference(objectPackage);
        }

        @Override
        public NameEntry getObjectName() {
            return getNameTable().get(objectName);
        }

        public String getObjectFullName() {
            if (getObjectPackage() != null)
                return getObjectPackage().getObjectName().getName() + '.' + getObjectName().getName();
            else
                return getObjectName().getName();
        }
    }

    private class ExportEntry extends IEEntry implements ee.l2.clientstuff.files.up.ExportEntry {
        private int objectClass;
        private int objectSuperClass;
        private int objectFlags;
        private int size;
        private int offset;

        private ExportEntry(int objectClass, int objectSuperClass, int objectPackage, int objectName, int objectFlags, int size, int offset) {
            super(objectPackage, objectName);
            this.objectClass = objectClass;
            this.objectSuperClass = objectSuperClass;
            this.objectFlags = objectFlags;
            this.size = size;
            this.offset = offset;
        }

        @Override
        public IEEntry getObjectClass() {
            return objectReference(objectClass);
        }

        @Override
        public IEEntry getObjectSuperClass() {
            return objectReference(objectSuperClass);
        }

        @Override
        public int getObjectFlags() {
            return objectFlags;
        }

        @Override
        public byte[] getObjectRawData() {
            byte[] raw = new byte[size];

            if (size != 0) {
                try {
                    seek(offset);
                    readFully(raw);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return raw;
        }

        public void setObjectRawData(byte[] objectRawData) {
            try {
                if (size != 0 && (objectRawData == null || objectRawData.length != size))
                    throw new IOException("length mismatch");

                if (size != 0) {
                    seek(offset);
                    write(objectRawData);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString() {
            return getObjectFullName() + '[' + getObjectClass().getObjectFullName() + ']';
        }
    }

    private class ImportEntry extends IEEntry implements ee.l2.clientstuff.files.up.ImportEntry {
        private int classPackage;
        private int className;

        private ImportEntry(int classPackage, int className, int objectPackage, int objectName) {
            super(objectPackage, objectName);
            this.classPackage = classPackage;
            this.className = className;
        }

        @Override
        public NameEntry getClassPackage() {
            return getNameTable().get(classPackage);
        }

        @Override
        public NameEntry getClassName() {
            return getNameTable().get(className);
        }

        public String getFullClassName() {
            return getClassPackage().getName() + '.' + getClassName().getName();
        }

        @Override
        public String toString() {
            return getObjectFullName() + '[' + getFullClassName() + ']';
        }
    }
}
