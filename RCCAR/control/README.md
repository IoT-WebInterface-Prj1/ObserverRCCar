### Yannju_ REDME.md

1. 시동 켜진 경우를 확인
   - ~~`Json` 으로 접근하여 상태 확인~~
   - `MQTT` 로 받아 오기
2. 이동 제어를 `MQTT` 로 받아오기
   - **rccar/drive/control** 로 .. 생각하고 있다 . . .   
   - **direct** 를 받아오고 `drive` 로 넘기기
     - 이때 **result**  를 받아 제대로 적용 되었는지 확인
     - 적용 여부 처리 필요
   - **direct** 가 제대로 되었다면 어플리케이션에 결과 **pub**