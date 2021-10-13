package com.nanioi.capstoneproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.nanioi.capstoneproject.Styling.StylingFragment
import com.nanioi.capstoneproject.closet.ClosetFragment
import com.nanioi.capstoneproject.home.HomeFragment
import com.nanioi.capstoneproject.mypage.MyPageFragment

class MainActivity : AppCompatActivity() ,NavigationView.OnNavigationItemSelectedListener{

    lateinit var drawerLayout : DrawerLayout
    lateinit var navigationView: NavigationView

    val homeFragment = HomeFragment()
    val avatarFragment = StylingFragment()
    val closetFragment = ClosetFragment()
    val myPageFragment = MyPageFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게


        drawerLayout = findViewById<DrawerLayout>(R.id.main_drawer_layout)
        navigationView = findViewById<NavigationView>(R.id.main_navigationView)
        navigationView.setNavigationItemSelectedListener(this) //navigation 리스너

        replaceFragment(homeFragment)

    }

    //by 나연. 툴바 메뉴 버튼 클릭 시 실행 함수 (21.09.24)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { // 메뉴 버튼
                drawerLayout.openDrawer(GravityCompat.START)    // 네비게이션 드로어 열기
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //by 나연. 드로어 내 아이템 클릭 시 이벤트 처리 함수 (21.09.24)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.main -> replaceFragment(homeFragment)
            R.id.styling -> replaceFragment(avatarFragment)
            R.id.closet -> replaceFragment(closetFragment)
            R.id.myPage -> replaceFragment(myPageFragment)
            R.id.logout -> startActivity(Intent(this,SignInActivity::class.java))
        }
        return false
    }

    //by 나연. 뒤로가기 처리 함수 (21.09.24)
    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers()
            // 테스트를 위해 뒤로가기 버튼시 Toast 메시지
            Toast.makeText(this,"back btn clicked",Toast.LENGTH_SHORT).show()
        } else{
            super.onBackPressed()
        }
    }

    //by 나연. fragment 전환 함수 (21.09.24)
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentContainer, fragment)
                commit()
            }
    }
}