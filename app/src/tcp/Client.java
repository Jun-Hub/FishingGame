package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Scanner;
import org.json.simple.JSONObject;
import com.google.gson.Gson;

// ������ ������ �÷����ϴ� ������ Ŭ�����̴�.

public class Client {

	private GameRoom room; // ������ ���� ���̴�
	private Socket socket;
	private String nickName = "�ļ���"; // ������ �г���

	
	public Client() { // �ƹ��� ������ ���� ���� ������ ���� ��

	}

	public Client(Socket socket) { // �г��� ������ ������ ����(���� �ο��ȵǰ�, ��ġ���� ��)
		this.socket = socket;
	}

	public Client(Socket socket, GameRoom room) { // �г��� ������ ������ ����(��ٷ� �ο�..)
		this.socket = socket;
		this.room = room;
	}

	public void runningClient() {
		// ����Ÿ�� �ۼ����ϱ� ���� ������ �غ�

		Socket socket = null; // 1. ���ϰ�ü ����
		String userNickName = null;

		Scanner scanner = new Scanner(System.in);
		System.out.println("���ӿ��� ����� �г����� �Է����ּ���.");
		userNickName = scanner.nextLine();

		this.nickName = userNickName; // �Է¹��� �г������� �г��� ����
		System.out.println("�г��� ���� �Ϸ� : " + this.nickName);
		System.out.println("");
		
		//���� ��ġ ����
		String search;	
		System.out.println("��ġ�Ϸ��� 1���� �Է��ϼ���");
		search = scanner.nextLine();

		if (!search.equals("1")) {
			while (true) {
				System.out.println("��ġ�Ϸ��� 1���� �Է��϶�ϱ��");
				search = scanner.nextLine();

				if (search.equals("1")) {
					break;
				}
			}
		}
		
		try {		
			socket = new Socket("localhost", 9000); // IP,Port ��ȣ�� �˾ƾ� ������ ������.

			// 2. ����Ÿ �ۼ����� ���� i/o stream�� ���� �Ѵ�.
			InputStream is = socket.getInputStream(); // ���� --> read();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			OutputStream os = socket.getOutputStream(); // �۽� --> write();
			PrintWriter pw = new PrintWriter(os);
			
			BufferedReader stringReader = new BufferedReader(new InputStreamReader(System.in));
			
			String json = "";
			Gson gson = new Gson();	//Client ��ü�� �����ϱ� ���� Gson ����
			
			json = gson.toJson(this);	//�ڱ� �ڽ��� Json�����ͷ� ����
			
			pw.println(json);	//������ json����
			pw.println(this.nickName);	//������ �ڱ� �г��� ����
			pw.flush();
			
			ResponseThread responseThread = new ResponseThread(br);
			responseThread.start();		//�����κ��� ����޾Ƽ� ����ϴ� ������
				
			// 3_1. ����Ÿ �۽�
			String attack = null;
					
			while (true) {
				attack = stringReader.readLine();

				if (!attack.equals("2")) {
					while (true) {
						System.out.println("�����Ϸ��� 2���� �Է��϶�ϱ��");
						attack = stringReader.readLine();

						if (search.equals("2")) {
							break;
						}
					}
				}
				pw.println(attack);
				pw.flush();
			}


		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			 try {
			 // 4. ���� �ݱ� --> ���� ����
				 if(socket!=null)
					 socket.close();
				 if(scanner!=null)
					 scanner.close();
			 }catch(IOException e) {e.printStackTrace();}
		}
	}
	
	public static void main(String[] args) {

		Client client = new Client();
		client.runningClient();
		
	}
	
	// Thread Ŭ������ ��ӹ��� Ŭ������ ����
	class ResponseThread extends Thread{
		
		BufferedReader bufferedReader;
		String response = null;		//�����κ��� ���� ����
		
		public ResponseThread(BufferedReader bufferedReader) {
			this.bufferedReader = bufferedReader;
		}

		public void run(){
			
			while (true) {
				try {
					response = bufferedReader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println(response);
				
				if(response.equals("������������������������������������������GAME OVER������������������������������������������\n")) {
					break;
				}
			}
			this.interrupt();
		}
	}

	public void EnterRoom(GameRoom _room) {
	//	_room.AddUser(this.nickName); // �뿡 �����Ų ��
		this.room = _room; // ������ ���� ���� ������ �����Ѵ�.(�߿�)
	}
}