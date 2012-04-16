/**
 * 
 */
package info.kwarc.sissi.ooalex.impl;

import info.kwarc.sissi.Message;
import info.kwarc.sissi.ooalex.OOMessage;

/**
 * @author cdavid
 * 
 */
public class OOMessageImpl extends Message implements OOMessage {
	private String data;
	private short semObjPositionX;
	private short semObjPositionY;
	private String semObjID;

	@Override
	public String getData() {
		return data;
	}

	@Override
	public String getSemObjID() {
		return semObjID;
	}

	@Override
	public short getSemObjPositionX() {
		return semObjPositionX;
	}

	@Override
	public short getSemObjPositionY() {
		return semObjPositionY;
	}

	@Override
	public void setData(String data) {
		this.data = data;
	}

	@Override
	public void setSemObjID(String semObjID) {
		this.semObjID = semObjID;
	}

	@Override
	public void setSemObjPositionX(short semObjPositionX) {
		this.semObjPositionX = semObjPositionX;
	}

	@Override
	public void setSemObjPositionY(short semObjPositionY) {
		this.semObjPositionY = semObjPositionY;
	}
}
