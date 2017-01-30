package com.gdyn.orpos.domain.manager;

import java.math.BigDecimal;

import oracle.retail.stores.foundation.comm.CommException;
import oracle.retail.stores.foundation.tour.gate.ValetException;
import oracle.retail.stores.foundation.tour.manager.Manager;

import org.apache.log4j.Logger;

import com.gdyn.co.employeediscount.response.GDYNEmployeeDiscResponseObject;

public class GDYNEmployeeDiscountManager extends Manager{

	protected static Logger logger = Logger
			.getLogger(com.gdyn.orpos.domain.manager.GDYNEmployeeDiscountManager.class);
	
	
	public static final String TYPE = "GDYNEmployeeDiscountManager";
	
	public GDYNEmployeeDiscountManager() {
		setName("GDYNEmployeeDiscountManager");
		setTechnicianName("GDYNEmployeeDiscountTechnician");
		getAddressDispatcherOptional();
	}
	
	/**
	 * Start the manager
	 */
	public void startUp() {
		super.startUp();
	}

	/**
	 * Close the manager
	 */
	public void shutdown() {
		super.shutdown();
	}

	public GDYNEmployeeDiscResponseObject[] getMaxSpendLimit(String employeeNumber) {
		GDYNEmployeeDiscResponseObject[] response = null;
		try {
			logger.info("Requesting max spend limit of employee GDYNEmployeeDiscManager");
			//System.out.println("Requesting max spend limit of employee GDYNEmployeeDiscManager  for employee "+employeeNumber);
			response = (GDYNEmployeeDiscResponseObject[]) sendValetWithRetry(new GDYNEmployeeDiscountValet(employeeNumber));
		} catch (ValetException e) {
			logger.error("Valet Exception", e);
		} catch (CommException e) {
			logger.error("Comm Exception", e);
		}
		return response;
	}
}
