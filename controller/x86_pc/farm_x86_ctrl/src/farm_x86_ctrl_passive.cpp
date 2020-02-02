//============================================================================
// Name        : farm_server_x86.cpp
// Author      : D.Stepanov
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include <fstream>
#include <string>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

#include "DataManager.h"
using namespace std;

namespace {

constexpr int BUFFER_SIZE = 65536;
constexpr int PORT = 5025;
string requestAllSensorsData = "get_sensors;";
string requestAllSetupData = "get_setup;";

/* Реальные значение от датчиков собираются в этой функции */
void testUpdateSensors(data::DataManager& sens) {
	/* initialize random seed: */
	srand(time(NULL));
	sens.setvalue("red_led", rand() % 99 + 1); //1 - 99
	sens.setvalue("blue_led", rand() % 99 + 1);
	sens.setvalue("air_temp", rand() % 99 + 1);
	sens.setvalue("cool_water_in_temp", rand() % 49 + 1);
	sens.setvalue("cool_water_out_temp", rand() % 49 + 1);
	sens.setvalue("sprinkle_water_temp", rand() % 49 + 1);
	sens.setvalue("tank_level", rand() % 99 + 1);
	sens.setvalue("sprinkle_water_flow_instant", rand() % 99 + 1);
	sens.setvalue("sprinkle_water_flow_average", rand() % 99 + 1);
	sens.setvalue("air_humidity", rand() % 99 + 1);
}

}

int main() {
	//инициализация данных Уставок
	data::DataManager setup = data::DataManager("setup:", { "red_led", "blue_led",
			"sprinkle_water_temp_max", "sprinkle_water_temp_min", "valve_on",
			"valve_off" });

	//пытаемся прочитать данные уставок из файла
	std::string setupFName = "setup.txt";
	bool readDone = false;
	std::ifstream setupFile(setupFName);
	if (setupFile.is_open()) {
		std::string line;
		//все хранится только в одной строке
		if (getline(setupFile, line)) {
			setup.unmarshall(line);
			readDone = true;
		}
		setupFile.close();
	}

	//прочитать не получилось, ставим данные уставок по умолчанию
	if (!readDone) {
		setup.setvalue("red_led", 500);
		setup.setvalue("blue_led", 300);
		setup.setvalue("sprinkle_water_temp_max", 28);
		setup.setvalue("sprinkle_water_temp_min", 26);
		setup.setvalue("valve_on", 10);
		setup.setvalue("valve_off", 12);
	}

	data::DataManager sensors = data::DataManager("sensors:", { "red_led", "blue_led",
			"air_temp", "cool_water_in_temp", "cool_water_out_temp", "sprinkle_water_temp",
			"tank_level", "sprinkle_water_flow_instant", "sprinkle_water_flow_average", "air_humidity"});

	int sockfd, newsockfd;
	socklen_t clilen;
	char buffer[BUFFER_SIZE];
	struct sockaddr_in serv_addr, cli_addr;
	int n = -1;

	//инициализация сокета
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) {
		printf("ERROR opening socket\n");
		exit(1);
	}

	bzero((char *) &serv_addr, sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = INADDR_ANY;
	serv_addr.sin_port = htons(PORT);

	if (bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
		printf("ERROR on binding\n");
		exit(1);
	}

	while (true) {
		// ждем подключения клиента - обрабатываем только одно входящее соединение
		listen(sockfd, 5);

		clilen = sizeof(cli_addr);
		newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
		if (newsockfd < 0) {
			printf("ERROR on accept\n");
			exit(1);
		}

		//установка таймаут в 2 секунды
		struct timeval t;
		t.tv_sec = 2;
		t.tv_usec = 0;

		setsockopt(newsockfd,     // Socket descriptor
				SOL_SOCKET, // To manipulate options at the sockets API level
				SO_RCVTIMEO, // Specify the receiving or sending timeouts
				&t, // option values
				sizeof(t));

		while (true) {
			//соединение установлено слушаем  новый сокет
			bzero(buffer, BUFFER_SIZE);
			n = read(newsockfd, buffer, BUFFER_SIZE);
			if (n < 0) {
				printf("ERROR reading from socket\n");
				break;
			}
			if (n == 0) {
				printf("n == 0 client closed connection\n");
				break;
			}

			// разбираем комманды
			string received = string(buffer);
			if (received == requestAllSensorsData) {

				//запрос данных датчиков
				testUpdateSensors(sensors);
				std::string msg = sensors.marshall();
				std::cout << msg << "\n";
				write(newsockfd, msg.c_str(), msg.length());

			} else if (received == requestAllSetupData) {

				// запрос уставок
				std::string msg = setup.marshall();
				std::cout << msg << "\n";
				write(newsockfd, msg.c_str(), msg.length());

			} else {

				printf("new setup received: %s\n", buffer);
				//установка новых уставок
				setup.unmarshall(received);
				std::ofstream setupFile(setupFName);
				if (setupFile.is_open()) {
					//записываем не то что принято а то что по факту установлено
					setupFile << setup.marshall();
					setupFile.close();
				}
			}

		}
		close(newsockfd);
	}
	close(sockfd);

	return 0;
}
