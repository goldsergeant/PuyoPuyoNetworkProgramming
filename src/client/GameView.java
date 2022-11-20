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
//	public Socket socket; // 연결소켓
//	public ObjectInputStream ois;
//	public ObjectOutputStream oos;
	public JLabel lbluserName;
	public JTextPane textArea;
	public RoomList roomList;
	public String roomName;
	
	public GameScreen gameScreen;
	Thread mainWork;
	
	private final static int UP_PRESSED	=0x001; // 키 값
	private final static int DOWN_PRESSED	=0x002;
	private final static int LEFT_PRESSED	=0x004;
	private final static int RIGHT_PRESSED	=0x008;
	public int keybuff; // 키 버퍼값

	public boolean loop = true;
	public int delay; // 프레임 조절 딜레이 1/1000 초 단위
	public long preTime; // loop 간격 조절을 위한 시간 체크
	
	public int gameStatus; // 게임 상태 0:중지, 1: 실행중
	
	public int myScore, enemyScore; // 나와 상대방 점수
	public int comboCount; // 콤보파괴시 증가하여 방해뿌요 생성 후 0으로 초기화

	public int curX, curY; // 현재 조작중인 뿌요의 위치
	public int subX, subY; // 조작중인 두번째 뿌요의 위치
	public int curP1, curP2; // 현재 조작중인 뿌요의 종류
	public int startX, startY; // 새로 뿌요 생성시 위치
	
	public int enemyCurX,enemyCurY;
	public int enemySubX,enemySubY;
	public int enemyCurP1,enemyCurP2;
	

	public int[][] myField = { // 내 게임 필드 0: 비어있음 9: 채워져있음(벽)
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
	
	public int[][] enemyField = { // 상대방 게임 필드, 제어 X 오로지 그리기용
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
	
	public int curShape; // 현재 조작중인 뿌요의 모양 (4가지) 순서대로 시계방향으로 회전
	/**
	 * 0: . A .           B가 컨트롤의 메인이 되는 뿌요임
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
	 * 뿌요의 종류
	 * 0: 비어있음
	 * 1: 빨간뿌요
	 * 2: 노란뿌요
	 * 3: 초록뿌요
	 * 4: 파랑뿌요
	 * 5: 보라뿌요
	 * 6: 방해뿌요(콤보쌓아 공격시 생성)
	 */
	
	/**
	 * 게임 화면을 클래스
	 */

	class GameScreen extends Canvas {
		
		public GameView main;
		public Graphics gc; // 더블버퍼링용 그래픽 컨텍스트
		public Image doubleBuffer; // 더블버퍼링용 백버퍼
		public Image backGround = new ImageIcon("src/resource/backGround.png").getImage(); // 배경이미지
		public Font font;
		public Image[] puyoTypeImg = { // 0은 puyoType도 비어있음 이므로 사용하지 않는다
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
		
		
		public void drawField() { // field 의 뿌요를 그리는 함수
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
				if (doubleBuffer == null) System.out.println("오프스크린 버퍼 생성 실패");
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
		
			
	} // GameScreen 클래스 끝
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
//		textArea.setFont(new Font("굴림", Font.PLAIN, 14));
//		textArea.setFocusable(false);
//		scrollPane.setViewportView(textArea);
//
//		txtInput = new JTextField();
//		txtInput.setBounds(664, 410, 194, 40);
//		contentPane.add(txtInput);
//		txtInput.setColumns(10);
//
//		btnSend = new JButton("Send");
//		btnSend.setFont(new Font("굴림", Font.PLAIN, 14));
//		btnSend.setBounds(880, 410, 69, 40);
//		contentPane.add(btnSend);
//
//		lbluserName = new JLabel("Name");
//		lbluserName.setBorder(new LineBorder(new Color(0, 0, 0)));
//		lbluserName.setBackground(Color.WHITE);
//		lbluserName.setFont(new Font("굴림", Font.BOLD, 14));
//		lbluserName.setHorizontalAlignment(SwingConstants.CENTER);
//		lbluserName.setBounds(664, 460, 62, 40);
//		contentPane.add(lbluserName);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setVisible(true);

//		AppendText("User " + userName + " connecting " + ip_addr + " " + port_no);
//		UserName = userName;
//		lbluserName.setText(userName);
		
		JButton btnNewButton = new JButton("종 료");
		btnNewButton.setFont(new Font("굴림", Font.PLAIN, 14));
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
				String enemyInformation[]=cm.data.split(" "); // p1 p2 curx cury subx suby 순서
				 enemyCurP1=Integer.parseInt(enemyInformation[0]);
				 enemyCurP2=Integer.parseInt(enemyInformation[1]);
				 enemyCurX=Integer.parseInt(enemyInformation[2]);
				 enemyCurY=Integer.parseInt(enemyInformation[3]);
				 enemySubX=Integer.parseInt(enemyInformation[4]);
				 enemySubY=Integer.parseInt(enemyInformation[5]);
			}
	}

	// keyboard enter key 치면 서버로 전송
//	public class TextSendAction implements ActionListener {
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			// Send button을 누르거나 메시지 입력하고 Enter key 치면
//			if (e.getSource() == btnSend || e.getSource() == txtInput) {
//				String msg = null;
//				// msg = String.format("[%s] %s\n", userName, txtInput.getText());
//				msg = txtInput.getText();
//				SendMessage(msg);
//				txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
//				txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
//			}
//		}
//	}

	public RoomList getRoomList() {
		return roomList;
	}

	public String getRoomName() {
		return roomName;
	}

	// 화면에 출력
//	public void AppendText(String msg) {
//		// textArea.append(msg + "\n");
//		// AppendIcon(icon1);
//		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
//		int len = textArea.getDocument().getLength();
//		// 끝으로 이동
//		textArea.setCaretPosition(len);
//		textArea.replaceSelection(msg + "\n");
//	}

//	public void myAppendText(String msg) {
//		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
//		int len = textArea.getDocument().getLength();
//		// 끝으로 이동
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

	// Server에게 network으로 전송
//	public void SendMessage(String msg) {
//		roomList.SendMessage(msg, "200");
//	}
	

	
	public void initGame() { // 게임 시작시 변수 초기 설정
		
		myScore = 0;
		enemyScore = 0;
		startX = 4; // 가려지는 부분
		startY = 1; // 가려지는 부분
		comboCount = 0;
		delay = 17; // 17 / 1000 = 58프레임
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
				if (System.currentTimeMillis() - preTime < delay) // 시간 딜레이 맞추는 작업
					Thread.sleep(delay - System.currentTimeMillis() + preTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public void process() { // 각종 로직 처리
		switch(gameStatus) {
		case 0:
			break;
		case 1:
			if (checkDropDone()) dropPuyo();
			checkChainRule();
		}
	}
	

	public boolean checkDropDone() { // 낙하중이던 뿌요가 멈췄는지 확인(바닥에 닿았는지)
		if (myField[curY + 1][curX] != 0 || curY + 1 == subY) {
			if (myField[subY + 1][subX] != 0 || subY + 1 == curY) {
				myField[curY][curX] = curP1;
				myField[subY][subX] = curP2;
				
				return true;
			}
		}
			
		return false;
	}
	
	public void checkChainRule() { // 뿌요가 멈춘경우 파괴조건을 만족하는지 확인 후 파괴
		// 필드 전체를 스캔하며 확인해야함, 제거되고 내려온 뿌요도 확인
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

	
	
	
	public void checkGameOver() { // 게임 오버 처리(뿌요가 천장을 침)
		
		//gameStatus = 0;
		//loop = false;
	}
	
	//private void createPuyo(Graphics g) { // 다음 뿌요 생성(아직 대기상태)
	// 나중에 구현 일단 아래걸로 바로 시작위치에 생성하기
	//} 
	
	public void dropPuyo() { // 다음 뿌요 드랍(대기상태에서 꺼내서 낙하)
		curP1 = (int)(Math.random() * 5 + 1);
		curP2 = (int)(Math.random() * 5 + 1);
		curX = startX;
		curY = startY;
		subX = startX;
		subY = startY - 1;
		curShape = 0;
	}
	
	public void removePuyo(int x, int y) { // 뿌요 제거
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
	
	
	public void keyProcess() { // 뿌요 이동
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
			try { // 키가 너무 빠르게 먹으면 안됨 딜레이
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
} // 전체 클래스 끝


