package com.gdyn.orpos.exportfile.rtlog;

import oracle.retail.stores.exportfile.RecordFormatObjectFactoryIfc;
import oracle.retail.stores.exportfile.formater.RecordFormatContentBuilderIfc;
import oracle.retail.stores.exportfile.rtlog.RTLogRecordFormatObjectFactory;

import org.apache.log4j.Logger;

public class GDYNRTLogRecordFormatObjectFactory extends
		RTLogRecordFormatObjectFactory implements RecordFormatObjectFactoryIfc {

	protected static final Logger logger = Logger.getLogger(GDYNRTLogRecordFormatObjectFactory.class);

	
    /**
     *  This method gets an instance of RecordFormatContentBuilderIfc
     */
	public RecordFormatContentBuilderIfc getRecordFormatContentBuilderInstance()
    {
        return new GDYNRTLogRecordFormatContentBuilder();
    }
}
