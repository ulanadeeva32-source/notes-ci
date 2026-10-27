package com.example.notes.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.notes.R
import com.example.notes.data.local.entity.Note
import com.example.notes.databinding.DialogAddNoteBinding
import com.example.notes.ui.viewmodel.NoteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddNoteDialogFragment : DialogFragment() {

    private val viewModel: NoteViewModel by activityViewModels()
    private lateinit var binding: DialogAddNoteBinding

    /** Открытая заметка; null, если создаём новую. */
    private val editedNote: Note? by lazy {
        arguments?.takeIf { it.containsKey(ARG_ID) }?.let {
            Note(
                id = it.getInt(ARG_ID),
                title = it.getString(ARG_TITLE).orEmpty(),
                content = it.getString(ARG_CONTENT).orEmpty(),
                timestamp = it.getLong(ARG_TIMESTAMP)
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddNoteBinding.inflate(layoutInflater)
        val note = editedNote

        // Черновик заполняем только при первом открытии (не при повороте экрана)
        if (savedInstanceState == null) {
            if (note != null) {
                viewModel.draftTitle = note.title
                viewModel.draftContent = note.content
                viewModel.clearError()
            } else {
                viewModel.clearDraft()
            }
        }

        binding.titleInput.setText(viewModel.draftTitle)
        binding.contentInput.setText(viewModel.draftContent)

        binding.titleInput.doAfterTextChanged {
            viewModel.draftTitle = it?.toString().orEmpty()
            viewModel.clearError()
        }
        binding.contentInput.doAfterTextChanged {
            viewModel.draftContent = it?.toString().orEmpty()
            viewModel.clearError()
        }

        viewModel.error.observe(this) { message ->
            binding.titleLayout.error = message
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (note == null) R.string.new_note else R.string.edit_note)
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel) { _, _ -> viewModel.clearDraft() }
            .create()
    }

    override fun onStart() {
        super.onStart()
        (dialog as? AlertDialog)
            ?.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setOnClickListener {
                val title = binding.titleInput.text?.toString().orEmpty()
                val content = binding.contentInput.text?.toString().orEmpty()
                val note = editedNote
                val saved = if (note == null) {
                    viewModel.addNote(title, content)
                } else {
                    viewModel.updateNote(note, title, content)
                }
                if (saved) {
                    viewModel.clearDraft()
                    dismiss()
                }
            }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        viewModel.clearDraft()
    }

    companion object {
        const val TAG = "AddNoteDialog"
        private const val ARG_ID = "id"
        private const val ARG_TITLE = "title"
        private const val ARG_CONTENT = "content"
        private const val ARG_TIMESTAMP = "timestamp"

        fun newInstance(note: Note?): AddNoteDialogFragment =
            AddNoteDialogFragment().apply {
                if (note != null) {
                    arguments = Bundle().apply {
                        putInt(ARG_ID, note.id)
                        putString(ARG_TITLE, note.title)
                        putString(ARG_CONTENT, note.content)
                        putLong(ARG_TIMESTAMP, note.timestamp)
                    }
                }
            }
    }
}