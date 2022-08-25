package model;

public class QueueMessage {
	private String userName;
	private TLVMessage tlv;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public TLVMessage getTlv() {
		return tlv;
	}
	public void setTlv(TLVMessage tlv) {
		this.tlv = tlv;
	}
	
	
}
