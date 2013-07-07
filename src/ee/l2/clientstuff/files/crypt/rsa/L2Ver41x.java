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
package ee.l2.clientstuff.files.crypt.rsa;

import java.math.BigInteger;

/**
 * @author acmi
 */
public interface L2Ver41x {
    public final static BigInteger MODULUS_ORIGINAL = new BigInteger(
            "97df398472ddf737ef0a0cd17e8d172f" +
                    "0fef1661a38a8ae1d6e829bc1c6e4c3c" +
                    "fc19292dda9ef90175e46e7394a18850" +
                    "b6417d03be6eea274d3ed1dde5b5d7bd" +
                    "e72cc0a0b71d03608655633881793a02" +
                    "c9a67d9ef2b45eb7c08d4be329083ce4" +
                    "50e68f7867b6749314d40511d09bc574" +
                    "4551baa86a89dc38123dc1668fd72d83", 16);
    public final static BigInteger PRIVATE_EXPONENT_ORIGINAL = new BigInteger("35", 16);

    public final static BigInteger MODULUS_L2ENCDEC = new BigInteger(
            "75b4d6de5c016544068a1acf125869f4" +
                    "3d2e09fc55b8b1e289556daf9b875763" +
                    "5593446288b3653da1ce91c87bb1a5c1" +
                    "8f16323495c55d7d72c0890a83f69bfd" +
                    "1fd9434eb1c02f3e4679edfa43309319" +
                    "070129c267c85604d87bb65bae205de3" +
                    "707af1d2108881abb567c3b3d069ae67" +
                    "c3a4c6a3aa93d26413d4c66094ae2039", 16);
    public final static BigInteger PUBLIC_EXPONENT_L2ENCDEC = new BigInteger(
            "30b4c2d798d47086145c75063c8e841e"+
                    "719776e400291d7838d3e6c4405b504c"+
                    "6a07f8fca27f32b86643d2649d1d5f12"+
                    "4cdd0bf272f0909dd7352fe10a77b34d"+
                    "831043d9ae541f8263c6fe3d1c14c2f0"+
                    "4e43a7253a6dda9a8c1562cbd493c1b6"+
                    "31a1957618ad5dfe5ca28553f746e2fc"+
                    "6f2db816c7db223ec91e955081c1de65", 16);
    public final static BigInteger PRIVATE_EXPONENT_L2ENCDEC = new BigInteger("1d", 16);
}
