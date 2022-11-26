package client;

import gameMsg.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RoomList extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private String UserName;
	private JButton btnCreate;
	private Socket socket; // 연결소켓
	
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private JTextField txtInput;
	
	// 임시로 만든 버튼, 게임화면객체
	private JButton tempGameStartButton;
	private JButton tempRefreshButton;
	
	private JList roomListView;
	private Vector<String> roomList;
	private String ip_addr;
	private String port_no;
	private RoomList thisRoomList;
	private GameView view;
	/**
	 * Create the frame.
	 */
	public RoomList(String username, String ip_addr, String port_no) {
		this.ip_addr=ip_addr;
		this.port_no=port_no;
		this.thisRoomList=this;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 392, 510);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 62, 352, 340);
		contentPane.add(scrollPane);
		
		txtInput = new JTextField();
		txtInput.setBounds(20, 423, 180, 40);
		contentPane.add(txtInput);
		txtInput.setColumns(10);

		btnCreate = new JButton("방 생성");
		btnCreate.setBounds(210, 423, 80, 40);
		contentPane.add(btnCreate);
		
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
		
		roomList = new Vector();
		roomListView = new JList(roomList);
		roomListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(roomListView);
		DefaultListCellRenderer renderer = (DefaultListCellRenderer) roomListView.getCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		tempGameStartButton = new JButton("입장");
		tempGameStartButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				SendMessage(roomListView.getSelectedValue().toString(), "301");
			}
		});
		
		tempGameStartButton.setBounds(300, 423, 60, 40);
		contentPane.add(tempGameStartButton);
		
		
		
		//
		
		tempRefreshButton = new JButton("Re");
		tempRefreshButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				roomListView.updateUI();
				repaint();
			}
		});
		tempRefreshButton.setBounds(310, 21, 50, 30);
		contentPane.add(tempRefreshButton);
		//
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				SendMessage("", "900");
			}
		});
		
		setVisible(true);
		UserName = username;
		
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			
			ListenNetwork net = new ListenNetwork();
			net.start();
			SendMessage("Hello","100");
			RoomCreateAction action = new RoomCreateAction();
			btnCreate.addActionListener(action);
			txtInput.addActionListener(action);
			txtInput.requestFocus();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		//view.setFocusable(true);
		SendMessage("", "304");
	}
	
	// Server Message를 수신해서 화면에 표시
	class ListenNetwork extends Thread {
		public void run() {
			tempRefreshButton.doClick();
			while (true) {
				try {
					Object obcm=null;
					GameMsg cm;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof GameMsg) {
						cm = (GameMsg) obcm;
						if (cm.code.matches("300") || cm.code.matches("304")) {
							String arr[]=cm.data.split(" ");
							for(int i=0;i<arr.length-1;i+=2) {
								if(roomList.contains(arr[i]+" "+arr[i+1]))
									continue;
								else
									roomList.add(arr[i]+" "+arr[i+1]);
							}
							tempRefreshButton.doClick();
						} else if (cm.code.matches("200")) {
							view.readMessage(cm);
							view.requestFocus();
						}else if(cm.code.matches("301")) {
							view=new GameView(UserName, ip_addr, port_no,thisRoomList,cm.data);
							view.requestFocus();
							setVisible(false);
						}else if(cm.code.matches("305")) {
							for(int i=0;i<roomList.size();i++) {
								if(roomList.get(i).startsWith(cm.data)) {
									roomList.remove(i);
								}
							}
							if(view!=null) {
							view.readMessage(cm);
							view.gameScreen.requestFocus();
							}
							tempRefreshButton.doClick();
						}else if(cm.code.matches("400")){
							view.readMessage(cm);
						}else if(cm.code.matches("501")) {
							view.readMessage(cm);
							view.requestFocus();
						}else if(cm.code.matches("502")) {
							view.readMessage(cm);
						}else if(cm.code.matches("505")) {
							view.readMessage(cm);
						}else if(cm.code.matches("506")) {
							view.readMessage(cm);
						}
					} else
						continue;
				} catch (IOException e) {
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
	class RoomCreateAction implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == btnCreate || e.getSource() == txtInput) {
				String msg = null;
				msg = txtInput.getText()+" "+UserName;
				SendMessage(msg, "300");
				view=new GameView(UserName, ip_addr,port_no,thisRoomList,txtInput.getText());
				view.setFocusable(true);
				view.requestFocus();
				txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
				setVisible(false);
				
			}
		}
	}

	// Server에게 network으로 전송
	public void SendMessage(String msg, String code) {
		try {
			GameMsg obcm = new GameMsg(UserName, code, msg);
			oos.writeObject(obcm);
		} catch (IOException e) {
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
}
