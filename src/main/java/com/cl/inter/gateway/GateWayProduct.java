package com.cl.inter.gateway;

/**
 * 通道产品 每一个产品对应联通、移动、电信三种类型，每种类型两根通道，一主，一辅。
 * 
 * @author zhu_tek
 * 
 */
public class GateWayProduct {

	public final static int CHINA_MOBILE = 0x01;
	public final static int CHINA_UNION = 0x02;
	public final static int CHINA_TELE = 0x03;

	public final static int[] CHINA_UNION_NUMBER = new int[] { 130, 131, 132, 145, 154, 155, 156, 185, 186 };
	public final static int[] CHINA_MOBILE_NUMBER = new int[] { 134, 135, 136, 137, 138, 139, 147, 150, 151, 152, 157,
			158, 159, 182, 183, 184, 187, 188 };
	public final static int[] CHINA_TELE_NUMBER = new int[] { 133, 153, 177, 178, 180, 181, 189 };

	// 产品ID
	private int productId;

	// 产品名称
	private String productName;

	// 移动主通道
	private int chinaMobileMaster;

	// 移动辅通道
	private int chinaMobileAssist;

	// 联通主通道
	private int chinaUnionMaster;

	// 联通辅通道
	private int chinaUnionAssist;

	// 电信主通道
	private int chinaTeleMaster;

	// 电信辅通道
	private int chinaTeleAssist;

	/**
	 * java bean 开始
	 * 
	 * @return null
	 */

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getChinaMobileMaster() {
		return chinaMobileMaster;
	}

	public void setChinaMobileMaster(int chinaMobileMaster) {
		this.chinaMobileMaster = chinaMobileMaster;
	}

	public int getChinaMobileAssist() {
		return chinaMobileAssist;
	}

	public void setChinaMobileAssist(int chinaMobileAssist) {
		this.chinaMobileAssist = chinaMobileAssist;
	}

	public int getChinaUnionMaster() {
		return chinaUnionMaster;
	}

	public void setChinaUnionMaster(int chinaUnionMaster) {
		this.chinaUnionMaster = chinaUnionMaster;
	}

	public int getChinaTeleMaster() {
		return chinaTeleMaster;
	}

	public void setChinaTeleMaster(int chinaTeleMaster) {
		this.chinaTeleMaster = chinaTeleMaster;
	}

	public int getChinaTeleAssist() {
		return chinaTeleAssist;
	}

	public void setChinaTeleAssist(int chinaTeleAssist) {
		this.chinaTeleAssist = chinaTeleAssist;
	}

	public int getChinaUnionAssist() {
		return chinaUnionAssist;
	}

	public void setChinaUnionAssist(int chinaUnionAssist) {
		this.chinaUnionAssist = chinaUnionAssist;
	}
}
