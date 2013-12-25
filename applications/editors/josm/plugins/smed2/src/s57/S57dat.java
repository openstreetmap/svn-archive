package s57;

public class S57dat {
	
	private enum S57field { DSID, DSSI, DSPM, DSPR, DSRC, DSHT, DSAC, CATD, CATX, DDDF, DDDR, DDDI, DDOM, DDRF, DDSI, DDSC,
		FRID, FOID, ATTF, NATF, FFPC, FFPT, FSPC, FSPT, VRID, ATTV, VRPC, VRPT, SGCC, SG2D, SG3D, ARCC, AR2D, EL2D, CT2D }
	private enum S57dsid { RCNM, RCID, EXPP, INTU, DSNM, EDTN, UPDN, UADT, ISDT, STED, PRSP, PSDN, PRED, PROF, AGEN, COMT }
	private enum S57dssi { DSTR, AALL, NALL, NOMR, NOCR, NOGR, NOLR, NOIN, NOCN, NOED, NOFA }
	private enum S57dspm { RCNM, RCID, HDAT, VDAT, SDAT, CSCL, DUNO, HUNI, PUNI, COUN, COMF, SOMF, COMT }
	private enum S57dspr { PROJ, PRP1, PRP2, PRP3, PRP4, FEAS, FNOR, FPMF, COMT }
	private enum S57dsrc { RPID, RYCO, RXCO, CURP, FPMF, RXVL, RYVL, COMT }
	private enum S57dsht { RCNM, RCID, PRCO, ESDT, LSDT, DCRT, CODT, COMT }
	private enum S57dsac { RCNM, RCID, PACC, HACC, SACC, FPMF, COMT }
	private enum S57catd { RCNM, RCID, FILE, LFIL, VOLM, IMPL, SLAT, WLON, NLAT, ELON, CRCS, COMT }
	private enum S57catx { RCNM, RCID, NAM1, NAM2, COMT }
	private enum S57dddf { RCNM, RCID, OORA, OAAC, OACO, OALL, OATY, DEFN, AUTH, COMT }
	private enum S57dddr { RFTP, RFVL }
	private enum S57dddi { RCNM, RCID, ATLB, ATDO, ADMU, ADFT, AUTH, COMT }
	private enum S57ddom { RAVA, DVAL, DVSD, DEFN, AUTH }
	private enum S57ddrf { RFTP, RFVL }
	private enum S57ddsi { RCNM, RCID, OBLB }
	private enum S57ddsc { ATLB, ASET, AUTH }
	private enum S57frid { RCNM, RCID, PRIM, GRUP, OBJL, RVER, RUIN }
	private enum S57foid { AGEN, FIDN, FIDS }
	private enum S57attf { ATTL, ATVL }
	private enum S57natf { ATTL, ATVL }
	private enum S57ffpc { FFUI, FFIX, NFPT }
	private enum S57ffpt { LNAM, RIND, COMT }
	private enum S57fspc { FSUI, FSIX, NSPT }
	private enum S57fspt { NAME, ORNT, USAG, MASK }
	private enum S57vrid { RCNM, RCID, RVER, RUIN }
	private enum S57attv { ATTL, ATVL }
	private enum S57vrpc { VPUI, VPIX, NVPT }
	private enum S57vrpt { NAME, ORNT, USAG, TOPI, MASK }
	private enum S57sgcc { CCUI, CCIX, CCNC }
	private enum S57sg2d { YCOO, XCOO }
	private enum S57sg3d { YCOO, XCOO, VE3D }
	private enum S57arcc { ATYP, SURF, ORDR, RESO, FPMF }
	private enum S57ar2d { STPT, CTPT, ENPT, YCOO, XCOO }
	private enum S57el2d { STPT, CTPT, ENPT, CDPM, CDPR, YCOO, XCOO }
	private enum S57ct2d { YCOO, XCOO }
	
}
