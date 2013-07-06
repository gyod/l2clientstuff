@DatFile(value = "ItemName", localizable = true)
@SafePackage
class ItemNameDat{
    ItemName[] itemNames;
}

class ItemName{
    int id;
    @Unicode
    String name;
    @Unicode
    String nameAdd;
    String desc;
    int unk5;
    int unk6;
    int unk7;
}