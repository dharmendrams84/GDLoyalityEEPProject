package com.gdyn.orpos.pos.services.sale;

// foundation imports
import oracle.retail.stores.foundation.manager.ifc.UIManagerIfc;
import oracle.retail.stores.foundation.tour.application.LaneActionAdapter;
import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.ui.POSUIManagerIfc;
import oracle.retail.stores.pos.ui.beans.DataInputBeanModel;

import com.gdyn.orpos.pos.ui.GDYNPOSUIManagerIfc;

//--------------------------------------------------------------------------
/**
    This road is traveled when the Email ID has been entered.
    It stores the loyalty ID in the cargo.
    <p>
    @version $Revision: /rgbustores_13.4x_generic_branch/1 $
**/
//--------------------------------------------------------------------------
/**
 * @author VivekIgnitiv
 *
 */
public class GDYNEmailIdEnteredRoad extends LaneActionAdapter
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 387947864548336466L;
	private static final String CUSTOMER_EMAIL_FIELD = "email";

	/**
        revision number
    **/
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";
    
    //----------------------------------------------------------------------
    /**
        Stores the loyalty ID in the cargo.
        <P>
        @param  bus  Service Bus
    **/
    //----------------------------------------------------------------------
    public void traverse(BusIfc bus)
    {
        /*
         * Get the input value from the UI Manager
         */
        POSUIManagerIfc ui;
        ui = (POSUIManagerIfc)bus.getManager(UIManagerIfc.TYPE);
        DataInputBeanModel dataBeanModel = (DataInputBeanModel)ui.getModel(GDYNPOSUIManagerIfc.ENTER_EMAIL_ID);
        String value = dataBeanModel.getValueAsString(CUSTOMER_EMAIL_FIELD);
        GDYNSaleCargo cargo = (GDYNSaleCargo)bus.getCargo();
        cargo.setLoyaltyEmailId(value);
    }
}
