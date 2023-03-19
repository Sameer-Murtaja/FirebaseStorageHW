package com.myhomework.firebasestoragehw

import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.myhomework.firebasestoragehw.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var ref: StorageReference
    private var progressDialog: ProgressDialog? = null
    val REQUEST_CODE = 100
    val FILE_NAME = "pdf File"
    var pdfPath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storage = Firebase.storage
        ref = storage.reference

        checkLastUploadedFile()

        binding.btnChoosePdfFile.setOnClickListener {
            pickPDFFile()
        }

        binding.btnUploadFile.setOnClickListener {
            pdfPath?.let { pdfPath ->
                showProgressDialog("Uploading..")
                ref.child("pdf/${FILE_NAME}.pdf")
                    .putFile(pdfPath)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "Upload Success", Toast.LENGTH_SHORT)
                            .show()
                        binding.tvUploadedFileName.text = pdfPath.lastPathSegment
                        hideProgressDialog()
                    }
                    .addOnFailureListener {
                        Log.e("Sam", "onCreate: ${it.message}")
                        Toast.makeText(applicationContext, "Upload failed, ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                        hideProgressDialog()
                    }
            } ?: run {
                Toast.makeText(this, "no file selected", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDownloadFile.setOnClickListener {
            showProgressDialog("Downloading..")
            ref.child("pdf/${FILE_NAME}.pdf")
                .downloadUrl
                .addOnSuccessListener {
                    downloadFile(it.toString(), "pdf file")
                    Toast.makeText(applicationContext, "Download Success", Toast.LENGTH_SHORT)
                        .show()
                    hideProgressDialog()
                }
                .addOnFailureListener {
                    Log.e("Sam", "onCreate: ${it.message}")
                    Toast.makeText(applicationContext, "Download failed, ${it.message}", Toast.LENGTH_SHORT)
                        .show()
                    hideProgressDialog()
                }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                pdfPath = uri
                binding.tvSelectedFileName.text = pdfPath?.lastPathSegment
            }
        }
    }

    private fun checkLastUploadedFile(){
        showProgressDialog("checking last uploaded file..")
        ref.child("pdf/${FILE_NAME}.pdf")
            .downloadUrl
            .addOnSuccessListener {
                binding.tvUploadedFileName.text = FILE_NAME
                hideProgressDialog()
            }.addOnFailureListener {
                hideProgressDialog()
                Toast.makeText(this, "no uploaded file found", Toast.LENGTH_SHORT).show()
            }
    }

    private fun pickPDFFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun downloadFile(downloadUrl: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(fileName)
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }


    private fun showProgressDialog(msg:String) {
        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage(msg)
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (progressDialog!!.isShowing)
            progressDialog!!.dismiss()
    }
}