package com.example.direktoripengguna

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.direktoripengguna.data.PostBaru
import com.example.direktoripengguna.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PostActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_USER_NAME = "user_name"
    }

    private var userId = 0

    private lateinit var etJudul: EditText
    private lateinit var btnTambah: Button
    private lateinit var tvStatus: TextView
    private lateinit var listPost: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        userId = intent.getIntExtra(EXTRA_USER_ID, 0)
        title = intent.getStringExtra(EXTRA_USER_NAME) ?: "Postingan"

        etJudul = findViewById(R.id.etJudul)
        btnTambah = findViewById(R.id.btnTambah)
        tvStatus = findViewById(R.id.tvStatus)
        listPost = findViewById(R.id.listPost)

        btnTambah.setOnClickListener { kirimPost() }

        muatPosts()
    }
    private fun muatPosts() {
        tvStatus.text = "Memuat postingan..."

        lifecycleScope.launch {
            try {
                val posts = RetrofitClient.api.getPostsByUser(userId)
                listPost.adapter = ArrayAdapter(
                    this@PostActivity,
                    android.R.layout.simple_list_item_1,
                    posts.map { it.title }
                )
                tvStatus.text = "${posts.size} postingan"
            } catch (e: HttpException) {
                tvStatus.text = "Server error: HTTP ${e.code()}"
            } catch (e: IOException) {
                tvStatus.text = "Tidak ada koneksi internet"
            }
        }
    }
    private fun kirimPost() {
        val judul = etJudul.text.toString().trim()
        if (judul.isEmpty()) {
            etJudul.error = "Judul tidak boleh kosong"
            return
        }

        btnTambah.isEnabled = false
        lifecycleScope.launch {
            try {
                val baru = PostBaru(
                    title = judul,
                    body = "Dibuat dari aplikasi Direktori Pengguna",
                    userId = userId
                )
                val hasil = RetrofitClient.api.buatPost(baru)
                tvStatus.text = "Tersimpan dengan id ${hasil.id}"
                etJudul.text.clear()
            } catch (e: HttpException) {
                tvStatus.text = "Gagal menyimpan: HTTP ${e.code()}"
            } catch (e: IOException) {
                tvStatus.text = "Tidak ada koneksi internet"
            } finally {
                btnTambah.isEnabled = true
            }
        }
    }
}