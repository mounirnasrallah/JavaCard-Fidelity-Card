package tp1_javacrad;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.JCSystem;

public class fidele extends Applet {
	
	final static byte INS_AUTH = 0x01;
	final static byte INS_COUNTER_INCR = 0x02;
	final static byte INS_COUNTER_RESET = 0x03;
	final static byte INS_CHANGE_PIN = 0x04;
	final static byte INS_PRINT_COUNTER = 0x05;
	final static byte CLA = 0x42;

	private byte[] pin = new byte[4]; 
	private byte counter[] = {0, 0};
	
	// Recast
	final static short zero = 0x0 ;
	final static short quatre = 0x4 ;
	
	static boolean[] authentified;
	
	
	private fidele() {
	
	}
	
	private void authentification(byte bArray[]){
		if(!authentified[0]){
			if(Util.arrayCompare(bArray, ISO7816.OFFSET_CDATA , pin, zero, quatre) == 0)
				authentified[0] = true ;
		}
	}
	
	
	private void print_counter(APDU apdu){
		byte tmp[] = apdu.getBuffer();
		tmp[0] = counter[0];
		tmp[0] = counter[1];
		apdu.setOutgoingAndSend((short)0, (short)2);
	}
	
	
	private void print_state(APDU apdu){
		byte tmp[] = apdu.getBuffer();
		if(authentified[0]){
			tmp[0] = (byte)0x90;
			tmp[1] = (byte)0x00;
		}
		else{
			tmp[0] = (byte)0x00;
			tmp[1] = (byte)0x00;
		}
		
		apdu.setOutgoingAndSend((short)0, (short)2);
	}
	
	
	private void changePïn(byte bArray[]){
		
		if(authentified[0])
			Util.arrayCopy(bArray, ISO7816.OFFSET_CDATA, pin, zero, quatre);
	}

	private void incrementeIncr(){
		if(authentified[0]){
				counter[0] += 1;	
		}
	}	
	
	private void resetCouter(){
		if(authentified[0]){
			counter[0] = 0;
		}
	}
	
	
	
	public static void install(byte bArray[], short bOffset, byte bLength)
			throws ISOException {
		new fidele().register();
		
	}
	
	
	public boolean select() {
	  
		authentified = JCSystem.makeTransientBooleanArray((short)1,JCSystem.CLEAR_ON_DESELECT);
		return true;
	}
	
	public void process(APDU arg0) throws ISOException {
		
		
		// Buffer temporaire pour stocker l'APDU
		byte[] buffer = arg0.getBuffer();
					
		// Verifie que l'Applet est sélectionnée 
		if (this.selectingApplet()) return;
		
		// Vérifie que le APDU a bien pour destination cette Applet
		if (buffer[ISO7816.OFFSET_CLA] != CLA) {
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}
		
		//setIncomingAndReceive
		
		// On switch le champ INS de l'APDU pour faire l'action qu'il faut
		switch (buffer[ISO7816.OFFSET_INS]){
			case INS_AUTH:
				arg0.setIncomingAndReceive();
				authentification(buffer);
				print_state(arg0);
				break;
				
			case INS_COUNTER_INCR:
				 incrementeIncr();	
				break;
				
			case INS_COUNTER_RESET:
				resetCouter();	
				break;
				
			case INS_CHANGE_PIN:
				arg0.setIncomingAndReceive();
				changePïn(buffer);
				break;
				
			case INS_PRINT_COUNTER:
				print_counter(arg0);
				break;
									
			default:
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
	}

	}
}
