# Aqua Fourier
Как тренировать модель:
* Кладем файлы в .mp3 или .wav формате в директории вида /data/raw_data/<температура воды>
* Запускаем скрипт /scripts/train_compile.sh
* В директории /data/classifier лежит наилучший классификатор в бинарном формате, его надо положить в корневую директорию сервера

Как собрать Android-приложение:
* Установить android-sdk
* Собрать AquaFourier/build.gradle

Как запустить сервер:
* Запускаем скрипт /server/server.py (python3). 
