import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BOARD)
GPIO.setwarnings(False)

class Relay:
    ''' Class to handle Relay
    Arguments:
    relay = string Relay label (i.e. "RELAY1","RELAY2","RELAY3","RELAY4")
    '''
    relaypins = {"RELAY1":35, "RELAY2":33, "RELAY3":31, "RELAY4":29}


    def __init__(self, relay):
        self.pin = self.relaypins[relay]
        self.relay = relay
        GPIO.setup(self.pin,GPIO.OUT)
        GPIO.output(self.pin, GPIO.LOW)

    def on(self):
        print(self.relay + " - ON")
        GPIO.output(self.pin,GPIO.HIGH)

    def off(self):
        print(self.relay + " - OFF")
        GPIO.output(self.pin,GPIO.LOW)



r1 = Relay("RELAY1")
r2 = Relay("RELAY2")
r3 = Relay("RELAY3")
r4 = Relay("RELAY4")

r1.on()
time.sleep(0.5)
r1.off()
time.sleep(0.5)

r2.on()
time.sleep(0.5)
r2.off()
time.sleep(0.5)

r3.on()
time.sleep(0.5)
r3.off()
time.sleep(0.5)

r4.on()
time.sleep(0.5)
r4.off()
time.sleep(0.5)