package com.example.revision_app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.revision_app.goals.GoalsActivity
import com.example.revision_app.revisions.RevisionActivity
import com.example.revision_app.tasks.TasksActivity
import com.example.revision_app.utils.NetworkUtil
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso

class HomeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupView()
    }

    private fun setupView() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val buttonToggleDrawer = findViewById<ImageButton>(R.id.button_toggle_drawer)
        val sidebarMenu: Menu = navigationView.menu
        val itemToHide: MenuItem? = sidebarMenu.findItem(R.id.nav_item1)
        val imageCentered = findViewById<ImageView>(R.id.imageViewCentered)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        buttonToggleDrawer.setOnClickListener {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView)
            } else {
                drawerLayout.openDrawer(navigationView)
            }
        }

        Picasso.get()
            .load("https://revisionfront-3lykdwzvva-ue.a.run.app/we_are_working.png")
            .error(R.mipmap.ic_launcher)
            .placeholder(R.mipmap.ic_launcher)
            .into(imageCentered)


        itemToHide?.isVisible = NetworkUtil.hasInternetContection(this)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_item0 -> {

                    val user = intent?.getSerializableExtra(  "EXTRA_USER")
                    val intent = Intent(this, GoalsActivity::class.java).apply {
                        putExtra("EXTRA_USER", user)
                    }

                    startActivity(intent)
                }
                R.id.nav_item1 -> {

                    val user = intent?.getSerializableExtra(  "EXTRA_USER")
                    val intent = Intent(this, RevisionActivity::class.java).apply {
                        putExtra("EXTRA_USER", user)
                    }

                    startActivity(intent)
                }
                R.id.nav_item2 -> {

                    val user = intent?.getSerializableExtra(  "EXTRA_USER")
                    val intent = Intent(this, TasksActivity::class.java).apply {
                        putExtra("EXTRA_USER", user)
                    }

                    startActivity(intent)
                }

                R.id.nav_item3 -> {

                    val sharedPreferences = getSharedPreferences("RevisionApp", Context.MODE_PRIVATE)

                    val editor = sharedPreferences.edit()
                    editor.remove("token")
                    editor.apply()

                    val intent = Intent(this, MainActivity::class.java)

                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}