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
		
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); Ȱ��ȭ ��Ű�� ���� â ���� �� �������� �α׾ƿ�
		setBounds(100, 100, 640, 510);
		setVisible(true);
		setTitle("PuyoPuyo2!");
		setResizable(false);
		setLayout(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		try { // ���ȭ�� ��������, �ȵǸ� path Ȯ��
			 mainBackGround = ImageIO.read(new File("./src/resource/mainBackGround.bmp"));
		} catch(IOException e) {
			JOptionPane.showMessageDialog(null,  "�̹��� �ҷ����� ����");
			System.exit(0);
		}

		setContentPane(contentPane);
		contentPane.paint(getGraphics());
		
	}
	
	public void paint(Graphics g) {
		g.drawImage(mainBackGround, 0, 0, null);
	}

}
