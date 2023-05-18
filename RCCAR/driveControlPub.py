import paho.mqtt.client as mqtt

def resultPub(_topic, client, res = 0, msg = ""):
    topic = _topic
    if len(msg) == 0:
        message = "fail"
        if res: message = "success"
    else: message = msg
    
    client.publish(topic, message)