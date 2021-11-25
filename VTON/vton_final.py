from Server_util import db_connection as db_func

import firebase_admin



def vton(user_id,body_image,top_image,bottom_image):
    
    #개인에게 할당되는 가상착용 결과를 url 로 뽑아줘야 함. 
    
    # 가상 착용 후, 업로드까지 완료 해야 함. - 아래는 업로드 과정 - 
    tom_result_path ="/Users/mac/Documents/GitHub/21-2_Capstone/VTON/tom_test.png"
    url_using_realtime = db_func.fileUpload("item/tom_result/"+user_id+"_result.png",tom_result_path)
    db_func.realtime_update(user_id,"avatar_front_imageUri",url_using_realtime)
    

def listen_and_vton(event):

    event_type = event.event_type
    event_path = event.path
    event_data = event.data


    if event_data:
        key_list=list(event.data.keys())

        key=key_list[len(key_list)-1
        
        ]
        print(key)
        body=event_data[key]['body_front_imageUri']
        top=event_data[key]['top']
        bottom=event_data[key]['bottom']
        user_id=event_data[key]['uid']

        vton(user_id,body,top,bottom)



def automatic_vton():
    firebase_admin.db.reference('call').listen(listen_and_vton)

if __name__=="__main__" :
    automatic_vton()



