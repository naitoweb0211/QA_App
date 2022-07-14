package jp.techacademy.yuki.naito.qa_app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.activity_question_send.*
import java.io.ByteArrayOutputStream


class QuestionDetailActivity : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private var mGenre: Int = 5
    var favorite = ""
    var questionUid = ""
    val user = FirebaseAuth.getInstance().currentUser
    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?: ""
            val name = map["name"] as? String ?: ""
            val uid = map["uid"] as? String ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                Log.d("お気に入り", dataSnapshot.getValue().toString())
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        title = mQuestion.title
        var dataBaseReference = FirebaseDatabase.getInstance().reference
        var favoriteReference = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(FavoritePATH)
        favoriteReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                favorite = dataSnapshot.getValue(String::class.java).toString()
                Log.d("お気に入り", favorite)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("seit", "ValueEventListener#onCancelled")
                // サーバーエラーかもしくはセキュリティとデータべーすルールによってデータにアクセスできない
            }
        })

        if(mQuestion.favorite.equals("")){
            Log.d("お気に入り","お気に入りではありません")
            favoriteImageView.setImageResource(R.drawable.ic_star_border)
        }else{
            Log.d("お気に入り","お気に入りです")
            favoriteImageView.setImageResource(R.drawable.ic_star)
        }
        favoriteImageView.setOnClickListener(this)
        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // --- ここまで ---
            }
        }

        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(
            FavoritePATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }

    override fun onClick(v: View) {
        var dataBaseReference = FirebaseDatabase.getInstance().reference
        var favoriteReference =
            dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString())
                .child(mQuestion.questionUid).child(
                FavoritePATH
            )
        if (v == favoriteImageView) {
            Log.d("クリックされました", "クリックされました")
            if (mQuestion.favorite.equals("")) {
                //favoriteImageView.setImageResource(R.drawable.ic_star)
                dataBaseReference = FirebaseDatabase.getInstance().reference
                favoriteReference =
                    dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString())
                        .child(mQuestion.questionUid).child(
                        FavoritePATH
                    )
                favoriteReference.setValue("お気に入り")
                dataBaseReference = FirebaseDatabase.getInstance().reference
                favoriteReference =
                    dataBaseReference.child(FavoritePATH).child(user!!.uid.toString()).child(mQuestion.questionUid)

                Log.d("パス", favoriteReference.toString())
                val data = HashMap<String, String>()
                data["uid"] = mQuestion.uid
                data["title"] = mQuestion.title
                data["body"] = mQuestion.body
                data["name"] = mQuestion.name
                val bytes = mQuestion.imageBytes
                val image: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
                val baos = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                Log.d("data[\"image\"]", bitmapString)
                data["image"] = bitmapString
                favoriteReference.setValue(data, this)
                Log.d("パス2", favoriteReference.toString())
                //progressBar.visibility = View.VISIBLE
                //mAdapter.notifyDataSetChanged()
                //listView.adapter = mAdapter
            } else {
                Log.d("お気に入りから削除されました", favoriteReference.toString())
                //favoriteImageView.setImageResource(R.drawable.ic_star_border)
                dataBaseReference = FirebaseDatabase.getInstance().reference
                favoriteReference =
                    dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString())
                        .child(mQuestion.questionUid).child(
                        FavoritePATH
                    )
                favoriteReference.setValue("")
                dataBaseReference = FirebaseDatabase.getInstance().reference
                favoriteReference =
                    dataBaseReference.child(FavoritePATH).child(user!!.uid.toString()).child(mQuestion.questionUid)
                favoriteReference.removeValue()
            }
    /*        progressBar.visibility = View.VISIBLE*/
            mAdapter.notifyDataSetChanged()
            listView.adapter = mAdapter
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
        progressBar.visibility = View.GONE

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.question_send_error_message), Snackbar.LENGTH_LONG).show()
        }
    }
}