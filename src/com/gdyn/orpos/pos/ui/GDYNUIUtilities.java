package com.gdyn.orpos.pos.ui;

import java.io.Serializable;

import oracle.retail.stores.foundation.manager.ifc.ParameterManagerIfc;
import oracle.retail.stores.foundation.manager.parameter.ParameterException;
import oracle.retail.stores.foundation.tour.gate.Gateway;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.pos.services.common.AbstractFinancialCargo;
import oracle.retail.stores.pos.services.common.BillPayCargo;
import oracle.retail.stores.pos.services.tender.TenderCargo;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.StatusBeanModel;

import org.apache.log4j.Logger;

/*
 * Can't extend UIUtilities, because of a private constructor
 */
public class GDYNUIUtilities
{
	/** logger for debugging. */
	private static final Logger logger = Logger.getLogger(GDYNUIUtilities.class);


	private GDYNUIUtilities()
	{
	}


	public static StatusBeanModel getStatusBean(AbstractFinancialCargo cargo)
	{
		StatusBeanModel sModel = null;
		sModel = new StatusBeanModel();
		boolean trainingModeOn = cargo.getRegister().getWorkstation().isTrainingMode();
		sModel.setStatus(POSUIManagerIfc.TRAINING_MODE_STATUS, trainingModeOn);
		sModel.setCashierName(cargo.getOperator().getPersonName().getFirstLastName());
		sModel.setSalesAssociateName("");
		sModel.setRegister(cargo.getRegister());
		ParameterManagerIfc pm;
		pm = (ParameterManagerIfc) Gateway.getDispatcher().getManager(ParameterManagerIfc.TYPE);
		try
		{
			Serializable[] values;

			values = pm.getParameterValues("IdentifySalesAssociateEveryTransaction");
			String parameterValue = (String) values[0];
			if (parameterValue.equalsIgnoreCase("Y"))
			{
				if (cargo instanceof TenderCargo)
				{
					TenderCargo tenderCargo = (TenderCargo)cargo;
					if (tenderCargo.getEmployee() != null)
					{
						sModel.setSalesAssociateName(tenderCargo.getEmployee().getPersonName().getFirstLastName());
					}
				}
				else if (cargo instanceof BillPayCargo)
				{
					BillPayCargo billPayCargo = (BillPayCargo) cargo;
					if (billPayCargo.getAccessEmployee() != null)
					{
						sModel.setSalesAssociateName(billPayCargo.getAccessEmployee().getPersonName().getFirstLastName());
					}
				}
			} else {
				values = pm.getParameterValues("DefaultToCashier");
				parameterValue = (String) values[0];
				if (parameterValue.equalsIgnoreCase("Y"))
				{
					sModel.setSalesAssociateName(cargo.getOperator().getPersonName().getFirstLastName());
				}
			}
		}
		catch (ParameterException e)
		{
			logger.error("" + Util.throwableToString(e) + "");
		}
		return sModel;
	}


}
