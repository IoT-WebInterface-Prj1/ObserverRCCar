## IoT Gateway & Edge Device & Android Programming Prj

* 이도현
* 이연주
* 이지한
* 지석현
---
### 스마트 RC카를 활용한 프로젝트 진행
1. 안드로이드 어플을 이용한 RCCar **이동** 제어
	- **방향키 버튼**을 통한 조작 기능 제공
2. **음성제어**를 통해 차량 제어
3. ***장애물인식*** 및 ***충돌 감지***를 통한 안전장치 동작
	- 충돌 감지 시 이전의 영상을 약 10초정도 녹화하여 업로드 (`블랙박스` 효과)
	- 안드로이드에서 해당 영상들을 직접 확인 가능
4. 차량 화면 `스트리밍` 제공
	- 안드로이드를 통해 차량 주행영상 스트리밍으로 **실시간**으로 확인 가능
---
### 실행방법
1. `ipconfig(window)` or `ifconfig(Linux)` 명령어를 통해 현재 자신의 **ip** 주소 확인
	- `MQTT` 통신을 위한 작업
2. 해당 **ip** 를 다음의 파일들의 `HostURL` 로 지정
	- Rpi와 안드로이드 에서 동일한 **Wifi** 에 접속하여 동일한 `Host` 로 할당해야 한다 !
	- [./RCCAR/Rccar.py](./RCCAR/Rccar.py) 의 `host_id`
	- [./iot_server/mjpeg/picam.py](./iot_server/mjpeg/picam.py) 의 `self.host_id`
	- [./Android/app/src/main/java/com/example/myapplication/MainActivity.kt](./Android/app/src/main/java/com/example/myapplication/MainActivity.kt) 의 `brokerUrl`
	- [./Android/app/src/main/java/com/example/myapplication/Control.kt](./Android/app/src/main/java/com/example/myapplication/Control.kt) 의 `brokerUrl`
3. 다음의 두 파일을 실행 (Rpi)
	- [./RCCAR/Rccar.py](./RCCAR/Rccar.py)
	- [./iot_server/manage.py](./iot_server/manage.py) >> `python manage.py 0.0.0.0:8000` 을 통해 실행
		- [./RCCAR/config/settings.py](./RCCAR/config/settings.py) 에서  `ALLOWED_HOSTS` 에 **IP** 추가해야한다
4. 안드로이드 어플을 `build` 후 다운 혹은 업로드된 `apk` 파일을 통해 실행