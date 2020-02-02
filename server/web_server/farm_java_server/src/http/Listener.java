package http;

import java.net.Socket;
import java.util.Date;
import java.util.List;

public class Listener {
	private static final String newLine = "\r\n";
	private final String localUri;

	public interface Serve {
		boolean exec(Methods m, String uri, Socket soc);
	}
	
	private final Serve srv;
	
	public Listener(String localUri, Serve srv) {
		this.localUri = localUri;
		this.srv = srv;

	}

	public boolean checkRequest(String reqFirstLine, Socket soc) {
		boolean res = false;
		//по стандарту URI вытаскивается из первой строки заголовка как подстрока между первыми двумя пробелами.
		String uri = getURI(reqFirstLine);
		Methods m =  getMethod(reqFirstLine);
		if (localUri.equals(uri)) {
			//вызываем собственно обработчик
			res = srv.exec(m, uri, soc);
		}
		
		return res;
	}
	
	//protected abstract String serve (Methods m, String uri, List<String> req);
	
	
	// отрезаются параметры запроса (если они присутствуют). 
	private String getURI(String header) {
		int from = header.indexOf(" ") + 1;
		int to = header.indexOf(" ", from);
		String uri = header.substring(from, to);
		int paramIndex = uri.indexOf("?");
		if (paramIndex != -1) {
			uri = uri.substring(0, paramIndex);
		}
		return uri;
	}
	
	private Methods getMethod(String header) {
		Methods res = null;
		String methodStr = header.substring(0, header.indexOf(" "));
		for (Methods m : Methods.values()) {
			if (methodStr.equals(m.toString())) {
				res = m;
			}
		}
		return res;
	}
	
}
