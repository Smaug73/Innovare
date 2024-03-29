'''


Il servizio deve prevedere la ricezione dei dati per la classificazione:
    -Directory o path della directory contenente le nuove immagini
Ricevuto il path le immagini devono essere caricate e formattate per la classificazione.
Finita la classificazione, bisogna restituire il valore della classificazione.
E' inoltre possibile specificare i modelli da voler utilizzare.
E' possibile restituire i modelli disponibili.


python Esempio1.py nomeModello
'''
import sys 

# TensorFlow and tf.keras
import tensorflow as tf
from PIL import Image
from tensorflow import keras

#import per testing con resnet
#from tensorflow.keras.applications.resnet50 import ResNet50
from tensorflow.keras.preprocessing import image
#from tensorflow.keras.applications.resnet50 import preprocess_input, decode_predictions
import numpy as np

import csv
import json

imgSizeX=256
imgSizeY=256

modelSubName="none"
substringSearch='-plant_'
labels=["Pianta Normale","Carenza di acqua","Eccesso di acqua","Infestanti","Ambigua"]
#Gli argomenti passati allo script sono il nome del modello e il path nel quale sono presenti le immagini
#che sono state segmentate.
#Esempio:
#python Esempio1.py stub.h5 path
#test
#python Innovare/innovare.utils/pythonScript/Esempio1.py InnovareModels/stub.h5 InnovareImages/test/

#Leggiamo il path delle foto e il nome del modello da dover utilizzare
#Questo serve all'amministratore per la scelta del modello da utilizzare
if len(sys.argv) != 3: 
    sys.exit('Errore: parametri insufficenti')

try:
    nomeScript,modelName,referencePath = sys.argv
    #print('Nome modello scelto: '+modelName)
    pathModel =modelName
    #   print(pathModel )
    model= tf.keras.models.load_model(pathModel)
    #Cerco il nome del modello dal path 
    for i in range(len(modelName)):
        #Fare attenzione al sistema operativo utilizzato
        if (modelName[(len(modelName)-i-1)] == '/') or (modelName[(len(modelName)-i-1)] == '\\'):
            print("trovato!")
            break
        else:
            print("cerco")

    modelSubName= modelName[(len(modelName)-i):(len(modelName))]
    print("Model name: i:{} len of modelpath:{} ".format(i,len(modelName)-1) )
    print(modelSubName)

except ImportError:
    print('ImportError: caricamento modello non possibile')
    sys.exit(1)

except IOError:
    print('IOError: modello non valido')
    sys.exit(1)

except RuntimeError:
    print('RunTimeError')
    sys.exit(1)

#Recupero file csv dalla dir nel quale e' stato effettuata la segmentazione
try:
    print(referencePath+"metadata.csv")
    csv_file= open(referencePath+"metadata.csv")
    csv_reader= csv.reader(csv_file, delimiter=',')

except IOError:
    print("IOError: csv non esistente")
    sys.exit(1)

#Carichiamo le immagini da classificare


def classificaImmagini(imageName):
    #Va modificata la grandezza, per testare utilizziamo la grandezza richiesta dalla rete test
    img= keras.preprocessing.image.load_img(
        referencePath+imageName, grayscale=False, color_mode='rgb', target_size=(imgSizeX,imgSizeY),
         interpolation='nearest'
    )

    img_array= keras.preprocessing.image.img_to_array(img)
    img_array = tf.expand_dims(img_array, 0)
    preds = model.predict(img_array)
    
    
    allClass=[]
   
    for i in range(5):
        singleClass={ "classe":"{}".format(labels[i]) , "score":round(preds[0][i]*100,1)}
        allClass.append(singleClass)

    return allClass
    #return PhotoDict={"uid":"'"+row[7]+"'" , "classification":allClass} 


allImageClassification=[]
count=0
for row in csv_reader:
    if count == 0:
        print(row)
        count=+1
    else:       
        if substringSearch in str(row[6]):  #Consideriamo solo le immagini della sola pianta
            classification=classificaImmagini(row[6])
            #print('"classification"': ,classification)
            PhotoDict={"hash":"'"+row[7]+"'" , "classifications":classification}
            #print("PhotoDict: ",PhotoDict)
            #Ricostruisco il nome della immagine di origine dalla quale proviene la pianta
            listStringName=row[6].split('plant')
            originalImgName=referencePath+listStringName[0]+'erased_background_with_boxes.jpg'
            dictRow={ 'path':referencePath+row[6],'hash':row[7],'date':row[0].replace("_","-"),'originalImage':originalImgName ,'classification':PhotoDict,'model':modelSubName }
            allImageClassification.append(dictRow)
        
        #test#test#test#test
        #print('RISULTATO:')
        #print(allImageClassification)
        #break#test

result= json.dumps(allImageClassification)
newFile= open(referencePath+"classification.json","w")
newFile.write(result)
newFile.close()
#print(result)
















