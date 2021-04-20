# Innovare


***
**Preparazione raspbarry** prima di avviare il gateway:
> 1. Controllare permessi sulla porta usb il path da controllare e' _/dev/ttyUSB0_
> 2. Controllare raggiungibilita' dell'ip nel quale risiede il MiddleLayer (un ping su ip basterebbe)


**Per avviare il gateway da terminale**:
Come mostrato nel file interno al progetto del gateway README.adoc, che si rifa' alla documentazione vertx:
> 1. Posizionarsi nella cartella del progetto
> 2. Eseguire comando: ./mvnw clean compile exec:java


**Possibili problemi**:

> * Se avviato tramite sudo il sofware non prelevera' la corretta posizione del file di configurazione poiche' la cartella home da utente root e' _/root/_ e non _/home/_, non avviare come root il software.
> * Possibili problemi di accesso ai file cache di vertx letti e scritti in _/tmp/_, per risolvere usare chmod e modificare permessi.

***
