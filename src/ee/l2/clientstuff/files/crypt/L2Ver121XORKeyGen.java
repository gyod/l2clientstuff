package ee.l2.clientstuff.files.crypt;

/**
 * @author acmi
 */
public class L2Ver121XORKeyGen implements XORKeyGen{
    @Override
    public int getKey(int offset) {
        return 0x22;
    }
}
