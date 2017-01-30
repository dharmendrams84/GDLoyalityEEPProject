//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.pos.services.pricing.employeediscount;

import java.math.BigDecimal;
import java.util.Vector;

import oracle.retail.stores.domain.arts.DataTransactionFactory;
import oracle.retail.stores.domain.arts.DataTransactionKeys;
import oracle.retail.stores.domain.employee.Employee;
import oracle.retail.stores.domain.employee.EmployeeIfc;
import oracle.retail.stores.domain.lineitem.SaleReturnLineItem;
import oracle.retail.stores.domain.tender.TenderLimitsIfc;
import oracle.retail.stores.domain.tender.TenderLineItemIfc;
import oracle.retail.stores.domain.utility.PersonName;
import oracle.retail.stores.domain.utility.PersonNameIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.application.LaneActionAdapter;
import oracle.retail.stores.foundation.tour.application.Letter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.common.CommonLetterIfc;
import oracle.retail.stores.pos.ui.DialogScreensIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DialogBeanModel;

import com.gdyn.co.employeediscount.response.GDYNEmployeeDiscResponseObject;
//import com.gdyn.co.employeediscount.response.GDYNEmployeeDiscResponseObject;
import com.gdyn.orpos.domain.arts.GDYNEmployeeTransaction;
import com.gdyn.orpos.domain.manager.GDYNEmployeeDiscountManager;

//import com.gdyn.orpos.domain.manager.GDYNEmployeeDiscountManager;
import com.gdyn.orpos.pos.services.pricing.GDYNPricingCargo;

/**
 * This aisle reads in the employee number and determines if it is
 * a valid employee in the database.
 * 
 * @author mlawrence
 */
public class GDYNFindEmployeeNumberAisle extends LaneActionAdapter
{
    private static final String NO_DATA_FOUND_RESOURCE = "noDataFoundResource";

	private static final String NO_DATA_FOUND = "NO_DATA_FOUND";

	private static final String EMP_STATUS_ONE = "1";

	private static final String EMPL_NOT_FOUND = "EMPL_NOT_FOUND";

	private static final long serialVersionUID = -9126842000409057365L;

    /** constant for declined dialog name */
    public static final String EMPLOYEE_ID_NOT_FOUND_ERROR_DIALOG = "EmployeeNotFound";
    
    
   

	/**
     * Reads in the employee number from the UI.
     * 
     * @param bus
     *            Service Bus
     */
    public void traverse(BusIfc bus)
 {
		POSUIManagerIfc ui = (POSUIManagerIfc) bus
				.getManager(UIManagerIfc.TYPE);

		GDYNPricingCargo cargo = (GDYNPricingCargo) bus.getCargo();
		String employeeID = cargo.getEmployeeDiscountID();

		GDYNEmployeeDiscountManager empDiscMngrr = new GDYNEmployeeDiscountManager();
		GDYNEmployeeDiscResponseObject[] responseObjects = empDiscMngrr
				.getMaxSpendLimit(cargo.getEmployeeDiscountID());
		
		GDYNEmployeeDiscountUtility.responseObjects = responseObjects;

		// Begin GD-116: Data input/output - Add the employee and a signature
		// line on the receipt.
		// lcatania (Starmount) Feb 21, 2013
		
		if (responseObjects != null
				&& responseObjects.length != 0
				&& responseObjects[0].getCode() != null
				&& (NO_DATA_FOUND
						.equalsIgnoreCase(responseObjects[0].getCode()))) {
			logger.info("No data found for the employee id  " + employeeID
					+ " in entilement tableresponseObjects[0].getCode() "
					+ responseObjects[0].getCode());

			String msg[] = new String[1];
			// initialize model bean
			DialogBeanModel dialogModel = new DialogBeanModel();
			dialogModel.setResourceID(NO_DATA_FOUND_RESOURCE);
			dialogModel.setType(DialogScreensIfc.ERROR);
			dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_OK,
					CommonLetterIfc.FAILURE);

			msg[0] = employeeID;
			//cargo.setEmployeeDiscountID(null);

			dialogModel.setArgs(msg);
			// display dialog
			ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);

		} else if (responseObjects != null
				&& responseObjects.length != 0
				&& responseObjects[0].getEmpStatusCode() != null
				&& (EMP_STATUS_ONE.equalsIgnoreCase(responseObjects[0]
						.getEmpStatusCode()) || EMPL_NOT_FOUND
						.equalsIgnoreCase(responseObjects[0].getEmpStatusCode()))) {
			logger.info("The employee id is not found: " + employeeID
					+ " responseObjects[0].getEmpStatusCode() "
					+ responseObjects[0].getEmpStatusCode());

			String msg[] = new String[1];
			// initialize model bean
			DialogBeanModel dialogModel = new DialogBeanModel();
			dialogModel.setResourceID(EMPLOYEE_ID_NOT_FOUND_ERROR_DIALOG);
			dialogModel.setType(DialogScreensIfc.ERROR);
			dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_OK,
					CommonLetterIfc.FAILURE);

			msg[0] = employeeID;
			cargo.setEmployeeDiscountID(null);
			// cargo.setDiscountEmployee(getEmployeeById(employeeID));
			dialogModel.setArgs(msg);
			// display dialog
			ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);

		}
		// End GD-116: Data input/output - Add the employee and a signature line
		// on the receipt.

		else if (responseObjects != null
				&& responseObjects.length != 0
				&& responseObjects[0].getEmpStatusCode() != null
				&& ("A".equalsIgnoreCase(responseObjects[0].getEmpStatusCode()) || "L"
						.equalsIgnoreCase(responseObjects[0].getEmpStatusCode()))) {

			
			logger.info("Employee  " + cargo.getEmployeeDiscountID()
					+ " is Active ");
			logger.info("responseObjects " + responseObjects[0].getCode()
					+ " : " + responseObjects[0].getEmpStatusCode());

			cargo.getDiscountEmployee().setResponseObject(responseObjects);
			cargo.setEmployeeDiscountID(employeeID);
			
			
			EmployeeIfc discountEmployee = cargo.getDiscountEmployee();
			/*new method  added by Dharmendra to fix issue POS-195 on 12/08/2016*/
			 getDiscountEmployeeFromId(discountEmployee, responseObjects);
			
			logger.info("After setting Employee Discount");
			bus.mail(new Letter(CommonLetterIfc.CONTINUE), BusIfc.CURRENT);
		} else {
			logger.info("else block entered responseObjects for employee  "
					+ employeeID + " is  " + responseObjects);

			if (responseObjects != null && responseObjects.length != 0) {
				logger.info("responseObjects[0].getEmpStatusCode() "
						+ responseObjects[0].getEmpStatusCode() + " : "
						+ responseObjects[0].getCode());
			}

			DialogBeanModel dialogModel = new DialogBeanModel();
			dialogModel.setResourceID("CoIsDown");
			dialogModel.setType(DialogScreensIfc.ERROR);
			dialogModel.setButtonLetter(DialogScreensIfc.BUTTON_OK,
					CommonLetterIfc.UNDO);

			cargo.setEmployeeDiscountID(null);

			ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);

		}
	}
    
    /*new method created by Dhaarmendra to set discount employee full name to fix issue POS-195*/ 
	protected EmployeeIfc getDiscountEmployeeFromId(EmployeeIfc discountEmployee,
			GDYNEmployeeDiscResponseObject[] responseObjects) {
		logger.info("getDiscountEmployeeFromId method entered");
		
		if (responseObjects != null && responseObjects.length != 0) {

			String employeeID = responseObjects[0].getEmployeeNumber();
			discountEmployee.setEmployeeID(employeeID);
			String firstName= "";
			String lastName= "";
			String fullName = "";
			discountEmployee.setFullName(fullName);
			PersonNameIfc personNameIfc = new PersonName();
			if (responseObjects[0].getFirstName() != null
					&& !"".equalsIgnoreCase(responseObjects[0].getFirstName())) {
				firstName = responseObjects[0].getFirstName();
				personNameIfc.setFirstName(firstName);
			}
			if (responseObjects[0].getLastName() != null
					&& !"".equalsIgnoreCase(responseObjects[0].getLastName())) {
				lastName = responseObjects[0].getLastName();
				personNameIfc.setLastName(lastName);
			}
			fullName = firstName+" "+lastName;
			personNameIfc.setFullName(fullName);

			discountEmployee.setPersonName(personNameIfc);
			discountEmployee.setResponseObject(responseObjects);
			
			discountEmployee.setEmployeeID(employeeID);
			discountEmployee.setFullName(fullName);
		}
		
		return discountEmployee;
	}

    // Begin GD-116: Data input/output - Add the employee and a signature line on the receipt.
    // lcatania (Starmount) Feb 21, 2013
    /**
     * Check if the employee id entered is in the database and return it.
     * 
     * EmployeeIfc
     * 
     * @param employeeID
     * @return
     */
    protected EmployeeIfc getEmployeeById(String employeeID)
    {
    	
        EmployeeIfc employee = null;
        GDYNEmployeeTransaction empTransaction = (GDYNEmployeeTransaction) DataTransactionFactory.create(DataTransactionKeys.EMPLOYEE_TRANSACTION);
        try
        {
            employee = empTransaction.lookupEmployeeSkipRole(employeeID);
        }
        catch (DataException de)
        {
            // log the error; set the error code in the cargo for future use.
            logger.error(
                    "EmployeeID '" + employeeID + "' error: " + de.getMessage() + "\n\t Error code = " +
                            Integer.toString(de.getErrorCode()) + "");
        }
        return employee;
    }
    // End GD-116: Data input/output - Add the employee and a signature line on the receipt.
}
