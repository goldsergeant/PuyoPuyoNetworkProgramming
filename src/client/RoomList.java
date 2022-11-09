package client;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class RoomList extends JFrame{
	private JTextPane textArea;
	
	public RoomList() {
		super("참가방 목록");
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
		setContentPane(panel);
		panel.setLayout(null);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(41, 481, 320, -427);
		panel.add(scrollPane);
		
		JLabel lblNewLabel = new JLabel("게임 방",SwingConstants.CENTER);
		lblNewLabel.setBounds(41, 10, 157, 25);
		lblNewLabel.setFont(new Font("휴먼둥근헤드라인",Font.BOLD,20));
		lblNewLabel.setForeground(Color.red);
		lblNewLabel.setBackground(Color.green);
		panel.add(lblNewLabel);
		lblNewLabel.setOpaque(true);
		Border border = BorderFactory.createLineBorder(Color.green, 1);
		 
        // set the border of this component
        lblNewLabel.setBorder(border);
        
        JLabel lblNewLabel_1 = new JLabel("방장",SwingConstants.CENTER);
        lblNewLabel_1.setBounds(256, 10, 105, 25);
        lblNewLabel_1.setFont(new Font("휴먼둥근헤드라인",Font.BOLD,20));
		lblNewLabel_1.setForeground(Color.red);
		lblNewLabel_1.setBackground(Color.green);
		lblNewLabel_1.setOpaque(true);
		border = BorderFactory.createLineBorder(Color.green, 1);
	
		lblNewLabel_1.setBorder(border);
        panel.add(lblNewLabel_1);
        
        JButton btnNewButton = new JButton("입장");
        btnNewButton.setBounds(143, 481, 97, 43);
        panel.add(btnNewButton);
        btnNewButton.setFont(new Font("휴먼둥근헤드라인",Font.BOLD,20));
        btnNewButton.setForeground(Color.red);
        btnNewButton.setOpaque(true);
        btnNewButton.setBackground(Color.green);
        
        textArea = new JTextPane();
        scrollPane.setViewportView(textArea);
        textArea.setBounds(41, 45, 301, 426);
        textArea.setEditable(true);
      
	}
}
