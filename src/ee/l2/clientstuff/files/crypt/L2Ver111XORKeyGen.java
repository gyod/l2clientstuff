package ee.l2.clientstuff.files.crypt;

/**
 * @author acmi
 */
public class L2Ver111XORKeyGen implements XORKeyGen{
    @Override
    public int getKey(int offset) {
        return 0xAC;
    }
}
