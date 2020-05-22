package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class NetworkGameServer {
	// ������ ��Ʈ�� �����մϴ�.
	private static final int PORT = 9000;
	// ������ Ǯ�� �ִ� ������ ������ �����մϴ�.
	private static final int THREAD_CNT = 10;
	private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT);
	
	static HashMap<String, Socket> userMap = new HashMap<String, Socket>();	//������ �г��ӿ� ���� ������ ���� �ؽ��� ����
    static RoomManager roomManager = new RoomManager(); // Ŭ���� ���� �� �ѹ��� �����ؾ� �Ѵ�.
	
	public static void main(String[] args) {
				
		System.out.println("====================[Game^Server]====================");        
		
		try {
			// �������� ����
			ServerSocket serverSocket = new ServerSocket(PORT);	
			
			// ���ϼ����� ����ɶ����� ���ѷ���
			while(true){
				// ���� ���� ��û�� �ö����� ����մϴ�.
				Socket clientSocket = serverSocket.accept();
				
				//���� �ϴ� �������� �Ŀ� 2���� ���� ������� �Ѱܼ� ���� ����
				
				// 3. �������� ���� �ۼ����� ���� i/o stream �� ���
				InputStream is = clientSocket.getInputStream(); //���� --> read();
				BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				OutputStream os = clientSocket.getOutputStream(); //�۽� --> write();
			  	PrintWriter pw = new PrintWriter(os);
			  	
				String userNickName = null;

				userNickName = br.readLine();
												
				System.out.println(userNickName + "�� �׽���\n");
				
				userMap.put(userNickName, clientSocket);		//�ؽ��ʿ� ���� �г��Ӱ� �������� ���
				
				//������ �� ���� �ֳ� ���� �����ؾ��ϳ� �˻��ϴ� ����		
				for(int i=0; i<roomManager.roomList.size(); i++) {	//roomList�� ���� 
					
					int j =0;	//roomList�� �� ���Ҵ��� Ȯ�ο� ����
										
					if(roomManager.roomList.get(i).full == false) {	//������ �������
						
						GameRoom waitingRoom = roomManager.roomList.get(i);	//����
						
						waitingRoom.AddUser(userNickName);	//���濡 ���� �߰�(Room�� ��������Ʈ�� �߰�)
						//clientMap.get(userNickName).EnterRoom(waitingRoom);	//�뿡 ���� ����(������ ���ѹ� ������ �ش� �� ���)					
																
						waitingRoom.full = true;	//2���� ������ �� ���ٴ� �� �˷���
						
						System.out.println("*system : " + userNickName + "�� ���濡 ���� �� ���� ����");
						System.out.println("*system : " + waitingRoom.GetOwner() + "�� ���ο� ���� �����Ͽ� ���� ����");
						
						Socket ownerSocket = userMap.get(waitingRoom.GetOwner());	//���濡 �ִ� �� ���� ����						
						OutputStream ownerOutput = ownerSocket.getOutputStream();
						PrintWriter ownerPw = new PrintWriter(ownerOutput);
												
						pw.println("[START]" + waitingRoom.GetOwner());
						ownerPw.println("[START]" + userNickName);
						pw.flush();
						ownerPw.flush();
						
						try{	//�濡�ִ� �� ���� ��� ������ ���������ν� �� ����
							// ��û�� ���� ������ Ǯ�� ������� ������ �־��ݴϴ�.
							// ���Ĵ� ������ ������ ó���մϴ�.
							threadPool.execute(new GameLogic(ownerSocket, waitingRoom, 0));
							threadPool.execute(new GameLogic(clientSocket, waitingRoom, 1));
							
							ownerPw.println("[SPEAR]1");
							ownerPw.flush();
							pw.println("[SPEAR]2");
							pw.flush();
						}catch(Exception e){
							e.printStackTrace();
						}
						
						break;
					}
					
					j++;
										
					if(j == roomManager.roomList.size()) {	//roomList�� �� ���Ҵµ��� ������ ������ �ʾҴٸ�, ���ο� �� ����
						GameRoom room = new GameRoom(false, userNickName);	//������ ���� ����, ���ÿ� ���� ��������Ʈ�� �������� �߰���
						
						roomManager.CreateRoom(room);	//��Ŵ����� �ؽ��ʿ� �� �߰�
						//clientMap.get(userNickName).EnterRoom(room);	//�뿡 ���� ����(������ ���ѹ� ������ �ش� �� ���)
						
						System.out.println("*system : " + userNickName + "�� �����...");
						pw.println("*system : �ٸ� ������ ��ٸ��� ��....");
						pw.flush();
						
						break;
					}
				}	
				
				if(roomManager.roomList.size() == 0) {	//������� ���� �ϳ��� ���ٸ�
					GameRoom room = new GameRoom(false, userNickName);	//������ ���� ����, ���ÿ� ���� ��������Ʈ�� �������� �߰���
					
					roomManager.CreateRoom(room);	//��Ŵ����� ����Ʈ�� �� �߰�
					//clientMap.get(userNickName).EnterRoom(room);	//�뿡 ���� ����(������ ���ѹ� ������ �ش� �� ���)
					
					System.out.println("*system : " + userNickName + "�� �����...\n");
				} 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
 
//���� ó���� ���� Ŭ�����Դϴ�.
class GameLogic implements Runnable {

	private Socket clientSocket = null;
	private GameRoom playingRoom = null;
	private String clientNickName = null;
	private String enemyNickName = null;
	private int playerNum;
	
	InputStream myIs = null;
	BufferedReader myBr = null;
	OutputStream enemyOs, myOs = null;
	PrintWriter enemyPw, myPw = null;

	public GameLogic(Socket clientSocket, GameRoom playingRoom, int playerNum) {

		this.clientSocket = clientSocket;
		this.playingRoom = playingRoom;
		this.playerNum = playerNum;

		try {
			// 3. �������� ���� �ۼ����� ���� i/o stream �� ���
			
			int enemyNum=6969;

			if (playerNum == 0) {
				enemyNum = 1;
			} else if (playerNum == 1) {
				enemyNum = 0;
			}

			clientNickName = playingRoom.userList.get(playerNum);
			enemyNickName = playingRoom.userList.get(enemyNum);

			Socket enemySocket = NetworkGameServer.userMap.get(enemyNickName);

			enemyOs = enemySocket.getOutputStream();
			enemyPw = new PrintWriter(new OutputStreamWriter(enemyOs, "UTF-8"));

			myIs = clientSocket.getInputStream(); // ���� --> read();
			myBr = new BufferedReader(new InputStreamReader(myIs, "UTF-8"));
			myOs = clientSocket.getOutputStream(); // �۽� --> write();
			myPw = new PrintWriter(myOs);

		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		String msgFromClient = null;

		// Ŭ���̾�Ʈ�� ȹ���� ����
		int myFish = 0;

		try {
				
			FishThread fishThread = new FishThread();
			fishThread.start();

			while (true) { // ���ӷ���

				msgFromClient = myBr.readLine();

				if (msgFromClient.startsWith("[SPEAR_X]")) {
					enemyPw.println(msgFromClient);
					enemyPw.flush();
				}
				if (msgFromClient.startsWith("[SPEAR_Y]")) {
					enemyPw.println(msgFromClient);
					enemyPw.flush();
				}
				
				if(msgFromClient.startsWith("[CHAT]")) {
					enemyPw.println(msgFromClient);
					enemyPw.flush();
					System.out.println("" + msgFromClient);				
				}

				if (msgFromClient.startsWith("[COLLISION]")) {
					enemyPw.println(msgFromClient);
					enemyPw.flush();

					String whatFish = msgFromClient.substring(11);
					int whatFishNum = Integer.parseInt(whatFish);

					if (whatFishNum == 6) {
						if (myFish > 0)
							myFish -= 1;
					} else if (whatFishNum == 8) {
						if (myFish > 0)
							myFish -= 1;
					} else if (whatFishNum == 9) {
						if (myFish > 0)
							myFish -= 1;
					} else {
						myFish += 1;
					}
				}	else if (msgFromClient.startsWith("[GAMEOVER]")) {
					System.out.println("=========== G A M E  O V E R ===========");
					// �������� ���� ���� ���� �����ֱ�
					enemyPw.println("[GAMEOVER]" + myFish);
					enemyPw.flush();
					
					try {	//��Ŷ�� ������ �ð��� ���� ���� close���� ���ð�
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}					
					break;
				} 
			}

			System.out.println(clientNickName + "���� ���� ����� : " + myFish + "����");
			NetworkGameServer.roomManager.RemoveRoom(playingRoom); // ������ �������Ƿ� �ش�浵 ����
			playingRoom = null; // �� ��ü ����
			
			String inDate = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
	        String inTime = new java.text.SimpleDateFormat("HHmmss").format(new java.util.Date());
			
			MongoClient mongoClient = new MongoClient("119.205.220.8", 27017);
			System.out.println("����DB ���� ����");

            //�����ͺ��̽� ����
            DB db = mongoClient.getDB("test");
            //�÷��� ��������
            DBCollection collection = db.getCollection("playLog");

            int idLength = clientNickName.length();

            //Ư�� ���ǿ� �´� ������ ���
            BasicDBObject o = new BasicDBObject();
            o.put("ID", clientNickName);
            DBCursor cursor = collection.find(o);

            if(!cursor.hasNext()) {//ù �÷��̶��
                //user ���̺� �����ͻ���
                BasicDBObject doc = new BasicDBObject();
                doc.put("Date", inDate);
                doc.put("Time", inTime);
                doc.put("ID", clientNickName);
                doc.put("NumofPlaying", 1);
                doc.put("PlayWithWho", enemyNickName);
                collection.insert(doc);
            } else {
                ArrayList<Integer> list = new ArrayList<>();

                while(cursor.hasNext()){
                    //�α� �պκ� �ڸ���
                    String temp = cursor.next().toString().substring(105 + idLength);
                    System.out.println("temp : "+ temp);

                    //�ڸ� �α��� ���� ����
                    String NumofPlay = temp.replaceAll("[^0-9]", "");;
                    System.out.println("NumofPlay : " + NumofPlay);
                    
                    int numofPlay = Integer.parseInt(NumofPlay);

                    //����Ʈ�� ���� �߰�
                    list.add(numofPlay);
                }

                //����Ʈ �� �ִ밪 + 1 = ���� playNum
                int cumulativeNum = Collections.max(list);
                cumulativeNum += 1;
                System.out.println("cumulativeNum : "+ cumulativeNum);

                //user ���̺� �����ͻ���
                BasicDBObject doc = new BasicDBObject();
                doc.put("Date", inDate);
                doc.put("Time", inTime);
                doc.put("ID", clientNickName);
                doc.put("NumofPlaying", cumulativeNum);
                doc.put("PlayWithWho", enemyNickName);
                collection.insert(doc);
            }

		} catch (IOException e) {
			System.out.println("����Ÿ �ۼ��ſ���");
			e.printStackTrace();
		} finally {
			close();
		}
	}
	public void close(){
		  try {
		  // 4. ���� �ݱ� --> ���� ����
		   enemyOs.close();
		   enemyPw.close();
		   myIs.close();
		   myBr.close();
		   myOs.close();
		   myPw.close();
		   clientSocket.close();
		   System.out.println("Ŭ�� ���� close �Ϸ�");
		  }catch(IOException e) {
		   System.out.println("close����");
		   e.printStackTrace();
		  }
	}
	
	public class FishThread extends Thread {
		
		public FishThread() {
			
		}
		
		public void run() {
			for (int i = 0; i < 10; i++) {

				try {
					Thread.sleep(3 * 1000); // �и��������̹Ƿ� 1000�� ���Ѵ�
					
					myPw.println("[FISH]");
					myPw.flush();
					
					System.out.println("����� ����\n");
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.interrupt();
		}
	}
}
 

/*class ConnectionWrap implements Runnable{

	private Socket ownerSocket = null;
	private Socket clientSocket = null;
	private GameRoom playingRoom = null;
	
	InputStream is, is2 = null;
	BufferedReader br, br2 = null;
	OutputStream os, os2 = null;
	PrintWriter pw, pw2 = null;

	public ConnectionWrap(Socket ownerSocket, Socket clientSocket, GameRoom playingRoom) {
		
		this.ownerSocket = ownerSocket;
		this.clientSocket = clientSocket;
		this.playingRoom = playingRoom;
		
		try {			
			 // 3. �������� ���� �ۼ����� ���� i/o stream �� ���
			 is = ownerSocket.getInputStream(); //���� --> read();
			 br = new BufferedReader(new InputStreamReader(is));
			 os = ownerSocket.getOutputStream(); //�۽� --> write();
		  	 pw = new PrintWriter(os);
		  	 
		  	 is2 = clientSocket.getInputStream(); //���� --> read();
			 br2 = new BufferedReader(new InputStreamReader(is2));
			 os2 = clientSocket.getOutputStream(); //�۽� --> write();
		  	 pw2 = new PrintWriter(os2);
		  	 
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		String msgFromOwner = null;
		String msgFromClient = null;
		String ownerNickName = playingRoom.userList.get(0);
		String clientNickName = playingRoom.userList.get(1);
		
		//Ŭ���̾�Ʈ���� ���� ����� ��
		playingRoom.ownerFish = 0;
		playingRoom.clientFish = 0;

		try {			
				//�÷��̾ spear���� ����
				pw.println("[SPEAR]1");
				pw2.println("[SPEAR]2");
				pw.flush();
				pw2.flush();
			
				FishThread fishThread = new FishThread();
				fishThread.start();
								
				while (true) { // ���ӷ���
								
					msgFromOwner = br.readLine();
					msgFromClient = br2.readLine();
					
					if(msgFromOwner.startsWith("[SPEAR_X]")) {
						pw2.println(msgFromOwner);
						pw2.flush();
					}
					if (msgFromOwner.startsWith("[SPEAR_Y]")) {
						pw2.println(msgFromOwner);
						pw2.flush();			
					}				
					
					if(msgFromClient.startsWith("[SPEAR_X]")) {
						pw.println(msgFromClient);
						pw.flush();
					}				
					if (msgFromClient.startsWith("[SPEAR_Y]")) {
						pw.println(msgFromClient);
						pw.flush();
					}
					
					if(msgFromOwner.startsWith("[COLLISION]")) {
						pw2.println(msgFromOwner);
						pw2.flush();
						
						String whatFish = msgFromOwner.substring(11);
						int whatFishNum = Integer.parseInt(whatFish);
						
					if (whatFishNum == 6) {
						if (playingRoom.ownerFish > 0)
							playingRoom.ownerFish -= 1;
					} else if (whatFishNum == 8) {
						if (playingRoom.ownerFish > 0)
							playingRoom.ownerFish -= 1;
					} else if (whatFishNum == 9) {
						if (playingRoom.ownerFish > 0)
							playingRoom.ownerFish -= 1;
					} else {
						playingRoom.ownerFish += 1;
					}
				}
					
					if(msgFromClient.startsWith("[COLLISION]")) {
						pw.println(msgFromClient);
						pw.flush();
						
						String whatFish = msgFromClient.substring(11);
						int whatFishNum = Integer.parseInt(whatFish);
						
					if (whatFishNum == 6) {
						if (playingRoom.clientFish > 0)
							playingRoom.clientFish -= 1;
					} else if (whatFishNum == 8) {
						if (playingRoom.clientFish > 0)
							playingRoom.clientFish -= 1;
					} else if (whatFishNum == 9) {
						if (playingRoom.clientFish > 0)
							playingRoom.clientFish -= 1;
					} else {
						playingRoom.clientFish += 1;
					}
				}
					
					if(msgFromOwner.startsWith("[GAMEOVER]") || msgFromClient.startsWith("[GAMEOVER]")) {		
						System.out.println("=========== G A M E  O V E R ===========");
						//������ ���� ���� �����ֱ�
						pw.println("[GAMEOVER]" + playingRoom.clientFish);
						pw2.println("[GAMEOVER]" + playingRoom.ownerFish);
						pw.flush();
						pw2.flush();
						break;
					}
				}
			
			System.out.println("���� �� / �� ����\n");
			System.out.println(ownerNickName + "���� ���� ����� : " + playingRoom.ownerFish + "����");
			System.out.println(clientNickName + "���� ���� ����� : " + playingRoom.clientFish + "����");

			NetworkGameServer.roomManager.RemoveRoom(playingRoom); // ������ �������Ƿ� �ش�浵 ����
			playingRoom = null;	//�� ��ü ����
			
		} catch (IOException e) {
			System.out.println("����Ÿ �ۼ��ſ���");
			e.printStackTrace();
		} finally {
			close();
		}
	}
	public void close(){
		  try {
		  // 4. ���� �ݱ� --> ���� ����
		   is.close();
		   br.close();
		   os.close();
		   pw.close();
		   is2.close();
		   br2.close();
		   os2.close();
		   pw2.close();
		   ownerSocket.close();
		   clientSocket.close();
		   System.out.println("�� ���� Ŭ�� ���� close �Ϸ�");
		  }catch(IOException e) {
		   System.out.println("close����");
		   e.printStackTrace();
		  }
	}
	
	public class FishThread extends Thread {
		
		public void FishThread() {
			
		}
		
		public void run() {
			for (int i = 0; i < 10; i++) {

				Random random = new Random();		
				int randomInt = random.nextInt(4)+3;

				System.out.println(" : " + randomInt);

				try {
					Thread.sleep(randomInt * 1000); // �и��������̹Ƿ� 1000�� ���Ѵ�
					
					pw.println("[FISH]");
					pw.flush();
					pw2.println("[FISH]");
					pw2.flush();
					
					System.out.println("����� ����\n");
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.interrupt();
		}
	}
}*/








