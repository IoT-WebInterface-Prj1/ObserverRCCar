import paho.mqtt.client as mqtt
from .driveFunc import drive
from .driveControlPub import resultPub
import threading

topic = "response"
isBoot = 0
lock = threading.Lock()

def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    if rc == 0:
        print("MQTT 연결 성공, drive/control 구독 신청 . . @. @. . ")
        client.subscribe("rccar/drive/#")
    else: print("연결 실패 : ", rc)
    
def on_message(client, userdata, msg):
    global isBoot
    
    value = str(msg.payload.decode())
    _, _, router = msg.topic.split("/")
    
    print(router, value, isBoot)
    if router == "boot": # 시동 관련
        bootTopic = topic + "/boot"
        try:
            if value == "on": 
                lock.acquire()
                isBoot = 1
                lock.release()
                # LED Func
            elif value == "off":
                lock.acquire()
                isBoot = 0
                lock.release()
                
            resultPub(bootTopic, client, 1)
        except Exception as err:
            #pub Err
            resultPub(bootTopic, client)
            pass
    else:
        directTopic = topic + "/direct"
        
        if isBoot == 0: pass #Pub Err
        else:
        # ======= is Boot =========
            result =  drive(value)
            if result: resultPub(directTopic, client, 1)
            else: resultPub(directTopic, client)

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

try:
    client.connect('localhost')
    client.loop_start()
except Exception as err:
    print(f"ERR ! /{err}/")