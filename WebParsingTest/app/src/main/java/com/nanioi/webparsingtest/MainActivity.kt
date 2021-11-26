package com.nanioi.webparsingtest

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.nanioi.webparsingtest.databinding.ActivityMainBinding
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class MainActivity : AppCompatActivity() {

    val weburl = "https://search.musinsa.com/ranking/best"
    val TAG = "Main Activity"

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //네트워크 관련 작업이 이루어지기 때문에 Async Task를 이용해야 한다.
        MyAsyncTask().execute(weburl)

        //아이템 사이에 구분선 넣어 주기
        binding.rvNewsList.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))

    }

    //AsyncTask 정의
    inner class MyAsyncTask: AsyncTask<String, String, String>(){ //input, progress update type, result type
        private var result : String = ""
        var newsList: ArrayList<Item> = arrayListOf() //MutableList 처음 보는 거네...?

        override fun onPreExecute() {
            super.onPreExecute()
            binding.progressBar.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String?): String {
            val doc: Document = Jsoup.connect("$weburl").get()
            val elts: Elements = doc.select("body.mensinsa form.goodsRankForm ul.goodsRankList li.li_box div.li_inner")
//            val eltsSize = elts.size
//            Log.d(TAG, eltsSize.toString())
            elts.forEachIndexed{ index, elem ->
                val a_href = elem.select("a").attr("href")
                val thumb_img = elem.select("img").attr("src")
                val title = elem.select("strong.tit_thumb").text()
                Log.d(TAG, "$index $a_href / http:$thumb_img / $title")

                //추출한 자료를 가지고 데이터 객체를 만들어 ArrayList에 추가해 준다.
                var mNews = Item(title, a_href, "http:" + thumb_img)
                newsList.add(mNews)
            }
            //Log.d(TAG, newsList.get(1).title.toString())
            return doc.title()
        }

        override fun onPostExecute(result: String?) {
            binding.progressBar.visibility = View.GONE
            //문서제목 출력
            binding.tvTitle.setText(result)

            //어답터 설정
            binding.rvNewsList.layoutManager = LinearLayoutManager(this@MainActivity).also {
                it.orientation = LinearLayoutManager.VERTICAL
            }
            var adapter = MyAdapter(newsList)
            binding.rvNewsList.adapter = adapter

        }
    }

}