## RCCAR 제어 부분

### Finish --
---
1. 시동 제어 
    - `MQTT` 통신 확인
    - **boot on** 을 받았을 경우
        - `control` 제어 가능
    - **boot off** 를 받았을 경우
        - `control` 제어 불가
        - 이동중이었다면 `stop`
2. 방향 제어
    - `MQTT` 통신 확인
    - `LED` 제어 
        - **stop**, **backward** 인 경우 정지등(정지 led) 둘다 **ON**
        - **right**, **left** 인 경우 하나씩만 **ON**
        - **forward** 의 경우 전체 **OFF**
3. 장애물 탐지
    - `Distance Sensor` 확인
        - **50cm** 이하인 경우 경고 로그 `publish`
        - **30cm** 이하인 경우 `stop()`
4. Buzzer
    - 장애물 감지시 **거리에 비례** 하여 Buzzer가 울리도록
        - 거리가 `가까울수록` 빠르게 울린다.

### Next --
---
1. 기울기센서
    - 3 색 led 이용  . . . .
3. 3색 led 를 이용해 시동, 장애물, 충돌 감지 표현
4. 잠금 여부 
5. 장애물 경로추종
6. 자동차 현재상태 서버
7. streaming 연결 (First!)