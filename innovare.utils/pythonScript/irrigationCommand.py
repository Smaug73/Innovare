import PiRelay
import time


if len(sys.argv) != 3:
    sys.exit('Errore: inserire on oppure off come parametro')
nomeScript,comando,id_rel = sys.argv

r1 = PiRelay.Relay("RELAY"+id_rel)

if comando=="on":
        r1.on()

if comando=="off":
        r1.off()