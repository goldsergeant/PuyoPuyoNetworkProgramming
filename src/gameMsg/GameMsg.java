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
	 * 100: 로그인
	 * 300: 게임방 생성
	 * 301: 게임방 참가
	 * 302: 게임방 나가기
	 * 304: 게임방 목록 요청
	 * 305: 게임방 제거
	 * 400: 게임 시작
	 * 500: 뿌요 생성
	 * 501: 뿌요 이동
	 * 502: 뿌요 위치 저장
	 * 503: 공격 뿌요 생성
	 * 504: 공격 뿌요 드랍
	 * 505: 내 공격 뿌요 필드 값
	 * 600: 콤보 효과음
	 * 900: 로그아웃
	 */
}
