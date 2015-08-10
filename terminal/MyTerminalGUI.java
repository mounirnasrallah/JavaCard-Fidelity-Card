package tp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.smartcardio.CardException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class MyTerminalGUI extends JFrame implements ActionListener, KeyEventDispatcher {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3755751334481076643L;
	private JTextField lilWindow;
	private JTextField counterVal;
	private JTextField status;
	private NumKeyboard kb;
	private JButton get;
	private JButton inc;
	private JButton reset;
	private JButton changePIN;
	
	private int state;
	private String savePIN;
	
	private MyTerminal terminal;
	
	public static void main(String[] args) {
		new MyTerminalGUI(); 
	}

	public MyTerminalGUI() {
		super("Secure Counter Terminal");
		
		state = 0;
		
		lilWindow = new JPasswordField();
		lilWindow.setEditable(false);
		//lilWindow.setPreferredSize(new Dimension(56, 30));
		lilWindow.setBackground(Color.WHITE);
		lilWindow.setFont(lilWindow.getFont().deriveFont(22.0f));
		lilWindow.setHorizontalAlignment(JTextField.CENTER);

		counterVal = new JTextField();
		counterVal.setText("??");
		counterVal.setEditable(false);
		counterVal.setPreferredSize(new Dimension(160,80));
		counterVal.setMaximumSize(new Dimension(160,80));
		counterVal.setFont(counterVal.getFont().deriveFont(40.0f));
		counterVal.setHorizontalAlignment(JTextField.CENTER);
		
		status = new JTextField();
		status.setEditable(false);
		status.setPreferredSize(new Dimension(300,20));
		status.setMargin(new Insets(0, 4, 0, 0));

		kb = new NumKeyboard();
		kb.attach(this);
		
		get= new JButton("Get");
		get.setActionCommand("get");
		get.addActionListener(this);
		inc = new JButton("Inc");
		inc.setActionCommand("inc");
		inc.addActionListener(this);
		reset = new JButton("Reset");
		reset.setActionCommand("reset");
		reset.addActionListener(this);
		changePIN = new JButton("Change PIN");
		changePIN.setActionCommand("changePIN");
		changePIN.addActionListener(this);

		try {
			terminal = new MyTerminal();
			counterVal.setText(""+terminal.getCounter());
		} catch (CardException e) {
			status.setText("no reader available");
		}

		JPanel mainPanel = new JPanel();
		JPanel numPadPanel = new JPanel();
		JPanel controlPanel = new JPanel();
		JPanel counterPanel = new JPanel();

		numPadPanel.setLayout(new BoxLayout(numPadPanel,BoxLayout.PAGE_AXIS));
		numPadPanel.add(lilWindow);
		numPadPanel.add(Box.createVerticalStrut(5));
		numPadPanel.add (kb);

		controlPanel.add(get);
		controlPanel.add(inc);
		controlPanel.add(reset);
		controlPanel.add(changePIN);

		counterPanel.setLayout(new BoxLayout(counterPanel,BoxLayout.PAGE_AXIS));
		counterPanel.add(counterVal);
		counterPanel.add(Box.createVerticalStrut(20));
		counterPanel.add(controlPanel);
		counterPanel.add(Box.createVerticalStrut(100));
		
		mainPanel.add(counterPanel);
		mainPanel.add(Box.createHorizontalStrut(60));
		mainPanel.add(numPadPanel);
		
		setLayout(new BorderLayout());
		add(Box.createVerticalStrut(60), BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(status, BorderLayout.SOUTH);

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(640, 480);
		
		setVisible(true);
	}
	

	private void doAction(String s) {
		try {
			if (s.equals("C")) {
				lilWindow.setText("");
			} else if (s.equals("V")) {
				if (state == 0) {
					terminal.authentify(stringToPIN(lilWindow.getText()));
					status.setText("authentified !");
					counterVal.setText(""+terminal.getCounter());
				} else if (state == 1) {
					terminal.authentify(stringToPIN(lilWindow.getText()));
					status.setText("Type your new PIN");
					state = 2;
				} else if (state == 2) {
					savePIN = lilWindow.getText();
					status.setText("Confirm your new PIN");
					state = 3;
				} else if (state == 3) {
					if (!savePIN.equals(lilWindow.getText())) {
						status.setText("Mismatch. Type your new PIN");
						state = 2;
					} else {
						savePIN = null;
						terminal.changePIN(stringToPIN(lilWindow.getText()));
						status.setText("PIN changed");
						resetState();
						counterVal.setText(""+terminal.getCounter());
					}
				}
				lilWindow.setText("");
			} else if (s.equals("get")) {
				counterVal.setText(""+terminal.getCounter());
			} else if (s.equals("inc")) {
				terminal.incCounter();
				counterVal.setText(""+terminal.getCounter());
			} else if (s.equals("reset")) {
				terminal.resetCounter();
				counterVal.setText(""+terminal.getCounter());
			} else if (s.equals("changePIN")) {
				get.setEnabled(false);
				inc.setEnabled(false);
				reset.setEnabled(false);
				changePIN.setEnabled(false);
				status.setText("Enter your old PIN");
				state = 1;
			} else {
				lilWindow.setText(lilWindow.getText() + s);
			}
		} catch (CardException e) {
			status.setText(e.getMessage());
			resetState ();
		}
	}
	
	private void resetState() {
		state = 0;
		get.setEnabled(true);
		inc.setEnabled(true);
		reset.setEnabled(true);
		changePIN.setEnabled(true);
		lilWindow.setText("");
	}

	private byte[] stringToPIN(String s) {
		byte [] PIN = new byte[s.length()];
		for (int i = 0; i < PIN.length; i++) {
			PIN[i] = (byte) (s.charAt(i) - '0');
		}
		return PIN;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		doAction(e.getActionCommand());
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			if (e.getKeyCode() == 10) {
				doAction("V");
			} else if (e.getKeyCode() == 27) {
				doAction("C");
			} else if (e.getKeyChar() == '+') {
				doAction("inc");
			} else if (e.getKeyChar() == '=') {
				doAction("reset");
			} else if ('0' <= e.getKeyChar() && e.getKeyChar() <= '9') {
				doAction(String.valueOf(e.getKeyChar()));
			}
		}
		return false;
	}
	
}
