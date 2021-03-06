package jp.techacademy.yuki.naito.qa_app

import java.io.Serializable
import java.util.ArrayList

class Question(val title: String, val body: String, var favorite: String, val name: String, val uid: String, val questionUid: String, val genre: Int, bytes: ByteArray, val answers: ArrayList<Answer>) : Serializable {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}