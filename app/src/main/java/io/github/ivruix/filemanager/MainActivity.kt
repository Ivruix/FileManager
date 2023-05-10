package io.github.ivruix.filemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity(), FileAdapter.OnItemClickListener, FileAdapter.OnFileLongClickListener {
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 123
    }

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_CODE_PERMISSIONS)
        } else {
            initRecyclerView(intent.getStringExtra("path"))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                initRecyclerView()
            }
        }
    }

    private fun initRecyclerView(path : String? = null) {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FileAdapter(getFiles(path ?: Environment.getExternalStorageDirectory().absolutePath))
        adapter.setOnItemClickListener(this)
        adapter.setOnFileLongClickListener(this)
        recyclerView.adapter = adapter
    }


    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onItemClick(file: File) {
        if (file.isDirectory) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("path", file.absolutePath)
            startActivity(intent)
        } else {
            launchFile(file)
        }
    }

    override fun onFileLongClick(file: File, view: View) {
        val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "*/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Share file using"))
    }

    private fun launchFile(file: File) {
        val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        val mimeType = getMimeType(uri)

        intent.setDataAndType(uri, mimeType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooser = Intent.createChooser(intent, "Open with")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        }
    }

    private fun getMimeType(uri: Uri): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    private fun getFiles(path: String): ArrayList<File> {
        val files = ArrayList<File>()
        val directory = File(path)

        val fileList = directory.listFiles()

        if (fileList != null) {
            for (file in fileList) {
                files.add(file)
            }
        }

        return files
    }
}
