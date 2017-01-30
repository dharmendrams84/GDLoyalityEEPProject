package oracle.retail.stores.domain.manager.datareplication;


public class CouponAttribute 
{
	
	String ID="";
	String minThreshold="";
	String couponType="";
	String externalSysValidation="";
	String maxDiscount="";
	public String getMaxDiscount() {
		return maxDiscount;
	}
	public void setMaxDiscount(String maxDiscount) {
		this.maxDiscount = maxDiscount;
	}
	public String getMaxAmount() {
		return maxAmount;
	}
	public void setMaxAmount(String maxAmount) {
		this.maxAmount = maxAmount;
	}
	String maxAmount = "";
	CouponHierarchy couponhierarchys[] = null;
	
	
	public CouponAttribute(String ID, String minThreshold,
			String couponType, String maxAmount, String maxDiscount,String externalSysValidation, CouponHierarchy[] couponhierarchys) 
	{
		this.ID=ID;
		this.minThreshold=minThreshold;
		this.couponType=couponType; 
		this.maxAmount=maxAmount;
		this.maxDiscount= maxDiscount;
		this.externalSysValidation=externalSysValidation;
		this.couponhierarchys=couponhierarchys;
		
	}
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getMinThreshold() {
		return minThreshold;
	}
	public void setMinThreshold(String minThreshold) {
		this.minThreshold = minThreshold;
	}
	public String getCouponType() {
		return couponType;
	}
	public void setCouponType(String couponType) {
		this.couponType = couponType;
	}
	public String getExternalSysValidation() {
		return externalSysValidation;
	}
	public void setExternalSysValidation(String externalSysValidation) {
		this.externalSysValidation = externalSysValidation;
	}
	public CouponHierarchy[] getCouponhierarchys() {
		return couponhierarchys;
	}
	public void setCouponhierarchys(CouponHierarchy[] couponhierarchys) {
		this.couponhierarchys = couponhierarchys;
	}

	
	

}
