package com.netease.engine.util;


import java.util.Hashtable;


/**
 * 
 * @author handongming
 *
 */
public final class FPGenerator {


	@SuppressWarnings("unchecked")
	public static FPGenerator make(long polynomial, int degree) {
        Long l = new Long(polynomial);
        FPGenerator fpgen = (FPGenerator) generators.get(l);
        if (fpgen == null) {
            fpgen = new FPGenerator(polynomial, degree);
            generators.put(l, fpgen);
        }
        return fpgen;
    }
	
    @SuppressWarnings("rawtypes")
	private static final Hashtable generators = new Hashtable(10);

    private static final long zero = 0;
    private static final long one = 0x8000000000000000L;


    public long reduce(long fp) {
        int N = (8 - degree/8);
        long local = (N == 8 ? 0 : fp & (-1L << 8*N));
        long temp = zero;
        for (int i = 0; i < N; i++) {
            temp ^= ByteModTable[8+i][((int)fp) & 0xff];
            fp >>>= 8;
        };
        return local ^ temp;
    }

    public long extend_byte(long f, int v) {
        f ^= (0xff & v);
        int i = (int)f;
        long result = (f>>>8);
        result ^= ByteModTable[7][i & 0xff];
        return result;
    }

    public long extend_char(long f, int v) {
        f ^= (0xffff & v);
        int i = (int)f;
        long result = (f>>>16);
        result ^= ByteModTable[6][i & 0xff]; i >>>= 8;
        result ^= ByteModTable[7][i & 0xff];
        return result;
    }

    public long extend_int(long f, int v) {
        f ^= (0xffffffffL & v);
        int i = (int)f;
        long result = (f>>>32);
        result ^= ByteModTable[4][i & 0xff]; i >>>= 8;
        result ^= ByteModTable[5][i & 0xff]; i >>>= 8;
        result ^= ByteModTable[6][i & 0xff]; i >>>= 8;
        result ^= ByteModTable[7][i & 0xff];
        return result;
    }

    public long extend_long(long f, long v) {
        f ^= v;
        long result = ByteModTable[0][(int)(f & 0xff)]; f >>>= 8;
        result ^= ByteModTable[1][(int)(f & 0xff)]; f >>>= 8;
        result ^= ByteModTable[2][(int)(f & 0xff)]; f >>>= 8;
        result ^= ByteModTable[3][(int)(f & 0xff)]; f >>>= 8;
        result ^= ByteModTable[4][(int)(f & 0xff)]; f >>>= 8;
        result ^= ByteModTable[5][(int)(f & 0xff)]; f >>>= 8;
        result ^= ByteModTable[6][(int)(f & 0xff)]; f >>>= 8;
        result ^= ByteModTable[7][(int)(f & 0xff)];
        return result;
    }


    public long fp(byte[] buf, int start, int n) {
        return extend(empty, buf, start, n);
    }

    public long fp(char[] buf, int start, int n) {
        return extend(empty, buf, start, n);
    }

    public long fp(CharSequence s) {
        return extend(empty, s);
    }

    public long fp(int[] buf, int start, int n) {
        return extend(empty, buf, start, n);
    }

    public long fp(long[] buf, int start, int n) {
        return extend(empty, buf, start, n);
    }

    public long fp8(String s) {
        return extend8(empty, s);
    }

    public long fp8(char[] buf, int start, int n) {
        return extend8(empty, buf, start, n);
    }

    public long extend(long f, byte v) {
        return reduce(extend_byte(f, v));
    }

    public long extend(long f, char v) {
        return reduce(extend_char(f, v));
    }

    public long extend(long f, int v) {
        return reduce(extend_int(f, v));
    }

    public long extend(long f, long v) {
        return reduce(extend_long(f, v));
    }

    public long extend(long f, byte[] buf, int start, int n) {
        for (int i = 0; i < n; i++) {
            f = extend_byte(f, buf[start+i]);
        }
        return reduce(f);
    }

    public long extend(long f, char[] buf, int start, int n) {
        for (int i = 0; i < n; i++) {
            f = extend_char(f, buf[start+i]);
        }
        return reduce(f);
    }

    public long extend(long f, CharSequence s) {
        int n = s.length();
        for (int i = 0; i < n; i++) {
            int v = (int) s.charAt(i);
            f = extend_char(f, v);
        }
        return reduce(f);
    }

    public long extend(long f, int[] buf, int start, int n) {
        for (int i = 0; i < n; i++) {
            f = extend_int(f, buf[start+i]);
        }
        return reduce(f);
    }

    public long extend(long f, long[] buf, int start, int n) {
        for (int i = 0; i < n; i++) {
            f = extend_long(f, buf[start+i]);
        }
        return reduce(f);
    }

    public long extend8(long f, String s) {
        int n = s.length();
        for (int i = 0; i < n; i++) {
            int x = (int) s.charAt(i);
            f = extend_byte(f, x);
        }
        return reduce(f);
    }

    public long extend8(long f, char[] buf, int start, int n) {
        for (int i = 0; i < n; i++) {
            f = extend_byte(f, buf[start+i]);
        }
        return reduce(f);
    }

    public final long empty;

    public final int degree;

    public long polynomial;

    private long[][] ByteModTable;

    private FPGenerator(long polynomial, int degree) {
        this.degree = degree;
        this.polynomial = polynomial;
        ByteModTable = new long[16][256];

        long[] PowerTable = new long[128];

        long x_to_the_i = one;
        long x_to_the_degree_minus_one = (one >>> (degree-1));
        for (int i = 0; i < 128; i++) {
            PowerTable[i] = x_to_the_i;
            boolean overflow = ((x_to_the_i & x_to_the_degree_minus_one) != 0);
            x_to_the_i >>>= 1;
            if (overflow) {
                x_to_the_i ^= polynomial;
            }
        }
        this.empty = PowerTable[64];

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 256; j++) {
                long v = zero;
                for (int k = 0; k < 8; k++) {
                    if ((j & (1 << k)) != 0) {
                        v ^= PowerTable[127 - i*8 - k];
                    }
                }
                ByteModTable[i][j] = v;
            }
        }
    }

    public static final long polynomials[][] = {
        null,
        {0xC000000000000000L, 0xC000000000000000L},
        {0xE000000000000000L, 0xE000000000000000L},
        {0xD000000000000000L, 0xB000000000000000L},
        {0xF800000000000000L, 0xF800000000000000L},
        {0xEC00000000000000L, 0xBC00000000000000L},
        {0xDA00000000000000L, 0xB600000000000000L},
        {0xE500000000000000L, 0xE500000000000000L},
        {0x9680000000000000L, 0xD480000000000000L},
        {0x80C0000000000000L, 0x8840000000000000L},
        {0xB0A0000000000000L, 0xE9A0000000000000L},
        {0xD9F0000000000000L, 0xC9B0000000000000L},
        {0xE758000000000000L, 0xDE98000000000000L},
        {0xE42C000000000000L, 0x94E4000000000000L},
        {0xD4CE000000000000L, 0xB892000000000000L},
        {0xE2AB000000000000L, 0x9E39000000000000L},
        {0xCCE4800000000000L, 0x9783800000000000L},
        {0xD8F8C00000000000L, 0xA9CDC00000000000L},
        {0x9A28200000000000L, 0xFD79E00000000000L},
        {0xC782500000000000L, 0x96CD300000000000L},
        {0xBEE6880000000000L, 0xE902C80000000000L},
        {0x86D7E40000000000L, 0xF066340000000000L},
        {0x9888060000000000L, 0x910ABE0000000000L},
        {0xFF30E30000000000L, 0xB27EFB0000000000L},
        {0x8E375B8000000000L, 0xA03D948000000000L},
        {0xD1415C4000000000L, 0xF5357CC000000000L},
        {0x91A916E000000000L, 0xB6CE66E000000000L},
        {0xE6D2FC5000000000L, 0xD55882B000000000L},
        {0x9A3BA0B800000000L, 0xFBD654E800000000L},
        {0xAEA5D2E400000000L, 0xF0E533AC00000000L},
        {0xDA88B7BE00000000L, 0xAA3AAEDE00000000L},
        {0xBA75BB4300000000L, 0xF5A811C500000000L},
        {0x9B6C9A2F80000000L, 0x9603CCED80000000L},
        {0xFABB538840000000L, 0xE2747090C0000000L},
        {0x8358898EA0000000L, 0x8C698D3D20000000L},
        {0xDA8ABD5BF0000000L, 0xF6DF3A0AF0000000L},
        {0xB090C3F758000000L, 0xD3B4D3D298000000L},
        {0xAD9882F5BC000000L, 0x88DA4FB544000000L},
        {0xC3C366272A000000L, 0xDCCF2A2262000000L},
        {0x9BC0224A97000000L, 0xAF5D96F273000000L},
        {0x8643FFF621800000L, 0x8E390C6EDC800000L},
        {0xE45C01919BC00000L, 0xCBB34C8945C00000L},
        {0x80D8141BC2E00000L, 0x886AFC3912200000L},
        {0xF605807C26500000L, 0xA3B92D28F6300000L},
        {0xCE9A2CFC76280000L, 0x98400C1921280000L},
        {0xF61894904C040000L, 0xC8BE6DBCEC8C0000L},
        {0xE3A44C104D160000L, 0xCA84A59443760000L},
        {0xC7E84953A11B0000L, 0xD9D4F6AA02CB0000L},
        {0xC26CDD1C9A358000L, 0x8BE8478434328000L},
        {0xAE125DBEB198C000L, 0xFCC5DBFD5AAAC000L},
        {0x86DE52A079A6A000L, 0xC5F16BD883816000L},
        {0xDF82486AAFE37000L, 0xA293EC735692D000L},
        {0xE91ABA275C272800L, 0xD686192874E3C800L},
        {0x963D0960DAB3FC00L, 0xBA9DE62072621400L},
        {0xA2188C4E8A46CE00L, 0xD31F75BCB4977E00L},
        {0xC43A416020A6CB00L, 0x99F57FECA12B3900L},
        {0xA4F72EF82A58AE80L, 0xCECE4391B81DA380L},
        {0xB39F119264BC0940L, 0x80A277D20DABB9C0L},
        {0xFD6616C0CBFA0B20L, 0xED16E64117DC11A0L},
        {0xFFA8BC44327B5390L, 0xEDFB13DB3B66C210L},
        {0xCAE8EB99E73AB548L, 0xC86135B6EA2F0B98L},
        {0xBA49BADCDD19B16CL, 0x8F1944AFB18564C4L},
        {0xECFC86D765EABBEEL, 0x9190E1C46CC13702L},
        {0xE1F8D6B3195D6D97L, 0xDF70267FF5E4C979L},
        {0xD74307D3FD3382DBL, 0x9999B3FFDC769B48L}
    };

    public static final FPGenerator std64 = make(polynomials[64][0], 64);

    public static final FPGenerator std32 = make(polynomials[32][0], 32);
    
    public static final FPGenerator std40 = make(polynomials[40][0], 40);

    public static final FPGenerator std24 = make(polynomials[24][0], 24);
}

