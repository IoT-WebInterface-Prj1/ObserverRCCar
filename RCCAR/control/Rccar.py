from Motor import Motor
import time

class Rccar:
    def __init__(self, left, right):
        self.motorLeft = Motor(left)
        self.motorRight = Motor(right)
    
    def forward(self):
        self.motorLeft.forward()        
        self.motorRight.forward() 
        
    def backward(self):
        self.motorLeft.backward()        
        self.motorRight.backward()     
        
    def right(self):
        self.motorLeft.forward()
        self.motorRight.shortBreak()
    
    def left(self):
        self.motorLeft.shortBreak()
        self.motorRight.forward()
    
    
if __name__ == "__main__":
    car = Rccar((5, 6, 26), (23, 24, 25))
    
    while True:
        print("모터 회전 >> FORWARD")
        car.forward()
        time.sleep(5)
        
        print("모터 회전 >> BACKWARD")
        car.backward()
        time.sleep(5)
        
        print("모터 회전 >> RIGHT")
        car.right()
        time.sleep(5)
        
        print("모터 회전 >> LEFT")
        car.backward()
        time.sleep(5)