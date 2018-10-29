package se.gustavkarlsson.krate.samples.android.gui.fragments.notes

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import se.gustavkarlsson.krate.samples.android.domain.Note

class NoteListAdapter : RecyclerView.Adapter<NoteListAdapter.ViewHolder>() {
    var notes: List<Note> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var onClick: ((Note) -> Unit)? = null
    var onSwipe: ((Note) -> Unit)? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        ItemTouchHelper(SwipeCallback()).attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        view.ellipsize = TextUtils.TruncateAt.END
        view.maxLines = 1
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notes[position], onClick)
    }

    override fun getItemCount() = notes.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(note: Note, onClick: ((Note) -> Unit)?) {
            val textView = itemView as TextView
            textView.text = note.presentationText
            if (onClick != null) {
                itemView.setOnClickListener { onClick(note) }
            }
        }
    }

    inner class SwipeCallback : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeFlag(
                ItemTouchHelper.ACTION_STATE_SWIPE,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            )
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (onSwipe != null) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val note = notes[position]
                    onSwipe?.invoke(note)
                }
            }
        }

        override fun isLongPressDragEnabled() = false
    }

    private val Note.presentationText: String
        get() {
            if (title.isNotBlank()) {
                return title
            }
            return content
        }
}
