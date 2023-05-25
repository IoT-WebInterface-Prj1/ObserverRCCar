import paho.mqtt.client as mqtt
from driveControlPub import resultPub
from Drive import Drive

topic = "rccar/response"
def bootControl(client, value):
    global lock
    isBoot = 0
    
    bootTopic = topic + "/boot"
    try:
        if value == "on": 
            isBoot = 1
            # LED Func
        elif value == "off": isBoot = 0
        
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
            else: raise  
            
            resultPub(driveTopic, client, 1, f"{value} success")
            
            return value
        except Exception as err:
            resultPub(driveTopic, client, 0, f"{value} fail")
            print(err)
            return "err"