package tcp;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomManager {

	List<GameRoom> roomList; // ���� ����Ʈ
//    HashMap<Boolean, GameRoom> roomMap;	//���� �� ������ȣ�� �ش� ���� ���� �ؽ��� ����

	public RoomManager(){
		roomList = new ArrayList<GameRoom>();
//		roomMap = new HashMap<Boolean, GameRoom>();
	}
	
//	public GameRoom CreateRoom(){ // ���� ���� ����(�� ��)
//		GameRoom room = new GameRoom();
//		roomList.add(room);
//		roomMap.put(2, room);
//		System.out.println("Room Created!");
//		return room;
//	}
	
	public GameRoom CreateRoom(String _owner){ // ������ ���� ������ �� ���(������ �������� ��)
		GameRoom room = new GameRoom(false, _owner);
		roomList.add(room);
		System.out.println("Room Created!");
		return room;
	}
	
	public GameRoom CreateRoom(GameRoom room){	// ���� ���� ����(�� ��)
		roomList.add(room);
//		roomMap.put(room.full, room);
		System.out.println("Room Created!");
		
		return room;
	}
	
	public GameRoom CreateRoom(List<String> _userList){
		GameRoom room = new GameRoom(_userList);
		roomList.add(room);
		System.out.println("Room Created!");
		return room;
	}
	
	public void RemoveRoom(GameRoom _room){
		roomList.remove(_room); // ���޹��� ���� �����Ѵ�.
		System.out.println("Room Deleted!");
	}
	
//	public static int RoomCount(){ return roomList.size();} // ���� ũ�⸦ ������
}