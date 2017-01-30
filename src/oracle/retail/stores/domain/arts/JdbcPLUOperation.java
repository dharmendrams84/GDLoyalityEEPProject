/* ===========================================================================
* Copyright (c) 2008, 2012, Oracle and/or its affiliates. All rights reserved. 
 * ===========================================================================
 * $Header: rgbustores/modules/domain/src/oracle/retail/stores/domain/arts/JdbcPLUOperation.java /rgbustores_13.4x_generic_branch/7 2012/02/03 13:46:49 jswan Exp $
 * ===========================================================================
 * NOTES
 * <other useful comments, qualifications, etc.>
 *
 * MODIFIED (MM/DD/YY)
 *    jswan  02/03/12 - Modified to support tax table processing.
 *    cgreen 09/15/11 - removed deprecated methods and changed static methods
 *                      to non-static
 *    jswan  09/06/11 - Fixed issues with gift card balance when
 *                      issuing/reloading multiple gift cards and one card
 *                      fails.
 *    blarse 07/15/11 - Fix misspelled word: retrival
 *    ohorne 06/28/11 - related item fix
 *    rrkohl 05/17/11 - fix to show kit header item
 *    vtemke 04/07/11 - Fixed bug # 12333404
 *    ohorne 02/23/11 - ItemNumber can be ItemID or PosItemID
 *    sgu    02/16/11 - read manufacturer item upc
 *    cgreen 01/10/11 - refactor blob helpers into one
 *    npoola 10/08/10 - removed the condition to check the manufacturer name to
 *                      do the manufacture search
 *    aariye 07/22/10 - Fix for multi UPC items
 *    cgreen 05/27/10 - convert to oracle packaging
 *    cgreen 05/26/10 - convert to oracle packaging
 *    sgu    05/24/10 - fix item desriptoin for related items
 *    cgreen 04/28/10 - updating deprecated names
 *    cgreen 04/26/10 - XbranchMerge cgreene_tech43 from
 *                      st_rgbustores_techissueseatel_generic_branch
 *    cgreen 04/26/10 - XbranchMerge cgreene_tech75 from
 *                      st_rgbustores_techissueseatel_generic_branch
 *    cgreen 04/02/10 - remove deprecated LocaleContantsIfc and currencies
 *    cgreen 03/30/10 - remove deprecated ARTSDatabaseIfcs and change
 *                      SQLException to DataException
 *    cgreen 02/05/10 - redo stock item query so as to provide specific joins
 *    abonda 01/03/10 - update header date
 *    dwfung 12/22/09 - Added POS item id to RelatedItemSummary
 *    jswan  12/17/09 - Merge from refresh to tip.
 *    nkgaut 12/16/09 - Changes for code review findings
 *    nkgaut 12/15/09 - Changed selectStockItem method to get the UIN Label for
 *                      Item
 *    jswan  12/14/09 - Results of Merge.
 *    jswan  12/14/09 - Modifications for 'Min return price for X days'
 *                      feature.
 *    sgu    12/08/09 - rework PLURequestor to use EnumSet and rename
 *                      set/unsetRequestType to add/removeRequestType
 *    sgu    12/03/09 - Change references to use the new/improved PLU apis
 *    sgu    12/01/09 - use selectPLU instead of selectPLUs for performance
 *                      reason in reading service items
 *    sgu    11/30/09 - add plu requestor to return plu information selectively
 *    nkgaut 11/11/09 - Serialisation code changes
 *    cgreen 10/15/09 - XbranchMerge cgreene_bug-9021312 from
 *                      rgbustores_13.1x_branch
 *    cgreen 10/15/09 - XbranchMerge cgreene_bug8935989-part3 from
 *                      rgbustores_13.1x_branch
 *    cgreen 10/15/09 - use DE_ITM_SHRT column for populating item's short desc
 *    cgreen 10/12/09 - search for manufacturer on ID_IDN_PS.ID_MF and use
 *                      manufacturer id if we already know it.
 *    cgreen 09/24/09 - refactor SQL statements up support preparedStatements
 *                      for updates and inserts to improve dept hist perf
 *    cgreen 08/07/09 - changed Tax lookup failed message from FATAL to WARN
 *    cgreen 04/14/09 - convert pricingGroupID to integer instead of string
 *    cgreen 03/30/09 - removed item name column from item image table
 *    cgreen 03/27/09 - remove thumbnail image column from database
 *    cgreen 03/19/09 - refactoring changes
 *    sgu    03/17/09 - use LocaleUtilities.getLocaleFromString to convert
 *                      String to Locale object
 *    nkgaut 02/26/09 - Handled dataException for item image for offline
 *                      support
 *    mahisi 02/21/09 - Fixed issue for promotion id
 *    cgreen 03/01/09 - upgrade to using prepared statements for PLU
 *    glwang 02/18/09 - fix the sql exception when retrieving advanced pricing
 *                      rules.
 *    ranojh 02/09/09 - Deprecated code for manufacturer by customer locale and
 *                      default locale.
 *    vikini 12/22/08 - Removed Log Messages of the Query
 *    nkgaut 12/15/08 - Changing the query to fetch item's description to
 *                      accomodate offline support
 *    vikini 12/10/08 - changing scope for getItemLevelMessages method
 *    vikini 12/10/08 - changing the methods scope for getItemMessage method
 *    nkgaut 12/02/08 - ILRM Code Review Changes
 *    npoola 11/30/08 - CSP POS and BO changes
 *    ddbake 11/18/08 - Update due to merge.
 *    ddbake 11/17/08 - Verify closing result statement and prepared statement
 *                      in all cases.
 *    sgu    11/17/08 - refresh
 *    sgu    11/17/08 - read tax group id as an integer instead of float
 *    kulu   11/14/08 - forward porting 7345748
 *    mchell 11/10/08 - Fixed the item images query for db2
 *    acadar 11/03/08 - localization of reason codes for discounts and merging
 *                      to tip
 *    acadar 10/31/08 - fixes for retrieving the reason codes for advanced
 *                      pricing rules
 *    acadar 10/31/08 - minor fixes for manual discounts localization
 *    acadar 10/30/08 - use localized reason codes for item and transaction
 *                      discounts
 *    vikini 10/30/08 - Changes from Code Review.Adding Code comments, Code
 *                      formatting.
 *    vikini 10/29/08 - Added new methods to retreive Item Level MEssages from
 *                      Db.
 *    ddbake 10/28/08 - Update for merge
 *    atirke 10/27/08 - modified query for item image
 *    ranojh 10/23/08 - Incorporated code review comments.
 *    ranojh 10/23/08 - Fixed UnitOfMeasure I18N changes
 *    ranojh 10/21/08 - Code Review changes
 *    ranojh 10/21/08 - Changes for POS for UnitOfMeasure I18N
 *    ranojh 10/17/08 - Changes for code review
 *    ranojh 10/17/08 - Changes for UnitOfMeasure and Item Size/Color and Style
 *    abonda 10/17/08 - I18Ning manufacturer name
 *    mipare 10/17/08 - dept list changes with locale requestor
 *    mipare 10/17/08 - Deptartment list changes for localized text
 *    mipare 10/16/08 - dept list changes
 *    acadar 10/16/08 - fix broken unit tests
 *    acadar 10/16/08 - fix for the broken unittests
 *    acadar 10/15/08 - I18n changes for discount rules: code reviews comments
 *    acadar 10/14/08 - updated to JdbcSCLUOperation to use localized method
 *                      calls
 *    acadar 10/14/08 - cleanup for retrieval of localized discount rule name
 *    acadar 10/14/08 - updates for reading the localized discount name for
 *                      customer
 *    masahu 10/09/08 - change of column names for as_itm_img table for db2
 *    acadar 10/09/08 - merges with label
 *    acadar 10/09/08 - added new method in JdbcDataOperation for generating
 *                      the In Clause. changed jdbc plu operation to call the
 *                      new method
 *    acadar 10/09/08 - updates to the sites that use a SearchCriteriaIfc;
 *                      added new method for retrieving localized deal
 *                      information
 *    masahu 10/09/08 -
 *    acadar 10/09/08 - updates the sites to set the LocaleRequestor
 *    acadar 10/08/08 - use LocaleRequestor to read the localzed name and
 *                      description for advanced pricing rules
 *    ohorne 10/08/08 - deprecated methods per I18N Database Technical
 *                      Specification
 *    cgreen 10/07/08 - fix item image lookup and gathering discount rules do
 *                      not duplicate them
 *    atirke 10/01/08 - modified for item images
 *    atirke 09/30/08 -
 *    atirke 09/29/08 - Query changed for advanced item inquiry and related
 *                      items
 *
 *
 * ===========================================================================
 * $Log:
 *       33   .v12x      1.29.1.2    9/14/2007 4:17:18 PM   Christian Greene
 *         intial check-in
 * 32   .v12x      1.29.1.1    8/24/2007 1:46:58 PM   Michael P. Barnett In
 *         selectPLUItem(), select advanced pricing rules even if the item is
 *         non-discountable.
 * 31   .v12x      1.29.1.0    8/23/2007 6:42:26 PM   Michael P. Barnett
 *         Read pricing rule effective date and expiration date from RU_PRDV
 *         table.
 * 30   360Commerce 1.29        8/7/2007 2:34:28 PM    Maisa De Camargo When
 *         retrieving Planograms for a item, ordering the list by planogram
 *         id.
 * 29   360Commerce 1.28        8/6/2007 9:18:42 AM    Alan N. Sinton  CR
 *         28029 - Order by ID_ITM_MBR, not ID_ITM.
 * 28   360Commerce 1.27        8/1/2007 6:14:29 PM    Alan N. Sinton  CR
 *         28029 Added "order by" to the kit component's query.
 * 27   360Commerce 1.26        8/1/2007 5:51:08 PM    Maisa De Camargo A
 *         Coupon is not discountable. Updated code to retrieve the price
 *         derivation rules if the item is discountable OR if it is a coupon.
 * 26   360Commerce 1.25        7/30/2007 5:41:16 PM   Maisa De Camargo
 *         Commented out again the code that sets the merchandiseHierarchy
 *         field. It will be refactored in a near future.
 * 25   360Commerce 1.24        7/26/2007 7:59:53 AM   Alan N. Sinton  CR
 *         27192 Make item lookup depend on department tax group ID if item's
 *         tax group ID is invalid.
 * 24   360Commerce 1.23        7/16/2007 4:33:44 PM   Maisa De Camargo
 *         Commented out the code that converts the merchandiseHierarchy DB
 *         Column to a int. This column has been converted to a character in
 *         the DB.
 * 23   360Commerce 1.22        6/13/2007 4:47:57 PM   Christian Greene bulk
 *         price change checkin
 * 22   360Commerce 1.21        6/1/2007 3:16:05 PM    Christian Greene
 *         Backing out PLU to pre-v1.0.0.414 version code
 * 21   360Commerce 1.20        5/30/2007 10:28:56 PM  Christian Greene
 *         removed permanent and selling price columns
 * 20   360Commerce 1.19        5/30/2007 7:32:19 AM   Manas Sahu      SIE
 *         Workshop Changes + ORPOS Changes + Item UPSERT changes +
 *         Miscellaneous
 * 19   360Commerce 1.18        5/23/2007 4:48:30 PM   Maisa De Camargo
 *         Fixed selectPLUItem. Using Left Outer join to join the
 *         TABLE_RETAIL_STORE_ITEM.
 * 18   360Commerce 1.17        5/18/2007 12:01:47 PM  Maisa De Camargo
 *         Retrieving the PromotionId and permanent item price columns for a
 *         PLUItem.
 * 17   360Commerce 1.16        5/15/2007 5:53:46 PM   Maisa De Camargo
 *         Added PromotionId, PromotionComponentId and
 *         PromotionComponentDetailId
 * 16   360Commerce 1.15        5/7/2007 2:21:04 PM    Sandy Gu
 *         enhance shipping method retrieval and internal tax engine to handle
 *         tax rules
 * 15   360Commerce 1.14        5/3/2007 11:57:43 PM   Sandy Gu
 *         Enhance transaction persistence layer to store inclusive tax
 * 14   360Commerce 1.13        4/30/2007 5:38:35 PM   Sandy Gu        added
 *         api to handle inclusive tax
 * 13   360Commerce 1.12        4/25/2007 10:01:17 AM  Anda D. Cadar   I18N
 *         merge
 * 12   360Commerce 1.11        9/29/2006 5:31:07 PM   Brendan W. Farrell
 *         Fix for oracle db.
 * 11   360Commerce 1.10        8/7/2006 3:28:49 PM    Charles D. Baker CR
 *         19,574 - updated Query to be DB2 friendly.
 * 10   360Commerce 1.9         6/8/2006 3:54:23 PM    Brett J. Larsen CR
 *         18490 - UDM - columns CD_MTH_PRDV, CD_SCP_PRDV and CD_BAS_PRDV's
 *         type was changed to INTEGER
 * 9    360Commerce 1.8         6/7/2006 7:08:08 PM    Brett J. Larsen CR
 *         18490 - UDM - updating tax percentage and override tax percentage
 *         to support the new decimal(8, 5) db type
 * 8    360Commerce 1.7         5/12/2006 5:26:26 PM   Charles D. Baker
 *         Merging with v1_0_0_53 of Returns Managament
 * 7    360Commerce 1.6         3/27/2006 8:18:26 AM   Dinesh Gautam   This
 *         file is updated to fix CR 172 and CR 10745.
 *         readPLUItem(JdbcDataConnection dataConnection, String key,
 *         Locale sqlLocale, String storeID,
 *         boolean retrieveStoreCoupons, String fieldsToInclude) is updated.
 *         Earlier retrieveStoreCoupons flag was being used to retrieve
 *         results, now results are not retrieved based on this flag. This
 *         flag is currently used to filter results retrieved.
 * 6    360Commerce 1.5         1/25/2006 4:11:09 PM   Brett J. Larsen merge
 *         7.1.1 changes (aka. 7.0.3 fixes) into 360Commerce view
 * 5    360Commerce 1.4         1/22/2006 11:41:15 AM  Ron W. Haight
 *         Removed references to com.ibm.math.BigDecimal
 * 4    360Commerce 1.3         12/13/2005 4:43:43 PM  Barry A. Pape
 *         Base-lining of 7.1_LA
 * 3    360Commerce 1.2         3/31/2005 4:28:38 PM   Robert Pearse
 * 2    360Commerce 1.1         3/10/2005 10:22:39 AM  Robert Pearse
 * 1    360Commerce 1.0         2/11/2005 12:11:55 PM  Robert Pearse
 * $:
 * 10   .v710     1.2.3.3     11/1/2005 16:54:12     Brendan W. Farrell Fix
 *         merge error to get age restriction.
 * 9    .v710     1.2.3.2     10/25/2005 17:52:59    Charles Suehs   Merged
 *         from v700.
 * 8    .v710     1.2.3.1     10/24/2005 17:29:25    Charles Suehs   Merge
 *         from JdbcPLUOperation.java, Revision 1.2.2.0
 * 7    .v710     1.2.3.0     9/21/2005 13:39:46     Brendan W. Farrell
 *         Initial Check in merge 67.
 * 6    .v700     1.2.2.2     11/16/2005 16:26:23    Jason L. DeLeau 4215:
 *         Get rid of redundant ArtsDatabaseifc class
 * 5    .v700     1.2.2.1     10/27/2005 14:37:09    Jason L. DeLeau 172:
 *         Make sure a transaction with a store coupon can be returned.
 * 4    .v700     1.2.2.0     9/13/2005 15:40:10     Jason L. DeLeau If an
 *         id_itm_pos maps to multiple id_itms, let the user choose which one
 *         to use.
 * 3    360Commerce1.2         3/31/2005 15:28:38     Robert Pearse
 * 2    360Commerce1.1         3/10/2005 10:22:39     Robert Pearse
 * 1    360Commerce1.0         2/11/2005 12:11:55     Robert Pearse
 * $
 * Revision 1.22.2.2  2005/01/21 17:43:43  kmcbride
 * @scr 7939: SelectPLUItem does not need to care about store coupons directly.
 *
 * Revision 1.22.2.1  2005/01/20 19:27:14  kmcbride
 * @scr 7939: Adding ability for poslog to retrieve store coupon line items while being as confident as possible that this does not regress other transaction lookups.
 *
 * Revision 1.22  2004/08/23 16:15:46  cdb
 * @scr 4204 Removed tab characters
 *
 * Revision 1.21  2004/07/19 21:12:36  kmcbride
 * @scr 5968: Fixed the threading issue with the static hash, each call creates it's own hash, also added a unit test that is capable of invoking multiple threads calling the class.  The unit test easily reproduced symptoms of thread safety issues with the previous class, and confirmed the updated class to be thread safe.
 *
 * Revision 1.20  2004/07/02 19:11:27  jdeleau
 * @scr 5982 Support Tax Holiday
 *
 * Revision 1.19  2004/06/18 13:59:09  jdeleau
 * @scr 2775 Unify the way rules are generated, so that flat files and
 * the database use the same business logic
 *
 * Revision 1.18  2004/06/17 22:33:59  jdeleau
 * @scr 2775 Table updates to normalize DB data.
 *
 * Revision 1.17  2004/06/17 17:36:34  mkp1
 * @scr 2775 Defects for Tax
 *
 * Revision 1.16  2004/06/11 18:46:12  jdeleau
 * @scr 2775 Fix SQL query for tax with no taxGroupIDs.
 *
 * Revision 1.15  2004/06/10 15:34:53  jdeleau
 * @scr 2775 Make Flat File PLU lookups contain tax rules
 *
 * Revision 1.14  2004/06/10 14:21:29  jdeleau
 * @scr 2775 Use the new tax data for the tax flat files
 *
 * Revision 1.13  2004/06/09 11:30:28  mkp1
 * @scr 2775 Added changes to accomodate Kit items
 *
 * Revision 1.12  2004/06/07 20:19:24  mkp1
 * @scr 2775 PLU now checks whether item is taxable before retrieving tax rules
 *
 * Revision 1.11  2004/06/03 16:22:41  jdeleau
 * @scr 2775 Initial Drop of send item tax support.
 *
 * Revision 1.10  2004/06/01 17:55:29  mkp1
 * @scr 2775 Fixed PLU to return correct tax calculator
 *
 * Revision 1.9  2004/05/27 19:31:44  jdeleau
 * @scr 2775 Remove unused imports as a result of tax engine rework
 *
 * Revision 1.8  2004/05/27 16:59:22  mkp1
 * @scr 2775 Checking in first revision of new tax engine.
 *
 * Revision 1.7  2004/04/09 16:55:46  cdb
 * @scr 4302 Removed double semicolon warnings.
 *
 * Revision 1.6  2004/02/17 22:08:46  epd
 * @scr 0
 * Updated to read/save the new size required flag
 *
 * Revision 1.5  2004/02/17 17:57:37  bwf
 * @scr 0 Organize imports.
 *
 * Revision 1.4  2004/02/17 16:18:46  rhafernik
 * @scr 0 log4j conversion
 *
 * Revision 1.3  2004/02/12 17:13:14  mcs
 * Forcing head revision
 *
 * Revision 1.2  2004/02/11 23:25:23  bwf
 * @scr 0 Organize imports.
 *
 * Revision 1.1.1.1  2004/02/11 01:04:27  cschellenger
 * updating to pvcs 360store-current
 *
 *    Rev 1.1   Feb 06 2004 12:21:18   cdb
 * Added handling for Damage Disount Eligible items.
 * Resolution for 3588: Discounts/MUPS - Gap Rollback
 *
 *    Rev 1.0   Aug 29 2003 15:30:58   CSchellenger
 * Initial revision.
 *
 *    Rev 1.20   14 Jul 2003 16:44:30   crain
 * Replaced item id with pos item id
 *
 *    Rev 1.19   11 Jul 2003 12:51:22   crain
 * Added compare at price; employee discount allowed; merchandise hierarchy group id
 *
 *    Rev 1.18   Jun 16 2003 11:52:12   bwf
 * Added functionality to change discount on lowest priced item to be able to be used on highest priced item also.
 * Resolution for 2765: Advanced Pricing Rule - Discount on Highest Priced Item
 *
 *    Rev 1.17   Apr 07 2003 16:00:38   baa
 * fix typo on select plu items
 * Resolution for POS SCR-1866: I18n Database  support
 *
 *    Rev 1.16   Mar 31 2003 16:05:32   bwf
 * Database Internationalization
 * Resolution for 1866: I18n Database  support
 *
 *    Rev 1.15   Mar 24 2003 16:53:58   RSachdeva
 * Database Internationalization
 * Resolution for POS SCR-1866: I18n Database  support
 *
 *    Rev 1.14   Mar 24 2003 16:34:08   baa
 * add multiple item descriptions for supported locales
 * Resolution for POS SCR-1843: Multilanguage support
 *
 *    Rev 1.13   Mar 14 2003 16:27:54   RSachdeva
 * Database Internationalization selectKitComponents
 * Resolution for POS SCR-1866: I18n Database  support
 *
 *    Rev 1.12   Feb 16 2003 12:49:46   mpm
 * Merged 5.1 changes.
 * Resolution for POS SCR-2053: Merge 5.1 changes into 6.0
 *
 *    Rev 1.11   Feb 06 2003 11:48:48   jgs
 * Fixed a build problem with a change to the signature of a static method.
 * Resolution for 105: Modify PLU Look to optionally include the store number in the item lookup.
 *
 *    Rev 1.10   Feb 03 2003 07:41:28   jgs
 * Add optional store number to PLU Lookup.
 * Resolution for 105: Modify PLU Look to optionally include the store number in the item lookup.
 *
 *    Rev 1.9   Jan 29 2003 11:02:50   RSachdeva
 * Database Internationalization
 * Resolution for POS SCR-1866: I18n Database  support
 *
 *    Rev 1.8   Jan 22 2003 12:00:16   mpb
 * SCR #1626
 *
 * Added reduction accounting disposition code to the lookup.
 * Resolution for POS SCR-1626: Pricing Feature
 *
 *    Rev 1.7   Jan 20 2003 11:49:02   jgs
 * Added code to read/write columns for allow repeating sources, deal distribution, and percent off lowest priced Item.
 * Resolution for 103: New Advanced Pricing Features
 *
 *    Rev 1.6   Jan 16 2003 08:58:32   RSachdeva
 * Added Database Subsystem Check.
 * Resolution for POS SCR-1866: I18n Database  support
 *
 *    Rev 1.5   Jan 08 2003 17:11:34   RSachdeva
 * Database Internationalization
 * Resolution for POS SCR-1866: I18n Database  support
 *
 *    Rev 1.4   Dec 31 2002 09:26:24   RSachdeva
 * Database Internationalization
 * Resolution for POS SCR-1866: I18n Database  support
 *
 *    Rev 1.3   Jul 18 2002 13:48:24   DCobb
 * Add Alteration item for POS 5.5 Alteration Package.
 * Resolution for POS SCR-1753: POS 5.5 Alterations Package
 *
 *    Rev 1.2   24 Jun 2002 11:53:32   jbp
 * remove changes from previous checkin
 * Resolution for POS SCR-1726: Void - Void of new special order gets stuck in the queue in DB2
 *
 *    Rev 1.1   24 Jun 2002 11:48:30   jbp
 * merge from 5.1 SCR 1726
 * Resolution for POS SCR-1726: Void - Void of new special order gets stuck in the queue in DB2
 *
 *    Rev 1.0   Jun 03 2002 16:36:44   msg
 * Initial revision.
 *
 * ===========================================================================
 */
package oracle.retail.stores.domain.arts;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import oracle.retail.stores.commerceservices.common.currency.CurrencyIfc;
import oracle.retail.stores.common.sql.SQLParameterIfc;
import oracle.retail.stores.common.sql.SQLParameterValue;
import oracle.retail.stores.common.sql.SQLSelectStatement;
import oracle.retail.stores.common.utility.LocaleRequestor;
import oracle.retail.stores.common.utility.LocaleUtilities;
import oracle.retail.stores.common.utility.LocalizedCodeIfc;
import oracle.retail.stores.common.utility.LocalizedText;
import oracle.retail.stores.common.utility.LocalizedTextIfc;
import oracle.retail.stores.domain.DomainGateway;
import oracle.retail.stores.domain.discount.AdvancedPricingRuleIfc;
import oracle.retail.stores.domain.discount.DiscountRuleConstantsIfc;
import oracle.retail.stores.domain.event.PriceChangeIfc;
import oracle.retail.stores.domain.stock.AlterationPLUItemIfc;
import oracle.retail.stores.domain.stock.GDYNLoyalityCpnAttrDtls;
import oracle.retail.stores.domain.stock.GDYNLoyalityCpnHrchyDtls;
import oracle.retail.stores.domain.stock.GDYNLoyalityMerchHrchyDtls;
import oracle.retail.stores.domain.stock.GiftCardPLUItemIfc;
import oracle.retail.stores.domain.stock.ItemClassificationConstantsIfc;
import oracle.retail.stores.domain.stock.ItemClassificationIfc;
import oracle.retail.stores.domain.stock.ItemImageIfc;
import oracle.retail.stores.domain.stock.ItemKitConstantsIfc;
import oracle.retail.stores.domain.stock.ItemKitIfc;
import oracle.retail.stores.domain.stock.KitComponentIfc;
import oracle.retail.stores.domain.stock.MerchandiseClassificationIfc;
import oracle.retail.stores.domain.stock.MessageDTO;
import oracle.retail.stores.domain.stock.PLUItem;
import oracle.retail.stores.domain.stock.PLUItemIfc;
import oracle.retail.stores.domain.stock.ProductGroupConstantsIfc;
import oracle.retail.stores.domain.stock.ProductGroupIfc;
import oracle.retail.stores.domain.stock.RelatedItemContainerIfc;
import oracle.retail.stores.domain.stock.RelatedItemGroupIfc;
import oracle.retail.stores.domain.stock.RelatedItemIfc;
import oracle.retail.stores.domain.stock.RelatedItemSummaryIfc;
import oracle.retail.stores.domain.stock.UnitOfMeasureIfc;
import oracle.retail.stores.domain.store.DepartmentIfc;
import oracle.retail.stores.domain.tax.NewTaxRuleIfc;
import oracle.retail.stores.domain.transaction.SearchCriteriaIfc;
import oracle.retail.stores.domain.utility.AlterationIfc;
import oracle.retail.stores.domain.utility.CodeConstantsIfc;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.domain.utility.EYSTime;
import oracle.retail.stores.domain.utility.GiftCardIfc;
import oracle.retail.stores.foundation.manager.data.DataException;
import oracle.retail.stores.foundation.manager.data.JdbcDataConnection;
import oracle.retail.stores.foundation.manager.ifc.data.DataActionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataConnectionIfc;
import oracle.retail.stores.foundation.manager.ifc.data.DataTransactionIfc;
import oracle.retail.stores.foundation.util.DBUtils;
import oracle.retail.stores.foundation.utility.LocaleMap;
import oracle.retail.stores.foundation.utility.Util;
import oracle.retail.stores.persistence.utility.ARTSDatabaseIfc;
import oracle.retail.stores.persistence.utility.DatabaseBlobHelperFactory;
import org.apache.log4j.Logger;
import com.gdyn.orpos.domain.stock.GDYNPLUItemIfc;
import com.gdyn.orpos.pos.loyalityConstants.GDYNLoyalityQueries;

/**
 * JdbcPLUOperation implements the price lookup JDBC data store operation.
 */
public class JdbcPLUOperation extends JdbcDataOperation
    implements ARTSDatabaseIfc, ProductGroupConstantsIfc, DiscountRuleConstantsIfc
{
    // ID for compatible serialization
    private static final long serialVersionUID = 6476326562623L;

    /**
     * The logger to which log messages will be sent.
     */
    protected static final Logger logger = Logger.getLogger(JdbcPLUOperation.class);

    /**
     * revision number of this class
     */
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/7 $";

    /** @deprecated as of 13.0. Use {@link ARTSDatabaseIfc#ITEM_TYPE_STOCK} instead. */
    public static final String ITEM_TYPE_STOCK          = "STCK";

    /** @deprecated as of 13.0. Use {@link ARTSDatabaseIfc#ITEM_TYPE_SERVICE} instead. */
    public static final String ITEM_TYPE_SERVICE        = "SRVC";

    /** @deprecated as of 13.0. Use {@link ARTSDatabaseIfc#ITEM_TYPE_STORE_COUPON} instead. */
    public static final String ITEM_TYPE_STORE_COUPON   = "SCPN";

    /**
     * Executes the SQL statements against the database.
     *
     * @param dataTransaction The data transaction
     * @param dataConnection The connection to the data source
     * @param action The information passed by the valet
     * @exception DataException upon error
     */
    public void execute(DataTransactionIfc dataTransaction, DataConnectionIfc dataConnection, DataActionIfc action)
            throws DataException
    {
    	
        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.execute");

        JdbcDataConnection connection = (JdbcDataConnection)dataConnection;

        // send back the selected data (or lack thereof)
        PLUItemIfc[] items = null;
        Object dataObject = action.getDataObject();
        if (dataObject instanceof SearchCriteriaIfc)
        {
            items = getPluItems(connection, (SearchCriteriaIfc) dataObject);
        }
        // Retrieves item excluding store coupons
        else if (dataObject instanceof StringSearchCriteria)
        {
            StringSearchCriteria inquiry = (StringSearchCriteria)dataObject;
            String pluNumber = inquiry.getIdentifier();
            items = readPLUItem(connection, pluNumber, inquiry.getLocaleRequestor(), null);
            items = readRelatedItems(connection, items, null);
            getItemMessages(connection, items);
        }
        // Retrieves item excluding store coupons
        else
        {
            String pluNumber = (String)action.getDataObject();
            items = readPLUItem(connection, pluNumber, getRequestLocales(LocaleMap.getLocale(LocaleMap.DEFAULT)), null);
            items = readRelatedItems(connection, items, null);
            getItemMessages(connection, items);
        }

        dataTransaction.setResult(items);

        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.execute");
    }

    /**
     * Selects items from the POS Identity and Item tables.
     *
     * @param dataConnection a connection to the database
     * @param inquiry search criteria indicating the search method (e.g. ItemNumber, ItemID, or PosItemID)
     * @return An array of PLUItems with associated advanced pricing rules
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public  PLUItemIfc[] getPluItems(JdbcDataConnection connection, SearchCriteriaIfc inquiry) throws DataException
    {
        if (inquiry.isSearchItemByItemNumber())
        {
            return getPluItemsByItemNumber(connection, inquiry);
        }
        else if (inquiry.isSearchItemByItemID())
        {
            return getPluItemsByItemID(connection, inquiry);
        }
        else if (inquiry.isSearchItemByPosItemID())
        {
            return getPluItemsByPosItemID(connection, inquiry);
        }
        else
        {
            //search method not explicitly stated so search by PosItemID
            return getPluItemsByPosItemID(connection, inquiry);
        }
    }

    /**
     * Selects items from the POS Identity and Item tables by ItemNumber
     *
     * @param dataConnection a connection to the database
     * @param inquiry search criteria indicating the ItemNumber
     * @return An array of PLUItems with associated advanced pricing rules
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] getPluItemsByItemNumber(JdbcDataConnection connection, SearchCriteriaIfc inquiry) throws DataException
    {
        PLUItemIfc[] items;
        LocaleRequestor localeRequestor = inquiry.getLocaleRequestor();
        String key = inquiry.getItemNumber();
        String storeID = inquiry.getStoreNumber();
        String geoCode = inquiry.getGeoCode();
        PLURequestor pluRequestor = inquiry.getPLURequestor();

        items = readPLUItemByItemNumber(connection, key, storeID, false, pluRequestor, localeRequestor);

        items = decoratePluItems(connection, items, storeID, geoCode, pluRequestor);
        return items;

    }

    /**
     * Selects items from the POS Identity and Item tables by ItemID
     *
     * @param dataConnection a connection to the database
     * @param inquiry search criteria indicating the ItemID
     * @return An array of PLUItems with associated advanced pricing rules
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
     public PLUItemIfc[] getPluItemsByItemID(JdbcDataConnection connection, SearchCriteriaIfc inquiry) throws DataException
    {
        PLUItemIfc[] items;
        LocaleRequestor localeRequestor = inquiry.getLocaleRequestor();
        String itemID = inquiry.getItemID();
        String storeID = inquiry.getStoreNumber();
        String geoCode = inquiry.getGeoCode();
        PLURequestor pluRequestor = inquiry.getPLURequestor();

        items = readPLUItemByItemID(connection, itemID, storeID, false, pluRequestor, localeRequestor);

        items = decoratePluItems(connection, items, storeID, geoCode, pluRequestor);
        return items;
    }

     /**
      * Selects items from the POS Identity and Item tables by PosItemID
      *
      * @param dataConnection a connection to the database
      * @param inquiry search criteria indicating the PosItemID
      * @return An array of PLUItems with associated advanced pricing rules
      * @exception DataException thrown when an error occurs executing the SQL
      *                against the DataConnection, or when processing the
      *                ResultSet
      */
    public PLUItemIfc[] getPluItemsByPosItemID(JdbcDataConnection connection, SearchCriteriaIfc inquiry) throws DataException
    {
        PLUItemIfc[] items;
        LocaleRequestor localeRequestor = inquiry.getLocaleRequestor();
        String posItemID = inquiry.getPosItemID();
        String storeID = inquiry.getStoreNumber();
        String geoCode = inquiry.getGeoCode();
        PLURequestor pluRequestor = inquiry.getPLURequestor();

        items = readPLUItemByPosItemID(connection, posItemID, storeID, false, pluRequestor, localeRequestor);

        items = decoratePluItems(connection, items, storeID, geoCode, pluRequestor);
        return items;
    }


    /**
     * Adds any applicable Tax Rules, Related Items and Item Messages to the supplied PluItems
     *
     * @param connection a connection to the database
     * @param items the items to decorate
     * @param storeID the store ID
     * @param geoCode the geocode
     * @param pluRequestor
     * @return the PluItems decorated with any applicable tax rules, related items and item messages
     * @throws DataException
     */
    public  PLUItemIfc[] decoratePluItems(JdbcDataConnection connection, PLUItemIfc[] items, String storeID,
            String geoCode, PLURequestor pluRequestor) throws DataException
    {
        if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.TaxRules))
        {
            assignTaxRules(connection, items, geoCode);
        }
        if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.RelatedItems))
        {
            items = readRelatedItems(connection, items, storeID);
        }
        if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.ItemMessages))
        {
            getItemMessages(connection, items);
        }
        return items;
    }

    /**
     * Read any related items for the list of given pluItems
     *
     * @param dataConnection DB connection
     * @param items Items to get related items for
     * @param storeID store number
     * @return The list of items passed in, witht he relatedItemContainerIfc
     *         populated.
     * @throws DataException
     * @since NEP67
     */
    public PLUItemIfc[] readRelatedItems(JdbcDataConnection dataConnection, PLUItemIfc[] items, String storeID)
            throws DataException
    {
        if (items != null)
        {
            for (int i = 0; i < items.length; i++)
            {
                readRelatedItems(dataConnection, items[i], storeID);
            }
        }
        return items;
    }

    /**
     * For a single PLUItem, populate the RelatedItemContainerIfc for that item
     * with all related items for that object. Example query.
     * <p><blockquote><pre>
     * SELECT RI.ID_ITM_RLTD, RI.CD_TY_RLTD_ITM, RI.CO_DPLY_PRI,
     *      RI.ASC_GP_RLTD_ITM, RI.FL_RLTD_ITM_RM, RI.FL_RLTD_ITM_RTN,
     *      I.DE_ITM, I.ID_DPT_POS
     * FROM CO_ASC_RLTD_ITM RI
     * JOIN AS_ITM I ON I.ID_ITM = RI.ID_ITM
     * JOIN AS_ITM_RTL_STR SI ON SI.ID_ITM = I.ID_ITM
     * WHERE SI.ID_STR_RT = '00692'
     * AND SI.ID_ITM = '11468055'
     * </pre></blockquote>
     *
     * @param dataConnection DB connection
     * @param items Items to get related items for
     * @param storeID store number
     * @return PLUItem passed in, with the relatedItemContainer populated.
     * @throws DataException
     * @since NEP67
     */
    public PLUItemIfc readRelatedItems(JdbcDataConnection dataConnection, PLUItemIfc item, String storeID)
            throws DataException
    {
        if (item != null)
        {
            RelatedItemContainerIfc relatedItemContainer = DomainGateway.getFactory().getRelatedItemContainerInstance();
            item.setRelatedItemContainer(relatedItemContainer);

            SQLSelectStatement sql = new SQLSelectStatement();

            // add tables
            sql.addTable(TABLE_RELATED_ITEM_ASSOCIATION);

            // add columns from related item association
            sql.addColumn(FIELD_RELATED_ITEM_ID);
            sql.addColumn(FIELD_POS_ITEM_ID);
            sql.addColumn(FIELD_RELATED_ITEM_TYPE_CODE);
            sql.addColumn(FIELD_DISPLAY_PRIORITY);
            sql.addColumn(FIELD_RELATED_ITEM_GROUP_ASSOCIATION);
            sql.addColumn(FIELD_REMOVE_RELATED_ITEM_FLAG);
            sql.addColumn(FIELD_RETURN_RELATED_ITEM_FLAG);
            // add columns from item
            sql.addColumn(FIELD_POS_DEPARTMENT_ID);

            sql.addOuterJoinQualifier(" JOIN " + TABLE_POS_IDENTITY +
                    " ON " + TABLE_POS_IDENTITY + "." + FIELD_ITEM_ID +
                    " = " + TABLE_RELATED_ITEM_ASSOCIATION+ "." + FIELD_RELATED_ITEM_ID);
            sql.addOuterJoinQualifier(" JOIN " + TABLE_ITEM +
                    " ON " + TABLE_ITEM + "." + FIELD_ITEM_ID +
                    " = " + TABLE_RELATED_ITEM_ASSOCIATION+ "." + FIELD_ITEM_ID);
            sql.addOuterJoinQualifier(" JOIN " + TABLE_RETAIL_STORE_ITEM +
                    " ON " + TABLE_RETAIL_STORE_ITEM + "." + FIELD_ITEM_ID +
                    " = " + TABLE_ITEM+ "." + FIELD_ITEM_ID);

            // add qualifiers
            sql.addQualifier(new SQLParameterValue(TABLE_RETAIL_STORE_ITEM, FIELD_ITEM_ID, item.getItemID()));
            // If no storeID is given, then the data needs to be setup such that only one store's
            // price info exists in the store server.
            if (storeID != null && storeID.length() > 0)
            {
                sql.addQualifier(new SQLParameterValue(TABLE_RETAIL_STORE_ITEM, FIELD_RETAIL_STORE_ID, storeID));
            }

            try
            {
                // execute the query and get the result set
                ResultSet rs = execute(dataConnection, sql);

                while (rs.next())
                {
                    // parse result set and create domain objects as necessary
                    int index = 0;
                    String relatedItemID = getSafeString(rs, ++index);
                    String posItemID = getSafeString(rs, ++index);
                    String typeCode = getSafeString(rs, ++index);
                    int displayPriority = rs.getInt(++index);
                    int groupNumber = rs.getInt(++index);
                    boolean deleteable = getBooleanFromString(rs, ++index);
                    boolean returnable = getBooleanFromString(rs, ++index);
                    String departmentID = getSafeString(rs, ++index);

                    RelatedItemSummaryIfc relatedItemSummary = DomainGateway.getFactory()
                            .getRelatedItemSummaryInstance();
                    //relatedItemSummary.setLocalizedDescriptions(item.getLocalizedDescriptions());
                    Locale sqlLocale = LocaleMap.getLocale(LocaleMap.DEFAULT);
                    LocaleRequestor localeRequestor = getRequestLocales(sqlLocale);
                    relatedItemSummary.setLocalizedDescriptions(applyLocaleDependentDescriptions(dataConnection,relatedItemID, localeRequestor));
                    relatedItemSummary.setDepartmentID(departmentID);
                    relatedItemSummary.setItemID(relatedItemID);
                    relatedItemSummary.setPosItemID(posItemID);

                    RelatedItemIfc relatedItem = DomainGateway.getFactory().getRelatedItemInstance();
                    relatedItem.setDisplayPriority(displayPriority);
                    relatedItem.setDeleteable(deleteable);
                    relatedItem.setReturnable(returnable);
                    relatedItem.setRelatedItemSummary(relatedItemSummary);
                    relatedItemContainer.addRelatedItem(typeCode, groupNumber, relatedItem);
                }

                // populate price changes and item images for the related items
                RelatedItemGroupIfc[] groups = relatedItemContainer.getAllRelatedItemGroups();
                if (groups.length > 0)
                {
                    JdbcSelectPriceChange selectPriceChange = new JdbcSelectPriceChange();
                    PLUItemIfc transientPLUItem = instantiatePLUItem();
                    transientPLUItem.setStoreID(storeID);
                    Calendar now = Calendar.getInstance();

                    for (int i = groups.length - 1; i >= 0; i--)
                    {
                        RelatedItemIfc[] items = groups[i].getRelatedItems();
                        for (int j = items.length - 1; j >= 0; j--)
                        {
                            RelatedItemSummaryIfc relatedItemSummary = items[j].getRelatedItemSummary();
                            transientPLUItem.setItemID(relatedItemSummary.getItemID());

                            PriceChangeIfc[] changes = selectPriceChange.readPermanentPriceChanges(dataConnection,
                                    transientPLUItem, now); // this is not timezone safe
                            transientPLUItem.setPermanentPriceChanges(changes);
                            changes = selectPriceChange.readAllTemporaryPriceChanges(dataConnection,
                                    transientPLUItem,now); // this is not timezone safe
                            transientPLUItem.setTemporaryPriceChangesAndTemporaryPriceChangesForReturns(changes);

                            relatedItemSummary.setPrice(transientPLUItem.getPrice());

                            applyItemImages(dataConnection, transientPLUItem, null);
                            relatedItemSummary.setItemImage(transientPLUItem.getItemImage());
                        }
                    }
                }
            }
            catch (DataException de)
            {
                logger.warn(de.toString());
                throw de;
            }
            catch (SQLException se)
            {
                dataConnection.logSQLException(se, "readRelatedItems");
                throw new DataException(DataException.SQL_ERROR, "readRelatedItems", se);
            }
            catch (Exception e)
            {
                logger.error("Unexpected exception in readRelatedItems " + e);
                throw new DataException(DataException.UNKNOWN, "readRelatedItems", e);
            }
        }
        return item;
    }

    /**
     * @param dataConnection
     * @param key
     * @param localeRequestor
     * @param storeID
     * @return
     * @throws DataException
     */
    public PLUItemIfc[] readPLUItem(JdbcDataConnection dataConnection, String key,
            LocaleRequestor localeRequestor, String storeID) throws DataException
    {
        // KLM: Overloading this method to allow for store coupon line
        // item retrieval. The default seems to be "no", except
        // for poslog. It requires these.
        //
    	/*System.out.println("inside readPLUItem JdbcPLUOperation.java");*/
        return readPLUItem(dataConnection, key, localeRequestor, storeID, false);
    }

    /**
     *
     * @param dataConnection
     * @param key
     * @param localeRequestor
     * @param storeID
     * @param retrieveStoreCoupons
     * @return
     * @throws DataException
     */
    public PLUItemIfc[] readPLUItem(JdbcDataConnection dataConnection, String key,
            LocaleRequestor localeRequestor, String storeID, boolean retrieveStoreCoupons) throws DataException
    {
        // KLM: Overloading this method to allow for store coupon line item retrieval.
        // The default seems to be "no", except for poslog. It requires these.
        return readPLUItem(dataConnection, key, storeID, retrieveStoreCoupons, new PLURequestor(), localeRequestor);
    }

    /**
     * Reads a PLUItem
     *
     * @param dataConnection
     * @param key
     * @param sqlLocale
     * @param storeID
     * @param retrieveStoreCoupons
     * @param fieldsToInclude
     * @return
     * @throws DataException
     * @deprecated as of 13.4.  Replaced by {@link #readPLUItemByPosItemID(JdbcDataConnection, String, String, boolean, PLURequestor, LocaleRequestor)}
     */
    public PLUItemIfc[] readPLUItem(JdbcDataConnection dataConnection, String key, String storeID,
            boolean retrieveStoreCoupons, PLURequestor pluRequestor, LocaleRequestor sqlLocale) throws DataException
    {
        return readPLUItemByPosItemID(dataConnection, key, storeID,retrieveStoreCoupons, pluRequestor, sqlLocale);
    }

    /**
     * Loads PluItems by a SQLParameterValue.  In general the SQLParameterValue should be for a column
     * in the PosIdentity table.
     *
     * @param dataConnection a connection to the database
     * @param key a SQLParameterValue to use in the query's where clause.
     * @paramstoreID the store ID.  If null then query is not restricted by store.
     * @param retrieveStoreCoupons load coupons flag
     * @param pluRequestor
     * @param sqlLocale the locale
     * @return An array of PLUItems
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    protected PLUItemIfc[] readPLUItemByKey(JdbcDataConnection dataConnection, SQLParameterValue key, String storeID,
            boolean retrieveStoreCoupons, PLURequestor pluRequestor, LocaleRequestor sqlLocale) throws DataException
    {
        List<SQLParameterIfc> qualifiers = new ArrayList<SQLParameterIfc>(2);
        qualifiers.add(key);

        if (storeID != null)
        {
            qualifiers.add(new SQLParameterValue(ALIAS_POS_IDENTITY, FIELD_RETAIL_STORE_ID, storeID));
        }

        PLUItemIfc[] items = selectPLUItem(dataConnection, qualifiers, pluRequestor, sqlLocale);

        // Process the result to filter out coupons if necessary. Do not use the
        // StoreCouponQualifier to weed out
        // store coupons. This may cause ItemNotFoundExceptions during the join
        // which will make any transactions
        // containing store coupons irretrievable for returns.

        /*
         * 27SEP07 Filtering out coupons does not allow the client to know when
         * coupons are part of a return transaction, or reject them when coupons
         * are attempted to be returned during a no-receipt return. Coupon plus
         * should be made ProhibitReturnFlag = true, if the domain logic prefers
         * them not to be returned. - CMG
         */

        // if (retrieveStoreCoupons == false && items.length > 0)
        // {
        // ArrayList itemList = new ArrayList();
        // for(int i=0; i<items.length; i++)
        // {
        // if(items[i].getItemClassification().getItemType() !=
        // ItemClassificationConstantsIfc.TYPE_STORE_COUPON)
        // {
        // itemList.add(items[i]);
        // }
        // }
        // int size = itemList.size();
        // // if there are no items left, throw a data exception
        // if (size == 0)
        // {
        // throw new DataException(DataException.WARNING,
        // "Store coupons can not be included in this lookup.");
        // }
        // items = (PLUItemIfc[]) itemList.toArray(new PLUItemIfc[size]);
        // }
        // since we are not filtering out coupons, configure any coupons we have
        for (int i = items.length - 1; i >= 0; i--)
        {
            if (items[i].isStoreCoupon())
                JdbcSCLUOperation.configureStoreCouponRules(items[i]);

        }

        return items;
    }


    /**
     * Loads PluItems by ItemID
     *
     * @param dataConnection a connection to the database
     * @param key the Item ID
     * @param storeID the store ID.  If null then query is not restricted by store.
     * @param retrieveStoreCoupons load coupons flag
     * @param pluRequestor
     * @param sqlLocale the locale
     * @return An array of PLUItems
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] readPLUItemByItemID(JdbcDataConnection dataConnection, String key, String storeID,
            boolean retrieveStoreCoupons, PLURequestor pluRequestor, LocaleRequestor sqlLocale) throws DataException
    {
        SQLParameterValue sqlParamVal = new SQLParameterValue(ALIAS_POS_IDENTITY, FIELD_ITEM_ID, key);
        return readPLUItemByKey(dataConnection, sqlParamVal, storeID, retrieveStoreCoupons, pluRequestor, sqlLocale);
    }

    /**
     * Loads PluItems by PosItemID
     *
     * @param dataConnection a connection to the database
     * @param key the PosItem ID
     * @param storeID the store ID.  If null then query is not restricted by store.
     * @param retrieveStoreCoupons load coupons flag
     * @param pluRequestor
     * @param sqlLocale the locale
     * @return An array of PLUItems
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] readPLUItemByPosItemID(JdbcDataConnection dataConnection, String key, String storeID,
            boolean retrieveStoreCoupons, PLURequestor pluRequestor, LocaleRequestor sqlLocale) throws DataException
    {
        SQLParameterValue sqlParamVal = new SQLParameterValue(ALIAS_POS_IDENTITY, FIELD_POS_ITEM_ID, key);
        return readPLUItemByKey(dataConnection, sqlParamVal, storeID,retrieveStoreCoupons, pluRequestor, sqlLocale);
    }

    /**
     * Loads PluItems by ItemNumber.
     *
     * @param dataConnection a connection to the database
     * @param key the ItemNumber. An ItemNumber can be either a ItemID or PosItemID
     * @param storeID the store ID.  If null then query is not restricted by store.
     * @param retrieveStoreCoupons load coupons flag
     * @param pluRequestor
     * @param sqlLocale the locale
     * @return An array of PLUItems
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] readPLUItemByItemNumber(JdbcDataConnection dataConnection, String key, String storeID,
            boolean retrieveStoreCoupons, PLURequestor pluRequestor, LocaleRequestor sqlLocale) throws DataException
    {
        //search by ItemID OR PosItemID
        List<SQLParameterIfc> orQualifiers = new ArrayList<SQLParameterIfc>(2);
        orQualifiers.add(new SQLParameterValue(ALIAS_POS_IDENTITY, FIELD_ITEM_ID, key));
        orQualifiers.add(new SQLParameterValue(ALIAS_POS_IDENTITY, FIELD_POS_ITEM_ID, key));

        List<SQLParameterIfc> andQualifiers = new ArrayList<SQLParameterIfc>(1);
        if (storeID != null)
        {
            // AND search by storeID
            andQualifiers.add(new SQLParameterValue(ALIAS_POS_IDENTITY, FIELD_RETAIL_STORE_ID, storeID));
        }

        PLUItemIfc[] items = selectPLUItem(dataConnection, andQualifiers, orQualifiers, pluRequestor, sqlLocale);

        //configure any coupons we have
        for (int i = items.length - 1; i >= 0; i--)
        {
            if (items[i].isStoreCoupon())
                JdbcSCLUOperation.configureStoreCouponRules(items[i]);

        }

        return items;
    }

    /**
     * Reads items from the POS Identity and Item tables.
     *
     * @param dataConnection a connection to the database
     * @param key the item lookup key
     * @param pluRequestor the plu requestor
     * @param locale the locale requestor
     * @return An array of PLUItems
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] readPLUItem(JdbcDataConnection dataConnection, String key,
            PLURequestor pluRequestor, LocaleRequestor locale) throws DataException
    {
        return selectPLUItem(dataConnection, getNotStoreCouponQualifiers(key), pluRequestor, locale);
    }

    /**
     * Returns a qualifier string to exclude Store Coupons from regular item
     * lookup.
     */
    protected List<SQLParameterIfc> getNotStoreCouponQualifiers(String key)
    {
        List<SQLParameterIfc> qualifiers = new ArrayList<SQLParameterIfc>(2);
        qualifiers.add(new SQLParameterValue(ALIAS_POS_IDENTITY, FIELD_POS_ITEM_ID, key));
        qualifiers.add(new SQLParameterValue(ALIAS_ITEM + "." + FIELD_ITEM_TYPE_CODE + " != ?", ITEM_TYPE_STORE_COUPON));
        return qualifiers;
    }

    /**
     * Selects items from the POS Identity and Item tables.
     *
     * @param dataConnection a connection to the database
     * @param qualifier a qualifier for item lookup
     * @param locale the LocaleRequestor object
     * @return An array of PLUItems with associated advanced pricing rules
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] selectPLUItem(JdbcDataConnection dataConnection, List<SQLParameterIfc> qualifiers, LocaleRequestor locale)
            throws DataException
    {
    	
        return selectPLUItem(dataConnection, qualifiers, new PLURequestor(), locale);
    }

    /**
     * Selects items from the POS Identity and Item tables.
     *
     * @param dataConnection a connection to the database
     * @param andQualifiers a list of qualifiers used in WHERE clause -- the qualifiers in the list will be seperated by "AND".
     * @param orQualifiers a list of qualifiers used in WHERE clause -- the qualifiers in the list will be seperated by "OR".
     * @param locale the LocaleRequestor object
     * @return An array of PLUItems with associated advanced pricing rules
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] selectPLUItem(JdbcDataConnection dataConnection, List<SQLParameterIfc> andQualifiers, List<SQLParameterIfc> orQualifiers, LocaleRequestor locale)
        throws DataException
    {
        return selectPLUItem(dataConnection, andQualifiers, orQualifiers, new PLURequestor(), locale);
    }

    /**
     * Selects items from the POS Identity and Item tables.
     *
     * @param dataConnection a connection to the database
     * @param qualifiers a list of qualifiers used in the WHERE clause -- the qualifiers in the list will be seperated by "AND".
     * @param pluRequestor the plu requestor
     * @param sqlLocale the LocaleRequestor object
     * @return An array of PLUItems with associated advanced pricing rules
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] selectPLUItem(JdbcDataConnection dataConnection, List<SQLParameterIfc> qualifiers,
            PLURequestor pluRequestor, LocaleRequestor sqlLocale) throws DataException
    {
        return selectPLUItem(dataConnection, qualifiers, new ArrayList<SQLParameterIfc>(0), pluRequestor,sqlLocale);
    }


    /**
     * Selects items from the POS Identity and Item tables.
     *
     * @param dataConnection a connection to the database
     * @param andQualifiers a list of qualifiers used in WHERE clause -- the qualifiers in the list will be seperated by "AND".
     * @param orQualifiers a list of qualifiers used in WHERE clause -- the qualifiers in the list will be seperated by "OR".
     * @param pluRequestor the plu requestor
     * @param sqlLocale the LocaleRequestor object
     * @return An array of PLUItems with associated advanced pricing rules
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] selectPLUItem(JdbcDataConnection dataConnection, List<SQLParameterIfc> andQualifiers, List<SQLParameterIfc> orQualifiers,
            PLURequestor pluRequestor, LocaleRequestor sqlLocale) throws DataException
    {
    	logger.info("inside selectPLUItem");
        if (usesWildcards(andQualifiers) || usesWildcards(orQualifiers))
        {
            // wildcards were used in the query
            // call selectPLUItems() to retrieve all the matching items
            return selectPLUItems(dataConnection, andQualifiers, orQualifiers, pluRequestor, sqlLocale);
        }

        SQLSelectStatement sql = buildSelectPLUItemSQL(andQualifiers, orQualifiers, pluRequestor, sqlLocale);

        // create a reference for the item
        ArrayList<PLUItemIfc> list = new ArrayList<PLUItemIfc>();

        try
        {
            // execute the query and get the result set
            ResultSet rs = execute(dataConnection, sql);

            while (rs.next())
            {
                PLUItemIfc pluItem = createPLUItem(rs);
                list.add(pluItem);
            }

            JdbcSelectPriceChange selectPriceChange = new JdbcSelectPriceChange();
            for (Iterator<PLUItemIfc> iter = list.iterator(); iter.hasNext();)
            {
                PLUItemIfc pluItem = iter.next();

                // populate price changes
                if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.Price))
                {
                    PriceChangeIfc[] changes = selectPriceChange.readPermanentPriceChanges(dataConnection, pluItem,
                            Calendar.getInstance()); // this is not timezone safe
                    pluItem.setPermanentPriceChanges(changes);
                    changes = selectPriceChange.readAllTemporaryPriceChanges(dataConnection, pluItem,
                            Calendar.getInstance()); // this is not timezone safe
                    pluItem.setTemporaryPriceChangesAndTemporaryPriceChangesForReturns(changes);
                }

                // update gift card amounts
                if (pluItem instanceof GiftCardPLUItemIfc)
                {
                    GiftCardPLUItemIfc gci = (GiftCardPLUItemIfc)pluItem;
                    gci.getGiftCard().setReqestedAmount(pluItem.getSellingPrice());
                }

                if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.Planogram))
                {
                    applyPlanogramIDs(dataConnection, pluItem);
                }

                if (pluItem.getItemClassification().getItemType() == ItemClassificationConstantsIfc.TYPE_STOCK)
                {
                    if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.StockItem))
                    {
                        selectStockItem(dataConnection, pluItem.getItemID(), pluItem, sqlLocale);
                    }
                }
                if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.LocalizedDescription))
                {
                    applyLocaleDependentDescriptions(dataConnection, pluItem, sqlLocale);
                }
                if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.ItemImage))
                {
                    applyItemImages(dataConnection, pluItem, sqlLocale);
                }
                //Added below condition to fix POS-243 by Monica on 23/09/2016
                if (!(pluItem instanceof GiftCardPLUItemIfc))
                {

                    GDYNPLUItemIfc gdynPluItem = (GDYNPLUItemIfc) pluItem;
                    logger.info("Before calling applyManufacture");
                    applyManufacturer(dataConnection, gdynPluItem, andQualifiers, sqlLocale);
                }                
                else
                {
                	applyManufacturer(dataConnection, pluItem, andQualifiers, sqlLocale);
                }
                
            	          	
            
                // this method is called for regular item search from the sale screen
                
                
              
                
                // Set discount rules if item is eligible and rules have been
                // requested by the caller
                // 24OCT07 Even if item is non-discountable, it is allowed to be
                // a source in a rule, so always get the rules. CR29301
                if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.AdvancedPricingRules)) // && (pluItem.isDiscountEligible() ||
                                         // pluItem.getItem().isStoreCoupon()))
                {
                    applyAdvancedPricingRules(dataConnection, pluItem, sqlLocale);
                }

                // and components to the ItemKit
                if (pluItem.isKitHeader())
                {
                    if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.KitComponents))
                    {
                        KitComponentIfc[] kitComps = selectKitComponents(dataConnection, pluItem.getItemID(), sqlLocale);
                        ((ItemKitIfc)pluItem).addComponentItems(kitComps);
                    }
                }

                // 27192
                if (pluRequestor == null || pluRequestor.containsRequestType(PLURequestor.RequestType.POSDepartment))
                {
                    getDepartmentByDeptID(dataConnection, pluItem, sqlLocale);
                }
            }
        }
        catch (DataException de)
        {
            logger.warn(de.toString());
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "PLUItem lookup");
            throw new DataException(DataException.SQL_ERROR, "PLUItem lookup", se);
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "PLUItem lookup", e);
        }

        if (list.size() == 0)
        {
            throw new DataException(DataException.NO_DATA,
                    "No PLU was found processing the result set in JdbcPLUOperation.");
        }

        return list.toArray(new PLUItemIfc[list.size()]);
    }

    /**
     * @param dataConnection
     * @param qualifier
     * @param selectDiscountRules
     * @param locale
     * @return
     * @throws DataException
     */
    public PLUItemIfc[] selectPLUItems(JdbcDataConnection dataConnection, List<SQLParameterIfc> qualifiers,
            PLURequestor pluRequestor, LocaleRequestor locale) throws DataException
    {
        return selectPLUItems(dataConnection, qualifiers, null,pluRequestor, locale);
    }


    /**
     * Selects items from the POS Identity and Item tables.
     *
     * @param dataConnection a connection to the database
     * @param andQualifiers a list of qualifiers used in WHERE clause -- the qualifiers in the list will be seperated by "AND".
     * @param orQualifiers a list of qualifiers used in WHERE clause -- the qualifiers in the list will be seperated by "OR".
     * @param pluRequestor the plu requestor
     * @param sqlLocale the LocaleRequestor object
     * @return An array of PLUItems with associated advanced pricing rules
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public PLUItemIfc[] selectPLUItems(JdbcDataConnection dataConnection,
            List<SQLParameterIfc> andQualifiers, List<SQLParameterIfc> orQualifiers,
            PLURequestor pluRequestor, LocaleRequestor locale) throws DataException
    {
        SQLSelectStatement sql = new SQLSelectStatement();

        // add tables
        sql.addTable(TABLE_POS_IDENTITY, ALIAS_POS_IDENTITY);
        sql.addTable(TABLE_ITEM, ALIAS_ITEM);

        // add columns
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_POS_ITEM_ID);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_ID);

        // add qualifiers
        sql.addJoinQualifier(ALIAS_POS_IDENTITY, FIELD_ITEM_ID, ALIAS_ITEM, FIELD_ITEM_ID);

        //add AND qualifiers
        sql.addQualifiers(andQualifiers);

        //add OR qualifiers
        sql.addOrQualifiers(orQualifiers);

        // perform the query
        ArrayList<String> results = new ArrayList<String>();
        try
        {
            ResultSet rs = execute(dataConnection, sql);

            while (rs.next())
            {
                int index = 0;
                String posItemID = getSafeString(rs, ++index);
                getSafeString(rs, ++index); // itemID
                results.add(posItemID);
            }
            rs.close();
        }
        catch (DataException de)
        {
            logger.warn(de.toString());
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "PLUItem lookup");
            throw new DataException(DataException.SQL_ERROR, "PLUItem lookup", se);
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "PLUItem lookup", e);
        }

        if (results.isEmpty())
        {
            throw new DataException(DataException.NO_DATA,
                    "No PLU was found processing the result set in JdbcPLUOperation.");
        }

        // for each selected item id, read the PLUItem information
        ArrayList<PLUItemIfc> items = new ArrayList<PLUItemIfc>();
        Iterator<String> i = results.iterator();

        // make sure locale is there
        if (locale == null)
        {
            locale = new LocaleRequestor(LocaleMap.getLocale(LocaleMap.DEFAULT));
        }

        while (i.hasNext())
        {
            items.add(readPLUItem(dataConnection, i.next(), pluRequestor, locale)[0]);
        }

        // convert results to array and return
        PLUItemIfc[] itemArray = new PLUItemIfc[items.size()];
        items.toArray(itemArray);
        return (itemArray);
    }

    /**
     * Selects kit component items from the database.
     *
     * @param dataConnection a connection to the database
     * @param int kitCode
     * @param sqlLocale locale being used in SQL Query
     * @param ItemKitIfc the ItemKit used to hold the KitComponents
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public KitComponentIfc[] selectKitComponents(JdbcDataConnection dataConnection, String kitCode,
            LocaleRequestor localeRequestor) throws DataException
    {
        SQLSelectStatement sql = new SQLSelectStatement();

        // add tables
        sql.addTable(TABLE_ITEM_COLLECTION, ALIAS_ITEM_COLLECTION);

        // add columns
        sql.addColumn(ALIAS_ITEM_COLLECTION, FIELD_ITEM_COLLECTION_ID);
        sql.addColumn(ALIAS_ITEM_COLLECTION, FIELD_ITEM_COLLECTION_MEMBER_COLLECTION);
        sql.addColumn(ALIAS_ITEM_COLLECTION, FIELD_ITEM_PER_ASSEMBLY_COUNT);

        // add kit code as qualifier
        sql.addQualifier(new SQLParameterValue(ALIAS_ITEM_COLLECTION, FIELD_ITEM_COLLECTION_ID, kitCode));

        sql.addOrdering(ALIAS_ITEM_COLLECTION, FIELD_ITEM_COLLECTION_MEMBER_COLLECTION);

        ArrayList<KitComponentIfc> selectedComponents = new ArrayList<KitComponentIfc>();
        ArrayList<KitComponentIfc> kitComponents = new ArrayList<KitComponentIfc>();

        try
        {
            ResultSet rs = execute(dataConnection, sql);

            // temporary handles for creating KitComponents
            KitComponentIfc component = null;
            PLUItemIfc plu = null;

            while (rs.next())
            {
                // parse the result set
                int index = 0;
                String itemKitID = getSafeString(rs, ++index);
                String itemID = getSafeString(rs, ++index);
                Float quantityF = new Float(rs.getFloat(++index));
                int quantity = quantityF.intValue();

                // create a component item
                component = DomainGateway.getFactory().getKitComponentInstance();
                // set the component specific attributes from the table
                component.setItemKitID(itemKitID);
                component.setItemID(itemID);
                component.setQuantity(BigDecimal.valueOf(quantity));

                // add the component to the selected results
                component.setKitComponent(true);
                selectedComponents.add(component);
            }

            Iterator<KitComponentIfc> i = selectedComponents.iterator();
            while (i.hasNext())
            {
                // do the plu lookup and set each component's values to those of
                // its plu
                component = i.next();
                plu = readPLUItemByItemID(dataConnection, component.getItemID(), null, false, new PLURequestor(), localeRequestor)[0];
                plu.setCloneAttributes((PLUItem)component);
                // use the pricing rules referenced by the plu item
                component.setAdvancedPricingRules(plu.getAdvancedPricingRules());

                int quantity = component.getQuantity().intValue();
                component.setQuantity(Util.I_BIG_DECIMAL_ONE);

                for (int count = 0; count < quantity; count++)
                {
                    kitComponents.add(component);
                }
            }
        }
        catch (DataException de)
        {
            logger.warn(de.toString());
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "KitComponent lookup");
            throw new DataException(DataException.SQL_ERROR, "KitComponent lookup", se);
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "KitComponent lookup", e);
        }

        if (selectedComponents.isEmpty())
        {
            throw new DataException(DataException.NO_DATA,
                    "No kit components found when processing the result set in JdbcPLUOperation.");
        }

        KitComponentIfc[] values = new KitComponentIfc[kitComponents.size()];
        kitComponents.toArray(values);
        return values;
    }

    /**
     * Selects advanced pricing rules from the database
     *
     * @param dataConnection a connection to the database
     * @param pluItem the PLU Item
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public AdvancedPricingRuleIfc selectAdvancedPricingRule(JdbcDataConnection dataConnection,
            AdvancedPricingRuleIfc advRule, LocaleRequestor sqlLocale) throws DataException
    {
        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.selectAdvancedPricingRule() begins.");
        AdvancedPricingRuleIfc rule = selectDiscount(dataConnection, advRule.getRuleID(), advRule
                .getSourceComparisonBasis(), new Hashtable<String, AdvancedPricingRuleIfc>(), sqlLocale);

        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.selectAdvancedPricingRule() rule=" + rule);
        return rule;
    }

    /**
     * Selects the department for the given PLUItem.
     *
     * @param dataConnection a connection to the database
     * @param pluItem the PLU Item
     */
    public void getDepartmentByDeptID(JdbcDataConnection dataConnection, PLUItemIfc pluItem,
            LocaleRequestor sqlLoclae)
    {
        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.getDepartment() begins");
        JdbcReadDepartment readDepartment = new JdbcReadDepartment();
        String departmentID = pluItem.getDepartmentID();
       // System.out.println("From JdbcPLUOperation departmentID "+departmentID);
        logger.info("From JdbcPLUOperation departmentID "+departmentID);
        try
        {
            DepartmentIfc department = readDepartment.selectDepartmentByDeptID(dataConnection, departmentID, sqlLoclae);
            pluItem.setDepartment(department);
        }
        catch (DataException de)
        {
            logger.error("Could not not read department for item: " + pluItem.getItemID() + " with department ID: "
                    + departmentID, de);
        }
        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.getDepartment() ends");
    }

    /**
     * Selects all the group advanced pricing rules
     *
     * @param dataConnection
     * @param pluItem
     * @param sqlLocale
     * @return
     * @throws DataException
     */
    protected AdvancedPricingRuleIfc[] selectGroupDiscounts(JdbcDataConnection dataConnection,
            PLUItemIfc pluItem, LocaleRequestor sqlLocale) throws DataException
    {

        ArrayList<AdvancedPricingRuleIfc> list = new ArrayList<AdvancedPricingRuleIfc>();
        ResultSet rs = null;
        SQLSelectStatement sql = new SQLSelectStatement();
        try

        {
            sql = buildGroupSQL(DiscountRuleConstantsIfc.COMPARISON_BASIS_ITEM_ID, pluItem);
            rs = execute(dataConnection, sql);
            // do a lookup for all the items that belong to the same rule
            Collection<AdvancedPricingRuleIfc> itemComparisonRules = readGroupAdvancedPricingRules(rs,
                    DiscountRuleConstantsIfc.COMPARISON_BASIS_ITEM_ID);

            for (AdvancedPricingRuleIfc rule : itemComparisonRules)
            {
                getLocalizedRuleData(dataConnection, rule, sqlLocale);
                if (rule.getThresholdTypeCode() == DiscountRuleConstantsIfc.THRESHOLD_QUANTITY)
                {
                    sql = buildItemSQL(Integer.parseInt(rule.getRuleID()));
                    rs = execute(dataConnection, sql);
                    rule = readSources(rs, rule);

                }
            }
            list.addAll(itemComparisonRules);

            String merchClassCodes = pluItem.getMerchandiseCodesString();
            if (merchClassCodes != null && !"''".equals(merchClassCodes))
            {
                sql = buildGroupSQL(DiscountRuleConstantsIfc.COMPARISON_BASIS_MERCHANDISE_CLASS, pluItem);
                rs = execute(dataConnection, sql);
                Collection<AdvancedPricingRuleIfc> classComparisonRules = readGroupAdvancedPricingRules(rs,
                        DiscountRuleConstantsIfc.COMPARISON_BASIS_MERCHANDISE_CLASS);
                for (AdvancedPricingRuleIfc rule : classComparisonRules)
                {
                    getLocalizedRuleData(dataConnection, rule, sqlLocale);
                    if (rule.getThresholdTypeCode() == DiscountRuleConstantsIfc.THRESHOLD_QUANTITY)
                    {
                        sql = buildClassSQL(Integer.parseInt(rule.getRuleID()));
                        rs = execute(dataConnection, sql);
                        rule = readSources(rs, rule);
                    }
                }
                list.addAll(classComparisonRules);
            }

            if (pluItem.getDepartmentID() != null)
            {
                sql = buildGroupSQL(DiscountRuleConstantsIfc.COMPARISON_BASIS_DEPARTMENT_ID, pluItem);
                rs = execute(dataConnection, sql);
                Collection<AdvancedPricingRuleIfc> depComparisonRules = readGroupAdvancedPricingRules(rs,
                        DiscountRuleConstantsIfc.COMPARISON_BASIS_DEPARTMENT_ID);
                for (AdvancedPricingRuleIfc rule : depComparisonRules)
                {
                    getLocalizedRuleData(dataConnection, rule, sqlLocale);
                    if (rule.getThresholdTypeCode() == DiscountRuleConstantsIfc.THRESHOLD_QUANTITY)
                    {
                        sql = buildDepartmentSQL(Integer.parseInt(rule.getRuleID()));
                        rs = execute(dataConnection, sql);
                        rule = readSources(rs, rule);
                    }
                }
                list.addAll(depComparisonRules);
            }
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "AdvancedPricing lookup");
            throw new DataException(DataException.SQL_ERROR, "Advanced Pricing lookup", se);
        }
        finally
        {
            if (rs != null)
            {
                try
                {
                    rs.close();

                }
                catch (SQLException se)

                {
                    dataConnection.logSQLException(se, "Advanced Pricing lookup -- Could not close result handle");

                }

            }

        }
        AdvancedPricingRuleIfc[] rules = new AdvancedPricingRuleIfc[list.size()];
        list.toArray(rules);
        return rules;
    }

    /**
     * Reads all the localized data for a discount rule
     *
     * @param dataConnection
     * @param rule
     * @param locale
     * @throws SQLException
     * @throws DataException
     */
    public void getLocalizedRuleData(JdbcDataConnection dataConnection, AdvancedPricingRuleIfc rule,
            LocaleRequestor locale) throws SQLException, DataException
    {
        SQLSelectStatement sql = buildLocalizedRuleSQL(rule, locale);
        ResultSet rs = execute(dataConnection, sql);
        rule = readLocalizedRule(rs, rule);
        // for discount rules, the reason code text is the localized rule name
        rule.getReason().setText(rule.getLocalizedNames());
        rs.close();
    }

    /**
     * Selects Group Discount based on the rule id.
     *
     * @param dataConnection a connection to the database
     * @param pluItem the PLU Item
     * @return AdvancedPricingRuleIfc[]
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    protected AdvancedPricingRuleIfc selectDiscount(JdbcDataConnection dataConnection, String ruleID,
            int comparisonBasis, Hashtable<String,AdvancedPricingRuleIfc> existingRules, LocaleRequestor sqlLocale) throws DataException
    {

        AdvancedPricingRuleIfc rule = null;
        ResultSet rs = null;

        SQLSelectStatement sql = new SQLSelectStatement();
        try
        {
            // search for group discount
            sql = buildGroupRuleSQL(comparisonBasis, ruleID);
            rs = execute(dataConnection, sql);
            List<AdvancedPricingRuleIfc> rules = readGroupAdvancedPricingRules(rs, comparisonBasis);

            // organize existing rules for next deal discount.
            for (Iterator<AdvancedPricingRuleIfc> iter = rules.iterator(); iter.hasNext();)
            {
                rule = iter.next();
                getLocalizedRuleData(dataConnection, rule, sqlLocale);
                existingRules.put(rule.getRuleID(), rule);
            }

            // search for deal discount
            sql = buildDealRuleSQL(comparisonBasis, ruleID);
            rs = execute(dataConnection, sql);
            rules.addAll(readDealAdvancedPricingRules(rs, comparisonBasis, existingRules));

            // reset result
            rule = null;

            if (!rules.isEmpty())
            {
                // there is only one rule retrieved
                for (Iterator<AdvancedPricingRuleIfc> i = rules.iterator(); i.hasNext();)
                {
                    // there is only one
                    AdvancedPricingRuleIfc advancedPricingRule = i.next();
                    if (ruleID.equals(advancedPricingRule.getRuleID()))
                    {
                        rule = advancedPricingRule;
                        getLocalizedRuleData(dataConnection, rule, sqlLocale);
                        break;
                    }
                }// end for (Iterator i = rules.iterator(); i.hasNext(); )

                if (comparisonBasis == DiscountRuleConstantsIfc.COMPARISON_BASIS_ITEM_ID)
                {
                    if (rule.getThresholdTypeCode() == DiscountRuleConstantsIfc.THRESHOLD_QUANTITY)
                    {
                        // do a lookup for all the items that belong to the same
                        // rule
                        sql = buildItemSQL(Integer.parseInt(ruleID));
                        rs = execute(dataConnection, sql);
                        rule = readSources(rs, rule);

                    }
                }
                if (comparisonBasis == DiscountRuleConstantsIfc.COMPARISON_BASIS_DEPARTMENT_ID)
                {
                    if (rule.getThresholdTypeCode() == DiscountRuleConstantsIfc.THRESHOLD_QUANTITY)
                    {
                        // do a lookup for all departments that belong to the
                        // same rule
                        sql = buildDepartmentSQL(Integer.parseInt(ruleID));
                        rs = execute(dataConnection, sql);
                        rule = readSources(rs, rule);
                    }
                }
                if (comparisonBasis == DiscountRuleConstantsIfc.COMPARISON_BASIS_MERCHANDISE_CLASS)
                {
                    if (rule.getThresholdTypeCode() == DiscountRuleConstantsIfc.THRESHOLD_QUANTITY)
                    {
                        // do a lookup for all departments that belong to the
                        // same rule
                        sql = buildClassSQL(Integer.parseInt(ruleID));
                        rs = execute(dataConnection, sql);
                        rule = readSources(rs, rule);
                    }
                }
            }// end if (!rules.isEmpty()

        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "AdvancedPricing lookup");
            throw new DataException(DataException.SQL_ERROR, "Advanced Pricing lookup", se);
        }
        finally
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                }
                catch (SQLException se)
                {
                    dataConnection.logSQLException(se, "Advanced Pricing lookup -- Could not close result handle");
                }
            }

        }

        return rule;
    }

    /**
     * Executes the SQL Statement.
     *
     * @param dataConnection a connection to the database
     * @param sql the SQl statement
     * @param int id comparison basis type
     * @return ArrayList of Advanced Pricing rules.
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    protected static ResultSet execute(JdbcDataConnection dataConnection, SQLSelectStatement sql) throws DataException
    {
        ResultSet rs;
        String sqlString = sql.getSQLString();
        dataConnection.execute(sqlString, sql.getParameterValues());
        rs = (ResultSet)dataConnection.getResult();
        return rs;
    }

    /**
     * Builds the sql for retrieving the items that belong to the same rule .
     *
     * @param the rule id
     * @return SQLStatement
     */
    protected SQLSelectStatement buildItemSQL(int id)
    {
        SQLSelectStatement sql = new SQLSelectStatement();
        // add tables
        sql.addTable(TABLE_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY, ALIAS_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY);
        // add columns
        sql.addColumn(ALIAS_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_ITEM_ID);
        sql.addColumn(ALIAS_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_THR_QUANTITY);
        sql.addQualifier(new SQLParameterValue(FIELD_PRICE_DERIVATION_RULE_ID, id));

        return sql;

    }

    /**
     * Builds select PLU item SQL. Example SQL query follows.
     * <p><blockquote><pre>
     * SELECT POSID.ID_ITM_POS, POSID.ID_ITM, POSID.FL_KY_PRH_QTY,
     *      POSID.FL_RTN_PRH, POSID.QU_UN_BLK_MNM, POSID.QU_UN_BLK_MXM,
     *      POSID.FL_CPN_ALW_MULTY, POSID.FL_ENTR_PRC_RQ, POSID.FL_CPN_ELNTC,
     *      POSID.FL_CPN_RST, POSID.FL_MDFR_RT_PRC, POSID.FL_SPO_ITM,
     *      POSID.FL_DSC_EM_ALW, RSI.RP_PRC_CMPR_AT_SLS, RSI.IDN_SLS_AG_RST,
     *      ITM.DE_ITM, ITM.ID_GP_TX, ITM.LU_EXM_TX, ITM.FL_ITM_SZ_REQ,
     *      ITM.ID_DPT_POS, ITM.FL_ITM_DSC, ITM.FL_ITM_DSC_DMG,
     *      ITM.FL_ITM_RGSTRY, ITM.LU_HRC_MR_LV, ITM.FL_AZN_FR_SLS,
     *      ITM.LU_KT_ST, ITM.FL_ITM_SBST_IDN, ITM.TY_ITM, ITM.ID_MRHRC_GP,
     *      PA_MF.NM_MF, ITM.ID_MF, POSID.ID_STR_RT, ITM.ID_STRC_MR_CD0,
     *      ITM.ID_STRC_MR_CD1, ITM.ID_STRC_MR_CD2, ITM.ID_STRC_MR_CD3,
     *      ITM.ID_STRC_MR_CD4, ITM.ID_STRC_MR_CD5, ITM.ID_STRC_MR_CD6,
     *      ITM.ID_STRC_MR_CD7, ITM.ID_STRC_MR_CD8, ITM.ID_STRC_MR_CD9
     * FROM ID_IDN_PS POSID
     * JOIN AS_ITM_RTL_STR RSI ON RSI.ID_ITM = POSID.ID_ITM
     *      AND RSI.ID_STR_RT = POSID.ID_STR_RT
     * JOIN AS_ITM ITM ON  ITM.ID_ITM = POSID.ID_ITM
     * LEFT JOIN PA_MF ON  ITM.ID_MF = PA_MF.ID_MF
     * WHERE POSID.ID_ITM_POS = '64122457440'
     * AND POSID.ID_STR_RT = '00692'
     * </pre></blockquote>
     *
     * @param id the type of the advanced pricing rule.
     * @return SQLSelectStatement
     */
    protected SQLSelectStatement buildSelectPLUItemSQL(List<SQLParameterIfc> qualifiers, PLURequestor pluRequestor,
            LocaleRequestor sqlLocale)
    {
        return buildSelectPLUItemSQL(qualifiers, null, pluRequestor, sqlLocale);
    }

    /**
     * Builds select PLU item SQL with configured using "AND"
     * and "OR" SQLParameter Qualifier lists.
     *
     * @param andQualifiers a list of qualifiers used in WHERE clause -- the qualifiers in the list will be seperated by "AND".
     * @param orQualifiers a list of qualifiers used in WHERE clause -- the qualifiers in the list will be seperated by "OR".
     * @param pluRequestor not used
     * @param sqlLocale not used
     * @return SQLSelectStatement
     */
    protected SQLSelectStatement buildSelectPLUItemSQL(List<SQLParameterIfc> andQualifiers, List<SQLParameterIfc> orQualifiers,
                                                              PLURequestor pluRequestor, LocaleRequestor sqlLocale)
    {
    	logger.info("--inside buildSelectPLUItemSQL--");
        SQLSelectStatement sql = new SQLSelectStatement();

        // add tables
        sql.addTable(TABLE_POS_IDENTITY, ALIAS_POS_IDENTITY);
        // sql.addTable(TABLE_ITEM_IMAGE,ALIAS_IMAGE);

        // add columns
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_POS_ITEM_ID);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_ID);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_QUANTITY_KEY_PROHIBIT_FLAG);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_PROHIBIT_RETURN_FLAG);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_POS_ITEM_MINIMUM_SALE_UNIT_COUNT);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_POS_ITEM_MAXIMUM_SALE_UNIT_COUNT);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_ALLOW_COUPON_MULTIPLY_FLAG);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_PRICE_ENTRY_REQUIRED_FLAG);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_ELECTRONIC_COUPON_FLAG);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_COUPON_RESTRICTED_FLAG);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_PRICE_MODIFIABLE_FLAG);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_SPECIAL_ORDER_ELIGIBLE);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_EMPLOYEE_DISCOUNT_ALLOWED_FLAG);
        sql.addColumn(ALIAS_RETAIL_STORE_ITEM + "." + FIELD_COMPARE_AT_SALE_UNIT_RETAIL_PRICE_AMOUNT);
        sql.addColumn(ALIAS_RETAIL_STORE_ITEM + "." + FIELD_SALE_AGE_RESTRICTION_ID);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_TAX_GROUP_ID);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_TAX_EXEMPT_CODE);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_SIZE_REQUIRED_FLAG);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_POS_DEPARTMENT_ID);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_DISCOUNT_FLAG);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_DAMAGE_DISCOUNT_FLAG);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_REGISTRY_FLAG);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_MERCHANDISE_HIERARCHY_LEVEL_CODE);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_AUTHORIZED_FOR_SALE_FLAG);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_KIT_SET_CODE);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_SUBSTITUTE_IDENTIFIED_FLAG);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_TYPE_CODE);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_MERCHANDISE_HIERARCHY_GROUP_ID);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_ITEM_MANUFACTURER_ID);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_MANUFACTURER_UPC_ITEM_ID);
        sql.addColumn(ALIAS_POS_IDENTITY + "." + FIELD_RETAIL_STORE_ID);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_MERCHANDISE_CLASSIFICATION_CODE0);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_MERCHANDISE_CLASSIFICATION_CODE1);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_MERCHANDISE_CLASSIFICATION_CODE2);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_MERCHANDISE_CLASSIFICATION_CODE3);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_MERCHANDISE_CLASSIFICATION_CODE4);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_MERCHANDISE_CLASSIFICATION_CODE5);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_MERCHANDISE_CLASSIFICATION_CODE6);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_MERCHANDISE_CLASSIFICATION_CODE7);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_MERCHANDISE_CLASSIFICATION_CODE8);
        sql.addColumn(ALIAS_ITEM + "." + FIELD_ITEM_MERCHANDISE_CLASSIFICATION_CODE9);
//        sql.addColumn(ALIAS_IMAGE + "." + FIELD_ITEM_IMAGE_LOCATION);
//        sql.addColumn(ALIAS_IMAGE + "." + FIELD_ITEM_FULL_IMAGE_NAME);
//        sql.addColumn(ALIAS_IMAGE + "." + FIELD_ITEM_THUMB_BLOB);
//        sql.addColumn(ALIAS_IMAGE + "." + FIELD_ITEM_FULL_BLOB);

        // add joins
        sql.addOuterJoinQualifier(" JOIN " + TABLE_RETAIL_STORE_ITEM + " " + ALIAS_RETAIL_STORE_ITEM +
            " ON " + ALIAS_RETAIL_STORE_ITEM + "." + FIELD_ITEM_ID +
            " = " + ALIAS_POS_IDENTITY + "." + FIELD_ITEM_ID +
            " AND " + ALIAS_RETAIL_STORE_ITEM + "." + FIELD_RETAIL_STORE_ID +
            " = " + ALIAS_POS_IDENTITY + "." + FIELD_RETAIL_STORE_ID);

        // join POS Identity and Item tables using the given qualifier
        sql.addOuterJoinQualifier("", /* JOIN */ TABLE_ITEM + " " + ALIAS_ITEM,
            /* ON */ ALIAS_ITEM, FIELD_ITEM_ID, /* = */  ALIAS_POS_IDENTITY, FIELD_ITEM_ID);

//        sql.addOuterJoinQualifier("LEFT", JOIN TABLE_ITEM_IMAGE + " " +
//                ALIAS_IMAGE, ON ALIAS_POS_IDENTITY, FIELD_POS_ITEM_ID, = ALIAS_IMAGE,
//                FIELD_ITEM_ID);

        sql.addQualifiers(andQualifiers);
        sql.addOrQualifiers(orQualifiers);
        return sql;
    }

    /**
     * Build the SQL query for selcting item images.
     *
     * @param itemId
     * @param sqlLocale
     * @return
     */
    protected SQLSelectStatement buildSelectItemImageSQL(String itemId, LocaleRequestor sqlLocale)
    {
        SQLSelectStatement sql = new SQLSelectStatement();

        sql.addTable(TABLE_ITEM_IMAGE, ALIAS_IMAGE);
        sql.addColumn(ALIAS_IMAGE + "." + FIELD_ITEM_IMAGE_LOCATION);
        sql.addColumn(ALIAS_IMAGE + "." + FIELD_ITEM_IMAGE_BLOB);
        sql.addQualifier(new SQLParameterValue(ALIAS_IMAGE, FIELD_ITEM_IMAGE_ID, itemId));
        return sql;
    }

    /**
     * Builds select PLU item manufacturer SQL.
     *
     * @param id the type of the advanced pricing rule.
     * @return SQLSelectStatement
     * @since 13.1
     */
    protected SQLSelectStatement buildLocalizedItemManufactureSQL(List<SQLParameterIfc> qualifiers, LocaleRequestor sqlLocale)
    {
        SQLSelectStatement sql = new SQLSelectStatement();

        // add tables
        sql.addTable(TABLE_ITEM_MANUFACTURER_I18N);

        // add columns
        sql.addColumn(TABLE_ITEM_MANUFACTURER_I18N, FIELD_LOCALE);
        sql.addColumn(TABLE_ITEM_MANUFACTURER_I18N, FIELD_ITEM_MANUFACTURER_NAME);

        // join the item table if we don't already know the manufacturer's id
        if (qualifiers != null && qualifiers.toString().indexOf(FIELD_ITEM_MANUFACTURER_ID) == -1)
        {
            sql.addOuterJoinQualifier("", TABLE_POS_IDENTITY + " " + ALIAS_POS_IDENTITY,
                    ALIAS_POS_IDENTITY, FIELD_ITEM_MANUFACTURER_ID,
                    TABLE_ITEM_MANUFACTURER_I18N, FIELD_ITEM_MANUFACTURER_ID);

            sql.addOuterJoinQualifier(" JOIN " + TABLE_RETAIL_STORE_ITEM +
                    " ON " + ALIAS_POS_IDENTITY + "." + FIELD_ITEM_ID +
                    " = " + TABLE_RETAIL_STORE_ITEM + "." + FIELD_ITEM_ID +
                    " AND " + ALIAS_POS_IDENTITY + "." + FIELD_RETAIL_STORE_ID +
                    " = " + TABLE_RETAIL_STORE_ITEM + "." + FIELD_RETAIL_STORE_ID);

            sql.addOuterJoinQualifier("", TABLE_ITEM + " " + ALIAS_ITEM,
                    ALIAS_POS_IDENTITY, FIELD_ITEM_ID,
                    ALIAS_ITEM, FIELD_ITEM_ID);

        }

        // add qualifier for locale
        Set<Locale> bestMatches = LocaleMap.getBestMatch("", sqlLocale.getLocales());
        sql.addQualifier(TABLE_ITEM_MANUFACTURER_I18N + "." + FIELD_LOCALE + " "
                + JdbcDataOperation.buildINClauseString(bestMatches));

        // add passed in qualifiers
        sql.addQualifiers(qualifiers);

        return sql;
    }

    protected void addLocalizedItemManufacture(ResultSet rs, PLUItemIfc pluItem) throws SQLException,
            DataException
    {
        Locale locale = LocaleUtilities.getLocaleFromString(getSafeString(rs, 1));
        String manufacturer = getSafeString(rs, 2);
        pluItem.setManufacturer(locale, manufacturer);
    }

    /**
     * @param rs
     * @return
     * @throws SQLException
     * @throws DataException
     */
    protected PLUItemIfc createItemImage(ResultSet rs, JdbcDataConnection dataConnection, PLUItemIfc pluItem) throws SQLException, DataException
    {
        // get and set item image information
        String imageLocation = getSafeString(rs, 1);
        byte[] fullBlob = DatabaseBlobHelperFactory.getInstance().getDatabaseBlobHelper(dataConnection.getConnection()).loadBlob(rs, FIELD_ITEM_IMAGE_BLOB);
        ItemImageIfc itemImage = DomainGateway.getFactory().getItemImageInstance();
        itemImage.setImageLocation(imageLocation);
        itemImage.setImageBlob(fullBlob);
        pluItem.setItemImage(itemImage);

        return pluItem;
    }

    /**
     * Create a PLUItem instance based on info from result set.
     *
     * @param rs
     * @return
     */
    protected PLUItemIfc createPLUItem(ResultSet rs) throws SQLException, DataException
    {
        // parse result set and create domain objects as necessary
        int index = 0;
        String posItemID = getSafeString(rs, ++index);
        String itemID = getSafeString(rs, ++index);
        boolean disableQuantityKey = getBooleanFromString(rs, ++index);
        boolean prohibitReturn = getBooleanFromString(rs, ++index);
        BigDecimal minimumSaleQuantity = getBigDecimal(rs, ++index);
        BigDecimal maximumSaleQuantity = getBigDecimal(rs, ++index);
        boolean multipleCouponsAllowed = getBooleanFromString(rs, ++index);
        boolean priceEntryRequired = getBooleanFromString(rs, ++index);
        boolean electronicCouponAvailable = getBooleanFromString(rs, ++index);
        boolean couponRestricted = getBooleanFromString(rs, ++index);
        boolean priceModifiable = getBooleanFromString(rs, ++index);
        boolean specialOrderEligible = getBooleanFromString(rs, ++index);
        boolean employeeDiscountAllowed = getBooleanFromString(rs, ++index);
        CurrencyIfc compareAtPrice = getCurrencyFromDecimal(rs, ++index);
        int restrictedAge = rs.getInt(++index);
        int taxGroupID = rs.getInt(++index);
        boolean taxable = getBooleanFromString(rs, ++index);
        boolean sizeRequired = getBooleanFromString(rs, ++index);
        String deptID = getSafeString(rs, ++index);
        boolean discountable = getBooleanFromString(rs, ++index);
        boolean damageDiscountable = getBooleanFromString(rs, ++index);
        boolean registryEligible = getBooleanFromString(rs, ++index);
        String productGroupID = rs.getString(++index);
        boolean saleable = getBooleanFromString(rs, ++index);
        Float kitCodeF = new Float(rs.getFloat(++index));
        int kitCode = kitCodeF.intValue();
        boolean substituteAvailable = getBooleanFromString(rs, ++index);
        String itemType = rs.getString(++index);
        String merchandiseHierarchyGroupId = rs.getString(++index);
        int manufacturerID = rs.getInt(++index);
        String manufacturerItemUPC = getSafeString(rs, ++index);
        String storeID = rs.getString(++index);

        PLUItemIfc pluItem = null;
        // create the appropriate PLUItemIfc type
        if (productGroupID != null && productGroupID.equals(PRODUCT_GROUP_GIFT_CARD))
        {
            // create a GiftCardPLUItemIfc
            pluItem = instantiateGiftCardPLUItem(priceEntryRequired);
        }
        // Added for alterations functionality
        else if (productGroupID != null && productGroupID.equals(PRODUCT_GROUP_ALTERATION))
        {
            pluItem = instantiateAlterationPLUItem();
        }
        else
        {
            switch (kitCode)
            {
                case ItemKitConstantsIfc.ITEM_KIT_CODE_HEADER:
                    // create an ItemKitIfc
                    pluItem = instantiateItemKit();
                    break;
                case ItemKitConstantsIfc.ITEM_KIT_CODE_COMPONENT:
                    // create an ItemKitIfc
                    pluItem = instantiateKitComponent();
                    break;
                default:
                    pluItem = instantiatePLUItem();
            }
        }

        // initialize common attributes
        pluItem.setItemID(itemID);
        pluItem.setPosItemID(posItemID);
        pluItem.setTaxable(taxable);
        pluItem.setTaxGroupID(taxGroupID);
        pluItem.setDepartmentID(deptID);
        pluItem.setItemSizeRequired(sizeRequired);
        // pluItem.setPlanogramID(planogramID);
        pluItem.setManufacturerID(manufacturerID);
        pluItem.setManufacturerItemUPC(manufacturerItemUPC);
        pluItem.setStoreID(storeID);
        if (compareAtPrice == null)
        {
            compareAtPrice = DomainGateway.getBaseCurrencyInstance();
        }
        pluItem.setCompareAtPrice(compareAtPrice);
        pluItem.setRestrictiveAge(restrictedAge);

        // build the merchandise classification list
        ItemClassificationIfc ic = pluItem.getItemClassification();
        ArrayList<MerchandiseClassificationIfc> classificationList = new ArrayList<MerchandiseClassificationIfc>();
        MerchandiseClassificationIfc merchandiseClassification = null;
        String merchandiseCode = null;
        for (int i = 0; i < 10; i++)
        {
            merchandiseCode = getSafeString(rs, ++index);
            if (!Util.isEmpty(merchandiseCode))
            {
                merchandiseClassification = DomainGateway.getFactory().getMerchandiseClassificationInstance();
                merchandiseClassification.setIdentifier(merchandiseCode);
                classificationList.add(merchandiseClassification);
            }
        }

        if (!classificationList.isEmpty())
        {
            MerchandiseClassificationIfc[] mcArray = new MerchandiseClassificationIfc[classificationList.size()];
            classificationList.toArray(mcArray);
            ic.setMerchandiseClassifications(mcArray);
        }
        // set the remaining ItemClassification attributes for the PLUItem
        ic.setQuantityModifiable(!disableQuantityKey);
        ic.setReturnEligible(!prohibitReturn);
        ic.setPriceOverridable(priceModifiable);
        ic.setMinimumSaleQuantity(minimumSaleQuantity);
        ic.setMaximumSaleQuantity(maximumSaleQuantity);
        ic.setMultipleCouponEligible(multipleCouponsAllowed);
        ic.setPriceEntryRequired(priceEntryRequired);
        ic.setElectronicCouponAvailable(electronicCouponAvailable);
        ic.setCouponRestricted(couponRestricted);
        ic.setDiscountEligible(discountable);
        ic.setDamageDiscountEligible(damageDiscountable);
        ic.setRegistryEligible(registryEligible);
        ic.setAuthorizedForSale(saleable);
        ic.setItemKitSetCode(kitCode);
        ic.setSubstituteItemAvailable(substituteAvailable);
        ic.setSpecialOrderEligible(specialOrderEligible);
        ic.setEmployeeDiscountAllowedFlag(employeeDiscountAllowed);
        ic.setMerchandiseHierarchyGroup(merchandiseHierarchyGroupId);

        if (itemType.equals(ITEM_TYPE_SERVICE))
        {
            ic.setItemType(ItemClassificationConstantsIfc.TYPE_SERVICE);
        }
        else if (itemType.equals(ITEM_TYPE_STOCK))
        {
            ic.setItemType(ItemClassificationConstantsIfc.TYPE_STOCK);
        }
        else if (itemType.equals(ITEM_TYPE_STORE_COUPON))
        {
            ic.setItemType(ItemClassificationConstantsIfc.TYPE_STORE_COUPON);
        }

        // create product group and set ID if necessary
        ProductGroupIfc pg = DomainGateway.getFactory().getProductGroupInstance();
        if (productGroupID != null)
        {
            pg.setGroupID(productGroupID);
        }
        ic.setGroup(pg);
//        ic.setExternalSystemCreateUIN(true); //TODO
//        ic.setSerialEntryTime("StoreReceiving"); //TODO
        return pluItem;
    }

    /**
     * Builds the sql for retrieving the classes that belong to the same rule .
     *
     * @param the rule id
     * @return SQLStatement
     */
    protected SQLSelectStatement buildClassSQL(int id)
    {
        SQLSelectStatement sql = new SQLSelectStatement();
        // add tables
        sql.addTable(TABLE_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY,
                ALIAS_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY);
        // add columns
        sql.addColumn(ALIAS_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY,
                FIELD_MERCHANDISE_CLASSIFICATION_CODE);
        sql.addColumn(ALIAS_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_THR_QUANTITY);
        sql.addQualifier(new SQLParameterValue(FIELD_PRICE_DERIVATION_RULE_ID, id));

        return sql;

    }

    /**
     * Builds the sql for retrieving the departments that belong to the same
     * rule .
     *
     * @param the rule id
     * @return SQLStatement
     */
    protected SQLSelectStatement buildDepartmentSQL(int id)
    {
        SQLSelectStatement sql = new SQLSelectStatement();
        // add tables
        sql.addTable(TABLE_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY,
                ALIAS_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY);
        // add columns
        sql.addColumn(ALIAS_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_POS_DEPARTMENT_ID);
        sql.addColumn(ALIAS_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_THR_QUANTITY);
        sql.addQualifier(new SQLParameterValue(FIELD_PRICE_DERIVATION_RULE_ID, id));

        return sql;

    }

    /**
     * Helper method adds common columns to the rule selection sql statements.
     *
     * @param SQLStatement
     */
    protected void addCommonColumns(SQLSelectStatement sql)
    {
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_ID);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_RETAIL_STORE_ID);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_TRANSACTION_CONTROL_BREAK_CODE);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_STATUS_CODE);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_INCLUDED_IN_BEST_DEAL_FLAG);

        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_DESCRIPTION);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_REASON_CODE);

        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_SCOPE_CODE);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_METHOD_CODE);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_ASSIGNMENT_BASIS_CODE);

        // get number of times to apply the rule, source and target bounds
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_APPLICATION_LIMIT);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_SOURCE_THRESHOLD_AMOUNT);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_SOURCE_LIMIT_AMOUNT);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_TARGET_THRESHOLD_AMOUNT);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_TARGET_LIMIT_AMOUNT);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_SOURCE_ANY_QUANTITY);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_TARGET_ANY_QUANTITY);

        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_THRESHOLD_TYPE_CODE);

        sql
                .addColumn(ALIAS_PRICE_DERIVATION_RULE,
                        FIELD_RETAIL_PRICE_MODIFIER_STOCK_LEDGER_ACCOUNTING_DISPOSITION_CODE);

        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_DEAL_DISTRIBUTION_FLAG);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_ALLOW_REPEATING_SOURCES_FLAG);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_ITM_DISC_TYPE_CODE);

        // Temporary Price Change Promotion Ids
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PROMOTION_ID);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PROMOTION_COMPONENT_ID);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PROMOTION_COMPONENT_DETAIL_ID);
    }

    /**
     * Selects all the advanced pricing rules that apply to an item
     *
     * @param dataConnection
     * @param pluItem
     * @param sqlLocale
     * @throws DataException
     */
    public void applyAdvancedPricingRules(JdbcDataConnection dataConnection, PLUItemIfc pluItem,
            LocaleRequestor sqlLocale) throws DataException
    {
        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.selectAdvancedPricingRules() begins.");

        AdvancedPricingRuleIfc[] rules = selectGroupDiscounts(dataConnection, pluItem, sqlLocale);
        pluItem.addAdvancedPricingRules(rules);

        // organize existing rules for next deal discount.
        Hashtable<String,AdvancedPricingRuleIfc> ruleMap = new Hashtable<String,AdvancedPricingRuleIfc>();
        for (int i = 0; i < rules.length; i++)
        {
            ruleMap.put(rules[i].getRuleID(), rules[i]);
        }

        pluItem.addAdvancedPricingRules(selectDealDiscounts(dataConnection, pluItem, ruleMap, sqlLocale));

        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.selectAdvancedPricingRules() ends");
    }

    /**
     * Query for Locale Dependent Description
     *
     * @param dataConnection the data connection to use
     * @param pluItem the plu item
     * @param sqlLocale
     * @throws DataException
     * @throws SQLException
     */
    protected void applyLocaleDependentDescriptions(JdbcDataConnection dataConnection, PLUItemIfc pluItem,
            LocaleRequestor localeRequestor) throws DataException, SQLException
    {
        SQLSelectStatement sql = new SQLSelectStatement();

        // Table to select from
        sql.addTable(TABLE_ITEM_I8);

        // add column
        sql.addColumn(FIELD_LOCALE);
        sql.addColumn(FIELD_ITEM_DESCRIPTION);
        sql.addColumn(FIELD_ITEM_SHORT_DESCRIPTION);

        // add identifier qualifier
        sql.addQualifier(new SQLParameterValue(FIELD_ITEM_ID, pluItem.getItemID()));

        // add qualifier for locale
        sql.addQualifier(FIELD_LOCALE + " "
                + buildINClauseString(LocaleMap.getBestMatch("", localeRequestor.getLocales())));

        ResultSet rs = null;
        try
        {
            // execute sql
            rs = execute(dataConnection, sql);

            Locale bestMatchingDefaultLocale = LocaleMap.getBestMatch(localeRequestor.getDefaultLocale());
            Locale locale = null;
            // parse result set
            while (rs.next())
            {
                locale = LocaleUtilities.getLocaleFromString(getSafeString(rs, 1));
                pluItem.setDescription(locale, getSafeString(rs, 2));
                pluItem.setShortDescription(locale, getSafeString(rs, 3));
                if (locale.equals(bestMatchingDefaultLocale))
                {
                    pluItem.getLocalizedDescriptions().setDefaultLocale(bestMatchingDefaultLocale);
                    pluItem.getShortLocalizedDescriptions().setDefaultLocale(bestMatchingDefaultLocale);
                }

            }
            rs.close();
        }
        catch (SQLException se)
        {
            throw new DataException(DataException.SQL_ERROR, "JdbcPLUOperation.applyLocaleDependentDescriptions()", se);
        }
        finally
        {
            DBUtils.getInstance().closeResultSet(rs);
        }

    }

    /**
     * Query for Locale Dependent Description
     *
     * @param dataConnection the data connection to use
     * @param ItemID the plu item
     * @param LocaleRequestor
     * @throws DataException
     * @throws SQLException
     */
    protected LocalizedTextIfc applyLocaleDependentDescriptions(JdbcDataConnection dataConnection, String itemId,
            LocaleRequestor localeRequestor) throws DataException, SQLException
    {
        LocalizedTextIfc localizedDesc = new LocalizedText();
        SQLSelectStatement descriptionsql = new SQLSelectStatement();
        // Table to select from
        descriptionsql.addTable(TABLE_ITEM_I8);

        // add column
        descriptionsql.addColumn(FIELD_LOCALE);
        descriptionsql.addColumn(FIELD_ITEM_DESCRIPTION);
        descriptionsql.addColumn(FIELD_ITEM_SHORT_DESCRIPTION);

        // add identifier qualifier
        descriptionsql.addQualifier(FIELD_ITEM_ID, inQuotes(itemId));

        // add qualifier for locale
        descriptionsql.addQualifier(FIELD_LOCALE + " "
                + buildINClauseString(LocaleMap.getBestMatch("", localeRequestor.getLocales())));

        ResultSet descriptionResultSet = null;
        PreparedStatement descPreparedStmt = null;
        try
        {
            Connection descriptionConnection = dataConnection.getConnection();
            descPreparedStmt = descriptionConnection.prepareStatement(descriptionsql.getSQLString());
            descriptionResultSet = descPreparedStmt.executeQuery();

            Locale locale = null;
            String desc = null;
            while (descriptionResultSet.next())
            {
                locale = LocaleUtilities.getLocaleFromString(getSafeString(descriptionResultSet, 1));
                desc = getSafeString(descriptionResultSet, 2);
                localizedDesc.putText(locale, desc);
            }
            descriptionResultSet.close();
        }
        catch (SQLException se)
        {
            throw new DataException(DataException.SQL_ERROR, "JdbcPLUOperation.readRelatedItems()", se);
        }
        finally
        {
            DBUtils.getInstance().closeResultSet(descriptionResultSet);
            DBUtils.getInstance().closeStatement(descPreparedStmt);
        }
        return localizedDesc;

    }

    /**
     * Apply an array of planogram ids to the list of items.
     *
     * @param dataConnection the data connection to use.
     * @param list the list of PLUItems required planogram ids
     * @param storeId the store owning the items
     */
    protected void applyPlanogramIDs(JdbcDataConnection dataConnection, PLUItemIfc pluItem) throws DataException
    {
        String[] planograms = getPlanogramIDForItem(dataConnection, pluItem.getItemID(), pluItem.getStoreID());
        pluItem.setPlanogramID(planograms);
    }

    /**
     * Query for and apply the found item images for the specified item.
     *
     * @param dataConnection
     * @param pluItem
     * @param sqlLocale
     */
    protected void applyItemImages(JdbcDataConnection dataConnection, PLUItemIfc pluItem,
            LocaleRequestor sqlLocale)
    {
        // new prepared statement for item images
        SQLSelectStatement itemImageSql = buildSelectItemImageSQL(pluItem.getItemID(), sqlLocale);
        ResultSet resultSet = null;
        try
        {
            // Get item's description
            resultSet = execute(dataConnection, itemImageSql);

            while (resultSet.next())
            {
                createItemImage(resultSet, dataConnection, pluItem);
            }
        }
        catch (SQLException se)
        {
            logger.warn("Item image lookup", se);
        }
        catch (DataException e)
        {
            logger.warn("Item image lookup", e);
        }
        finally
        {
            DBUtils.getInstance().closeResultSet(resultSet);
        }
    }

    /**
     * Query for and apply the found manufacturer info for the specified item.
     *
     * @param dataConnection
     * @param pluItem
     * @param sqlLocale
     */
    protected void applyManufacturer(JdbcDataConnection dataConnection, PLUItemIfc pluItem,
            List<SQLParameterIfc> qualifiers, LocaleRequestor sqlLocale)
        throws DataException
    {
    	logger.debug("Inside applyManufacturer JdbcPLUOperation ");
    	logger.info("Inside applyManufacturer JdbcPLUOperation ");
		if (pluItem.isStoreCoupon()) {
			
			readCpnAttrDtls(dataConnection, pluItem);
			readCpnHierarchyDtls(dataConnection, pluItem);
			//setLoyalityDiscountRate(pluItem);
		}else{
			
			/*readLoyalityMerchHrchyDtls(dataConnection, pluItem);*/
			readLoyalityMerchHrchyDtls(dataConnection, pluItem.getItem().getItemClassification().getMerchandiseHierarchyGroup(), pluItem);
		}
        // if we already know the manufacturer's id, just use that as the qualifier
        if (pluItem.getManufacturerID() > 0)
        {
            qualifiers = new ArrayList<SQLParameterIfc>(1);
            qualifiers.add(new SQLParameterValue(TABLE_ITEM_MANUFACTURER_I18N, FIELD_ITEM_MANUFACTURER_ID, pluItem.getManufacturerID()));
        }

        // build the statement
        SQLSelectStatement localizedItemManufactureSql = buildLocalizedItemManufactureSQL(qualifiers, sqlLocale);

        ResultSet resultSet = null;
        try
        {
            // Get item's description
            resultSet = execute(dataConnection, localizedItemManufactureSql);

            while (resultSet.next())
            {
                addLocalizedItemManufacture(resultSet, pluItem);
            }
        }
        catch (SQLException se)
        {
        	
            throw new DataException(DataException.SQL_ERROR, "Manufacturer lookup", se);
        }
        finally
        {
        	
            DBUtils.getInstance().closeResultSet(resultSet);
        }
        
        
    }

    /**
     * Builds select SQL in order to select pricing rules. Rules can be built in
     * six combinations of three table. The PriceDerivationRule table, either
     * the ItemPriceDerivation or MixAndMatchPriceDerivationItem, and then one
     * of three tables: ItemPriceDerivationRuleEligibility,
     * DepartmentPriceDerivationRuleEligibility or
     * MerchandiseStructurePriceDerivationRuleEligibility.
     * <ul>
     *  <li>PriceDerivationRule, ItemPriceDerivation, ItemPriceDerivationRuleEligibility</li>
     *  <li>PriceDerivationRule, ItemPriceDerivation, DepartmentPriceDerivationRuleEligibility</li>
     *  <li>PriceDerivationRule, ItemPriceDerivation, MerchandiseStructurePriceDerivationRuleEligibility</li>
     *  <li>PriceDerivationRule, MixAndMatchPriceDerivationItem, ItemPriceDerivationRuleEligibility</li>
     *  <li>PriceDerivationRule, MixAndMatchPriceDerivationItem, DepartmentPriceDerivationRuleEligibility</li>
     *  <li>PriceDerivationRule, MixAndMatchPriceDerivationItem, MerchandiseStructurePriceDerivationRuleEligibility</li>
     * </ul>
     *
     * @param comparisonBasis whether the rule is at the item, class or dept level.
     * @param deal if true this code uses MixAndMatch (i.e. Deal) tables, else
     *            it uses ItemPrice (Group)
     * @param ruleID if not null, select a rule by a specific rule id
     * @param pluItem if not null select many rules by the item attribute
     *            matching comparisonBasis
     * @return SQLSelectStatement
     */
    protected SQLSelectStatement buildRuleSQL(int comparisonBasis, boolean deal, Integer ruleID, PLUItemIfc pluItem)
    {
        SQLSelectStatement sql = new SQLSelectStatement();

        // add tables
        sql.addTable(TABLE_PRICE_DERIVATION_RULE, ALIAS_PRICE_DERIVATION_RULE);
        // add columns
        addCommonColumns(sql);
        if (deal)
        {
            sql.addColumn(ALIAS_MIX_AND_MATCH_PRICE_DERIVATION_ITEM, FIELD_MIX_MATCH_PRICE_REDUCTION_MONETARY_AMOUNT);
            sql.addColumn(ALIAS_MIX_AND_MATCH_PRICE_DERIVATION_ITEM, FIELD_MIX_MATCH_PRICE_REDUCTION_PERCENT);
            sql.addColumn(ALIAS_MIX_AND_MATCH_PRICE_DERIVATION_ITEM, FIELD_MIX_MATCH_PRICE_POINT_REDUCTION);
        }
        else
        {
            sql.addColumn(ALIAS_ITEM_PRICE_DERIVATION, FIELD_ITEM_PRICE_DERIVATION_SALE_UNIT_MONETARY_AMOUNT);
            sql.addColumn(ALIAS_ITEM_PRICE_DERIVATION, FIELD_ITEM_PRICE_DERIVATION_SALE_UNIT_PERCENT);
            sql.addColumn(ALIAS_ITEM_PRICE_DERIVATION, FIELD_ITEM_PRICE_DERIVATION_PRICE_POINT);
        }
        // add comparison specific columns and joins
        switch (comparisonBasis)
        {
            case COMPARISON_BASIS_ITEM_ID:
                // add columns
                sql.addColumn(ALIAS_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_THR_AMOUNT);
                sql.addColumn(ALIAS_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_THR_QUANTITY);
                sql.addColumn(ALIAS_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_ITEM_ID);

                // add qualifiers
                if (ruleID != null)
                {
                    sql.addQualifier(new SQLParameterValue(ALIAS_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY,
                            FIELD_PRICE_DERIVATION_RULE_ID, ruleID));
                }

                if (pluItem != null)
                {
                    sql.addQualifier(new SQLParameterValue(ALIAS_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY,
                            FIELD_ITEM_ID, pluItem.getItemID()));
                }

                // add joins
                sql.addOuterJoinQualifier(" JOIN " + TABLE_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY + " " + ALIAS_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY +
                        " ON " + ALIAS_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY + "." + FIELD_PRICE_DERIVATION_RULE_ID +
                        " = " + ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_PRICE_DERIVATION_RULE_ID +
                        " AND " + ALIAS_ITEM_PRICE_DERIVATION_RULE_ELIGIBILITY + "." + FIELD_RETAIL_STORE_ID +
                        " = " + ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_RETAIL_STORE_ID);
                break;

            case COMPARISON_BASIS_DEPARTMENT_ID:
                // add columns
                sql.addColumn(ALIAS_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_THR_AMOUNT);
                sql.addColumn(ALIAS_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_THR_QUANTITY);
                sql.addColumn(ALIAS_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_POS_DEPARTMENT_ID);

                // add qualifiers
                if (ruleID != null)
                {
                    sql.addQualifier(new SQLParameterValue(ALIAS_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY,
                            FIELD_PRICE_DERIVATION_RULE_ID, ruleID));
                }

                if (pluItem != null)
                {
                    sql.addQualifier(new SQLParameterValue(ALIAS_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY,
                            FIELD_POS_DEPARTMENT_ID, pluItem.getDepartmentID()));
                }

                // add joins
                sql.addOuterJoinQualifier(" JOIN " + TABLE_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY + " " + ALIAS_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY +
                        " ON " + ALIAS_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY + "." + FIELD_PRICE_DERIVATION_RULE_ID +
                        " = " + ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_PRICE_DERIVATION_RULE_ID +
                        " AND " + ALIAS_DEPARTMENT_PRICE_DERIVATION_RULE_ELIGIBILITY + "." + FIELD_RETAIL_STORE_ID +
                        " = " + ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_RETAIL_STORE_ID);
                break;

            case COMPARISON_BASIS_MERCHANDISE_CLASS:
                // add columns
                sql.addColumn(ALIAS_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_THR_AMOUNT);
                sql.addColumn(ALIAS_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY, FIELD_THR_QUANTITY);
                sql.addColumn(ALIAS_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY,
                        FIELD_MERCHANDISE_CLASSIFICATION_CODE);

                // add qualifiers
                if (ruleID != null)
                {
                    sql.addQualifier(new SQLParameterValue(ALIAS_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY,
                            FIELD_PRICE_DERIVATION_RULE_ID, ruleID));
                }

                if (pluItem != null)
                {
                    sql.addQualifier(ALIAS_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY + "."
                            + FIELD_MERCHANDISE_CLASSIFICATION_CODE + " IN  (" + pluItem.getMerchandiseCodesString() + ")");
                }

                // add joins
                sql.addOuterJoinQualifier(" JOIN " + TABLE_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY + " " + ALIAS_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY +
                        " ON " + ALIAS_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY + "." + FIELD_PRICE_DERIVATION_RULE_ID +
                        " = " + ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_PRICE_DERIVATION_RULE_ID +
                        " AND " + ALIAS_MERCHANDISE_STRUCTURE_PRICE_DERIVATION_RULE_ELIGIBILITY + "." + FIELD_RETAIL_STORE_ID +
                        " = " + ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_RETAIL_STORE_ID);
        } // switch

        // add dates for all discounts
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_EFFECTIVE_DATE);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_EXPIRATION_DATE);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE, FIELD_CUSTOMER_PRICING_GROUP_ID);

        if (deal)
        {
            sql.addColumn(ALIAS_MIX_AND_MATCH_PRICE_DERIVATION_ITEM, FIELD_MIX_AND_MATCH_LIMIT_COUNT);
            sql.addColumn(ALIAS_MIX_AND_MATCH_PRICE_DERIVATION_ITEM, FIELD_PROMOTIONAL_PRODUCT_ID);
            sql.addColumn(ALIAS_MIX_AND_MATCH_PRICE_DERIVATION_ITEM, FIELD_COMPARISON_BASIS_CODE);

            sql.addOuterJoinQualifier(" JOIN " + TABLE_MIX_AND_MATCH_PRICE_DERIVATION_ITEM + " " + ALIAS_MIX_AND_MATCH_PRICE_DERIVATION_ITEM +
                    " ON " + ALIAS_MIX_AND_MATCH_PRICE_DERIVATION_ITEM + "." + FIELD_PRICE_DERIVATION_RULE_ID +
                    " = " + ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_PRICE_DERIVATION_RULE_ID +
                    " AND " + ALIAS_MIX_AND_MATCH_PRICE_DERIVATION_ITEM + "." + FIELD_RETAIL_STORE_ID +
                    " = " + ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_RETAIL_STORE_ID);
        }
        else
        {
            sql.addOuterJoinQualifier(" JOIN " + TABLE_ITEM_PRICE_DERIVATION + " " + ALIAS_ITEM_PRICE_DERIVATION +
                    " ON " + ALIAS_ITEM_PRICE_DERIVATION + "." + FIELD_PRICE_DERIVATION_RULE_ID +
                    " = " + ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_PRICE_DERIVATION_RULE_ID +
                    " AND " + ALIAS_ITEM_PRICE_DERIVATION + "." + FIELD_RETAIL_STORE_ID +
                    " = " + ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_RETAIL_STORE_ID);
        }
        sql.addQualifier(currentTimestampRangeCheckingString(
                ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_PRICE_DERIVATION_RULE_EFFECTIVE_DATE,
                ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_PRICE_DERIVATION_RULE_EXPIRATION_DATE));

        // add qualifier for the status
        String statusExpiredString = STATUS_DESCRIPTORS[STATUS_EXPIRED];
        sql.addQualifier(new SQLParameterValue(ALIAS_PRICE_DERIVATION_RULE + "." + FIELD_PRICE_DERIVATION_RULE_STATUS_CODE +
                " != ?", statusExpiredString));

        // limit search to type of item
        if (pluItem != null)
        {
            int itemType = ItemClassificationConstantsIfc.TYPE_UNKNOWN;
            if (pluItem.getItemClassification() != null)
            {
                itemType = pluItem.getItemClassification().getItemType();
            }

            switch (itemType)
            {
                case ItemClassificationConstantsIfc.TYPE_STORE_COUPON:
                    sql.addQualifier(new SQLParameterValue(FIELD_PRICE_DERIVATION_RULE_ASSIGNMENT_BASIS_CODE, ASSIGNMENT_STORE_COUPON));
                    break;

                default:
                    sql.addQualifier(new SQLParameterValue(FIELD_PRICE_DERIVATION_RULE_ASSIGNMENT_BASIS_CODE, ASSIGNMENT_ITEM));
            }
        }

        return sql;
    }

    /**
     * Builds SQL for selecting group rules for a specified item.
     *
     * @param comparisonBasis whether the rule is at an item, class or dept level
     * @param pluItem the item to gather rules for
     * @return
     */
    protected SQLSelectStatement buildGroupSQL(int comparisonBasis, PLUItemIfc pluItem)
    {
        return buildRuleSQL(comparisonBasis, false, null, pluItem);
    }

    /**
     * Builds the sql for reading the localized name and description for the
     * advanced pricing rules
     *
     * @param AdvancedPricingRuleIfc rule
     * @param LocaleRequestor sqlLocale
     * @return SQLSelectStatement
     */
    private SQLSelectStatement buildLocalizedRuleSQL(AdvancedPricingRuleIfc rule, LocaleRequestor sqlLocale)
    {
        SQLSelectStatement sql = new SQLSelectStatement();

        // add tables

        sql.addTable(TABLE_PRICE_DERIVATION_RULE, ALIAS_PRICE_DERIVATION_RULE);
        sql.addTable(TABLE_PRICE_DERIVATION_RULE_I8, ALIAS_PRICE_DERIVATION_RULE_I8);

        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE_I8, FIELD_LOCALE);
        sql.addColumn(ALIAS_PRICE_DERIVATION_RULE_I8, FIELD_PRICE_DERIVATION_RULE_NAME);

        sql.addJoinQualifier(ALIAS_PRICE_DERIVATION_RULE_I8, FIELD_PRICE_DERIVATION_RULE_ID,
                ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_ID);

        sql.addJoinQualifier(ALIAS_PRICE_DERIVATION_RULE, FIELD_RETAIL_STORE_ID, ALIAS_PRICE_DERIVATION_RULE_I8,
                FIELD_RETAIL_STORE_ID);

        // add qualifier for locale
        Set<Locale> bestMatches = LocaleMap.getBestMatch("", sqlLocale.getLocales());
        sql.addQualifier(ALIAS_PRICE_DERIVATION_RULE_I8 + "." + FIELD_LOCALE + " "
                + JdbcDataOperation.buildINClauseString(bestMatches));
        sql.addQualifier(new SQLParameterValue(ALIAS_PRICE_DERIVATION_RULE, FIELD_PRICE_DERIVATION_RULE_ID, Integer.valueOf(rule.getRuleID())));
        // sql.addQualifier(ALIAS_PRICE_DERIVATION_RULE+ "." +
        // FIELD_RETAIL_STORE_ID + " = " +
        // JdbcDataOperation.makeSafeString(rule.getStoreID()));

        return sql;
    }

    /**
     * Builds SQL for selecting group rules for an advanced pricing rule id.
     *
     * @param comparisonBasis whether the rule is at an item, class or dept level
     * @param ruleID the specific rule to retrieve
     * @return
     */
    protected SQLSelectStatement buildGroupRuleSQL(int comparisonBasis, String ruleID)
    {
        return buildRuleSQL(comparisonBasis, false, Integer.valueOf(ruleID), null);
    }

    /**
     * Select the deal discounts along with the localized name and description
     *
     * @param JdbcDataConnection dataConnection
     * @param PLUItemIfc pluItem
     * @param Hashtable existingRules
     * @param LocaleRequestor sqlLocale
     * @throws DataException
     */
    protected AdvancedPricingRuleIfc[] selectDealDiscounts(JdbcDataConnection dataConnection,
            PLUItemIfc pluItem, Hashtable<String,AdvancedPricingRuleIfc> existingRules, LocaleRequestor sqlLocale) throws DataException
    {

        ResultSet rs = null;
        SQLSelectStatement sql = new SQLSelectStatement();
        try
        {
            sql = buildDealSQL(DiscountRuleConstantsIfc.COMPARISON_BASIS_ITEM_ID, pluItem);
            rs = execute(dataConnection, sql);
            List<AdvancedPricingRuleIfc> itemComparisonRules = readDealAdvancedPricingRules(rs,
                    DiscountRuleConstantsIfc.COMPARISON_BASIS_ITEM_ID, existingRules);
            for (AdvancedPricingRuleIfc rule : itemComparisonRules)
            {
                getLocalizedRuleData(dataConnection, rule, sqlLocale);
                if (rule.getThresholdTypeCode() == DiscountRuleConstantsIfc.THRESHOLD_QUANTITY)
                {
                    sql = buildItemSQL(Integer.parseInt(rule.getRuleID()));
                    rs = execute(dataConnection, sql);
                    rule = readSources(rs, rule);
                }
            }

            String merchClassCodes = pluItem.getMerchandiseCodesString();
            if (merchClassCodes != null && !"''".equals(merchClassCodes))
            {
                sql = buildDealSQL(DiscountRuleConstantsIfc.COMPARISON_BASIS_MERCHANDISE_CLASS, pluItem);
                rs = execute(dataConnection, sql);
                List<AdvancedPricingRuleIfc> classComparisonRules = readDealAdvancedPricingRules(rs,
                        DiscountRuleConstantsIfc.COMPARISON_BASIS_MERCHANDISE_CLASS, existingRules);
                for (AdvancedPricingRuleIfc rule : classComparisonRules)
                {
                    getLocalizedRuleData(dataConnection, rule, sqlLocale);
                    if (rule.getThresholdTypeCode() == DiscountRuleConstantsIfc.THRESHOLD_QUANTITY)
                    {
                        sql = buildClassSQL(Integer.parseInt(rule.getRuleID()));
                        rs = execute(dataConnection, sql);
                        rule = readSources(rs, rule);
                    }
                }
            }

            // department type rules
            if (pluItem.getDepartmentID() != null)
            {
                sql = buildDealSQL(DiscountRuleConstantsIfc.COMPARISON_BASIS_DEPARTMENT_ID, pluItem);
                rs = execute(dataConnection, sql);
                List<AdvancedPricingRuleIfc> depComparisonRules = readDealAdvancedPricingRules(rs,
                        DiscountRuleConstantsIfc.COMPARISON_BASIS_DEPARTMENT_ID, existingRules);
                for (AdvancedPricingRuleIfc rule : depComparisonRules)
                {
                    getLocalizedRuleData(dataConnection, rule, sqlLocale);
                    if (rule.getThresholdTypeCode() == DiscountRuleConstantsIfc.THRESHOLD_QUANTITY)
                    {
                        sql = buildDepartmentSQL(Integer.parseInt(rule.getRuleID()));
                        rs = execute(dataConnection, sql);
                        rule = readSources(rs, rule);
                    }
                }
            }
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "AdvancedPricing lookup");
            throw new DataException(DataException.SQL_ERROR, "Advanced Pricing lookup", se);
        }
        finally
        {
            if (rs != null)
            {
                try
                {
                    rs.close();
                }
                catch (SQLException se)
                {
                    dataConnection.logSQLException(se, "Advanced Pricing lookup -- Could not close result handle");
                }
            }

        }

        return existingRules.values().toArray(
                new AdvancedPricingRuleIfc[existingRules.size()]);
    }

    /**
     * Builds Deal SQL.
     *
     * @param comparisonBasis the type of the advanced pricing rule.
     * @return SQLSelectStatement
     */
    protected SQLSelectStatement buildDealSQL(int comparisonBasis, PLUItemIfc pluItem)
    {
        return buildRuleSQL(comparisonBasis, true, null, pluItem);
    }

    /**
     * Builds Deal SQL.
     *
     * @param comparisonBasis the type of the advanced pricing rule.
     * @return SQLSelectStatement
     */
    protected SQLSelectStatement buildDealRuleSQL(int comparisonBasis, String ruleID)
    {
        return buildRuleSQL(comparisonBasis, true, Integer.valueOf(ruleID), null);
    }

    /**
     * Reads advanced pricing rules result set and converts to array of
     * AdvancedPricingRuleIfc objects. If result set is empty, NO_DATA exception
     * is not thrown. Rather, null is returned.
     *
     * @param rs ResultSet
     * @param comparisonBasis comparison basis type
     * @return array of advanced pricing rules
     * @exception DataException is thrown if an error occurs parsing result set
     */
    protected ArrayList<AdvancedPricingRuleIfc> readGroupAdvancedPricingRules(ResultSet rs, int comparisonBasis) throws SQLException

    {
        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.readGroupAdvancedPricingRules() begins.");
        ArrayList<AdvancedPricingRuleIfc> discountsArray = new ArrayList<AdvancedPricingRuleIfc>();
        while (rs.next())
        {
            AdvancedPricingRuleIfc rule = DomainGateway.getFactory().getAdvancedPricingRuleInstance();
            int index = 0;
            rule.setRuleID(JdbcDataOperation.getSafeString(rs, ++index));
            rule.setStoreID(JdbcDataOperation.getSafeString(rs, ++index));

            index = readBasicAdvancedPricingRules(rs, rule);

            if (comparisonBasis > -1)
            {
                // threshold for amount
                CurrencyIfc thrAmount = JdbcDataOperation.getCurrencyFromDecimal(rs, ++index);

                // get around decimal index because Postgres' getInt doesn't
                // handle decimals indexes as integers.
                // int sourceQuantity = rs.getInt(++index); //original code
                Float sourceQuantityF = new Float(rs.getFloat(++index));
                int sourceQuantity = sourceQuantityF.intValue();

                String entry = JdbcDataOperation.getSafeString(rs, ++index);

                switch (rule.getThresholdTypeCode())
                {
                    case DiscountRuleConstantsIfc.THRESHOLD_QUANTITY:
                        rule.getSourceList().addEntry(entry, sourceQuantity);
                        break;
                    case DiscountRuleConstantsIfc.THRESHOLD_AMOUNT:
                        rule.getSourceList().addEntry(entry, thrAmount);
                        break;
                    default:
                        logger.warn("Invalid thresholdTypeCode read in JDBCPLUOperation.");
                        break;
                }

                EYSDate date = JdbcDataOperation.dateToEYSDate(rs, ++index);
                rule.setEffectiveDate(date);
                rule.setEffectiveTime(new EYSTime(date));

                date = JdbcDataOperation.dateToEYSDate(rs, ++index);
                rule.setExpirationDate(date);
                rule.setExpirationTime(new EYSTime(date));
                // checking the null validation of pricingGroupID and inserting
                // in to AdvancePricingRuleIfc object if it is not null
                String pricingGroup = JdbcDataOperation.getSafeString(rs, ++index);
                if (!"".equals(pricingGroup))
                {
                    rule.setPricingGroupID(Integer.parseInt(pricingGroup));

                }// end if
            }

            rule.setSourceComparisonBasis(comparisonBasis);
            rule.setSourcesAreTargets(true);

            discountsArray.add(rule);
        }
        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.readGroupAdvancedPricingRules() ends.");
        return discountsArray;
    }

    /**
     * Reads basic advanced pricing rules result set and converts to array of
     * AdvancedPricingRuleIfc objects. If result set is empty, NO_DATA exception
     * is not thrown. Rather, null is returned.
     *
     * @param rs ResultSet
     * @param AdvancedPricingRule
     * @return index
     */
    protected int readBasicAdvancedPricingRules(ResultSet rs, AdvancedPricingRuleIfc rule) throws SQLException
    {
        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.readBasicAdvancedPricingRules() begins.");

        int index = 2;
        String appliedWhen = null;
        String status = null;
        String inBestDeal = null;

        appliedWhen = getSafeString(rs, ++index);
        status = getSafeString(rs, ++index);
        inBestDeal = getSafeString(rs, ++index);

        rule.setDescription(getSafeString(rs, ++index));

        Float reasonCodeF = new Float(rs.getFloat(++index));
        int reasonCode = reasonCodeF.intValue();
        String reasonCodeString = CodeConstantsIfc.CODE_UNDEFINED;
        try
        {
            reasonCodeString = Integer.toString(reasonCode);
        }
        catch (Exception e)
        {
            // do nothing, code already initialized to UNDEFINED
        }
        LocalizedCodeIfc reason = DomainGateway.getFactory().getLocalizedCode();
        reason.setCode(reasonCodeString);
        rule.setReason(reason);
        Float discountScopeF = new Float(rs.getFloat(++index));
        int discountScope = discountScopeF.intValue();
        rule.setDiscountScope(discountScope);
        Float discountMethodF = new Float(rs.getFloat(++index));
        int discountMethod = discountMethodF.intValue();
        rule.setDiscountMethod(discountMethod);
        Float argumentBasisF = new Float(rs.getFloat(++index));
        int argumentBasis = argumentBasisF.intValue();
        rule.setAssignmentBasis(argumentBasis);

        Float applicationLimitF = new Float(rs.getFloat(++index));
        int applicationLimit = applicationLimitF.intValue();
        rule.setApplicationLimit(applicationLimit);

        rule.setSourceThreshold(getCurrencyFromDecimal(rs, ++index));
        rule.setSourceLimit(getCurrencyFromDecimal(rs, ++index));
        rule.setTargetThreshold(getCurrencyFromDecimal(rs, ++index));
        rule.setTargetLimit(getCurrencyFromDecimal(rs, ++index));
        rule.setSourceAnyQuantity(rs.getInt(++index));
        rule.setTargetAnyQuantity(rs.getInt(++index));
        Float thresholdTypeF = new Float(rs.getFloat(++index));
        int thresholdType = thresholdTypeF.intValue();
        rule.setThresholdTypeCode(thresholdType);
        rule.setAccountingMethod(rs.getInt(++index));
        boolean flag = getBooleanFromString(rs, ++index);
        rule.setDealDistribution(flag);
        flag = getBooleanFromString(rs, ++index);
        rule.setAllowRepeatingSources(flag);
        rule.setCalcDiscOnItemType(rs.getInt(++index));

        // Add the Promotion Fields here
        try
        {
            rule.setPromotionId(rs.getInt(++index));
        }
        catch (Exception e)
        {
            rule.setPromotionId(0);
        }
        rule.setPromotionComponentId(rs.getInt(++index));
        rule.setPromotionComponentDetailId(rs.getInt(++index));

        CurrencyIfc discountAmount = getCurrencyFromDecimal(rs, ++index);

        rule.setDiscountRate(getPercentage(rs, ++index));

        // identify if its a FixedPrice Discount
        CurrencyIfc fixedPrice = getCurrencyFromDecimal(rs, ++index);
        if (fixedPrice.signum() != 0)
        {
            rule.setFixedPrice(fixedPrice);
        }
        else
        {
            rule.setDiscountAmount(discountAmount);
        }

        // translate values
        setDiscountRuleValues(rule, appliedWhen, status, inBestDeal);
        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.readBasicAdvancedPricingRules() ends.");
        return index;
    }

    /**
     * Reads the result set that contains all the valid items for an advanced
     * pricing rule.
     *
     * @param result set
     * @return ArrayList with item id's
     * @exception SQLException
     */
    protected AdvancedPricingRuleIfc readSources(ResultSet rs, AdvancedPricingRuleIfc rule) throws SQLException
    {
        while (rs.next())
        {
            int index = 0; // begin parse result set
            String sourceID = JdbcDataOperation.getSafeString(rs, ++index);
            Float quantityF = new Float(rs.getFloat(++index));
            int quantity = quantityF.intValue();

            rule.getSourceList().addEntry(sourceID, quantity);

        }

        return rule;
    }

    /**
     * Reads the localized Result Set
     *
     * @param rs
     * @param rule
     * @return AdvancedPricingRuleIfc
     * @throws SQLException
     */
    private AdvancedPricingRuleIfc readLocalizedRule(ResultSet rs, AdvancedPricingRuleIfc rule)
            throws SQLException
    {
        while (rs.next())
        {
            int index = 0; // begin parse result set
            String localeString = JdbcDataOperation.getSafeString(rs, ++index);
            String localizedName = JdbcDataOperation.getSafeString(rs, ++index);
            Locale lcl = LocaleUtilities.getLocaleFromString(localeString);
            rule.setName(lcl, localizedName);
        }

        return rule;
    }

    /**
     * Reads advanced pricing rules result set and converts to array of
     * AdvancedPricingRuleIfc objects.
     *
     * @param rs ResultSet
     * @param comparisonBasis comparison basis type
     * @param pluItem
     * @return array of advanced pricing rules
     * @exception SQLException is thrown if an error occurs parsing result set
     */
    protected ArrayList<AdvancedPricingRuleIfc> readDealAdvancedPricingRules(ResultSet rs, int comparisonBasis,
            Hashtable<String, AdvancedPricingRuleIfc> existingRules) throws SQLException
    {
        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.readDealAdvancedPricingRules() begins.");
        ArrayList<AdvancedPricingRuleIfc> discountsArray = new ArrayList<AdvancedPricingRuleIfc>();
        List<String> ruleIDArray = new ArrayList<String>();
        while (rs.next())
        {
            int index = 0;
            String ruleID = JdbcDataOperation.getSafeString(rs, ++index);
            String storeID = JdbcDataOperation.getSafeString(rs, ++index);

            AdvancedPricingRuleIfc rule = existingRules.get(ruleID);
            if (rule == null)
            {
                rule = DomainGateway.getFactory().getAdvancedPricingRuleInstance();
                rule.setRuleID(ruleID);
                rule.setStoreID(storeID);
                existingRules.put(ruleID, rule);

            }
            if (!ruleIDArray.contains(ruleID))
            {
                index = readBasicAdvancedPricingRules(rs, rule);

                if (comparisonBasis > -1) {
                    // threshold for amount
                    CurrencyIfc thrAmount = JdbcDataOperation.getCurrencyFromDecimal(rs, ++index);

                    // get around decimal index because Postgres' getInt doesn't
                    // handle decimals indexes as integers.
                    // int sourceQuantity = rs.getInt(++index);
                    Float sourceQuantityF = new Float(rs.getFloat(++index));
                    int sourceQuantity = sourceQuantityF.intValue();
                    String sourceEntry = JdbcDataOperation.getSafeString(rs, ++index);

                    switch (rule.getThresholdTypeCode())
                    {
                        case DiscountRuleConstantsIfc.THRESHOLD_QUANTITY:
                            rule.getSourceList().addEntry(sourceEntry, sourceQuantity);
                            break;
                        case DiscountRuleConstantsIfc.THRESHOLD_AMOUNT:
                            rule.getSourceList().addEntry(sourceEntry, thrAmount);
                            break;
                        default:
                            logger.warn("Invalid thresholdTypeCode read in JDBCPLUOperation.");
                            break;
                    }
                }

                EYSDate date = JdbcDataOperation.dateToEYSDate(rs, ++index);
                rule.setEffectiveDate(date);
                rule.setEffectiveTime(new EYSTime(date));

                date = JdbcDataOperation.dateToEYSDate(rs, ++index);
                rule.setExpirationDate(date);
                rule.setExpirationTime(new EYSTime(date));

                rule.setPricingGroupID(rs.getInt(++index));

                // get around decimal index because Postgres' getInt doesn't
                // handle decimals indexes as integers.
                // int targetQuantity = rs.getInt(++index);
                Float targetQuantityF = new Float(rs.getFloat(++index));
                int targetQuantity = targetQuantityF.intValue();
                String targetEntry = JdbcDataOperation.getSafeString(rs, ++index);

                Float targetCompBasisF = new Float(rs.getFloat(++index));
                int targetCompBasis = targetCompBasisF.intValue();
                rule.setTargetComparisonBasis(targetCompBasis);

                rule.setSourceComparisonBasis(comparisonBasis);

                rule.getTargetList().addEntry(targetEntry, targetQuantity);
                rule.setSourcesAreTargets(false);
                ruleIDArray.add(ruleID);
            }
            else
            {
                // set the column number for target cquantity
                // it varies based on whether there is comparisonBasis.
                if (comparisonBasis > -1) {
                    index = 34;
                }
                else
                {
                    index = 31;
                }
                Float targetQuantityF = new Float(rs.getFloat(++index));
                int targetQuantity = targetQuantityF.intValue();
                String targetEntry = JdbcDataOperation.getSafeString(rs, ++index);
                rule.getTargetList().addEntry(targetEntry, targetQuantity);
            }
        }
        discountsArray.addAll(existingRules.values());
        if (logger.isDebugEnabled())
            logger.debug("JdbcPLUOperation.readDealAdvancedPricingRules() ends.");
        return discountsArray;
    }

    /**
     * Sets values in discount rule object.
     *
     * @param rule AdvancedPricingRuleIfc object already created
     * @param appliedWhen string value of applied when attribute
     * @param status string value of status
     */
    public void setDiscountRuleValues(AdvancedPricingRuleIfc rule, String appliedWhen, String status,
            String includedInBestDealFlag)
    {
        // set applied when value
        if (appliedWhen.equals("DT"))
        {
            rule.setAppliedWhen(DiscountRuleConstantsIfc.APPLIED_DETAIL);
        }
        else if (appliedWhen.equals("MT"))
        {
            rule.setAppliedWhen(DiscountRuleConstantsIfc.APPLIED_MERCHANDISE_SUBTOTAL);
        }
        else
        {
            rule.setAppliedWhen(DiscountRuleConstantsIfc.APPLIED_UNDEFINED);
        }

        // set status
        rule.setStatus(DiscountRuleConstantsIfc.STATUS_PENDING);
        for (int i = 0; i < DiscountRuleConstantsIfc.STATUS_DESCRIPTORS.length; i++)
        {
            if (status.equals(DiscountRuleConstantsIfc.STATUS_DESCRIPTORS[i]))
            {
                rule.setStatus(i);
                i = DiscountRuleConstantsIfc.STATUS_DESCRIPTORS.length;
            }
        }

        if (includedInBestDealFlag.equals("1"))
        {
            rule.setIncludedInBestDeal(true);
        }
        else
        {
            rule.setIncludedInBestDeal(false);
        }

    }

    /**
     * Selects items from the Stock Item table.
     * <p><blockquote><pre>
     * SELECT SITM.LU_UOM_SLS, SITM.FL_VLD_SRZ_ITM, UOM.FL_UOM_ENG_MC, UOM_I8.LCL, UOM_I8.NM_UOM, UOM_I8.DE_UOM, UOM.FL_DFLT_UOM, SITM.ED_CLR, SITM.ED_SZ, SITM.LU_STYL, SITM.FL_FE_RSTK, SITM.QW_ITM_PCK, SITM.FL_SRZ_CRT_EXT, SITM.CD_SRZ_CPT_TM, SL.NM_SRZ_ITM_LB
     * FROM CO_UOM UOM
     * JOIN CO_UOM_I8 UOM_I8 ON UOM.LU_UOM = UOM_I8.LU_UOM
     *      AND UOM_I8.LCL IN ('en')
     * JOIN AS_ITM_STK SITM ON SITM.LU_UOM_SLS = UOM.LU_UOM
     * LEFT JOIN AS_ITM_SRZ_LB_I8 SL ON SITM.ID_SRZ_ITM_LB = SL.ID_SRZ_ITM_LB
     *      AND SL.LCL IN ('en')
     * WHERE SITM.ID_ITM = '1234';
     * </pre></blockquote>
     *
     * @param dataConnection a connection to the database
     * @param itemID the item ID
     * @param pluItem the PLU Item
     * @exception DataException thrown when an error occurs executing the SQL
     *                against the DataConnection, or when processing the
     *                ResultSet
     */
    public void selectStockItem(JdbcDataConnection dataConnection, String itemID, PLUItemIfc pluItem,
            LocaleRequestor localeRequestor) throws DataException
    {
        SQLSelectStatement sql = new SQLSelectStatement();

        // add tables
        sql.addTable(TABLE_UNIT_OF_MEASURE, ALIAS_UNIT_OF_MEASURE);

        // add columns
        sql.addColumn(ALIAS_STOCK_ITEM + "." + FIELD_STOCK_ITEM_SALE_UNIT_OF_MEASURE_CODE);
        sql.addColumn(ALIAS_STOCK_ITEM + "." + FIELD_SERIALIZED_ITEM_VALIDATION_FLAG);
        sql.addColumn(ALIAS_UNIT_OF_MEASURE + "." + FIELD_UNIT_OF_MEASURE_ENGLISH_METRIC_FLAG);
        sql.addColumn(ALIAS_UNIT_OF_MEASURE_I8 + "." + FIELD_LOCALE);
        sql.addColumn(ALIAS_UNIT_OF_MEASURE_I8 + "." + FIELD_UNIT_OF_MEASURE_NAME);
        sql.addColumn(ALIAS_UNIT_OF_MEASURE_I8 + "." + FIELD_UNIT_OF_MEASURE_DESCRIPTION);
        sql.addColumn(ALIAS_UNIT_OF_MEASURE + "." + FIELD_DEFAULT_UNIT_OF_MEASURE_FLAG);
        sql.addColumn(ALIAS_STOCK_ITEM, FIELD_COLOR_CODE);
        sql.addColumn(ALIAS_STOCK_ITEM, FIELD_SIZE_CODE);
        sql.addColumn(ALIAS_STOCK_ITEM, FIELD_STYLE_CODE);
        sql.addColumn(ALIAS_STOCK_ITEM + "." + FIELD_STOCK_ITEM_RESTOCKING_FEE_FLAG);
        sql.addColumn(ALIAS_STOCK_ITEM + "." + FIELD_PACK_ITEM_WEIGHT_COUNT);

        //Adding columns for serialisation
        sql.addColumn(ALIAS_STOCK_ITEM + "." + FIELD_SERIALIZED_ITEM_EXTERNAL_SYSTEM_CREATE_UIN);
        sql.addColumn(ALIAS_STOCK_ITEM + "." + FIELD_SERIALIZED_ITEM_CAPTURE_TIME);
        sql.addColumn(ALIAS_SERIALIZED_ITEM_LABEL_I8 + "." + FIELD_UIN_LABEL_NAME);

        // add joins
        sql.addOuterJoinQualifier(" JOIN " +  TABLE_UNIT_OF_MEASURE_I8 + " " + ALIAS_UNIT_OF_MEASURE_I8 +
                " ON " + ALIAS_UNIT_OF_MEASURE + "." + FIELD_UNIT_OF_MEASURE_CODE +
                " = " + ALIAS_UNIT_OF_MEASURE_I8 + "." + FIELD_UNIT_OF_MEASURE_CODE +
                " AND " + ALIAS_UNIT_OF_MEASURE_I8 + "." + FIELD_LOCALE +
                buildINClauseString(LocaleMap.getBestMatch("", localeRequestor.getLocales())));

        sql.addOuterJoinQualifier(" JOIN " +  TABLE_STOCK_ITEM + " "  + ALIAS_STOCK_ITEM +
                " ON " + ALIAS_STOCK_ITEM + "." + FIELD_STOCK_ITEM_SALE_UNIT_OF_MEASURE_CODE +
                " = " + ALIAS_UNIT_OF_MEASURE + "." + FIELD_UNIT_OF_MEASURE_CODE);

        sql.addOuterJoinQualifier(" LEFT JOIN " + TABLE_SERIALIZED_ITEM_LABEL_I8 + " " + ALIAS_SERIALIZED_ITEM_LABEL_I8 +
                " ON " + ALIAS_STOCK_ITEM + "." + FIELD_UIN_LABEL_ID +
                " = " + ALIAS_SERIALIZED_ITEM_LABEL_I8 + "." + FIELD_UIN_LABEL_ID +
                " AND " + ALIAS_SERIALIZED_ITEM_LABEL_I8 + "." + FIELD_LOCALE +
                buildINClauseString(LocaleMap.getBestMatch("", localeRequestor.getLocales())));

        // for the specific item
        sql.addQualifier(new SQLParameterValue(ALIAS_STOCK_ITEM, FIELD_ITEM_ID, itemID));

        try
        {
            ResultSet rs = execute(dataConnection, sql);
            Locale locale = null;
            UnitOfMeasureIfc pluUOM = DomainGateway.getFactory().getUnitOfMeasureInstance();
            while (rs.next())
            {
                // parse the result set
                int index = 0;
                String uomCode = getSafeString(rs, ++index);
                boolean isSerializedItem = getBooleanFromString(rs, ++index);
                boolean isMetric = getBooleanFromString(rs, ++index);
                locale = LocaleUtilities.getLocaleFromString(getSafeString(rs, ++index));
                String uomName = getSafeString(rs, ++index);
                /* String uomDescription = */getSafeString(rs, ++index);
                boolean isDefaultUOM = getBooleanFromString(rs, ++index);
                /* String colorCode = */getSafeString(rs, ++index);
                /* String sizeCode = */getSafeString(rs, ++index);
                /* String styleCode = */getSafeString(rs, ++index);
                boolean hasRestockingFee = getBooleanFromString(rs, ++index);
                BigDecimal itemWeight = getBigDecimal(rs, ++index);
                boolean isExternalSystemCreateUINAllowed = getBooleanFromString(rs, ++index);
                String serialEntryTime = getSafeString(rs, ++index);
                String UINLabel = getSafeString(rs, ++index);

                // Determine if we need to make the unit of measure object for
                // this PLU item
                // if the uom is not the default value, then make the uom
                // reference
                if (!isDefaultUOM) // not the default, make uom reference object
                {
                    pluUOM.setName(locale, uomName);
                    pluUOM.setMetric(isMetric);
                    pluUOM.setUnitID(uomCode);
                    pluItem.setUnitOfMeasure(pluUOM);
                }
                pluItem.setItemWeight(itemWeight);
                pluItem.getItemClassification().setSerializedItem(isSerializedItem);
                pluItem.getItemClassification().setRestockingFeeFlag(hasRestockingFee);
                pluItem.getItemClassification().setExternalSystemCreateUIN(isExternalSystemCreateUINAllowed);
                pluItem.getItemClassification().setSerialEntryTime(serialEntryTime);
                pluItem.getItemClassification().setUINLabel(UINLabel);
            }
        }
        catch (DataException de)
        {
            logger.warn(de.toString());
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "StockItem lookup");
            throw new DataException(DataException.SQL_ERROR, "StockItem lookup", se);
        }
        catch (Exception e)
        {
            throw new DataException(DataException.UNKNOWN, "StockItem lookup", e);
        }
    }

    /**
     * Returns a new PLUItemIfc
     *
     * @return a new PLUItemIfc
     */
    static protected PLUItemIfc instantiatePLUItem()
    {
        return (DomainGateway.getFactory().getPLUItemInstance());
    }

    /**
     * Returns a new ItemKitIfc
     *
     * @return a new ItemKitIfc
     */
    static protected ItemKitIfc instantiateItemKit()
    {
        return DomainGateway.getFactory().getItemKitInstance();
    }

    /**
     * Returns a new KitComponentIfc
     *
     * @return a new KitComponentIfc
     */
    static protected KitComponentIfc instantiateKitComponent()
    {
        return DomainGateway.getFactory().getKitComponentInstance();
    }

    /**
     * Returns a new GiftCardPLUItemIfc
     *
     * @return a new GiftCardPLUItemIfc
     */
    static protected GiftCardPLUItemIfc instantiateGiftCardPLUItem(boolean openAmount)
    {
        GiftCardIfc gc = DomainGateway.getFactory().getGiftCardInstance();

        /* TODO figure out how to get price for gift card
         * gc.setInitialBalance(amount); gc.setCurrentBalance(amount);
         */
        gc.setOpenAmount(openAmount);
        GiftCardPLUItemIfc gcpi = DomainGateway.getFactory().getGiftCardPLUItemInstance();

        gcpi.setGiftCard(gc);

        return gcpi;
    }

    /**
     * Returns a new AlterationPLUItemIfc
     *
     * @return a new AlterationPLUItemIfc
     */
    static protected AlterationPLUItemIfc instantiateAlterationPLUItem()
    {
        AlterationIfc alt = DomainGateway.getFactory().getAlterationInstance();
        AlterationPLUItemIfc alterationPLU = DomainGateway.getFactory().getAlterationPLUItemInstance();
        alterationPLU.setAlteration(alt);

        return alterationPLU;
    }

    /**
     * Assign the tax rules to the array of PLUItems in parameter items.
     *
     * @param connection
     * @param items
     * @param geoCode
     * @throws DataException
     */
    public void assignTaxRules(JdbcDataConnection connection, PLUItemIfc[] items, String geoCode) throws DataException
    {
        if (geoCode != null && geoCode.length() > 0)
        {
            for (int i = 0; i < items.length; i++)
            {
                if (items[i].isKitHeader())
                {
                    ItemKitIfc kitItem = (ItemKitIfc)items[i];
                    KitComponentIfc[] kitComponents = kitItem.getComponentItems();

                    for (int j = 0; j < kitComponents.length; j++)
                    {
                        NewTaxRuleIfc[] rules = retrieveItemTaxRules(connection, geoCode, kitComponents[j]);
                        kitComponents[j].setTaxRules(rules);
                    }
                }
                else
                {
                    NewTaxRuleIfc[] rules = retrieveItemTaxRules(connection, geoCode, items[i]);
                    items[i].setTaxRules(rules);
                }
            }
        }
    }

    /**
     * Retrieves the tax rules first by using the item's tax group ID. If that
     * fails then attempt to lookup tax rules by the item's department's tax
     * group ID.
     *
     * @param connection
     * @param geoCode
     * @param lineItem
     * @return The tax rules for the item if successful.
     * @throws DataException
     */
    protected NewTaxRuleIfc[] retrieveItemTaxRules(JdbcDataConnection connection, String geoCode, PLUItemIfc lineItem)
            throws DataException
    {
        int taxGroupID = lineItem.getTaxGroupID();
        NewTaxRuleIfc[] rules = null;
        boolean usedDepartmentTaxGroupID = false;
        try
        {
            rules = retrieveItemTaxRules(connection, taxGroupID, geoCode);
            if (rules == null || rules.length == 0)
            {
                // this used to be a FATAL but is now just a WARN
                logger.warn("Tax lookup failed for item's tax group ID, attempting to lookup tax rules "
                        + "based on item's department's tax group ID");
                usedDepartmentTaxGroupID = true;
                taxGroupID = lineItem.getDepartment().getTaxGroupID();
                rules = retrieveItemTaxRules(connection, taxGroupID, geoCode);
                if (rules == null || rules.length == 0)
                {
                    logger.warn("Tax lookup failed for departments's tax group ID");
                }
            }
        }
        catch (DataException e)
        {
            // this used to be a FATAL but is now just a WARN
            logger.warn("Tax lookup failed for item's tax group ID, attempting to lookup tax rules "
                    + "based on item's department's tax group ID", e);
            if (usedDepartmentTaxGroupID == false)
            {
                taxGroupID = lineItem.getDepartment().getTaxGroupID();
                rules = retrieveItemTaxRules(connection, taxGroupID, geoCode);
                if (rules == null || rules.length == 0)
                {
                    logger.warn("Tax lookup failed for departments's tax group ID");
                }
            }
        }
        return rules;
    }

    /**
     * Request for all tax rules, regardless of taxGroupID. This is to get a set
     * of rules for a store.
     *
     * @param dataConnection
     * @param geoCode
     * @return taxRules for the geoCode.
     * @throws DataException on error
     */
    public ArrayList<Integer> retrieveItemTaxRules(JdbcDataConnection dataConnection, String geoCode) throws DataException
    {
        // Get a list of all taxGroupIDS.
        try
        {
            SQLSelectStatement sql = new SQLSelectStatement();
            sql.addTable(TABLE_TAXABLE_GROUP);
            sql.addColumn(FIELD_TAX_GROUP_ID);

            // Finish getting all taxGroupIds
            ArrayList<Integer> taxGroupIds = new ArrayList<Integer>();
            ResultSet resultSet = execute(dataConnection, sql);
            if (resultSet != null)
            {
                while (resultSet.next())
                {
                    int taxGroupId = resultSet.getInt(1);
                    taxGroupIds.add(Integer.valueOf(taxGroupId));
                }
            }
            resultSet.close();
            return taxGroupIds;
        }
        catch (SQLException sqlException)
        {
            dataConnection.logSQLException(sqlException, "PLUItem lookup -- Error Retrieving Tax Group IDS");
            throw new DataException(DataException.SQL_ERROR, "PLUItem lookup", sqlException);
        }
    }

    /**
     * Retrieve the tax rules, based on taxGroupID and GeoCode. These two keys
     * define a tax rule.
     *
     * @param dataConnection connection to the database.
     * @param taxGroupID taxGroup ID to use when selecting tax rules from the
     *            DB.
     * @param geoCode GeoCode to use when selecting tax rules from the DB.
     * @return TaxRuleIfc[] array of tax rules.
     * @throws DataException When there are problems reading the rules
     */
    public NewTaxRuleIfc[] retrieveItemTaxRules(JdbcDataConnection dataConnection, int taxGroupID, String geoCode)
            throws DataException
    {
        ResultSet resultSet = null;
        try
        {
            ArrayList<FFTaxVO> ffTaxVOs = new ArrayList<FFTaxVO>();
            TaxGroupInformationHolder[] taxGroupInformation = retrieveTaxGroupInformation(dataConnection, taxGroupID,
                    geoCode);
            for (int i = 0; i < taxGroupInformation.length; i++)
            {
                FFTaxVO ffTaxVO = new FFTaxVO();
                ffTaxVO.setTaxAuthorityId(taxGroupInformation[i].taxAuthority);
                ffTaxVO.setTaxGroupId(taxGroupID);
                ffTaxVO.setTaxType(taxGroupInformation[i].taxType);
                ffTaxVO.setTaxRuleName(taxGroupInformation[i].taxRuleName);
                ffTaxVO.setCompoundSequenceNumber(taxGroupInformation[i].compoundSequenceNumber);
                ffTaxVO.setTaxOnGrossAmountFlag(taxGroupInformation[i].taxOnGrossAmountFlag);
                ffTaxVO.setCalculationMethodCode(taxGroupInformation[i].calculationMethodCode);
                ffTaxVO.setTaxRateRuleUsageCode(taxGroupInformation[i].taxRateUsageCode);
                ffTaxVO.setRoundingCode(taxGroupInformation[i].roundingCode);
                ffTaxVO.setRoundingDigits(taxGroupInformation[i].roundingDigits);
                ffTaxVO.setTaxHoliday(taxGroupInformation[i].taxHoliday);
                ffTaxVO.setInclusiveTaxFlag(taxGroupInformation[i].inclusiveTaxFlag);

                SQLSelectStatement sql = new SQLSelectStatement();
                sql.addTable(TABLE_TAX_RATE_RULE);

                sql.addColumn(FIELD_TYPE_CODE);
                sql.addColumn(FIELD_TAX_PERCENTAGE);
                sql.addColumn(FIELD_TAX_AMOUNT);
                sql.addColumn(FIELD_TAX_ABOVE_THRESHOLD_AMOUNT_FLAG);
                sql.addColumn(FIELD_TAX_THRESHOLD_AMOUNT);
                sql.addColumn(FIELD_MINIMUM_TAXABLE_AMOUNT);
                sql.addColumn(FIELD_MAXIMUM_TAXABLE_AMOUNT);
                sql.addColumn(FIELD_TAX_RATE_EXPIRATION_TIMESTAMP);
                sql.addColumn(FIELD_TAX_RATE_EFFECTIVE_TIMESTAMP);

                sql.addQualifier(new SQLParameterValue(FIELD_TAX_AUTHORITY_ID, taxGroupInformation[i].taxAuthority));
                sql.addQualifier(new SQLParameterValue(FIELD_TAX_GROUP_ID, taxGroupID));
                sql.addQualifier(new SQLParameterValue(FIELD_TAX_TYPE, taxGroupInformation[i].taxType));
                sql.addQualifier(new SQLParameterValue(FIELD_TAX_HOLIDAY, taxGroupInformation[i].taxHoliday));
                sql.addOrdering(FIELD_TAX_RATE_RULE_SEQUENCE_NUMBER);

                resultSet = execute(dataConnection, sql);
                while (resultSet.next())
                {
                    int index = 0;
                    ffTaxVO.setTaxTypeCode(resultSet.getInt(++index));
                    BigDecimal pct = resultSet.getBigDecimal(++index);
                    if (pct != null)
                    {
                        pct.setScale(TAX_PERCENTAGE_SCALE);
                        ffTaxVO.setTaxPercentage(pct);
                    }
                    ffTaxVO.setTaxAmount(getCurrencyFromDecimal(resultSet, ++index));
                    ffTaxVO.setTaxAboveThresholdAmountFlag(getBooleanFromString(resultSet, ++index));
                    ffTaxVO.setTaxThresholdAmount(getCurrencyFromDecimal(resultSet, ++index));
                    ffTaxVO.setMinimumTaxableAmount(getCurrencyFromDecimal(resultSet, ++index));
                    ffTaxVO.setMaximumTaxableAmount(getMaximumTaxableAmount(resultSet.getString(++index)));
                    ffTaxVO.setTaxRateExpirationTimestamp(JdbcDataOperation.dateToEYSDate(resultSet, ++index));
                    ffTaxVO.setTaxRateEffectiveTimestamp(JdbcDataOperation.dateToEYSDate(resultSet, ++index));
                    ffTaxVOs.add(ffTaxVO);
                    ffTaxVO = (FFTaxVO)ffTaxVO.clone();
                }
                resultSet.close();
            }
            JdbcReadNewTaxRules reader = new JdbcReadNewTaxRules();
            return reader.retrieveItemTaxRules(ffTaxVOs);
        }
        catch (SQLException sqlException)
        {
            dataConnection.logSQLException(sqlException, "PLUItem lookup -- Error Retrieving Tax Calculator");
            throw new DataException(DataException.SQL_ERROR, "PLUItem lookup", sqlException);
        }
        finally
        {
            if (resultSet != null)
            {
                try
                {
                    resultSet.close();
                }
                catch (SQLException se)
                {
                    dataConnection.logSQLException(se, "PLUItem lookup -- Could not close result handle");
                }
            }
        }
    }

    /*
     * This method returns null if the database value is null.  This is required by
     * the Tax Table processing.  If the database value is not null, it returns the
     * value as a currency object.
     * @param amount String
     * @return CurrencyIfc
     */
    protected CurrencyIfc getMaximumTaxableAmount(String amount)
    {
        CurrencyIfc c = null;
        
        if (amount != null)
        {
            c = DomainGateway.getBaseCurrencyInstance(amount);
        }
        
        return c;
    }

    /**
     * Get information about the taxGroup we want tax information for. Example
     * query.
     * <p><blockquote><pre>
     * SELECT TXJURAUTHLNK.ID_ATHY_TX,
     *     TXRU.TY_TX,
     *     TXRU.FLG_TX_HDY,
     *     TXRU.NM_RU_TX,
     *     TXRU.DE_RU_TX,
     *     TXRU.AI_CMPND,
     *     TXRU.FL_TX_GS_AMT,
     *     TXRU.CD_CAL_MTH,
     *     TXRU.CD_TX_RT_RU_USG,
     *     TXRU.FL_TX_INC,
     *     ATHY.SC_RND,
     *     ATHY.QU_DGT_RND
     * FROM RU_TX_GP TXRU
     * LEFT JOIN PA_ATHY_TX ATHY ON ATHY.ID_ATHY_TX = TXRU.ID_ATHY_TX
     * LEFT JOIN CO_TX_JUR_ATHY_LNK TXJURAUTHLNK ON TXJURAUTHLNK.ID_ATHY_TX = ATHY.ID_ATHY_TX
     * WHERE TXRU.ID_GP_TX = '100'
     * AND TXJURAUTHLNK.ID_CD_GEO = '78729'
     * ORDER BY TXRU.AI_CMPND
     * </pre></blockquote>
     *
     * @param dataConnection
     * @param taxGroupId
     * @param geoCode
     * @return TaxGroup information
     * @throws DataException
     */
    public TaxGroupInformationHolder[] retrieveTaxGroupInformation(JdbcDataConnection dataConnection, int taxGroupId,
            String geoCode) throws DataException
    {
        ResultSet resultSet = null;
        ArrayList<TaxGroupInformationHolder> taxGroupInformations = new ArrayList<TaxGroupInformationHolder>();

        try
        {
            SQLSelectStatement sql = new SQLSelectStatement();
            // add tables
            sql.addTable(TABLE_TAX_GROUP_RULE, ALIAS_TAX_GROUP_RULE);
            // add column

            sql.addColumn(ALIAS_TABLE_TAX_JURISDICTION_AUTH_LNK, FIELD_TAX_AUTHORITY_ID);
            sql.addColumn(ALIAS_TAX_GROUP_RULE, FIELD_TAX_TYPE);
            sql.addColumn(ALIAS_TAX_GROUP_RULE, FIELD_TAX_HOLIDAY);
            sql.addColumn(ALIAS_TAX_GROUP_RULE, FIELD_TAX_RULE_NAME);
            sql.addColumn(ALIAS_TAX_GROUP_RULE, FIELD_TAX_RULE_DESCRIPTION);
            sql.addColumn(ALIAS_TAX_GROUP_RULE, FIELD_COMPOUND_SEQUENCE_NUMBER);
            sql.addColumn(ALIAS_TAX_GROUP_RULE, FIELD_TAX_ON_GROSS_AMOUNT_FLAG);
            sql.addColumn(ALIAS_TAX_GROUP_RULE, FIELD_CALCULATION_METHOD_CODE);
            sql.addColumn(ALIAS_TAX_GROUP_RULE, FIELD_TAX_RATE_RULE_USAGE_CODE);
            sql.addColumn(ALIAS_TAX_GROUP_RULE, FIELD_FLG_TAX_INCLUSIVE);
            sql.addColumn(ALIAS_TAX_AUTHORITY, FIELD_ROUNDING_CODE);
            sql.addColumn(ALIAS_TAX_AUTHORITY, FIELD_ROUNDING_DIGITS_QUANTITY);
            // add joins
            sql.addOuterJoinQualifier("LEFT", TABLE_TAX_AUTHORITY + " " + ALIAS_TAX_AUTHORITY,
                    ALIAS_TAX_AUTHORITY, FIELD_TAX_AUTHORITY_ID, ALIAS_TAX_GROUP_RULE, FIELD_TAX_AUTHORITY_ID);
            sql.addOuterJoinQualifier("LEFT", TABLE_TAX_JURISDICTION_AUTH_LNK + " " + ALIAS_TABLE_TAX_JURISDICTION_AUTH_LNK,
                    ALIAS_TABLE_TAX_JURISDICTION_AUTH_LNK, FIELD_TAX_AUTHORITY_ID, ALIAS_TAX_GROUP_RULE, FIELD_TAX_AUTHORITY_ID);
            // add qualifiers
            sql.addQualifier(new SQLParameterValue(ALIAS_TAX_GROUP_RULE, FIELD_TAX_GROUP_ID, taxGroupId));
            sql.addQualifier(new SQLParameterValue(ALIAS_TABLE_TAX_JURISDICTION_AUTH_LNK, FIELD_GEO_CODE, geoCode));

            sql.addOrdering(ALIAS_TAX_GROUP_RULE + "." + FIELD_COMPOUND_SEQUENCE_NUMBER);

            resultSet = execute(dataConnection, sql);

            if (resultSet != null)
            {
                TaxGroupInformationHolder taxGroupInformation = null;
                while (resultSet.next())
                {
                    int index = 0;
                    taxGroupInformation = new TaxGroupInformationHolder();
                    taxGroupInformation.taxAuthority = resultSet.getInt(++index);
                    taxGroupInformation.taxType = resultSet.getInt(++index);
                    taxGroupInformation.taxHoliday = getBooleanFromString(resultSet, ++index);
                    taxGroupInformation.taxRuleName = getSafeString(resultSet, ++index);
                    taxGroupInformation.taxRuleDescription = getSafeString(resultSet, ++index);
                    taxGroupInformation.compoundSequenceNumber = resultSet.getInt(++index);
                    taxGroupInformation.taxOnGrossAmountFlag = getBooleanFromString(resultSet, ++index);
                    taxGroupInformation.calculationMethodCode = resultSet.getInt(++index);
                    taxGroupInformation.taxRateUsageCode = resultSet.getInt(++index);
                    taxGroupInformation.inclusiveTaxFlag = getBooleanFromString(resultSet, ++index);
                    taxGroupInformation.roundingCode = resultSet.getInt(++index);
                    taxGroupInformation.roundingDigits = resultSet.getInt(++index);
                    taxGroupInformations.add(taxGroupInformation);
                }
            }
        }
        catch (SQLException sqlException)
        {
            dataConnection.logSQLException(sqlException, "PLUItem lookup -- Error Retrieving Tax Rule");
            throw new DataException(DataException.SQL_ERROR, "PLUItem lookup", sqlException);
        }
        finally
        {
            if (resultSet != null)
            {
                try
                {
                    resultSet.close();
                }
                catch (SQLException se)
                {
                    dataConnection.logSQLException(se, "PLUItem lookup -- Could not close result handle");
                }
            }
        }
        return taxGroupInformations.toArray(new TaxGroupInformationHolder[taxGroupInformations.size()]);
    }

    /**
     * Retrieves the planogramIDs for an item and store
     *
     * @param dataConnection
     * @param itemID
     * @param storeID
     * @return String[]
     * @throws DataException
     */
    public String[] getPlanogramIDForItem(JdbcDataConnection dataConnection, String itemID, String storeID)
            throws DataException
    {
        String planogramID[] = null;
        ResultSet planogramResultSet = null;
        try
        {
            SQLSelectStatement sql = new SQLSelectStatement();

            sql.addTable(TABLE_PLANOGRAM, ALIAS_PLANOGRAM);
            sql.addTable(TABLE_ITEM_POG_ASSOCIATION, ALIAS_ITEM_POG_ASSOCIATION);

            sql.addColumn(ALIAS_PLANOGRAM + "." + FIELD_EXT_PLANOGRAM_ID);

            sql.addJoinQualifier(ALIAS_PLANOGRAM, FIELD_PLANOGRAM_ID, ALIAS_ITEM_POG_ASSOCIATION, FIELD_PLANOGRAM_ID);
            sql.addQualifier(new SQLParameterValue(ALIAS_ITEM_POG_ASSOCIATION, FIELD_ITEM_ID, itemID));
            sql.addQualifier(new SQLParameterValue(ALIAS_ITEM_POG_ASSOCIATION, FIELD_RETAIL_STORE_ID, storeID));
            sql.addOrdering(ALIAS_PLANOGRAM + "." + FIELD_PLANOGRAM_ID);

            ArrayList<String> resultList = new ArrayList<String>();
            planogramResultSet = execute(dataConnection, sql);

            if (planogramResultSet != null)
            {
                while (planogramResultSet.next())
                {
                    int index = 0;
                    String planOgramID = getSafeString(planogramResultSet, ++index);
                    resultList.add(planOgramID);
                }
            }
            planogramID = resultList.toArray(new String[resultList.size()]);
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "readPlanogramID");
            throw new DataException(DataException.SQL_ERROR, "readPlanogramID", se);
        }
        finally
        {
            if (planogramResultSet != null)
            {
                try
                {
                    planogramResultSet.close();
                }
                catch (SQLException se)
                {
                    dataConnection.logSQLException(se, "PLUItem lookup -- Could not close result handle");
                }
            }
        }
        return planogramID;
    }

    /**
     * Returns the a Locale Requestor with all potentially required locales
     *
     * @return A locale requestor object with all required locales.
     */
    public static LocaleRequestor getRequestLocales(Locale defaultLocale)
    {
        Locale[] supportedLocales = LocaleMap.getSupportedLocales();
        Locale defaultLocaleMatch = LocaleMap.getBestMatch(defaultLocale);
        int defaultIndex = 0;
        for (int i = 0; i < supportedLocales.length; i++)
        {
            if (defaultLocaleMatch.equals(supportedLocales[i]))
            {
                defaultIndex = i;
                break;
            }
        }
        return new LocaleRequestor(supportedLocales, defaultIndex);
    }

    /**
     * Retreives Item Messages per Item from the Db and sets it into the PLU
     * Item Object
     *
     * @return void
     * @param connection
     * @param items
     * @throws DataException
     */
    public void getItemMessages(JdbcDataConnection connection, PLUItemIfc[] items)
    {
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
     * Method which gets the ILRM Message for a Given Item The Catch block
     * simply prints the exception caused during execution as the requirement is
     * to just print the error not propogate it
     *
     * @param dataConnection
     * @param item
     * @throws DataException
     */
    public void getItemLevelMessages(JdbcDataConnection dataConnection, PLUItemIfc item)
    {
        if (item != null)
        {
            SQLSelectStatement sql = new SQLSelectStatement();
            MessageDTO mdto = null;
            List<MessageDTO> messageList = new ArrayList<MessageDTO>();
            Map<String, List<MessageDTO>> messagesMap = new HashMap<String, List<MessageDTO>>();
            ResultSet rs = null;
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
            sql.addQualifier(new SQLParameterValue(TABLE_ITEM_MESSAGE_ASSOCIATION, FIELD_ITEM_ID, item.getItemID()));
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
                logger.debug(str);// execute the query and get the result set
                rs = execute(dataConnection, sql);

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
                        mdto.addLocalizedItemMessage(LocaleUtilities.getLocaleFromString(rs.getString(FIELD_LOCALE)), rs
                                .getString(FIELD_MESSAGE_DESCRIPTION));
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
                    mdto.addLocalizedItemMessage(LocaleUtilities.getLocaleFromString(rs.getString(FIELD_LOCALE)), rs
                            .getString(FIELD_MESSAGE_DESCRIPTION));

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
                logger.error("Unexpected exception in readRelatedItems " + e);
            }
            finally
            {
                if (rs != null)
                {
                    try
                    {
                        rs.close();
                    }
                    catch (SQLException se)
                    {
                        logger.error(se);
                    }
                }
            }
        }
    }

    /**
     * Returns true if a SQLParameter value contains one or more "%" or "*" characters
     * @param qualifiers a list of qualifiers
     * @return true if a wildcard is used in any of the supplied list of SQLParameters
     */
    static public boolean usesWildcards(List<SQLParameterIfc> qualifiers)
    {
        if (qualifiers != null)
        {
            for (SQLParameterIfc qualifier : qualifiers)
            {
                if (qualifier.toString().indexOf("%") >= 0 || qualifier.toString().indexOf("*") >= 0)
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    public void readCpnAttrDtls(JdbcDataConnection dataConnection,PLUItemIfc pluItem)
            throws DataException
 {

		
		String itemId = pluItem.getItemID();
		// add tables
		SQLSelectStatement sql = new SQLSelectStatement();
		sql.addTable("CT_COUPON_ATTR");
		sql.addColumn("COUPON_ID");
		sql.addColumn("COUPON_TYPE");
		sql.addColumn("ITM_APPLY_TO");
		sql.addColumn("MIN_MO_TH");
		sql.addColumn("MAX_MO_TH");
		sql.addColumn("MAX_DISC_AMT");
		sql.addColumn("WS_VLDT_FLAG");
		sql.addColumn("TS_CRT_RCRD");
		sql.addColumn("TS_MDF_RCRD");

		sql.addQualifier("COUPON_ID", "'"+itemId+"'");
		
		GDYNLoyalityCpnAttrDtls gdynLoyalityCpnAttrDtls = pluItem.getLoyalityCpnAttrDtls();
				
		// perform the query
		try {
			String sqlToRun = sql.getSQLString();
			logger.info("SQL Query to get coupon attribute details for item "+itemId+" :     "+ sqlToRun);
			dataConnection.execute(sqlToRun);
			ResultSet rs = (ResultSet) dataConnection.getResult();
			while (rs.next())
			{
				String loyCpnId = getSafeString(rs, 1);
				String loylCpnType = getSafeString(rs, 2);
				String itmApplyTo = getSafeString(rs, 3);
				String minMonthThrhold = getSafeString(rs, 4);
				String maxMoThrhold=  getSafeString(rs, 5);
				String maxDiscAmt=  getSafeString(rs,6);
				String validityFlag = getSafeString(rs, 7);

							
				if (!Util.isEmpty(loyCpnId)) {
					gdynLoyalityCpnAttrDtls.setLoylCpnId(loyCpnId);
				}
				if (!Util.isEmpty(loylCpnType)) {
					gdynLoyalityCpnAttrDtls.setLoylCpnType(loylCpnType);
				}
				if (!Util.isEmpty(itmApplyTo)) {
					gdynLoyalityCpnAttrDtls.setItmApplyTo(itmApplyTo);
				}
				if (!Util.isEmpty(minMonthThrhold)) {
					gdynLoyalityCpnAttrDtls.setMinMonthlyThrhold(new BigDecimal(minMonthThrhold));
				}
				if (!Util.isEmpty(maxMoThrhold)) {
					gdynLoyalityCpnAttrDtls.setMaxMonthlyThrhold(new BigDecimal(maxMoThrhold));
				}
				if (!Util.isEmpty(maxDiscAmt)) {
					gdynLoyalityCpnAttrDtls.setMaxDiscAmount(new BigDecimal(maxDiscAmt));
				}
				if (!Util.isEmpty(validityFlag)) {
					gdynLoyalityCpnAttrDtls.setValidityFlag(validityFlag);
				}
				pluItem.setLoyalityCpnAttrDtls(gdynLoyalityCpnAttrDtls);

				
			}
			
			rs.close();
			
		} catch (DataException de) 
		{
			logger.error("Exception while reading coupon attribute details" +de);
			throw de;
		} 
		catch (SQLException se) 
		{
			dataConnection.logSQLException(se, "JdbcPLUOperation");
			throw new DataException(DataException.SQL_ERROR,
					"readCpnAttrDtls", se);
		} 
		catch (Exception e) 
		{
			logger.error("Exception while reading coupon attribute details" +e);
			throw new DataException(DataException.UNKNOWN, "JdbcPLUOperation",e);
		}
		
	}


    
    public void readCpnHierarchyDtls(JdbcDataConnection dataConnection,PLUItemIfc pluItem)
            throws DataException
 {

		logger.info("inside readCpnHierarchyDtls JdbcPLUOperation for item id "+  pluItem.getItemID());
		
		
		String itemId = pluItem.getItemID();
		// add tables
		SQLSelectStatement sql = new SQLSelectStatement();
		sql.addTable("CT_COUPON_HIER");
		sql.addColumn("COUPON_ID");
		sql.addColumn("DISCOUNT_DIVISION");
		sql.addColumn("DISCOUNT_GROUP");
		sql.addColumn("DISCOUNT_DPT");
		sql.addColumn("DISCOUNT_CLASS");
		sql.addColumn("DISCOUNT_SUBCLS");
		
		sql.addQualifier("COUPON_ID",  inQuotes(itemId));

		List<GDYNLoyalityCpnHrchyDtls> loyalityCpnHrchyDtlsList =  pluItem.getLoyalityCpnHrchyDtlsList();;
		
		// perform the query
		try {
			String sqlToRun = sql.getSQLString();
			logger.info("SQL Query to get coupon hierarchy details for item "+itemId+" :     "+ sqlToRun);		dataConnection.execute(sqlToRun);
			ResultSet rs = (ResultSet) dataConnection.getResult();
			
			while (rs.next()) {
				GDYNLoyalityCpnHrchyDtls loyalityCpnHrchyDtls = new GDYNLoyalityCpnHrchyDtls();
				loyalityCpnHrchyDtls.setLoylCpnId(getSafeString(rs, 1));
				loyalityCpnHrchyDtls.setDiscountDivision(getSafeString(rs, 2));
				loyalityCpnHrchyDtls.setDiscountGroup(getSafeString(rs, 3));
				loyalityCpnHrchyDtls.setDiscountDept(getSafeString(rs, 4));
				loyalityCpnHrchyDtls.setDiscountClass(getSafeString(rs, 5));
				loyalityCpnHrchyDtls.setDiscountSubclass(getSafeString(rs, 6));
				loyalityCpnHrchyDtlsList.add(loyalityCpnHrchyDtls);			
					}
			
			pluItem.setLoyalityCpnHrchyDtlsList(loyalityCpnHrchyDtlsList);
			rs.close();

		} catch (DataException de) {
			logger.error("Exception while reading coupon hierarchy details" +de);
			throw de;
		} catch (SQLException se) {
			dataConnection.logSQLException(se, "JdbcPLUOperation");
			throw new DataException(DataException.SQL_ERROR,
					"readCpnHierarchyDtls", se);
		} catch (Exception e)
		{
			logger.error("Exception while reading coupon hierarchy details" +e);
			throw new DataException(DataException.UNKNOWN, "JdbcPLUOperation",e);
			
		}
		
	}

    
    public  void readLoyalityMerchHrchyDtls(JdbcDataConnection dataConnection,PLUItemIfc pluItemIfc)
            throws DataException
    {
    	
    	String merchGrpId = "'"+pluItemIfc.getItem().getItemClassification().getMerchandiseHierarchyGroup()+"'";
    	
        // add tables
    	/*code changes added by dharmendra on 26/08/2016 to fix POS-218*/
        SQLSelectStatement sql = new SQLSelectStatement();
        sql.addTable("MERCHANDISE_HIERARCHY_VIEW");
        sql.addColumn("ID_MRHRC_GP");
        sql.addColumn("DIV");
        sql.addColumn("GRP");
        sql.addColumn("DPT");
        sql.addColumn("CLS");
        sql.addColumn("SCL");
      
        sql.addQualifier("ID_MRHRC_GP", merchGrpId);
        
        
        // perform the query
        try
 {
			String sqlToRun = sql.getSQLString();
			logger.info("1111 sqlToRun to get merchandise details for item id "+ pluItemIfc.getItemID()+" " + sqlToRun);
			dataConnection.execute(sqlToRun);
			ResultSet rs = (ResultSet) dataConnection.getResult();
			GDYNLoyalityMerchHrchyDtls loyalityMerchHrchyDtls = pluItemIfc
					.getLoyalityMerchHrchyDtls();
			while (rs.next()) {

				String divisionId = getSafeString(rs, 2);
				String groupId = getSafeString(rs, 3);
				String deptId = getSafeString(rs, 4);
				String classId = getSafeString(rs, 5);
				String subClassId = getSafeString(rs, 6);

				/*loyalityMerchHrchyDtls.setDivisionId(divisionId);
				loyalityMerchHrchyDtls.setGroupId(groupId);
				loyalityMerchHrchyDtls.setDeptId(deptId);
				loyalityMerchHrchyDtls.setClassId(classId);
				loyalityMerchHrchyDtls.setSubClassId(subClassId);
				pluItemIfc.setItemDivision(divisionId);*/
				logger.info("1111   Merchandise Details for item "
						+ pluItemIfc.getItemID() + " divisionId : " + divisionId + " groupId : "
						+ groupId + " deptId : " + deptId + " classId : " + classId + " subClassId : "
						+ subClassId);
			}
			rs.close();
		}
        catch (DataException de)
        {
        	logger.error("Exception while reading merchandise hierarchy view" +de);
            throw de;
        }
        catch (SQLException se)
        {
            dataConnection.logSQLException(se, "JdbcPLUOperation");
            throw new DataException(DataException.SQL_ERROR, "JdbcPLUOperation", se);
        }
        catch (Exception e)
        {  
        	logger.error("Exception while reading merchandise hierarchy view" +e);
            throw new DataException(DataException.UNKNOWN, "JdbcPLUOperation", e);
        }

        
       
        
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
