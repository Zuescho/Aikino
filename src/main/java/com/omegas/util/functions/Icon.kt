package com.omegas.util.functions

import com.omegas.model.Icon
import com.omegas.util.AlertType
import com.omegas.util.Preferences.hideIcon
import javafx.application.Platform
import javafx.scene.control.ButtonType
import net.sf.image4j.codec.ico.ICOEncoder
import org.ini4j.Wini
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.concurrent.thread


@Throws(Exception::class)
private fun convertToIcon(file: File):String{
    val pngFileName = file.toString()
    var outputFile = File(pngFileName.replace(".png", " Icon.ico"))
    var version = 1
    while(outputFile.exists()){
        outputFile =  File(pngFileName.replace(".png","  Icon v$version.ico"))
        version++
    }
    val bi = ImageIO.read(file)
    ICOEncoder.write(bi, outputFile)
    return outputFile.name
}

private fun setIcon(folderPath: String, iconName:String, hideFile: Boolean) {
    try {
        val diPath = File("$folderPath\\desktop.ini")
        if (!diPath.exists()) {
            val writer = FileWriter(diPath)
            writer.write("")
            writer.close()
            val desktopIni = Wini(diPath)
            desktopIni.put(".ShellClassInfo", "IconResource", "$iconName,0")
            desktopIni.put("ViewState", "Mode", null)
            desktopIni.put("ViewState", "Vid", null)
            desktopIni.put("ViewState", "FolderType", "Pictures")
            desktopIni.store()
            val runtime = Runtime.getRuntime()
            runtime.exec("attrib +h +s \"$diPath\"")
            runtime.exec("attrib +s \"$folderPath\"")
            if (hideFile) {
                Files.setAttribute(File("$folderPath\\$iconName").toPath(), "dos:hidden", true)
            }
            showMessage("Icon applied to folder $folderPath", AlertType.INFO, "Icon applied successfully")
        } else if (diPath.exists()) {
            Platform.runLater{// displays alert on FX thread
                val alert = displayAlert("Folder already has an icon. Do you want to overwrite icon?",
                    "Overwrite Icon Confirmation",false,
                    ButtonType.YES,
                    ButtonType.NO)
                alert.showAndWait()
                if(alert.result == ButtonType.YES){
                    thread(true) {
                        diPath.delete()
                        setIcon(folderPath, iconName, hideFile)
                    }
                }else{
                    thread {
                        File("$folderPath\\$iconName").delete()
                    }
                }
                alert.close()
            }
        }
    } catch (e: Exception) {
        Platform.runLater {
            exceptionDialog(e)
        }
    }
}


private fun applyIcon(icon: Icon){
    setIcon(icon.file.absolutePath, icon.iconName, hideIcon)
}

fun createIcon(pngFile:File?, delete:Boolean):Icon?{
    val name =
        pngFile?.let{
            val iconName = convertToIcon(it)
            if(delete){
                it.delete()
            }
            iconName
        }
    return if(pngFile == null){
        null
    }else{
        Icon(pngFile, name!!)
    }
}

fun applyIcon(icon: Icon?, file: File){
    val fileToDelete = icon?.file
    icon?.file = file
    if(icon!=null){
        applyIcon(icon)
    }else{
        showMessage("Icon could not be created.", AlertType.ERROR,"Icon Creation Failed")
    }
    fileToDelete?.delete()
}