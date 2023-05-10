package io.github.ivruix.filemanager

import android.graphics.BitmapFactory
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FileAdapter(private val files: ArrayList<File>) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(file: File)
    }

    interface OnFileLongClickListener {
        fun onFileLongClick(file: File, view: View)
    }

    private var onItemClickListener: OnItemClickListener? = null
    private var onFileLongClickListener: OnFileLongClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        val fileIcon: ImageView = itemView.findViewById(R.id.file_icon)
        val fileName: TextView = itemView.findViewById(R.id.file_name)
        val fileSize: TextView = itemView.findViewById(R.id.file_size)
        val fileDate: TextView = itemView.findViewById(R.id.file_date)

        init {
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(files[adapterPosition])
            }

            itemView.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            val position = adapterPosition
            val file = files[position]
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

        holder.fileName.text = file.name

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US)
        holder.fileDate.text = simpleDateFormat.format(Date(getFileTimeOfCreation(file)))

        if (file.isDirectory) {
            holder.fileIcon.setImageResource(R.drawable.folder)
            holder.fileSize.text = "Folder"
        } else {
            holder.fileSize.text = bytesToString(file.length())

            when (file.extension) {
                "pdf" -> holder.fileIcon.setImageResource(R.drawable.pdf)
                "rar" -> holder.fileIcon.setImageResource(R.drawable.rar)
                "txt" -> holder.fileIcon.setImageResource(R.drawable.txt)
                "zip" -> holder.fileIcon.setImageResource(R.drawable.zip)
                "png", "jpg" -> {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    holder.fileIcon.setImageBitmap(bitmap)
                }
                else -> holder.fileIcon.setImageResource(R.drawable.blank)
            }
        }
    }

    private fun getFileTimeOfCreation(file: File) : Long {
        val attr = Files.readAttributes(
            file.toPath(),
            BasicFileAttributes::class.java
        )
        return attr.creationTime().toMillis()
    }

    private fun bytesToString(bytes: Long) : String {
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
