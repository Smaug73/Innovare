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
from tensorflow.keras.applications.resnet50 import ResNet50
from tensorflow.keras.preprocessing import image
from tensorflow.keras.applications.resnet50 import preprocess_input, decode_predictions
import numpy as np



pathImages='/home/stefano/Scrivania/Lavoro/immagini/prova/ele.png'

#Leggiamo il path delle foto e il nome del modello da dover utilizzare
if len(sys.argv) != 2: 
    sys.exit('Errore: parametri sbagliati')


try:
    nomeScript,modelName = sys.argv
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

#Carichiamo le immagini da classificare
'''
image_generator = image.ImageDataGenerator(rescale=1./255,validation_split=0.99)   #Effettuiamo delle predizioni utilizzando alcune foto prese casualmente da una cartella presa a parte dal dataset
                                                                                  
pred_gen= image_generator.flow_from_directory(directory=pathImages,
                                                           shuffle=True,
                                                           class_mode='input',
                                                           subset='validation')
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

















