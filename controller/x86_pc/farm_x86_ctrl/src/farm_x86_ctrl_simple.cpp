//============================================================================
// Name        : farm_server_x86_simple.cpp
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
#include <arpa/inet.h>

using namespace std;

namespace {

constexpr int BUFFER_SIZE = 65536;
constexpr int PORT = 8080;
string requestAllSensorsData = "get_sensors;\n";
string requestAllSetupData = "get_setup;\n";
string ip = "127.0.0.1";
string setup = "setup:blue_led=301,red_led=506,sprinkle_water_temp_max=27,sprinkle_water_temp_min=25,valve_off=15,valve_on=10;\n";
string sensors = "sensors:air_humidity=66,air_temp=69,blue_led=62,cool_water_in_temp=26,cool_water_out_temp=17,red_led=75,sprinkle_water_flow_average=54,sprinkle_water_flow_instant=32,sprinkle_water_temp=23,tank_level=2;\n";

}

int main(int argc, char ** argv) {

	if (argc == 2) {
		ip = string(argv[1]);
	}

	//пытаемся прочитать данные уставок из файла
	std::string setupFName = "setup.txt";
	std::ifstream setupFile(setupFName);
	if (setupFile.is_open()) {
		std::string line;
		//все хранится только в одной строке
		if (getline(setupFile, line)) {
			setup = line;
		}
		setupFile.close();
	}

	int sockfd;
	socklen_t socklen;
	char buffer[BUFFER_SIZE];
	struct sockaddr_in comm_addr;
	int n = -1;

	while (true) {

		//инициализация сокета
		sockfd = socket(AF_INET, SOCK_STREAM, 0);
		if (sockfd < 0) {
			printf("ERROR opening socket\n");
			exit(1);
		}

		bzero((char *) &comm_addr, sizeof(comm_addr));
		comm_addr.sin_family = AF_INET;
		comm_addr.sin_addr.s_addr = inet_addr(ip.c_str());
		comm_addr.sin_port = htons(PORT);
		socklen = sizeof(comm_addr);

		printf("connecting to commutator \n");
		if (connect(sockfd, (struct sockaddr *) &comm_addr, socklen) == -1) {
			printf("ERROR connecting to commutator\n");
			sleep(1);
			continue;
		}
		printf("connected to commutator %08X\n",
				htonl(comm_addr.sin_addr.s_addr));

		while (true) {
			//соединение установлено слушаем сокет
			bzero(buffer, BUFFER_SIZE);
			printf("read from socket \n");
			n = read(sockfd, buffer, BUFFER_SIZE);
			if (n < 0) {
				printf("ERROR reading from socket\n");
				close(sockfd);
				break;
			}
			if (n == 0) {
				printf("n == 0 client closed connection\n");
				close(sockfd);
				break;
			}
			printf("receive:%s\n", buffer);
			// разбираем комманды
			string received = string(buffer);
			if (received == requestAllSensorsData) {
				printf("sensors\n");
				printf("%s\n", sensors.c_str());
				//запрос данных датчиков
				write(sockfd, sensors.c_str(), sensors.length());
			} else if (received == requestAllSetupData) {
				printf("setups\n");
				printf("%s\n", setup.c_str());
				// запрос уставок
				write(sockfd, setup.c_str(), setup.length());
			} else {
				printf("new setup received: %s\n", buffer);
				//установка новых уставок
				setup=received;
				std::ofstream setupFile(setupFName);
				if (setupFile.is_open()) {
					setupFile << setup <<"\n";
					setupFile.close();
				}
			}

		}
	}
	return 0;
}
