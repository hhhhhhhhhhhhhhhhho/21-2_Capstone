package com.nanioi.closetapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.nanioi.closetapplication.User.LoginUserData
import com.nanioi.closetapplication.User.SignInActivity
import com.nanioi.closetapplication.closet.ClosetFragment
import com.nanioi.closetapplication.closet.ItemModel
import com.nanioi.closetapplication.databinding.ActivityAddImageBinding
import com.nanioi.closetapplication.databinding.ActivityMainBinding
import com.nanioi.closetapplication.home.HomeFragment
import com.nanioi.closetapplication.mypage.MyPageFragment
import com.nanioi.closetapplication.styling.StylingFragment
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() , NavigationView.OnNavigationItemSelectedListener{

    private var auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var toolbar: Toolbar? = null
    private var drawerLayout: DrawerLayout? = null
    private var navView: NavigationView? = null
    private var drawerToggle: ActionBarDrawerToggle? = null
    private var navHeaderView: View? = null
    private var tvHeaderName: TextView? = null
    private var tvHeaderEmail: TextView? = null
    private var tvHeaderGender: TextView? = null
    private var tvHeaderCm: TextView? = null
    private var tvHeaderKg: TextView? = null
    private var imgHeaderProfile : ImageView ?=null

    val homeFragment = HomeFragment()
    val stylingFragment = StylingFragment()
    val closetFragment = ClosetFragment()
    val myPageFragment = MyPageFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initViews()

    }

    //by 나연. Main 페이지 뷰 툴바, drawerLayout, navigationView 초기화 (21.09.24)
    @SuppressLint("SetTextI18n")
    private fun initViews() = with(binding) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        drawerLayout = binding.mainDrawerLayout
        navView = binding.mainNavigationView
        navView?.setNavigationItemSelectedListener(this@MainActivity) //navigation 리스너

        drawerToggle = ActionBarDrawerToggle(
            this@MainActivity,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        supportActionBar?.setDisplayShowTitleEnabled(false)
        drawerLayout?.addDrawerListener(drawerToggle!!)
        navView?.setNavigationItemSelectedListener(this@MainActivity)

        navHeaderView = navView?.getHeaderView(0)

        tvHeaderName = navHeaderView!!.findViewById(R.id.tv_header_name)
        tvHeaderEmail = navHeaderView!!.findViewById(R.id.tv_header_email)
        tvHeaderGender = navHeaderView!!.findViewById(R.id.tv_header_gender)
        tvHeaderCm = navHeaderView!!.findViewById(R.id.tv_header_cm)
        tvHeaderKg = navHeaderView!!.findViewById(R.id.tv_header_kg)
        imgHeaderProfile = navHeaderView!!.findViewById(R.id.img_header_profile)


        tvHeaderName?.text = "이름 : ${LoginUserData.name}"
        tvHeaderEmail?.text = LoginUserData.email
        tvHeaderGender?.text = "성별 : ${LoginUserData.gender}"
        tvHeaderCm?.text = "키 : ${LoginUserData.cm}cm"
        tvHeaderKg?.text = "몸무게 : ${LoginUserData.kg}kg"
        Glide.with(navHeaderView!!).load(LoginUserData.avatar_front_ImageUri).into(imgHeaderProfile!!)

        replaceFragment(homeFragment)
    }

    //by 나연. 툴바 메뉴 버튼 클릭 시 실행 함수 (21.09.24)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { // 메뉴 버튼
                drawerLayout?.openDrawer(GravityCompat.START)    // 네비게이션 드로어 열기
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //by 나연. 드로어 내 아이템 클릭 시 이벤트 처리 함수 (21.09.24)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.main -> replaceFragment(homeFragment)
            R.id.styling -> replaceFragment(stylingFragment)
            R.id.closet -> replaceFragment(closetFragment)
            R.id.myPage -> replaceFragment(myPageFragment)
            R.id.logout -> logout(this)
        }
        return false
    }

    //by 나연. 뒤로가기 처리 함수 (21.09.24)
    override fun onBackPressed() {
        if(drawerLayout?.isDrawerOpen(GravityCompat.START)!!){
            drawerLayout?.closeDrawers()
            // 테스트를 위해 뒤로가기 버튼시 Toast 메시지
            Toast.makeText(this,"back btn clicked", Toast.LENGTH_SHORT).show()
        } else{
            super.onBackPressed()
        }
    }

    //by 나연. fragment 전환 함수 (21.09.24)
    public fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentContainer, fragment)
                commit()
            }
    }

    //by 나연. 로그아웃 실행 함수 (21.10,18)
    private fun logout(context: Context){
        //todo 회원 로그아웃 코드 구현하기
        var logOutDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        logOutDialog.setTitle("알림")
        logOutDialog.setMessage("로그아웃 하시겠습니까?")

        logOutDialog.setPositiveButton("확인") { dialog, _ ->
            LoginUserData.uid = null
            LoginUserData.email = null
            LoginUserData.name = null
            LoginUserData.gender = null
            LoginUserData.cm = null
            LoginUserData.kg = null
            LoginUserData.body_front_ImageUri = null
            LoginUserData.body_back_ImageUri = null
            LoginUserData.avatar_front_ImageUri = null
            LoginUserData.avatar_back_ImageUri = null

            dialog.dismiss()
            startActivity(Intent(context, SignInActivity::class.java))
            finish()
        }
        logOutDialog.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        logOutDialog.setCancelable(false)
        logOutDialog.show()

        auth.signOut()
    }
}