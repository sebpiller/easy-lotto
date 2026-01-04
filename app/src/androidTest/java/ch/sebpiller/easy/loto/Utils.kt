package ch.sebpiller.easy.loto

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream


fun saveBitmapToFile(bitmap: Bitmap, filename: String) {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val file = File(context.getExternalFilesDir(null), filename)
    println(file.absolutePath)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
}


