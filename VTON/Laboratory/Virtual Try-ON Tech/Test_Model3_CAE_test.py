import tensorflow as tf
from keras.datasets import mnist
from keras.utils import np_utils
from keras.models import Sequential, load_model
from keras.layers import Dense, Conv2D,Conv3D, MaxPooling2D, Dropout, Flatten, Conv2DTranspose,GaussianNoise
from keras.callbacks import ModelCheckpoint, EarlyStopping

import matplotlib.pyplot as plt
import os
from PIL import Image
import numpy as np
import pandas as pd

from tensorflow.keras import layers
from tensorflow.keras.datasets import mnist
from tensorflow.keras.models import Model
from keras.callbacks import TensorBoard
import keras
from util import autoresize as ar

autoencoder =  keras.models.load_model('VTON/Laboratory/흐릿하게 나오는VTON 결과를 CAE 를 통해서 해결 할 수 있을까 ? /best CAE model.h5')
image = Image.open('VTON/Laboratory/흐릿하게 나오는VTON 결과를 CAE 를 통해서 해결 할 수 있을까 ? /VTON결과_testimage.png')

image = np.asarray(image)
img = ar.resizeImg(image,64,64)
imgArray = img/255
imgArray = imgArray.reshape(1,64,64,3)

imgArray=autoencoder.predict(imgArray)

imgArray = imgArray.reshape(64,64,3)

imgArray = Image.fromarray((imgArray*255).astype(np.uint8))
plt.imshow(imgArray)
imgArray.save('./saved.png')
