/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.ktfiles

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.DocumentsContract.EXTRA_INITIAL_URI
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.transaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.toolbar

class MainActivity : AppCompatActivity() {

    private val handleIntentActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val directoryUri = it.data?.data ?: return@registerForActivityResult
            contentResolver.takePersistableUriPermission(
                directoryUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            showDirectoryContents(directoryUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val openDirectoryButton = findViewById<FloatingActionButton>(R.id.fab_open_directory)
        openDirectoryButton.setOnClickListener {
            openDirectory()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val directoryOpen = supportFragmentManager.backStackEntryCount > 0
            supportActionBar?.let { actionBar ->
                actionBar.setDisplayHomeAsUpEnabled(directoryOpen)
                actionBar.setDisplayShowHomeEnabled(directoryOpen)
            }

            if (directoryOpen) {
                openDirectoryButton.visibility = View.GONE
            } else {
                openDirectoryButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        supportFragmentManager.popBackStack()
        return false
    }

    fun showDirectoryContents(directoryUri: Uri) {
        supportFragmentManager.commit {
            val directoryTag = directoryUri.toString()
            val directoryFragment = DirectoryFragment.newInstance(directoryUri)
            replace(R.id.fragment_container, directoryFragment, directoryTag)
            addToBackStack(directoryTag)
        }
    }

    private fun openDirectory() {
        val uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Android/data")
        val treeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", "primary:Android/data")
        contentResolver.persistedUriPermissions.find {
            it.uri.equals(treeUri) && it.isReadPermission
        }?.run {
            showDirectoryContents(treeUri)
        } ?: handleIntentActivityResult.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).putExtra(EXTRA_INITIAL_URI, uri))
    }
}
