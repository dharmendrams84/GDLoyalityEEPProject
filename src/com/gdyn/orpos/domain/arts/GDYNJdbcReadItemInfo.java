//------------------------------------------------------------------------------
//
// Copyright (c) 2012-2013, Starmount and Groupe Dynamite.
// All rights reserved.
//
//------------------------------------------------------------------------------

package com.gdyn.orpos.domain.arts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.common.utility.LocaleRequestor;
import oracle.retail.stores.common.utility.LocaleUtilities;
import oracle.retail.stores.domain.arts.JdbcDataOperation;
import oracle.retail.stores.domain.arts.JdbcPLUOperation;
import oracle.retail.stores.domain.arts.JdbcReadNewTaxRules;
import oracle.retail.stores.domain.arts.PLURequestor;
import oracle.retail.stores.domain.stock.GDYNLoyalityMerchHrchyDtls;
import oracle.retail.stores.domain.stock.MessageDTO;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.tax.GeoCodeVO;
import oracle.retail.stores.domain.transaction.SearchCriteriaIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;
import oracle.retail.stores.foundation.utility.LocaleMap;

import org.apache.commons.lang.StringUtils;

import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityQueries;

/**
 * JdbcPLUOperation implements the price lookup JDBC data store operation.
 */
@SuppressWarnings("deprecation")

// Begin GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
// lcatania (Starmount) Mar 22, 2013
// Change super class from JdbcReadItemInfo to GDYNJdbcPLUOperation to retrieve
// color, size and style of an item
public class GDYNJdbcReadItemInfo extends GDYNJdbcPLUOperation
// End GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
{
    /**
     * 
     */
    private static final long serialVersionUID = 8057826928091802441L;
    
    // Begin GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description
    // lcatania (Starmount) Mar 22, 2013
    // Before was inherited from super class
    protected static String DEFAULT_SELECTED_VALUE = "-1";
    // End GD-237: GD_Item lookup mismatch columns when viewing items returned in item lookup by item description

    /**
     * Executes the SQL statements against the database.
     * 
     * @param dataTransaction
     *            The data transaction
     * @param dataConnection
     *            The connection to the data source
     * @param action
     *            The information passed by the valet
     * @exception DataException
     *                upon error
     */
    @Override
    public void execute(DataTransactionIfc dataTransaction,
            DataConnectionIfc dataConnection,
            DataActionIfc action)
            throws DataException
    {
        if (logger.isDebugEnabled())
            logger.debug("Entering GDYNJdbcReadItemInfo.execute");
        
        logger.info("Entering GDYNJdbcReadItemInfo.execute");
      //  System.out.println("Entering GDYNJdbcReadItemInfo.execute");
        JdbcDataConnection connection = (JdbcDataConnection) dataConnection;
        SearchCriteriaIfc itemInfo = (SearchCriteriaIfc) action.getDataObject();

        PLUItemIfc[] items = readItemInfo(connection, itemInfo);
        
        for(PLUItemIfc pluItem : items){
        	String itemDivision=readItemDivision(connection,pluItem.getItem().getItemClassification().getMerchandiseHierarchyGroup());
        	//readLoyalityMerchHrchyDtls(connection, pluItem.getItem().getItemClassification().getMerchandiseHierarchyGroup(), pluItem)   ; 
        	logger.info("item division for "+pluItem.getItemID()+ " : "+itemDivision);
        	pluItem.setItemDivision(itemDivision);
        }
        if (itemInfo.getGeoCode() == null)
        {
            JdbcReadNewTaxRules taxReader = new JdbcReadNewTaxRules();
            GeoCodeVO geoCodeVO = taxReader.readGeoCodeFromStoreId(connection, itemInfo.getStoreNumber());
            assignTaxRules(connection, items, geoCodeVO.getGeoCode());
        }
        else
        {
            assignTaxRules(connection, items, itemInfo.getGeoCode());
        }

        // Search Item by any method, This call retrieves corresponding Item Messages and updates Item Object
        getItemMessages(connection, items);

        dataTransaction.setResult(items);

        if (logger.isDebugEnabled())
            logger.debug("Exiting GDYNJdbcReadItemInfo.execute");
    }

    /**
     * Reads items from the POS Identity and Item tables.
     * 
     * @param dataConnection
     *            a connection to the database
     * @param info
     *            the item lookup key
     * @return An array of PLUItems
     * @exception DataException
     *                thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] readItemInfo(JdbcDataConnection dataConnection, SearchCriteriaIfc info)
            throws DataException
    {
    	 logger.info("Entering GDYNJdbcReadItemInfo.readItemInfo");
    	 //System.out.println("Entering GDYNJdbcReadItemInfo.readItemInfo");
        String itemDesc = info.getDescription();
        String itemTypeCode = info.getItemTypeCode();
        String itemUOMCode = info.getItemUOMCode();
        String itemStyleCode = info.getItemStyleCode();
        String itemColorCode = info.getItemColorCode();
        String itemSizeCode = info.getItemSizeCode();

        LocaleRequestor localeRequestor = info.getLocaleRequestor();

        if (itemDesc != null)
        {
            itemDesc = protectString(itemDesc);
        }
        String itemDept = info.getDepartmentID();
        String storeID = info.getStoreNumber();
        int maxMatches = info.getMaximumMatches();
        String itemManufacurer = info.getManufacturer();
        if (itemManufacurer != null)
        {
            itemManufacurer = protectString(itemManufacurer);
        }

        String qualifier = null;

        // keep track of just searching by an item identifier (e.g. itemID, posItemID, or both) and store id
        boolean searchingByItemAndStore = false;

        if (info.isSearchItemByItemNumber() && !StringUtils.isEmpty(info.getItemNumber()))
        {
            String itemNo = protectString(info.getItemNumber()); // protect any single quotation marks
            if (itemNo.indexOf('%') > -1)
            {
                qualifier = "(" + ALIAS_POS_IDENTITY + "." + FIELD_ITEM_ID + " LIKE UPPER(" + inQuotes(itemNo) + ")"
                        + " OR " + ALIAS_POS_IDENTITY + "." + FIELD_POS_ITEM_ID + " LIKE UPPER(" + inQuotes(itemNo)
                        + "))";
            }
            else
            {
                qualifier = "(" + ALIAS_POS_IDENTITY + "." + FIELD_ITEM_ID + " = " + inQuotes(itemNo)
                        + ALIAS_POS_IDENTITY + "." + FIELD_POS_ITEM_ID + " = " + inQuotes(itemNo) + ")";
                searchingByItemAndStore = true;
            }
        }
        else if (info.isSearchItemByPosItemID() && !StringUtils.isEmpty(info.getPosItemID()))
        {
            String posItemID = protectString(info.getPosItemID()); // protect any single quotation marks
            if (posItemID.indexOf('%') > -1)
            {
                qualifier = "UPPER(" + ALIAS_POS_IDENTITY + "." + FIELD_POS_ITEM_ID + ")" + " LIKE " + "UPPER("
                        + inQuotes(posItemID) + ")";
            }
            else
            {
                qualifier = ALIAS_POS_IDENTITY + "." + FIELD_POS_ITEM_ID + " = " + inQuotes(posItemID);
                searchingByItemAndStore = true;
            }
        }
        else if (info.isSearchItemByItemID() && !StringUtils.isEmpty(info.getItemID()))
        {
            String itemID = protectString(info.getItemID()); // protect any single quotation marks
            if (itemID.indexOf('%') > -1)
            {
                qualifier = "UPPER(" + ALIAS_POS_IDENTITY + "." + FIELD_POS_ITEM_ID + ")" + " LIKE " + "UPPER("
                        + inQuotes(itemID) + ")";
            }
            else
            {
                qualifier = ALIAS_POS_IDENTITY + "." + FIELD_POS_ITEM_ID + " = " + inQuotes(itemID);
                searchingByItemAndStore = true;
            }
        }

        if (itemDesc != null)
        {
            Set<Locale> bestMatches = LocaleMap.getBestMatch("", localeRequestor.getLocales());
            String descLocaleQualifier = null;
            if (itemDesc.indexOf('%') > -1)
            {
                descLocaleQualifier =
                        "UPPER(" + ALIAS_ITEM_I8 + "." + FIELD_ITEM_DESCRIPTION + ")" + " LIKE " + "UPPER('" + itemDesc
                                + "')";
            }
            else
            {
                descLocaleQualifier =
                        "UPPER(" + ALIAS_ITEM_I8 + "." + FIELD_ITEM_DESCRIPTION + ")" + " LIKE " + "UPPER('%"
                                + itemDesc + "%')";
            }
            if (info.getSearchLocale() != null)
            {
                descLocaleQualifier = descLocaleQualifier + " AND " + ALIAS_ITEM_I8 + "." + FIELD_LOCALE + " = '"
                        + LocaleMap.getBestMatch(info.getSearchLocale()).toString() + "'";
            }
            else
            {
                descLocaleQualifier = descLocaleQualifier + " AND " + ALIAS_ITEM_I8 + "." + FIELD_LOCALE + " "
                        + JdbcDataOperation.buildINClauseString(bestMatches);
            }

            if (qualifier != null)
            {
                // Using Locale Table to search description
                qualifier = qualifier + " AND " + descLocaleQualifier;
            }
            else
            {
                // Using Locale Table to search description
                qualifier = descLocaleQualifier;
            }
        }

        // search by manufacturer
        if (itemManufacurer != null)
        {
            Set<Locale> bestMatches = LocaleMap.getBestMatch("", localeRequestor.getLocales());
            String manufLocaleQualifier = null;
            if (itemManufacurer.indexOf('%') > -1)
            {
                manufLocaleQualifier =
                        "UPPER(" + ALIAS_ITEM_MANUFACTURER_I18N + "." + FIELD_ITEM_MANUFACTURER_NAME + ")" + " LIKE "
                                + "UPPER('" + itemManufacurer + "')";
            }
            else
            {
                manufLocaleQualifier =
                        "UPPER(" + ALIAS_ITEM_MANUFACTURER_I18N + "." + FIELD_ITEM_MANUFACTURER_NAME + ")" + " LIKE "
                                + "UPPER('%" + itemManufacurer + "%')";
            }
            if (info.getSearchLocale() != null)
            {
                manufLocaleQualifier = manufLocaleQualifier + " AND " + ALIAS_ITEM_MANUFACTURER_I18N + "."
                        + FIELD_LOCALE + " = '" + LocaleMap.getBestMatch(info.getSearchLocale()).toString() + "'";
            }
            else
            {
                manufLocaleQualifier = manufLocaleQualifier + " AND " + ALIAS_ITEM_MANUFACTURER_I18N + "."
                        + FIELD_LOCALE + " " + JdbcDataOperation.buildINClauseString(bestMatches);
            }
            if (qualifier != null)
            {
                // Using Locale Table to search manufacturer name
                qualifier = qualifier + " AND " + manufLocaleQualifier;
            }
            else
            {
                // Using Locale Table to search manufacturer name
                qualifier = manufLocaleQualifier;
            }
        }
        // End addition
        if (itemDept != null &&
                !itemDept.equals("-1"))
        {
            if (qualifier != null)
            {
                qualifier = qualifier + " AND " + ALIAS_ITEM + "." + FIELD_POS_DEPARTMENT_ID + " = "
                        + inQuotes(itemDept);
            }
            else
            {
                qualifier = ALIAS_ITEM + "." + FIELD_POS_DEPARTMENT_ID + " = " + inQuotes(itemDept);
            }
        }

        if (itemTypeCode != null && !itemTypeCode.equals(DEFAULT_SELECTED_VALUE))
        {
            if (qualifier != null)
            {
                qualifier = qualifier + " AND " + ALIAS_ITEM + "." + FIELD_ITEM_TYPE_CODE + " = "
                        + inQuotes(itemTypeCode);
            }
            else
            {
                qualifier = ALIAS_ITEM + "." + FIELD_ITEM_TYPE_CODE + " = " + inQuotes(itemTypeCode);
            }
        }

        if (itemUOMCode != null && !itemUOMCode.equals(DEFAULT_SELECTED_VALUE))
        {
            if (qualifier != null)
            {
                qualifier = qualifier + " AND " + ALIAS_UNIT_OF_MEASURE + "." + FIELD_UNIT_OF_MEASURE_CODE + " = "
                        + inQuotes(itemUOMCode);
            }
            else
            {
                qualifier = ALIAS_UNIT_OF_MEASURE + "." + FIELD_UNIT_OF_MEASURE_CODE + " = "
                        + inQuotes(itemUOMCode);
            }
        }
        if (itemStyleCode != null && !itemStyleCode.equals(DEFAULT_SELECTED_VALUE))
        {
            if (qualifier != null)
            {
                qualifier = qualifier + " AND " + ALIAS_STOCK_ITEM + "." + FIELD_STYLE_CODE + " = "
                        + inQuotes(itemStyleCode);
            }
            else
            {
                qualifier = ALIAS_STOCK_ITEM + "." + FIELD_STYLE_CODE + " = "
                        + inQuotes(itemStyleCode);
            }

        }
        if (itemColorCode != null && !itemColorCode.equals(DEFAULT_SELECTED_VALUE))
        {
            if (qualifier != null)
            {
                qualifier = qualifier + " AND " + ALIAS_STOCK_ITEM + "." + FIELD_COLOR_CODE + " = "
                        + inQuotes(itemColorCode);
            }
            else
            {
                qualifier = ALIAS_STOCK_ITEM + "." + FIELD_COLOR_CODE + " = "
                        + inQuotes(itemColorCode);
            }
        }
        if (itemSizeCode != null && !itemSizeCode.equals(DEFAULT_SELECTED_VALUE))
        {
            if (qualifier != null)
            {
                qualifier = qualifier + " AND " + ALIAS_STOCK_ITEM + "." + FIELD_SIZE_CODE + " = "
                        + inQuotes(itemSizeCode);
            }
            else
            {
                qualifier = ALIAS_STOCK_ITEM + "." + FIELD_SIZE_CODE + " = " + inQuotes(itemSizeCode);
            }
        }

        if (storeID != null)
        {
            if (qualifier != null)
            {
                qualifier = qualifier + " AND " + ALIAS_POS_IDENTITY + "." + FIELD_RETAIL_STORE_ID + " = "
                        + makeSafeString(storeID);
            }
            else
            {
                qualifier = ALIAS_POS_IDENTITY + "." + FIELD_RETAIL_STORE_ID + " = "
                        + makeSafeString(storeID);
            }
            searchingByItemAndStore = searchingByItemAndStore & true;
        }
        else
        {
            searchingByItemAndStore = false;
        }

        PLUItemIfc[] items = null;
        boolean usePlanogramID = info.isUsePlanogramID();

        // construct a PLU requestor
        PLURequestor pluRequestor = new PLURequestor();
        if (itemManufacurer == null)
        {
            pluRequestor.removeRequestType(PLURequestor.RequestType.Manufacture);
        }
        if (!usePlanogramID)
        {
            pluRequestor.removeRequestType(PLURequestor.RequestType.Planogram);
        }

        if (searchingByItemAndStore)
        {
            if (info.isSearchItemByItemNumber())
            {
                items = readPLUItemByItemNumber(dataConnection, info.getItemNumber(), storeID, false, pluRequestor,
                        localeRequestor);
            }
            else if (info.isSearchItemByItemID())
            {
                items = readPLUItemByItemID(dataConnection, info.getItemID(), storeID, false, pluRequestor,
                        localeRequestor);
            }
            else if (info.isSearchItemByPosItemID())
            {
                items = readPLUItemByPosItemID(dataConnection, info.getPosItemID(), storeID, false, pluRequestor,
                        localeRequestor);
            }
        }
        else
        {
            items = selectItemInfo(dataConnection,
                    qualifier,
                    maxMatches,
                    pluRequestor,
                    localeRequestor);
        }

        items = readRelatedItems(dataConnection, items, storeID);
		
        return items;
    }

    /**
     * Selects items from the POS Identity, Stock Item and Item tables.
     * 
     * @param dataConnection
     *            a connection to the database
     * @param qualifier
     *            a qualifier for item lookup
     * @param maxMatches
     *            maximum number of results to return
     * @param pluRequestor
     *            plu information to include
     * @param sqlLocale
     *            locale being used in SQL Query
     * @return An array of PLUItems
     * @exception DataException
     *                thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] selectItemInfo(JdbcDataConnection dataConnection,
            String qualifier,
            int maxMatches,
            PLURequestor pluRequestor,
            LocaleRequestor localeRequestor)
            throws DataException
    {
    	
    	logger.info("Entering GDYNJdbcReadItemInfo.selectItemInfo");
    	 //System.out.println("Entering GDYNJdbcReadItemInfo.selectItemInfo");
        SQLSelectStatement sql = new SQLSelectStatement();

        // add tables
        sql.addTable(TABLE_POS_IDENTITY, ALIAS_POS_IDENTITY);
        sql.addTable(TABLE_ITEM, ALIAS_ITEM);
        // add table for manufacturer
        boolean isManufacturerSearch = (qualifier.indexOf(FIELD_ITEM_MANUFACTURER_NAME) > -1);
        if (isManufacturerSearch)
        {
            sql.addTable(TABLE_ITEM_MANUFACTURER, ALIAS_ITEM_MANUFACTURER);

        }

        // Set distinct flag to true
        sql.setDistinctFlag(true);
        // add columns
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_POS_ITEM_ID);// FIELD_ITEM_ID);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_RETAIL_STORE_ID);

        // add qualifiers
        if (isManufacturerSearch)
        {
            // need pos identities which are manufactured by this manu
            sql.addQualifier(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_MANUFACTURER_ID +
                    " = " + ALIAS_ITEM_MANUFACTURER + "." + FIELD_ITEM_MANUFACTURER_ID);
        }
        else
        {
            sql.addQualifier(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_ID +
                    " = " + ALIAS_ITEM + "." + FIELD_ITEM_ID);
        }

        if (qualifier.indexOf(FIELD_STYLE_CODE) > -1 || qualifier.indexOf(FIELD_COLOR_CODE) > -1
                || qualifier.indexOf(FIELD_SIZE_CODE) > -1 || qualifier.indexOf(FIELD_UNIT_OF_MEASURE_CODE) > -1)
        {
            sql.addTable(TABLE_STOCK_ITEM, ALIAS_STOCK_ITEM);
            sql.addTable(TABLE_UNIT_OF_MEASURE, ALIAS_UNIT_OF_MEASURE);
            sql.addQualifier(ALIAS_STOCK_ITEM + "." + FIELD_ITEM_ID + " = " + ALIAS_ITEM + "." + FIELD_ITEM_ID
                    + " AND " +
                    ALIAS_STOCK_ITEM + "." + FIELD_STOCK_ITEM_SALE_UNIT_OF_MEASURE_CODE + " = " + ALIAS_UNIT_OF_MEASURE
                    + "." + FIELD_UNIT_OF_MEASURE_CODE);
        }

        // Using Locale Table for description search
        sql.addTable(TABLE_ITEM_I8, ALIAS_ITEM_I8);
        if (qualifier.indexOf(FIELD_ITEM_MANUFACTURER_NAME) > -1)
        {
            sql.addTable(TABLE_ITEM_MANUFACTURER_I18N, ALIAS_ITEM_MANUFACTURER_I18N);
        }

        // Add qualifier for Locale Table
        sql.addQualifier(ALIAS_ITEM_I8 + "." + FIELD_ITEM_ID + " = " + ALIAS_ITEM + "." + FIELD_ITEM_ID);
        if (qualifier.indexOf(FIELD_ITEM_MANUFACTURER_NAME) > -1)
        {
            sql.addQualifier(ALIAS_ITEM_MANUFACTURER_I18N + "." + FIELD_ITEM_MANUFACTURER_ID + " = "
                    + ALIAS_ITEM_MANUFACTURER
                    + "." + FIELD_ITEM_MANUFACTURER_ID);
        }

        // use the parameterized qualifier as well
        sql.addQualifier(qualifier);
        sql.addOrdering(ALIAS_POS_IDENTITY, FIELD_POS_ITEM_ID);

        // perform the query
        ArrayList<String> results = new ArrayList<String>();
        try
        {
            String sqlToRun = sql.getSQLString();
            dataConnection.execute(sqlToRun);

            ResultSet rs = (ResultSet) dataConnection.getResult();

            while (rs.next())
            {
                int index = 0;
                String itemID = getSafeString(rs, ++index);
                String storeID = getSafeString(rs, ++index);
                String result = itemID + "," + storeID;
                results.add(result);
            }
            rs.close();
        }
        catch (DataException de)
        {
            logger.warn(de);
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "ReadItemInfo");
            throw new DataException(DataException.SQL_ERROR, "ReadItemInfo", se);
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "ReadItemInfo", e);
        }

        if (results.isEmpty())
        {
            throw new DataException(DataException.NO_DATA,
                    "No PLU was found processing the result set in JdbcReadItemInfo.");
        }

        // see if data read exceeds maximum matches parameter
        if ((maxMatches > 0) && (results.size() > maxMatches))
        {
            throw new DataException(DataException.RESULT_SET_SIZE,
                    "Too many records were found processing the result set in JdbcReadItemInfo.");
        }

        // for each selected item id, read the PLUItem information
        ArrayList<PLUItemIfc> items = new ArrayList<PLUItemIfc>();
        Iterator<String> i = results.iterator();
        while (i.hasNext())
        {
            String result = i.next();
            String itemID = null;
            String storeID = null;
            StringTokenizer strTk = new StringTokenizer(result, ",");
            while (strTk.hasMoreTokens())
            {
                itemID = strTk.nextToken();
                storeID = strTk.nextToken();
            }
            PLUItemIfc item = readPLUItem(dataConnection, itemID, storeID, false, pluRequestor, localeRequestor)[0];
            items.add(item);
        }

        // convert results to array and return
        PLUItemIfc[] itemArray = new PLUItemIfc[items.size()];
        items.toArray(itemArray);
        return (itemArray);
    }

    /**
     * Searches the protectString for a single quote then adds another single
     * quote to protect it.
     * 
     * @param protectString
     *            the string to protect
     * @return the string with any single quotation marks protected
     */
    static public String protectString(String protectString)
    {
    	logger.info("Entering GDYNJdbcReadItemInfo.protectString");
        StringBuilder buf = new StringBuilder(protectString);
        int count = 0;
        for (int i = 0; i < buf.length(); ++i)
        {
            switch (buf.charAt(i))
            {
                case '\'': // Single Quote
                    buf.insert(i, '\''); // add another
                    i++;
                    break;

                case '\\': // backslash character
                    // Escape the backslash character
                    count = i++;
                    buf = jdbcHelperClass.backSlashChar(count, buf);
                    break;

            }
        }
        return (buf.toString());
    }

    /**
     * Retrieves Item Messages per Item from the DB and sets
     * it into the PLU Item Object
     * 
     * @return void
     * @param connection
     * @param items
     * @throws DataException
     */
    public void getItemMessages(JdbcDataConnection connection, PLUItemIfc[] items)
    {
    	//System.out.println("inside getItemMessages");
        if (items != null)
        {
            for (int itemCtr = 0; itemCtr < items.length; itemCtr++)
            {
                PLUItemIfc item = items[itemCtr];
                getItemLevelMessages(connection, item);
            }
        }
    }

    /**
     * Method which gets the ILRM Message for a Given Item
     * 
     * The Catch block simply prints the exception caused during execution
     * as the requirement is to just print the error not propogate it
     * 
     * @param dataConnection
     * @param item
     * @throws DataException
     */
    public void getItemLevelMessages(JdbcDataConnection dataConnection, PLUItemIfc item)
    {
    	//System.out.println(" inside getItemLevelMessages");
        if (item != null)
        {
            SQLSelectStatement sql = new SQLSelectStatement();
            MessageDTO mdto = null;
            List<MessageDTO> messageList = new ArrayList<MessageDTO>();
            Map<String, List<MessageDTO>> messagesMap = new HashMap<String, List<MessageDTO>>();

            // add tables
            sql.addTable(TABLE_ITEM_MESSAGE_ASSOCIATION);
            sql.addTable(TABLE_ASSET_MESSAGES);
            sql.addTable(TABLE_ASSET_MESSAGES_I18N);

            sql.addColumn(TABLE_ITEM_MESSAGE_ASSOCIATION, FIELD_MESSAGE_TYPE);
            sql.addColumn(TABLE_ITEM_MESSAGE_ASSOCIATION, FIELD_MESSAGE_CODE_ID);
            sql.addColumn(TABLE_ITEM_MESSAGE_ASSOCIATION, FIELD_MESSAGE_TRANSACTION_TYPE);
            sql.addColumn(TABLE_ASSET_MESSAGES_I18N, FIELD_LOCALE);
            sql.addColumn(TABLE_ASSET_MESSAGES_I18N, FIELD_MESSAGE_DESCRIPTION);
            // add columns from related item association

            // add qualifiers //TODO change ITEM_ID below to the IFC name
            sql.addQualifier(TABLE_ITEM_MESSAGE_ASSOCIATION, FIELD_ITEM_ID, "'" + item.getItemID() + "'");
            sql.addJoinQualifier(TABLE_ITEM_MESSAGE_ASSOCIATION, FIELD_MESSAGE_CODE_ID, TABLE_ASSET_MESSAGES,
                    FIELD_MESSAGE_CODE_ID);
            sql.addJoinQualifier(TABLE_ASSET_MESSAGES, FIELD_MESSAGE_CODE_ID, TABLE_ASSET_MESSAGES_I18N,
                    FIELD_MESSAGE_CODE_ID);
            // price info exists in the store server.

            sql.addOrdering(TABLE_ITEM_MESSAGE_ASSOCIATION, FIELD_MESSAGE_TRANSACTION_TYPE);

            try
            {
                String str = sql.getSQLString();
                String transactionType = null;
                String messageType = null;
                logger.debug(str);
                // execute the query and get the result set
                dataConnection.execute(sql.getSQLString());
                ResultSet rs = (ResultSet) dataConnection.getResult();

                while (rs.next())
                {
                    if (transactionType != null
                            && !transactionType.equalsIgnoreCase(rs.getString(FIELD_MESSAGE_TRANSACTION_TYPE)))
                    {
                        messageList.add(mdto);
                        messagesMap.put(transactionType, messageList);
                        messageList = null;
                        messageType = null;
                        messageList = new ArrayList<MessageDTO>();
                    }

                    if (messageType != null && messageType.equalsIgnoreCase(rs.getString(FIELD_MESSAGE_TYPE)))
                    {
                        mdto.addLocalizedItemMessage(LocaleUtilities.getLocaleFromString(rs.getString(FIELD_LOCALE)),
                                rs.getString(FIELD_MESSAGE_DESCRIPTION));
                        continue;
                    }
                    else if (messageType != null && !messageType.equalsIgnoreCase(rs.getString(FIELD_MESSAGE_TYPE)))
                    {
                        messageList.add(mdto);
                    }

                    messageType = rs.getString(FIELD_MESSAGE_TYPE);

                    mdto = new MessageDTO();
                    mdto.setDefaultItemMessage(rs.getString(FIELD_MESSAGE_DESCRIPTION));
                    mdto.setItemMessageCodeID(rs.getString(FIELD_MESSAGE_CODE_ID));
                    mdto.setItemMessageTransactionType(rs.getString(FIELD_MESSAGE_TRANSACTION_TYPE));
                    mdto.setItemMessageType(messageType);
                    mdto.addLocalizedItemMessage(LocaleUtilities.getLocaleFromString(rs.getString(FIELD_LOCALE)),
                            rs.getString(FIELD_MESSAGE_DESCRIPTION));

                    logger.info(mdto.toString());
                    transactionType = rs.getString(FIELD_MESSAGE_TRANSACTION_TYPE);
                }
                messageList.add(mdto);
                messagesMap.put(transactionType, messageList);
                item.setAllItemLevelMessages(messagesMap);
            }
            catch (DataException de)
            {
                logger.error(de.toString());
            }
            catch (SQLException se)
            {
                logger.error(se);
            }
            catch (Exception e)
            {
                logger.error("Unexpected exception in readItemMessage " + e);
            }
        }
    }
    
    public  String readItemDivision(JdbcDataConnection dataConnection,String merchGrpId)
            throws DataException
    {
    	
    //	System.out.println("inside readItemDivision GDYNJdbcReadItemInfo");
    	logger.info("inside readItemDivision merchandise hierarcy id "+merchGrpId);
        // add tables
        SQLSelectStatement sql = new SQLSelectStatement();
        sql.addTable("MERCHANDISE_HIERARCHY_VIEW");
        sql.addColumn("ID_MRHRC_GP");
        sql.addColumn("DIV");
        sql.addColumn("DIVDESC");
        sql.addColumn("GRP");
        sql.addColumn("GRPDESC");
        sql.addColumn("DPT");
        sql.addColumn("DPTDESC");
        sql.addColumn("CLS");
        sql.addColumn("CLSDESC");
        sql.addColumn("SCL");
        sql.addColumn("SCLDESC");
        
       // String merhGrpIdVal = "5:050202330001";
        merchGrpId ="'"+merchGrpId+"'";
        //System.out.println( "from readItem view merchGrpId  "+merchGrpId);
        logger.info( "from readItem view merchGrpId  "+merchGrpId);
        sql.addQualifier("ID_MRHRC_GP", merchGrpId);
        
        String divisionId= "";
        
        // perform the query
        try
        {
            String sqlToRun = sql.getSQLString();
           // System.out.println("sqlToRun "+sqlToRun);
            dataConnection.execute(sqlToRun);
            ResultSet rs = (ResultSet) dataConnection.getResult();
            
            while (rs.next())
            {
             // System.out.println(getSafeString(rs, 1)+" : "+getSafeString(rs, 2)+" : "+getSafeString(rs, 3)+ " : "+getSafeString(rs,4)); 
              divisionId =getSafeString(rs, 2);
            }
            rs.close();
        }
        catch (DataException de)
        {
            logger.warn(de);
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "GDYNJdbcReadItemOriginalPrice");
            throw new DataException(DataException.SQL_ERROR, "ReadItemOriginalPrice", se);
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "ReadItemOriginalPrice", e);
        }

        //System.out.println("After getting resultset divisionId "+divisionId);
        logger.info("After getting resultset divisionId "+divisionId);
       return divisionId; 
      
    }
    
    
    public  void readLoyalityMerchHrchyDtls(JdbcDataConnection dataConnection,String merchHrchyId , PLUItemIfc pluItemIfc)
            throws DataException
    {
    	
    	/*String query = "SELECT * FROM (SELECT SCL.ID_MRHRC_GP_CHLD ID_MRHRC_GP ,trim (leading '0' FROM SUBSTR(DV.ID_MRHRC_GP_CHLD,3)) div , TRIM(LEADING ' ' FROM TRIM(LEADING '*' FROM HGP1.NM_MRHRC_GP)) DivDesc ,"+
    			"trim (leading '0' FROM SUBSTR(GP.ID_MRHRC_GP_CHLD,3)) grp , TRIM(LEADING ' ' FROM TRIM(LEADING '*'FROM HGP2.NM_MRHRC_GP)) GrpDesc ,"+
    			"trim (leading '0' FROM SUBSTR(DP.ID_MRHRC_GP_CHLD,3)) dpt , TRIM(LEADING ' ' FROM TRIM(LEADING '*'FROM HGP3.NM_MRHRC_GP)) DptDesc ,"+
    			"trim (Leading '0' FROM SUBSTR(CL.ID_MRHRC_GP_CHLD, LENGTH(DP.ID_MRHRC_GP_CHLD)+1)) cls , TRIM(LEADING ' ' FROM TRIM(LEADING '*' FROM HGP4.NM_MRHRC_GP)) ClsDesc ,"+
    			"lpad(trim (Leading '0' FROM SUBSTR(SCL.ID_MRHRC_GP_CHLD, LENGTH(CL.ID_MRHRC_GP_CHLD)+1)), 3, '0') scl ,TRIM(LEADING ' ' FROM TRIM(LEADING '*' FROM HGP5.NM_MRHRC_GP)) SCLDesc "+
    			"FROM ST_ASCTN_MRHRC SCL INNER JOIN CO_MRHRC_GP HGP5 ON SCL.ID_MRHRC_GP_CHLD=HGP5.ID_MRHRC_GP "+
    			" INNER JOIN ST_ASCTN_MRHRC CL ON SCL.ID_MRHRC_GP_PRNT = CL.ID_MRHRC_GP_CHLD AND SCL.ID_MRHRC_FNC =CL.ID_MRHRC_FNC AND SCL.ID_MRHRC_LV =CL.ID_MRHRC_LV+1 "+
    			"INNER JOIN CO_MRHRC_GP HGP4 ON CL.ID_MRHRC_GP_CHLD=HGP4.ID_MRHRC_GP "+
    			" INNER JOIN ST_ASCTN_MRHRC DP ON CL.ID_MRHRC_GP_PRNT = DP.ID_MRHRC_GP_CHLD AND CL.ID_MRHRC_FNC =DP.ID_MRHRC_FNC AND CL.ID_MRHRC_LV =DP.ID_MRHRC_LV+1 "+
    			"INNER JOIN CO_MRHRC_GP HGP3 ON DP.ID_MRHRC_GP_CHLD=HGP3.ID_MRHRC_GP INNER JOIN ST_ASCTN_MRHRC GP ON DP.ID_MRHRC_GP_PRNT = GP.ID_MRHRC_GP_CHLD "+
    			" AND DP.ID_MRHRC_FNC =GP.ID_MRHRC_FNC AND DP.ID_MRHRC_LV =GP.ID_MRHRC_LV+1 INNER JOIN CO_MRHRC_GP HGP2 ON GP.ID_MRHRC_GP_CHLD=HGP2.ID_MRHRC_GP "+
    			" INNER JOIN ST_ASCTN_MRHRC DV ON GP.ID_MRHRC_GP_PRNT = DV.ID_MRHRC_GP_CHLD AND GP.ID_MRHRC_FNC =DV.ID_MRHRC_FNC AND GP.ID_MRHRC_LV =DV.ID_MRHRC_LV+1 "+
    			"INNER JOIN CO_MRHRC_GP HGP1 ON DV.ID_MRHRC_GP_CHLD=HGP1.ID_MRHRC_GP WHERE 1 =1 AND SCL.ID_MRHRC_LV =5 )"+
    			"WHERE id_mrhrc_gp = ";*/
    	try{
    	String query = GDYNLoyalityQueries.merch_hrcy_query;
    	query = query+ makeSafeString(merchHrchyId);
    	dataConnection.execute(query);
    	
    	logger.info("2222 sqlToRun to get merchandise details for item id "+ pluItemIfc.getItemID()+" " + query);

		ResultSet rs = (ResultSet) dataConnection.getResult();
		GDYNLoyalityMerchHrchyDtls loyalityMerchHrchyDtls = pluItemIfc
				.getLoyalityMerchHrchyDtls();
		while (rs.next()) {
			
			
			String divisionId = getSafeString(rs, 2);
			String groupId = getSafeString(rs, 4);
			String deptId = getSafeString(rs, 6);
			String classId = getSafeString(rs, 8);
			String subClassId = getSafeString(rs, 10);

			loyalityMerchHrchyDtls.setDivisionId(divisionId);
			loyalityMerchHrchyDtls.setGroupId(groupId);
			loyalityMerchHrchyDtls.setDeptId(deptId);
			loyalityMerchHrchyDtls.setClassId(classId);
			loyalityMerchHrchyDtls.setSubClassId(subClassId);
			pluItemIfc.setItemDivision(divisionId);
			logger.info("2222   Merchandise Details for item "
					+ pluItemIfc.getItemID() + " divisionId : " + divisionId + " groupId : "
					+ groupId + " deptId : " + deptId + " classId : " + classId + " subClassId : "
					+ subClassId);

		}
    	}catch(Exception e){
    		logger.debug(e.getMessage());
    	}
    }

}
