package tcp;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {

	List<String> userList;
	String roomOwner; // ����
	String roomName; // �� �̸�
	int roomNumber; //�� ��ȣ
	int ownerFish, clientFish;	//�� ������ ���� ����� ��
	int fishEa = 10;
	
	Boolean full = false;	//���������� ����

	public GameRoom(Boolean full) { // �ƹ��� ���� ���� ������ ��
		userList = new ArrayList<String>();
		this.full = full;
	}

	public GameRoom(Boolean full, String _user) { // ������ ���� ���鶧
		userList = new ArrayList<String>();
		userList.add(_user); // ������ �߰���Ų ��
		this.roomOwner = _user; // ������ ������ �����.
		this.full = full;
	}

	public GameRoom(List<String> _userList) { // ���� ����Ʈ�� ���� ������
		this.userList = _userList; // ��������Ʈ ����
		this.roomOwner = userList.get(0); // ù��° ������ �������� ����
	}

	public void AddUser(String userNickName) {
		userList.add(userNickName);
	}

//	public void ExitRoom(String _user) {
//		userList.remove(_user);
//
//		if (userList.size() < 1) { // ��� �ο��� �� ���� �����ٸ�
//			RoomManager.RemoveRoom(this); // �� ���� �����Ѵ�.
//			return;
//		}
//
//		if (userList.size() < 2) { // �濡 ���� �ο��� 1�� ���϶�
//			this.roomOwner = userList.get(0); // ����Ʈ�� ù��° ������ ������ �ȴ�.
//			return;
//		}
//
//	}

	// ���� ����
	
	@SuppressWarnings("unused")
	public void Broadcast(byte[] data) {
		for (String user : userList) { // �濡 ���� ������ ����ŭ �ݺ�
			// �� �������� �����͸� �����ϴ� �޼��� ȣ��~
			// ex) user.SendData(data);
			
//			try {
//				user.sock.getOutputStream().write(data); // �̷������� ����Ʈ�迭�� ������.
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}

	public void SetOwner(String _user) {
		this.roomOwner = _user; // Ư�� ����ڸ� �������� �����Ѵ�.
	}

	public void SetRoomName(String _name) { // �� �̸��� ����
		this.roomName = _name;
	}
	
	public String GetUserByNickName(String _nickName){ // �г����� ���ؼ� �濡 ���� ������ ������
		
		for(String user : userList){
			if(user.equals(_nickName)){
				return user; // ������ ã�Ҵٸ�
			}
		}
		return null; // ã�� ������ ���ٸ�
	}

	public String GetRoomName() { // �� �̸��� ������
		return roomName;
	}
	
	public int GetRoomNumber() { // �� ��ȣ�� ������
		return roomNumber;
	}

	public int GetUserSize() { // ������ ���� ����
		return userList.size();
	}

	public String GetOwner() { // ������ ����
		return roomOwner;
	}
}