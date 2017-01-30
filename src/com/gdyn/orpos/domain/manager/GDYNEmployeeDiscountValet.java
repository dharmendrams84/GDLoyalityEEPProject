package com.gdyn.orpos.domain.manager;

import java.io.Serializable;

import oracle.retail.stores.foundation.tour.manager.ValetIfc;

import org.apache.log4j.Logger;

import com.gdyn.co.employeediscount.response.GDYNEmployeeDiscResponseObject;



public class GDYNEmployeeDiscountValet implements ValetIfc {

	private static final long serialVersionUID = -2508722685309124444L;
	
	protected static Logger logger = Logger
			.getLogger("com.gdyn.orpos.domain.manager.printing.GDYNEmployeeDiscountValet");

	
	private String employeeNumber = null;
	

	
	public GDYNEmployeeDiscountValet(String employeeNumber) {
		
		this.employeeNumber = employeeNumber;
			}

	
	@Override
	public Serializable execute(Object technician) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("Requesting max spend limit of employee GDYNEmployeeValet  for employee "+employeeNumber);
		GDYNEmployeeDiscountTechnician gdt = new GDYNEmployeeDiscountTechnician(); 
	GDYNEmployeeDiscResponseObject[] resp = ((GDYNEmployeeDiscountTechnician) technician).getMaxSpendLimit(employeeNumber);
		
		return resp;
	}

	
	/**
	 * @param SendMessage
	 */
	/*public void setRequest(String operatorName, String employeeId,
			BigDecimal denomination) {
		this.oprName = operatorName;
		this.employeeId = employeeId;
		this.maxSpendLimit = denomination;
	}*/

	// ----------------------------------------------------------------------
	/**
	 * Method to default display string function.
	 * <p>
	 * 
	 * @return String representation of object
	 **/
	// ----------------------------------------------------------------------
	public String toString() {
		return "Class:  " + getClass().getName() + " Revision " + ")"
				+ hashCode();
	}
}
