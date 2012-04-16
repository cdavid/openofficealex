/**
 * 
 */
package info.kwarc.sissi.ooalex.impl;

import java.util.Map;

import info.kwarc.sissi.Message;
import info.kwarc.sissi.ooalex.OOMessage;

/**
 * @author cdavid
 *
 */
public class MessageAdapter {
	static public OOMessage adapt(Message msg) {
		OOMessage ooMsg = new OOMessageImpl();
		ooMsg.setAction(msg.getAction());
		ooMsg.setReqId(msg.getReqId());
		Map<String,String> h = msg.getParamsAsMap();
		
		
		ooMsg.setData(h.get("data"));
		ooMsg.setSemObjID(h.get("semObjID"));
		ooMsg.setSemObjPositionX(Short.valueOf(h.get("semObjPositionX")));
		ooMsg.setSemObjPositionY(Short.valueOf(h.get("semObjPositionY")));
		
		return ooMsg;
	}
	
	static public Message adapt(OOMessage msg) {
		Message m = new Message();
		
		return m;
	}
}
