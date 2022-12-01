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
	 * 100: 서버 로그인
	 * 200: 채팅메세지 (방에서만)
	 * 300: 게임방 생성
	 * 301: 게임방 참가
	 * 302: 게임방 나가기 (두명 다 나가면 삭제)
	 * 304: 게임방 목록 요청
	 * 305: 게임방 제거
	 * 400: 게임시작
	 * 401: 게임종료
	 * 500: 뿌요 생성
	 * 501: 뿌요 이동
	 * 502: 뿌요 파괴(방해 뿌요 포함)
	 * 503: 공격 뿌요 생성(대기상태)
	 * 504: 공격 뿌요 드랍(공격)
	 * 505: 뿌요 중력
	 * 506: 잘린 뿌요
	 * 900: 서버 로그아웃
	 */
}
