package com.bharathkalyans.firebasedemo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private val personCollectionRef = Firebase.firestore.collection("persons")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnSaveToDatabase.setOnClickListener {
            val firstName = firstName.text.toString()
            val lastName = lastName.text.toString()
            val age = age.text.toString().toInt()

            val person = Person(firstName, lastName, age)

            savePerson(person)
        }

        //Realtime Updating of TextView when data changes!
        subscribeToRealTimeUpdates()

        /*btnRetrieveDatabase.setOnClickListener {
            retrievePersons()
        }
*/
    }

    private fun subscribeToRealTimeUpdates() {
        personCollectionRef.addSnapshotListener { querySnapShot, firebaseException ->
            firebaseException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            querySnapShot?.let {
                val sb = StringBuilder()
                for (documents in it) {
                    val person = documents.toObject<Person>()
                    sb.append("$person\n")
                }
                tvPersonData.text = sb.toString()
            }
        }
    }

    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = personCollectionRef.get().await()
            val sb = StringBuilder()
            for (documents in querySnapshot.documents) {
                val person = documents.toObject(Person::class.java)
                sb.append("$person\n")
            }

            withContext(Dispatchers.Main) {
                tvPersonData.text = sb
            }
        } catch (e: Exception) {
            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            personCollectionRef.add(person).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Successfully Saved Data", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

}













