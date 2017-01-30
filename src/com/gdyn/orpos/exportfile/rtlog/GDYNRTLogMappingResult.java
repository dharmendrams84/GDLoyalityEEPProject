package com.gdyn.orpos.exportfile.rtlog;

import oracle.retail.stores.exportfile.formater.RecordFormatIfc;
import oracle.retail.stores.exportfile.rtlog.RTLogMappingResult;
import oracle.retail.stores.exportfile.rtlog.RTLogMappingResultIfc;

import org.apache.log4j.Logger;

public class GDYNRTLogMappingResult extends RTLogMappingResult implements
		RTLogMappingResultIfc {

	protected static final Logger logger = Logger.getLogger(GDYNRTLogMappingResult.class);

	/** Customer record format  */
    protected RecordFormatIfc customer   = null;
    
	/**
     * Constructor
     */
    public GDYNRTLogMappingResult()
    {
        super();
    }

    /**
     * Gets the <code>customer</code> value.
     * @return the customer
     */
    public RecordFormatIfc getCustomer()
    {
        return customer;
    }

    /**
     * Sets the <code>customer</code> value.
     * @param customer the customer to set
     */
    public void setCustomer(RecordFormatIfc pCustomer)
    {
        this.customer = pCustomer;
    }
    

}
