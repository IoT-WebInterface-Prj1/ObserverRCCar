import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
class Tilt:
    def __init__(self, pin):
        self.GPIO_tilt = pin
        GPIO.setup(self.GPIO_tilt, GPIO.IN, pull_up_down=GPIO.PUD_UP)
        
    def getTilt(self):
        if GPIO.input(self.GPIO_tilt): return 1
        else: return 0