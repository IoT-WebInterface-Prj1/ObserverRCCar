import paho.mqtt.client as mqtt

def resultPub(_topic, client, res = 0):
    topic = _topic
    message = "off"
    if res: message = "on"
    
    client.publish(topic, message)