package com.gdyn.orpos.pos.loyalityConstants;

public interface GDYNLoyalityQueries {

	 public static String merch_hrcy_query = "SELECT * FROM (SELECT SCL.ID_MRHRC_GP_CHLD ID_MRHRC_GP ,trim (leading '0' FROM SUBSTR(DV.ID_MRHRC_GP_CHLD,3)) div , TRIM(LEADING ' ' FROM TRIM(LEADING '*' FROM HGP1.NM_MRHRC_GP)) DivDesc ,"+
	 			"trim (leading '0' FROM SUBSTR(GP.ID_MRHRC_GP_CHLD,3)) grp , TRIM(LEADING ' ' FROM TRIM(LEADING '*'FROM HGP2.NM_MRHRC_GP)) GrpDesc ,"+
	 			"trim (leading '0' FROM SUBSTR(DP.ID_MRHRC_GP_CHLD,3)) dpt , TRIM(LEADING ' ' FROM TRIM(LEADING '*'FROM HGP3.NM_MRHRC_GP)) DptDesc ,"+
	 			"trim (Leading '0' FROM SUBSTR(CL.ID_MRHRC_GP_CHLD, LENGTH(DP.ID_MRHRC_GP_CHLD)+1)) cls , TRIM(LEADING ' ' FROM TRIM(LEADING '*' FROM HGP4.NM_MRHRC_GP)) ClsDesc ,"+
	 			"lpad(trim (Leading '0' FROM SUBSTR(SCL.ID_MRHRC_GP_CHLD, LENGTH(CL.ID_MRHRC_GP_CHLD)+1)), 3, '0') scl ,TRIM(LEADING ' ' FROM TRIM(LEADING '*' FROM HGP5.NM_MRHRC_GP)) SCLDesc "+
	 			"FROM ST_ASCTN_MRHRC SCL INNER JOIN CO_MRHRC_GP HGP5 ON SCL.ID_MRHRC_GP_CHLD=HGP5.ID_MRHRC_GP "+
	 			" INNER JOIN ST_ASCTN_MRHRC CL ON SCL.ID_MRHRC_GP_PRNT = CL.ID_MRHRC_GP_CHLD AND SCL.ID_MRHRC_FNC =CL.ID_MRHRC_FNC AND SCL.ID_MRHRC_LV =CL.ID_MRHRC_LV+1 "+
	 			"INNER JOIN CO_MRHRC_GP HGP4 ON CL.ID_MRHRC_GP_CHLD=HGP4.ID_MRHRC_GP "+
	 			" INNER JOIN ST_ASCTN_MRHRC DP ON CL.ID_MRHRC_GP_PRNT = DP.ID_MRHRC_GP_CHLD AND CL.ID_MRHRC_FNC =DP.ID_MRHRC_FNC AND CL.ID_MRHRC_LV =DP.ID_MRHRC_LV+1 "+
	 			"INNER JOIN CO_MRHRC_GP HGP3 ON DP.ID_MRHRC_GP_CHLD=HGP3.ID_MRHRC_GP INNER JOIN ST_ASCTN_MRHRC GP ON DP.ID_MRHRC_GP_PRNT = GP.ID_MRHRC_GP_CHLD "+
	 			" AND DP.ID_MRHRC_FNC =GP.ID_MRHRC_FNC AND DP.ID_MRHRC_LV =GP.ID_MRHRC_LV+1 INNER JOIN CO_MRHRC_GP HGP2 ON GP.ID_MRHRC_GP_CHLD=HGP2.ID_MRHRC_GP "+
	 			" INNER JOIN ST_ASCTN_MRHRC DV ON GP.ID_MRHRC_GP_PRNT = DV.ID_MRHRC_GP_CHLD AND GP.ID_MRHRC_FNC =DV.ID_MRHRC_FNC AND GP.ID_MRHRC_LV =DV.ID_MRHRC_LV+1 "+
	 			"INNER JOIN CO_MRHRC_GP HGP1 ON DV.ID_MRHRC_GP_CHLD=HGP1.ID_MRHRC_GP WHERE 1 =1 AND SCL.ID_MRHRC_LV =5 )"+
	 			"WHERE id_mrhrc_gp = ";
}
