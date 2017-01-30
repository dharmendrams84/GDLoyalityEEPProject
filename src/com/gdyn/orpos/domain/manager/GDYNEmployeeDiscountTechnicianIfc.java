package com.gdyn.orpos.domain.manager;

import com.gdyn.co.employeediscount.response.GDYNEmployeeDiscResponseObject;

public interface GDYNEmployeeDiscountTechnicianIfc {

	  /* Technician Type
	     */
	    public static final String TYPE = "GDYNEmployeeDiscountTechnician";
	    
	    public GDYNEmployeeDiscResponseObject[] getMaxSpendLimit(String employeeNumber);

}
