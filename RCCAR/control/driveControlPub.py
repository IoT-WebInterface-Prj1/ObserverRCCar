import paho.mqtt.client as mqtt

def resultPub(_topic, client, res = 0):
    topic = _topic
    message = "fail"
    if res: message = "success"
    
    client.publish(topic, message)