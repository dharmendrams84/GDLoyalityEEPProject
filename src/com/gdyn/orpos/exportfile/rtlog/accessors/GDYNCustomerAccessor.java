package com.gdyn.orpos.exportfile.rtlog.accessors;

import oracle.retail.stores.exportfile.ExportFileException;
import oracle.retail.stores.exportfile.formater.RecordFormatCatalogIfc;
import oracle.retail.stores.exportfile.formater.RecordFormatIfc;
import oracle.retail.stores.exportfile.mapper.AbstractAccessor;
import oracle.retail.stores.exportfile.mapper.AccessorIfc;
import oracle.retail.stores.exportfile.mapper.MappingResultIfc;
import oracle.retail.stores.xmlreplication.extractor.ReplicationExportException;
import oracle.retail.stores.xmlreplication.result.EntityIfc;
import oracle.retail.stores.xmlreplication.result.Row;
import oracle.retail.stores.xmlreplication.result.RowIfc;
import oracle.retail.stores.xmlreplication.result.TableIfc;

import org.apache.log4j.Logger;

import com.gdyn.orpos.exportfile.rtlog.GDYNRTLogMappingResult;
import com.gdyn.orpos.persistence.utility.GDYNARTSDatabaseIfc;

public class GDYNCustomerAccessor extends AbstractAccessor implements
		AccessorIfc, GDYNARTSDatabaseIfc {

	protected static final Logger logger = Logger
			.getLogger(GDYNCustomerAccessor.class);

	public static final String formatName = "GDYNCustomer";
	
	public static final String CUSTOMER_DEFAULT_EMAIL_RECORD = "00000000000";

	/**
	 * This method finds/creates the RTLog Customer working records.
	 * 
	 * @param recordName
	 * @param tableName
	 * @param row
	 * @param results
	 * @param foramtCatalog
	 * @param entity
	 * @return RecordFormatIfc
	 * @exception ExportFileException
	 */
	public RecordFormatIfc getWorkingRecordFormat(String recordName,
			String tableName, Row row, MappingResultIfc results,
			RecordFormatCatalogIfc formatCatalog, EntityIfc entity)
			throws ExportFileException {
		GDYNRTLogMappingResult arResults = (GDYNRTLogMappingResult) results;
		RecordFormatIfc workingRecordFormat = arResults.getCustomer();

		logger.debug("DEBUG: GDYNCustomerAccessor.getWorkingRecordFormat recordName: "
				+ recordName + " tableName: " + tableName);

		if (workingRecordFormat == null) {
			workingRecordFormat = formatCatalog.getFormat(formatName);
			arResults.setCustomer(workingRecordFormat);
		}

		workingRecordFormat.setExportable(isExportable(entity));

		return workingRecordFormat;
	}

	/*
	 * Prevent totals export when the transaction is for Open rather than Close
	 * Store.
	 */
	private boolean isExportable(EntityIfc entity) throws ExportFileException {
		boolean exportable = true;
		try {
			TableIfc trans = entity.getTable(TABLE_LOYALTY_TRANSACTION_RECORD);
			RowIfc row = trans.getRow(0);
			String customerID = row.getFieldValueAsString(FIELD_LOYALTY_ID);
			String aiTrn = row
					.getFieldValueAsString(FIELD_TRANSACTION_SEQUENCE_NUMBER);
			
			if ((null == customerID) || (0 == customerID.length())) {
				
				// set the 11 0's for the Email.
				customerID = CUSTOMER_DEFAULT_EMAIL_RECORD; 
			} else {
				
				logger.debug("DEBUG: " + aiTrn
						+ " TCUST exported with customer " + customerID);
			}
		} catch (ReplicationExportException e) {
			throw new ExportFileException(e.getMessage(), e);
		}

		return exportable;
	}
}
