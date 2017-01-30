package oracle.retail.stores.domain.stock;

import java.io.Serializable;
import java.util.Date;

public class GDYNLoyalityCpnHrchyDtls implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	protected String loylCpnId ;	
	
	protected String discountDivision;
	
	protected String discountGroup ;

	protected String discountDept ;
	
	protected String discountClass ;
	
	protected String discountSubclass ;
	
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

	protected Date createdDate ;
	
	protected Date modifiedDate ;

	/**
	 * @return the discountDivision
	 */
	public String getDiscountDivision() {
		return discountDivision;
	}

	/**
	 * @param discountDivision the discountDivision to set
	 */
	public void setDiscountDivision(String discountDivision) {
		this.discountDivision = discountDivision;
	}

	/**
	 * @return the discountGroup
	 */
	public String getDiscountGroup() {
		return discountGroup;
	}

	/**
	 * @param discountGroup the discountGroup to set
	 */
	public void setDiscountGroup(String discountGroup) {
		this.discountGroup = discountGroup;
	}

	/**
	 * @return the discountDept
	 */
	public String getDiscountDept() {
		return discountDept;
	}

	/**
	 * @param discountDept the discountDept to set
	 */
	public void setDiscountDept(String discountDept) {
		this.discountDept = discountDept;
	}

	/**
	 * @return the discountClass
	 */
	public String getDiscountClass() {
		return discountClass;
	}

	/**
	 * @param discountClass the discountClass to set
	 */
	public void setDiscountClass(String discountClass) {
		this.discountClass = discountClass;
	}

	/**
	 * @return the discountSubclass
	 */
	public String getDiscountSubclass() {
		return discountSubclass;
	}

	/**
	 * @param discountSubclass the discountSubclass to set
	 */
	public void setDiscountSubclass(String discountSubclass) {
		this.discountSubclass = discountSubclass;
	}

	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the modifiedDate
	 */
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * @param modifiedDate the modifiedDate to set
	 */
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
}
