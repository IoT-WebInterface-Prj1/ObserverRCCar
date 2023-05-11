import paho.mqtt.client as mqtt
from driveControlPub import resultPub
from Drive import Drive

topic = "rccar/response"
            
class FaultOperError(Exception):    # Exception을 상속받아서 새로운 예외를 만듦
    def __init__(self):
        super().__init__('잘못된 명령어 입력')    
            
def bootControl(client, value):
    global lock
    isBoot = 0
    
    bootTopic = topic + "/boot"
    try:
        if value == "on": 
            isBoot = 1
            # LED Func
        elif value == "off": isBoot = 0
            
        resultPub(bootTopic, client, 1)
        return isBoot
    except Exception as err:
        #pub Err
        resultPub(bootTopic, client)
        print(f"ERR 발생 !  [[{err}]]")
        
def driveControl(isBoot, client, value, Motors):
    driveTopic = topic + "/control"
    
    if isBoot == 0: 
        errMsg = "ERR : Boot is //OFF//"
        resultPub(driveTopic, client, 0, errMsg)
    else:
    # ======= is Boot =========
        try:
            print(f"Drive Val [[{value}]]")
            if value == "forward": Motors.forward()
            elif value == "backward": Motors.backward()
            elif value == "right": Motors.right()
            elif value == "left": Motors.left()
            elif value == "stop": Motors.stop()
            else: raise FaultOperError 
            
            resultPub(driveTopic, client, 1, f"{value} success")
        except Exception as err:
            resultPub(driveTopic, client, 0, f"{value} fail")
            print(err)