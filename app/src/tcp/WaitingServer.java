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
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class WaitingServer {
	// ������ ��Ʈ�� �����մϴ�.
	private static final int PORT = 8500;
	// ������ Ǯ�� �ִ� ������ ������ �����մϴ�.
	private static final int THREAD_CNT = 20;
	public static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT);	
	public static HashMap<String, Socket> waitingMap = new HashMap<>();
	static HashMap<String, Socket> playingMap = new HashMap<>();	//������ �г��ӿ� ���� ������ ���� �ؽ��� ����
	public static ArrayList<String> userList = new ArrayList<>();
    static RoomManager roomManager = new RoomManager(); // Ŭ���� ���� �� �ѹ��� �����ؾ� �Ѵ�.
	
	public static void main(String[] args) {
				
		System.out.println("====================[Waiting112Server]====================");        
		
		try {
			// �������� ����
			ServerSocket serverSocket = new ServerSocket(PORT);	
			
			// ���ϼ����� ����ɶ����� ���ѷ���
			while (true) {
				// ���� ���� ��û�� �ö����� ����մϴ�.
				Socket clientSocket = serverSocket.accept();

				// ���� �ϴ� �������� �Ŀ� 2���� ���� ������� �Ѱܼ� ���� ����
				
				// 3. �������� ���� �ۼ����� ���� i/o stream �� ���
				InputStream is = clientSocket.getInputStream(); // ���� -->	// read();
				BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

				String userNickName = null;
				userNickName = br.readLine();
				System.out.println(userNickName + "�� �α���\n");
				
				waitingMap.put(userNickName, clientSocket);
				playingMap.put(userNickName, clientSocket);
				userList.add(userNickName);
				
				//�α����� ������ �� �÷��̾������ �˷��ֱ�	
				Iterator<String> iterator = waitingMap.keySet().iterator();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					System.out.println("������ : "+ key); 
					OutputStream outputStream = waitingMap.get(key).getOutputStream(); // �۽�
					PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					printWriter2.println("[CLEAR]clear");
					printWriter2.flush();
					
					for(String id : waitingMap.keySet()){
						printWriter.println("[PLAYER]" + id);
						System.out.println("�̰ɺ��� : "+ id); 
						printWriter.flush();
			        }
				}
				threadPool.execute(new WaitingChat(userNickName, clientSocket));				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

//���� ó���� ���� Ŭ�����Դϴ�.
class WaitingChat implements Runnable{

	String userNickName = null;
	Socket clientSocket = null;
	
	InputStream is = null;
	BufferedReader br = null;
	OutputStream os = null;
	PrintWriter pw = null;

	public WaitingChat(String userNickName, Socket clientSocket) {
		this.userNickName = userNickName;
		this.clientSocket = clientSocket;
		
		try {
			this.is = clientSocket.getInputStream();
			this.br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			this.os = clientSocket.getOutputStream();
			this.pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		String msgFromClient = null;

		try {	
			while (true) {

				msgFromClient = br.readLine();
				
				if(msgFromClient != null){

					if (msgFromClient.startsWith("[CHAT]")) {
						Iterator<String> iterator = WaitingServer.waitingMap.keySet().iterator();
						while (iterator.hasNext()) { // �α����� ���������� �޽��� ������
							String key = (String) iterator.next();
							OutputStream outputStream = WaitingServer.waitingMap.get(key).getOutputStream(); // �۽�
							PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
							printWriter.println(msgFromClient);
							printWriter.flush();
						}
						System.out.println("" + msgFromClient);
					} else if(msgFromClient.startsWith("[WHISPER]")) {
						String whisper = msgFromClient.substring(9);
						int sub = whisper.indexOf(" ") + 1;
						String whispertoWhom = whisper.substring(1, sub-1);
						System.out.println("�Ӹ� ��� : " + whispertoWhom);
						String whisperMsg = whisper.substring(sub);
						System.out.println("�Ӹ� ���� : " + whisperMsg);
						
						if(WaitingServer.userList.contains(whispertoWhom)) {
							//�Ӹ������ ������ �������
							pw.println("[WHISPER]" + userNickName + " : " + whisperMsg);
							pw.flush();
							//if(WaitingServer.playingMap.containsKey(whispertoWhom)) {	//�Ӹ� ��밡 �÷��� ���ϰ��
								OutputStream outputStream2 = WaitingServer.playingMap.get(whispertoWhom).getOutputStream(); // �۽�
								PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream2, "UTF-8"));
								printWriter2.println("[WHISPER]" + userNickName + " : " + whisperMsg);
								printWriter2.flush();
							//} else if(WaitingServer.waitingMap.containsKey(whispertoWhom)) {	//�Ӹ� ��밡 ������ ���
							//	OutputStream outputStream2 = WaitingServer.waitingMap.get(whispertoWhom).getOutputStream();
							//	PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream2, "UTF-8"));
							//	printWriter2.println("[WHISPER]" + userNickName + " : " + whisperMsg);
							//	printWriter2.flush();
							//}							
						} else {						
							pw.println("[WHISPERTONULL]");
							pw.flush();
						}
					} else if(msgFromClient.startsWith("[START]")) {
						String ownerNickName = msgFromClient.substring(7);
						//���� ���� ������Ͽ� �־��ְ�
						//WaitingServer.playingMap.put(userNickName, WaitingServer.waitingMap.get(userNickName));
						//WaitingServer.playingMap.put(ownerNickName, WaitingServer.waitingMap.get(ownerNickName));
						//���� ������Ͽ����� ��� ����
						WaitingServer.waitingMap.remove(userNickName);
						WaitingServer.waitingMap.remove(ownerNickName);
						//���ӽ����� ������ �� �÷��̾������ �˷��༭ ����ڸ���Ʈ �ٽ� ����	
						Iterator<String> iterator = WaitingServer.waitingMap.keySet().iterator();
						while (iterator.hasNext()) {
							String key = (String) iterator.next();
							OutputStream outputStream = WaitingServer.waitingMap.get(key).getOutputStream(); // �۽�
							PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
							PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
							printWriter2.println("[CLEAR]clear");
							printWriter2.flush();
							
							for(String id : WaitingServer.waitingMap.keySet()){
								printWriter.println("[PLAYER]" + id);
								printWriter.flush();
					        }
						}
					}
					
					
					
					
					/*else if(msgFromClient.startsWith("[SEARCH]")) {
						
						String userNickName = msgFromClient.substring(8);
						
						System.out.println(userNickName + "�� ����");
						System.out.println("");
						
						WaitingServer.playingMap.put(userNickName, clientSocket);		//�ؽ��ʿ� ���� �г��Ӱ� �������� ���
						
						//������ �� ���� �ֳ� ���� �����ؾ��ϳ� �˻��ϴ� ����		
						for(int i=0; i<WaitingServer.roomManager.roomList.size(); i++) {	//roomList�� ���� 
							
							int j =0;	//roomList�� �� ���Ҵ��� Ȯ�ο� ����
												
							if(WaitingServer.roomManager.roomList.get(i).full == false) {	//������ �������
								
								GameRoom waitingRoom = WaitingServer.roomManager.roomList.get(i);	//����
								
								waitingRoom.AddUser(userNickName);	//���濡 ���� �߰�(Room�� ��������Ʈ�� �߰�)
																		
								waitingRoom.full = true;	//2���� ������ �� ���ٴ� �� �˷���
								
								System.out.println("*system : " + userNickName + "�� ���濡 ���� �� ���� ����");
								System.out.println("*system : " + waitingRoom.GetOwner() + "�� ���ο� ���� �����Ͽ� ���� ����");
								
								Socket ownerSocket = WaitingServer.playingMap.get(waitingRoom.GetOwner());	//���濡 �ִ� �� ���� ����						
								OutputStream ownerOutput = ownerSocket.getOutputStream();
								PrintWriter ownerPw = new PrintWriter(ownerOutput);
														
								pw.println("[START]");
								ownerPw.println("[START]");
								pw.flush();
								ownerPw.flush();
								
								WaitingServer.waitingMap.remove(userNickName);
								WaitingServer.waitingMap.remove(waitingRoom.GetOwner());
								//���ӽ����� ������ �� �÷��̾������ �˷��༭ ����ڸ���Ʈ �ٽ� ����	
								Iterator<String> iterator = WaitingServer.waitingMap.keySet().iterator();
								while (iterator.hasNext()) {
									String key = (String) iterator.next();
									System.out.println("������ : "+ key); 
									OutputStream outputStream = WaitingServer.waitingMap.get(key).getOutputStream(); // �۽�
									PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
									PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
									printWriter2.println("[CLEAR]clear");
									printWriter2.flush();
									
									for(String id : WaitingServer.waitingMap.keySet()){
										printWriter.println("[PLAYER]" + id);
										System.out.println("�̰ɺ��� : "+ id); 
										printWriter.flush();
							        }
								}
								
								try{	//�濡�ִ� �� ���� ��� ������ ���������ν� �� ����
									// ��û�� ���� ������ Ǯ�� ������� ������ �־��ݴϴ�.
									// ���Ĵ� ������ ������ ó���մϴ�.
									
									WaitingServer.threadPool.execute(new PlayingLogic(ownerSocket, waitingRoom, 0));
									WaitingServer.threadPool.execute(new PlayingLogic(clientSocket, waitingRoom, 1));
									PlayingLogic playingLogic = new PlayingLogic(ownerSocket, waitingRoom, 0);
									playingLogic.start();									
									PlayingLogic playingLogic2 = new PlayingLogic(clientSocket, waitingRoom, 1);
									playingLogic2.start();
																		
								}catch(Exception e){
									e.printStackTrace();
								}
								
								break;
							}
							
							j++;
												
							if(j == WaitingServer.roomManager.roomList.size()) {	//roomList�� �� ���Ҵµ��� ������ ������ �ʾҴٸ�, ���ο� �� ����
								GameRoom room = new GameRoom(false, userNickName);	//������ ���� ����, ���ÿ� ���� ��������Ʈ�� �������� �߰���
								
								WaitingServer.roomManager.CreateRoom(room);	//��Ŵ����� �ؽ��ʿ� �� �߰�
								
								System.out.println("*system : " + userNickName + "�� �����...");
								pw.println("*system : �ٸ� ������ ��ٸ��� ��....");
								pw.flush();
															
								
								break;
							}
						}	
						
						if(WaitingServer.roomManager.roomList.size() == 0) {	//������� ���� �ϳ��� ���ٸ�
							GameRoom room = new GameRoom(false, userNickName);	//������ ���� ����, ���ÿ� ���� ��������Ʈ�� �������� �߰���
							
							WaitingServer.roomManager.CreateRoom(room);	//��Ŵ����� ����Ʈ�� �� �߰�
							
							System.out.println("*system : " + userNickName + "�� �����...\n");
						}
						
					}*/
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}
	
	public void close() {
		try {
			// 4. ���� �ݱ� --> ���� ����
			is.close();
			br.close();
			os.close();
			pw.close();
			System.out.println("Ŭ�� ���� close �Ϸ�");
		} catch (IOException e) {
			System.out.println("close����");
			e.printStackTrace();
		}
	}
}

/*class PlayingLogic implements Runnable {
	
	private Socket clientSocket = null;
	private GameRoom playingRoom = null;
	private String clientNickName = null;
	private String enemyNickName = null;
	private int playerNum;
	
	InputStream myIs = null;
	BufferedReader myBr = null;
	OutputStream enemyOs, myOs = null;
	PrintWriter enemyPw, myPw = null;
	
	public PlayingLogic(Socket clientSocket, GameRoom playingRoom, int playerNum) {

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

			Socket enemySocket = WaitingServer.playingMap.get(enemyNickName);

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
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			if (playerNum == 0) {
				myPw.println("[SPEAR]1");
				myPw.flush();
			} else if (playerNum == 1) {
				myPw.println("[SPEAR]2");
				myPw.flush();
			}
			
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
				
				if (msgFromClient.startsWith("[COLLISION]")) {
					System.out.println("Ŭ��κ��� [COLLISION]�޾Ҵ�...." + msgFromClient + "\n");
					enemyPw.println(msgFromClient);
					enemyPw.flush();
					System.out.println("������� [COLLISION]���´�...." + msgFromClient + "\n");

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
				} else if (msgFromClient.startsWith("[GAMEOVER]")) {
					System.out.println("=========== G A M E  O V E R ===========");
					// �������� ���� ���� ���� �����ֱ�
					enemyPw.println("[GAMEOVER]" + myFish);
					enemyPw.flush();
					
					try {	//��Ŷ�� ������ �ð��� ���� ���� close���� ���ð�
						Thread.sleep(3500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}					
					break;
				}
				
				if(msgFromClient.startsWith("[CHAT]")) {
					enemyPw.println(msgFromClient);
					enemyPw.flush();
					System.out.println("" + msgFromClient);				
				} 
				if(msgFromClient.startsWith("[WHISPER]")) {
					String whisper = msgFromClient.substring(9);
					int sub = whisper.indexOf(" ") + 1;
					String whispertoWhom = whisper.substring(1, sub-1);
					System.out.println("�Ӹ� ��� : " + whispertoWhom);
					String whisperMsg = whisper.substring(sub);
					System.out.println("�Ӹ� ���� : " + whisperMsg);
					
					if(WaitingServer.userList.contains(whispertoWhom)) {
						//�Ӹ������ ������ �������
						myPw.println("[WHISPER]" + clientNickName + " : " + whisperMsg);
						myPw.flush();
						if(WaitingServer.playingMap.containsKey(whispertoWhom)) {	//�Ӹ� ��밡 �÷��� ���ϰ��
							OutputStream outputStream2 = WaitingServer.playingMap.get(whispertoWhom).getOutputStream(); // �۽�
							PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream2, "UTF-8"));
							printWriter2.println("[WHISPER]" + clientNickName + " : " + whisperMsg);
							printWriter2.flush();
						} else if(WaitingServer.waitingMap.containsKey(whispertoWhom)) {	//�Ӹ� ��밡 ������ ���
							OutputStream outputStream2 = WaitingServer.waitingMap.get(whispertoWhom).getOutputStream();
							PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream2, "UTF-8"));
							printWriter2.println("[WHISPER]" + clientNickName + " : " + whisperMsg);
							printWriter2.flush();
						}
					} else {						
						myPw.println("[WHISPERTONULL]");
						myPw.flush();
					}
				}

				 
			}

			System.out.println(clientNickName + "���� ���� ����� : " + myFish + "����");
			WaitingServer.roomManager.RemoveRoom(playingRoom); // ������ �������Ƿ� �ش�浵 ����
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
                
                mongoClient.close();
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
		  } catch(IOException e) {
		   System.out.println("close����");
		   e.printStackTrace();
		  }
	}
	
	private class FishThread extends Thread {
		
		private FishThread() {
			
		}
		
		public void run() {
			for (int i = 0; i < 10; i++) {

				try {
					Thread.sleep(3 * 1000); // �и��������̹Ƿ� 1000�� ���Ѵ�
					
					myPw.println("[FISH]");
					myPw.flush();
					
					//System.out.println("����� ����\n");
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.interrupt();
		}
	}
}*/




				
