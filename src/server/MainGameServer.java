package server;

import gameMsg.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

public class MainGameServer extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextArea textArea;
	private JTextField txtPortNumber;
	private ServerSocket socket;
	private Socket client_socket;
	public Vector<UserService> userVec = new Vector<UserService>();
	public HashMap<String, String> userStatus = new HashMap<String, String>();
	public Vector<String> userList = new Vector<String>();
	public Vector<String> roomList = new Vector<String>();
	public HashMap<String, Integer> roomMap = new HashMap<String, Integer>();
	public HashMap<String, String> userLocation = new HashMap<String, String>();
	public HashMap<String, String> whoRoomMade = new HashMap<String, String>();

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
				btnServerStart.setEnabled(false);
				txtPortNumber.setEnabled(false);
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 356, 300, 35);
		contentPane.add(btnServerStart);
	}

	class AcceptServer extends Thread {
		public void run() {
			while (true) {
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept();

					AppendText("새로운 사용자 from " + client_socket);

					UserService new_user = new UserService(client_socket);
					userVec.add(new_user);
					new_user.start();
					userStatus.put(new_user.userName, "O");
					AppendText("현재 사용자의 수: " + userVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
				}
			}
		}
	}

	public void AppendText(String str) {
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public void AppendObject(GameMsg msg) {
		textArea.append("code = " + msg.code + "\n");
		textArea.append("id = " + msg.userName + "\n");
		textArea.append("data = " + msg.data + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	class UserService extends Thread {
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		private Socket client_socket;
		private Vector<UserService> user_vc;
		public String userName = "";

		public UserService(Socket client_socket) {
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
			userVec.removeElement(this);
			this.client_socket = null;
			AppendText("사용자 " + "[" + userName + "] 가 퇴장하였습니다. 남은 사용자 수: " + userVec.size());
		}

		public void WriteAllObject(GameMsg obj) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				user.WriteGameMsg(obj);
			}
			System.out.println(user_vc.toString());
		}

		public void WriteOthers(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this)
					user.WriteOne(str, "200");
			}
		}

		public void WriteOne(String msg, String code) {
			GameMsg obcm = new GameMsg("SERVER", code, msg);
			WriteGameMsg(obcm);
		}

		public void WriteGameMsg(GameMsg obj) {
			try {
				oos.writeObject(obj);
			} catch (IOException e) {
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
			try {
				obj = ois.readObject();
				if(obj instanceof GameMsg)
					cm = (GameMsg)obj;
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
			while (true) {
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
					AppendText("새로운 사용자 " + userName + " 입장");
					String msg = "";
					for (String key : whoRoomMade.keySet()) {
						msg += key + " " + whoRoomMade.get(key) + " ";
					}
					WriteGameMsg(new GameMsg("SERVER", "300", msg));
				} else if (cm.code.matches("200")) {
					String msg = String.format("[%s] %s", cm.userName, cm.data);
					AppendText(msg);
					for (int i = 0; i < user_vc.size(); i++) {
						UserService us = user_vc.get(i);
						if (us.userName.equals(userStatus.get(cm.userName))) {
							us.WriteGameMsg(cm);
							WriteGameMsg(cm);
						}
					}
				} else if (cm.code.matches("300")) {
					if (!(roomMap.containsKey(cm.data))) {
						roomMap.put(cm.data.split(" ")[0], 1);
						whoRoomMade.put(cm.data.split(" ")[0], cm.userName);
						AppendText(String.format("방 생성: %s", cm.data));
					}
					WriteAllObject(cm);
				} else if (cm.code.matches("304")) {
					String msg = "";
					for (String key : whoRoomMade.keySet()) {
						msg += key + " " + whoRoomMade.get(key) + " ";
					}
					WriteAllObject(new GameMsg("SERVER", "300", msg));
				} else if (cm.code.matches("302")) {
					if (roomMap.get(cm.data) == 2) {
						roomMap.put(cm.data, 1);
						userStatus.remove(userStatus.get(cm.userName));
						userStatus.remove(cm.userName);
						WriteAllObject(cm);
					}
					if (roomMap.get(cm.data) == 0 || cm.userName.equals(whoRoomMade.get(cm.data))) {
						roomMap.remove(cm.data);
						whoRoomMade.remove(cm.data);
						WriteAllObject(new GameMsg("server", "305", cm.data));
					}
				} else if (cm.code.matches("501")) {
					for (int i = 0; i < user_vc.size(); i++) {
						UserService us = user_vc.get(i);
						if (us.userName.equals(userStatus.get(cm.userName))) {
							us.WriteGameMsg(cm);
						}
					}
				} else if (cm.code.matches("502")) {
					for (int i = 0; i < user_vc.size(); i++) {
						UserService us = user_vc.get(i);
						if (us.userName.equals(userStatus.get(cm.userName))) {
							us.WriteGameMsg(cm);
						}
					}
				} else if (cm.code.matches("505")) {
					for (int i = 0; i < user_vc.size(); i++) {
						UserService us = user_vc.get(i);
						if (us.userName.equals(userStatus.get(cm.userName))) {
							us.WriteGameMsg(cm);
						}
					}
				}else if(cm.code.matches("506")){
					for (int i = 0; i < user_vc.size(); i++) {
						UserService us = user_vc.get(i);
						if (us.userName.equals(userStatus.get(cm.userName))) {
							us.WriteGameMsg(cm);
						}
					}
				}
				else if (cm.code.matches("900")) {
					Logout();
					break;
				} else if (cm.code.matches("301")) {
					if (roomMap.get(cm.data.split(" ")[0]) < 2) {
						String opp_user = cm.data.split(" ")[1];
						userStatus.put(opp_user, cm.userName);
						userStatus.put(cm.userName, opp_user);
						roomMap.put(cm.data.split(" ")[0], 2);
						WriteGameMsg(new GameMsg("server", "301", cm.data.split(" ")[0]));
						for (int i = 0; i < user_vc.size(); i++) {
							UserService us = user_vc.get(i);
							if (us.userName.equals(userStatus.get(cm.userName))) {
								us.WriteGameMsg(new GameMsg("SERVER", "400", ""));
								WriteGameMsg(new GameMsg("SERVER", "400", ""));
							}
						}
					}
				}
			} // while
		} // run
	}

}
