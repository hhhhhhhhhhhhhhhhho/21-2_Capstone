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

    //by ??????. Main ????????? ??? ??????, drawerLayout, navigationView ????????? (21.09.24)
    @SuppressLint("SetTextI18n")
    private fun initViews() = with(binding) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // ???????????? ?????? ??? ?????? ?????????
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24) // ????????? ????????? ??????
        supportActionBar?.setDisplayShowTitleEnabled(false) // ????????? ????????? ????????????

        drawerLayout = binding.mainDrawerLayout
        navView = binding.mainNavigationView
        navView?.setNavigationItemSelectedListener(this@MainActivity) //navigation ?????????

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


        tvHeaderName?.text = "?????? : ${LoginUserData.name}"
        tvHeaderEmail?.text = LoginUserData.email
        tvHeaderGender?.text = "?????? : ${LoginUserData.gender}"
        tvHeaderCm?.text = "??? : ${LoginUserData.cm}cm"
        tvHeaderKg?.text = "????????? : ${LoginUserData.kg}kg"
        Glide.with(navHeaderView!!).load(LoginUserData.avatar_front_ImageUrl).into(imgHeaderProfile!!)

        replaceFragment(homeFragment)
    }

    //by ??????. ?????? ?????? ?????? ?????? ??? ?????? ?????? (21.09.24)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { // ?????? ??????
                drawerLayout?.openDrawer(GravityCompat.START)    // ??????????????? ????????? ??????
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //by ??????. ????????? ??? ????????? ?????? ??? ????????? ?????? ?????? (21.09.24)
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

    //by ??????. ???????????? ?????? ?????? (21.09.24)
    override fun onBackPressed() {
        if(drawerLayout?.isDrawerOpen(GravityCompat.START)!!){
            drawerLayout?.closeDrawers()
            // ???????????? ?????? ???????????? ????????? Toast ?????????
            Toast.makeText(this,"back btn clicked", Toast.LENGTH_SHORT).show()
        } else{
            super.onBackPressed()
        }
    }

    //by ??????. fragment ?????? ?????? (21.09.24)
    public fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentContainer, fragment)
                commit()
            }
    }

    //by ??????. ???????????? ?????? ?????? (21.10,18)
    private fun logout(context: Context){
        //todo ?????? ???????????? ?????? ????????????
        var logOutDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        logOutDialog.setTitle("??????")
        logOutDialog.setMessage("???????????? ???????????????????")

        logOutDialog.setPositiveButton("??????") { dialog, _ ->
            LoginUserData.uid = null
            LoginUserData.email = null
            LoginUserData.name = null
            LoginUserData.gender = null
            LoginUserData.cm = null
            LoginUserData.kg = null
            LoginUserData.body_front_ImageUrl = null
            LoginUserData.body_back_ImageUrl = null
            LoginUserData.avatar_front_ImageUrl = null
            LoginUserData.avatar_back_ImageUrl = null

            dialog.dismiss()
            startActivity(Intent(context, SignInActivity::class.java))
            finish()
        }
        logOutDialog.setNegativeButton("??????") { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        logOutDialog.setCancelable(false)
        logOutDialog.show()

        auth.signOut()
    }
}