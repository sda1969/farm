farm_rpi_ctrl
-------------
Предназначен для запуска на Raspberry PI или на x86-64,
определяется при сборке.
управляет внешним контроллером, подключенном по uart

Инструкция для Линукс - как собирать проект
-------------------------------------------
Возьмите с оффициального репозитория Raspberry на github
компилятор arm-rpi-4.9.3-linux-gnueabihf.tar.gz
и распакуйте  в какую-нибудь папку, например /home/user/bin

пропишите папку /home/user/bin/rpi/tools/arm-bcm2708/arm-linux-gnueabihf/bin
в переменную PATH в файле .profile
export PATH="$HOME/tools/rpi/tools/arm-bcm2708/arm-linux-gnueabihf/bin:$PATH"

Запустите сборку при помощи скриптов:
build_pi.sh - для целевой платформы Линукс на Raspberry PI
build_x86-64.sh - для платформы Линукс на x86-64, удобно для отладки
если повезет то в папке bin/ появится исполняемый файл farm_rpi_ctrl.

скопируйте его на целевую платформу:
scp farm_rpi_ctrl root@<ip адресс платформы>:/

и запустите его на выполненение
ssh root@<ip адресс платформы> farm_rpi_ctrl
