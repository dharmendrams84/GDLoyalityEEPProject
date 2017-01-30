package com.gdyn.orpos.domain.arts;

import java.util.ArrayList;

import oracle.retail.stores.domain.manager.datareplication.CouponAttribute;
import oracle.retail.stores.foundation.manager.data.DataAction;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.DataTransaction;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;

import org.apache.log4j.Logger;

/**
 * Class for persisting loyalty coupon attribute.
 */
public class GDYNCouponAttributeDataTransaction extends DataTransaction
{
	 /**
	 * 
	 */
	private static final long serialVersionUID = 3889839165085978862L;
	
	/**
     * The logger to which log messages will be sent.
     */
    private static final Logger logger = Logger.getLogger(GDYNCouponAttributeDataTransaction.class);


	// --------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public GDYNCouponAttributeDataTransaction()
    {
        super("GDYNCouponAttributeDataTransaction");
    }
	
    //---------------------------------------------------------------------
    /**
       persist coupon attributes to database.
       @param  List of CouponAttribute model.
       @exception  DataException when an error occurs.
    **/
    //---------------------------------------------------------------------
    public void updateCouponAttributes(ArrayList<CouponAttribute> couponModels) throws DataException
    {
        if (logger.isDebugEnabled()) logger.debug(
                     "GDYNCouponAttributeDataTransaction.updateCouponAttributes");

        // Add a DataAction to update all Coupon attribute in XML.
        DataActionIfc[] dataActions = new DataActionIfc[1];
        DataAction da = new DataAction();
        da.setDataOperationName("UpdateCouponAttribute");
        da.setDataObject(couponModels);
        dataActions[0] = da;
        setDataActions(dataActions);
        getDataManager().execute(this);

        if (logger.isDebugEnabled()) logger.debug(
                    "GDYNCouponAttributeDataTransaction.updateCouponAttributes");
    }
}
