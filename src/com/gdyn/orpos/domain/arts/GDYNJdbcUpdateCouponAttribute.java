
package com.gdyn.orpos.domain.arts;

import java.util.ArrayList;

import oracle.retail.stores.common.data.JdbcUtilities;
import oracle.retail.stores.common.sql.SQLDeleteStatement;
import oracle.retail.stores.common.sql.SQLInsertStatement;
import oracle.retail.stores.common.sql.SQLUpdateStatement;
import oracle.retail.stores.domain.arts.JdbcDataOperation;
import oracle.retail.stores.domain.manager.datareplication.CouponAttribute;
import oracle.retail.stores.domain.transaction.SaleReturnTransactionIfc;
import oracle.retail.stores.domain.transaction.TransactionIDIfc;
import oracle.retail.stores.domain.transaction.TransactionIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;

import org.apache.log4j.Logger;

import com.gdyn.orpos.persistence.utility.GDYNARTSDatabaseIfc;

/**
 * This operation save loyalty coupon attribute to database. It contains
 * the method that save loyalty coupon attributes in xml to custom loyalty coupon table in the database.
 * 
 * @version $Revision: /rgbustores_13.4x_generic_branch/1 $
 */
public class GDYNJdbcUpdateCouponAttribute extends JdbcDataOperation implements GDYNARTSDatabaseIfc
{
    private static final long serialVersionUID = -1189757875199611111L;
    
    /**
     * The logger to which log messages will be sent.
     */
    private static final Logger logger = Logger.getLogger(GDYNJdbcUpdateCouponAttribute.class);

    /**
     * Class constructor.
     */
    public GDYNJdbcUpdateCouponAttribute()
    {
        super();
        setName("GDYNJdbcUpdateCouponAttribute");
    }

    /**
     * Executes the SQL statements against the database.
     * 
     * @param dataTransaction
     * @param dataConnection a connection to the database
     * @param action
     */
    public void execute(DataTransactionIfc dataTransaction,
                        DataConnectionIfc dataConnection,
                        DataActionIfc action)
        throws DataException
    {
        if (logger.isDebugEnabled()) logger.debug( "GDYNJdbcUpdateCouponAttribute.execute");

        JdbcDataConnection connection = (JdbcDataConnection)dataConnection;

        /*
         *get coupon models from data object.
         */
        ArrayList<CouponAttribute> couponModels = (ArrayList<CouponAttribute>)action.getDataObject();

        for(int i = 0; i < couponModels.size(); i++)
        {
            insertLoyaltyCouponTODB(connection, couponModels.get(i));
        }

        if (logger.isDebugEnabled()) logger.debug( "GDYNJdbcUpdateCouponAttribute.execute");
    }

    /**
     * Reads all transactions between the specified reporting periods.
     * 
     * @param dataConnection a connection to the database
     * @param storeID The retail store ID
     * @param periods The reporting periods that begin and end the time period
     *            wanted.
     * @return The list of transactions.
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public void insertLoyaltyCouponTODB(JdbcDataConnection dataConnection,
    		CouponAttribute couponAttribute)
        throws DataException
    {
    	boolean insert = false;
    	 if (logger.isDebugEnabled()) logger.debug(
                 "GDYNJdbcUpdateCouponAttribute.insertLoyaltyCouponTODB()");

	    SQLInsertStatement sql = new SQLInsertStatement();
	
	    /*
	     * Add Table(s)
	     */
	    sql.setTable(TABLE_LOYALTY_COUPON_ATTRIBUTE);
	
	    /*
	     * Add Column(s)
	     */
	    
	    sql.addColumn(FIELD_LOYALTY_COUPON_ID,
	    		makeSafeString(couponAttribute.getID().toString()));
	    sql.addColumn(FIELD_LOYALTY_COUPON_TYPE,
	    		makeSafeString(couponAttribute.getCouponType().toString()));
	    sql.addColumn(FIELD_ITEM_APPLY_TO,
	    		makeSafeString(getApplyTO(couponAttribute.getCouponhierarchys()[0].getApplyTO()).toString()));
	    sql.addColumn(FIELD_MINIMUM_THRESHOLD_AMOUNT,
	    		makeSafeString(getMinThreshold(couponAttribute.getMinThreshold()).toString()));
	    sql.addColumn(FIELD_MAXIMUM_THRESHOLD_AMOUNT,
	    		makeSafeString(getMaxThreshold(couponAttribute.getMaxAmount()).toString()));
	    sql.addColumn(FIELD_MAXIMUM_LOYATY_DISCOUNT_AMOUNT,
	    		makeSafeString(getMaxDisocunt(couponAttribute.getMaxDiscount()).toString()));
	    sql.addColumn(FIELD_WEBSERVICE_VALIDATION_FLAG,
	    		makeSafeString(getExternalSysValidation(couponAttribute.getExternalSysValidation()).toString()));
	    sql.addColumn(FIELD_RECORD_CREATION_TIMESTAMP, getSQLCurrentTimestampFunction());
	    sql.addColumn(FIELD_RECORD_LAST_MODIFIED_TIMESTAMP, getSQLCurrentTimestampFunction());
	
	    /*
	     * Add Qualifier(s)
	     */
	    // For the specific transaction only
	    
	    try
	    {
	    
	
	    	 dataConnection.execute(sql.getSQLString());
	    }
	    catch(DataException e)
	    {
	    	if(e.getErrorCode() == DataException.REFERENTIAL_INTEGRITY_ERROR)
	    	{
	    		  insert = true;
	    		 logger.error( "Coupon Attribute  Insert count was not greater than 0");
	             logger.error( "Coupon Attribute already exist in DB so we need to update the existing coupon");
	             updateLoyaltyCouponTODB( dataConnection,
	             		 couponAttribute);
	    		
	    	}
	    	
	    }
	    	 
	    if(!insert)
	    {
	    	for(int i=0;i<couponAttribute.getCouponhierarchys().length;i++)
	    	 {
	    		 insertLoyaltyCouponHierarchy(dataConnection,
	    				 couponAttribute,i);
	    	 }
	    }
	 

    }


    private void updateLoyaltyCouponTODB(JdbcDataConnection dataConnection,
			CouponAttribute couponAttribute) throws DataException
    {
    	//update the existing filed with new data.
    	
    	 if (logger.isDebugEnabled()) logger.debug(
                 "GDYNJdbcUpdateCouponAttribute.updateLoyaltyCouponTODB()");
    	

        SQLUpdateStatement sql = new SQLUpdateStatement();

        /*
         * Add Table(s)
         */
        sql.setTable(TABLE_LOYALTY_COUPON_ATTRIBUTE);

        /*
         * Add Column(s)
         */
        sql.addColumn(FIELD_LOYALTY_COUPON_TYPE,
        		makeSafeString(couponAttribute.getCouponType().toString()));
        sql.addColumn(FIELD_ITEM_APPLY_TO,
        		makeSafeString(getApplyTO(couponAttribute.getCouponhierarchys()[0].getApplyTO()).toString()));
        sql.addColumn(FIELD_MINIMUM_THRESHOLD_AMOUNT,
        		makeSafeString(getMinThreshold(couponAttribute.getMinThreshold()).toString()));
        sql.addColumn(FIELD_MAXIMUM_THRESHOLD_AMOUNT,
        		makeSafeString(getMaxThreshold(couponAttribute.getMaxAmount()).toString()));
        sql.addColumn(FIELD_MAXIMUM_LOYATY_DISCOUNT_AMOUNT,
        		makeSafeString(getMaxDisocunt(couponAttribute.getMaxDiscount()).toString()));
        sql.addColumn(FIELD_WEBSERVICE_VALIDATION_FLAG,
        		makeSafeString(getExternalSysValidation(couponAttribute.getExternalSysValidation()).toString()));
        sql.addColumn(FIELD_RECORD_CREATION_TIMESTAMP, getSQLCurrentTimestampFunction());
        sql.addColumn(FIELD_RECORD_LAST_MODIFIED_TIMESTAMP, getSQLCurrentTimestampFunction());
        sql.addQualifier(FIELD_LOYALTY_COUPON_ID
				+ " = " +makeSafeString(couponAttribute.getID().toString()));
        
        dataConnection.execute(sql.getSQLString());
    	
    	//delete existing hierarchy and insert new 
    	 deleteLoyaltyCouponHierarchy(dataConnection,
				 couponAttribute);
    	 
    	 for(int i=0;i<couponAttribute.getCouponhierarchys().length;i++)
    	 {
    		 insertLoyaltyCouponHierarchy(dataConnection,
    				 couponAttribute,i);
    	 }
    	 
    	 
    	
	}

	private void deleteLoyaltyCouponHierarchy(
			JdbcDataConnection dataConnection, CouponAttribute couponAttribute) 
			throws DataException
	{
		
		 if (logger.isDebugEnabled()) logger.debug(
                 "GDYNJdbcUpdateCouponAttribute.deleteLoyaltyCouponHierarchy()");
		 SQLDeleteStatement sql = new SQLDeleteStatement();
		 sql.setTable(TABLE_LOYALTY_COUPON_HIERARCHY);
		 
		 sql.addQualifier(FIELD_LOYALTY_COUPON_ID
					+ " = " +makeSafeString(couponAttribute.getID().toString()));
		 
		 dataConnection.execute(sql.getSQLString());
		 
		
	}

	private Object getExternalSysValidation(String externalSysValidation) 
    {
		return externalSysValidation;
	}

	private Object getMaxDisocunt(String string) 
	{
		return string;
	}

	private Object getMaxThreshold(String string) 
	{
		return string;
	}

	private Object getMinThreshold(String minThreshold) 
	{
		return minThreshold;
	}

	private  String getApplyTO(String applyTO) 
	{
		return applyTO;
	}


	private void insertLoyaltyCouponHierarchy(
			JdbcDataConnection dataConnection, CouponAttribute couponAttribute, int index) 
			throws DataException
    {
		
		 if (logger.isDebugEnabled()) logger.debug(
                 "GDYNJdbcUpdateCouponAttribute.insertLoyaltyCouponHierarchy()");
    	
    	 SQLInsertStatement sql = new SQLInsertStatement();

         /*
          * Add Table(s)
          */
         sql.setTable(TABLE_LOYALTY_COUPON_HIERARCHY);

         /*
          * Add Column(s)
          */
         sql.addColumn(FIELD_LOYALTY_COUPON_ID,
        		 makeSafeString(couponAttribute.getID().toString()));
         sql.addColumn(FIELD_LOYALTY_DISCOUNT_DIVISION,
        		 makeSafeString(getDivision(couponAttribute.getCouponhierarchys()[index].getDivision()).toString()));
         sql.addColumn(FIELD_LOYALTY_DISCOUNT_GROUP,
        		 makeSafeString(getGroup(couponAttribute.getCouponhierarchys()[index].getGroup()).toString()));
         sql.addColumn(FIELD_LOYALTY_DISCOUNT_DPT,
        		 makeSafeString(getDepartment(couponAttribute.getCouponhierarchys()[index].getDepartment()).toString()));
         sql.addColumn(FIELD_LOYALTY_DISCOUNT_CLASS,
        		 makeSafeString(getClass(couponAttribute.getCouponhierarchys()[index].getHierarchyclass()).toString()));
         sql.addColumn(FIELD_LOYALTY_DISCOUNT_SUBCLASS,
        		 makeSafeString(getSubClass(couponAttribute.getCouponhierarchys()[index].getSubClass()).toString()));
         sql.addColumn(FIELD_RECORD_CREATION_TIMESTAMP, getSQLCurrentTimestampFunction());
         sql.addColumn(FIELD_RECORD_LAST_MODIFIED_TIMESTAMP, getSQLCurrentTimestampFunction());
          
         dataConnection.execute(sql.getSQLString());
	}
    
    private Object getSubClass(String subClass) 
    {
		return subClass;
	}

	private Object getClass(String class1) 
	{
		return class1;
	}

	private Object getDepartment(String department) 
	{
		return department;
	}

	private Object getGroup(String group) 
	{
		return group;
	}

	private Object getDivision(String division) 
	{
		return division;
	}

	public static String getSQLCurrentTimestampFunction()
    {
        return jdbcHelperClass.getSQLCurrentTimestampFunction();
    }
      

	/**
     */
    protected String getWorkstationID(SaleReturnTransactionIfc trans)
    {
        return getWorkstationID(trans.getWorkstation().getWorkstationID());
    }

    /**
     * Returns SQL-formatted workstation identifier from transaction ID object.
     * 
     * @param transactionID object
     * @return SQL-formatted workstation identifier
     */
    protected String getWorkstationID(TransactionIDIfc transactionID)
    {
        return (getWorkstationID(transactionID.getWorkstationID()));
    }

    /**
     * Returns SQL-formatted workstation identifier from string.
     * 
     * @param input string
     * @return SQL-formatted workstation identifier
     */
    protected String getWorkstationID(String input)
    {
        StringBuffer sb = new StringBuffer("'");
        sb.append(input);
        sb.append("'");
        return (sb.toString());
    }

    /**
     * Returns the store ID for the transaction
     * 
     * @param trans The transaction
     * @return the store ID
     */
    protected String getStoreID(TransactionIfc trans)
    {
        return ("'" + trans.getWorkstation().getStore().getStoreID() + "'");
    }

    /**
     * Returns the SQL-formatted store ID from the transaction ID object.
     * 
     * @param transactionID transaction ID object
     * @return the sql-formatted store ID
     */
    protected String getStoreID(TransactionIDIfc transactionID)
    {
        return (getStoreID(transactionID.getStoreID()));
    }

    /**
     * Returns the length formatted SQL-formatted store ID from the transaction
     * ID object.
     * 
     * @param transactionID transaction ID object
     * @return the sql-formatted store ID
     */
    protected String getFormattedStoreID(TransactionIDIfc transactionID)
    {
        return (getStoreID(transactionID.getFormattedStoreID()));
    }

    /**
     * Returns the store ID
     * 
     * @param storeID The store ID
     * @return the store ID
     */
    protected String getStoreID(String storeID)
    {
        return ("'" + storeID + "'");
    }

    /**
     */
    protected String getBusinessDayString(TransactionIfc trans)
    {
        return (dateToSQLDateString(trans.getBusinessDay().dateValue()));
    }

    /**
     * Returns the transaction sequence number
     * 
     * @param transaction a pos transaction
     * @return The transaction sequence number
     */
    public String getTransactionSequenceNumber(TransactionIfc transaction)
    {
        return (String.valueOf(transaction.getTransactionSequenceNumber()));
    }

}
