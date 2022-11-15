package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class GameView extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;
	public JPanel contentPane;
	public JTextField txtInput;
	public String UserName;
	public JButton btnSend;
	public Socket socket; // 연결소켓
	public ObjectInputStream ois;
	public ObjectOutputStream oos;
	public JLabel lbluserName;
	public JTextPane textArea;
	public JScrollPane scrollPane;
	public GameScreen gameScreen; // 실제 게임이 진행되는 화면
	public int status; // 게임의 상태
	
	public final int gameWidth = 640;
	public final int gameHeight = 480;
	Thread mainWork; // 스레드 객체

	/**
	 * Create the frame.
	 */
	public GameView(String userName, String ip_addr, String port_no) {
		super("Puyo Puyo2!!");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 980, 560);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		//getContentPane().setBackground(new java.awt.Color(170, 0, 0));
		
		gameScreen = new GameScreen(this);
		gameScreen.setBounds(10, 10, gameWidth, gameHeight);
		contentPane.add(gameScreen);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(664, 19, 285, 381);
		contentPane.add(scrollPane);
		
		textArea = new JTextPane();
		textArea.setEditable(true);
		textArea.setFont(new Font("굴림", Font.PLAIN, 14));
		textArea.setFocusable(false);
		scrollPane.setViewportView(textArea);

		txtInput = new JTextField();
		txtInput.setBounds(664, 410, 194, 40);
		contentPane.add(txtInput);
		txtInput.setColumns(10);
		
		btnSend = new JButton("Send");
		btnSend.setFont(new Font("굴림", Font.PLAIN, 14));
		btnSend.setBounds(880, 410, 69, 40);
		contentPane.add(btnSend);

		lbluserName = new JLabel("Name");
		lbluserName.setBorder(new LineBorder(new Color(0, 0, 0)));
		lbluserName.setBackground(Color.WHITE);
		lbluserName.setFont(new Font("굴림", Font.BOLD, 14));
		lbluserName.setHorizontalAlignment(SwingConstants.CENTER);
		lbluserName.setBounds(664, 460, 62, 40);
		contentPane.add(lbluserName);
		setVisible(true);

		AppendText("User " + userName + " connecting " + ip_addr + " " + port_no);
		UserName = userName;
		lbluserName.setText(userName);

		JButton btnNewButton = new JButton("종 료");
		btnNewButton.setFont(new Font("굴림", Font.PLAIN, 14));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GameMsg msg = new GameMsg(UserName, "400", "Bye");
				SendGameMsg(msg);
				System.exit(0);
			}
		});
		btnNewButton.setBounds(880, 459, 69, 40);
		contentPane.add(btnNewButton);
		

		InitGame();
		

		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));

			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());

			GameMsg obcm = new GameMsg(userName, "100", "Hello");
			SendGameMsg(obcm);

			ListenNetwork net = new ListenNetwork();
			net.start();
			TextSendAction action = new TextSendAction();
			btnSend.addActionListener(action);
			txtInput.addActionListener(action);
			txtInput.requestFocus();

		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			AppendText("connect error");
		}
	}
	
	public void run() {
		try {
			while (true) {
				gameScreen.repaint();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void InitGame() {
		
		status = 0;
		
		mainWork = new Thread(this);
		mainWork.start();

		gameScreen.backGround = GrapImage("./src/resource/backGround.bmp");
		gameScreen.repaint();
	}
	
	public Image GrapImage(String fileUrl) {
		Image img;
		Toolkit tk = Toolkit.getDefaultToolkit();
		img = tk.getImage(fileUrl);
		try {
			MediaTracker mt = new MediaTracker(this);
			mt.addImage(img, 0);
			mt.waitForID(0);
		} catch (Exception ee) {
			ee.printStackTrace();
			return null;
		}
		return img;
	}

	public GameMsg ReadGameMsg() {
		Object obj = null;
		GameMsg cm = null;

			try {
				obj = ois.readObject();
				cm = (GameMsg) obj;
			} catch (ClassNotFoundException | IOException e) {
				AppendText("ReadGameMsg Error");
				e.printStackTrace();
				try {
					oos.close();
					socket.close();
					ois.close();
					socket = null;
					return null;
				} catch (IOException e1) {
					e1.printStackTrace();
					try {
						oos.close();
						socket.close();
						ois.close();
					} catch (IOException e2) {
						e2.printStackTrace();
					}

					socket = null;
					return null;
				}

			}


		return cm;
	}
	// Server Message를 수신해서 화면에 표시
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {
				GameMsg cm = ReadGameMsg();
				if (cm==null)
					break;
				if (socket == null)
					break;
				String msg;
				if(cm.userName.equals(UserName)) {
					msg = String.format("%s", cm.data);
					myAppendText(msg);
				}else {
				msg = String.format("[%s] %s", cm.userName, cm.data);
				AppendText(msg);
			}
			}
		}
	}

	// keyboard enter key 치면 서버로 전송
	class TextSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String msg = null;
				// msg = String.format("[%s] %s\n", userName, txtInput.getText());
				msg = txtInput.getText();
				SendMessage(msg);
				txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
				if (msg.contains("/exit")) // 종료 처리
					System.exit(0);
			}
		}
	}

	public void AppendIcon(ImageIcon icon) {
		int len = textArea.getDocument().getLength();
		// 끝으로 이동
		textArea.setCaretPosition(len);
		textArea.insertIcon(icon);
	}

	// 화면에 출력
	public void AppendText(String msg) {
		// textArea.append(msg + "\n");
		// AppendIcon(icon1);
		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
		int len = textArea.getDocument().getLength();
		// 끝으로 이동
		textArea.setCaretPosition(len);
		textArea.replaceSelection(msg + "\n");
	}
	
	public void myAppendText(String msg) {
		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
		int len = textArea.getDocument().getLength();
		// 끝으로 이동
		textArea.setCaretPosition(len);
		textArea.replaceSelection(msg + "\n");
		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet right = new SimpleAttributeSet();
		StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
		doc.setParagraphAttributes(len, doc.getLength(), right, false);
		len=textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
		doc = textArea.getStyledDocument();
		right = new SimpleAttributeSet();
		StyleConstants.setAlignment(right, StyleConstants.ALIGN_LEFT);
		doc.setParagraphAttributes(len, doc.getLength(), right, false);
	}


	// Server에게 network으로 전송
	public void SendMessage(String msg) {
		GameMsg obcm = new GameMsg(UserName, "200", msg);
		SendGameMsg(obcm);
	}

	// 하나의 Message 보내는 함수
	// Android와 호환성을 위해 code, userName, data 모드 각각 전송한다.
	public void SendGameMsg(GameMsg obj) {
		try {
			oos.writeObject(obj);
			oos.flush();
		} catch (IOException e) {
			AppendText("SendGameMsg Error");
			e.printStackTrace();
			try {
				oos.close();
				socket.close();
				ois.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}

// 실제 게임화면을 보여주는 캔버스
class GameScreen extends Canvas {
	
	private static final long serialVersionUID = 1L;
	public Image backGround;
	public GameView main;
	public Image doubleBuffer; // 더블버퍼링용 백버퍼
	public Graphics gc; // 더블버퍼링용 그래픽 context
	
	
	public GameScreen(GameView main) {
		this.main = main;
	}
	
	public void paint(Graphics g) {
		if (gc == null) {
			doubleBuffer = createImage(main.gameWidth, main.gameHeight);
			if (doubleBuffer == null) System.out.println("offScreen buffer create error");
			else gc = doubleBuffer.getGraphics();
			return;
		}
		update(g);
	}
	
	public void update(Graphics g) {
		// 화면 블링크를 방지하기 위해, paint에서 바로 화면을 그리지 않고 update method 호출
		if (gc == null) return;
		DoublePaint();
		g.drawImage(doubleBuffer, 0, 0, this);
	}
	
	public void DoublePaint() {
		// 실제 그리는 동작 수행
		switch(main.status) {
		case 0:
			DrawBackGround();
			break;
		case 1:
			DrawBackGround();
			break;
		case 2:
			DrawBackGround();
			break;
		case 3:
			DrawBackGround();
			break;
		case 4:
			DrawBackGround();
			break;
		default:
			DrawBackGround();
			break;
		}
		
	}
	
	public void DrawBackGround() {
		gc.drawImage(backGround, 0, 0, this);
	}
}