import io
import time
import numpy as np
from picamera import PiCamera
import picamera.array
import cv2   
from datetime import datetime

class MJpegStreamCam:

    def __init__(self, framerate=25, width=640, height=480):
        self.size = (width, height)
        self.framerate = framerate

        self.camera = PiCamera()
        self.camera.rotation = 180
        self.camera.resolution = self.size
        self.camera.framerate = self.framerate

    def __del__(self):
        self.camera.close()

    # def snapshot(self):
    #     #ret, image = self.camera.read()
    #     #encode_param=[int(cv2.IMWRITE_JPEG_QUALITY), 80]
    #     #is_success, jpg = cv2.imencode('.jpeg', image, encode_param)

    #     frame = io.BytesIO()
    #     self.camera.capture(frame, 'jpeg', use_video_port=True)
    #     frame.seek(0)
    #     return frame.getvalue() # byte 배열 리턴 
    
    # detected - none detectd 화면 표시 / 초음파 센서 거리 화면 표시
    # 화면 녹화 

    def __iter__(self):
        encode_param=[int(cv2.IMWRITE_JPEG_QUALITY), 80]
        face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_fullbody.xml')

        with picamera.array.PiRGBArray(self.camera, size=self.size) as stream:
            while True:
                self.camera.capture(stream, format='bgr', resize=self.size, use_video_port=True)
                frame = stream.array
                gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
                faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=4, minSize=(80, 80))
                
                text = "none" if len(faces) == 0 else "detected"

                cv2.putText(frame, text, (int(self.size[0]/2.5), 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)
                
                for (x,y,w,h) in faces:
                    cv2.rectangle(frame,(x,y),(x+w,y+h),(0,0,255),2)
                
                
                is_success, buffer = cv2.imencode(".jpg", frame, encode_param)

                yield (b'--myboundary\n'
                    b'Content-Type: image/jpeg\n'
                    b'Content-Length: ' + f"{len(buffer)}".encode() + b'\n'
                    b'\n' + buffer.tobytes() + b'\n')
                
                stream.truncate(0)
