package tp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class NumKeyboard extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 923692177506019520L;
	private static final int BSIZE = 65;
	
	public NumKeyboard() {
		setLayout(new GridLayout(4, 3));
		for (int i = 1; i <= 9; i++) {
			add (makeButton(""+i));
		}
		add (makeButton("C"));
		add (makeButton("0"));
		add (makeButton("V"));
		getButton('C').setBackground(Color.RED);
		getButton('V').setBackground(Color.GREEN);
		Dimension sz = new Dimension(3*BSIZE, 4*BSIZE);
		setMinimumSize(sz);
		setPreferredSize(sz);
		setMaximumSize(sz);
	}
	
	public void attach(ActionListener l) {
		for (int i = 0; i < 10; i++)
			getButton(i).addActionListener(l);
		getButton('C').addActionListener(l);
		getButton('V').addActionListener(l);
	}
	
	private JButton makeButton(String s) {
		JButton b = new JButton(s);
		b.setFont(b.getFont().deriveFont(18.0f));
		b.setActionCommand(s);
		b.setBackground(Color.LIGHT_GRAY);
		return b;
	}
	
	private JButton getButton(int i) {
		if (i == 0)
			return (JButton) getComponent(10);
		else if (i > 0 && i < 10)
			return (JButton) getComponent(i-1);
		else if (i == 'C')
			return (JButton) getComponent(9);
		else if (i == 'V')
			return (JButton) getComponent(11);
		return null;
	}

}
