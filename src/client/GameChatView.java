package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.imageio.ImageIO;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import gameMsg.*;

public class GameChatView extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtInput;
	private String UserName;
	private JButton btnSend;
	private Socket socket; // 연결소켓

	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	private JLabel lbluserName;
	private JTextPane textArea;

	// 임시로 연결
	private GameScreenView game;

	/**
	 * Create the frame.
	 */
	public GameChatView(String userName, String ip_addr, String port_no) {
		
		game = new GameScreenView();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 394, 630);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 352, 471);
		contentPane.add(scrollPane);

		textArea = new JTextPane();
		textArea.setEditable(true);
		textArea.setFont(new Font("굴림", Font.PLAIN, 14));
		scrollPane.setViewportView(textArea);

		txtInput = new JTextField();
		txtInput.setBounds(74, 489, 209, 40);
		contentPane.add(txtInput);
		txtInput.setColumns(10);

		btnSend = new JButton("Send");
		btnSend.setFont(new Font("굴림", Font.PLAIN, 14));
		btnSend.setBounds(295, 489, 69, 40);
		contentPane.add(btnSend);

		lbluserName = new JLabel("Name");
		lbluserName.setBorder(new LineBorder(new Color(0, 0, 0)));
		lbluserName.setBackground(Color.WHITE);
		lbluserName.setFont(new Font("굴림", Font.BOLD, 14));
		lbluserName.setHorizontalAlignment(SwingConstants.CENTER);
		lbluserName.setBounds(12, 539, 62, 40);
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
		btnNewButton.setBounds(295, 539, 69, 40);
		contentPane.add(btnNewButton);

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
			// TODO Auto-generated catch block
			e.printStackTrace();
			AppendText("connect error");
		}

	}

	public GameMsg ReadGameMsg() {
		Object obj = null;
		GameMsg cm = new GameMsg("", "", "");
		// Android와 호환성을 위해 각각의 Field를 따로따로 읽는다.

			try {
				obj = ois.readObject();
				cm = (GameMsg) obj;
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				AppendText("ReadGameMsg Error");
				e.printStackTrace();
				try {
					oos.close();
					socket.close();
					ois.close();
					socket = null;
					return null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					try {
						oos.close();
						socket.close();
						ois.close();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
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
				msg = String.format("[%s] %s", cm.userName, cm.data);
				AppendText(msg);
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

	// ImageIcon icon1 = new ImageIcon("src/icon1.jpg");

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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// textArea.append("메세지 송신 에러!!\n");
			// System.exit(0);
		}
	}
}
