from gpiozero import LED

class LEDS:
    def __init__(self, pins):
        self.led_1 = LED(pins[0])
        self.led_2 = LED(pins[1])
        
        self.off()
        
    def on(self):
        self.led_1.on()
        self.led_2.on()
        
    def off(self):
        self.led_1.off()
        self.led_2.off()
        
    def firstOn(self):
        self.led_1.on()
        self.led_2.off()
        
    def secondOn(self):
        self.led_1.off()
        self.led_2.on()
        
    def toggle(self):
        self.led_1.toggle()
        self.led_2.toggle()
        
    def blink(self):
        self.led_1.blink()
        self.led_2.blink()