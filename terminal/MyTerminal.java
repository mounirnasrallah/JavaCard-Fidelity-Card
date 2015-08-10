package tp;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

public class MyTerminal {

	public final static byte[] APPLET_AID =
		{(byte) 0xB0, 0x00, 0x00, 0x01, 0x02, 0x03, 0x00};
	private final static CommandAPDU SELECT_APDU =
			new CommandAPDU(0x00, 0xA4, 0x04, 0x00, APPLET_AID);
	private final static CommandAPDU RESET_APDU =
			new CommandAPDU(0xB0, 0x00, 0x00, 0x00);
	private final static CommandAPDU GET_APDU =
			new CommandAPDU(0xB0, 0x02, 0x00, 0x00);
	private final static CommandAPDU INC_APDU =
			new CommandAPDU(0xB0, 0x04, 0x00, 0x00);

	CardTerminal myterm;
	CardChannel mychannel;

	public MyTerminal() throws CardException {
		CardTerminals terminals = TerminalFactory.getDefault().terminals();
		// supports only one connected reader
		myterm = terminals.list().get(0);
		mychannel = null;
	}

	public int getCounter() throws CardException {
		ResponseAPDU r;
		if (!myterm.isCardPresent())
			throw new CardNotPresentException("no card in reader");
		ensureSession();
		r = mychannel.transmit(GET_APDU);
		switch (r.getSW()) {
		case 0x9000:
			return s2u(r.getData()[0]) | (s2u(r.getData()[1]) << 8);
		case 0x6985:
			throw new CardException("not authentified");
		default:
			throw new CardException("generic error");
		}
	}
	
	public void incCounter() throws CardException {
		ResponseAPDU r;
		if (!myterm.isCardPresent())
			throw new CardNotPresentException("no card in reader");
		ensureSession();
		r = mychannel.transmit(INC_APDU);
		switch (r.getSW()) {
		case 0x9000:
			return;
		case 0x6985:
			throw new CardException("not authentified");
		default:
			throw new CardException("generic error");
		}
	}
	
	public void resetCounter() throws CardException {
		ResponseAPDU r;
		if (!myterm.isCardPresent())
			throw new CardNotPresentException("no card in reader");
		ensureSession();
		r = mychannel.transmit(RESET_APDU);
		switch (r.getSW()) {
		case 0x9000:
			return;
		case 0x6985:
			throw new CardException("not authentified");
		default:
			throw new CardException("generic error");
		}
	}

	public void authentify(byte[] PIN) throws CardException {
		ResponseAPDU r;
		if (!myterm.isCardPresent())
			throw new CardNotPresentException("no card in reader");
		ensureSession();
		r = mychannel.transmit(makeAuthAPDU (PIN));
		switch (r.getSW()) {
		case 0x9000:
			return;
		case 0x6300:
			throw new CardException("wrong PIN");
		default:
			throw new CardException("generic error");
		}
	}
	
	public void changePIN(byte[] PIN) throws CardException {
		ResponseAPDU r;
		if (!myterm.isCardPresent())
			throw new CardNotPresentException("no card in reader");
		ensureSession();
		r = mychannel.transmit(makeChangePINAPDU (PIN));
		switch (r.getSW()) {
		case 0x9000:
			return;
		default:
			throw new CardException("generic error");
		}
	}
	
	private void ensureSession() throws CardException {
		Card card;
		ResponseAPDU r;
		if (mychannel != null) {
			try {
				// send a dummy APDU to check the channel
				mychannel.transmit(new CommandAPDU(0xB0, 0xFF, 0x00, 0x00));
			} catch (CardException e) {
				mychannel = null;
			}
		}
		if (mychannel == null) {
			card = myterm.connect("T=1");
			mychannel = card.getBasicChannel();
			r = mychannel.transmit(SELECT_APDU);
			if (r.getSW() != 0x9000)
				throw new CardException("no applet present");
		}
	}
	
	private CommandAPDU makeAuthAPDU (byte[] PIN) {
		return new CommandAPDU(0xB0, 0x10, 0x00, 0x00, PIN);
	}
	
	private CommandAPDU makeChangePINAPDU (byte[] PIN) {
		return new CommandAPDU(0xB0, 0x12, 0x00, 0x00, PIN);
	}
	
	private int s2u(byte n) {
		return (n < 0) ? (256 + n) : n;
	}

}
