from PIL.TiffImagePlugin import DATE_TIME
import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
from firebase_admin import db

from uuid import uuid4
from time import sleep


cred = credentials.Certificate('Server_util/closet-89ea8-firebase-adminsdk-zsasm-96c3e02090.json')
default_app=firebase_admin.initialize_app(cred,{ 
    'databaseURL': 'Server_util/closet-89ea8-firebase-adminsdk-zsasm-96c3e02090.json',
    'storageBucket':f"closet-89ea8.appspot.com"
    })
    #버킷은 바이너리 객체의 상위 컨테이너이다. 버킷은 Storage에서 데이터를 보관하는 기본 컨테이너이다.
bucket = storage.bucket()#기본 버킷 사용
print("** Firebase 연동에 성공했습니다 **")

def storage_connection():
    #Firebase database 인증 및 앱 초기화
    '''
    cred = credentials.Certificate('Server_tcp/closet-89ea8-firebase-adminsdk-zsasm-18c6197e81.json')
    default_app=firebase_admin.initialize_app(cred,{
        'storageBucket':f"closet-89ea8.appspot.com"
    })
    #버킷은 바이너리 객체의 상위 컨테이너이다. 버킷은 Storage에서 데이터를 보관하는 기본 컨테이너이다.
    bucket = storage.bucket()#기본 버킷 사용

    print("** Firebase 연동에 성공했습니다 **")
    '''
    return bucket

'''
ref = db.reference('User/Az6OTF01ReYn3VN1GUQAsMarJn32')
result =ref.get('bodyImageUri')
#print(ref.update({'remove':'Server_tcp/test.png'}))
'''

def realtime_update(user,new_colmn,new_data):
    users_ref = db.reference('Users')
    result  = user+'/'+new_colmn
    
    users_ref.update({
        result : new_data
    })


def fileUpload(upload_path,file):
    
    blob = bucket.blob(upload_path)
    #new token and metadata 설정
    new_token = uuid4()
    metadata = {"firebaseStorageDownloadTokens": new_token} #access token이 필요하다.
    blob.metadata = metadata    
    #upload file
    blob.upload_from_filename(filename=file)
    blob.make_public()
    public_url = blob.public_url
    print("print public url : ",public_url)    
    return public_url



def listener(event):
    #print(event.event_type)  # can be 'put' or 'patch'
    #print(event.path[1:])  # relative to the reference, it seems
    #print(event.data)  # new data at /reference/event.path. None if deleted

    #key_list=list(event.data.keys())
    #realkey=key_list[len(key_list)-1]
    
    return event
    

if __name__ == '__main__':
    
    #print("테스트용 파일을 업로드 합니다. ")
    url = fileUpload('user/avatar/test.png','VTON/Server_util/test_remove_2.png')
    print(url)
    
    realtime_update('kzbuEMGh1yMymoRfcvASXbF7tOB3','avatarImageUri',url)
    #fileUpload('item/remove_bg/','VTON/Server_util/test_remove.png')
    
    #new_data=firebase_admin.db.reference('Users').listen(listener)
    
    