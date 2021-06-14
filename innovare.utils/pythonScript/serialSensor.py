import serial
import sys
import time

try:
    idSens = sys.argv[1]
    #print(idSens)

    sr = serial.Serial('/dev/ttyACM0',1200)
    sr.write(bytes(idSens+'M!','ascii'))
    time.sleep(3)
    #print(sr.read_all())

    sr.write(bytes(idSens+'D0!','ascii'))
    time.sleep(3)
    res=sr.read_all()
    #print(str(res))
    ris=str(res).split('+')
    val1=ris[1]
    #print(ris)
    print(ris[2].split("\\r\\n'")[0])
    print(val1)
except :
    sys.exit(1)