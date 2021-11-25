### autonomical image resize

import cv2
import os

def resizeImg(image, width=None, height=None):
  dim=None
  (h,w) = image.shape[:2]
  if width is None and height is None:
    return image
  
  elif width is not None and height is not None:
    dim = (width, height)
  
  elif width is None:
    r = height/float(h)
    dim = (int(w*r), height)
  else:
    r = width / float(w)
    dim = (width, int(h*r))

  resized = cv2.resize(image, dim, interpolation=cv2.INTER_AREA)
  return resized

def main():

  folder = "./originalImgs/"
  newResizedFolder = "./newResizedImgs/"
  for filename in os.listdir(folder):
    img = cv2.imread(os.path.join(folder, filename))
    if img is not None:
      newImage = resizeImg('/content/Unknown-6.png')
      newImgPath = newResizedFolder + filename
      cv2.imwrite(newImgPath, newImage)
      
