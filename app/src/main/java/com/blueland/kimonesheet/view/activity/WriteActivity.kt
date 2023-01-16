package com.blueland.kimonesheet.view.activity

import android.app.Activity
import com.blueland.kimonesheet.R
import com.blueland.kimonesheet.base.BaseActivity
import com.blueland.kimonesheet.databinding.ActivityWriteBinding

class WriteActivity : BaseActivity<ActivityWriteBinding>(R.layout.activity_write) {

    override fun initListener() {
        super.initListener()
        binding.apply {
            btnSave.setOnClickListener {
                // TODO: 저장
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}