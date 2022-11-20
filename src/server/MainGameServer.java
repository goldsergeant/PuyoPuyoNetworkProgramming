package server;

import gameMsg.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.NonWritableChannelException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MainGameServer extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextArea textArea;
	private JTextField txtPortNumber;
	private ServerSocket socket; // 서버소켓
	private Socket client_socket; // accept() 에서 생성된 client 소켓
	public Vector<UserService> userVec = new Vector<UserService>(); // 연결된 사용자를 저장할 벡터
	public HashMap<String,String>userStatus=new HashMap<String,String>();
	public Vector<String> userList = new Vector<String>(); // 회원가입자 목록
	public Vector<String> roomList = new Vector<String>(); // 게임방 목록
	public HashMap<String, Integer> roomMap = new HashMap<String, Integer>(); // 게임방마다 인원 저장
	public HashMap<String, String> userLocation = new HashMap<String, String>(); // 유저마다 현재 위치(로비, 게임방)저장
	public HashMap<String, String> whoRoomMade=new HashMap<String,String>();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainGameServer frame = new MainGameServer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainGameServer() {
		
		/**
		 * 유저 목록은 DB를 사용하지 않고 서버에서 리스트로 관리
		 */
		userList.add("yujin");
		userList.add("leeso");
		userList.add("hansung");
		userList.add("bugi");
		// 위에 등록된 회원만 접속 가능
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 440);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 298);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(13, 318, 87, 26);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setText("30000");
		txtPortNumber.setBounds(112, 318, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);

		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					e1.printStackTrace();
				}
				AppendText("Game Server Running..");
				btnServerStart.setText("Game Server Running..");
				btnServerStart.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
				txtPortNumber.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 356, 300, 35);
		contentPane.add(btnServerStart);
	}

	// 새로운 참가자 accept() 하고 user thread를 새로 생성한다.
	class AcceptServer extends Thread {
		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
					
					AppendText("새로운 참가자 from " + client_socket);
					// User 당 하나씩 Thread 생성
					UserService new_user = new UserService(client_socket);
					userVec.add(new_user); // 새로운 참가자 배열에 추가
					new_user.start(); // 만든 객체의 스레드 실행
					userStatus.put(new_user.userName,"O" );
					AppendText("현재 참가자 수 " + userVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
				}
			}
		}
	}

	public void AppendText(String str) {
		// textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public void AppendObject(GameMsg msg) {
		// textArea.append("사용자로부터 들어온 object : " + str+"\n");
		textArea.append("code = " + msg.code + "\n");
		textArea.append("id = " + msg.userName + "\n");
		textArea.append("data = " + msg.data + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	// User 당 생성되는 Thread
	// Read One 에서 대기 -> Write All
	class UserService extends Thread {
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		private Socket client_socket;
		private Vector<UserService> user_vc;
		public String userName = "";

		public UserService(Socket client_socket) {
			// 매개변수로 넘어온 자료 저장
			this.client_socket = client_socket;
			this.user_vc = userVec;
			try {
				oos = new ObjectOutputStream(client_socket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(client_socket.getInputStream());
			} catch (Exception e) {
				AppendText("userService error");
			}
		}

		public void Logout() {
			userVec.removeElement(this); // Logout한 현재 객체를 벡터에서 지운다
			this.client_socket = null;
			AppendText("사용자 " + "[" + userName + "] 퇴장. 현재 참가자 수 " + userVec.size());
		}

		// 모든 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
		public void WriteAll(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				user.WriteOne(str, "200");
			}
		}
		// 모든 User들에게 Object를 방송.
		public void WriteAllObject(GameMsg obj) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				user.WriteGameMsg(obj);
			}
		}

		// 나를 제외한 User들에게 방송. 각각의 UserService Thread의 WriteOne() 을 호출한다.
		public void WriteOthers(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this)
					user.WriteOne(str, "200");
			}
		}

		// UserService Thread가 담당하는 Client 에게 1:1 전송
		public void WriteOne(String msg, String code) {
			GameMsg obcm = new GameMsg("SERVER", code, msg);
			WriteGameMsg(obcm);
		}
		
		//
		public void WriteGameMsg(GameMsg obj) {
			try {
			    oos.writeObject(obj);
			} 
			catch (IOException e) {
				AppendText("oos.writeObject(ob) error");		
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;				
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Logout();
			
			}
		}
		
		public GameMsg ReadGameMsg() {
			Object obj = null;
			GameMsg cm = null;
			// Android와 호환성을 위해 각각의 Field를 따로따로 읽는다.
			try {
				obj = ois.readObject();
				cm=(GameMsg) obj;
			} catch (ClassNotFoundException e) {
				Logout();
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				Logout();
				return null;
			}
			return cm;
		}
		
		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				GameMsg cm = null; 
				if (client_socket == null)
					break;
				cm = ReadGameMsg();
				if (cm == null)
					break;
				if (cm.code.length() == 0)
					break;
				AppendObject(cm);
				if (cm.code.matches("100")) {
					userName = cm.userName;
					AppendText("새로운 참가자 " + userName + " 입장.");
				} else if (cm.code.matches("200")) {
					String msg = String.format("[%s] %s", cm.userName, cm.data);
					AppendText(msg); // server 화면에 출력
					for(int i=0;i<user_vc.size();i++) {
						UserService us=user_vc.get(i);
						if(us.userName.equals(userStatus.get(cm.userName))) {
							us.WriteGameMsg(cm);
							WriteGameMsg(cm);
						}
					}
				} else if (cm.code.matches("300")) {
					if (!(roomMap.containsKey(cm.data))) {
						roomMap.put(cm.data.split(" ")[0], 1);
						whoRoomMade.put(cm.data.split(" ")[0],cm.userName);
						roomList.add(cm.data);
						userLocation.put(cm.userName, cm.data);
						AppendText(String.format("방 생성: %s %s", cm.data,cm.userName));
						WriteAllObject(cm);
					}
				} else if (cm.code.matches("304")) {
					for (int i = 0; i < roomList.size(); i++) {
						WriteAllObject(new GameMsg("SERVER", "300", roomList.get(i)));
					}
				}else if(cm.code.matches("302")){
					if(roomMap.get(cm.data)==2) {
						roomMap.put(cm.data,1);
						userStatus.remove(userStatus.get(cm.userName));
						userStatus.remove(cm.userName);
						WriteAllObject(cm);
						}if(roomMap.get(cm.data)==0 ||cm.userName.equals(whoRoomMade.get(cm.data))) {
							roomMap.remove(cm.data);
							roomList.remove(cm.data);
							whoRoomMade.remove(cm.data);
							WriteAllObject(new GameMsg("server", "305", cm.data));
						}
				}else if(cm.code.matches("501")) {
					for(int i=0;i<user_vc.size();i++) {
						UserService us=user_vc.get(i);
						if(us.userName.equals(userStatus.get(cm.userName))) {
							us.WriteGameMsg(cm);
				
						}
					}
				}
				else if (cm.code.matches("900")) { // logout message 처리
					Logout();
					break;
				}else if(cm.code.matches("301")){
					if(roomMap.get(cm.data.split(" ")[0])<2) {
					String opp_user=cm.data.split(" ")[1];
					userStatus.put(opp_user, cm.userName);
					userStatus.put(cm.userName, opp_user);
					roomMap.put(cm.data.split(" ")[0],2);
					WriteGameMsg(new GameMsg("server", "301",cm.data.split(" ")[0]));
					}
				}
			} // while
		} // run
	}

}
