import requests
import threading
import subprocess
import time
import os

MAX_FILES = 2  # 최대 파일 개수 (current + recent)
RECORD_INTERVAL = 10  # 녹화 간격 (초)

# 녹화할 파일 이름과 경로
filename_template = "recorded_{timestamp}.avi"
filepath_template = "/home/pi/workspace/iot_server/media/{filename}"

# 녹화 중인 파일과 최근 녹화 파일 경로 저장을 위한 리스트
recordings = []

# 녹화 중인 파일이 생성될 때까지 대기하는 이벤트
file_created = threading.Event()

def start_recording():
    # 현재 시간으로 파일 이름 생성
    timestamp = int(time.time())
    filename = filename_template.format(timestamp=timestamp)
    filepath = filepath_template.format(filename=filename)

    # 리스트가 꽉 찼다면 새로운 파일 저장을 위해 리스트에서 가장 오래된 파일 삭제
    if len(recordings) == MAX_FILES:
        oldest_file = recordings.pop(0)
        os.remove(oldest_file)

    # 새로운 파일 저장
    recordings.append(filepath)

    # 스트리밍 영상 10초 간격 녹화 시작
    # command = f"ffmpeg -f video4linux2 -i /dev/video0 -r 30 -t {RECORD_INTERVAL} {filepath}"
    command = f"ffmpeg -i http://127.0.0.1:8000/mjpeg/?mode=stream -t {RECORD_INTERVAL} {filepath}"
    subprocess.run(command.split(), check=True)

    # 녹화 중인 파일이 생성됨을 알림
    file_created.set()

def upload_file(filepath):
    # 파일을 업로드할 url
    url = "http://127.0.0.1:8000/upload"
    
    with open(filepath, 'rb') as f:
        files = {"file": f}

        try:
            response = requests.post(url, files=files, timeout=10)
            response.raise_for_status()
            print(f"{filepath} 업로드 완료")
        except requests.exceptions.RequestException as e:
            print(f"{filepath} 업로드 실패: {e}")

def check_upload_condition():
    pass
    # 만약 기울기 센서에 변화가 있다면 True 반환
    # 아니라면 False 반환 


def manage_recordings():
    try:
        while True:
            start_recording()

            # 업로드 조건이 되면 녹화 중인 파일과 최근 녹화 파일 1개 업로드
            if check_upload_condition():
                # 현재 진행중인 녹화 종료하고 저장하는 코드 추가
                current_recording = recordings.pop()
                recent_recording = recordings.pop(0)
                #ffmpeg를 강제 종료해도 진행중이던 녹화는 저장이 된다고 함
                os.system("pkill ffmpeg") 
                # ffmpeg 프로세스가 종료되기를 기다림
                time.sleep(1)

                # 최근 녹화 파일과 현재 녹화 파일 업로드
                upload_file(recent_recording)
                upload_file(current_recording)

                # 녹화 파일 리스트 초기화
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
# django 서버 종료시 같이 종료 -> 유효??
t.setDaemon(True)
t.start()