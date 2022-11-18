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

		gamePane.requestFocus();
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

	class GamePane extends JPanel implements KeyListener, Runnable {
		
		private final static int UP_PRESSED	=0x001; // Ű ��
		private final static int DOWN_PRESSED	=0x002;
		private final static int LEFT_PRESSED	=0x004;
		private final static int RIGHT_PRESSED	=0x008;
		
		Thread mainWork;
		boolean loop = true;
		private int delay; // ������ ���� ������ 1/1000 �� ����
		private long preTime; // loop ���� ������ ���� �ð� üũ
		
		private Image backGround; // ����̹���
		private int gameStatus; // ���� ���� 0:����, 1: ������
		private int keybuff; // Ű ���۰�
		private int random = (int)(Math.random() * 5 + 1); // ��� ������ ���� ����
		private int myScore, enemyScore; // ���� ���� ����
		private int curX, curY; // ���� �������� �ѿ��� ��ġ
		private int startX, startY; // ���� �ѿ� ������ ��ġ
		private int comboCount; // �޺��ı��� �����Ͽ� ���ػѿ� ���� �� 0���� �ʱ�ȭ
		
		private int[][] myField = { // �� ���� �ʵ� 0: ������� 9: ä��������(��)
				{9, 0, 0, 0, 0, 0, 0, 9}, // 0,0 ~ 7,0
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 9, 9, 9, 9, 9, 9, 9} // 0,14 ~ 7,14
			};
		
		private int[][] enemyField = { // ���� ���� �ʵ�, ���� X ������ �׸����
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 0, 0, 0, 0, 0, 0, 9},
				{9, 9, 9, 9, 9, 9, 9, 9}
			};
		
		private int curShape; // ���� �������� �ѿ��� ��� (4����) ������� �ð�������� ȸ��
		/**
		 * 0: . A .           B�� ��Ʈ���� ������ �Ǵ� �ѿ���
		 *    . B .
		 *    . . .
		 *    
		 * 1: . . .
		 *    . B A
		 *    . . .
		 *    
		 * 2: . . .
		 *    . B .
		 *    . A .
		 *    
		 * 3: . . .
		 *    A B .
		 *    . . .
		 */
		
		private int puyoType;
		/**
		 * �ѿ��� ����
		 * 0: �������
		 * 1: �����ѿ�
		 * 2: ����ѿ�
		 * 3: �ʷϻѿ�
		 * 4: �Ķ��ѿ�
		 * 5: ����ѿ�
		 * 6: ���ػѿ�(�޺��׾� ���ݽ� ����)
		 */
		
		Image puyoA, puyoB;
		Image[] puyoTypeImg = { // 0�� puyoType�� ������� �̹Ƿ� ������� �ʴ´�
				null,
				new ImageIcon("src/resource/puyoRed.png").getImage(),
				new ImageIcon("src/resource/puyoYellow.png").getImage(),
				new ImageIcon("src/resource/puyoGreen.png").getImage(),
				new ImageIcon("src/resource/puyoBlue.png").getImage(),
				new ImageIcon("src/resource/puyoPurple.png").getImage()
		};
		
	
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
			startX = 4; // �������� �κ�
			startY = 1; // �������� �κ�
			comboCount = 0;
			delay = 17; // 17 / 1000 = 58������
		}
		
		public void run() {
			try {
				while(loop) {
					preTime = System.currentTimeMillis();
					
					this.repaint();
					//process(); ���� ���� ó��
					//keyprocess(); Ű �Է� ó��
					if (System.currentTimeMillis() - preTime < delay)
						Thread.sleep(delay - System.currentTimeMillis() + preTime);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
		private void checkGameOver() { // ���� ���� ó��(�ѿ䰡 õ���� ħ)
			
			gameStatus = 0;
		}
		
		//private void createPuyo(Graphics g) { // ���� �ѿ� ����(���� ������)
		// ���߿� ���� �ϴ� �Ʒ��ɷ� �ٷ� ������ġ�� �����ϱ�
		//} 
		
		private void dropPuyo(Graphics g) { // ���� �ѿ� ���(�����¿��� ������ ����)
			myField[startX][startY] = random;
			myField[startX][startY + 1] = random;
			curX = startX;
			curY = startY;
		}
		
		private void removePuyo(int x, int y) { // �ѿ� ����
			myField[x][y] = 0;
			comboCount++;
		}
		
		private void movePuyo(Graphics g) { // �ѿ� �̵�
			switch(gameStatus) {
			case 0:
				break;
			case 1:
				switch(keybuff) {
				case 0:
					break;
				case UP_PRESSED:
					curShape = (curShape + 1) % 4;
					break;
				case DOWN_PRESSED:
					switch(curShape) {
					case 0:
						if (myField[curX][curY + 1] == 0) curY++;
						break;
					case 1:
						if (myField[curX][curY + 1] == 0 && myField[curX + 1][curY + 1] == 0) curY++;
						break;
					case 2:
						if (myField[curX][curY + 2] == 0) curY++;
						break;
					case 3:
						if (myField[curX][curY + 1] == 0 && myField[curX - 1][curY + 1] == 0) curY++;
						break;
					}
					break;
				case LEFT_PRESSED:
					switch(curShape) {
					case 0:
						if (myField[curX - 1][curY] == 0 && myField[curX - 1][curY - 1] == 0) curX--;
						break;
					case 1:
						if (myField[curX - 1][curY] == 0) curX--;
						break;
					case 2:
						if (myField[curX - 1][curY] == 0 && myField[curX - 1][curY + 1] == 0) curX--;
						break;
					case 3:
						if (myField[curX - 2][curY] == 0) curX--;
						break;
					}
					break;
				case RIGHT_PRESSED:
					switch(curShape) {
					case 0:
						if (myField[curX + 1][curY] == 0 && myField[curX + 1][curY - 1] == 0) curX++;
						break;
					case 1:
						if (myField[curX + 2][curY] == 0) curX++;
						break;
					case 2:
						if (myField[curX + 1][curY] == 0 && myField[curX + 1][curY + 1] == 0) curX++;
						break;
					case 3:
						if (myField[curX + 1][curY] == 0) curX++;
						break;
					}
					break;
				}
			}
		}
		
		private void checkDropDone(Graphics g) { // �������̴� �ѿ䰡 ������� Ȯ��(�ٴڿ� ��Ҵ���)
			
		}
		
		private void checkChainRule() { // �ѿ䰡 ������ �ı������� �����ϴ��� Ȯ�� �� �ı�
			
		}
		
		
		
		private void drawField(int[][] field) { // field �� �ѿ並 �׸��� �Լ�
			for (int i = 0; i < 15; i++) {
				for (int j = 0; j < 8; j++) {
					if (field[i][j] > 0 && field[i][j] < 7) {
						// ȭ���� ��ǥ���
					}
				}
			}
		}
	
		public void paintComponent(Graphics g) {
			g.drawImage(backGround, 0, 0, null);
		}
		
		public void keyPressed(KeyEvent e) {
		
			if (gameStatus == 1) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_UP:
					keybuff|=UP_PRESSED;
					break;
				case KeyEvent.VK_DOWN:
					keybuff|=DOWN_PRESSED;
					break;
				case KeyEvent.VK_LEFT:
					keybuff|=LEFT_PRESSED;
					break;
				case KeyEvent.VK_RIGHT:
					keybuff|=RIGHT_PRESSED;
					break;
				default:
					break;
				}
			} else {
				keybuff = e.getKeyCode();
			}
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
			default:
				break;
			}
		}
		
		public void keyTyped(KeyEvent e) {}
		
		
	} // GamePane Ŭ���� ��
	
	
} // ��ü Ŭ���� ��


