package com.tux.simpleclipboadmanager

import android.app.Application
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.androidModule

class MyApplication : Application(), KodeinAware {
  override val kodein = Kodein.lazy {
    import(androidModule(this@MyApplication))
    import(ClipboadManagerModule(this@MyApplication).scmModule)
  }
}