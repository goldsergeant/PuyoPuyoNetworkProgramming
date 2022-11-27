package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.security.PublicKey;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
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
	public JLabel lbluserName;
	public JTextPane textArea;
	public RoomList roomList;
	public String roomName;

	public GameScreen gameScreen;
	Thread mainWork;

	private final static int UP_PRESSED = 0x001; // 키 값
	private final static int DOWN_PRESSED = 0x002;
	private final static int LEFT_PRESSED = 0x004;
	private final static int RIGHT_PRESSED = 0x008;
	public int keybuff; // 키 버퍼값

	public boolean loop = true;
	public int delay; // 프레임 조절 딜레이 1/1000 초 단위
	public long puyoDelay;
	public long preTime; // loop 간격 조절을 위한 시간 체크

	public int gameStatus; // 게임 상태 0:중지, 1: 실행중

	public int myScore, enemyScore; // 나와 상대방 점수
	public int comboCount; // 콤보파괴시 증가하여 방해뿌요 생성 후 0으로 초기화

	public int curX, curY; // 현재 조작중인 뿌요의 위치
	public int subX, subY; // 조작중인 두번째 뿌요의 위치
	public int curP1, curP2; // 현재 조작중인 뿌요의 종류
	public int startX, startY; // 새로 뿌요 생성시 위치

	public int enemyCurX, enemyCurY;
	public int enemySubX, enemySubY;
	public int enemyCurP1, enemyCurP2;
	private Clip clip;

	public int[][] myField = { // 내 게임 필드 0: 비어있음 9: 채워져있음(벽)
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, // 0,0 ~ 7,0
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 9, 9, 9, 9, 9, 9, 9 } // 0,13 ~ 7,13
	};

	public int[][] enemyField = { // 상대방 게임 필드, 제어 X 오로지 그리기용
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 9, 9, 9, 9, 9, 9, 9 } };

	public int curShape; // 현재 조작중인 뿌요의 모양 (4가지) 순서대로 시계방향으로 회전
	/**
	 * 0: . A . B가 컨트롤의 메인이 되는 뿌요임 . B . . . .
	 * 
	 * 1: . . . . B A . . .
	 * 
	 * 2: . . . . B . . A .
	 * 
	 * 3: . . . A B . . . .
	 */

	private JTextField textField;
	public int puyoType;
	/**
	 * 뿌요의 종류 0: 비어있음 1: 빨간뿌요 2: 노란뿌요 3: 초록뿌요 4: 파랑뿌요 5: 보라뿌요 6: 방해뿌요(콤보쌓아 공격시 생성)
	 */

	/**
	 * 게임 화면을 클래스
	 */

	public boolean[][] visited = new boolean[14][8]; // 파괴체인을 위해 방문한 필드 기록
	public boolean[][] enemyVisited = new boolean[14][8];
	public ArrayList<Integer> visitedX = new ArrayList<Integer>(); // 지나간 필드 기록을 위한 배열
	public ArrayList<Integer> enemyVisitedX = new ArrayList<Integer>();
	public ArrayList<Integer> visitedY = new ArrayList<Integer>();
	public ArrayList<Integer> enemyVisitedY = new ArrayList<Integer>();
	public int destroyCount; // 파괴체인을 위한 연결된 뿌요 카운트
	public int enemyDestroyCount;
	public int checkGravity; // 0:중력 X, 1: 중력 O, 2: 중력적용완료, 파괴로직 실행
	public int enemyCheckGravity;

	class GameScreen extends Canvas {
		public GameView main;
		public Graphics gc; // 더블버퍼링용 그래픽 컨텍스트
		public Image doubleBuffer; // 더블버퍼링용 백버퍼
		public Image backGround = new ImageIcon("src/resource/backGround.png").getImage(); // 배경이미지
		public Font font;
		public Image[] puyoTypeImg = { // 0은 puyoType도 비어있음 이므로 사용하지 않는다
				null, new ImageIcon("src/resource/puyoRed.png").getImage(),
				new ImageIcon("src/resource/puyoYellow.png").getImage(),
				new ImageIcon("src/resource/puyoGreen.png").getImage(),
				new ImageIcon("src/resource/puyoBlue.png").getImage(),
				new ImageIcon("src/resource/puyoPurple.png").getImage() };

		public GameScreen(GameView gameView) {
			this.main = gameView;
			backGround = new ImageIcon("src/resource/backGround.png").getImage();
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
						gc.drawImage(puyoTypeImg[enemyField[i][j]], 32 * 12 + 32 * j, 32 * i, this);
					}
				}
			}
		}

		public void drawBackGround() {
			if (gc != null)
				gc.drawImage(backGround, 0, 0, this);
		}

		public void drawMyPuyo() {
			if (gc != null && curP1 != 0 && curP2 != 0 && curX != 0 && curY != 0 && subX != 0 && subY != 0) {
				gc.drawImage(puyoTypeImg[curP1], 32 * curX, 32 * curY, this);
				gc.drawImage(puyoTypeImg[curP2], 32 * subX, 32 * subY, this);
			}
		}

		public void drawEnemyPuyo() {

			if (gc != null && enemyCurP1 != 0 && enemyCurP2 != 0 && enemyCurX != 0 && enemyCurY != 0 && enemySubX != 0
					&& enemySubY != 0) {
				gc.drawImage(puyoTypeImg[enemyCurP1], 32 * 12 + 32 * enemyCurX, 32 * enemyCurY, this);
				gc.drawImage(puyoTypeImg[enemyCurP2], 32 * 12 + 32 * enemySubX, 32 * enemySubY, this);
			}
		}

		public void paint(Graphics g) {
			if (gc == null) {
				doubleBuffer = createImage(640, 480);
				if (doubleBuffer == null)
					System.out.println("오프스크린 버퍼 생성 실패");
				else
					gc = doubleBuffer.getGraphics();
				return;
			}
			update(g);
		}

		public void update(Graphics g) {
			if (gc == null)
				return;
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
				break;
			}
		}

	} // GameScreen 클래스 끝

	/**
	 * Create the frame.
	 */
	public GameView(String userName, String ip_addr, String port_no, RoomList roomList, String roomName) {
		super("Puyo Puyo2!!");
		this.roomList = roomList;
		this.roomName = roomName;
		this.UserName = userName;
		setBounds(100, 100, 980, 560);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setVisible(true);

		JButton btnNewButton = new JButton("종 료");
		btnNewButton.setFont(new Font("굴림", Font.PLAIN, 14));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				roomList.SendMessage(roomName, "302");
				setVisible(false);
				gameStatus = 0;
				mainWork.interrupt();
				// clip.close(); 음악, 나중에 활성화시켜줄것
				roomList.setVisible(true);
			}
		});
		btnNewButton.setBounds(880, 459, 69, 40);
		contentPane.add(btnNewButton);

		gameScreen = new GameScreen(this);
		gameScreen.setFocusable(true);
		gameScreen.setBounds(10, 10, 640, 480);
		contentPane.add(gameScreen);

		textField = new JTextField();
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setText("\uB300\uAE30..");
		textField.setBounds(815, 10, 134, 46);
		contentPane.add(textField);
		textField.setColumns(10);
		addKeyListener(this);

		gameScreen.requestFocus();
		gameScreen.repaint();
	}

	public void abc() {
//        File bgm;
//        AudioInputStream stream;
//        AudioFormat format;
//        DataLine.Info info;
//        
//        bgm = new File("src/resource/bgm.wav"); // 사용시에는 개별 폴더로 변경할 것
//        // 현재 파일 없는 상태, 게임에서 추출해서 넣은뒤 수정하겠음
//        
//        
//        try {
//               stream = AudioSystem.getAudioInputStream(bgm);
//               format = stream.getFormat();
//               info = new DataLine.Info(Clip.class, format);
//               clip = (Clip)AudioSystem.getLine(info);
//               clip.open(stream);
//              // clip.start(); //시끄러워서.. 다 만들고 주석만 풀면 됨
//              // clip.loop(100); 
//               
//        } catch (Exception e) {
//           System.out.println("err : " + e);
//       }

	}

	public void readMessage(GameMsg cm) {

		if (cm.data.equals(roomName)) {
			setVisible(false);
			gameStatus = 0;
			mainWork.interrupt();
			// clip.close(); 나중에 실행
			roomList.setVisible(true);
		} else if (cm.code.matches("400")) {
			initGame();
		} else if (cm.code.matches("501")) {
			String enemyInformation[] = cm.data.split(" "); // p1 p2 curx cury subx suby 순서
			enemyCurP1 = Integer.parseInt(enemyInformation[0]);
			enemyCurP2 = Integer.parseInt(enemyInformation[1]);
			enemyCurX = Integer.parseInt(enemyInformation[2]);
			enemyCurY = Integer.parseInt(enemyInformation[3]);
			enemySubX = Integer.parseInt(enemyInformation[4]);
			enemySubY = Integer.parseInt(enemyInformation[5]);
			if (enemyField[enemyCurY + 1][enemyCurX] != 0 || enemyCurY + 1 == enemySubY) { // 적 뿌요도 같이 확인
				if (enemyField[enemySubY + 1][enemySubX] != 0 || enemySubY + 1 == enemyCurY) {
					enemyField[enemyCurY][enemyCurX] = enemyCurP1;
					enemyField[enemySubY][enemySubX] = enemyCurP2;
				}
			}
		} else if (cm.code.matches("502")) {
			enemyCheckChainRule();
		} else if (cm.code.matches("505")) {
			if(enemyCheckGravity==2) {
				enemyCheckChainRule();
				enemyCheckGravity = 0;
			}
		} else if (cm.code.matches("506")) {
			String enemyInformation[] = cm.data.split(" "); // p1 p2 curx cury subx suby 순서
			int enemyCurP1 = Integer.parseInt(enemyInformation[0]);
			int enemyCurP2 = Integer.parseInt(enemyInformation[1]);
			int enemyCurX = Integer.parseInt(enemyInformation[2]);
			int enemyCurY = Integer.parseInt(enemyInformation[3]);
			int enemySubX = Integer.parseInt(enemyInformation[4]);
			int enemySubY = Integer.parseInt(enemyInformation[5]);
//				 if (enemyField[enemyCurY + 1][enemyCurX] != 0 || enemyCurY + 1 == enemySubY) { // 적 뿌요도 같이 확인
//						if (enemyField[enemySubY + 1][enemySubX] != 0 || enemySubY + 1 == enemyCurY) {
			enemyField[enemyCurY][enemyCurX] = enemyCurP1;
			enemyField[enemySubY][enemySubX] = enemyCurP2;
//						}
//					}
			enemyCheckGravity=1;
		}
	}

	public RoomList getRoomList() {
		return roomList;
	}

	public String getRoomName() {
		return roomName;
	}

	public void initGame() { // 게임 시작시 변수 초기 설정

		myScore = 0;
		enemyScore = 0;
		startX = 4; // 가려지는 부분
		startY = 1; // 가려지는 부분
		comboCount = 0;
		delay = 17; // 17 / 1000 = 58프레임
		puyoDelay = 2000;
		gameStatus = 1;
		dropPuyo();
		mainWork = new Thread(this);
		mainWork.start();
		// abc();
		textField.setText("게임 시작!!");
		destroyCount = 0;
		enemyDestroyCount=0;
		checkGravity = 0;
		enemyCheckGravity=0;
		visitedX.clear();
		visitedY.clear();
	}

	public void run() {
		try {
			Timer timer = new Timer();
			TimerTask task = new TimerTask() {
				public void run() {
					puyoDown(); // 만약 통신 에러가 발생하면 원인인지 확인?
				}
			};
			timer.scheduleAtFixedRate(task, 1000, puyoDelay); // 두번째 파라미터는 언제 시작할지 결정하는 함수, puyodelay로 걸을 필요가 없음

			Timer gravityTimer = new Timer();
			TimerTask gravityTask = new TimerTask() {
				public void run() {
					if (checkGravity == 1) {
						checkGravity = 0;

						for (int i = 11; i >= 0; i--) {
							for (int j = 1; j < 7; j++) {
								if (myField[i + 1][j] == 0 && gameStatus == 1) {
									if (myField[i][j] > 0) {
										myField[i + 1][j] = myField[i][j];
										myField[i][j] = 0;
									}
									checkGravity = 1;
								}
							}
						}

						if (checkGravity == 0)
							checkGravity = 2; // 중력 영향을 다 받으면 체인 룰 실행
					}
				}
			};
				gravityTimer.scheduleAtFixedRate(gravityTask, puyoDelay, 150);
				
				Timer enemyGravityTimer = new Timer();
				TimerTask enemyGravityTask = new TimerTask() {
					public void run() {
						if (enemyCheckGravity == 1) {
							enemyCheckGravity = 0;

							for (int i = 11; i >= 0; i--) {
								for (int j = 1; j < 7; j++) {
									if (enemyField[i + 1][j] == 0 && gameStatus == 1) {
										if (enemyField[i][j] > 0) {
											enemyField[i + 1][j] = enemyField[i][j];
											enemyField[i][j] = 0;
										}
										enemyCheckGravity = 1;
									}
								}
							}

							if (enemyCheckGravity == 0)
								enemyCheckGravity = 2; // 중력 영향을 다 받으면 체인 룰 실행
						}
					}
			};
			enemyGravityTimer.scheduleAtFixedRate(enemyGravityTask, puyoDelay, 150);
			

			while (loop) {

				preTime = System.currentTimeMillis();
				gameScreen.repaint();
				process(); // 전체 프로세스 처리
				keyProcess(); // 키 입력 받아서 처리

				if (System.currentTimeMillis() - preTime < delay) { // 시간 딜레이 맞추는 작업, 10주차 자바게임에서 가져옴
					Thread.sleep(delay - System.currentTimeMillis() + preTime);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void process() { // 각종 로직 처리
		switch (gameStatus) {
		case 0:
			break;
		case 1:
			if (checkDropDone()) {
				roomList.SendMessage("", "502");
				roomList.SendMessage("", "505");
				dropPuyo();
				checkChainRule();
				if (checkGravity == 2) {
					checkChainRule();
					checkGravity = 0;
				}
			}
		}
	}

	public boolean checkEnemyDropDone() {
		if (enemyField[enemyCurY + 1][enemyCurX] != 0 || enemyCurY + 1 == enemySubY) { // 적 뿌요도 같이 확인
			if (enemyField[enemySubY + 1][enemySubX] != 0 || enemySubY + 1 == enemyCurY) {
				enemyField[enemyCurY][enemyCurX] = enemyCurP1;
				enemyField[enemySubY][enemySubX] = enemyCurP2;
				return true;
			}
		}
		return false;
	}

	public boolean checkDropDone() { // 낙하중이던 뿌요가 멈췄는지 확인(바닥에 닿았는지)

		if (enemyField[enemyCurY + 1][enemyCurX] != 0 || enemyCurY + 1 == enemySubY) { // 적 뿌요도 같이 확인
			if (enemyField[enemySubY + 1][enemySubX] != 0 || enemySubY + 1 == enemyCurY) {
				enemyField[enemyCurY][enemyCurX] = enemyCurP1;
				enemyField[enemySubY][enemySubX] = enemyCurP2;
			}
		}

		if (myField[curY + 1][curX] != 0 || curY + 1 == subY) {
			if (myField[subY + 1][subX] != 0 || subY + 1 == curY) {
				myField[curY][curX] = curP1;
				myField[subY][subX] = curP2;
				if (gameStatus == 1)
					roomList.SendMessage(curP1 + " " + curP2 + " " + curX + " " + curY + " " + subX + " " + subY,
							"501");
				return true;
			}
		}

		return false;
	}

	public void checkChainRule() { // 모든 필드를 확인하며 방문하지 않고, 뿌요가 있는경우 파괴로직실행
		// 계속해서 돌리면 오류발생 가능, 뿌요가 낙하된(조작해서이던, 파괴되서이던) 경우에만 실행
		clearVisitedField();
		visitedX.clear();
		visitedY.clear();

		for (int i = 0; i < 13; i++) {
			for (int j = 1; j < 7; j++) {
				if (myField[i][j] != 0 && !visited[i][j]) {
					destroyCount = 0;
					if (puyoDestroy(i, j, myField[i][j])) {
						if (destroyCount > 3)
							realDestroyPuyo();
						visitedX.clear();
						visitedY.clear();
					}
				} // 방문, 뿌요존재확인
			} // y for 문
		} // x for문
	}

	public void enemyCheckChainRule() {
		enemyClearVisitedField();
		enemyVisitedX.clear();
		enemyVisitedY.clear();

		for (int i = 0; i < 13; i++) {
			for (int j = 1; j < 7; j++) {
				if (enemyField[i][j] != 0 && !enemyVisited[i][j]) {
					enemyDestroyCount = 0;
					if (enemyPuyoDestroy(i, j, enemyField[i][j])) {
						if (enemyDestroyCount > 3)
							enemyRealDestroyPuyo();
						enemyVisitedX.clear();
						enemyVisitedY.clear();
					}
				} // 방문, 뿌요존재확인
			} // y for 문
		} // x for문
	}

	public boolean puyoDestroy(int x, int y, int puyo_type) { // 파괴로직실행, 인접 뿌요들을 확인하며 자신과 동일하면 카운트 증가

		visited[x][y] = true;
		destroyCount++;

		visitedX.add(x);
		visitedY.add(y);
		if (!visited[x + 1][y] && myField[x + 1][y] == puyo_type) {
			if (puyoDestroy(x + 1, y, puyo_type))
				;
		}
		if (!visited[x][y + 1] && myField[x][y + 1] == puyo_type) {
			if (puyoDestroy(x, y + 1, puyo_type))
				;
		}
		if (x != 0 && !visited[x - 1][y] && myField[x - 1][y] == puyo_type) {
			if (puyoDestroy(x - 1, y, puyo_type))
				;
		}
		if (!visited[x][y - 1] && myField[x][y - 1] == puyo_type) {
			if (puyoDestroy(x, y - 1, puyo_type))
				;
		}
		return true;
	}

	public boolean enemyPuyoDestroy(int x, int y, int puyo_type) { // 파괴로직실행, 인접 뿌요들을 확인하며 자신과 동일하면 카운트 증가

		enemyVisited[x][y] = true;
		enemyDestroyCount++;
		
		enemyVisitedX.add(x);
		enemyVisitedY.add(y);
		if (!enemyVisited[x + 1][y] && enemyField[x + 1][y] == puyo_type) {
			if (enemyPuyoDestroy(x + 1, y, puyo_type))
				;
		}
		if (!enemyVisited[x][y + 1] && enemyField[x][y + 1] == puyo_type) {
			if (enemyPuyoDestroy(x, y + 1, puyo_type))
				;
		}
		if (x != 0 && !enemyVisited[x - 1][y] && enemyField[x - 1][y] == puyo_type) {
			if (enemyPuyoDestroy(x - 1, y, puyo_type))
				;
		}
		if (!enemyVisited[x][y - 1] && enemyField[x][y - 1] == puyo_type) {
			if (enemyPuyoDestroy(x, y - 1, puyo_type))
				;
		}
		return true;
	}

	public void realDestroyPuyo() { // 실제 뿌요들을 필드에서 제거, 위에 쌓인 뿌요들을 드랍??

		for (int i = 0; i < visitedX.size(); i++) {
			myField[visitedX.get(i)][visitedY.get(i)] = 0;
		}

		myScore += destroyCount * 10;
		// System.out.println(String.format("my score : %d", myScore)); // 디버깅용
		clearVisitedField();
		checkGravity = 1;
	}
	
	public void enemyRealDestroyPuyo() { // 실제 뿌요들을 필드에서 제거, 위에 쌓인 뿌요들을 드랍??

		for (int i = 0; i < enemyVisitedX.size(); i++) {
			enemyField[enemyVisitedX.get(i)][enemyVisitedY.get(i)] = 0;
		}
		// System.out.println(String.format("my score : %d", myScore)); // 디버깅용
		enemyClearVisitedField();
		enemyCheckGravity = 1;
	}

	public void clearVisitedField() { // 방문사실 초기화
		for (int i = 0; i < 13; i++) {
			for (int j = 1; j < 7; j++) {
				visited[i][j] = false;
			}
		}
	}

	public void enemyClearVisitedField() { // 방문사실 초기화
		for (int i = 0; i < 13; i++) {
			for (int j = 1; j < 7; j++) {
				enemyVisited[i][j] = false;
			}
		}
	}

	public void gravity() { // 아래에 있는 뿌요가 파괴될 시 위에 있는 뿌요들은 비어있는 공간으로 내려옴

	}

	public void cutConnect() {
		myField[curY][curX] = curP1;
		myField[subY][subX] = curP2;
		checkGravity = 1;
		dropPuyo();
	}

	public void enemyCutConnect() {
		enemyField[enemyCurY][enemyCurX] = enemyCurP1;
		enemyField[enemySubY][enemySubX] = enemyCurP2;
		enemyCheckGravity=1;
	}

	public void keyPressed(KeyEvent e) {

		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			keybuff |= UP_PRESSED;
			break;
		case KeyEvent.VK_DOWN:
			keybuff |= DOWN_PRESSED;
			puyoDelay = 300;
			break;
		case KeyEvent.VK_LEFT:
			keybuff |= LEFT_PRESSED;
			break;
		case KeyEvent.VK_RIGHT:
			keybuff |= RIGHT_PRESSED;
			break;
		default:
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			keybuff &= ~UP_PRESSED;
			break;
		case KeyEvent.VK_DOWN:
			keybuff &= ~DOWN_PRESSED;
			puyoDelay = 1000;
			break;
		case KeyEvent.VK_LEFT:
			keybuff &= ~LEFT_PRESSED;
			break;
		case KeyEvent.VK_RIGHT:
			keybuff &= ~RIGHT_PRESSED;
			break;
		default:
			break;
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void puyoDown() {

		switch (curShape) {
		case 0:
			if (myField[curY + 1][curX] == 0) {
				curY++;
				subY++;
			}
			break;
		case 1:
			if (myField[curY + 1][curX] == 0 && myField[subY + 1][subX] == 0) {
				curY++;
				subY++;
			} else { // 한쪽이 끊기면 나머지 한쪽은 자유낙하
				roomList.SendMessage(curP1 + " " + curP2 + " " + curX + " " + curY + " " + subX + " " + subY, "506");
				cutConnect();
			}
			break;
		case 2:
			if (myField[curY + 2][curX] == 0) {
				curY++;
				subY++;
			}
			break;
		case 3:
			if (myField[curY + 1][curX] == 0 && myField[subY + 1][subX] == 0) {
				curY++;
				subY++;
			} else {
				roomList.SendMessage(curP1 + " " + curP2 + " " + curX + " " + curY + " " + subX + " " + subY, "506");
				cutConnect();
			}
			break;
		}
		if (gameStatus == 1)
			roomList.SendMessage(curP1 + " " + curP2 + " " + curX + " " + curY + " " + subX + " " + subY, "501");
	}

	public void checkGameOver() { // 게임 오버 처리(뿌요가 천장을 침)

		// gameStatus = 0;
		// loop = false;
	}

	// private void createPuyo(Graphics g) { // 다음 뿌요 생성(아직 대기상태)
	// 나중에 구현 일단 아래걸로 바로 시작위치에 생성하기
	// }

	public void dropPuyo() { // 다음 뿌요 드랍(대기상태에서 꺼내서 낙하)
		curP1 = (int) (Math.random() * 5 + 1);
		curP2 = (int) (Math.random() * 5 + 1);
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
		switch (curShape) {
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
		roomList.SendMessage(curP1 + " " + curP2 + " " + curX + " " + curY + " " + subX + " " + subY, "501");
	}

	public void keyProcess() { // 뿌요 이동
		switch (gameStatus) {
		case 0:
			break;
		case 1:
			switch (keybuff) {
			case 0:
				break;
			case UP_PRESSED:
				turnPuyo();
				break;
			case DOWN_PRESSED:
				puyoDown();
				break;
			case LEFT_PRESSED:
				switch (curShape) {
				case 0:
					if (myField[curY][curX - 1] == 0 && myField[curY - 1][curX - 1] == 0) {
						curX--;
						subX--;
					}
					break;
				case 1:
					if (myField[curY][curX - 1] == 0) {
						curX--;
						subX--;
					}
					break;
				case 2:
					if (myField[curY][curX - 1] == 0 && myField[curY + 1][curX - 1] == 0) {
						curX--;
						subX--;
					}
					break;
				case 3:
					if (myField[curY][curX - 2] == 0) {
						curX--;
						subX--;
					}
					break;
				}
				roomList.SendMessage(curP1 + " " + curP2 + " " + curX + " " + curY + " " + subX + " " + subY, "501");
				break;
			case RIGHT_PRESSED:
				switch (curShape) {
				case 0:
					if (myField[curY][curX + 1] == 0 && myField[curY - 1][curX + 1] == 0) {
						curX++;
						subX++;
					}
					break;
				case 1:
					if (myField[curY][curX + 2] == 0) {
						curX++;
						subX++;
					}
					break;
				case 2:
					if (myField[curY][curX + 1] == 0 && myField[curY + 1][curX + 1] == 0) {
						curX++;
						subX++;
					}
					break;
				case 3:
					if (myField[curY][curX + 1] == 0) {
						curX++;
						subX++;
					}
					break;
				}
				roomList.SendMessage(curP1 + " " + curP2 + " " + curX + " " + curY + " " + subX + " " + subY, "501");
				break;
			}
			try { // 키가 너무 빠르게 먹으면 안됨 딜레이
				Thread.sleep(100); // 약간 0.05초는 너무 짧은 느낌?
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
} // 전체 클래스 끝
