import PiRelay
import time


if len(sys.argv) != 3: 
    sys.exit('Errore: inserire on oppure off come parametro')
try:
    nomeScript,modelName,referencePath = sys.argv

r1 = PiRelay.Relay("RELAY1")
r2 = PiRelay.Relay("RELAY2")
r3 = PiRelay.Relay("RELAY3")
r4 = PiRelay.Relay("RELAY4")

r1.on()
time.sleep(2)
r1.off()
time.sleep(2)

r2.on()
time.sleep(2)
r2.off()
time.sleep(2)

r3.on()
time.sleep(2)
r3.off()
time.sleep(2)

r4.on()
time.sleep(2)
r4.off()
time.sleep(2)