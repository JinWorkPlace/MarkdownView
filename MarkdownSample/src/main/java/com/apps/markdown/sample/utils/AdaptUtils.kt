package com.apps.markdown.sample.utils

import androidx.recyclerview.widget.RecyclerView

val Adapt.recyclerView: RecyclerView?
    get() {
        // internally throws if recycler is not present (detached from recyclerView)
        return try {
            recyclerView()
        } catch (t: Throwable) {
            Debug.e(t)
            null
        }
    }