#Test funzionamento libreria RPi.GPIO

import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BOARD)
GPIO.setwarnings(False)

relaypins = {"RELAY1":35, "RELAY2":33, "RELAY3":31, "RELAY4":29}

#Accensione pin da 1 a 4
GPIO.setup(relaypins["RELAY1"],GPIO.OUT)
GPIO.output(relaypins["RELAY1"], GPIO.LOW)

print("RELAY1 - ON")
GPIO.output(relaypins["RELAY1"],GPIO.HIGH)
print("RELAY1 - OFF")
GPIO.output(relaypins["RELAY1"],GPIO.LOW)

GPIO.setup(relaypins["RELAY2"],GPIO.OUT)
GPIO.output(relaypins["RELAY2"], GPIO.LOW)

print("RELAY2 - ON")
GPIO.output(relaypins["RELAY2"],GPIO.HIGH)
print("RELAY2 - OFF")
GPIO.output(relaypins["RELAY2"],GPIO.LOW)

GPIO.setup(relaypins["RELAY3"],GPIO.OUT)
GPIO.output(relaypins["RELAY3"], GPIO.LOW)

print("RELAY3 - ON")
GPIO.output(relaypins["RELAY3"],GPIO.HIGH)
print("RELAY3 - OFF")
GPIO.output(relaypins["RELAY3"],GPIO.LOW)

GPIO.setup(relaypins["RELAY4"],GPIO.OUT)
GPIO.output(relaypins["RELAY4"], GPIO.LOW)

print("RELAY4 - ON")
GPIO.output(relaypins["RELAY4"],GPIO.HIGH)
print("RELAY4 - OFF")
GPIO.output(relaypins["RELAY4"],GPIO.LOW)