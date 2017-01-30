package oracle.retail.stores.domain.stock;

import java.io.Serializable;

public class GDYNLoyalityMerchHrchyDtls implements Serializable{
 
	private static final long serialVersionUID = 1L;
	
	protected String  divisionId ;
	
	protected String groupId ;
	
	protected String deptId ;
	
	protected String classId ;
	
	protected String subClassId;

	/**
	 * @return the divisionId
	 */
	public String getDivisionId() {
		return divisionId;
	}

	/**
	 * @param divisionId the divisionId to set
	 */
	public void setDivisionId(String divisionId) {
		this.divisionId = divisionId;
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the deptId
	 */
	public String getDeptId() {
		return deptId;
	}

	/**
	 * @param deptId the deptId to set
	 */
	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	/**
	 * @return the classId
	 */
	public String getClassId() {
		return classId;
	}

	/**
	 * @param classId the classId to set
	 */
	public void setClassId(String classId) {
		this.classId = classId;
	}

	/**
	 * @return the subClassId
	 */
	public String getSubClassId() {
		return subClassId;
	}

	/**
	 * @param subClassId the subClassId to set
	 */
	public void setSubClassId(String subClassId) {
		this.subClassId = subClassId;
	}
}
