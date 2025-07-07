package com.apps.markdown.sample.sample.ui.adapt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.apps.markdown.sample.R
import io.noties.adapt.Item

class CheckForUpdateItem(private val action: () -> Unit) : Item<CheckForUpdateItem.Holder>(43L) {

    override fun createHolder(inflater: LayoutInflater, parent: ViewGroup): Holder {
        return Holder(inflater.inflate(R.layout.adapt_check_for_update, parent, false))
    }

    override fun render(holder: Holder) {
        holder.button.setOnClickListener { action() }
    }

    class Holder(view: View) : Item.Holder(view) {
        val button: View = requireView(R.id.button)
    }
}