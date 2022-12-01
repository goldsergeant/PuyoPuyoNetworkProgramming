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
	 * 100: �α���
	 * 300: ���ӹ� ����
	 * 301: ���ӹ� ����
	 * 302: ���ӹ� ������
	 * 304: ���ӹ� ��� ��û
	 * 305: ���ӹ� ����
	 * 400: ���� ����
	 * 500: �ѿ� ����
	 * 501: �ѿ� �̵�
	 * 502: �ѿ� ��ġ ����
	 * 505: �� ���� �ѿ� �ʵ� ��
	 * 600: �޺� ȿ����
	 * 900: �α׾ƿ�
	 */
}
