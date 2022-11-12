package client;
// JavaChatClientView.java
// 실질적인 채팅 창
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;

public class RoomList extends JFrame {
	private JPanel contentPane;
	private String UserName;
	private JButton btnSend;
	private static final  int BUF_LEN = 128; //  Windows 처럼 BUF_LEN 을 정의
	private Socket socket; // 연결소켓
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	//private JTextArea textArea;
	private JTextPane textArea;
	
	
	// 임시로 만든 버튼, 게임화면객체
	private JButton tempGameStartButton;
	private MainGameChatView game;
	
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
		textArea.setFont(new Font("굴림체", Font.PLAIN, 14));
		scrollPane.setViewportView(textArea);

		btnSend = new JButton("Send");
		btnSend.setBounds(143, 423, 76, 40);
		contentPane.add(btnSend);
		
		JLabel lblNewLabel = new JLabel("게임방");
		lblNewLabel.setBounds(76, 21, 91, 31);
		contentPane.add(lblNewLabel);
		lblNewLabel.setFont(new Font("휴먼둥근헤드라인",Font.PLAIN,14));
		lblNewLabel.setForeground(Color.red);
		
		JLabel lblNewLabel_1 = new JLabel("방장");
		lblNewLabel_1.setBounds(231, 21, 76, 31);
		contentPane.add(lblNewLabel_1);
		lblNewLabel_1.setFont(new Font("휴먼둥근헤드라인",Font.PLAIN,14));
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
				
				game = new MainGameChatView("yujin", "127.0.0.1", "30000");
			}
		});
		
		tempGameStartButton.setBounds(250, 420, 80, 40);
		contentPane.add(tempGameStartButton);
		
		
		
		
		setVisible(true);
	
		AppendText("User " + username + " connecting " + ip_addr + " " + port_no);
		UserName = username;
		
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			is = socket.getInputStream();
			dis = new DataInputStream(is);
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
			
			SendMessage("/login " + UserName);
			ListenNetwork net = new ListenNetwork();
			net.start();
			Myaction action=new Myaction();
			btnSend.addActionListener(action);
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AppendText("connect error");
		}

	}
	// Server Message를 수신해서 화면에 표시
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {
				try {
					// String msg = dis.readUTF();
					byte[] b = new byte[BUF_LEN];
					int ret;
					ret = dis.read(b);
					if (ret < 0) {
						AppendText("dis.read() < 0 error");
						try {
							dos.close();
							dis.close();
							socket.close();
							break;
						} catch (Exception ee) {
							break;
						}// catch문 끝
					}
					String	msg = new String(b, "euc-kr");
					msg = msg.trim(); // 앞뒤 blank NULL, \n 모두 제거
					AppendText(msg); // server 화면에 출력
				} catch (IOException e) {
					AppendText("dis.read() error");
					try {
						dos.close();
						dis.close();
						socket.close();
						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝
				
			}
		}
	}
	public void AppendIcon(ImageIcon icon) {
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len); // place caret at the end (with no selection)
		textArea.insertIcon(icon);	
	}
	// 화면에 출력
	public void AppendText(String msg) {
		//textArea.append(msg + "\n");
		int len = textArea.getDocument().getLength(); // same value as
        textArea.setCaretPosition(len); // place caret at the end (with no selection)
 		textArea.replaceSelection(msg + "\n"); // there is no selection, so inserts at caret
 	}

	// Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
	public byte[] MakePacket(String msg) {
		byte[] packet = new byte[BUF_LEN];
		byte[] bb = null;
		int i;
		for (i = 0; i < BUF_LEN; i++)
			packet[i] = 0;
		try {
			bb = msg.getBytes("euc-kr");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		for (i = 0; i < bb.length; i++)
			packet[i] = bb[i];
		return packet;
	}

	// Server에게 network으로 전송
	public void SendMessage(String msg) {
		try {
			// dos.writeUTF(msg);
			byte[] bb;
			bb = MakePacket(msg);
			dos.write(bb, 0, bb.length);
		} catch (IOException e) {
			AppendText("dos.write() error");
			try {
				dos.close();
				dis.close();
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}
}
class Myaction implements ActionListener{

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
