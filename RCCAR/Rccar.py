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
<<<<<<< HEAD
=======
from gpiozero import RGBLED
from colorzero import Color
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f

import time
import threading

<<<<<<< HEAD
=======
host_id = '172.30.1.120'
port = 1883

>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
class FaultOperError(Exception):    # Exception을 상속받아서 새로운 예외를 만듦
    def __init__(self):
        super().__init__('잘못된 접근 발생')    

class Rccar:
<<<<<<< HEAD
    def __init__(self, left, right, echo, trigger, stop, buzzer, tilt):
=======
    def __init__(self, left, right, echo, trigger, stop, buzzer, tilt, rgbled):
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
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
<<<<<<< HEAD
        # self.state = LED(state)
=======
        self.rgb_led = RGBLED(rgbled[0], rgbled[1], rgbled[2])
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
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
<<<<<<< HEAD
            self.client.connect('172.30.1.18', 1883, 60)
=======
            self.client.connect(host_id, port, 60)
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
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
            
            # if result and self.getBoot(): resultPub(f"{self.topic}/boot", self.client, 0, "Already Boot!")             
            # Set Boot -------------
            # else: 
            self.setBoot(result)
                # resultPub(f"{self.topic}/boot", self.client, 1)
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
            self.tilt = None
        # Boot ON -> Sensor ON
        else:    
            if self.ultrasonic == None: self.ultrasonic = DistanceSensor(self.echo, self.trig) #Echo : 9, Trigger : 10
            if self.tilt == None: self.tilt = Tilt(self.tilt_pin)
<<<<<<< HEAD
=======
            # RGB LED Control
            self.warnningControl("yellow")
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
            
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
<<<<<<< HEAD
=======
            self.warnningControl("orange")
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
            
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
<<<<<<< HEAD
        if color == "r":
            pass
        elif color == "g":
            pass
        elif color == "b":
            pass
=======
        self.rgb_led.color = Color(color)
        self.rgb_led.blink(on_time=0.4, off_time=0.1, n=2)
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
        
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
<<<<<<< HEAD
                self.warnningControl("r")
                self.buzzerControl("crash")
=======
                self.buzzerControl("crash")
                self.warnningControl("red")
                
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
                # resultPub(tiltTopic, self.client, 1, tiltMsg)
        except FaultOperError as err:
            # resultPub(tiltTopic, self, client, 0, "잘못된 접근 - ERR_TILT")
            print(err + " < ERR _ TILT > ")
            
if __name__ == "__main__":    
    leftMotor, rightMotor = (5, 6, 26), (23, 24, 25)
    echo, trig = 9, 10
    stopLEDs = (20, 21)
    buzzer = 2
    tilt = 3
<<<<<<< HEAD
    
    car = Rccar(leftMotor, rightMotor, echo, trig, stopLEDs, buzzer, tilt) 
=======
    rgbled = (17, 27, 22)
    
    car = Rccar(leftMotor, rightMotor, echo, trig, stopLEDs, buzzer, tilt, rgbled) 
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
    
    # --- mqtt 실행 ---
    client = car.getClient()
    client.loop_start()
    
    while True:
        # --- distance sensor ---
        distance = car.getSensor("distance")
        if distance != None: car.detect(distance.distance)
        
        # --- tilt sensor ---
        tilt = car.getSensor("tilt")
        if tilt != None: car.tiltControl()
            
<<<<<<< HEAD
        time.sleep(0.5) # sleep 을 주지 않으면 동작 안함 ! 
=======
        time.sleep(0.5) # sleep 을 주지 않으면 동작 안함 ! 
>>>>>>> 733a5a0d781f3ce9958712d274184754d15f420f
