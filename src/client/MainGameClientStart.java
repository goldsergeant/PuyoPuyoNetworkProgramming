package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;

public class MainGameClientStart extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtUserName;
	private JTextField txtIpAddress;
	private JTextField txtPortNumber;
	private JLabel titleLabel;
	private ImageIcon title;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainGameClientStart frame = new MainGameClientStart();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainGameClientStart() {
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 760, 960);
		setTitle("PuyoPuyo2!!");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		getContentPane().setBackground(new java.awt.Color(85, 85, 170));
		ImageIcon startIcon=new ImageIcon("./src/resource/startButton.png");
		JButton btnStart = new JButton(startIcon);
		btnStart.setBounds(524, 170, 200, 280);
		btnStart.setBackground(getForeground());
		btnStart.setBorderPainted(false);
		btnStart.setFocusPainted(false);
		btnStart.setContentAreaFilled(false);
	
		contentPane.add(btnStart);
		
		JLabel lblNewLabel = new JLabel("아이디");
		lblNewLabel.setBounds(606, 62, 82, 33);
		contentPane.add(lblNewLabel);
		
		txtUserName = new JTextField();
		txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
		txtUserName.setBounds(565, 105, 116, 33);
		contentPane.add(txtUserName);
		txtUserName.setColumns(10);
		
		JLabel lblIpAddress = new JLabel("IP Address");
		lblIpAddress.setBounds(590, 562, 82, 33);
		contentPane.add(lblIpAddress);
		
		txtIpAddress = new JTextField();
		txtIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
		txtIpAddress.setText("127.0.0.1");
		txtIpAddress.setColumns(10);
		txtIpAddress.setBounds(565, 605, 116, 33);
		contentPane.add(txtIpAddress);
		
		JLabel lblPortNumber = new JLabel("Port Number");
		lblPortNumber.setBounds(590, 683, 82, 33);
		contentPane.add(lblPortNumber);
		
		txtPortNumber = new JTextField();
		txtPortNumber.setText("30000");
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setColumns(10);
		txtPortNumber.setBounds(565, 735, 116, 33);
		contentPane.add(txtPortNumber);
		
		title = new ImageIcon("./src/resource/title.png");
		titleLabel = new JLabel("Title", title, JLabel.CENTER);
		titleLabel.setBounds(12, 10, 500, 902);
		contentPane.add(titleLabel);

		Myaction action = new Myaction();
		txtUserName.addActionListener(action);
		txtIpAddress.addActionListener(action);
		txtPortNumber.addActionListener(action);
		btnStart.addActionListener(action);
		btnStart.addMouseListener(new ButtonAction());
	}
	
	class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			String username = txtUserName.getText().trim();
			String ip_addr = txtIpAddress.getText().trim();
			String port_no = txtPortNumber.getText().trim();
			new RoomList(username, ip_addr, port_no);
			setVisible(false);
		}
	}`
	
	class ButtonAction implements MouseListener{

		@Override
		public void mouseClicked(MouseEvent e) {
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			JButton button=(JButton) e.getSource();
			button.setSize(button.getWidth()+10, button.getHeight()+10);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			JButton button=(JButton) e.getSource();
			button.setSize(button.getWidth()-10, button.getHeight()-10);
		}
		
	}
}


