# MQTT 통신
import paho.mqtt.client as mqtt
from driveControlSub import *
from driveControlPub import *

# Seonsors
from Drive import Drive
from LEDS import LEDS
from gpiozero import  DistanceSensor
from gpiozero import LED
from gpiozero import Buzzer

import time
import threading

class Rccar:
    def __init__(self, left, right, echo, trigger, stop):
        # Sensors ==============
        # Motor --
        self.motorDrive = Drive(left, right)
        # Ultrasonic --
        self.ultrasonic = None
        self.echo = echo
        self.trig = trigger
        # LEDS --
        self.stop_led = LEDS(stop)
        # self.state = LED(state)
        # =====================
        
        # MQTT --------------
        self.client = mqtt.Client()
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        # --------------------
        
        # State Val ------------
        self.states = {
            'stop' : 0, 
            'forward' : 1, 
            'backward' : 2, 
            'left' : 3,
            'right' : 4
        }
        
        self.isBoot = 0
        self.nowControl = self.states['stop']
        # -------------------
        self.start()
        
    def start(self):
        try:
            self.client.on_connect = self.on_connect
            self.client.on_message = self.on_message
            self.client.connect('localhost')
        except Exception as err:
            print(f"ERR ! /{err}/")
            
    def getClient(self):
        return self.client
        
    def on_connect(self, client, userdata, flags, rc):
        print("Connected with result code " + str(rc))
        if rc == 0:
            print("MQTT 연결 성공, rccar/drive/# 구독 신청 . . @. @. . ")
            client.subscribe("rccar/drive/#")
        else: print("연결 실패 : ", rc)
        
    def on_message(self, client, userdata, msg):
        value = str(msg.payload.decode())
        _, _, router = msg.topic.split("/")
        
        if router == "boot": # 시동 관련
            result = bootControl(self.client, value)
            
            # Set Boot -------------
            self.setBoot(result)
            # ----------------------
            
            print(f"Boot State -> [[{self.getBoot()}]]")
        else:
            bootState = self.getBoot()
            result = driveControl(bootState, client, value, self.motorDrive)  
            
            # Set State -------------
            self.setState(result)
            # ----------------------
            self.ledControl()
    
    # Setter
    def setBoot(self, result):
        lock = threading.Lock()
        
        # Setting ----------
        lock.acquire()
        self.isBoot = result
        lock.release()
        # -----------------
        
        # Boot OFF -> Drive Stop, Sensor OFF
        if (result == 0):
            self.motorDrive.stop()
            self.ultrasonic = None
        # Boot ON -> Sensor ON
        else:    
            self.ultrasonic = DistanceSensor(self.echo, self.trig) #Echo : 9, Trigger : 10
            
    def setState(self, result):
        lock = threading.Lock()
        lock.acquire()
        self.nowControl = self.states[result]
        lock.release()
        
    # Getter
    def getBoot(self):
        return self.isBoot
    
    def getState(self):
        return self.nowControl
    
    def getSensor(self, sensor):
        if (sensor == "distance"): return self.ultrasonic
    
    # Control ---
    def detect(self, dist):
        detectTopic = "rccar/response/detect"
        detectMsg = f"Object Detect!! //Distance : {dist * 100}(cm)"
        
        resultPub(detectTopic, self.client, 1, detectMsg)
        if (dist < 0.3): self.motorDrive.stop()            
        
        # Buzzer Control
        self.buzzerControl(dist)
        
    def ledControl(self):
        now = self.getState()
        
        if (now == 0 or now == 2): self.stop_led.on() # stop , backward
        elif (now == 3): self.stop_led.firstOn() # left
        elif (now == 4): self.stop_led.secondOn()
        else: self.stop_led.off() # forward or ect... led off
        
    def buzzerControl(self, dist):
            pass
            
if __name__ == "__main__":    
    car = Rccar((5, 6, 26), (23, 24, 25), 9, 10, (20, 21))
    
    # --- mqtt 실행 ---
    client = car.getClient()
    client.loop_start()
    
    while True:
        # --- distance sensor ---
        distance = car.getSensor("distance")
        
        if distance != None:
            if (distance.distance < 0.5):
                car.detect(distance.distance)
            
        time.sleep(0.01) # sleep 을 주지 않으면 동작 안함 ! 