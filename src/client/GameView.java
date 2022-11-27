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

	private final static int UP_PRESSED = 0x001; // Ű ��
	private final static int DOWN_PRESSED = 0x002;
	private final static int LEFT_PRESSED = 0x004;
	private final static int RIGHT_PRESSED = 0x008;
	public int keybuff; // Ű ���۰�

	public boolean loop = true;
	public int delay; // ������ ���� ������ 1/1000 �� ����
	public long puyoDelay;
	public long preTime; // loop ���� ������ ���� �ð� üũ

	public int gameStatus; // ���� ���� 0:����, 1: ������

	public int myScore, enemyScore; // ���� ���� ����
	public int comboCount; // �޺��ı��� �����Ͽ� ���ػѿ� ���� �� 0���� �ʱ�ȭ

	public int curX, curY; // ���� �������� �ѿ��� ��ġ
	public int subX, subY; // �������� �ι�° �ѿ��� ��ġ
	public int curP1, curP2; // ���� �������� �ѿ��� ����
	public int startX, startY; // ���� �ѿ� ������ ��ġ

	public int enemyCurX, enemyCurY;
	public int enemySubX, enemySubY;
	public int enemyCurP1, enemyCurP2;
	private Clip clip;

	public int[][] myField = { // �� ���� �ʵ� 0: ������� 9: ä��������(��)
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, // 0,0 ~ 7,0
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 9, 9, 9, 9, 9, 9, 9 } // 0,13 ~ 7,13
	};

	public int[][] enemyField = { // ���� ���� �ʵ�, ���� X ������ �׸����
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 0, 0, 0, 0, 0, 0, 9 },
			{ 9, 0, 0, 0, 0, 0, 0, 9 }, { 9, 9, 9, 9, 9, 9, 9, 9 } };

	public int curShape; // ���� �������� �ѿ��� ��� (4����) ������� �ð�������� ȸ��
	/**
	 * 0: . A . B�� ��Ʈ���� ������ �Ǵ� �ѿ��� . B . . . .
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
	 * �ѿ��� ���� 0: ������� 1: �����ѿ� 2: ����ѿ� 3: �ʷϻѿ� 4: �Ķ��ѿ� 5: ����ѿ� 6: ���ػѿ�(�޺��׾� ���ݽ� ����)
	 */

	/**
	 * ���� ȭ���� Ŭ����
	 */

	public boolean[][] visited = new boolean[14][8]; // �ı�ü���� ���� �湮�� �ʵ� ���
	public boolean[][] enemyVisited = new boolean[14][8];
	public ArrayList<Integer> visitedX = new ArrayList<Integer>(); // ������ �ʵ� ����� ���� �迭
	public ArrayList<Integer> enemyVisitedX = new ArrayList<Integer>();
	public ArrayList<Integer> visitedY = new ArrayList<Integer>();
	public ArrayList<Integer> enemyVisitedY = new ArrayList<Integer>();
	public int destroyCount; // �ı�ü���� ���� ����� �ѿ� ī��Ʈ
	public int enemyDestroyCount;
	public int checkGravity; // 0:�߷� X, 1: �߷� O, 2: �߷�����Ϸ�, �ı����� ����
	public int enemyCheckGravity;

	class GameScreen extends Canvas {
		public GameView main;
		public Graphics gc; // ������۸��� �׷��� ���ؽ�Ʈ
		public Image doubleBuffer; // ������۸��� �����
		public Image backGround = new ImageIcon("src/resource/backGround.png").getImage(); // ����̹���
		public Font font;
		public Image[] puyoTypeImg = { // 0�� puyoType�� ������� �̹Ƿ� ������� �ʴ´�
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
					System.out.println("������ũ�� ���� ���� ����");
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

	} // GameScreen Ŭ���� ��

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

		JButton btnNewButton = new JButton("�� ��");
		btnNewButton.setFont(new Font("����", Font.PLAIN, 14));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				roomList.SendMessage(roomName, "302");
				setVisible(false);
				gameStatus = 0;
				mainWork.interrupt();
				// clip.close(); ����, ���߿� Ȱ��ȭ�����ٰ�
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
//        bgm = new File("src/resource/bgm.wav"); // ���ÿ��� ���� ������ ������ ��
//        // ���� ���� ���� ����, ���ӿ��� �����ؼ� ������ �����ϰ���
//        
//        
//        try {
//               stream = AudioSystem.getAudioInputStream(bgm);
//               format = stream.getFormat();
//               info = new DataLine.Info(Clip.class, format);
//               clip = (Clip)AudioSystem.getLine(info);
//               clip.open(stream);
//              // clip.start(); //�ò�������.. �� ����� �ּ��� Ǯ�� ��
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
			// clip.close(); ���߿� ����
			roomList.setVisible(true);
		} else if (cm.code.matches("400")) {
			initGame();
		} else if (cm.code.matches("501")) {
			String enemyInformation[] = cm.data.split(" "); // p1 p2 curx cury subx suby ����
			enemyCurP1 = Integer.parseInt(enemyInformation[0]);
			enemyCurP2 = Integer.parseInt(enemyInformation[1]);
			enemyCurX = Integer.parseInt(enemyInformation[2]);
			enemyCurY = Integer.parseInt(enemyInformation[3]);
			enemySubX = Integer.parseInt(enemyInformation[4]);
			enemySubY = Integer.parseInt(enemyInformation[5]);
			if (enemyField[enemyCurY + 1][enemyCurX] != 0 || enemyCurY + 1 == enemySubY) { // �� �ѿ䵵 ���� Ȯ��
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
			String enemyInformation[] = cm.data.split(" "); // p1 p2 curx cury subx suby ����
			int enemyCurP1 = Integer.parseInt(enemyInformation[0]);
			int enemyCurP2 = Integer.parseInt(enemyInformation[1]);
			int enemyCurX = Integer.parseInt(enemyInformation[2]);
			int enemyCurY = Integer.parseInt(enemyInformation[3]);
			int enemySubX = Integer.parseInt(enemyInformation[4]);
			int enemySubY = Integer.parseInt(enemyInformation[5]);
//				 if (enemyField[enemyCurY + 1][enemyCurX] != 0 || enemyCurY + 1 == enemySubY) { // �� �ѿ䵵 ���� Ȯ��
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

	public void initGame() { // ���� ���۽� ���� �ʱ� ����

		myScore = 0;
		enemyScore = 0;
		startX = 4; // �������� �κ�
		startY = 1; // �������� �κ�
		comboCount = 0;
		delay = 17; // 17 / 1000 = 58������
		puyoDelay = 2000;
		gameStatus = 1;
		dropPuyo();
		mainWork = new Thread(this);
		mainWork.start();
		// abc();
		textField.setText("���� ����!!");
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
					puyoDown(); // ���� ��� ������ �߻��ϸ� �������� Ȯ��?
				}
			};
			timer.scheduleAtFixedRate(task, 1000, puyoDelay); // �ι�° �Ķ���ʹ� ���� �������� �����ϴ� �Լ�, puyodelay�� ���� �ʿ䰡 ����

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
							checkGravity = 2; // �߷� ������ �� ������ ü�� �� ����
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
								enemyCheckGravity = 2; // �߷� ������ �� ������ ü�� �� ����
						}
					}
			};
			enemyGravityTimer.scheduleAtFixedRate(enemyGravityTask, puyoDelay, 150);
			

			while (loop) {

				preTime = System.currentTimeMillis();
				gameScreen.repaint();
				process(); // ��ü ���μ��� ó��
				keyProcess(); // Ű �Է� �޾Ƽ� ó��

				if (System.currentTimeMillis() - preTime < delay) { // �ð� ������ ���ߴ� �۾�, 10���� �ڹٰ��ӿ��� ������
					Thread.sleep(delay - System.currentTimeMillis() + preTime);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void process() { // ���� ���� ó��
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
		if (enemyField[enemyCurY + 1][enemyCurX] != 0 || enemyCurY + 1 == enemySubY) { // �� �ѿ䵵 ���� Ȯ��
			if (enemyField[enemySubY + 1][enemySubX] != 0 || enemySubY + 1 == enemyCurY) {
				enemyField[enemyCurY][enemyCurX] = enemyCurP1;
				enemyField[enemySubY][enemySubX] = enemyCurP2;
				return true;
			}
		}
		return false;
	}

	public boolean checkDropDone() { // �������̴� �ѿ䰡 ������� Ȯ��(�ٴڿ� ��Ҵ���)

		if (enemyField[enemyCurY + 1][enemyCurX] != 0 || enemyCurY + 1 == enemySubY) { // �� �ѿ䵵 ���� Ȯ��
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

	public void checkChainRule() { // ��� �ʵ带 Ȯ���ϸ� �湮���� �ʰ�, �ѿ䰡 �ִ°�� �ı���������
		// ����ؼ� ������ �����߻� ����, �ѿ䰡 ���ϵ�(�����ؼ��̴�, �ı��Ǽ��̴�) ��쿡�� ����
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
				} // �湮, �ѿ�����Ȯ��
			} // y for ��
		} // x for��
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
				} // �湮, �ѿ�����Ȯ��
			} // y for ��
		} // x for��
	}

	public boolean puyoDestroy(int x, int y, int puyo_type) { // �ı���������, ���� �ѿ���� Ȯ���ϸ� �ڽŰ� �����ϸ� ī��Ʈ ����

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

	public boolean enemyPuyoDestroy(int x, int y, int puyo_type) { // �ı���������, ���� �ѿ���� Ȯ���ϸ� �ڽŰ� �����ϸ� ī��Ʈ ����

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

	public void realDestroyPuyo() { // ���� �ѿ���� �ʵ忡�� ����, ���� ���� �ѿ���� ���??

		for (int i = 0; i < visitedX.size(); i++) {
			myField[visitedX.get(i)][visitedY.get(i)] = 0;
		}

		myScore += destroyCount * 10;
		// System.out.println(String.format("my score : %d", myScore)); // ������
		clearVisitedField();
		checkGravity = 1;
	}
	
	public void enemyRealDestroyPuyo() { // ���� �ѿ���� �ʵ忡�� ����, ���� ���� �ѿ���� ���??

		for (int i = 0; i < enemyVisitedX.size(); i++) {
			enemyField[enemyVisitedX.get(i)][enemyVisitedY.get(i)] = 0;
		}
		// System.out.println(String.format("my score : %d", myScore)); // ������
		enemyClearVisitedField();
		enemyCheckGravity = 1;
	}

	public void clearVisitedField() { // �湮��� �ʱ�ȭ
		for (int i = 0; i < 13; i++) {
			for (int j = 1; j < 7; j++) {
				visited[i][j] = false;
			}
		}
	}

	public void enemyClearVisitedField() { // �湮��� �ʱ�ȭ
		for (int i = 0; i < 13; i++) {
			for (int j = 1; j < 7; j++) {
				enemyVisited[i][j] = false;
			}
		}
	}

	public void gravity() { // �Ʒ��� �ִ� �ѿ䰡 �ı��� �� ���� �ִ� �ѿ���� ����ִ� �������� ������

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
			} else { // ������ ����� ������ ������ ��������
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

	public void checkGameOver() { // ���� ���� ó��(�ѿ䰡 õ���� ħ)

		// gameStatus = 0;
		// loop = false;
	}

	// private void createPuyo(Graphics g) { // ���� �ѿ� ����(���� ������)
	// ���߿� ���� �ϴ� �Ʒ��ɷ� �ٷ� ������ġ�� �����ϱ�
	// }

	public void dropPuyo() { // ���� �ѿ� ���(�����¿��� ������ ����)
		curP1 = (int) (Math.random() * 5 + 1);
		curP2 = (int) (Math.random() * 5 + 1);
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

	public void keyProcess() { // �ѿ� �̵�
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
			try { // Ű�� �ʹ� ������ ������ �ȵ� ������
				Thread.sleep(100); // �ణ 0.05�ʴ� �ʹ� ª�� ����?
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
} // ��ü Ŭ���� ��
