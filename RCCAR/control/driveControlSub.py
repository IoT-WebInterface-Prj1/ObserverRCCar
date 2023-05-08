import paho.mqtt.client as mqtt
# from .models import
# from django.utls import timezone
from .driveFunc import drive
from .driveControlPub import resultPub
import threading

topic = ""
isBoot = 0
lock = threading.Lock()

def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    if rc == 0:
        print("MQTT 연결 성공, drive/control 구독 신청 . . @. @. . ")
        client.subscribe("rccar/drive/#")
    else: print("연결 실패 : ", rc)
    
def on_message(client, userdata, msg):
    value = str(msg.payload.decode())
    _, _, router = msg.topic.split("/")
    
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
                
            # resultpub(bootTopic, client, 1)
        except Exception as err:
            #pub Err
            # resultpub(bootTopic, client)
            pass
    else:
        directTopic = topic + "/direct"
        pass
        if isBoot == 0: pass #Pub Err
        #    result =  drive(direct)
        # if result:
        #     resultpub(directTopic, client, 1)
        # else: resultpub(directTopic, client)

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

try:
    client.connect('localhost')
    client.loop_start()
except Exception as err:
    print(f"ERR ! /{err}/")