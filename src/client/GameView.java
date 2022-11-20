package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.Socket;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import gameMsg.*;

public class GameView extends JFrame implements KeyListener, Runnable {

	private static final long serialVersionUID = 1L;
	public JPanel contentPane;
	public JTextField txtInput;
	public String UserName;
	public JButton btnSend;
//	public Socket socket; // �������
//	public ObjectInputStream ois;
//	public ObjectOutputStream oos;
	public JLabel lbluserName;
	public JTextPane textArea;
	public RoomList roomList;
	public String roomName;
	
	public GameScreen gameScreen;
	Thread mainWork;
	
	private final static int UP_PRESSED	=0x001; // Ű ��
	private final static int DOWN_PRESSED	=0x002;
	private final static int LEFT_PRESSED	=0x004;
	private final static int RIGHT_PRESSED	=0x008;
	public int keybuff; // Ű ���۰�

	public boolean loop = true;
	public int delay; // ������ ���� ������ 1/1000 �� ����
	public long preTime; // loop ���� ������ ���� �ð� üũ
	
	public int gameStatus; // ���� ���� 0:����, 1: ������
	
	public int myScore, enemyScore; // ���� ���� ����
	public int comboCount; // �޺��ı��� �����Ͽ� ���ػѿ� ���� �� 0���� �ʱ�ȭ

	public int curX, curY; // ���� �������� �ѿ��� ��ġ
	public int subX, subY; // �������� �ι�° �ѿ��� ��ġ
	public int curP1, curP2; // ���� �������� �ѿ��� ����
	public int startX, startY; // ���� �ѿ� ������ ��ġ
	
	public int enemyCurX,enemyCurY;
	public int enemySubX,enemySubY;
	public int enemyCurP1,enemyCurP2;
	

	public int[][] myField = { // �� ���� �ʵ� 0: ������� 9: ä��������(��)
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
			{9, 9, 9, 9, 9, 9, 9, 9} // 0,13 ~ 7,13
		};
	
	public int[][] enemyField = { // ���� ���� �ʵ�, ���� X ������ �׸����
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
	
	public int curShape; // ���� �������� �ѿ��� ��� (4����) ������� �ð�������� ȸ��
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
	
	public int puyoType;
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
	
	/**
	 * ���� ȭ���� Ŭ����
	 */

	class GameScreen extends Canvas {
		
		public GameView main;
		public Graphics gc; // ������۸��� �׷��� ���ؽ�Ʈ
		public Image doubleBuffer; // ������۸��� �����
		public Image backGround = new ImageIcon("src/resource/backGround.png").getImage(); // ����̹���
		public Font font;
		public Image[] puyoTypeImg = { // 0�� puyoType�� ������� �̹Ƿ� ������� �ʴ´�
				null,
				new ImageIcon("src/resource/puyoRed.png").getImage(),
				new ImageIcon("src/resource/puyoYellow.png").getImage(),
				new ImageIcon("src/resource/puyoGreen.png").getImage(),
				new ImageIcon("src/resource/puyoBlue.png").getImage(),
				new ImageIcon("src/resource/puyoPurple.png").getImage()
		};
		
	
		public GameScreen(GameView gameView) {
			this.main = gameView;
			backGround = new ImageIcon("src/resource/backGround.png").getImage();
//			setSize(new Dimension(backGround.getWidth(null), backGround.getHeight(null)));
//			setPreferredSize(new Dimension(backGround.getWidth(null), backGround.getHeight(null)));
			setLayout(null);
		}
		
		
		public void drawField() { // field �� �ѿ並 �׸��� �Լ�
			for (int i = 0; i < 14; i++) {
				for (int j = 0; j < 8; j++) {
					if (myField[i][j] > 0 && myField[i][j] < 7) {
						gc.drawImage(puyoTypeImg[myField[i][j]], 32 * j, 32 * i, this);
					}
				}
			}
		}
		
		public void drawEnemyField() {
			for (int i = 0; i < 14; i++) {
				for (int j = 0; j < 8; j++) {
					if (enemyField[i][j] > 0 && enemyField[i][j] < 7) {
						gc.drawImage(puyoTypeImg[enemyField[i][j]], 32 * j, 32 * i, this);
					}
				}
			}
		}
		
		public void drawBackGround() {
		if(gc!=null)
			gc.drawImage(backGround, 0, 0, this);
		}
		
	
		public void drawMyPuyo() {
			gc.drawImage(puyoTypeImg[curP1], 32 * curX, 32 * curY, this);
			gc.drawImage(puyoTypeImg[curP2], 32 * subX, 32 * subY, this);
		}
		
		public void drawEnemyPuyo() {
		
			if(gc!=null&&enemyCurP1!=0&&enemyCurP2!=0&&enemyCurX!=0&&enemyCurY!=0&&enemySubX!=0&&enemySubY!=0) {
				gc.drawImage(puyoTypeImg[enemyCurP1], 32*12+32 * enemyCurX, 32 * enemyCurY, this);
				gc.drawImage(puyoTypeImg[enemyCurP2], 32*12+32 * enemySubX, 32 * enemySubY, this);
				System.out.println(enemyCurP1+" "+enemyCurP2+" "+enemyCurX+" "+enemyCurY+" "+enemySubX+" "+enemySubY);
			}
		}
		
		
		
		public void paint(Graphics g) {
			if (gc == null) {
				doubleBuffer = createImage(640, 480);
				if (doubleBuffer == null) System.out.println("������ũ�� ���� ���� ����");
				else gc = doubleBuffer.getGraphics();
				return;
			}
			update(g);
		}
		
		public void update(Graphics g) {
			if (gc == null) return;
			doublePaint();
			
			g.drawImage(doubleBuffer, 0, 0, this);
		}
		
		public void doublePaint() {
			switch (gameStatus) {
			case 0:
				drawBackGround();
				break;
			case 1:
				drawBackGround();
				drawField();
				drawEnemyField();
				drawMyPuyo();
				drawEnemyPuyo();
				roomList.SendMessage(curP1+" "+curP2+" "+curX+" "+curY+" "+subX+" "+subY, "501");
				break;
			}
		}
		
	/*	public void doubleEnemyPaint() {
			switch(gameStatus) {
			case 0:
				drawBackGround();
				break;
			case 1:
				drawBackGround();
				drawEnemyField();
				drawEnemyPuyo();
				break;
			}
		}*/
		
			
	} // GameScreen Ŭ���� ��
	/**
	 * Create the frame.
	 */
	public GameView(String userName, String ip_addr, String port_no, RoomList roomList, String roomName){
		super("Puyo Puyo2!!");
		this.roomList = roomList;
		this.roomName = roomName;
		this.UserName=userName;
		setBounds(100, 100, 980, 560);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
//
//		JScrollPane scrollPane = new JScrollPane();
//		scrollPane.setBounds(664, 19, 285, 381);
//		contentPane.add(scrollPane);
//
//		textArea = new JTextPane();
//		textArea.setEditable(true);
//		textArea.setFont(new Font("����", Font.PLAIN, 14));
//		textArea.setFocusable(false);
//		scrollPane.setViewportView(textArea);
//
//		txtInput = new JTextField();
//		txtInput.setBounds(664, 410, 194, 40);
//		contentPane.add(txtInput);
//		txtInput.setColumns(10);
//
//		btnSend = new JButton("Send");
//		btnSend.setFont(new Font("����", Font.PLAIN, 14));
//		btnSend.setBounds(880, 410, 69, 40);
//		contentPane.add(btnSend);
//
//		lbluserName = new JLabel("Name");
//		lbluserName.setBorder(new LineBorder(new Color(0, 0, 0)));
//		lbluserName.setBackground(Color.WHITE);
//		lbluserName.setFont(new Font("����", Font.BOLD, 14));
//		lbluserName.setHorizontalAlignment(SwingConstants.CENTER);
//		lbluserName.setBounds(664, 460, 62, 40);
//		contentPane.add(lbluserName);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setVisible(true);

//		AppendText("User " + userName + " connecting " + ip_addr + " " + port_no);
//		UserName = userName;
//		lbluserName.setText(userName);
		
		JButton btnNewButton = new JButton("�� ��");
		btnNewButton.setFont(new Font("����", Font.PLAIN, 14));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				roomList.SendMessage(roomName, "302");
				setVisible(false);
				gameStatus=0;
				roomList.setVisible(true);
			}
		});
		btnNewButton.setBounds(880, 459, 69, 40);
		contentPane.add(btnNewButton);

		gameScreen = new GameScreen(this);
		gameScreen.setFocusable(true);
		gameScreen.setBounds(10, 10, 640, 480);
		contentPane.add(gameScreen);
		addKeyListener(this);
//
//		TextSendAction action = new TextSendAction();
//		btnSend.addActionListener(action);
//		txtInput.addActionListener(action);

		initGame();
		gameScreen.requestFocus();
		gameScreen.repaint();
	}

	public void readMessage(GameMsg cm) {

			if(cm.data.equals(roomName)) {
				setVisible(false);
				roomList.setVisible(true);
			}else if(cm.code.matches("501")) {
				String enemyInformation[]=cm.data.split(" "); // p1 p2 curx cury subx suby ����
				 enemyCurP1=Integer.parseInt(enemyInformation[0]);
				 enemyCurP2=Integer.parseInt(enemyInformation[1]);
				 enemyCurX=Integer.parseInt(enemyInformation[2]);
				 enemyCurY=Integer.parseInt(enemyInformation[3]);
				 enemySubX=Integer.parseInt(enemyInformation[4]);
				 enemySubY=Integer.parseInt(enemyInformation[5]);
			}
	}

	// keyboard enter key ġ�� ������ ����
//	public class TextSendAction implements ActionListener {
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			// Send button�� �����ų� �޽��� �Է��ϰ� Enter key ġ��
//			if (e.getSource() == btnSend || e.getSource() == txtInput) {
//				String msg = null;
//				// msg = String.format("[%s] %s\n", userName, txtInput.getText());
//				msg = txtInput.getText();
//				SendMessage(msg);
//				txtInput.setText(""); // �޼����� ������ ���� �޼��� ����â�� ����.
//				txtInput.requestFocus(); // �޼����� ������ Ŀ���� �ٽ� �ؽ�Ʈ �ʵ�� ��ġ��Ų��
//			}
//		}
//	}

	public RoomList getRoomList() {
		return roomList;
	}

	public String getRoomName() {
		return roomName;
	}

	// ȭ�鿡 ���
//	public void AppendText(String msg) {
//		// textArea.append(msg + "\n");
//		// AppendIcon(icon1);
//		msg = msg.trim(); // �յ� blank�� \n�� �����Ѵ�.
//		int len = textArea.getDocument().getLength();
//		// ������ �̵�
//		textArea.setCaretPosition(len);
//		textArea.replaceSelection(msg + "\n");
//	}

//	public void myAppendText(String msg) {
//		msg = msg.trim(); // �յ� blank�� \n�� �����Ѵ�.
//		int len = textArea.getDocument().getLength();
//		// ������ �̵�
//		textArea.setCaretPosition(len);
//		textArea.replaceSelection(msg + "\n");
//		StyledDocument doc = textArea.getStyledDocument();
//		SimpleAttributeSet right = new SimpleAttributeSet();
//		StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
//		doc.setParagraphAttributes(len, doc.getLength(), right, false);
//		len = textArea.getDocument().getLength();
//		textArea.setCaretPosition(len);
//		doc = textArea.getStyledDocument();
//		right = new SimpleAttributeSet();
//		StyleConstants.setAlignment(right, StyleConstants.ALIGN_LEFT);
//		doc.setParagraphAttributes(len, doc.getLength(), right, false);
//	}

	// Server���� network���� ����
//	public void SendMessage(String msg) {
//		roomList.SendMessage(msg, "200");
//	}
	

	
	public void initGame() { // ���� ���۽� ���� �ʱ� ����
		
		myScore = 0;
		enemyScore = 0;
		startX = 4; // �������� �κ�
		startY = 1; // �������� �κ�
		comboCount = 0;
		delay = 17; // 17 / 1000 = 58������
		gameStatus = 1;
		dropPuyo();
		mainWork = new Thread(this);
		mainWork.start();
	}
	
	public void run() {
		try {
			while(loop) {
				preTime = System.currentTimeMillis();
				gameScreen.repaint();
				process();
				keyProcess();
				if (System.currentTimeMillis() - preTime < delay) // �ð� ������ ���ߴ� �۾�
					Thread.sleep(delay - System.currentTimeMillis() + preTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public void process() { // ���� ���� ó��
		switch(gameStatus) {
		case 0:
			break;
		case 1:
			if (checkDropDone()) dropPuyo();
			checkChainRule();
		}
	}
	

	public boolean checkDropDone() { // �������̴� �ѿ䰡 ������� Ȯ��(�ٴڿ� ��Ҵ���)
		if (myField[curY + 1][curX] != 0 || curY + 1 == subY) {
			if (myField[subY + 1][subX] != 0 || subY + 1 == curY) {
				myField[curY][curX] = curP1;
				myField[subY][subX] = curP2;
				
				return true;
			}
		}
			
		return false;
	}
	
	public void checkChainRule() { // �ѿ䰡 ������ �ı������� �����ϴ��� Ȯ�� �� �ı�
		// �ʵ� ��ü�� ��ĵ�ϸ� Ȯ���ؾ���, ���ŵǰ� ������ �ѿ䵵 Ȯ��
	}
	

	public void keyPressed(KeyEvent e) {
		
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

	
	
	
	public void checkGameOver() { // ���� ���� ó��(�ѿ䰡 õ���� ħ)
		
		//gameStatus = 0;
		//loop = false;
	}
	
	//private void createPuyo(Graphics g) { // ���� �ѿ� ����(���� ������)
	// ���߿� ���� �ϴ� �Ʒ��ɷ� �ٷ� ������ġ�� �����ϱ�
	//} 
	
	public void dropPuyo() { // ���� �ѿ� ���(�����¿��� ������ ����)
		curP1 = (int)(Math.random() * 5 + 1);
		curP2 = (int)(Math.random() * 5 + 1);
		curX = startX;
		curY = startY;
		subX = startX;
		subY = startY - 1;
		curShape = 0;
	}
	
	public void removePuyo(int x, int y) { // �ѿ� ����
		myField[x][y] = 0;
		comboCount++;
	}
	
	public void turnPuyo() {
		switch(curShape) {
		case 0:
			if (myField[curY][curX + 1] == 0) {
				curShape++;
				subX = curX + 1;
				subY = curY;
			}
			break;
		case 1:
			if (myField[curY + 1][curX] == 0) {
				curShape++;
				subX = curX;
				subY = curY + 1;
			}
			break;
		case 2:
			if (myField[curY][curX - 1] == 0) {
				curShape++;
				subX = curX - 1;
				subY = curY;
			}
			break;
		case 3:
			if (myField[curY - 1][curX] == 0) {
				curShape = 0;
				subX = curX;
				subY = curY - 1;
			}
			break;
		}
	}
	
	
	public void keyProcess() { // �ѿ� �̵�
		switch(gameStatus) {
		case 0:
			break;
		case 1:
			switch(keybuff) {
			case 0:
				break;
			case UP_PRESSED:
				turnPuyo();
				break;
			case DOWN_PRESSED:
				switch(curShape) {
				case 0:
					if (myField[curY + 1][curX] == 0) { curY++; subY++; }
					break;
				case 1:
					if (myField[curY + 1][curX] == 0) curY++;
					if (myField[subY + 1][subX] == 0) subY++;
					break;
				case 2:
					if (myField[curY + 2][curX] == 0) { curY++; subY++; }
					break;
				case 3:
					if (myField[curY + 1][curX] == 0) curY++;
					if (myField[subY + 1][subX] == 0) subY++;
					break;
				}
				break;
			case LEFT_PRESSED:
				switch(curShape) {
				case 0:
					if (myField[curY][curX - 1] == 0 && myField[curY - 1][curX - 1] == 0) { curX--; subX--; }
					break;
				case 1:
					if (myField[curY][curX - 1] == 0) { curX--; subX--; }
					break;
				case 2:
					if (myField[curY][curX - 1] == 0 && myField[curY + 1][curX - 1] == 0) { curX--; subX--; }
					break;
				case 3:
					if (myField[curY][curX - 2] == 0) { curX--; subX--; }
					break;
				}
				break;
			case RIGHT_PRESSED:
				switch(curShape) {
				case 0:
					if (myField[curY][curX + 1] == 0 && myField[curY - 1][curX + 1] == 0) { curX++; subX++; }
					break;
				case 1:
					if (myField[curY][curX + 2] == 0) { curX++; subX++; }
					break; 
				case 2:
					if (myField[curY][curX + 1] == 0 && myField[curY + 1][curX + 1] == 0) { curX++; subX++; }
					break;
				case 3:
					if (myField[curY][curX + 1] == 0) { curX++; subX++; }
					break;
				}
				break;
			}
			try { // Ű�� �ʹ� ������ ������ �ȵ� ������
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
} // ��ü Ŭ���� ��


