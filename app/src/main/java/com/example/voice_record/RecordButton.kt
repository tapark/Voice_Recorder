package com.example.voice_record

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton

class RecordButton(context: Context, attrs: AttributeSet): AppCompatImageButton(context, attrs) {



    fun updateIconWithState(state: State) {
        when(state) {
            State.BEFORE_RECORDING -> {
                setImageResource(R.drawable.ic_record)
            }
            State.ON_RECODING -> {
                setImageResource(R.drawable.ic_stop)
            }
            State.AFTER_RECODING -> {
                setImageResource(R.drawable.ic_play)
            }
            State.ON_PLAYING -> {
                setImageResource(R.drawable.ic_stop)
            }
        }

    }

}