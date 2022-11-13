package client;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class GameScreenView extends JFrame {

	private JPanel contentPane;
	private Image mainBackGround;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new GameScreenView();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the main frame.
	 */
	public GameScreenView() {
		
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 활성화 시키면 게임 창 닫을 시 서버에서 로그아웃
		setBounds(100, 100, 640, 510);
		setVisible(true);
		setTitle("PuyoPuyo2!");
		setResizable(false);
		setLayout(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		try { // 배경화면 가져오기, 안되면 path 확인
			 mainBackGround = ImageIO.read(new File("./src/resource/mainBackGround.bmp"));
		} catch(IOException e) {
			JOptionPane.showMessageDialog(null,  "이미지 불러오기 실패");
			System.exit(0);
		}

		setContentPane(contentPane);
		contentPane.paint(getGraphics());
		
	}
	
	public void paint(Graphics g) {
		g.drawImage(mainBackGround, 0, 0, null);
	}

}
