package oracle.retail.stores.pos.ui.beans;

import java.util.Vector;

import oracle.retail.stores.domain.DomainGateway;

public class GDYNPhoneNoBeanModel extends POSBaseBeanModel{

	 /**
	 * 
	 */
	//private static final long serialVersionUID = 1L;
	private static final long serialVersionUID = 1529681861137053823L;
	
	 public GDYNPhoneNoBeanModel()
	    {
		 fieldPhoneNo = "";
	    }
	protected String fieldPhoneNo = "";

	/**
	 * @return the fieldPhoneNo
	 */
	public String getFieldPhoneNo() {
		return fieldPhoneNo;
	}

	/**
	 * @param fieldPhoneNo the fieldPhoneNo to set
	 */
	public void setFieldPhoneNo(String fieldPhoneNo) {
		this.fieldPhoneNo = fieldPhoneNo;
	}
}
