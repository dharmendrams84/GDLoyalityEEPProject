package oracle.retail.stores.domain.stock;

import java.io.Serializable;
import java.math.BigDecimal;

public class GDYNLoyalityCpnAttrDtls implements Serializable{

	//static final long serialVersionUID = 7321887861981917531L;
	private static final long serialVersionUID = 1L;

	
	  protected String loylCpnId ;	
	
	  protected String loylCpnType;
	    
	    /**
		 * @return the loylCpnType
		 */
		public String getLoylCpnType() {
			return loylCpnType;
		}

		/**
		 * @return the loylCpnId
		 */
		public String getLoylCpnId() {
			return loylCpnId;
		}

		/**
		 * @param loylCpnId the loylCpnId to set
		 */
		public void setLoylCpnId(String loylCpnId) {
			this.loylCpnId = loylCpnId;
		}

		/**
		 * @param loylCpnType the loylCpnType to set
		 */
		public void setLoylCpnType(String loylCpnType) {
			this.loylCpnType = loylCpnType;
		}

		protected String itmApplyTo;
	    
	    /**
		 * @return the itmApplyTo
		 */
		public String getItmApplyTo() {
			return itmApplyTo;
		}

		/**
		 * @param itmApplyTo the itmApplyTo to set
		 */
		public void setItmApplyTo(String itmApplyTo) {
			this.itmApplyTo = itmApplyTo;
		}

		protected BigDecimal minMonthlyThrhold;
	    
	    /**
		 * @return the minMonthlyThrhold
		 */
		public BigDecimal getMinMonthlyThrhold() {
			return minMonthlyThrhold;
		}

		/**
		 * @param minMonthlyThrhold the minMonthlyThrhold to set
		 */
		public void setMinMonthlyThrhold(BigDecimal minMonthlyThrhold) {
			this.minMonthlyThrhold = minMonthlyThrhold;
		}

		protected BigDecimal maxMonthlyThrhold;
	    
	    /**
		 * @return the maxMonthlyThrhold
		 */
		public BigDecimal getMaxMonthlyThrhold() {
			return maxMonthlyThrhold;
		}

		/**
		 * @param maxMonthlyThrhold the maxMonthlyThrhold to set
		 */
		public void setMaxMonthlyThrhold(BigDecimal maxMonthlyThrhold) {
			this.maxMonthlyThrhold = maxMonthlyThrhold;
		}

		protected BigDecimal maxDiscAmount;
	    
	    /**
		 * @return the maxDiscAmount
		 */
		public BigDecimal getMaxDiscAmount() {
			return maxDiscAmount;
		}

		/**
		 * @param maxDiscAmount the maxDiscAmount to set
		 */
		public void setMaxDiscAmount(BigDecimal maxDiscAmount) {
			this.maxDiscAmount = maxDiscAmount;
		}

		protected String validityFlag;
		
			
		/**
		 * @return the validityFlag
		 */
		public String getValidityFlag() {
			return validityFlag;
		}

		/**
		 * @param validityFlag the validityFlag to set
		 */
		public void setValidityFlag(String validityFlag) {
			this.validityFlag = validityFlag;
		}

		
		
		

}
