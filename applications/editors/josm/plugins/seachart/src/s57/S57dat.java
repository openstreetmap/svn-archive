// License: GPL. For details, see LICENSE file.
package s57;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

import s57.S57map.Feature;
import s57.S57map.Pflag;
import s57.S57obj.Obj;

/**
 * @author Malcolm Herring
 */
public final class S57dat { // S57 ENC file fields lookup tables & methods
    private S57dat() {
        // Hide default constructor for utilities classes
    }
    // CHECKSTYLE.OFF: LineLength

    public static class S57conv {
        int asc;    // 0=A(), 1+=A(n)
        int bin;    // 0=ASCII, +ve=b1n(unsigned LE), -ve=b2n(signed LE), n>8=b1(n/8)(unsigned BE)
        S57conv(int a, int b) {
            asc = a; bin = b;
        }
    }

    public enum S57subf { I8RN, RCNM, RCID, EXPP, INTU, DSNM, EDTN, UPDN, UADT, ISDT, STED, PRSP, PSDN, PRED, PROF, AGEN, COMT, DSTR, AALL, NALL, NOMR, NOCR, NOGR, NOLR, NOIN, NOCN,
        NOED, NOFA, HDAT, VDAT, SDAT, CSCL, DUNI, HUNI, PUNI, COUN, COMF, SOMF, PROJ, PRP1, PRP2, PRP3, PRP4, FEAS, FNOR, FPMF, RPID, RYCO, RXCO, CURP, RXVL, RYVL, PRCO, ESDT,
        LSDT, DCRT, CODT, PACC, HACC, SACC, FILE, LFIL, VOLM, IMPL, SLAT, WLON, NLAT, ELON, CRCS, NAM1, NAM2, OORA, OAAC, OACO, OALL, OATY, DEFN, AUTH, RFTP, RFVL, ATLB, ATDO,
        ADMU, ADFT, RAVA, DVAL, DVSD, OBLB, ASET, PRIM, GRUP, OBJL, RVER, RUIN, FIDN, FIDS, ATTL, ATVL, FFUI, FFIX, NFPT, LNAM, RIND, FSUI, FSIX, NSPT, NAME, ORNT, USAG, MASK,
        VPUI, VPIX, NVPT, TOPI, CCUI, CCIX, CCNC, YCOO, XCOO, VE3D, ATYP, SURF, ORDR, RESO, STPT, CTPT, ENPT, CDPM, CDPR }

    private static final EnumMap<S57subf, S57conv> convs = new EnumMap<>(S57subf.class);
    static {
        convs.put(S57subf.I8RN, new S57conv(5, 2));
        convs.put(S57subf.RCNM, new S57conv(2, 1)); convs.put(S57subf.RCID, new S57conv(10, 4)); convs.put(S57subf.EXPP, new S57conv(1, 1));
        convs.put(S57subf.INTU, new S57conv(1, 1)); convs.put(S57subf.DSNM, new S57conv(0, 0)); convs.put(S57subf.EDTN, new S57conv(0, 0));
        convs.put(S57subf.UPDN, new S57conv(0, 0)); convs.put(S57subf.UADT, new S57conv(8, 0)); convs.put(S57subf.ISDT, new S57conv(8, 0));
        convs.put(S57subf.STED, new S57conv(4, 0)); convs.put(S57subf.PRSP, new S57conv(3, 1)); convs.put(S57subf.PSDN, new S57conv(0, 0));
        convs.put(S57subf.PRED, new S57conv(0, 0)); convs.put(S57subf.PROF, new S57conv(2, 1)); convs.put(S57subf.AGEN, new S57conv(2, 2));
        convs.put(S57subf.COMT, new S57conv(0, 0)); convs.put(S57subf.DSTR, new S57conv(2, 1)); convs.put(S57subf.AALL, new S57conv(1, 1));
        convs.put(S57subf.NALL, new S57conv(1, 1)); convs.put(S57subf.NOMR, new S57conv(0, 4)); convs.put(S57subf.NOCR, new S57conv(0, 4));
        convs.put(S57subf.NOGR, new S57conv(0, 4)); convs.put(S57subf.NOLR, new S57conv(0, 4)); convs.put(S57subf.NOIN, new S57conv(0, 4));
        convs.put(S57subf.NOCN, new S57conv(0, 4)); convs.put(S57subf.NOED, new S57conv(0, 4)); convs.put(S57subf.NOFA, new S57conv(0, 4));
        convs.put(S57subf.HDAT, new S57conv(3, 1)); convs.put(S57subf.VDAT, new S57conv(2, 1)); convs.put(S57subf.SDAT, new S57conv(2, 1));
        convs.put(S57subf.CSCL, new S57conv(0, 4)); convs.put(S57subf.DUNI, new S57conv(2, 1)); convs.put(S57subf.HUNI, new S57conv(2, 1));
        convs.put(S57subf.PUNI, new S57conv(2, 1)); convs.put(S57subf.COUN, new S57conv(2, 1)); convs.put(S57subf.COMF, new S57conv(0, 4));
        convs.put(S57subf.SOMF, new S57conv(0, 4)); convs.put(S57subf.PROJ, new S57conv(3, 1)); convs.put(S57subf.PRP1, new S57conv(0, -4));
        convs.put(S57subf.PRP2, new S57conv(0, -4)); convs.put(S57subf.PRP3, new S57conv(0, -4)); convs.put(S57subf.PRP4, new S57conv(0, -4));
        convs.put(S57subf.FEAS, new S57conv(0, -4)); convs.put(S57subf.FNOR, new S57conv(0, -4)); convs.put(S57subf.FPMF, new S57conv(0, 4));
        convs.put(S57subf.RPID, new S57conv(1, 1)); convs.put(S57subf.RYCO, new S57conv(0, -4)); convs.put(S57subf.RXCO, new S57conv(0, -4));
        convs.put(S57subf.CURP, new S57conv(2, 1)); convs.put(S57subf.RXVL, new S57conv(0, -4)); convs.put(S57subf.RYVL, new S57conv(0, -4));
        convs.put(S57subf.PRCO, new S57conv(2, 2)); convs.put(S57subf.ESDT, new S57conv(8, 0)); convs.put(S57subf.LSDT, new S57conv(8, 0));
        convs.put(S57subf.DCRT, new S57conv(0, 0)); convs.put(S57subf.CODT, new S57conv(8, 0)); convs.put(S57subf.PACC, new S57conv(0, 4));
        convs.put(S57subf.HACC, new S57conv(0, 4)); convs.put(S57subf.SACC, new S57conv(0, 4)); convs.put(S57subf.FILE, new S57conv(0, 0));
        convs.put(S57subf.LFIL, new S57conv(0, 0)); convs.put(S57subf.VOLM, new S57conv(0, 0)); convs.put(S57subf.IMPL, new S57conv(3, 0));
        convs.put(S57subf.SLAT, new S57conv(0, 0)); convs.put(S57subf.WLON, new S57conv(0, 0)); convs.put(S57subf.NLAT, new S57conv(0, 0));
        convs.put(S57subf.ELON, new S57conv(0, 0)); convs.put(S57subf.CRCS, new S57conv(0, 0)); convs.put(S57subf.NAM1, new S57conv(12, 5));
        convs.put(S57subf.NAM2, new S57conv(12, 5)); convs.put(S57subf.OORA, new S57conv(1, 1)); convs.put(S57subf.OAAC, new S57conv(6, 0));
        convs.put(S57subf.OACO, new S57conv(5, 2)); convs.put(S57subf.OALL, new S57conv(0, 0)); convs.put(S57subf.OATY, new S57conv(1, 1));
        convs.put(S57subf.DEFN, new S57conv(0, 0)); convs.put(S57subf.AUTH, new S57conv(2, 2)); convs.put(S57subf.RFTP, new S57conv(2, 1));
        convs.put(S57subf.RFVL, new S57conv(0, 0)); convs.put(S57subf.ATLB, new S57conv(5, 2)); convs.put(S57subf.ATDO, new S57conv(1, 1));
        convs.put(S57subf.ADMU, new S57conv(0, 0)); convs.put(S57subf.ADFT, new S57conv(0, 0)); convs.put(S57subf.RAVA, new S57conv(1, 1));
        convs.put(S57subf.DVAL, new S57conv(0, 0)); convs.put(S57subf.DVSD, new S57conv(0, 0)); convs.put(S57subf.OBLB, new S57conv(5, 2));
        convs.put(S57subf.ASET, new S57conv(1, 1)); convs.put(S57subf.PRIM, new S57conv(1, 1)); convs.put(S57subf.GRUP, new S57conv(3, 1));
        convs.put(S57subf.OBJL, new S57conv(5, 2)); convs.put(S57subf.RVER, new S57conv(3, 2)); convs.put(S57subf.RUIN, new S57conv(1, 1));
        convs.put(S57subf.FIDN, new S57conv(10, 4)); convs.put(S57subf.FIDS, new S57conv(5, 2)); convs.put(S57subf.ATTL, new S57conv(5, 2));
        convs.put(S57subf.ATVL, new S57conv(0, 0)); convs.put(S57subf.FFUI, new S57conv(1, 1)); convs.put(S57subf.FFIX, new S57conv(0, 2));
        convs.put(S57subf.NFPT, new S57conv(0, 2)); convs.put(S57subf.LNAM, new S57conv(17, 8)); convs.put(S57subf.RIND, new S57conv(0, 1));
        convs.put(S57subf.FSUI, new S57conv(1, 1)); convs.put(S57subf.FSIX, new S57conv(0, 2)); convs.put(S57subf.NSPT, new S57conv(0, 2));
        convs.put(S57subf.NAME, new S57conv(12, 5)); convs.put(S57subf.ORNT, new S57conv(1, 1)); convs.put(S57subf.USAG, new S57conv(1, 1));
        convs.put(S57subf.MASK, new S57conv(1, 1)); convs.put(S57subf.VPUI, new S57conv(1, 1)); convs.put(S57subf.VPIX, new S57conv(0, 2));
        convs.put(S57subf.NVPT, new S57conv(0, 2)); convs.put(S57subf.TOPI, new S57conv(1, 1)); convs.put(S57subf.CCUI, new S57conv(1, 1));
        convs.put(S57subf.CCIX, new S57conv(0, 2)); convs.put(S57subf.CCNC, new S57conv(0, 2)); convs.put(S57subf.YCOO, new S57conv(0, -4));
        convs.put(S57subf.XCOO, new S57conv(0, -4)); convs.put(S57subf.VE3D, new S57conv(0, -4)); convs.put(S57subf.ATYP, new S57conv(1, 1));
        convs.put(S57subf.SURF, new S57conv(1, 1)); convs.put(S57subf.ORDR, new S57conv(1, 1)); convs.put(S57subf.RESO, new S57conv(0, 4));
        convs.put(S57subf.STPT, new S57conv(0, 0)); convs.put(S57subf.CTPT, new S57conv(0, 0)); convs.put(S57subf.ENPT, new S57conv(0, 0));
        convs.put(S57subf.CDPM, new S57conv(0, 0)); convs.put(S57subf.CDPR, new S57conv(0, 0));
    }

    public enum S57field { I8RI, DSID, DSSI, DSPM, DSPR, DSRC, DSHT, DSAC, CATD, CATX, DDDF, DDDR, DDDI, DDOM, DDRF, DDSI, DDSC,
        FRID, FOID, LNAM, ATTF, NATF, FFPC, FFPT, FSPC, FSPT, VRID, ATTV, VRPC, VRPT, SGCC, SG2D, SG3D, ARCC, AR2D, EL2D, CT2D }

    private static ArrayList<S57subf> S57i8ri = new ArrayList<>(Arrays.asList(S57subf.I8RN));
    private static ArrayList<S57subf> S57dsid = new ArrayList<>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.EXPP, S57subf.INTU, S57subf.DSNM, S57subf.EDTN, S57subf.UPDN,
            S57subf.UADT, S57subf.ISDT, S57subf.STED, S57subf.PRSP, S57subf.PSDN, S57subf.PRED, S57subf.PROF, S57subf.AGEN, S57subf.COMT));
    private static ArrayList<S57subf> S57dssi = new ArrayList<>(Arrays.asList(S57subf.DSTR, S57subf.AALL, S57subf.NALL, S57subf.NOMR, S57subf.NOCR, S57subf.NOGR, S57subf.NOLR,
            S57subf.NOIN, S57subf.NOCN, S57subf.NOED, S57subf.NOFA));
    private static ArrayList<S57subf> S57dspm = new ArrayList<>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.HDAT, S57subf.VDAT, S57subf.SDAT, S57subf.CSCL, S57subf.DUNI,
            S57subf.HUNI, S57subf.PUNI, S57subf.COUN, S57subf.COMF, S57subf.SOMF, S57subf.COMT));
    private static ArrayList<S57subf> S57dspr = new ArrayList<>(Arrays.asList(S57subf.PROJ, S57subf.PRP1, S57subf.PRP2, S57subf.PRP3, S57subf.PRP4, S57subf.FEAS, S57subf.FNOR,
            S57subf.FPMF, S57subf.COMT));
    private static ArrayList<S57subf> S57dsrc = new ArrayList<>(Arrays.asList(S57subf.RPID, S57subf.RYCO, S57subf.RXCO, S57subf.CURP, S57subf.FPMF, S57subf.RXVL, S57subf.RYVL,
            S57subf.COMT));
    private static ArrayList<S57subf> S57dsht = new ArrayList<>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.PRCO, S57subf.ESDT, S57subf.LSDT, S57subf.DCRT, S57subf.CODT, S57subf.COMT));
    private static ArrayList<S57subf> S57dsac = new ArrayList<>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.PACC, S57subf.HACC, S57subf.SACC, S57subf.FPMF, S57subf.COMT));
    private static ArrayList<S57subf> S57catd = new ArrayList<>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.FILE, S57subf.LFIL, S57subf.VOLM, S57subf.IMPL, S57subf.SLAT,
            S57subf.WLON, S57subf.NLAT, S57subf.ELON, S57subf.CRCS, S57subf.COMT));
    private static ArrayList<S57subf> S57catx = new ArrayList<>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.NAM1, S57subf.NAM2, S57subf.COMT));
    private static ArrayList<S57subf> S57dddf = new ArrayList<>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.OORA, S57subf.OAAC, S57subf.OACO, S57subf.OALL, S57subf.OATY,
            S57subf.DEFN, S57subf.AUTH, S57subf.COMT));
    private static ArrayList<S57subf> S57dddr = new ArrayList<>(Arrays.asList(S57subf.RFTP, S57subf.RFVL));
    private static ArrayList<S57subf> S57dddi = new ArrayList<>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.ATLB, S57subf.ATDO, S57subf.ADMU, S57subf.ADFT, S57subf.AUTH, S57subf.COMT));
    private static ArrayList<S57subf> S57ddom = new ArrayList<>(Arrays.asList(S57subf.RAVA, S57subf.DVAL, S57subf.DVSD, S57subf.DEFN, S57subf.AUTH));
    private static ArrayList<S57subf> S57ddrf = new ArrayList<>(Arrays.asList(S57subf.RFTP, S57subf.RFVL));
    private static ArrayList<S57subf> S57ddsi = new ArrayList<>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.OBLB));
    private static ArrayList<S57subf> S57ddsc = new ArrayList<>(Arrays.asList(S57subf.ATLB, S57subf.ASET, S57subf.AUTH));
    private static ArrayList<S57subf> S57frid = new ArrayList<>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.PRIM, S57subf.GRUP, S57subf.OBJL, S57subf.RVER, S57subf.RUIN));
    private static ArrayList<S57subf> S57foid = new ArrayList<>(Arrays.asList(S57subf.AGEN, S57subf.FIDN, S57subf.FIDS));
    private static ArrayList<S57subf> S57lnam = new ArrayList<>(Arrays.asList(S57subf.LNAM));
    private static ArrayList<S57subf> S57attf = new ArrayList<>(Arrays.asList(S57subf.ATTL, S57subf.ATVL));
    private static ArrayList<S57subf> S57natf = new ArrayList<>(Arrays.asList(S57subf.ATTL, S57subf.ATVL));
    private static ArrayList<S57subf> S57ffpc = new ArrayList<>(Arrays.asList(S57subf.FFUI, S57subf.FFIX, S57subf.NFPT));
    private static ArrayList<S57subf> S57ffpt = new ArrayList<>(Arrays.asList(S57subf.LNAM, S57subf.RIND, S57subf.COMT));
    private static ArrayList<S57subf> S57fspc = new ArrayList<>(Arrays.asList(S57subf.FSUI, S57subf.FSIX, S57subf.NSPT));
    private static ArrayList<S57subf> S57fspt = new ArrayList<>(Arrays.asList(S57subf.NAME, S57subf.ORNT, S57subf.USAG, S57subf.MASK));
    private static ArrayList<S57subf> S57vrid = new ArrayList<>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.RVER, S57subf.RUIN));
    private static ArrayList<S57subf> S57attv = new ArrayList<>(Arrays.asList(S57subf.ATTL, S57subf.ATVL));
    private static ArrayList<S57subf> S57vrpc = new ArrayList<>(Arrays.asList(S57subf.VPUI, S57subf.VPIX, S57subf.NVPT));
    private static ArrayList<S57subf> S57vrpt = new ArrayList<>(Arrays.asList(S57subf.NAME, S57subf.ORNT, S57subf.USAG, S57subf.TOPI, S57subf.MASK));
    private static ArrayList<S57subf> S57sgcc = new ArrayList<>(Arrays.asList(S57subf.CCUI, S57subf.CCIX, S57subf.CCNC));
    private static ArrayList<S57subf> S57sg2d = new ArrayList<>(Arrays.asList(S57subf.YCOO, S57subf.XCOO));
    private static ArrayList<S57subf> S57sg3d = new ArrayList<>(Arrays.asList(S57subf.YCOO, S57subf.XCOO, S57subf.VE3D));
    private static ArrayList<S57subf> S57arcc = new ArrayList<>(Arrays.asList(S57subf.ATYP, S57subf.SURF, S57subf.ORDR, S57subf.RESO, S57subf.FPMF));
    private static ArrayList<S57subf> S57ar2d = new ArrayList<>(Arrays.asList(S57subf.STPT, S57subf.CTPT, S57subf.ENPT, S57subf.YCOO, S57subf.XCOO));
    private static ArrayList<S57subf> S57el2d = new ArrayList<>(Arrays.asList(S57subf.STPT, S57subf.CTPT, S57subf.ENPT, S57subf.CDPM, S57subf.CDPR, S57subf.YCOO, S57subf.XCOO));
    private static ArrayList<S57subf> S57ct2d = new ArrayList<>(Arrays.asList(S57subf.YCOO, S57subf.XCOO));

    private static final EnumMap<S57field, ArrayList<S57subf>> fields = new EnumMap<>(S57field.class);
    static {
        fields.put(S57field.I8RI, S57i8ri);
        fields.put(S57field.DSID, S57dsid); fields.put(S57field.DSSI, S57dssi); fields.put(S57field.DSPM, S57dspm); fields.put(S57field.DSPR, S57dspr);
        fields.put(S57field.DSRC, S57dsrc); fields.put(S57field.DSHT, S57dsht); fields.put(S57field.DSAC, S57dsac); fields.put(S57field.CATD, S57catd);
        fields.put(S57field.CATX, S57catx); fields.put(S57field.DDDF, S57dddf); fields.put(S57field.DDDR, S57dddr); fields.put(S57field.DDDI, S57dddi);
        fields.put(S57field.DDOM, S57ddom); fields.put(S57field.DDRF, S57ddrf); fields.put(S57field.DDSI, S57ddsi); fields.put(S57field.DDSC, S57ddsc);
        fields.put(S57field.FRID, S57frid); fields.put(S57field.FOID, S57foid); fields.put(S57field.LNAM, S57lnam); fields.put(S57field.ATTF, S57attf);
        fields.put(S57field.NATF, S57natf); fields.put(S57field.FFPC, S57ffpc); fields.put(S57field.FFPT, S57ffpt); fields.put(S57field.FFPC, S57fspc);
        fields.put(S57field.FSPT, S57fspt); fields.put(S57field.VRID, S57vrid); fields.put(S57field.ATTV, S57attv); fields.put(S57field.VRPC, S57vrpc);
        fields.put(S57field.VRPT, S57vrpt); fields.put(S57field.SGCC, S57sgcc); fields.put(S57field.SG2D, S57sg2d); fields.put(S57field.SG3D, S57sg3d);
        fields.put(S57field.ARCC, S57arcc); fields.put(S57field.AR2D, S57ar2d); fields.put(S57field.EL2D, S57el2d); fields.put(S57field.CT2D, S57ct2d);
    }

    private static byte[] leader = {'0', '0', '0', '0', '0', ' ', 'D', ' ', ' ', ' ', ' ', ' ', '0', '0', '0', '0', '0', ' ', ' ', ' ', '0', '0', '0', '4'};
    private static byte[] buffer;
    private static int offset;
    private static int maxoff;
    private static int index;
    private static S57field field;
    private static String aall = "US-ASCII";
    private static String nall = "US-ASCII";
    public static int rnum;

    private static S57conv findSubf(S57subf subf) {
        ArrayList<S57subf> subs = fields.get(field);
        boolean wrap = false;
        while (true) {
            if (index == subs.size()) {
                if (!wrap) {
                    index = 0;
                    wrap = true;
                } else {
                    System.out.println("ERROR: Subfield not found " + subf.name() + " in " + field.name() + " in record " + rnum);
                    System.exit(-1);
                }
            }
            S57subf sub = subs.get(index++);
            S57conv conv = convs.get(sub);
            if (sub == subf) {
                return conv;
            } else {
                offset += (conv.bin != 0) ? ((conv.bin < 8) ? Math.abs(conv.bin) : conv.bin / 8) : conv.asc;
            }
        }
    }

    public static void setField(byte[] buf, int off, S57field fld, int len) {
        buffer = buf;
        offset = off;
        maxoff = off + len - 1;
        field = fld;
        index = 0;
    }

    public static boolean more() {
        return (offset < maxoff);
    }

    public static Object decSubf(byte[] buf, int off, S57field fld, S57subf subf) {
        buffer = buf;
        offset = off;
        index = 0;
        return decSubf(fld, subf);
    }

    public static Object decSubf(S57field fld, S57subf subf) {
        field = fld;
        index = 0;
        return decSubf(subf);
    }

    public static Object decSubf(S57subf subf) {
        S57conv conv = findSubf(subf);
        if (conv.bin == 0) {
            String str = "";
            int i = 0;
            if (conv.asc == 0) {
                for (i = 0; buffer[offset+i] != 0x1f; i++) { }
                try {
                    String charset = "";
                    if (field == S57field.ATTF) charset = aall;
                    else if (field == S57field.NATF) charset = nall;
                    else charset = "US-ASCII";
                    str = new String(buffer, offset, i, charset);
                } catch (UnsupportedEncodingException e) {
                    System.err.println(e.getMessage());
                    System.exit(-1);
                }
                offset += i + 1;
            } else {
                str = new String(buffer, offset, conv.asc);
                offset += conv.asc;
            }
            return str;
        } else {
            int f = Math.abs(conv.bin);
            if (f < 5) {
                long val = buffer[offset + --f];
                if (conv.bin > 0)
                    val &= 0xff;
                while (f > 0) {
                    val = (val << 8) + (buffer[offset + --f] & 0xff);
                }
                offset += Math.abs(conv.bin);
                if ((subf == S57subf.AALL) || (subf == S57subf.NALL)) {
                    String charset = "";
                    switch ((int) val) {
                    case 0:
                        charset = "US-ASCII";
                        break;
                    case 1:
                        charset = "ISO-8859-1";
                        break;
                    case 2:
                        charset = "UTF-16LE";
                        break;
                    }
                    if (subf == S57subf.NALL) {
                        nall = charset;
                    } else {
                        aall = charset;
                    }
                }
                return val;
            } else {
                if (f == 5) {
                    long val = buffer[offset++] & 0xff;
                    f--;
                    while (f > 0) {
                        val = (val << 8) + (buffer[offset + --f] & 0xff);
                    }
                    offset += 4;
                    return val;
                } else {
                    long val = buffer[offset++] & 0xff;
                    val = (val << 8) + (buffer[offset++] & 0xff);
                    f = 4;
                    while (f > 0) {
                        val = (val << 8) + (buffer[offset + --f] & 0xff);
                    }
                    offset += 4;
                    f = 2;
                    while (f > 0) {
                        val = (val << 8) + (buffer[offset + --f] & 0xff);
                    }
                    offset += 2;
                    return val;
                }
            }
        }
    }

    public static byte[] encSubf(S57subf subf, Object val) {
        S57conv conv = convs.get(subf);
        if ((conv.bin == 0) || asc) {
            String sval = "";
            if (val instanceof String) {
                sval = (String) val;
            } else if (val instanceof Integer) {
                sval = ((Integer) val).toString();
            } else if (val instanceof Long) {
                sval = ((Long) val).toString();
            } else if (val instanceof Double) {
                sval = ((Double) val).toString();
            }
            index = sval.length();
            try {
                buffer = (sval + " ").getBytes("ISO-8859-1");
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }
            if (conv.asc == 0) {
                buffer[index] = 0x01f;
            } else {
                buffer = Arrays.copyOf(buffer, conv.asc);
                while (index < buffer.length) {
                    buffer[index++] = ' ';
                }
            }
        } else {
            int f = Math.abs(conv.bin);
            long lval;
            if (val instanceof String) {
                lval = Long.parseLong((String) val);
            } else if (val instanceof Double) {
                double dval = (double) val;
                lval = (long) dval;
            } else if (val instanceof Integer) {
                lval = (int) val;
            } else {
                lval = (long) val;
            }
            buffer = new byte[f];
            for (int i = 0; i < f; i++) {
                buffer[i] = (byte) (lval & 0xff);
                lval >>= 8;
            }
        }
        return buffer;
    }

    static class Index {
        byte[] field;
        int length;
        int offset;
        Index(byte[] id, int l, int o) {
            field = id;
            length = l;
            offset = o;
        }
    }

    public static class Fparams {
        public S57field field;
        public Object[] params;
        public Fparams(S57field f, Object[] p) {
            field = f;
            params = p;
        }
    }

    static boolean asc = false;

    public static byte[] encRecord(String i8rn, ArrayList<Fparams> fparams) {
        asc = true;
        return encRecord(Integer.parseInt(i8rn), fparams);
    }

    public static byte[] encRecord(int i8rn, ArrayList<Fparams> fparams) {
        ArrayList<Index> index = new ArrayList<>();
        int offset = 3;
        int maxlen = 3;
        byte[] buf = encSubf(S57subf.I8RN, i8rn);
        buf = Arrays.copyOf(buf, 3);
        buf[2] = 0x1e;
        index.add(new Index("0001".getBytes(), 3, 0));
        for (Fparams sfparams : fparams) {
            for (int ip = 0; ip < sfparams.params.length;) {
                for (S57subf subf : fields.get(sfparams.field)) {
                    byte[] next = encSubf(subf, sfparams.params[ip++]);
                    buf = Arrays.copyOf(buf, (buf.length + next.length));
                    System.arraycopy(next, 0, buf, (buf.length - next.length), next.length);
                }
            }
            buf = Arrays.copyOf(buf, (buf.length + 1));
            buf[buf.length-1] = 0x1e;
            int flen = buf.length - offset;
            index.add(new Index(sfparams.field.toString().getBytes(StandardCharsets.UTF_8), flen, offset));
            maxlen = (flen > maxlen) ? flen : maxlen;
            offset += flen;
        }
        int mlen = String.valueOf(maxlen).length();
        String ffmt = "%0" + mlen + "d";
        int olen = String.valueOf(offset).length();
        String ofmt = "%0" + olen + "d";
        int ilen = 4 + mlen + olen;
        int isiz = (ilen * index.size()) + 1;
        byte[] ibuf = new byte[isiz];
        int i = 0;
        for (Index item : index) {
            for (byte ch : item.field) {
                ibuf[i++] = ch;
            }
            byte[] digits = String.format(ffmt, item.length).getBytes();
            for (byte ch : digits) {
                ibuf[i++] = ch;
            }
            digits = String.format(ofmt, item.offset).getBytes();
            for (byte ch : digits) {
                ibuf[i++] = ch;
            }
        }
        ibuf[i] = 0x1e;
        byte[] fbuf = Arrays.copyOf(leader, (leader.length + ibuf.length + buf.length));
        System.arraycopy(ibuf, 0, fbuf, leader.length, ibuf.length);
        System.arraycopy(buf, 0, fbuf, (leader.length + ibuf.length), buf.length);
        fbuf[20] = (byte) (mlen + 0x30);
        fbuf[21] = (byte) (olen + 0x30);
        System.arraycopy(String.format("%05d", fbuf.length).getBytes(), 0, fbuf, 0, 5);
        System.arraycopy(String.format("%05d", (leader.length + ibuf.length)).getBytes(), 0, fbuf, 12, 5);
        asc = false;
        return fbuf;
    }

    enum Prims { N, P, L, A, PA, PL, LA, PLA }

    private static final EnumMap<Obj, Prims> S57prims = new EnumMap<>(Obj.class);
    static {
        S57prims.put(Obj.UNKOBJ, Prims.PLA); S57prims.put(Obj.M_COVR, Prims.A); S57prims.put(Obj.M_NSYS, Prims.A); S57prims.put(Obj.AIRARE, Prims.PA);
        S57prims.put(Obj.ACHBRT, Prims.PA); S57prims.put(Obj.ACHARE, Prims.PA); S57prims.put(Obj.BCNCAR, Prims.P); S57prims.put(Obj.BCNISD, Prims.P);
        S57prims.put(Obj.BCNLAT, Prims.P); S57prims.put(Obj.BCNSAW, Prims.P); S57prims.put(Obj.BCNSPP, Prims.P); S57prims.put(Obj.BERTHS, Prims.PLA);
        S57prims.put(Obj.BRIDGE, Prims.PLA); S57prims.put(Obj.BUISGL, Prims.PA); S57prims.put(Obj.BUAARE, Prims.PA); S57prims.put(Obj.BOYCAR, Prims.P);
        S57prims.put(Obj.BOYINB, Prims.P); S57prims.put(Obj.BOYISD, Prims.P); S57prims.put(Obj.BOYLAT, Prims.P); S57prims.put(Obj.BOYSAW, Prims.P);
        S57prims.put(Obj.BOYSPP, Prims.P); S57prims.put(Obj.CBLARE, Prims.A); S57prims.put(Obj.CBLOHD, Prims.L); S57prims.put(Obj.CBLSUB, Prims.L);
        S57prims.put(Obj.CANALS, Prims.A); S57prims.put(Obj.CTSARE, Prims.PA); S57prims.put(Obj.CAUSWY, Prims.LA); S57prims.put(Obj.CTNARE, Prims.PA);
        S57prims.put(Obj.CHKPNT, Prims.PA); S57prims.put(Obj.CGUSTA, Prims.P); S57prims.put(Obj.COALNE, Prims.L); S57prims.put(Obj.CONZNE, Prims.A);
        S57prims.put(Obj.COSARE, Prims.A); S57prims.put(Obj.CTRPNT, Prims.P); S57prims.put(Obj.CONVYR, Prims.LA); S57prims.put(Obj.CRANES, Prims.PA);
        S57prims.put(Obj.CURENT, Prims.P); S57prims.put(Obj.CUSZNE, Prims.A); S57prims.put(Obj.DAMCON, Prims.LA); S57prims.put(Obj.DAYMAR, Prims.P);
        S57prims.put(Obj.DWRTCL, Prims.L); S57prims.put(Obj.DWRTPT, Prims.A); S57prims.put(Obj.DEPARE, Prims.A); S57prims.put(Obj.DEPCNT, Prims.L);
        S57prims.put(Obj.DISMAR, Prims.P); S57prims.put(Obj.DOCARE, Prims.A); S57prims.put(Obj.DRGARE, Prims.A); S57prims.put(Obj.DRYDOC, Prims.A);
        S57prims.put(Obj.DMPGRD, Prims.PA); S57prims.put(Obj.DYKCON, Prims.L); S57prims.put(Obj.EXEZNE, Prims.A); S57prims.put(Obj.FAIRWY, Prims.A);
        S57prims.put(Obj.FNCLNE, Prims.L); S57prims.put(Obj.FERYRT, Prims.LA); S57prims.put(Obj.FSHZNE, Prims.A); S57prims.put(Obj.FSHFAC, Prims.PLA);
        S57prims.put(Obj.FSHGRD, Prims.A); S57prims.put(Obj.FLODOC, Prims.A); S57prims.put(Obj.FOGSIG, Prims.P); S57prims.put(Obj.FORSTC, Prims.PLA);
        S57prims.put(Obj.FRPARE, Prims.A); S57prims.put(Obj.GATCON, Prims.PLA); S57prims.put(Obj.GRIDRN, Prims.PA); S57prims.put(Obj.HRBARE, Prims.A);
        S57prims.put(Obj.HRBFAC, Prims.PA); S57prims.put(Obj.HULKES, Prims.PA); S57prims.put(Obj.ICEARE, Prims.A); S57prims.put(Obj.ICNARE, Prims.PA);
        S57prims.put(Obj.ISTZNE, Prims.A); S57prims.put(Obj.LAKARE, Prims.A); S57prims.put(Obj.LNDARE, Prims.PLA); S57prims.put(Obj.LNDELV, Prims.PL);
        S57prims.put(Obj.LNDRGN, Prims.PA); S57prims.put(Obj.LNDMRK, Prims.PLA); S57prims.put(Obj.LIGHTS, Prims.P); S57prims.put(Obj.LITFLT, Prims.P);
        S57prims.put(Obj.LITVES, Prims.P); S57prims.put(Obj.LOCMAG, Prims.PLA); S57prims.put(Obj.LOKBSN, Prims.A); S57prims.put(Obj.LOGPON, Prims.PA);
        S57prims.put(Obj.MAGVAR, Prims.PLA); S57prims.put(Obj.MARCUL, Prims.PLA); S57prims.put(Obj.MIPARE, Prims.PA); S57prims.put(Obj.MORFAC, Prims.PLA);
        S57prims.put(Obj.MPAARE, Prims.PA); S57prims.put(Obj.NAVLNE, Prims.L); S57prims.put(Obj.OBSTRN, Prims.PLA); S57prims.put(Obj.OFSPLF, Prims.PA);
        S57prims.put(Obj.OSPARE, Prims.A); S57prims.put(Obj.OILBAR, Prims.L); S57prims.put(Obj.PILPNT, Prims.P); S57prims.put(Obj.PILBOP, Prims.PA);
        S57prims.put(Obj.PIPARE, Prims.PA); S57prims.put(Obj.PIPOHD, Prims.L); S57prims.put(Obj.PIPSOL, Prims.PL); S57prims.put(Obj.PONTON, Prims.LA);
        S57prims.put(Obj.PRCARE, Prims.PA); S57prims.put(Obj.PRDARE, Prims.PA); S57prims.put(Obj.PYLONS, Prims.PA); S57prims.put(Obj.RADLNE, Prims.L);
        S57prims.put(Obj.RADRNG, Prims.A); S57prims.put(Obj.RADRFL, Prims.P); S57prims.put(Obj.RADSTA, Prims.P); S57prims.put(Obj.RTPBCN, Prims.P);
        S57prims.put(Obj.RDOCAL, Prims.PL); S57prims.put(Obj.RDOSTA, Prims.P); S57prims.put(Obj.RAILWY, Prims.L); S57prims.put(Obj.RAPIDS, Prims.PLA);
        S57prims.put(Obj.RCRTCL, Prims.L); S57prims.put(Obj.RECTRC, Prims.LA); S57prims.put(Obj.RCTLPT, Prims.PA); S57prims.put(Obj.RSCSTA, Prims.P);
        S57prims.put(Obj.RESARE, Prims.A); S57prims.put(Obj.RETRFL, Prims.P); S57prims.put(Obj.RIVERS, Prims.LA); S57prims.put(Obj.ROADWY, Prims.PLA);
        S57prims.put(Obj.RUNWAY, Prims.PLA); S57prims.put(Obj.SNDWAV, Prims.PLA); S57prims.put(Obj.SEAARE, Prims.PA); S57prims.put(Obj.SPLARE, Prims.PA);
        S57prims.put(Obj.SBDARE, Prims.PLA); S57prims.put(Obj.SLCONS, Prims.PLA); S57prims.put(Obj.SISTAT, Prims.P); S57prims.put(Obj.SISTAW, Prims.P);
        S57prims.put(Obj.SILTNK, Prims.PA); S57prims.put(Obj.SLOTOP, Prims.L); S57prims.put(Obj.SLOGRD, Prims.PA); S57prims.put(Obj.SMCFAC, Prims.PA);
        S57prims.put(Obj.SOUNDG, Prims.P); S57prims.put(Obj.SPRING, Prims.P); S57prims.put(Obj.STSLNE, Prims.L); S57prims.put(Obj.SUBTLN, Prims.A);
        S57prims.put(Obj.SWPARE, Prims.A); S57prims.put(Obj.TESARE, Prims.A); S57prims.put(Obj.TS_PRH, Prims.PA); S57prims.put(Obj.TS_PNH, Prims.PA);
        S57prims.put(Obj.TS_PAD, Prims.PA); S57prims.put(Obj.TS_TIS, Prims.PA); S57prims.put(Obj.T_HMON, Prims.PA); S57prims.put(Obj.T_NHMN, Prims.PA);
        S57prims.put(Obj.T_TIMS, Prims.PA); S57prims.put(Obj.TIDEWY, Prims.LA); S57prims.put(Obj.TOPMAR, Prims.P); S57prims.put(Obj.TSELNE, Prims.LA);
        S57prims.put(Obj.TSSBND, Prims.L); S57prims.put(Obj.TSSCRS, Prims.A); S57prims.put(Obj.TSSLPT, Prims.A); S57prims.put(Obj.TSSRON, Prims.A);
        S57prims.put(Obj.TSEZNE, Prims.A); S57prims.put(Obj.TUNNEL, Prims.LA); S57prims.put(Obj.TWRTPT, Prims.A); S57prims.put(Obj.UWTROC, Prims.P);
        S57prims.put(Obj.UNSARE, Prims.A); S57prims.put(Obj.VEGATN, Prims.PLA); S57prims.put(Obj.WATTUR, Prims.PLA); S57prims.put(Obj.WATFAL, Prims.PL);
        S57prims.put(Obj.WEDKLP, Prims.PA); S57prims.put(Obj.WRECKS, Prims.PA); S57prims.put(Obj.TS_FEB, Prims.PA);
        S57prims.put(Obj.NOTMRK, Prims.P); S57prims.put(Obj.WTWAXS, Prims.L); S57prims.put(Obj.WTWPRF, Prims.L); S57prims.put(Obj.BUNSTA, Prims.PA);
        S57prims.put(Obj.COMARE, Prims.A); S57prims.put(Obj.HRBBSN, Prims.A); S57prims.put(Obj.LKBSPT, Prims.A); S57prims.put(Obj.PRTARE, Prims.A);
        S57prims.put(Obj.REFDMP, Prims.P); S57prims.put(Obj.TERMNL, Prims.PA); S57prims.put(Obj.TRNBSN, Prims.PA); S57prims.put(Obj.WTWARE, Prims.A);
        S57prims.put(Obj.WTWGAG, Prims.PA); S57prims.put(Obj.TISDGE, Prims.N); S57prims.put(Obj.VEHTRF, Prims.PA); S57prims.put(Obj.EXCNST, Prims.PA);
        S57prims.put(Obj.LG_SDM, Prims.A); S57prims.put(Obj.LG_VSP, Prims.A); S57prims.put(Obj.LITMAJ, Prims.P); S57prims.put(Obj.LITMIN, Prims.P);
    }

    public static void S57geoms(S57map map) {
        for (ArrayList<Feature> list : map.features.values()) {
            for (Feature feature : list) {
                switch (S57prims.get(feature.type)) {
                case N:
                    break;
                case P:
                    if (feature.geom.prim != Pflag.POINT) {
                        //                        Snode node = feature.geom.centre;
                        //                        node.flg = Nflag.ISOL;
                        //                        map.nodes.put(++map.xref, node);
                        //                        feature.geom = map.new Geom(Pflag.POINT);
                        //                        feature.geom.centre = node;
                        //                        feature.geom.elems.add(map.new Prim(map.xref));
                    }
                    break;
                case L:
                    break;
                case A:
                    break;
                case PA:
                    break;
                case PL:
                    break;
                case LA:
                    if (feature.geom.prim == Pflag.POINT) {
                        //                        list.remove(feature);
                    }
                    break;
                case PLA:
                    // No changes needed
                    break;
                }
            }
        }
    }
}
