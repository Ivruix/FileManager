package io.github.ivruix.filemanager

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity(), FileAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        var path = Environment.getExternalStorageDirectory().absolutePath

        if (intent.hasExtra("path")) {
            path = intent.getStringExtra("path")
        }

        adapter = FileAdapter(getFiles(path))
        adapter.setOnItemClickListener(this)
        recyclerView.adapter = adapter
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
