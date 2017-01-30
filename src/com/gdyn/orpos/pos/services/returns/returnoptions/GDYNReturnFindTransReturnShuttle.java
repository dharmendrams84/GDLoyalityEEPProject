//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------
package com.gdyn.orpos.pos.services.returns.returnoptions;

import org.apache.log4j.Logger;

import com.gdyn.orpos.pos.services.returns.returnfindtrans.GDYNReturnFindTransCargo;

import oracle.retail.stores.foundation.tour.ifc.BusIfc;
import oracle.retail.stores.pos.services.returns.returnoptions.ReturnFindTransReturnShuttle;

//--------------------------------------------------------------------------
/**
    This shuttle updates the current service with the information
    from the Find Transaction by ID service.
    <p>
    @version $Revision: /rgbustores_13.4x_generic_branch/1 $
**/
//--------------------------------------------------------------------------
public class GDYNReturnFindTransReturnShuttle extends ReturnFindTransReturnShuttle
{
    // This id is used to tell
    // the compiler not to generate a
    // new serialVersionUID.
    //
    private static final long serialVersionUID = -3156085511846321892L;

    /** 
        The logger to which log messages will be sent.
    **/
    protected static Logger logger = Logger.getLogger(com.gdyn.orpos.pos.services.returns.returnoptions.GDYNReturnFindTransReturnShuttle.class);

    /**
       revision number
    **/
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/1 $";
    /**
       Child cargo.
    **/
    protected GDYNReturnFindTransCargo rftCargo = null;

    //----------------------------------------------------------------------
    /**
       Copies information needed from child service.
       <P>
       @param  bus    Child Service Bus to copy cargo from.
    **/
    //----------------------------------------------------------------------
    public void load(BusIfc bus)
    {

        rftCargo = (GDYNReturnFindTransCargo)bus.getCargo();

    }

    //----------------------------------------------------------------------
    /**
       Stores information needed by parent service.
       <P>
       @param  bus     Parent Service Bus to copy cargo to.
    **/
    //----------------------------------------------------------------------
    public void unload(BusIfc bus)
    {

        GDYNReturnOptionsCargo cargo = (GDYNReturnOptionsCargo)bus.getCargo();

        if (rftCargo.getTransferCargo())
        {
            cargo.setReturnData(cargo.addReturnData(cargo.getReturnData(), rftCargo.getReturnData()));
            cargo.setOriginalTransaction(rftCargo.getOriginalTransaction());
            cargo.setOriginalTransactionId(rftCargo.getOriginalTransactionId());
            cargo.setHaveReceipt(rftCargo.haveReceipt());
            cargo.setGiftReceiptSelected(rftCargo.isGiftReceiptSelected());
            
            //Have to set the GeoCode for obtaining tax rules
            if(cargo.getStoreStatus() != null &&
                    cargo.getStoreStatus().getStore() != null )
            {
                cargo.setGeoCode(cargo.getStoreStatus().getStore().getGeoCode());
                cargo.setStoreID(cargo.getStoreStatus().getStore().getStoreID());
            }
        }

        cargo.setOriginalExchangeTransaction(rftCargo.isOriginalExchangeTransaction());
        cargo.resetExternalOrderItemsSelectForReturn();
        cargo.setOriginalExternalOrderReturnTransactions(
                rftCargo.getOriginalExternalOrderReturnTransactions());
    }

    //----------------------------------------------------------------------
    /**
       Returns a string representation of this object.
       <P>
       @return String representation of object
    **/
    //----------------------------------------------------------------------
    public String toString()
    {                                   // begin toString()
        // result string
        String strResult = new String("Class:  ReturnFindTransReturnShuttle (Revision " +
                                      getRevisionNumber() +
                                      ")" + hashCode());

        // pass back result
        return(strResult);
    }                                   // end toString()

    //----------------------------------------------------------------------
    /**
       Returns the revision number of the class.
       <P>
       @return String representation of revision number
    **/
    //----------------------------------------------------------------------
    public String getRevisionNumber()
    {                                   // begin getRevisionNumber()
        // return string
        return(revisionNumber);
    }                                   // end getRevisionNumber()

}
