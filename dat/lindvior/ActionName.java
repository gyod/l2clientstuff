@DatFile(value = "ActionName", localizable = true)
@SafePackage
class ActionNameDat {
    ActionName[] actions;
}

class ActionName{
    @IntConst(1)
    int tag;
    int id;
    int type;
    int category;
    @Length(lengthType = LengthType.COMPACT)
    int[] unkIds;
    String name;
    String icon;
    @StringConst("none")
    String unkStr;
    String desc;
    boolean unkBool;
    @Unicode
    String cmd;
}
