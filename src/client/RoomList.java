package client;

import gameMsg.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RoomList extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private String UserName;
	private JButton btnSend;
	private Socket socket; // 연결소켓
	
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private JTextPane textArea;
	private JTextField txtInput;
	// 임시로 만든 버튼, 게임화면객체
	private JButton tempGameStartButton;
	private GameChatView game;
	
	/**
	 * Create the frame.
	 */
	public RoomList(String username, String ip_addr, String port_no) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 392, 510);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 62, 352, 340);
		contentPane.add(scrollPane);
		
		textArea = new JTextPane();
		textArea.setEditable(true);
		textArea.setFont(new Font("굴림", Font.PLAIN, 14));
		scrollPane.setViewportView(textArea);
		
		txtInput = new JTextField();
		txtInput.setBounds(20, 423, 200, 40);
		contentPane.add(txtInput);
		txtInput.setColumns(10);

		btnSend = new JButton("Send");
		btnSend.setBounds(240, 423, 60, 40);
		contentPane.add(btnSend);
		
		JLabel lblNewLabel = new JLabel("게임방");
		lblNewLabel.setBounds(76, 21, 91, 31);
		contentPane.add(lblNewLabel);
		lblNewLabel.setFont(new Font("굴림",Font.PLAIN,14));
		lblNewLabel.setForeground(Color.red);
		
		JLabel lblNewLabel_1 = new JLabel("방장");
		lblNewLabel_1.setBounds(231, 21, 76, 31);
		contentPane.add(lblNewLabel_1);
		lblNewLabel_1.setFont(new Font("굴림",Font.PLAIN,14));
		lblNewLabel_1.setForeground(Color.red);
		ImageIcon image1=new ImageIcon("src/red.jpg");
		JLabel lblNewLabel_2 = new JLabel();
		lblNewLabel_2.setIcon(image1);
		lblNewLabel_2.setBounds(12, 21, 56, 31);
		contentPane.add(lblNewLabel_2);
		
		/**
		 * 임시로 만든 게임 화면 입장 버튼
		 */
		
		tempGameStartButton = new JButton("임시입장");
		tempGameStartButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				game = new GameChatView("yujin", "127.0.0.1", "30000");
			}
		});
		
		tempGameStartButton.setBounds(300, 423, 60, 40);
		contentPane.add(tempGameStartButton);
		
		
		
		
		setVisible(true);
	
		AppendText("User " + username + " connecting " + ip_addr + " " + port_no);
		UserName = username;
		
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			
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
	
	// Server Message를 수신해서 화면에 표시
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {
				try {
					Object obcm=null;
					String msg=null;
					GameMsg cm;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof GameMsg) {
						cm = (GameMsg) obcm;
						msg = String.format("[%s] %s", cm.userName, cm.data);
					} else
						continue;
					AppendText(msg);
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
						ois.close();
						oos.close();
						socket.close();

						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝

				
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
					msg = txtInput.getText();
					SendMessage(msg);
					txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
					txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
				}
			}
		}

	// 화면에 출력
	public void AppendText(String msg) {
		//textArea.append(msg + "\n");
		int len = textArea.getDocument().getLength(); // same value as
        textArea.setCaretPosition(len); // place caret at the end (with no selection)
 		textArea.replaceSelection(msg + "\n"); // there is no selection, so inserts at caret
 	}

	// Server에게 network으로 전송
	public void SendMessage(String msg) {
		try {
			GameMsg obcm=new GameMsg(UserName, "200", msg);
			oos.writeObject(obcm);
		} catch (IOException e) {
			AppendText("dos.write() error");
			try {
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	public void SendChatMsg(GameMsg obj) {
		try {
			oos.writeObject(obj.code);
			oos.writeObject(obj.userName);
			oos.writeObject(obj.data);
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
