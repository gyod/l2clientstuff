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

import ee.l2.clientstuff.files.streams.l2data.IntConst;
import ee.l2.clientstuff.files.streams.l2data.L2DataInputStream;
import ee.l2.clientstuff.files.streams.l2data.L2DataOutputStream;
import ee.l2.clientstuff.files.streams.l2data.Length;
import ee.l2.clientstuff.files.streams.randomaccess.RandomAccessInputStream;
import ee.l2.clientstuff.files.streams.randomaccess.RandomAccessOutputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author acmi
 */
public class L2UnrealPackage {
    private int version;
    private int license;
    private int flags;
    private final UUID uuid;
    private final List<Generation> generations = new ArrayList<>();

    private List<NameEntry> names = new ArrayList<>();
    private List<ExportEntry> exports = new ArrayList<>();
    private List<ImportEntry> imports = new ArrayList<>();

    public L2UnrealPackage(UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getLicense() {
        return license;
    }

    public void setLicense(int license) {
        this.license = license;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public UUID getUUID() {
        return uuid;
    }

    public List<Generation> getGenerations() {
        return generations;
    }

    public int getNameTableSize() {
        return names.size();
    }

    public NameEntry getNameEntry(int index) {
        return names.get(index);
    }

    public int getExportTableSize(){
        return exports.size();
    }

    public ExportEntry getExportEntry(int index){
        return exports.get(index);
    }

    public int getImportTableSize(){
        return imports.size();
    }

    public ImportEntry getImportEntry(int index){
        return imports.get(index);
    }

    private int getNameEntryIndex(String name) {
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).getName().equals(name))
                return i;
        }
        return -1;
    }

    private IEEntry objectReference(int index) {
        if (index > 0)
            return exports.get(index - 1);
        else if (index < 0)
            return imports.get(-index - 1);

        return null;
    }

    private int objectReference(IEEntry entry) {
        if (entry instanceof ExportEntry)
            return exports.indexOf(entry) + 1;
        else if (entry instanceof ImportEntry)
            return -(imports.indexOf(entry) + 1);
        else
            return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        L2UnrealPackage that = (L2UnrealPackage) o;

        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static L2UnrealPackage read(RandomAccessInputStream input) throws IOException {
        L2DataInputStream in = new L2DataInputStream(Objects.requireNonNull(input));

        try {
            L2UnrealPackageHeader header = in.readObject(L2UnrealPackageHeader.class);
            L2UnrealPackage unrealPackage = new L2UnrealPackage(UUID.fromString(String.format(
                    "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
                    header.uuid[3], header.uuid[2], header.uuid[1], header.uuid[0],
                    header.uuid[5], header.uuid[4],
                    header.uuid[7], header.uuid[6],
                    header.uuid[8], header.uuid[9],
                    header.uuid[10], header.uuid[11], header.uuid[12], header.uuid[13], header.uuid[14], header.uuid[15]
            )));
            unrealPackage.version = header.version & 0xffff;
            unrealPackage.license = (header.version >> 16) & 0xffff;
            unrealPackage.flags = header.flags;

            for (int i = 0; i < header.generations; i++)
                unrealPackage.generations.add(new Generation(in.readInt(), in.readInt()));

            input.seek(header.nameOffset);
            for (int i = 0; i < header.nameCount; i++) {
                NameEntry entry = unrealPackage.new NameEntry(in.readLine());
                entry.setFlags(in.readInt());
                unrealPackage.names.add(entry);
            }

            for (int i = 0; i < header.importCount; i++)
                unrealPackage.imports.add(unrealPackage.new ImportEntry());
            input.seek(header.importOffset);
            for (int i = 0; i < header.importCount; i++) {
                ImportEntry entry = unrealPackage.imports.get(i);
                entry.classPackage = unrealPackage.getNameEntry(in.readCompactInt());
                entry.className = unrealPackage.getNameEntry(in.readCompactInt());
                entry.objectPackage = unrealPackage.objectReference(in.readInt());
                entry.objectName = unrealPackage.getNameEntry(in.readCompactInt());
            }

            for (int i = 0; i < header.exportCount; i++)
                unrealPackage.exports.add(unrealPackage.new ExportEntry());
            int[] dataOffsets = new int[header.exportCount];
            input.seek(header.exportOffset);
            for (int i = 0; i < header.exportCount; i++) {
                ExportEntry entry = unrealPackage.exports.get(i);
                entry.objectClass = unrealPackage.objectReference(in.readCompactInt());
                entry.objectSuperClass = unrealPackage.objectReference(in.readCompactInt());
                entry.objectPackage = unrealPackage.objectReference(in.readInt());
                entry.objectName = unrealPackage.getNameEntry(in.readCompactInt());
                entry.objectFlags = in.readInt();
                entry.objectRawData = new byte[in.readCompactInt()];
                dataOffsets[i] = in.readCompactInt();
            }

            for (int i = 0; i < header.exportCount; i++) {
                ExportEntry entry = unrealPackage.exports.get(i);
                if (entry.objectRawData.length > 0) {
                    input.seek(dataOffsets[i]);
                    in.readFully(entry.objectRawData);
                }
            }

            return unrealPackage;
        } catch (ReflectiveOperationException e) {
            throw new IOException(e);
        }
    }

    public void write(RandomAccessOutputStream output) throws IOException {
        L2DataOutputStream out = new L2DataOutputStream(Objects.requireNonNull(output));

        try {
            L2UnrealPackageHeader header = new L2UnrealPackageHeader();
            header.version = version | (license << 16);
            header.flags = flags;
            header.nameCount = names.size();
            header.exportCount = exports.size();
            header.importCount = imports.size();
            ByteBuffer bb = ByteBuffer.allocate(16);
            bb.putInt(Integer.reverseBytes((int) (uuid.getMostSignificantBits() >> 32)));
            bb.putShort(Short.reverseBytes((short) (uuid.getMostSignificantBits() >> 16)));
            bb.putShort(Short.reverseBytes((short) uuid.getMostSignificantBits()));
            bb.putLong(uuid.getLeastSignificantBits());
            bb.flip();
            bb.get(header.uuid);
            header.generations = generations.size();

            output.seek(0x38);
            for (Generation generation : generations)
                out.writeObject(generation);
            header.nameOffset = output.position();
            for (NameEntry entry : names) {
                out.writeObject(entry);
            }

            int[] dataOffsets = new int[exports.size()];
            for (int i = 0; i < exports.size(); i++) {
                ExportEntry entry = exports.get(i);
                if (entry.objectRawData != null && entry.objectRawData.length > 0) {
                    dataOffsets[i] = output.position();
                    out.write(entry.objectRawData);
                }
            }
            header.importOffset = output.position();
            for (ImportEntry entry : imports) {
                out.writeCompactInt(getNameEntryIndex(entry.classPackage.getName()));
                out.writeCompactInt(getNameEntryIndex(entry.className.getName()));
                out.writeInt(objectReference(entry.objectPackage));
                out.writeCompactInt(getNameEntryIndex(entry.objectName.getName()));
            }

            header.exportOffset = output.position();
            for (int i = 0; i < exports.size(); i++) {
                ExportEntry entry = exports.get(i);
                out.writeCompactInt(objectReference(entry.objectClass));
                out.writeCompactInt(objectReference(entry.objectSuperClass));
                out.writeInt(objectReference(entry.objectPackage));
                out.writeCompactInt(getNameEntryIndex(entry.objectName.getName()));
                out.writeInt(entry.objectFlags);
                out.writeCompactInt(entry.objectRawData != null ? entry.objectRawData.length : 0);
                out.writeCompactInt(dataOffsets[i]);
            }

            output.seek(0);
            out.writeObject(header);
        } catch (ReflectiveOperationException e) {
            throw new IOException(e);
        }
    }

    /**
     * IO support structure
     */
    public static class L2UnrealPackageHeader {
        @IntConst(0x9e2a83c1)
        int signature = 0x9e2a83c1;
        int version;
        int flags;
        int nameCount;
        int nameOffset;
        int exportCount;
        int exportOffset;
        int importCount;
        int importOffset;
        @Length(16)
        byte[] uuid = new byte[16];
        int generations;
    }

    public static class Generation {
        private final int exportCount;
        private final int importCount;

        public Generation(int exportCount, int importCount) {
            this.exportCount = exportCount;
            this.importCount = importCount;
        }

        public int getExportCount() {
            return exportCount;
        }

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

    public final class NameEntry {
        private final String name;
        private int flags;

        public NameEntry(String name) {
            this.name = Objects.requireNonNull(name);
        }

        public String getName() {
            return name;
        }

        public int getFlags() {
            return flags;
        }

        public void setFlags(int flags) {
            this.flags = flags;
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

    private abstract class IEEntry {
        protected IEEntry objectPackage;
        protected NameEntry objectName;

        public IEEntry getObjectPackage() {
            return objectPackage;
        }

        public NameEntry getObjectName() {
            return objectName;
        }

        public String getObjectFullName() {
            if (objectPackage != null)
                return objectPackage.objectName.getName() + '.' + objectName.getName();
            else
                return objectName.getName();
        }
    }

    public final class ExportEntry extends IEEntry {
        private IEEntry objectClass;
        private IEEntry objectSuperClass;
        private int objectFlags;
        private byte[] objectRawData;

        public IEEntry getObjectClass() {
            return objectClass;
        }

        public IEEntry getObjectSuperClass() {
            return objectSuperClass;
        }

        public int getObjectFlags() {
            return objectFlags;
        }

        public void setObjectFlags(int objectFlags) {
            this.objectFlags = objectFlags;
        }

        public byte[] getObjectRawData() {
            return objectRawData;
        }

        public void setObjectRawData(byte[] objectRawData) {
            this.objectRawData = objectRawData;
        }

        @Override
        public String toString() {
            return getObjectFullName() + '[' + objectClass.getObjectFullName() + ']';
        }
    }

    public final class ImportEntry extends IEEntry {
        private NameEntry classPackage;
        private NameEntry className;

        public NameEntry getClassPackage() {
            return classPackage;
        }

        public NameEntry getClassName() {
            return className;
        }

        public String getFullClassName() {
            return classPackage.getName() + '.' + className.getName();
        }

        @Override
        public String toString() {
            return getObjectFullName() + '[' + getFullClassName() + ']';
        }
    }
}
