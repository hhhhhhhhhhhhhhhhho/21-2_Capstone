package com.nanioi.closetapplication.styling.recommend

import android.util.Log
import android.util.Xml
import com.nanioi.closetapplication.R
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder

private val ns: String? = null

//11번가 openApi xml Parsing

// 원하는 액티비티 코드 추가
//    inner class XmlParsingTask() : AsyncTask<Any?, Any?, List<RecommendItemModel>>() {
//        override fun onPostExecute(result: List<RecommendItemModel>) {
//            super.onPostExecute(result)
//            recyclerAdapter.submitList(result)
//        }
//
//        override fun doInBackground(vararg params: Any?): List<RecommendItemModel> { // xml 파싱할때 여기서 데이터 받아와 reedFeed 부분은 저 XmlParsingTask 파일보면 있으
//            return readFeed(parsingData(keyword))
//        }
//    }

fun parsingData(keyword : String): XmlPullParser {

    val apiKey = "392d1a9ebfe2deb7694d652fd495b316"
    val apiCode = "ProductSearch"
    val Keyword = keyword
    val pageNum = "1"
    val pageSize = "10"
    val sortCd = "CP"
    //var option =

    //"http://openapi.11st.co.kr/openapi/OpenApiService.tmall?key=[key]&apiCode=ProductSearch&keyword=[keyword]&option=Categories"
    var keyDecode = URLDecoder.decode(apiKey, "UTF-8")
    val urlBuilder = StringBuilder("http://openapi.11st.co.kr/openapi/OpenApiService.tmall") /*URL*/
    urlBuilder.append("?" + URLEncoder.encode("key", "UTF-8") + "=" + URLEncoder.encode(keyDecode, "UTF-8"))
    urlBuilder.append("&" + URLEncoder.encode("apiCode", "UTF-8") + "=" + URLEncoder.encode(apiCode, "UTF-8")) //OpenAPI 서비스 코드 → 상품검색 : ProductSearch
    urlBuilder.append("&" + URLEncoder.encode("keyword", "UTF-8") + "=" + URLEncoder.encode(Keyword, "UTF-8")) //검색 요청 값
    urlBuilder.append("&"+ URLEncoder.encode("pageNum", "UTF-8") + "=" + URLEncoder.encode(pageNum, "UTF-8")) // 페이지 넘버(Default 1)
    urlBuilder.append("&"+ URLEncoder.encode("pageSize", "UTF-8") + "=" + URLEncoder.encode(pageSize, "UTF-8")) // 한페이지에 출력되는 상품 수(Default 50, 최대 200)
    urlBuilder.append("&"+ URLEncoder.encode("sortCd", "UTF-8") + "=" + URLEncoder.encode(sortCd, "UTF-8")) // 정렬순서 "CP" -> 인기도

    val url = URL(urlBuilder.toString())

    // 구글링해서 퍼오넉라 xmlPArser 생성해주는 부분같음
    val parser: XmlPullParser = Xml.newPullParser()
    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
    parser.setInput(InputStreamReader(url.openStream(), "EUC-KR"))
    parser.nextTag()

    return parser
}

@Throws(XmlPullParserException::class, IOException::class)
fun readFeed(parser: XmlPullParser): List<RecommendItemModel> { // 이거 Accumulate랑 New에서 doInBackgourd에서 이 함수 호출하는거
    val entries = mutableListOf<RecommendItemModel>() // 그 받아오는 데이터 클래스로 리스트 만들고

    parser.require(XmlPullParser.START_TAG, ns, "ProductSearchResponse") // 그 xml 데이터 부분 보면 알텐디 <response> 그부분으로 들어가서 아래 코드 태그들로 타고 들어가서 원하는 데이터들 뽑아오는 부분이
    while (parser.next() != XmlPullParser.END_TAG) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            continue
        }
        if (parser.name == "Product") {
            entries.add(readEntry(parser))
        } else {
            skip(parser)
        }
    }
    return entries
}

@Throws(XmlPullParserException::class, IOException::class)
fun readEntry(parser: XmlPullParser): RecommendItemModel {
    parser.require(XmlPullParser.START_TAG, ns, "Product")

    var ProductCode:String?=null
    var ProductName: String? = null
    var ProductPrice: String? = null
    var ProductImage: String? = null
    var Seller: String? = null
    var DetailPageUrl: String? = null
    var SalePrice: String? = null

    while (parser.next() != XmlPullParser.END_TAG) { // 태그들 보며 이동하면서 원하는 태그들에 데이터 뽑아오는 부
        if (parser.eventType != XmlPullParser.START_TAG) {
            continue
        }
        when (parser.name) {
            "ProductCode" -> ProductCode = readProductCode(parser)
            "ProductName" -> ProductName = readProductName(parser)
            "ProductPrice" -> ProductPrice = readProductPrice(parser)
            "ProductImage" -> ProductImage = readProductImage(parser)
            "Seller" -> Seller = readSeller(parser)
            "DetailPageUrl" -> DetailPageUrl = readDetailPageUrl(parser)
            "SalePrice" -> SalePrice = readSalePrice(parser)
            else -> skip(parser)
        }
    }
    return RecommendItemModel(ProductCode, ProductName, ProductPrice, ProductImage, Seller,DetailPageUrl,SalePrice) // 클래스에 데이터 넣어서 반환
}

// Processes title tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
fun readProductCode(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "ProductCode")
    val productCode = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "ProductCode")
    return productCode
}


// Processes title tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
fun readProductName(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "ProductName")
    val productName = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "ProductName")

    return productName
}

// Processes link tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
fun readProductPrice(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "ProductPrice")
    val productPrice = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "ProductPrice")
    return productPrice
}
// Processes title tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
fun readProductImage(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "ProductImage")
    val productImage = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "ProductImage")

    return productImage
}

// Processes link tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
fun readSeller(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "Seller")
    val seller = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "Seller")
    return seller
}
// Processes link tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
fun readDetailPageUrl(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "DetailPageUrl")
    val detailPageUrl = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "DetailPageUrl")
    return detailPageUrl
}
// Processes link tags in the feed.
@Throws(IOException::class, XmlPullParserException::class)
fun readSalePrice(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, ns, "SalePrice")
    val salePrice = readText(parser)
    parser.require(XmlPullParser.END_TAG, ns, "SalePrice")
    return salePrice
}

// For the tags title and summary, extracts their text values.
@Throws(IOException::class, XmlPullParserException::class)
fun readText(parser: XmlPullParser): String {
    var result = ""
    if (parser.next() == XmlPullParser.TEXT) {
        result = parser.text
        parser.nextTag()
    }
    return result
}

@Throws(XmlPullParserException::class, IOException::class)
fun skip(parser: XmlPullParser) {

    if (parser.eventType != XmlPullParser.START_TAG) {
        throw IllegalStateException()
    }
    var depth = 1
    while (depth != 0) {
        if (parser.name == "Products")
            break
        when (parser.next()) {
            XmlPullParser.END_TAG -> depth--
            XmlPullParser.START_TAG -> depth++
            null -> parser.next()
        }
    }
}