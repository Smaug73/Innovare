import PiRelay
import time


if len(sys.argv) != 3: 
    sys.exit('Errore: inserire on oppure off come parametro')
try:
    nomeScript,comando = sys.argv

    r1 = PiRelay.Relay("RELAY1")
    
    if comando=="on":
        r1.on()

    if comando=="off":
        r1.off()

    '''
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
    '''