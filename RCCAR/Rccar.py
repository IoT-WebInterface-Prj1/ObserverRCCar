# MQTT 통신
import paho.mqtt.client as mqtt
from driveControlSub import *
from driveControlPub import *

# Seonsors
from Drive import Drive
from LEDS import LEDS
from Tilt import Tilt
from gpiozero import  DistanceSensor
from gpiozero import LED
from gpiozero import Buzzer
from gpiozero import RGBLED
from colorzero import Color

import time
import threading

host_id = '172.30.1.120'
port = 1883

class FaultOperError(Exception):    # Exception을 상속받아서 새로운 예외를 만듦
    def __init__(self):
        super().__init__('잘못된 접근 발생')    

class Rccar:
    def __init__(self, left, right, echo, trigger, stop, buzzer, tilt, rgbled):
        self.topic = "rccar/response"
        # Sensors ==============
        # Motor --
        self.motorDrive = Drive(left, right)
        # Ultrasonic --
        self.ultrasonic = None
        self.echo = echo
        self.trig = trigger
        # LEDS --
        self.stop_led = LEDS(stop)
        self.rgb_led = RGBLED(rgbled[0], rgbled[1], rgbled[2])
        # Buzzer --
        self.buzzer = Buzzer(buzzer)
        # Tilt --
        self.tilt = None
        self.tilt_pin = tilt
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
        self.isBuzzerOn = 0
        self.nowControl = self.states['stop']
        # -------------------
        self.start()
        
    def start(self):
        try:
            self.client.on_connect = self.on_connect
            self.client.on_message = self.on_message
            self.client.connect(host_id, port, 60)
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
        _, mode, router = msg.topic.split("/")
        if mode == "drive":
            if router == "boot": # 시동 관련
                result = bootControl(self.client, value)
                
                if result and self.getBoot(): resultPub(f"{self.topic}/boot", self.client, 0, "Already Boot!")             
                # Set Boot -------------
                else: 
                    self.setBoot(result)
                    resultPub(f"{self.topic}/boot", self.client, 1)
                # ----------------------
                
                print(f"Boot State -> [[{self.getBoot()}]]")
            elif router == "control":
                bootState = self.getBoot()
                result = driveControl(bootState, client, value, self.motorDrive)  
                
                # Set State -------------
                self.setState(result)
                # ----------------------
                self.ledControl()
        elif mode == "state":
            if router == "boot": 
                msg = "OFF"
                if (self.getBoot): msg = "ON"
                serverPub("boot", msg, self.client)
    
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
            self.tilt = None
            serverPub("boot", "OFF", self.client)
        # Boot ON -> Sensor ON
        else:    
            if self.ultrasonic == None: self.ultrasonic = DistanceSensor(self.echo, self.trig) #Echo : 9, Trigger : 10
            if self.tilt == None: self.tilt = Tilt(self.tilt_pin)
            # RGB LED Control
            self.warnningControl("yellow")
            serverPub("boot", "ON", self.client)
            
    def setState(self, result):
        lock = threading.Lock()
        lock.acquire()
        self.nowControl = self.states[result]
        lock.release()
        
    def setBuzzerOn(self, result):
        lock = threading.Lock()
        lock.acquire()
        if (result == 0 and self.isBuzzerOn == 1): self.buzzer.off() # 부저 상태를 0로 바꾸고 싶을때, 부저가 켜져있는 경우 = 부저를 끄려고 할 때
        self.isBuzzerOn = result
        lock.release()
        
    # Getter
    def getBoot(self):
        return self.isBoot
    
    def getState(self):
        return self.nowControl
    
    def getBuzzerOn(self):
        return self.isBuzzerOn
    
    def getSensor(self, sensor):
        if (sensor == "distance"): return self.ultrasonic
        elif (sensor == "tilt"): return self.tilt
    
    # Control ---
    def detect(self, dist):
        if (dist < 0.5):
            detectTopic = self.topic + "/detect"
            detectMsg = f"Object Detect!! //Distance : {dist * 100}(cm)"
            self.warnningControl("orange")
            
            # resultPub(detectTopic, self.client, 1, detectMsg)
            if (dist < 0.3): self.motorDrive.stop()            
            
            # Buzzer Control
            self.buzzerControl("on", dist)
        else: self.buzzerControl("off")
        
    def ledControl(self):
        now = self.getState()
        
        if (now == 0 or now == 2): self.stop_led.on() # stop , backward
        elif (now == 3): self.stop_led.firstOn() # left
        elif (now == 4): self.stop_led.secondOn()
        else: self.stop_led.off() # forward or ect... led off
        
    def warnningControl(self, color):
        self.rgb_led.color = Color(color)
        self.rgb_led.blink(on_time=0.4, off_time=0.1, n=2)
        
    def buzzerControl(self, op, dist = 0):
        # op : {'on' : ultrasonic Detect, 'off' : ultrasonic Not Detect, 'crash' : tilt Detect }
        if (op == "on"): 
            self.setBuzzerOn(1)
            self.buzzer.beep(on_time =  0.4* dist, off_time = 0.1 * dist)
        elif (op == "off"): self.setBuzzerOn(0)
        elif (op == 'crash') : 
            self.setBuzzerOn(1)
            self.buzzer.beep(on_time =  0.1, off_time = 0.1, n = 2)
        
    def tiltControl(self):
        tiltTopic = self.topic + "/tilt"
        tiltMsg = "Crash Occur!!"
        
        try :
            if (self.tilt.getTilt()): 
                self.buzzerControl("crash")
                self.warnningControl("red")
                
                resultPub(tiltTopic, self.client, 1, tiltMsg)
        except FaultOperError as err:
            resultPub(tiltTopic, self, client, 0, "잘못된 접근 - ERR_TILT")
            print(err + " < ERR _ TILT > ")
            
if __name__ == "__main__":    
    leftMotor, rightMotor = (5, 6, 26), (23, 24, 25)
    echo, trig = 9, 10
    stopLEDs = (20, 21)
    buzzer = 2
    tilt = 3
    rgbled = (17, 27, 22)
    
    car = Rccar(leftMotor, rightMotor, echo, trig, stopLEDs, buzzer, tilt, rgbled) 
    
    # --- mqtt 실행 ---
    client = car.getClient()
    client.loop_start()
    
    while True:
        # --- distance sensor ---
        distance = car.getSensor("distance")
        if distance != None: car.detect(distance.distance)
        
        # --- tilt sensor ---r
        tilt = car.getSensor("tilt")
        if tilt != None: car.tiltControl()
            
        time.sleep(0.5) # sleep 을 주지 않으면 동작 안함 ! 