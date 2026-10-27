package com.example.notes.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notes.R
import com.example.notes.data.local.entity.Note
import com.example.notes.databinding.ActivityMainBinding
import com.example.notes.ui.adapter.NoteAdapter
import com.example.notes.ui.auth.AuthActivity
import com.example.notes.ui.dialog.AddNoteDialogFragment
import com.example.notes.ui.viewmodel.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private val adapter = NoteAdapter(
        onItemClick = { note -> showNoteDialog(note) },
        onDeleteClick = { note -> confirmDelete(note) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.subtitle = viewModel.currentEmail // кто сейчас вошёл

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // StateFlow собирается только пока экран виден (STARTED) и отменяется при выходе
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.notes)
                    binding.emptyView.isVisible = state.notes.isEmpty() && !state.isLoading
                    binding.emptyView.setText(
                        if (viewModel.searchQuery.isBlank()) R.string.empty_list else R.string.nothing_found
                    )
                    state.errorMessage?.let { message ->
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                            .setAnchorView(binding.fab)
                            .show()
                        viewModel.onErrorShown()
                    }
                }
            }
        }

        binding.fab.setOnClickListener { showNoteDialog(null) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)

        val current = viewModel.searchQuery
        if (current.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(current, false)
            searchView.clearFocus()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == R.id.action_profile) {
            showProfileDialog()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    /** Профиль: показывает email и позволяет выйти из аккаунта. */
    private fun showProfileDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.profile)
            .setMessage(getString(R.string.profile_message, viewModel.currentEmail))
            .setPositiveButton(R.string.logout) { _, _ -> logout() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun logout() {
        viewModel.logout()
        startActivity(
            Intent(this, AuthActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    /** note == null — новая заметка, иначе — просмотр и редактирование выбранной. */
    private fun showNoteDialog(note: Note?) {
        if (supportFragmentManager.findFragmentByTag(AddNoteDialogFragment.TAG) == null) {
            AddNoteDialogFragment.newInstance(note)
                .show(supportFragmentManager, AddNoteDialogFragment.TAG)
        }
    }

    /** Вопрос перед удалением. */
    private fun confirmDelete(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_message, note.title))
            .setPositiveButton(R.string.delete) { _, _ -> deleteNote(note) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteNote(note: Note) {
        viewModel.deleteNote(note)
        Snackbar.make(binding.root, R.string.note_deleted, Snackbar.LENGTH_LONG)
            .setAnchorView(binding.fab)
            .setAction(R.string.undo) { viewModel.restoreNote(note) }
            .show()
    }
}