package com.bharathkalyans.firebasedemo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.SetOptions
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
            val person = getOldPerson()
            savePerson(person)
        }

        btnUpdateToDatabase.setOnClickListener {
            val oldPerson = getOldPerson()
            val newPersonMap = getNewPersonMap()
            updatePerson(oldPerson, newPersonMap)
        }

        btnRetrieveDatabase.setOnClickListener {
            retrievePersons()
        }

        btnDelete.setOnClickListener {
            deletePerson(getOldPerson())
        }

        btnBatchWrite.setOnClickListener {
            changeName("9STzpdnXJ0lQ4CgNzpP2","Elon","Musk")
        }
    }

    private fun changeName(
        personId: String,
        firstName: String,
        lastName: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Firebase.firestore.runBatch { batch ->
                val personRef = personCollectionRef.document(personId)
                batch.update(personRef, "firstName", firstName)
                batch.update(personRef, "lastName", lastName)

            }.await()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun deletePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()
        if (personQuery.documents.isNotEmpty()) {
            for (document in personQuery) {
                try {
                    //Delete's the Whole Document!
                    personCollectionRef.document(document.id).delete().await()
                    //Delete's Certain Value
//                    personCollectionRef.document(document.id).update(mapOf(
//                        "firstName" to FieldValue.delete()
//                    ))
                } catch (e: java.lang.Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "No Person Matched!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }

    private fun updatePerson(person: Person, newPersonMap: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {
            val personQuery = personCollectionRef
                .whereEqualTo("firstName", person.firstName)
                .whereEqualTo("lastName", person.lastName)
                .whereEqualTo("age", person.age)
                .get()
                .await()
            if (personQuery.documents.isNotEmpty()) {
                for (document in personQuery) {
                    try {
                        personCollectionRef.document(document.id).set(
                            newPersonMap, SetOptions.merge()
                        ).await()

                    } catch (e: java.lang.Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "No Person Matched!", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }

    private fun getNewPersonMap(): Map<String, Any> {
        val firstName = newFirstName.text
        val lastName = newLastName.text
        val age = newAge.text.toString()

        val map = mutableMapOf<String, Any>()

        if (firstName.isNotEmpty()) {
            map["firstName"] = firstName.toString()
        }
        if (lastName.isNotEmpty()) {
            map["lastName"] = lastName.toString()
        }
        if (firstName.isNotEmpty()) {
            map["age"] = age.toInt()
        }
        return map
    }

    private fun getOldPerson(): Person {
        val firstName = firstName.text.toString()
        val lastName = lastName.text.toString()
        val age = age.text.toString().toInt()

        return Person(firstName, lastName, age)
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
        //Below Listener will over ride the above listener making the Person Data not Visible!
//        studentCollectionRef.addSnapshotListener { querySnapShot, firebaseException ->
//            firebaseException?.let {
//                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
//                return@addSnapshotListener
//            }
//            querySnapShot?.let {
//                val sb = StringBuilder()
//                for (documents in it) {
//                    val student = documents.toObject<Student>()
//                    sb.append("$student\n")
//                }
//                tvPersonData.text = sb.toString()
//            }
//        }
    }

    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
        val age1 = etAge1.text.toString().toInt()
        val age2 = etAge2.text.toString().toInt()

        try {
            val querySnapshot = personCollectionRef
                .whereGreaterThan("age", age1)
                .whereLessThan("age", age2)
                .orderBy("age")
                .get()
                .await()

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
//            studentCollectionRef.add(Student("Student")).await()
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













