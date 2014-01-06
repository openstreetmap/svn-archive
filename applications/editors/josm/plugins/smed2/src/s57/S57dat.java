package s57;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

import js57toosm.Js57toosm;

public class S57dat {
	
	public enum Dom { BT, GT, DG, DATE, INT, REAL, AN, HEX, CL }
	
	public static class S57conv {
		int asc;	// 0=A(), 1+=A(n)
		int bin;	// 0=ASCII, +ve=b1n, -ve=b2n
		Dom dom;	// S57 data domain
		S57conv(int a, int b, Dom d) {
			asc = a; bin = b; dom = d;
		}
	}
	
	public enum S57subf { I8RN, RCNM, RCID, EXPP, INTU, DSNM, EDTN, UPDN, UADT, ISDT, STED, PRSP, PSDN, PRED, PROF, AGEN, COMT, DSTR, AALL, NALL, NOMR, NOCR, NOGR, NOLR, NOIN, NOCN,
		NOED, NOFA, HDAT, VDAT, SDAT, CSCL, DUNI, HUNI, PUNI, COUN, COMF, SOMF, PROJ, PRP1, PRP2, PRP3, PRP4, FEAS, FNOR, FPMF, RPID, RYCO, RXCO, CURP, RXVL, RYVL, PRCO, ESDT,
		LSDT, DCRT, CODT, PACC, HACC, SACC, FILE, LFIL, VOLM, IMPL, SLAT, WLON, NLAT, ELON, CRCS, NAM1, NAM2, OORA, OAAC, OACO, OALL, OATY, DEFN, AUTH, RFTP, RFVL, ATLB, ATDO,
		ADMU, ADFT, RAVA, DVAL, DVSD, OBLB, ASET, PRIM, GRUP, OBJL, RVER, RUIN, FIDN, FIDS, ATTL, ATVL, FFUI, FFIX, NFPT, LNAM, RIND, FSUI, FSIX, NSPT, NAME, ORNT, USAG, MASK,
		VPUI, VPIX, NVPT, TOPI, CCUI, CCIX, CCNC, YCOO, XCOO, VE3D, ATYP, SURF, ORDR, RESO, STPT, CTPT, ENPT, CDPM, CDPR }

	private static final EnumMap<S57subf, S57conv> convs = new EnumMap<S57subf, S57conv>(S57subf.class);
	static {
		convs.put(S57subf.I8RN, new S57conv(5,2,Dom.INT));
		convs.put(S57subf.RCNM, new S57conv(2,1,Dom.AN)); convs.put(S57subf.RCID, new S57conv(10,32,Dom.INT)); convs.put(S57subf.EXPP, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.INTU, new S57conv(1,1,Dom.INT)); convs.put(S57subf.DSNM, new S57conv(0,0,Dom.BT)); convs.put(S57subf.EDTN, new S57conv(0,0,Dom.BT));
		convs.put(S57subf.UPDN, new S57conv(0,0,Dom.BT)); convs.put(S57subf.UADT, new S57conv(8,0,Dom.DATE)); convs.put(S57subf.ISDT, new S57conv(8,0,Dom.DATE));
		convs.put(S57subf.STED, new S57conv(4,0,Dom.REAL)); convs.put(S57subf.PRSP, new S57conv(3,1,Dom.AN)); convs.put(S57subf.PSDN, new S57conv(0,0,Dom.BT));
		convs.put(S57subf.PRED, new S57conv(0,0,Dom.BT)); convs.put(S57subf.PROF, new S57conv(2,1,Dom.AN)); convs.put(S57subf.AGEN, new S57conv(2,2,Dom.AN));
		convs.put(S57subf.COMT, new S57conv(0,0,Dom.BT)); convs.put(S57subf.DSTR, new S57conv(2,1,Dom.AN)); convs.put(S57subf.AALL, new S57conv(1,1,Dom.INT));
		convs.put(S57subf.NALL, new S57conv(1,1,Dom.INT)); convs.put(S57subf.NOMR, new S57conv(0,4,Dom.INT)); convs.put(S57subf.NOCR, new S57conv(0,4,Dom.INT));
		convs.put(S57subf.NOGR, new S57conv(0,4,Dom.INT)); convs.put(S57subf.NOLR, new S57conv(0,4,Dom.INT)); convs.put(S57subf.NOIN, new S57conv(0,4,Dom.INT));
		convs.put(S57subf.NOCN, new S57conv(0,4,Dom.INT)); convs.put(S57subf.NOED, new S57conv(0,4,Dom.INT)); convs.put(S57subf.NOFA, new S57conv(0,4,Dom.INT));
		convs.put(S57subf.HDAT, new S57conv(3,1,Dom.INT)); convs.put(S57subf.VDAT, new S57conv(2,1,Dom.INT)); convs.put(S57subf.SDAT, new S57conv(2,1,Dom.INT));
		convs.put(S57subf.CSCL, new S57conv(0,4,Dom.INT)); convs.put(S57subf.DUNI, new S57conv(2,1,Dom.INT)); convs.put(S57subf.HUNI, new S57conv(2,1,Dom.INT));
		convs.put(S57subf.PUNI, new S57conv(2,1,Dom.INT)); convs.put(S57subf.COUN, new S57conv(2,1,Dom.AN)); convs.put(S57subf.COMF, new S57conv(0,4,Dom.INT));
		convs.put(S57subf.SOMF, new S57conv(0,4,Dom.INT)); convs.put(S57subf.PROJ, new S57conv(3,1,Dom.AN)); convs.put(S57subf.PRP1, new S57conv(0,-4,Dom.REAL));
		convs.put(S57subf.PRP2, new S57conv(0,-4,Dom.REAL)); convs.put(S57subf.PRP3, new S57conv(0,-4,Dom.REAL)); convs.put(S57subf.PRP4, new S57conv(0,-4,Dom.REAL));
		convs.put(S57subf.FEAS, new S57conv(0,-4,Dom.REAL)); convs.put(S57subf.FNOR, new S57conv(0,-4,Dom.REAL)); convs.put(S57subf.FPMF, new S57conv(0,4,Dom.INT));
		convs.put(S57subf.RPID, new S57conv(1,1,Dom.DG)); convs.put(S57subf.RYCO, new S57conv(0,-4,Dom.REAL)); convs.put(S57subf.RXCO, new S57conv(0,-4,Dom.REAL));
		convs.put(S57subf.CURP, new S57conv(2,1,Dom.AN)); convs.put(S57subf.RXVL, new S57conv(0,-4,Dom.REAL)); convs.put(S57subf.RYVL, new S57conv(0,-4,Dom.REAL));
		convs.put(S57subf.PRCO, new S57conv(2,2,Dom.AN)); convs.put(S57subf.ESDT, new S57conv(8,0,Dom.DATE)); convs.put(S57subf.LSDT, new S57conv(8,0,Dom.DATE));
		convs.put(S57subf.DCRT, new S57conv(0,0,Dom.BT)); convs.put(S57subf.CODT, new S57conv(8,0,Dom.DATE)); convs.put(S57subf.PACC, new S57conv(0,4,Dom.REAL));
		convs.put(S57subf.HACC, new S57conv(0,4,Dom.REAL)); convs.put(S57subf.SACC, new S57conv(0,4,Dom.REAL)); convs.put(S57subf.FILE, new S57conv(0,0,Dom.BT));
		convs.put(S57subf.LFIL, new S57conv(0,0,Dom.BT)); convs.put(S57subf.VOLM, new S57conv(0,0,Dom.BT)); convs.put(S57subf.IMPL, new S57conv(3,0,Dom.AN));
		convs.put(S57subf.SLAT, new S57conv(0,0,Dom.REAL)); convs.put(S57subf.WLON, new S57conv(0,0,Dom.REAL)); convs.put(S57subf.NLAT, new S57conv(0,0,Dom.REAL));
		convs.put(S57subf.ELON, new S57conv(0,0,Dom.REAL)); convs.put(S57subf.CRCS, new S57conv(0,0,Dom.HEX)); convs.put(S57subf.NAM1, new S57conv(12,40,Dom.AN));
		convs.put(S57subf.NAM2, new S57conv(12,40,Dom.AN)); convs.put(S57subf.OORA, new S57conv(1,1,Dom.AN)); convs.put(S57subf.OAAC, new S57conv(6,0,Dom.BT));
		convs.put(S57subf.OACO, new S57conv(5,2,Dom.INT)); convs.put(S57subf.OALL, new S57conv(0,0,Dom.BT)); convs.put(S57subf.OATY, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.DEFN, new S57conv(0,0,Dom.BT)); convs.put(S57subf.AUTH, new S57conv(2,2,Dom.AN)); convs.put(S57subf.RFTP, new S57conv(2,1,Dom.AN));
		convs.put(S57subf.RFVL, new S57conv(0,0,Dom.BT)); convs.put(S57subf.ATLB, new S57conv(5,2,Dom.INT)); convs.put(S57subf.ATDO, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.ADMU, new S57conv(0,0,Dom.BT)); convs.put(S57subf.ADFT, new S57conv(0,0,Dom.BT)); convs.put(S57subf.RAVA, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.DVAL, new S57conv(0,0,Dom.BT)); convs.put(S57subf.DVSD, new S57conv(0,0,Dom.BT)); convs.put(S57subf.OBLB, new S57conv(5,2,Dom.INT));
		convs.put(S57subf.ASET, new S57conv(1,1,Dom.AN)); convs.put(S57subf.PRIM, new S57conv(1,1,Dom.AN)); convs.put(S57subf.GRUP, new S57conv(3,1,Dom.INT));
		convs.put(S57subf.OBJL, new S57conv(5,2,Dom.INT)); convs.put(S57subf.RVER, new S57conv(3,2,Dom.INT)); convs.put(S57subf.RUIN, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.FIDN, new S57conv(10,4,Dom.INT)); convs.put(S57subf.FIDS, new S57conv(5,2,Dom.INT)); convs.put(S57subf.ATTL, new S57conv(5,2,Dom.INT));
		convs.put(S57subf.ATVL, new S57conv(0,0,Dom.GT)); convs.put(S57subf.FFUI, new S57conv(1,1,Dom.AN)); convs.put(S57subf.FFIX, new S57conv(0,2,Dom.INT));
		convs.put(S57subf.NFPT, new S57conv(0,2,Dom.INT)); convs.put(S57subf.LNAM, new S57conv(17,64,Dom.AN)); convs.put(S57subf.RIND, new S57conv(0,1,Dom.AN));
		convs.put(S57subf.FSUI, new S57conv(1,1,Dom.AN)); convs.put(S57subf.FSIX, new S57conv(0,2,Dom.INT)); convs.put(S57subf.NSPT, new S57conv(0,2,Dom.INT));
		convs.put(S57subf.NAME, new S57conv(12,40,Dom.AN)); convs.put(S57subf.ORNT, new S57conv(1,1,Dom.AN)); convs.put(S57subf.USAG, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.MASK, new S57conv(1,1,Dom.AN)); convs.put(S57subf.VPUI, new S57conv(1,1,Dom.AN)); convs.put(S57subf.VPIX, new S57conv(0,2,Dom.INT));
		convs.put(S57subf.NVPT, new S57conv(0,2,Dom.INT)); convs.put(S57subf.TOPI, new S57conv(1,1,Dom.AN)); convs.put(S57subf.CCUI, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.CCIX, new S57conv(0,2,Dom.INT)); convs.put(S57subf.CCNC, new S57conv(0,2,Dom.INT)); convs.put(S57subf.YCOO, new S57conv(0,-4,Dom.REAL));
		convs.put(S57subf.XCOO, new S57conv(0,-4,Dom.REAL)); convs.put(S57subf.VE3D, new S57conv(0,-4,Dom.REAL)); convs.put(S57subf.ATYP, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.SURF, new S57conv(1,1,Dom.AN)); convs.put(S57subf.ORDR, new S57conv(1,1,Dom.INT)); convs.put(S57subf.RESO, new S57conv(0,4,Dom.REAL));
		convs.put(S57subf.STPT, new S57conv(0,0,Dom.CL)); convs.put(S57subf.CTPT, new S57conv(0,0,Dom.CL)); convs.put(S57subf.ENPT, new S57conv(0,0,Dom.CL));
		convs.put(S57subf.CDPM, new S57conv(0,0,Dom.CL)); convs.put(S57subf.CDPR, new S57conv(0,0,Dom.CL));
	}
	
	public enum S57field { I8RI, DSID, DSSI, DSPM, DSPR, DSRC, DSHT, DSAC, CATD, CATX, DDDF, DDDR, DDDI, DDOM, DDRF, DDSI, DDSC,
		FRID, FOID, ATTF, NATF, FFPC, FFPT, FSPC, FSPT, VRID, ATTV, VRPC, VRPT, SGCC, SG2D, SG3D, ARCC, AR2D, EL2D, CT2D }
	
	private static ArrayList<S57subf> S57i8ri = new ArrayList<S57subf>(Arrays.asList(S57subf.I8RN));
	private static ArrayList<S57subf> S57dsid = new ArrayList<S57subf>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.EXPP, S57subf.INTU, S57subf.DSNM, S57subf.EDTN, S57subf.UPDN,
			S57subf.UADT, S57subf.ISDT, S57subf.STED, S57subf.PRSP, S57subf.PSDN, S57subf.PRED, S57subf.PROF, S57subf.AGEN, S57subf.COMT));
	private static ArrayList<S57subf> S57dssi = new ArrayList<S57subf>(Arrays.asList(S57subf.DSTR, S57subf.AALL, S57subf.NALL, S57subf.NOMR, S57subf.NOCR, S57subf.NOGR, S57subf.NOLR,
			S57subf.NOIN, S57subf.NOCN, S57subf.NOED, S57subf.NOFA ));
	private static ArrayList<S57subf> S57dspm = new ArrayList<S57subf>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.HDAT, S57subf.VDAT, S57subf.SDAT, S57subf.CSCL, S57subf.DUNI,
			S57subf.HUNI, S57subf.PUNI, S57subf.COUN, S57subf.COMF, S57subf.SOMF, S57subf.COMT ));
	private static ArrayList<S57subf> S57dspr = new ArrayList<S57subf>(Arrays.asList(S57subf.PROJ, S57subf.PRP1, S57subf.PRP2, S57subf.PRP3, S57subf.PRP4, S57subf.FEAS, S57subf.FNOR,
			S57subf.FPMF, S57subf.COMT ));
	private static ArrayList<S57subf> S57dsrc = new ArrayList<S57subf>(Arrays.asList(S57subf.RPID, S57subf.RYCO, S57subf.RXCO, S57subf.CURP, S57subf.FPMF, S57subf.RXVL, S57subf.RYVL,
			S57subf.COMT ));
	private static ArrayList<S57subf> S57dsht = new ArrayList<S57subf>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.PRCO, S57subf.ESDT, S57subf.LSDT, S57subf.DCRT, S57subf.CODT, S57subf.COMT ));
	private static ArrayList<S57subf> S57dsac = new ArrayList<S57subf>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.PACC, S57subf.HACC, S57subf.SACC, S57subf.FPMF, S57subf.COMT ));
	private static ArrayList<S57subf> S57catd = new ArrayList<S57subf>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.FILE, S57subf.LFIL, S57subf.VOLM, S57subf.IMPL, S57subf.SLAT,
			S57subf.WLON, S57subf.NLAT, S57subf.ELON, S57subf.CRCS, S57subf.COMT ));
	private static ArrayList<S57subf> S57catx = new ArrayList<S57subf>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.NAM1, S57subf.NAM2, S57subf.COMT ));
	private static ArrayList<S57subf> S57dddf = new ArrayList<S57subf>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.OORA, S57subf.OAAC, S57subf.OACO, S57subf.OALL, S57subf.OATY,
			S57subf.DEFN, S57subf.AUTH, S57subf.COMT ));
	private static ArrayList<S57subf> S57dddr = new ArrayList<S57subf>(Arrays.asList(S57subf.RFTP, S57subf.RFVL ));
	private static ArrayList<S57subf> S57dddi = new ArrayList<S57subf>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.ATLB, S57subf.ATDO, S57subf.ADMU, S57subf.ADFT, S57subf.AUTH, S57subf.COMT ));
	private static ArrayList<S57subf> S57ddom = new ArrayList<S57subf>(Arrays.asList(S57subf.RAVA, S57subf.DVAL, S57subf.DVSD, S57subf.DEFN, S57subf.AUTH ));
	private static ArrayList<S57subf> S57ddrf = new ArrayList<S57subf>(Arrays.asList(S57subf.RFTP, S57subf.RFVL ));
	private static ArrayList<S57subf> S57ddsi = new ArrayList<S57subf>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.OBLB ));
	private static ArrayList<S57subf> S57ddsc = new ArrayList<S57subf>(Arrays.asList(S57subf.ATLB, S57subf.ASET, S57subf.AUTH ));
	private static ArrayList<S57subf> S57frid = new ArrayList<S57subf>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.PRIM, S57subf.GRUP, S57subf.OBJL, S57subf.RVER, S57subf.RUIN ));
	private static ArrayList<S57subf> S57foid = new ArrayList<S57subf>(Arrays.asList(S57subf.AGEN, S57subf.FIDN, S57subf.FIDS ));
	private static ArrayList<S57subf> S57attf = new ArrayList<S57subf>(Arrays.asList(S57subf.ATTL, S57subf.ATVL ));
	private static ArrayList<S57subf> S57natf = new ArrayList<S57subf>(Arrays.asList(S57subf.ATTL, S57subf.ATVL ));
	private static ArrayList<S57subf> S57ffpc = new ArrayList<S57subf>(Arrays.asList(S57subf.FFUI, S57subf.FFIX, S57subf.NFPT ));
	private static ArrayList<S57subf> S57ffpt = new ArrayList<S57subf>(Arrays.asList(S57subf.LNAM, S57subf.RIND, S57subf.COMT ));
	private static ArrayList<S57subf> S57fspc = new ArrayList<S57subf>(Arrays.asList(S57subf.FSUI, S57subf.FSIX, S57subf.NSPT ));
	private static ArrayList<S57subf> S57fspt = new ArrayList<S57subf>(Arrays.asList(S57subf.NAME, S57subf.ORNT, S57subf.USAG, S57subf.MASK ));
	private static ArrayList<S57subf> S57vrid = new ArrayList<S57subf>(Arrays.asList(S57subf.RCNM, S57subf.RCID, S57subf.RVER, S57subf.RUIN ));
	private static ArrayList<S57subf> S57attv = new ArrayList<S57subf>(Arrays.asList(S57subf.ATTL, S57subf.ATVL ));
	private static ArrayList<S57subf> S57vrpc = new ArrayList<S57subf>(Arrays.asList(S57subf.VPUI, S57subf.VPIX, S57subf.NVPT ));
	private static ArrayList<S57subf> S57vrpt = new ArrayList<S57subf>(Arrays.asList(S57subf.NAME, S57subf.ORNT, S57subf.USAG, S57subf.TOPI, S57subf.MASK ));
	private static ArrayList<S57subf> S57sgcc = new ArrayList<S57subf>(Arrays.asList(S57subf.CCUI, S57subf.CCIX, S57subf.CCNC ));
	private static ArrayList<S57subf> S57sg2d = new ArrayList<S57subf>(Arrays.asList(S57subf.YCOO, S57subf.XCOO ));
	private static ArrayList<S57subf> S57sg3d = new ArrayList<S57subf>(Arrays.asList(S57subf.YCOO, S57subf.XCOO, S57subf.VE3D ));
	private static ArrayList<S57subf> S57arcc = new ArrayList<S57subf>(Arrays.asList(S57subf.ATYP, S57subf.SURF, S57subf.ORDR, S57subf.RESO, S57subf.FPMF ));
	private static ArrayList<S57subf> S57ar2d = new ArrayList<S57subf>(Arrays.asList(S57subf.STPT, S57subf.CTPT, S57subf.ENPT, S57subf.YCOO, S57subf.XCOO ));
	private static ArrayList<S57subf> S57el2d = new ArrayList<S57subf>(Arrays.asList(S57subf.STPT, S57subf.CTPT, S57subf.ENPT, S57subf.CDPM, S57subf.CDPR, S57subf.YCOO, S57subf.XCOO ));
	private static ArrayList<S57subf> S57ct2d = new ArrayList<S57subf>(Arrays.asList(S57subf.YCOO, S57subf.XCOO ));
	
	private static final EnumMap<S57field, ArrayList<S57subf>> fields = new EnumMap<S57field, ArrayList<S57subf>>(S57field.class);
	static {
		fields.put(S57field.I8RI, S57i8ri);
		fields.put(S57field.DSID, S57dsid); fields.put(S57field.DSSI, S57dssi); fields.put(S57field.DSPM, S57dspm); fields.put(S57field.DSPR, S57dspr);
		fields.put(S57field.DSRC, S57dsrc); fields.put(S57field.DSHT, S57dsht); fields.put(S57field.DSAC, S57dsac); fields.put(S57field.CATD, S57catd);
		fields.put(S57field.CATX, S57catx); fields.put(S57field.DDDF, S57dddf); fields.put(S57field.DDDR, S57dddr); fields.put(S57field.DDDI, S57dddi);
		fields.put(S57field.DDOM, S57ddom); fields.put(S57field.DDRF, S57ddrf); fields.put(S57field.DDSI, S57ddsi); fields.put(S57field.DDSC, S57ddsc);
		fields.put(S57field.FRID, S57frid); fields.put(S57field.FOID, S57foid); fields.put(S57field.ATTF, S57attf); fields.put(S57field.NATF, S57natf);
		fields.put(S57field.FFPC, S57ffpc); fields.put(S57field.FFPT, S57ffpt); fields.put(S57field.FFPC, S57fspc); fields.put(S57field.FSPT, S57fspt);
		fields.put(S57field.VRID, S57vrid); fields.put(S57field.ATTV, S57attv); fields.put(S57field.VRPC, S57vrpc); fields.put(S57field.VRPT, S57vrpt);
		fields.put(S57field.SGCC, S57sgcc); fields.put(S57field.SG2D, S57sg2d); fields.put(S57field.SG3D, S57sg3d); fields.put(S57field.ARCC, S57arcc);
		fields.put(S57field.AR2D, S57ar2d); fields.put(S57field.EL2D, S57el2d); fields.put(S57field.CT2D, S57ct2d); 
	}

	private static byte[] buffer;
	private static int offset;
	private static int maxoff;
	private static int index;
	private static S57field field;
	
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
	
	public static Object getSubf(byte[] buf, int off, S57field fld, S57subf subf) {
		buffer = buf;
		offset = off;
		index = 0;
		return getSubf(fld, subf);
	}
	
	public static Object getSubf(S57field fld, S57subf subf) {
		field = fld;
		index = 0;
		return getSubf(subf);
	}

	public static Object getSubf(S57subf subf) {
		ArrayList<S57subf> subs = fields.get(field);
		boolean wrap = false;
		while (true) {
			if (index == subs.size()) {
				if (!wrap) {
					index = 0;
					wrap = true;
				} else {
					System.out.println("ERROR: Subfield not found " + subf.name() + " in " + field.name() + " in record " + Js57toosm.rnum);
					System.exit(-1);
				}
			}
			S57subf sub = subs.get(index++);
			S57conv conv = convs.get(sub);
			if (sub == subf) {
				if (conv.bin == 0) {
					String str = "";
					if (conv.asc == 0) {
						while (buffer[offset] != 0x1f) {
							str += buffer[offset++];
						}
					} else {
						str = new String(buffer, offset, conv.asc);
						offset += conv.asc;
					}
					return str;
				} else {
					int f = Math.abs(conv.bin);
					if (f < 8) {
						long val = buffer[offset + --f];
						if (conv.bin > 0)
							val &= 0xff;
						while (f > 0) {
							val = (val << 8) + (buffer[offset + --f] & 0xff);
						}
						offset += Math.abs(conv.bin);
						return val;
					} else {
						f /= 8;
						long val = 0;
						for (int i = 0; i < f; i++) {
							val = (val << 8) + (buffer[offset++] & 0xff);
						}
						return val;
					}
				}
			} else {
				offset += (conv.bin != 0) ? ((conv.bin < 8) ? Math.abs(conv.bin) : conv.bin / 8) : conv.asc;
			}
		}
	}

}
