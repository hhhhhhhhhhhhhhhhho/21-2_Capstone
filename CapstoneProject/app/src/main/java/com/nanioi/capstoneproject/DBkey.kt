package com.nanioi.capstoneproject

class DBKey {
    companion object {
        const val DB_ARTICLES = "Articles"
        const val DB_USERS = "Users"
        const val DB_CHATS = "Chats"
        const val CHILD_CHAT = "chat"
    }
}

// 예시고 음 그 사람 얼굴사진, 몸사진, 체중, 키 , 상의 , 하의 카테고리 별로 하면 좋을거같은데
// 입력받는 부분들에 따라 데이터 관리 필요한 부분들만 이런식으로 전역변수 형식처럼 지정해 놓으면 될거같아요