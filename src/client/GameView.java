package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import gameMsg.*;

public class GameView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtInput;
	private String UserName;
	private JButton btnSend;
	private Socket socket; // �������
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private JLabel lbluserName;
	private JTextPane textArea;
	private RoomList roomList;
	private String roomName;
	

	/**
	 * Create the frame.
	 */
	public GameView(String userName, String ip_addr, String port_no, RoomList roomList, String roomName) {
		super("Puyo Puyo2!!");
		this.roomList = roomList;
		this.roomName = roomName;
		setBounds(100, 100, 980, 560);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(664, 19, 285, 381);
		contentPane.add(scrollPane);

		textArea = new JTextPane();
		textArea.setEditable(true);
		textArea.setFont(new Font("����", Font.PLAIN, 14));
		textArea.setFocusable(false);
		scrollPane.setViewportView(textArea);

		txtInput = new JTextField();
		txtInput.setBounds(664, 410, 194, 40);
		contentPane.add(txtInput);
		txtInput.setColumns(10);

		btnSend = new JButton("Send");
		btnSend.setFont(new Font("����", Font.PLAIN, 14));
		btnSend.setBounds(880, 410, 69, 40);
		contentPane.add(btnSend);

		lbluserName = new JLabel("Name");
		lbluserName.setBorder(new LineBorder(new Color(0, 0, 0)));
		lbluserName.setBackground(Color.WHITE);
		lbluserName.setFont(new Font("����", Font.BOLD, 14));
		lbluserName.setHorizontalAlignment(SwingConstants.CENTER);
		lbluserName.setBounds(664, 460, 62, 40);
		contentPane.add(lbluserName);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setVisible(true);

		AppendText("User " + userName + " connecting " + ip_addr + " " + port_no);
		UserName = userName;
		lbluserName.setText(userName);

		JButton btnNewButton = new JButton("�� ��");
		btnNewButton.setFont(new Font("����", Font.PLAIN, 14));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				roomList.SendMessage(roomName, "302");
				setVisible(false);
				roomList.setVisible(true);
			}
		});
		btnNewButton.setBounds(880, 459, 69, 40);
		contentPane.add(btnNewButton);

		JPanel gamePane = new GamePane(new ImageIcon("src/resource/backGround.png").getImage());
		gamePane.setBounds(10, 10, 640, 480);
		contentPane.add(gamePane);

		TextSendAction action = new TextSendAction();
		btnSend.addActionListener(action);
		txtInput.addActionListener(action);
		txtInput.requestFocus();

		repaint();
	}

	public void readMessage(GameMsg cm) {
		if (cm.code.matches("200")) {
			if (cm.userName.equals(UserName)) {
				myAppendText(cm.data);
			} else {
				AppendText(String.format("[%s] %s", cm.userName, cm.data));
			}
		}else if(cm.code.matches("305")) {
			if(cm.data.equals(roomName)) {
				setVisible(false);
				roomList.setVisible(true);
			}
		}
	}

	// keyboard enter key ġ�� ������ ����
	class TextSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button�� �����ų� �޽��� �Է��ϰ� Enter key ġ��
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String msg = null;
				// msg = String.format("[%s] %s\n", userName, txtInput.getText());
				msg = txtInput.getText();
				SendMessage(msg);
				txtInput.setText(""); // �޼����� ������ ���� �޼��� ����â�� ����.
				txtInput.requestFocus(); // �޼����� ������ Ŀ���� �ٽ� �ؽ�Ʈ �ʵ�� ��ġ��Ų��
			}
		}
	}

	public RoomList getRoomList() {
		return roomList;
	}

	public String getRoomName() {
		return roomName;
	}

	// ȭ�鿡 ���
	public void AppendText(String msg) {
		// textArea.append(msg + "\n");
		// AppendIcon(icon1);
		msg = msg.trim(); // �յ� blank�� \n�� �����Ѵ�.
		int len = textArea.getDocument().getLength();
		// ������ �̵�
		textArea.setCaretPosition(len);
		textArea.replaceSelection(msg + "\n");
	}

	public void myAppendText(String msg) {
		msg = msg.trim(); // �յ� blank�� \n�� �����Ѵ�.
		int len = textArea.getDocument().getLength();
		// ������ �̵�
		textArea.setCaretPosition(len);
		textArea.replaceSelection(msg + "\n");
		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet right = new SimpleAttributeSet();
		StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
		doc.setParagraphAttributes(len, doc.getLength(), right, false);
		len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
		doc = textArea.getStyledDocument();
		right = new SimpleAttributeSet();
		StyleConstants.setAlignment(right, StyleConstants.ALIGN_LEFT);
		doc.setParagraphAttributes(len, doc.getLength(), right, false);
	}

	// Server���� network���� ����
	public void SendMessage(String msg) {
		roomList.SendMessage(msg, "200");
	}
	
	
	
	
	
	/**
	 * ���Ӱ� ���õ� ���� ó���ϴ� Ŭ����
	 */

	class GamePane extends JPanel implements KeyListener {
		
		public final static int UP_PRESSED		=0x001; // Ű ��
		public final static int DOWN_PRESSED	=0x002;
		public final static int LEFT_PRESSED	=0x004;
		public final static int RIGHT_PRESSED	=0x008;
		
		protected Image backGround; // ����̹���
		public int gameStatus; // ���� ���� 0:����, 1: ������
		private int keybuff; // Ű ���۰�
		private int random = (int)(Math.random()*5); // ��� ������ ����
		private int myScore, enemyScore; // ���� ���� ����
		
		private int[][] myField = { // �� ���� �ʵ� 0: ������� 1: ä��������(��)
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 1, 1, 1, 1, 1, 1, 1}
			};
		
		private int[][] enemyField = { // ���� ���� �ʵ�
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 1},
				{1, 1, 1, 1, 1, 1, 1, 1}
			};
		
		Image curPuyo;
		Image nextPuyo;
		
	
		public GamePane(Image backGround) {
			this.backGround = backGround;
			setSize(new Dimension(backGround.getWidth(null), backGround.getHeight(null)));
			setPreferredSize(new Dimension(backGround.getWidth(null), backGround.getHeight(null)));
			setLayout(null);
			
			initGame();
		}
		
		private void initGame() { // ���� ���۽� ���� �ʱ� ����
			myScore = 0;
			enemyScore = 0;
			gameStatus = 0;
		}
		
		public void checkGameOver() { // ���� ���� ó��(�ѿ䰡 õ���� ħ)
			
			gameStatus = 0;
		}
		
		public void createPuyo(Graphics g) { // ���� �ѿ� ����(���� ������)
			
		}
		
		public void dropPuyo(Graphics g) { // ���� �ѿ� ���(�����¿��� ������ ����)
			
		}
		
		public void removePuyo(Graphics g) { // �ѿ� ����
			
		}
		
		public void movePuyo(Graphics g) { // �ѿ� �̵�
			
		}
		
		public void turnPuyo(Graphics g) { // ���� �ѿ� ȸ��
			
		}
		
		public void checkDropDone(Graphics g) { // �������̴� �ѿ䰡 ������� Ȯ��(�ٴڿ� ��Ҵ���)
			
		}
		
		/*
		public void checkLeftWall() { // �ѿ䰡 ���� ���� ������ ���̻� �������� ����
			
		}
		
		public void checkRightWall() { // �ѿ䰡 ������ ���� ������ ���̻� ���������� ����
			
		}
		*/
		
		
		
		
	
		public void paintComponent(Graphics g) {
			g.drawImage(backGround, 0, 0, null);
		}
		
		public void keyPressed(KeyEvent e) {
		
			if (gameStatus == 1) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					keybuff|=LEFT_PRESSED;
					break;
				case KeyEvent.VK_UP:
					keybuff|=UP_PRESSED;
					break;
				case KeyEvent.VK_RIGHT:
					keybuff|=RIGHT_PRESSED;
					break;
				case KeyEvent.VK_DOWN:
					keybuff|=DOWN_PRESSED;
					break;
				default:
					break;
				}
			} else if (gameStatus != 1) keybuff = e.getKeyCode();
		}
		
		public void keyReleased(KeyEvent e) {
			switch(e.getKeyCode()){
			case KeyEvent.VK_UP:
				keybuff&=~UP_PRESSED;
				break;
			case KeyEvent.VK_DOWN:
				keybuff&=~DOWN_PRESSED;
				break;
			case KeyEvent.VK_LEFT:
				keybuff&=~LEFT_PRESSED;
				break;
			case KeyEvent.VK_RIGHT:
				keybuff&=~RIGHT_PRESSED;
				break;
			}
		}
		
		public void keyTyped(KeyEvent e) {}
		
		public void keyProcess() {
			switch(gameStatus) {
			case 0:
				break;
			case 1:
				switch(keybuff) {
				case 0:
					break;
				case UP_PRESSED:
					break;
				case DOWN_PRESSED:
					break;
				case LEFT_PRESSED:
					break;
				case RIGHT_PRESSED:
					break;
				}
			}
		}
		
		
	} // GamePane Ŭ���� ��
	
	
} // ��ü Ŭ���� ��


