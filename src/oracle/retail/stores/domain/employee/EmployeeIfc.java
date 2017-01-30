/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  oracle.retail.stores.commerceservices.security.EmployeeComplianceIfc
 *  oracle.retail.stores.domain.employee.EmployeeTypeEnum
 *  oracle.retail.stores.domain.employee.RoleIfc
 *  oracle.retail.stores.domain.financial.HardTotalsDataIfc
 *  oracle.retail.stores.domain.utility.EYSDate
 *  oracle.retail.stores.domain.utility.EYSDomainIfc
 *  oracle.retail.stores.domain.utility.PersonNameIfc
 */
package oracle.retail.stores.domain.employee;

import java.util.Date;
import java.util.Locale;

import com.gdyn.co.employeediscount.response.GDYNEmployeeDiscResponseObject;

import oracle.retail.stores.commerceservices.security.EmployeeComplianceIfc;
import oracle.retail.stores.domain.employee.EmployeeTypeEnum;
import oracle.retail.stores.domain.employee.RoleIfc;
import oracle.retail.stores.domain.financial.HardTotalsDataIfc;
import oracle.retail.stores.domain.utility.EYSDate;
import oracle.retail.stores.domain.utility.EYSDomainIfc;
import oracle.retail.stores.domain.utility.PersonNameIfc;

public interface EmployeeIfc
extends EYSDomainIfc,
HardTotalsDataIfc {
    public static final int LOGIN_STATUS_UNKNOWN = 0;
    public static final int LOGIN_STATUS_ACTIVE = 1;
    public static final int LOGIN_STATUS_INACTIVE = 2;
    public static final String[] LOGIN_STATUS_DESCRIPTORS = new String[]{"Unknown", "Active", "Inactive"};
    public static final int MAXIMUM_TEMP_EMPOYEE_ID = 9999;
    public static final String PASSWORD_CHARSET = "UTF-8";
    public static final String revisionNumber = "$Revision: /rgbustores_13.4x_generic_branch/3 $";
    
   

    public void assimilate(EmployeeIfc var1);

    public String getAlternateID();

    public int getDaysValid();

    public EmployeeComplianceIfc getEmployeeCompliance();

    public String getEmployeeID();

    public EYSDate getExpirationDate();

    public String getFullName();

    public String getLoginID();

    public int getLoginStatus();

    public PersonNameIfc getName();

    public int getNumberFailedPasswords();

    public String getPassword();

    public byte[] getPasswordBytes();

    public String getEmployeePasswordSalt();

    public Date getPasswordCreationDate();

    public PersonNameIfc getPersonName();

    public Locale getPreferredLocale();

    public RoleIfc getRole();

    public String getStoreID();

    public EmployeeTypeEnum getType();

    public byte[] getFingerprintBiometrics();

    public Date getLastLoginTime();

    public boolean hasAccessToFunction(int var1);

    public boolean isPasswordChangeRequired();

    public String loginStatusToString();

    public void setAlternateID(String var1);

    public void setDaysValid(int var1);

    public void setEmployeeCompliance(EmployeeComplianceIfc var1);

    public void setEmployeeID(String var1);

    public void setExpirationDate(EYSDate var1);

    public void setFullName(String var1);

    public void setLoginID(String var1);

    public void setLoginStatus(int var1);

    public void setName(PersonNameIfc var1);

    public void setNumberFailedPasswords(int var1);

    public void setPassword(String var1);

    public void setPasswordBytes(byte[] var1);

    public void setEmployeePasswordSalt(String var1);

    public void setPasswordChangeRequired(boolean var1);

    public void setPasswordCreationDate(Date var1);

    public void setPersonName(PersonNameIfc var1);

    public void setPreferredLocale(Locale var1);

    public void setRole(RoleIfc var1);

    public void setStoreID(String var1);

    public void setType(EmployeeTypeEnum var1);

    public void setFingerprintBiometrics(byte[] var1);

    public void setLastLoginTime(Date var1);

    public String toJournalString();

    public String toJournalString(Locale var1);
    
    public GDYNEmployeeDiscResponseObject[] getResponseObject();
    
    public void setResponseObject(GDYNEmployeeDiscResponseObject[] responseObject);
}
