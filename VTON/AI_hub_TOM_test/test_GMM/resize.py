import os
import cv2

in_Path = '/Users/cho/Desktop/test_GMM/resize/in'
out_Path = '/Users/cho/Desktop/test_GMM/resize/out'

file_list = os.listdir(in_Path)

for file in file_list:
    file_Path = in_Path + '/' + file

    img = cv2.imread(file_Path)
    resize_Img = cv2.resize(img, (384, 512), interpolation=cv2.INTER_CUBIC)
    resize_Img = cv2.cvtColor(resize_Img, cv2.COLOR_BGR2GRAY)

    save_Path = out_Path + '/' + file
    cv2.imwrite(save_Path, resize_Img)


print("END!!!")