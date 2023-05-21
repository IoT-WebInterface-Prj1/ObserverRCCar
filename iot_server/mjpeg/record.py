import requests
import threading
import subprocess
import time
import os
import paho.mqtt.client as mqtt

host_id = '172.30.1.62'
port = 1883
            
def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    if rc == 0:
        print("MQTT 연결 성공, rccar/drive/# 구독 신청 . . @. @. . ")
        client.subscribe("rccar/response/#")
    else: print("연결 실패 : ", rc)

    
def on_message(client, userdata, msg):
    value = str(msg.payload.decode())
    _, _, router = msg.topic.split("/")
    if router=="tilt":
        print(value)
        check_upload_condition(True)


# MQTT --------------
client = mqtt.Client()
# --------------------

try:
    client.on_connect = on_connect
    client.on_message = on_message
    client.connect(host_id, port, 60)
    client.loop_start()
except Exception as err:
    print(f"ERR ! /{err}/")


MAX_FILES = 2  # 최대 파일 개수: 여유롭게 5개 
RECORD_INTERVAL = 5  # 녹화 간격 (초)

# 녹화할 파일 이름과 경로
filename_template = "recorded_{timestamp}.mp4"
filepath_template = "/home/pi/workspace/iot_server/media/{filename}"

# 녹화 중인 파일과 최근 녹화 파일 경로 저장을 위한 리스트
recordings = []

# 녹화 중인 파일이 생성될 때까지 대기하는 이벤트
file_created = threading.Event()

def start_recording():
    global recordings
    # 현재 시간으로 파일 이름 생성
    timestamp = int(time.time())
    filename = filename_template.format(timestamp=timestamp)
    filepath = filepath_template.format(filename=filename)

    # 리스트가 꽉 찼다면 새로운 파일 저장을 위해 리스트에서 가장 오래된 파일 삭제
    if len(recordings) == MAX_FILES:
        oldest_file = recordings.pop(0)
        if oldest_file:
            os.remove(oldest_file)

    # 새로운 파일 저장
    recordings.append(filepath)

    # 스트리밍 영상 10초 간격 녹화 시작
    # command = f"ffmpeg -f video4linux2 -i /dev/video0 -r 30 -t {RECORD_INTERVAL} {filepath}"
    # command = f"ffmpeg -i http://localhost:8000/mjpeg/?mode=stream -t {RECORD_INTERVAL} {filepath}"
    # ffmpeg -i http://localhost:8000/mjpeg/?mode=stream -t 5 /home/pi/Videos/test1.mp4
    command = f'ffmpeg -i http://localhost:8000/mjpeg/?mode=stream -c:v copy -c:a copy {filepath}'
    # os.system(command)
    subprocess.call(command, shell=True)

    # 녹화 중인 파일이 생성됨을 알림
    file_created.set()

def upload_file(filepath):
    # 파일을 업로드할 url
    url = "http://127.0.0.1:8000/mjpeg/upload/"
    file_name = filepath.split("/")[-1]

    with open(filepath, 'rb') as f:
        data = {
            'file_name': file_name
        }
        files = {'sec_file': (file_name, f, 'video/mp4')}
        # files = {'sec_file': f}

        try:
            response = requests.post(url, data=data, files=files, timeout=10)
            response.raise_for_status()
            print(f"{filepath} 업로드 완료")
        except requests.exceptions.RequestException as e:
            print(f"{filepath} 업로드 실패: {e}")

def check_upload_condition(condition=False):
    if condition == True:
        print("흔들렸습니다!!!!!")
    return condition


def manage_recordings():
    try:
        while True:
            print("꼬우!")
            start_recording()

            # 업로드 조건이 되면 녹화 중인 파일과 최근 녹화 파일 1개 업로드
            if check_upload_condition():
                isRecordings = 0

                # 현재 진행중인 녹화 종료하고 저장하는 코드 추가
                os.system("pkill ffmpeg") 
                current_recording = recordings.pop()
                if len(recordings):
                    isRecordings = 1
                    recent_recording = recordings.pop(0)
                time.sleep(1)

                # 최근 녹화 파일과 현재 녹화 파일 업로드
                if isRecordings == 1: upload_file(recent_recording)
                upload_file(current_recording)

                # 녹화 파일 리스트 초기화
                os.remove(current_recording)
                os.remove(recent_recording)
                recordings.clear()
                file_created.clear()


            time.sleep(RECORD_INTERVAL)
    except KeyboardInterrupt:
        print("녹화 관리 스레드가 중지되었습니다.")
    finally:
        # 녹화 종료 후 열려 있는 ffmpeg 프로세스 강제 종료
        os.system("pkill ffmpeg")

# manage_recordings() 함수를 스레드로 실행
t = threading.Thread(target=manage_recordings)
t.start()