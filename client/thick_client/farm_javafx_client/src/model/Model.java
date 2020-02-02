package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

public class Model {
	private static final int TIMEOUT_MS = 3000;
	private Callback cbk = null;
	private Thread udpThrd;
	private Random random = new Random();
	private int port = 5025;
	private Socket soc;
	private BufferedReader in = null;
	private BufferedWriter out = null;
	private String requestAllSensorsData = "get_sensors;";
	private String requestAllSetupData = "get_setup;";
	
	public interface Callback {
		void onUpdateSensors(SensorsData data);
		void onUpdateSetup(SensorsSetup data);
		void onEnd(String msg);
	}

	public void registerCallback(Callback cbk) {
		if (cbk != null) {
			this.cbk = cbk;
		}
	}

	private void run() {
		
		udpThrd = new Thread(() -> {
			String reсeived = "";
			
			//Запрашиваем исходное значение уставок в начале сеанса
			try {
				out.write(requestAllSetupData+"\n");
				out.flush();
			} catch (IOException e1) {
				disconnect();
			}
			
			//ждем ответа или таймаута в n секунд
			try {
				reсeived = in.readLine();
			} catch (IOException e1) {
				System.out.println(e1.getMessage());
				disconnect();
				return;
			}
			//если установлены функции обратного вызова в UI
			System.out.println(reсeived);
			if (cbk != null) {
				//разбираем принятое сообщение и отдаем его в UI
				try {
					cbk.onUpdateSetup((SensorsSetup) FarmProtocol.unmarshall(reсeived, SensorsSetup.class));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// регулярно раз в секунду посылаем запросы на данные датчиков,
			// пока не остановят из UI или соедение не разорвется по внешним причинам 
			while (!Thread.interrupted()) {
				//ждем секунду
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// прервали - выходим
					break;
				}
				//посылаем запрос на данные датчиков
				try {
					out.write(requestAllSensorsData+"\n");
					out.flush();
					
				} catch (IOException e1) {
					System.out.println(e1.getMessage());
					disconnect();
				}

				//принимаем ответ с данными датчиков
				try {
					reсeived = in.readLine();
				} catch (IOException e1) {
					System.out.println(e1.getMessage());
					disconnect();
				}

				//если установлены функции обратного вызова в UI
				System.out.println(reсeived);
				if (cbk != null) {
					try {
						cbk.onUpdateSensors((SensorsData) FarmProtocol.unmarshall(reсeived, SensorsData.class));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		udpThrd.start();
	}
	
	public void sendSetup(SensorsSetup set) {
		//Посылаем уставки
		try {
			out.write(FarmProtocol.marshall(set)+"\n");
			out.flush();
			
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
			disconnect();
		}
	}
	

	private void stop() {
		udpThrd.interrupt();
	}

	// ------
	public void connect(String sip) throws UnknownHostException, SocketException, IOException {
		InetAddress ipaddr;
		ipaddr = InetAddress.getByName(sip);
		soc = new Socket();
		SocketAddress sockaddr = new InetSocketAddress(ipaddr, port);
		// приходится указывать таймаут в двух местах - разобраться
		soc.setSoTimeout(TIMEOUT_MS);
		soc.connect(sockaddr, TIMEOUT_MS);
		in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));
		run();
	}

	public void disconnect() {
		stop();
		try {
			soc.close();
		} catch (IOException e) {
			// незнаю что делать если close() не получится
		}
		cbk.onEnd("disconnect");
	}
}
