package http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


//Why is it impossible, without attempting I/O, to detect that TCP socket was gracefully closed by peer?
public class MyHttpServer {
	private final static int TIMEOUT_IN_100MS_TICS = 100;
	private final static short port = 8000;
	private ServerSocket ssoc;
	private Socket soc;
	private static final String newLine = "\r\n";
	//нужен синхронизированный доступ
	private static List<Listener> listeners = new LinkedList<>();
	
	private static class TimeOutException extends Exception {
		public TimeOutException(String msg) {
			super(msg);
		}
	}
	
	synchronized public static void addListener(Listener c) {
		listeners.add(c);
	}
	
	public MyHttpServer() throws IOException {
		ssoc = new ServerSocket(port);
		while (true) {
			//new Connection(ssoc.accept());
			soc = ssoc.accept();		
			new Thread(() -> {
				try {
					threadFunc();
				} catch (Exception e) {
					System.out.println("Connection thread done..");
				}
			}).start();
			
			Runtime r = Runtime.getRuntime();
			r.gc();// убрать потом
			System.out.println("memory: " + r.freeMemory());

		}
	}

	protected void threadFunc() throws Exception {

		BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
		System.out.println("accept from: " + soc.getInetAddress());
		
		while (true) {// exit exception only
					
			String reqFirstLine = "";
			//читаем первую строчку запроса и отдаем всем зарегистрированным получателям	
			//смотрим список зарегистрированных получателей
			while(!in.ready())
				;
			
			if(in.ready()) {
				reqFirstLine = in.readLine();// BufferedReader - не читает последний \n
				//System.out.println(reqFirstLine);
			}
			
			boolean reply = false;
			for (Listener cs : listeners) {
				if (reply = cs.checkRequest(reqFirstLine, soc)) {
					//кто-то обработал 
					break;
				}
			}
			
			if (!reply ) {
				//clear 
				while (in.ready()) {
					String reqLine = in.readLine();// BufferedReader - не читает последний \n
					// System.out.println(reqLine);
				}
				//отправляем ошибку 404 not found
				System.out.println(reqFirstLine+" - 404 Страница не найдена");
				String style = "<style type=\"text/css\">h1 { background-color: silver; color: red; text-align: center;}</style>";
				String content = "<h1>"+reqFirstLine+" - 404 Страница не найдена. </h1>";
				String title = "Ошибка";
				String response = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>"+title+
						"</title><link rel=\"icon\" type=\"image/png\" href=\"/favicon.ico?v=2\"/>"+style+"</head><body>"+content+"</body></html>";  
				   
				String err = "HTTP/1.1 404 NOT FOUND" + newLine + "Content-Type: text/html" + newLine + "Date: " + new Date()
						+ newLine + "Content-length: " + response.length() + newLine + newLine + response;
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));
				out.write(err);
				out.flush();
				//нельзя выходить из соединения сразу же
			}
			
			// ожидаем дальнейшего использования соединения eсли keep-alive
			int cnt = 0;
			while (!in.ready()) {
				Thread.sleep(100);
				if (++cnt >= TIMEOUT_IN_100MS_TICS) {
					soc.close();
					throw new TimeOutException("connection timeout");
				}
			}
		}

	}
	
	
	
	public static void main(String[] args) {
		System.out.println("MyHttpServer run..");

		try {
			addListener(new Listener("/h", (a, b, c) -> {
				if (a == null) {
					//501 + Allow:
					return false;
				}
				System.out.println(a.toString() + " " + b);
				
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));

					// Выбрасываем все остальные строки запроса, здесь они нафиг не нужны
					// Пока есть строки с данными
					while (in.ready()) {
						String reqLine = in.readLine();// BufferedReader - не читает последний \n
						// System.out.println(reqLine);
					}

					String response = "Hello, World2!";
					String reply = "HTTP/1.0 200 OK" + newLine + "Content-Type: text/plain" + newLine + "Date: "
							+ new Date() + newLine + "Content-length: " + response.length() + newLine + newLine
							+ response;

					out.write(reply);
					out.flush();

					return true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}

			}));
			
			addListener(new Listener("/", (a, b, c) -> {
				if (a == null) {
					//501 + Allow:
					return false;
				}
				System.out.println(a.toString() + " " + b);
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));

					// Выбрасываем все остальные строки запроса, здесь они нафиг не нужны
					// Пока есть строки с данными
					while (in.ready()) {
						String reqLine = in.readLine();// BufferedReader - не читает последний \n
						// System.out.println(reqLine);
					}
					String title = "Тестовая страница";
					String response = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>"+title+
							"</title><link rel=\"icon\" type=\"image/png\" href=\"/favicon.ico?v=2\" /></head><body><h1>test page</h1> </body></html>";
					String reply = "HTTP/1.0 200 OK" + newLine + "Content-Type: text/html" + newLine + "Date: "
							+ new Date() + newLine + "Content-length: " + response.length() + newLine + newLine
							+ response;

					out.write(reply);
					out.flush();

					return true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}

			}));
			

			addListener(new Listener("/favicon.ico", (a, b, c) -> {
				if (a == null) {
					//501 + Allow:
					return false;
				}
				System.out.println(a.toString() + " " + b);
				try {
					BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
					DataOutputStream dout = new DataOutputStream(c.getOutputStream());

					// Выбрасываем все остальные строки запроса, здесь они нафиг не нужны
					// Пока есть строки с данными
					while (in.ready()) {
						String reqLine = in.readLine();// BufferedReader - не читает последний \n
						// System.out.println(reqLine);
					}

					byte[] buff = new byte[65536];
					int n = 0;
					try (DataInputStream reader = new DataInputStream(new FileInputStream("favicon.ico"))) {
						if (reader.available() > 0) {
							n = reader.read(buff);
						}
					}
					String reply = "HTTP/1.0 200 OK" + newLine + "Content-Type: image/png" + newLine + "Date: "
							+ new Date() + newLine + "Content-length: " + n + newLine + newLine;

					// заголовок ответа
					out.write(reply);
					out.flush();
					//выдаем бинарные данные
					dout.write(buff, 0, n);
					dout.flush();

					return true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}

			}));

			new MyHttpServer();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
