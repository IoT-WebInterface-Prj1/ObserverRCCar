from Motor import Motor

class Drive:
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
        
    def stop(self):
        self.motorLeft.shortBreak()
        self.motorRight.shortBreak()