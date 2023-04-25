package com.example.mystoryapp.customview

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.example.mystoryapp.R

class PassEditText : AppCompatEditText {

    var isValidPass: Boolean = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val pass = text?.trim()
                when {
                    pass.isNullOrEmpty() -> {
                        isValidPass = false
                        error = resources.getString(R.string.input_pass)
                    }
                    pass.length < 8 -> {
                        isValidPass = false
                        error = resources.getString(R.string.pass_length)
                    }
                    else -> {
                        isValidPass = true
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }
}