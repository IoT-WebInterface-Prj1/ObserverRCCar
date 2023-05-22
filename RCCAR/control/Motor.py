import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BCM)
class Motor:
    def __init__(self, pinNum):
        self.GPIO_RP = pinNum[0]
        self.GPIO_RN = pinNum[1]
        self.GPIO_EN = pinNum[2]

        GPIO.setup(self.GPIO_RP, GPIO.OUT)
        GPIO.setup(self.GPIO_RN, GPIO.OUT)
        GPIO.setup(self.GPIO_EN, GPIO.OUT)
        
    def __del__(self):
        GPIO.cleanup()
        
    def bootOn(self):
        GPIO.output(self.GPIO_EN, True)
        
    def bootOff(self):
        GPIO.output(self.GPIO_EN, False)
        
    def forward(self):
        self.bootOn()
        GPIO.output(self.GPIO_RP, True)
        GPIO.output(self.GPIO_RN, False)
    
    def backward(self):
        self.bootOn()
        GPIO.output(self.GPIO_RP, False)
        GPIO.output(self.GPIO_RN, True)
      
    def shortBreak(self):
        GPIO.output(self.GPIO_RP, True)
        GPIO.output(self.GPIO_RN, True)
    
if __name__ == "__main__":
    motorLeft = Motor(5, 6, 26)
    motorRight = Motor(23, 24, 25)
    
    while True:
        print("모터 회전 >> FORWARD")
        motorLeft.forward()
        motorRight.forward()
        time.sleep(5)
        
        print("모터 회전 >> BACKWARD")
        motorLeft.backward()
        motorRight.backward()
        time.sleep(5)