package com.gdyn.orpos.pos.services.sale;

import oracle.retail.stores.domain.lineitem.SaleReturnLineItemIfc;
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.PosLaneActionAdapter;
import oracle.retail.stores.pos.services.sale.SaleCargo;
import oracle.retail.stores.pos.ui.DialogScreensIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DialogBeanModel;

public class GDYNCouponAlreadyExistsAisle extends PosLaneActionAdapter{
	
	 private static final long serialVersionUID = -854255340100754777L;

	 public static final String DIALOG_RESOURCE_ID = "couponAlreadyExits";

	    /* (non-Javadoc)
	     * @see oracle.retail.stores.foundation.tour.application.LaneActionAdapter#traverse(oracle.retail.stores.foundation.tour.ifc.BusIfc)
	     */
	    @Override
	    public void traverse(BusIfc bus)
	    {
	        SaleCargo cargo = (SaleCargo)bus.getCargo();
	        SaleReturnLineItemIfc srli = cargo.getLineItem();
	        DialogBeanModel dialogModel = new DialogBeanModel();
	        dialogModel.setResourceID(DIALOG_RESOURCE_ID);
	        dialogModel.setType(DialogScreensIfc.ERROR);
	        dialogModel.setArgs(new String[] { srli.getItemID() });
	        POSUIManagerIfc ui = (POSUIManagerIfc)bus.getManager(UIManagerIfc.TYPE);
	        ui.showScreen(POSUIManagerIfc.DIALOG_TEMPLATE, dialogModel);        
	    }
}
