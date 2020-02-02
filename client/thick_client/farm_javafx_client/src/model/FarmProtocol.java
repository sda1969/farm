package model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FarmProtocol {

	// не надо создавать объект этого класса
	private FarmProtocol() {
	};

	// создает объект указанного класса, инициализирует поля значением по умолчанию
	// которое для UI имеет смысл что данные не доступны
	// разбирает строку и присваивает значения соответствующим полям

	public static Object unmarshall(String str, Class<?> clazz) throws Exception {
		//создадим объект заданного класса, который будем возвращать
		Object o;
		try {
			o = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
			throw new Exception(e1.getMessage());
		}

		List<String> fnames = new ArrayList<>();
		
		// получаем список полей и присваиваем исходное значение
		for (Field field : clazz.getDeclaredFields()) {
			String name = field.getName();
			if(name.equals("prefix"))
				continue;
			fnames.add(name);
			try {
				field.set(o, -1);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new Exception(e.getMessage());
			}
		}

		// разбираем строку и присваиваем полям извлеченные значения
		// не найденные останутся в значении по умолчанию
		String prefix = null;
		try {
			prefix = (String) clazz.getField("prefix").get(null);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e1) {
			throw new Exception(e1.getMessage());
		}
		//префикс должен совпасть
		if (!prefix.equals(str.substring(0, prefix.length()))) {
			throw new Exception("wrong prefix="+ prefix);
		}
		
		String tmp = str.substring(prefix.length());		//выбрасываем префикс  
		String body = tmp.substring(0, tmp.length() - 1);	//выбрасываем последнюю ;
		
		String[] pairs = body.split(",");
		for (String pair : pairs) {
			String[] val = pair.split("=");
			if (val != null) {
				if (val.length == 2) {
					if (fnames.contains(val[0])) {
						try {
							clazz.getField(val[0]).set(o, Integer.parseInt(val[1]));
						} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
								| IllegalAccessException e) {
							// не смогли присвоить - тогда просто игнорируем
						}
					}
				}
			}
		}
		return o;
	}

	// берет любой объект с данными и возвращает строку
	public static String marshall(Object o) throws Exception {
		String res = "";
		Class<?> clazz = o.getClass();
		
		String prefix = null;
		try {
			prefix = (String) clazz.getField("prefix").get(null);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e1) {
			throw new Exception(e1.getMessage());
		}
		
		for (Field field : clazz.getDeclaredFields()) {
			try {
				String name = field.getName();
				if(name.equals("prefix"))
					continue;
				res += (name + "=" + field.get(o) + ",");
			} catch (IllegalArgumentException | IllegalAccessException e) {
				res = null;
				break;
			}
		}
		res = prefix+res;
		res = res.substring(0, res.length() - 1) + ";";
		System.out.println(res);
		return res;
	}
}
