import PiRelay
import time

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