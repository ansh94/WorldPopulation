package com.anshdeep.worldpopulation.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.anshdeep.worldpopulation.R
import com.anshdeep.worldpopulation.api.model.Contact
import com.anshdeep.worldpopulation.api.model.CountryResult
import com.anshdeep.worldpopulation.api.model.WorldPopulation
import com.anshdeep.worldpopulation.data.CountryRemoteDataSource
import com.anshdeep.worldpopulation.util.plusAssign
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import ir.mahdi.mzip.zip.ZipArchive
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileWriter
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val csvHeader = "name,number"

    private val compositeDisposable = CompositeDisposable()

    private val remoteDataSource = CountryRemoteDataSource()

    private val adapter = CountriesAdapter {
        showFullScreenImage(it)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countries_list.adapter = adapter
        countries_list.layoutManager = LinearLayoutManager(this)

        val dividerItemDecoration = DividerItemDecoration(countries_list.context,
                LinearLayoutManager.VERTICAL)

        countries_list.addItemDecoration(dividerItemDecoration)

        if (isConnectedToInternet()) {
            loadCountries()
        } else {
            Snackbar.make(mainView, "You are not connected to the internet", Snackbar.LENGTH_LONG).show()

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.contacts) {
            checkContactsPermission()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun isConnectedToInternet(): Boolean {
        val connManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        val ni = connManager.activeNetworkInfo
        return ni != null && ni.isConnected
    }

    private fun loadCountries() {
        loading.visibility = View.VISIBLE

        compositeDisposable += remoteDataSource
                .getCountries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<CountryResult>() {

                    override fun onError(e: Throwable) {
                        loading.visibility = View.INVISIBLE
                        Snackbar.make(mainView, "Some error occurred", Snackbar.LENGTH_LONG).show()
                    }

                    // called every time observable emits the data
                    override fun onNext(data: CountryResult) {
                        adapter.swapData(data.worldpopulation)
                    }

                    // called when observable finishes emitting all the data
                    override fun onComplete() {
                        loading.visibility = View.INVISIBLE
                        countries_list.visibility = View.VISIBLE

                    }
                })
    }

    private fun showFullScreenImage(country: WorldPopulation) {
        val intent = Intent(this, FullScreenImageActivity::class.java)
        intent.putExtra("IMAGE_URL", country.flag)
        startActivity(intent)
    }

    private fun checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_CONTACTS)) {

                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
            }
        } else {
            // Permission has already been granted
            Log.d("MainActivity", "permission already granted: ")


            // wrap an expensive method call in a observable
            Observable.fromCallable { generateCsvFile() }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { Snackbar.make(mainView, "Contacts zipped and saved in storage", Snackbar.LENGTH_LONG).show() }
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            2 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty()
                                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                                && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    checkContactsPermission()

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Snackbar.make(mainView, "Permission denied", Snackbar.LENGTH_LONG).show()


                }
                return
            }

        // Add other 'when' lines to check for other
        // permissions this app might request.

            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun getContactNames(): List<Contact> {
        val contacts = arrayListOf<Contact>()

        val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        while (phones!!.moveToNext()) {
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            val contact = Contact(name, phoneNumber)
            contacts.add(contact)
        }
        phones.close()

        return contacts
    }

    private fun generateCsvFile() {

        val contactList = getContactNames()

        var fileWriter: FileWriter? = null

        try {
            fileWriter = FileWriter(Environment.getExternalStorageDirectory().toString() + File.separator + "contacts.csv")

            fileWriter.append(csvHeader)
            fileWriter.append('\n')

            for (contact in contactList) {
                fileWriter.append(contact.contactName)
                fileWriter.append(',')
                fileWriter.append(contact.contactNumber)
                fileWriter.append('\n')
            }

            Log.d("MainActivity", "Write CSV successfully!")

            zip()


        } catch (e: Exception) {
            Log.d("MainActivity", "Writing CSV error!")

            e.printStackTrace()
        } finally {
            try {
                fileWriter?.flush()
                fileWriter?.close()
            } catch (e: IOException) {
                Log.d("MainActivity", "Flushing/closing error!")

                e.printStackTrace()
            }
        }
    }

    private fun zip() {
        val zipArchive = ZipArchive.zip("/storage/emulated/0/contacts.csv", "/storage/emulated/0/contacts.zip", "")

        Log.d("MainActivity", "Zip successful!")
    }


    override fun onDestroy() {
        super.onDestroy()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }
}
