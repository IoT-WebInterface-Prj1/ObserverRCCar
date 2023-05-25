import paho.mqtt.client as mqtt

def resultPub(_topic, client, res = 0, msg = ""):
    topic = _topic
    if len(msg) == 0:
        message = "fail"
        if res: message = "success"
    else: message = msg
    
    client.publish(topic, message)
    
def serverPub(_topic, msg, client):
    topic = "rccar/state/response/"
    
    topic += _topic
    client.publish(topic, msg)