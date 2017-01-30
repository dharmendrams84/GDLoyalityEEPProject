package com.gdyn.orpos.exportfile.rtlog;

import oracle.retail.stores.exportfile.ExportFileException;
import oracle.retail.stores.exportfile.formater.RecordFormatContentBuilderIfc;
import oracle.retail.stores.exportfile.formater.RecordFormatIfc;
import oracle.retail.stores.exportfile.rtlog.RTLogMappingResultIfc;
import oracle.retail.stores.exportfile.rtlog.RTLogRecordFormatContentBuilder;

import org.apache.log4j.Logger;

public class GDYNRTLogRecordFormatContentBuilder extends
		RTLogRecordFormatContentBuilder implements
		RecordFormatContentBuilderIfc {

    /** Logger for this class */
	protected static final Logger logger = Logger.getLogger(GDYNRTLogRecordFormatContentBuilder.class);

    /**
     * Constructor
     */
    public GDYNRTLogRecordFormatContentBuilder()
    {
        rtlog = new StringBuffer();
    }

    /**
     * Export the transaction data records; this includes items, discounts, tax, tenders, etc.
     * @param rlogResults
     * @param thRecordFormat
     * @throws ExportFileException
     */
    @Override
    protected void exportTransactionDataRecords(RTLogMappingResultIfc rlogResults, RecordFormatIfc thRecordFormat) throws ExportFileException
    {
    	super.exportTransactionDataRecords(rlogResults, thRecordFormat);
    	
    	RecordFormatIfc sctRecordFormat = ((GDYNRTLogMappingResult)rlogResults).getCustomer();
        if (sctRecordFormat != null)
        {
            setCountsAndDate(sctRecordFormat, -1);
            if(sctRecordFormat.getFieldFormats()[2].getName().equals("CustomerID"))
            {
            	if(sctRecordFormat.getFieldFormats()[2].getValue().trim().equals(""))
            	{
            		sctRecordFormat.getFieldFormats()[2].setValue("00000000000");
            	}
            	
            }
            rtlog.append(sctRecordFormat.getFormatedRecord());
        }  
    }
}
