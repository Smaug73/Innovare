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

substringSearch='-plant_'
#Gli argomenti passati allo script sono il nome del modello e il path nel quale sono presenti le immagini
#che sono state segmentate.
#Esempio:
#python Esempio1.py stub.h5 path

#Leggiamo il path delle foto e il nome del modello da dover utilizzare
#Questo serve all'amministratore per la scelta del modello da utilizzare
if len(sys.argv) != 3: 
    sys.exit('Errore: parametri insufficenti')

try:
    nomeScript,modelName,referencePath = sys.argv
    #print('Nome modello scelto: '+modelName)
    pathModel = '/home/stefano/Scrivania/Lavoro/Modelli/'+modelName
    #   print(pathModel )
    model= tf.keras.models.load_model(pathModel)
    
except ImportError:
    print('ImportError: caricamento modello non possibile')
    sys.exit()

except IOError:
    print('IOError: modello non valido')
    sys.exit()

except RuntimeError:
    print('RunTimeError')
    sys.exit()

#Recupero file csv dalla dir nel quale e' stato effettuata la segmentazione
try:
    csv_file= open(referencePath+"/metadata.csv")
    csv_reader= csv.reader(csv_file, delimiter=',')

except IOError:
    print("IOError: csv non esistente")
    sys.exit()

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
    score = tf.nn.softmax(preds[0])
    #print(score[0])
    #print(score.shape.as_list())
    #print(score.numpy()[4])
    allClass=[]
    for i in range(score.shape.as_list()[0]):
        singleClass={ "classe":"{}".format(i) , "score":score.numpy()[4]*100}
        allClass.append(singleClass)
    return allClass
    #return PhotoDict={"uid":"'"+row[7]+"'" , "classification":allClass} 

            

            
'''
image_generator = image.ImageDataGenerator(rescale=1./255,validation_split=0.99)   #Effettuiamo delle predizioni utilizzando alcune foto prese casualmente da una cartella presa a parte dal dataset
                                                                                  
pred_gen= image_generator.flow_from_directory(directory=pathImages,
                                                           shuffle=True,
                                                           class_mode='input',
                                                           subset='validation')
'''
allImageClassification=[]
count=0
for row in csv_reader:
    if count == 0:
        print(row)
        count=+1
    else:
        if substringSearch in str(row[6]):  #Condiriamo solo le immagini della sola pianta
            classification=classificaImmagini(row[6])
            #print('"classification"': ,classification)
            PhotoDict={"hash":"'"+row[7]+"'" , "classifications":classification}
            #print("PhotoDict: ",PhotoDict)
            #Ricostruisco il nome della immagine di origine dalla quale proviene la pianta
            listStringName=row[6].split('plant')
            originalImgName=referencePath+listStringName[0]+'erased_background_with_boxes.jpg'
            dictRow={ 'path':referencePath+row[6],'hash':row[7],'date':row[0].replace("_","-"),'originalImage':originalImgName ,'classification':PhotoDict }
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

'''
#try:
img= keras.preprocessing.image.load_img(
        pathImages, grayscale=False, color_mode='rgb', target_size=(256,256),
        interpolation='nearest'
    )
img_array= keras.preprocessing.image.img_to_array(img)
img_array = tf.expand_dims(img_array, 0)

preds = model.predict(img_array)
    # decode the results into a list of tuples (class, description, probability)
    # (one such list for each sample in the batch)
score = tf.nn.softmax(preds[0])

result = "[class:'{}', score: '{}']".format(np.argmax(score), 100 * np.max(score) )
print(
    result
)
exit(result)

#except Exception:
 #   print('Errore durante la classificazione')
'''
















