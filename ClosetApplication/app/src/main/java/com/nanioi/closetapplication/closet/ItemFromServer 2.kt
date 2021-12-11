package com.nanioi.closetapplication.closet

data class ItemFromServer(
    var userId: String,
    var userBodyImage:String,
    var topImageUrl: String,
    var bottomImageUrl: String,
    var accessoryImageUrl: String,
    var shoesImageUrl: String
) {
    constructor() : this("", "","", "" ,"", "")
}