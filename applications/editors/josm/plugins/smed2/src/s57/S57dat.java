package s57;

import java.util.EnumMap;

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
	
	public enum S57field { DSID, DSSI, DSPM, DSPR, DSRC, DSHT, DSAC, CATD, CATX, DDDF, DDDR, DDDI, DDOM, DDRF, DDSI, DDSC,
		FRID, FOID, ATTF, NATF, FFPC, FFPT, FSPC, FSPT, VRID, ATTV, VRPC, VRPT, SGCC, SG2D, SG3D, ARCC, AR2D, EL2D, CT2D }
	public enum S57dsid { RCNM, RCID, EXPP, INTU, DSNM, EDTN, UPDN, UADT, ISDT, STED, PRSP, PSDN, PRED, PROF, AGEN, COMT }
	public enum S57dssi { DSTR, AALL, NALL, NOMR, NOCR, NOGR, NOLR, NOIN, NOCN, NOED, NOFA }
	public enum S57dspm { RCNM, RCID, HDAT, VDAT, SDAT, CSCL, DUNO, HUNI, PUNI, COUN, COMF, SOMF, COMT }
	public enum S57dspr { PROJ, PRP1, PRP2, PRP3, PRP4, FEAS, FNOR, FPMF, COMT }
	public enum S57dsrc { RPID, RYCO, RXCO, CURP, FPMF, RXVL, RYVL, COMT }
	public enum S57dsht { RCNM, RCID, PRCO, ESDT, LSDT, DCRT, CODT, COMT }
	public enum S57dsac { RCNM, RCID, PACC, HACC, SACC, FPMF, COMT }
	public enum S57catd { RCNM, RCID, FILE, LFIL, VOLM, IMPL, SLAT, WLON, NLAT, ELON, CRCS, COMT }
	public enum S57catx { RCNM, RCID, NAM1, NAM2, COMT }
	public enum S57dddf { RCNM, RCID, OORA, OAAC, OACO, OALL, OATY, DEFN, AUTH, COMT }
	public enum S57dddr { RFTP, RFVL }
	public enum S57dddi { RCNM, RCID, ATLB, ATDO, ADMU, ADFT, AUTH, COMT }
	public enum S57ddom { RAVA, DVAL, DVSD, DEFN, AUTH }
	public enum S57ddrf { RFTP, RFVL }
	public enum S57ddsi { RCNM, RCID, OBLB }
	public enum S57ddsc { ATLB, ASET, AUTH }
	public enum S57frid { RCNM, RCID, PRIM, GRUP, OBJL, RVER, RUIN }
	public enum S57foid { AGEN, FIDN, FIDS }
	public enum S57attf { ATTL, ATVL }
	public enum S57natf { ATTL, ATVL }
	public enum S57ffpc { FFUI, FFIX, NFPT }
	public enum S57ffpt { LNAM, RIND, COMT }
	public enum S57fspc { FSUI, FSIX, NSPT }
	public enum S57fspt { NAME, ORNT, USAG, MASK }
	public enum S57vrid { RCNM, RCID, RVER, RUIN }
	public enum S57attv { ATTL, ATVL }
	public enum S57vrpc { VPUI, VPIX, NVPT }
	public enum S57vrpt { NAME, ORNT, USAG, TOPI, MASK }
	public enum S57sgcc { CCUI, CCIX, CCNC }
	public enum S57sg2d { YCOO, XCOO }
	public enum S57sg3d { YCOO, XCOO, VE3D }
	public enum S57arcc { ATYP, SURF, ORDR, RESO, FPMF }
	public enum S57ar2d { STPT, CTPT, ENPT, YCOO, XCOO }
	public enum S57el2d { STPT, CTPT, ENPT, CDPM, CDPR, YCOO, XCOO }
	public enum S57ct2d { YCOO, XCOO }
	
	public enum S57subf { RCNM, RCID, EXPP, INTU, DSNM, EDTN, UPDN, UADT, ISDT, STED, PRSP, PSDN, PRED, PROF, AGEN, COMT, DSTR, AALL, NALL,
		NOMR, NOCR, NOGR, NOLR, NOIN, NOCN, NOED, NOFA, HDAT, VDAT, SDAT, CSCL, DUNI, HUNI, PUNI, COUN, COMF, SOMF, PROJ, PRP1, PRP2, PRP3, PRP4,
		FEAS, FNOR, FPMF, RPID, RYCO, RXCO, CURP, RXVL, RYVL, PRCO, ESDT, LSDT, DCRT, CODT, PACC, HACC, SACC, FILE, LFIL, VOLM, IMPL, SLAT, WLON, NLAT, ELON,
		CRCS, NAM1, NAM2, OORA, OAAC, OACO, OALL, OATY, DEFN, AUTH, RFTP, RFVL, ATLB, ATDO, ADMU, ADFT, RAVA, DVAL, DVSD, OBLB, ASET, PRIM, GRUP, RVER, RUIN,
		FIDN, FIDS, ATTL, ATVL, FFUI, FFIX, NFPT, LNAM, RIND, FSUI, FSIX, NSPT, NAME, ORNT, USAG, MASK, VPUI, VPIX, NVPT, TOPI, CCUI, CCIX, CCNC, YCOO, XCOO,
		VE3D, ATYP, SURF, ORDR, RESO, STPT, CTPT, ENPT, CDPM, CDPR }

	private static final EnumMap<S57subf, S57conv> convs = new EnumMap<S57subf, S57conv>(S57subf.class);
	static {
		convs.put(S57subf.RCNM, new S57conv(2,1,Dom.AN)); convs.put(S57subf.RCID, new S57conv(10,4,Dom.INT)); convs.put(S57subf.EXPP, new S57conv(1,1,Dom.AN));
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
		convs.put(S57subf.ELON, new S57conv(0,0,Dom.REAL)); convs.put(S57subf.CRCS, new S57conv(0,0,Dom.HEX)); convs.put(S57subf.NAM1, new S57conv(12,5,Dom.AN));
		convs.put(S57subf.NAM2, new S57conv(12,5,Dom.AN)); convs.put(S57subf.OORA, new S57conv(1,1,Dom.AN)); convs.put(S57subf.OAAC, new S57conv(6,0,Dom.BT));
		convs.put(S57subf.OACO, new S57conv(5,2,Dom.INT)); convs.put(S57subf.OALL, new S57conv(0,0,Dom.BT)); convs.put(S57subf.OATY, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.DEFN, new S57conv(0,0,Dom.BT)); convs.put(S57subf.AUTH, new S57conv(2,2,Dom.AN)); convs.put(S57subf.RFTP, new S57conv(2,1,Dom.AN));
		convs.put(S57subf.RFVL, new S57conv(0,0,Dom.BT)); convs.put(S57subf.ATLB, new S57conv(5,2,Dom.INT)); convs.put(S57subf.ATDO, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.ADMU, new S57conv(0,0,Dom.BT)); convs.put(S57subf.ADFT, new S57conv(0,0,Dom.BT)); convs.put(S57subf.RAVA, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.DVAL, new S57conv(0,0,Dom.BT)); convs.put(S57subf.DVSD, new S57conv(0,0,Dom.BT)); convs.put(S57subf.OBLB, new S57conv(5,2,Dom.INT));
		convs.put(S57subf.ASET, new S57conv(1,1,Dom.AN)); convs.put(S57subf.PRIM, new S57conv(1,1,Dom.AN)); convs.put(S57subf.GRUP, new S57conv(3,1,Dom.INT));
		convs.put(S57subf.RVER, new S57conv(3,2,Dom.INT)); convs.put(S57subf.RUIN, new S57conv(1,1,Dom.AN)); convs.put(S57subf.FIDN, new S57conv(10,4,Dom.INT));
		convs.put(S57subf.FIDS, new S57conv(5,2,Dom.INT)); convs.put(S57subf.ATTL, new S57conv(5,2,Dom.INT)); convs.put(S57subf.ATVL, new S57conv(0,0,Dom.GT));
		convs.put(S57subf.FFUI, new S57conv(1,1,Dom.AN)); convs.put(S57subf.FFIX, new S57conv(0,2,Dom.INT)); convs.put(S57subf.NFPT, new S57conv(0,2,Dom.INT));
		convs.put(S57subf.LNAM, new S57conv(17,8,Dom.AN)); convs.put(S57subf.RIND, new S57conv(0,1,Dom.AN)); convs.put(S57subf.FSUI, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.FSIX, new S57conv(0,2,Dom.INT)); convs.put(S57subf.NSPT, new S57conv(0,2,Dom.INT)); convs.put(S57subf.NAME, new S57conv(12,5,Dom.AN));
		convs.put(S57subf.ORNT, new S57conv(1,1,Dom.AN)); convs.put(S57subf.USAG, new S57conv(1,1,Dom.AN)); convs.put(S57subf.MASK, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.VPUI, new S57conv(1,1,Dom.AN)); convs.put(S57subf.VPIX, new S57conv(0,2,Dom.INT)); convs.put(S57subf.NVPT, new S57conv(0,2,Dom.INT));
		convs.put(S57subf.TOPI, new S57conv(1,1,Dom.AN)); convs.put(S57subf.CCUI, new S57conv(1,1,Dom.AN)); convs.put(S57subf.CCIX, new S57conv(0,2,Dom.INT));
		convs.put(S57subf.CCNC, new S57conv(0,2,Dom.INT)); convs.put(S57subf.YCOO, new S57conv(0,-4,Dom.REAL)); convs.put(S57subf.XCOO, new S57conv(0,-4,Dom.REAL));
		convs.put(S57subf.VE3D, new S57conv(0,-4,Dom.REAL)); convs.put(S57subf.ATYP, new S57conv(1,1,Dom.AN)); convs.put(S57subf.SURF, new S57conv(1,1,Dom.AN));
		convs.put(S57subf.ORDR, new S57conv(1,1,Dom.INT)); convs.put(S57subf.RESO, new S57conv(0,4,Dom.REAL)); convs.put(S57subf.STPT, new S57conv(0,0,Dom.CL));
		convs.put(S57subf.CTPT, new S57conv(0,0,Dom.CL)); convs.put(S57subf.ENPT, new S57conv(0,0,Dom.CL)); convs.put(S57subf.CDPM, new S57conv(0,0,Dom.CL));
		convs.put(S57subf.CDPR, new S57conv(0,0,Dom.CL));
	}

}
