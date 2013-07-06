@DatFile(value = "EventLookChange")
@SafePackage
class EventLookChangeDat{
    EventLookChange[] eventLookChanges;
}

class EventLookChange{
    int unk1;
    String unk2;
    @Length(10)
    int[] unk3;
    @Length(45)
    int[] unk4;
}