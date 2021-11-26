

import db_connection as db_func
from urllib import request
import time
import os
import firebase_admin
from google.cloud import storage
import time
import urllib.request
from pathlib import Path
from rembg.bg import remove


def download_blob(source_blob_name, destination_file_name):
    """Downloads a blob from the bucket."""
    # The ID of your GCS bucket
    # bucket_name = "your-bucket-name"

    # The ID of your GCS object
    # source_blob_name = "storage-object-name"

    # The path to which the file should be downloaded
    # destination_file_name = "local/path/to/file"

    bucket = db_func.storage_connection()

    # Construct a client side representation of a blob.
    # Note `Bucket.blob` differs from `Bucket.get_blob` as it doesn't retrieve
    # any content from Google Cloud Storage. As we don't need additional data,
    # using `Bucket.blob` is preferred here.
    blob = bucket.blob(source_blob_name)
    blob.download_to_filename(destination_file_name)
    #os.system('gsutil acl ch -u AllUsers:R closet-89ea8.appspot.com/item/remove_bg/')

    print(
        "Downloaded storage object {} from bucket {} to local file {}.".format(
            source_blob_name, "Default", destination_file_name
        )
    )

#res=download_blob('closet-89ea8.appspot.com','Server_tcp/removedbg_image/test.png')

#img = Image.open(BytesIO(res))


def remove_background(user_id,target_file):
    #https://github.com/danielgatis/rembg    
    #removed_bg_fileUrl="completed_removing/"+user_id+"_output.png" ### FIXME test removing 지우고, 
    removed_bg_fileUrl="https://firebasestorage.googleapis.com/v0/b/closet-89ea8.appspot.com/o/user%2Fbody1%2FfPFIyQhZe6VYheDbDFbjqXMQoxK2_img_body1.jpg?alt=media&token=e7ab0ece-c118-48b8-9ba7-92db20b47bcf"
    #os.system("curl -s "+target_file+" | rembg > "+removed_bg_fileUrl)
    removed_bg_fileURL_onFireStore = db_func.fileUpload("user/avatar/"+user_id+"_body_front_removedBg.png",removed_bg_fileUrl)
    print("**** FireStore Fileupload 완료 *****")
    db_func.realtime_update(user_id,"avatar_front_imageUrl",removed_bg_fileURL_onFireStore)
    print("**** Realtime Database Sync 완료 ****")
    

def listen_and_removeBg(event):
    
    event_type = event.event_type
    key = event.path[1:29] #string 
    event_data = event.data # dict

    if key:
        print(type(event_data))
        print(event_data)
        user_id = event_data['uid']
        body_Uri = event_data['body_front_imageUrl']
        remove_background(user_id,body_Uri)
        

def automatic_removeBg():
    firebase_admin.db.reference('Users').listen(listen_and_removeBg)


if __name__=="__main__":   
    automatic_removeBg()


