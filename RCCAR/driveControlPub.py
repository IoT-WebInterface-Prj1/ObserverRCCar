import paho.mqtt.client as mqtt

topic = "rccar/state/"
def resultPub(_topic, client, res = 0, msg = ""):
    topic = _topic
    if len(msg) == 0:
        message = "fail"
        if res: message = "success"
    else: message = msg
    
    client.publish(topic, message)
    
def serverPub(_topic, msg, client):
    topic += _topic
    client.publish(topic, msg)