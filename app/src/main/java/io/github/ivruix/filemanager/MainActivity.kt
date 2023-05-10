package io.github.ivruix.filemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

class MainActivity : AppCompatActivity(), FileAdapter.OnItemClickListener,
    FileAdapter.OnFileLongClickListener {
    enum class SortBy {
        SORT_BY_NAME, SORT_BY_SIZE, SORT_BY_TIME_OF_CREATION, SORT_BY_EXTENSION
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 123
    }

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FileAdapter

    private var sortBy: SortBy = SortBy.SORT_BY_NAME
    private var sortAscending: Boolean = true

    private var currentPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get current path
        currentPath = intent.getStringExtra("path")

        // Check for permissions
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_CODE_PERMISSIONS)
        } else {
            initRecyclerView()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
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

    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FileAdapter(this, getFiles())
        adapter.setOnItemClickListener(this)
        adapter.setOnFileLongClickListener(this)
        recyclerView.adapter = adapter
    }

    // Checks whether all necessary permissions are granted
    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onItemClick(file: File) {
        if (file.isDirectory) {
            // If user clicked on a directory navigate into it
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("path", file.absolutePath)
            startActivity(intent)
        } else {
            // If user clicked on a file open it
            launchFile(file)
        }
    }

    override fun onFileLongClick(file: File, view: View) {
        // Share file on long click
        val uri =
            FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "*/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Share file using"))
    }

    private fun launchFile(file: File) {
        val uri =
            FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        val mimeType = getMimeType(uri)

        intent.setDataAndType(uri, mimeType)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Check if an app exists that can open this file
        // If there are multiple suitable apps then give user a choice between them
        val chooser = Intent.createChooser(intent, "Open with")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        }
    }

    private fun getMimeType(uri: Uri): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    private fun getFiles(): ArrayList<File> {
        val files = ArrayList<File>()
        val directory = File(currentPath ?: Environment.getExternalStorageDirectory().absolutePath)

        val fileList = directory.listFiles()

        if (fileList != null) {
            for (file in fileList) {
                files.add(file)
            }
        }

        // Sort files
        when (sortBy) {
            SortBy.SORT_BY_NAME -> files.sortBy { it.name }
            SortBy.SORT_BY_SIZE -> files.sortBy { if (it.isFile) it.length() else 0 }
            SortBy.SORT_BY_TIME_OF_CREATION -> files.sortBy { getFileTimeOfCreation(it) }
            SortBy.SORT_BY_EXTENSION -> files.sortBy { if (it.isFile) it.extension else "" }
        }
        if (!sortAscending) {
            files.reverse()
        }

        return files
    }

    private fun getFileTimeOfCreation(file: File): Long {
        val attr = Files.readAttributes(
            file.toPath(), BasicFileAttributes::class.java
        )
        return attr.creationTime().toMillis()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sort_by_name -> {
                sortBy = SortBy.SORT_BY_NAME
                initRecyclerView()
            }

            R.id.menu_sort_by_size -> {
                sortBy = SortBy.SORT_BY_SIZE
                initRecyclerView()
            }

            R.id.menu_sort_by_time_of_creation -> {
                sortBy = SortBy.SORT_BY_TIME_OF_CREATION
                initRecyclerView()
            }

            R.id.menu_sort_by_extension -> {
                sortBy = SortBy.SORT_BY_EXTENSION
                initRecyclerView()
            }

            R.id.menu_sort_ascending -> {
                sortAscending = true
                initRecyclerView()
            }

            R.id.menu_sort_descending -> {
                sortAscending = false
                initRecyclerView()
            }
        }

        return true
    }

    override fun onDestroy() {
        // Update file hashes
        val db = FileHashDatabaseHelper(this)
        for (file in getFiles()) {
            db.insertFileHash(file)
        }
        super.onDestroy()
    }
}
