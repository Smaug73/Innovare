import PyRelay
import time

r1 = PyRelay.Relay("RELAY1")
r2 = PyRelay.Relay("RELAY2")
r3 = PyRelay.Relay("RELAY3")
r4 = PyRelay.Relay("RELAY4")

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