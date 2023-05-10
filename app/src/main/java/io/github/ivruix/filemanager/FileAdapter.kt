package io.github.ivruix.filemanager

import android.content.Context
import android.graphics.BitmapFactory
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FileAdapter(private val context: Context, private val files: ArrayList<File>) :
    RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(file: File)
    }

    interface OnFileLongClickListener {
        fun onFileLongClick(file: File, view: View)
    }

    private var onItemClickListener: OnItemClickListener? = null
    private var onFileLongClickListener: OnFileLongClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        val fileIcon: ImageView = itemView.findViewById(R.id.file_icon)
        val fileName: TextView = itemView.findViewById(R.id.file_name)
        val fileSize: TextView = itemView.findViewById(R.id.file_size)
        val fileDate: TextView = itemView.findViewById(R.id.file_date)
        val constraintLayout: ConstraintLayout = itemView.findViewById(R.id.constraint_layout)

        init {
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(files[adapterPosition])
            }

            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            val position = adapterPosition
            val file = files[position]

            // Add share menu for files
            if (files[position].isFile) {
                menu?.add(Menu.NONE, 0, Menu.NONE, "Share")?.setOnMenuItemClickListener {
                    onFileLongClickListener?.onFileLongClick(file, itemView)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]

        holder.fileName.text = truncateFileName(file.name, 35)

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US)
        holder.fileDate.text = simpleDateFormat.format(Date(getFileTimeOfCreation(file)))

        if (file.isDirectory) {
            holder.fileIcon.setImageResource(R.drawable.folder)
            holder.fileSize.text = "Folder"
        } else {
            holder.fileSize.text = bytesToString(file.length())

            // Check whether the file has been changed
            val db = FileHashDatabaseHelper(context)
            val hash = db.getFileHash(file)
            if (hash != null) {
                val newHash = db.calculateHash(file)
                if (newHash != hash) {
                    holder.constraintLayout.setBackgroundColor(
                        ContextCompat.getColor(
                            context, R.color.light_blue
                        )
                    )
                }
            }

            // Choose an appropriate icon
            when (file.extension) {
                "avi" -> holder.fileIcon.setImageResource(R.drawable.avi)
                "bin" -> holder.fileIcon.setImageResource(R.drawable.bin)
                "doc" -> holder.fileIcon.setImageResource(R.drawable.doc)
                "docx" -> holder.fileIcon.setImageResource(R.drawable.docx)
                "exe" -> holder.fileIcon.setImageResource(R.drawable.exe)
                "mkv" -> holder.fileIcon.setImageResource(R.drawable.mkv)
                "mov" -> holder.fileIcon.setImageResource(R.drawable.mov)
                "mp3" -> holder.fileIcon.setImageResource(R.drawable.mp3)
                "mp4" -> holder.fileIcon.setImageResource(R.drawable.mp4)
                "pdf" -> holder.fileIcon.setImageResource(R.drawable.pdf)
                "ppt" -> holder.fileIcon.setImageResource(R.drawable.ppt)
                "rar" -> holder.fileIcon.setImageResource(R.drawable.rar)
                "txt" -> holder.fileIcon.setImageResource(R.drawable.txt)
                "xls" -> holder.fileIcon.setImageResource(R.drawable.xls)
                "xlsx" -> holder.fileIcon.setImageResource(R.drawable.xlsx)
                "zip" -> holder.fileIcon.setImageResource(R.drawable.zip)
                "png", "jpg", "bmp" -> {
                    // Set preview for images
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    holder.fileIcon.setImageBitmap(bitmap)
                }

                else -> holder.fileIcon.setImageResource(R.drawable.blank)
            }
        }
    }

    private fun truncateFileName(fileName: String, maxLength: Int): String {
        return if (fileName.length <= maxLength) {
            fileName
        } else {
            fileName.substring(0, maxLength - 3) + "..."
        }
    }


    private fun getFileTimeOfCreation(file: File): Long {
        val attr = Files.readAttributes(
            file.toPath(), BasicFileAttributes::class.java
        )
        return attr.creationTime().toMillis()
    }

    private fun bytesToString(bytes: Long): String {
        return "$bytes B"
    }

    override fun getItemCount(): Int {
        return files.size
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    fun setOnFileLongClickListener(onFileLongClickListener: OnFileLongClickListener) {
        this.onFileLongClickListener = onFileLongClickListener
    }
}
