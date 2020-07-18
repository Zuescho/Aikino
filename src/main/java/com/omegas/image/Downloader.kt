package com.omegas.image

import com.omegas.util.exceptionDialog
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object Downloader {
    @Throws(java.lang.Exception::class)
    fun getFileName(url: String?): String? {
        return url?.let{url.substring(url.lastIndexOf("/"))}
    }
    fun getImage(imageURL: String): Image {
        return Image(getFullURL(imageURL))
    }

    fun getFullURL(imageURL: String, size:String = "w1280"):String{
        return "https://image.tmdb.org/t/p/$size$imageURL"
    }
    fun download(imageURL: String?, folder: String, image:Image): String? {
        return try {
            val folderFile = File(folder)
            if(!folderFile.exists()){
                folderFile.mkdirs()
            }
            print(folder)
            val outputFile = File(folder + "/" + getFileName(imageURL))
            if (imageURL != null) {
                val bufferedImage: BufferedImage = SwingFXUtils.fromFXImage(image,null)
                ImageIO.write(bufferedImage,"jpg", outputFile)
            }
            outputFile.absolutePath
        } catch (e: Exception) {
            exceptionDialog(e)
            null
        }
    }
}