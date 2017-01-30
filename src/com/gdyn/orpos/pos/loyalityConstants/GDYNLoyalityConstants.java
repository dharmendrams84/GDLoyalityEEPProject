package com.gdyn.orpos.pos.loyalityConstants;

import java.math.BigDecimal;

public class GDYNLoyalityConstants {

	 public static Boolean isLoyalityCpnExists = Boolean.FALSE;
	 
	 /*new boolean value added by Dharmendra to recalculate discount when multiple coupons applied to a transaction*/ 
	 public static Boolean recalculateMultipleCpnDiscount = Boolean.FALSE;
	 public static Boolean recalculateTransactionDiscount = Boolean.FALSE;
		
	 public static  BigDecimal minThreshHoldAmt = BigDecimal.ZERO;
	 
	 public static String itemApplyToC = "C";
	 
	 public static String itemApplyToR = "R";
	 
	 public static String itemApplyToB = "B";
	 
	 public static String blankString = "";
	 
	 
	 
	 //new method added by Dharmendra to check whether string is empty
	 public static Boolean isEmpty(String str){
		 if(str==null || str.length()==0){
			 return true;
		 }else{
			 return false;
		 }
	 }
	// PSO-379 new method added by Ashwini to get formatted phoneno 
	 public static String getFormattedPhoneNo(String str) {

			str = str.replaceAll("\\(", "");
			str = str.replaceAll("\\)", "");
			str = str.replaceAll(" ", "-");
			str = str.replaceAll("-", "");
			return str;
		}
}
