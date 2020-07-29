import subprocess
import sys
from datetime import datetime
import yagmail
import time
import schedule

pathLog="/home/pi/LogFileVproWeather/"
receiver="stefanorondinella.uni@gmail.com"
body="Test mail"



def saveOutput(o):
    now = datetime.now()
    nameFile=now.strftime("%d_%m_%_Y_%H_%M")+'.log'
    f=open(pathLog+nameFile, "x")
    f.write(o)
    return nameFile

#Funzione per invio mail
def sendmail(file):
    print(file)
    yag=yagmail.SMTP("mail.progetto.prova@gmail.com","p4$$word")
    yag.send(to=receiver,subject="PROGETTO INNOVARE WEATHERSTATION LOG",contents=body,attachments=pathLog+file)
    yag.send(to="stefanorondinella.uni@gmail.com",subject="PROGETTO INNOVARE WEATHERSTATION LOG",contents=body,attachments=pathLog+file)
    yag.send(to="marioluca@gmail.com",subject="PROGETTO INNOVARE WEATHERSTATION LOG",contents=body,attachments=pathLog+file)
    yag.send(to="aversano.lerina@gmail.com",subject="PROGETTO INNOVARE WEATHERSTATION LOG",contents=body,attachments=pathLog+file)
    yag.send(to="kateryna.romanyuk20@gmail.com",subject="PROGETTO INNOVARE WEATHERSTATION LOG",contents=body,attachments=pathLog+file)
    yag.close()

#Funzione per avvio processo
def processrun():
    out=subprocess.run(["./vproweather"], stdout=subprocess.PIPE,universal_newlines=True) #DA MODIFICARE
    #out=subprocess.run(["./vproweather","-g","/dev/ttyUSB "], check= True, stdout=subprocess.PIPE ,universal_newlines=True)

    print("Output: ")
    output=vars(out)
    print(output.get("stdout"))
    file=saveOutput(output.get("stdout"))
    print("Invio mail...")
    sendmail(file)
    return True

#process = subprocess.run(['./vproweather'], check= True, stdout=subprocess.PIPE ,universal_newlines=True)
#output= process.stdout
#out=subprocess.run(["./vproweather","-g","/dev/ttyUSB "], check= True, stdout=subprocess.PIPE ,universal_newlines=True)


schedule.every().day.at("12:00").do(processrun)

while True:
    schedule.run_pending()
    time.sleep(50) # wait one minute
