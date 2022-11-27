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

	private final static int UP_PRESSED = 0x001; // �궎 媛�
	private final static int DOWN_PRESSED = 0x002;
	private final static int LEFT_PRESSED = 0x004;
	private final static int RIGHT_PRESSED = 0x008;
	public int keybuff; // �궎 踰꾪띁媛�

	public boolean loop = true;
	public int delay; // �봽�젅�엫 議곗젅 �뵜�젅�씠 1/1000 珥� �떒�쐞
	public long puyoDelay;
	public long preTime; // loop 媛꾧꺽 議곗젅�쓣 �쐞�븳 �떆媛� 泥댄겕

	public int gameStatus; // 寃뚯엫 �긽�깭 0:以묒�, 1: �떎�뻾以�

	public int myScore, enemyScore; // �굹�� �긽��諛� �젏�닔
	public int comboCount; // 肄ㅻ낫�뙆愿댁떆 利앷��븯�뿬 諛⑺빐肉뚯슂 �깮�꽦 �썑 0�쑝濡� 珥덇린�솕

	public int curX, curY; // �쁽�옱 議곗옉以묒씤 肉뚯슂�쓽 �쐞移�
	public int subX, subY; // 議곗옉以묒씤 �몢踰덉㎏ 肉뚯슂�쓽 �쐞移�
	public int curP1, curP2; // �쁽�옱 議곗옉以묒씤 肉뚯슂�쓽 醫낅쪟
	public int startX, startY; // �깉濡� 肉뚯슂 �깮�꽦�떆 �쐞移�

	public int enemyCurX, enemyCurY;
	public int enemySubX, enemySubY;
	public int enemyCurP1, enemyCurP2;
	private Clip clip;

	public int[][] myField = { // �궡 寃뚯엫 �븘�뱶 0: 鍮꾩뼱�엳�쓬 9: 梨꾩썙�졇�엳�쓬(踰�)
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, // 0,0 ~ 7,0
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 9, 9, 9, 9, 9, 9, 9 } // 0,13 ~ 7,13
	};

	public int[][] enemyField = { // �긽��諛� 寃뚯엫 �븘�뱶, �젣�뼱 X �삤濡쒖� 洹몃━湲곗슜
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 9, 9, 9, 9, 9, 9, 9 } };

	public int curShape; // �쁽�옱 議곗옉以묒씤 肉뚯슂�쓽 紐⑥뼇 (4媛�吏�) �닚�꽌��濡� �떆怨꾨갑�뼢�쑝濡� �쉶�쟾
	/**
	 * 0: . A . B媛� 而⑦듃濡ㅼ쓽 硫붿씤�씠 �릺�뒗 肉뚯슂�엫 . B . . . .
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
	 * 肉뚯슂�쓽 醫낅쪟 0: 鍮꾩뼱�엳�쓬 1: 鍮④컙肉뚯슂 2: �끂��肉뚯슂 3: 珥덈줉肉뚯슂 4: �뙆�옉肉뚯슂 5: 蹂대씪肉뚯슂 6: 諛⑺빐肉뚯슂(肄ㅻ낫�뙎�븘 怨듦꺽�떆 �깮�꽦)
	 */

	/**
	 * 寃뚯엫 �솕硫댁쓣 �겢�옒�뒪
	 */

	public boolean[][] visited = new boolean[14][8]; // �뙆愿댁껜�씤�쓣 �쐞�빐 諛⑸Ц�븳 �븘�뱶 湲곕줉
	public boolean[][] enemyVisited = new boolean[14][8];
	public ArrayList<Integer> visitedX = new ArrayList<Integer>(); // 吏��굹媛� �븘�뱶 湲곕줉�쓣 �쐞�븳 諛곗뿴
	public ArrayList<Integer> enemyVisitedX = new ArrayList<Integer>();
	public ArrayList<Integer> visitedY = new ArrayList<Integer>();
	public ArrayList<Integer> enemyVisitedY = new ArrayList<Integer>();
	public int destroyCount; // �뙆愿댁껜�씤�쓣 �쐞�븳 �뿰寃곕맂 肉뚯슂 移댁슫�듃
	public int enemyDestroyCount;
	public int checkGravity; // 0:以묐젰 X, 1: 以묐젰 O, 2: 以묐젰�쟻�슜�셿猷�, �뙆愿대줈吏� �떎�뻾
	public int enemyCheckGravity;

	class GameScreen extends Canvas {
		public GameView main;
		public Graphics gc; // �뜑釉붾쾭�띁留곸슜 洹몃옒�뵿 而⑦뀓�뒪�듃
		public Image doubleBuffer; // �뜑釉붾쾭�띁留곸슜 諛깅쾭�띁
		public Image backGround = new ImageIcon("src/resource/backGround.png").getImage(); // 諛곌꼍�씠誘몄�
		public Font font;
		public Image[] puyoTypeImg = { // 0�� puyoType�룄 鍮꾩뼱�엳�쓬 �씠誘�濡� �궗�슜�븯吏� �븡�뒗�떎
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

		public void drawField() { // field �쓽 肉뚯슂瑜� 洹몃━�뒗 �븿�닔
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
					System.out.println("�삤�봽�뒪�겕由� 踰꾪띁 �깮�꽦 �떎�뙣");
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

	} // GameScreen �겢�옒�뒪 �걹

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

		JButton btnNewButton = new JButton("醫� 猷�");
		btnNewButton.setFont(new Font("援대┝", Font.PLAIN, 14));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				roomList.SendMessage(roomName, "302");
				setVisible(false);
				gameStatus = 0;
				mainWork.interrupt();
				// clip.close(); �쓬�븙, �굹以묒뿉 �솢�꽦�솕�떆耳쒖쨪寃�
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
//        bgm = new File("src/resource/bgm.wav"); // �궗�슜�떆�뿉�뒗 媛쒕퀎 �뤃�뜑濡� 蹂�寃쏀븷 寃�
//        // �쁽�옱 �뙆�씪 �뾾�뒗 �긽�깭, 寃뚯엫�뿉�꽌 異붿텧�빐�꽌 �꽔���뮘 �닔�젙�븯寃좎쓬
//        
//        
//        try {
//               stream = AudioSystem.getAudioInputStream(bgm);
//               format = stream.getFormat();
//               info = new DataLine.Info(Clip.class, format);
//               clip = (Clip)AudioSystem.getLine(info);
//               clip.open(stream);
//              // clip.start(); //�떆�걚�윭�썙�꽌.. �떎 留뚮뱾怨� 二쇱꽍留� ��硫� �맖
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
			// clip.close(); �굹以묒뿉 �떎�뻾
			roomList.setVisible(true);
		} else if (cm.code.matches("400")) {
			initGame();
		} else if (cm.code.matches("501")) {
			String enemyInformation[] = cm.data.split(" "); // p1 p2 curx cury subx suby �닚�꽌
			enemyCurP1 = Integer.parseInt(enemyInformation[0]);
			enemyCurP2 = Integer.parseInt(enemyInformation[1]);
			enemyCurX = Integer.parseInt(enemyInformation[2]);
			enemyCurY = Integer.parseInt(enemyInformation[3]);
			enemySubX = Integer.parseInt(enemyInformation[4]);
			enemySubY = Integer.parseInt(enemyInformation[5]);
			if (enemyField[enemyCurY + 1][enemyCurX] != 0 || enemyCurY + 1 == enemySubY) { // �쟻 肉뚯슂�룄 媛숈씠 �솗�씤
				if (enemyField[enemySubY + 1][enemySubX] != 0 || enemySubY + 1 == enemyCurY) {
					enemyField[enemyCurY][enemyCurX] = enemyCurP1;
					enemyField[enemySubY][enemySubX] = enemyCurP2;
				}
			}
//			String arr[]=cm.data.split(" ");
//			int a=0;
//			for (int i = 0; i < 14; i++) {
//				for (int j = 0; j < 8; j++) {
//					enemyField[i][j]=Integer.parseInt(arr[a]);
//					a++;
//				}
//			}
		} else if (cm.code.matches("502")) {
			enemyCheckChainRule();
		} else if (cm.code.matches("505")) {
			if(enemyCheckGravity==2) {
				enemyCheckChainRule();
				enemyCheckGravity = 0;
			}
		} else if (cm.code.matches("506")) {
			String enemyInformation[] = cm.data.split(" "); // p1 p2 curx cury subx suby �닚�꽌
			int enemyCurP1 = Integer.parseInt(enemyInformation[0]);
			int enemyCurP2 = Integer.parseInt(enemyInformation[1]);
			int enemyCurX = Integer.parseInt(enemyInformation[2]);
			int enemyCurY = Integer.parseInt(enemyInformation[3]);
			int enemySubX = Integer.parseInt(enemyInformation[4]);
			int enemySubY = Integer.parseInt(enemyInformation[5]);
//				 if (enemyField[enemyCurY + 1][enemyCurX] != 0 || enemyCurY + 1 == enemySubY) { // �쟻 肉뚯슂�룄 媛숈씠 �솗�씤
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

	public void initGame() { // 寃뚯엫 �떆�옉�떆 蹂��닔 珥덇린 �꽕�젙

		myScore = 0;
		enemyScore = 0;
		startX = 4; // 媛��젮吏��뒗 遺�遺�
		startY = 1; // 媛��젮吏��뒗 遺�遺�
		comboCount = 0;
		delay = 17; // 17 / 1000 = 58�봽�젅�엫
		puyoDelay = 2000;
		gameStatus = 1;
		dropPuyo();
		mainWork = new Thread(this);
		mainWork.start();
		// abc();
		textField.setText("寃뚯엫 �떆�옉!!");
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
					puyoDown(); // 留뚯빟 �넻�떊 �뿉�윭媛� 諛쒖깮�븯硫� �썝�씤�씤吏� �솗�씤?
				}
			};
			timer.scheduleAtFixedRate(task, 1000, puyoDelay); // �몢踰덉㎏ �뙆�씪誘명꽣�뒗 �뼵�젣 �떆�옉�븷吏� 寃곗젙�븯�뒗 �븿�닔, puyodelay濡� 嫄몄쓣 �븘�슂媛� �뾾�쓬

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
							checkGravity = 2; // 以묐젰 �쁺�뼢�쓣 �떎 諛쏆쑝硫� 泥댁씤 猷� �떎�뻾
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
								enemyCheckGravity = 2; // 以묐젰 �쁺�뼢�쓣 �떎 諛쏆쑝硫� 泥댁씤 猷� �떎�뻾
						}
					}
			};
			enemyGravityTimer.scheduleAtFixedRate(enemyGravityTask, puyoDelay, 150);
			

			while (loop) {

				preTime = System.currentTimeMillis();
				gameScreen.repaint();
				process(); // �쟾泥� �봽濡쒖꽭�뒪 泥섎━
				keyProcess(); // �궎 �엯�젰 諛쏆븘�꽌 泥섎━

				if (System.currentTimeMillis() - preTime < delay) { // �떆媛� �뵜�젅�씠 留욎텛�뒗 �옉�뾽, 10二쇱감 �옄諛붽쾶�엫�뿉�꽌 媛��졇�샂
					Thread.sleep(delay - System.currentTimeMillis() + preTime);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void process() { // 媛곸쥌 濡쒖쭅 泥섎━
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
		if (enemyField[enemyCurY + 1][enemyCurX] != 0 || enemyCurY + 1 == enemySubY) { // �쟻 肉뚯슂�룄 媛숈씠 �솗�씤
			if (enemyField[enemySubY + 1][enemySubX] != 0 || enemySubY + 1 == enemyCurY) {
				enemyField[enemyCurY][enemyCurX] = enemyCurP1;
				enemyField[enemySubY][enemySubX] = enemyCurP2;
				return true;
			}
		}
		return false;
	}

	public boolean checkDropDone() { // �굺�븯以묒씠�뜕 肉뚯슂媛� 硫덉톬�뒗吏� �솗�씤(諛붾떏�뿉 �떯�븯�뒗吏�)

		if (enemyField[enemyCurY + 1][enemyCurX] != 0 || enemyCurY + 1 == enemySubY) { // �쟻 肉뚯슂�룄 媛숈씠 �솗�씤
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

	public void checkChainRule() { // 紐⑤뱺 �븘�뱶瑜� �솗�씤�븯硫� 諛⑸Ц�븯吏� �븡怨�, 肉뚯슂媛� �엳�뒗寃쎌슦 �뙆愿대줈吏곸떎�뻾
		// 怨꾩냽�빐�꽌 �룎由щ㈃ �삤瑜섎컻�깮 媛��뒫, 肉뚯슂媛� �굺�븯�맂(議곗옉�빐�꽌�씠�뜕, �뙆愿대릺�꽌�씠�뜕) 寃쎌슦�뿉留� �떎�뻾
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
				} // 諛⑸Ц, 肉뚯슂議댁옱�솗�씤
			} // y for 臾�
		} // x for臾�
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
				} // 諛⑸Ц, 肉뚯슂議댁옱�솗�씤
			} // y for 臾�
		} // x for臾�
	}

	public boolean puyoDestroy(int x, int y, int puyo_type) { // �뙆愿대줈吏곸떎�뻾, �씤�젒 肉뚯슂�뱾�쓣 �솗�씤�븯硫� �옄�떊怨� �룞�씪�븯硫� 移댁슫�듃 利앷�

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

	public boolean enemyPuyoDestroy(int x, int y, int puyo_type) { // �뙆愿대줈吏곸떎�뻾, �씤�젒 肉뚯슂�뱾�쓣 �솗�씤�븯硫� �옄�떊怨� �룞�씪�븯硫� 移댁슫�듃 利앷�

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

	public void realDestroyPuyo() { // �떎�젣 肉뚯슂�뱾�쓣 �븘�뱶�뿉�꽌 �젣嫄�, �쐞�뿉 �뙎�씤 肉뚯슂�뱾�쓣 �뱶�엻??

		for (int i = 0; i < visitedX.size(); i++) {
			myField[visitedX.get(i)][visitedY.get(i)] = 0;
		}

		myScore += destroyCount * 10;
		// System.out.println(String.format("my score : %d", myScore)); // �뵒踰꾧퉭�슜
		clearVisitedField();
		checkGravity = 1;
	}
	
	public void enemyRealDestroyPuyo() { // �떎�젣 肉뚯슂�뱾�쓣 �븘�뱶�뿉�꽌 �젣嫄�, �쐞�뿉 �뙎�씤 肉뚯슂�뱾�쓣 �뱶�엻??

		for (int i = 0; i < enemyVisitedX.size(); i++) {
			enemyField[enemyVisitedX.get(i)][enemyVisitedY.get(i)] = 0;
		}
		// System.out.println(String.format("my score : %d", myScore)); // �뵒踰꾧퉭�슜
		enemyClearVisitedField();
		enemyCheckGravity = 1;
	}

	public void clearVisitedField() { // 諛⑸Ц�궗�떎 珥덇린�솕
		for (int i = 0; i < 13; i++) {
			for (int j = 1; j < 7; j++) {
				visited[i][j] = false;
			}
		}
	}

	public void enemyClearVisitedField() { // 諛⑸Ц�궗�떎 珥덇린�솕
		for (int i = 0; i < 13; i++) {
			for (int j = 1; j < 7; j++) {
				enemyVisited[i][j] = false;
			}
		}
	}

	public void gravity() { // �븘�옒�뿉 �엳�뒗 肉뚯슂媛� �뙆愿대맆 �떆 �쐞�뿉 �엳�뒗 肉뚯슂�뱾�� 鍮꾩뼱�엳�뒗 怨듦컙�쑝濡� �궡�젮�샂

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
			} else { // �븳履쎌씠 �걡湲곕㈃ �굹癒몄� �븳履쎌� �옄�쑀�굺�븯
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

	public void checkGameOver() { // 寃뚯엫 �삤踰� 泥섎━(肉뚯슂媛� 泥쒖옣�쓣 移�)

		// gameStatus = 0;
		// loop = false;
	}

	// private void createPuyo(Graphics g) { // �떎�쓬 肉뚯슂 �깮�꽦(�븘吏� ��湲곗긽�깭)
	// �굹以묒뿉 援ы쁽 �씪�떒 �븘�옒嫄몃줈 諛붾줈 �떆�옉�쐞移섏뿉 �깮�꽦�븯湲�
	// }

	public void dropPuyo() { // �떎�쓬 肉뚯슂 �뱶�엻(��湲곗긽�깭�뿉�꽌 爰쇰궡�꽌 �굺�븯)
		curP1 = (int) (Math.random() * 5 + 1);
		curP2 = (int) (Math.random() * 5 + 1);
		curX = startX;
		curY = startY;
		subX = startX;
		subY = startY - 1;
		curShape = 0;
	}

	public void removePuyo(int x, int y) { // 肉뚯슂 �젣嫄�
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
		if(gameStatus==1) {
			roomList.SendMessage(curP1 + " " + curP2 + " " + curX + " " + curY + " " + subX + " " + subY, "501");
		}
	}

	public void keyProcess() { // 肉뚯슂 �씠�룞
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
//				if(gameStatus==1) {
//					String msg="";
//					for (int i = 0; i < 14; i++) {
//						for (int j = 0; j < 8; j++) {
//							msg+=myField[i][j]+" ";
//						}
//					}
//					roomList.SendMessage(msg, "501");
//				}
				roomList.SendMessage(curP1 + " " + curP2 + " " + curX + " " + curY + " " + subX + " " + subY, "501");
				break;
			}
			if (!Thread.currentThread().isInterrupted()) {
				try { // �궎媛� �꼫臾� 鍮좊Ⅴ寃� 癒뱀쑝硫� �븞�맖 �뵜�젅�씠
					Thread.sleep(80); // �빟媛� 0.05珥덈뒗 �꼫臾� 吏㏃� �뒓�굦?
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
} // �쟾泥� �겢�옒�뒪 �걹
