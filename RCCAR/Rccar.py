# MQTT 통신
import paho.mqtt.client as mqtt
from driveControlSub import *
from driveControlPub import *

# Seonsors
from Drive import Drive
from gpiozero import  DistanceSensor

import time
import threading

class Rccar:
    def __init__(self, left, right):
        # Sensors ==============
        # Motor
        self.motorDrive = Drive(left, right)
        #Ultrasonic
        self.ultrasonic = DistanceSensor(16, 12)
        self.ultrasonic.threshold_distance = 0.5
        self.ultrasonic.when_in_range = self.detect
        # =====================
        
        # MQTT --------------
        self.client = mqtt.Client()
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        
        self.isBoot = 0
        self.lock = threading.Lock()
        # -------------------
        
        self.strat()
        
    def start(self):
        try:
            self.client.connect('localhost')
            self.client.loop_start()
        except Exception as err:
            print(f"ERR ! /{err}/")
        
    def on_connect(self, client, userdata, flags, rc):
        print("Connected with result code " + str(rc))
        if rc == 0:
            print("MQTT 연결 성공, drive/control 구독 신청 . . @. @. . ")
            client.subscribe("rccar/drive/#")
        else: print("연결 실패 : ", rc)
        
    def on_message(self, client, userdata, msg):
        value = str(msg.payload.decode())
        _, _, router = msg.topic.split("/")
        
        if router == "boot": # 시동 관련
            result = bootControl(self.client, value)
            self.lock.acquire()
            self.setBoot(result)
            self.lock.release()
        else:
            bootState = self.getBoot()
            driveControl(bootState, client, value, self.motorDrive)
    
    def setBoot(self, result):
        self.isBoot = result
        
    def getBoot(self):
        return self.isBoot
    
    def detect(self):
        dist = self.ultrasonic.distance 
        detectTopic = self.topic + "/detect"
        detectMsg = f"Object Detect!! //Distance : {dist * 100}(cm)"
        
        resultPub(detectTopic, self.client, 1, detectMsg)
        if (dist < 0.3): self.stop()            
    
if __name__ == "__main__":
    car = Rccar((5, 6, 26), (23, 24, 25))