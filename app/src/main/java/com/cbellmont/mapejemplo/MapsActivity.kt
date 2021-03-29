package com.cbellmont.mapejemplo

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cbellmont.mapejemplo.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.material.snackbar.Snackbar
import java.util.*


class MapsActivity : AppCompatActivity(), PlacesAdapter.OnItemClicked {

    private lateinit var binding: ActivityMapsBinding

    private val latLngMadrid = LatLng(40.4167, -3.70325)

    private val token = AutocompleteSessionToken.newInstance()

    private lateinit var autocompleteAdapter : PlacesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        autocompleteAdapter = PlacesAdapter(this)
        binding.rvSuggestions.layoutManager = LinearLayoutManager(this)
        binding.rvSuggestions.adapter = autocompleteAdapter

        binding.etAddress.addTextChangedListener(watcher)
    }


    override fun onItemClicked(place: String) {
        binding.etAddress.removeTextChangedListener (watcher)
        autocompleteAdapter.updateData(listOf())
        binding.etAddress.setText(place)
        binding.etAddress.addTextChangedListener (watcher)
    }

    private val watcher = object : TextWatcher{
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(textNullable: Editable?) {
            textNullable?.let { text ->
                Log.d(MapsActivity::class.java.name, "El usuario ha escrito $text")

                if (text.count() >= 3) {
                    Log.d(MapsActivity::class.java.name, "Comenzamos a filtrar $text")
                    if (!Places.isInitialized()) {
                        Places.initialize(this@MapsActivity, BuildConfig.MAPS_API_KEY, Locale.getDefault())
                    }

                    val placesClient = Places.createClient(this@MapsActivity)

                    val request = FindAutocompletePredictionsRequest.builder()
                        .setOrigin(latLngMadrid)
                        .setCountries("ES")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(text.toString())
                        .build()

                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                            val predictionsList = mutableListOf<String>()
                            Log.d(MapsActivity::class.java.name, "Se han recibido las siguientes sugerencias:")
                            for (prediction in response.autocompletePredictions) {
                                predictionsList.add(prediction.getFullText(null).toString())
                                Log.d(MapsActivity::class.java.name, "${prediction.getFullText(null)}")
                            }
                            autocompleteAdapter.updateData(predictionsList)
                        }.addOnFailureListener { exception: Exception? ->
                            if (exception is ApiException) {
                                Snackbar.make(binding.root, "Se ha producido un error en la respuesta de Google", Snackbar.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }
    }

}