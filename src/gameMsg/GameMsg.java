package gameMsg;


import java.io.Serializable;

public class GameMsg implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public String code;
	public String userName;
	public String data;
	
	public GameMsg(String userName, String code, String data) {

		this.userName = userName;
		this.code = code;
		this.data = data;
	}

	/**
	 * protocol
	 * 100: ���� �α���
	 * 200: ä�ø޼���
	 * 300: ���ӹ� ����
	 * 301: ���ӹ� ������
	 * 400: ���ӽ���
	 * 401: ��������
	 * 500: �ѿ� ����
	 * 501: �ѿ� �̵�
	 * 502: �ѿ� �ı�(���� �ѿ� ����)
	 * 503: ���� �ѿ� ����(������)
	 * 504: ���� �ѿ� ���(����)
	 * 900: ���� �α׾ƿ�
	 */
}
