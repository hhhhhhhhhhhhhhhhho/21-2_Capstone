package com.nanioi.capstoneproject

class DBkey {
    companion object{
        const val USERS = "Users"
        const val LIKED_BY = "likedBy"
        const val LIKE = "like"
        const val DIS_LIKE = "disLike"
        const val USER_ID = "userId"
        const val NAME = "name"
        const val MATCH = "match"
    }
}

// 예시고 음 그 사람 얼굴사진, 몸사진, 체중, 키 , 상의 , 하의 카테고리 별로 하면 좋을거같은데
// 입력받는 부분들에 따라 데이터 관리 필요한 부분들만 이런식으로 전역변수 형식처럼 지정해 놓으면 될거같아요