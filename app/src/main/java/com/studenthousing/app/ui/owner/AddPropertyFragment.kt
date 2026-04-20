package com.studenthousing.app.ui.owner

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.studenthousing.app.R
import com.studenthousing.app.StudentHousingApp
import com.studenthousing.app.data.model.CoordinatesDto
import com.studenthousing.app.data.model.CreateLocationDto
import com.studenthousing.app.data.model.CreatePropertyRequest
import com.studenthousing.app.data.repo.ResultState
import com.studenthousing.app.databinding.FragmentAddPropertyBinding
import com.studenthousing.app.ui.CommonViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AddPropertyFragment : Fragment(R.layout.fragment_add_property) {
    private var _binding: FragmentAddPropertyBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AddPropertyViewModel

    private var savedLat: Double? = null
    private var savedLng: Double? = null
    private val selectedPhotoUris = mutableListOf<Uri>()
    private val uploadedImageUrls = mutableListOf<String>()

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val remaining = 6 - selectedPhotoUris.size
            val toAdd = uris.take(remaining)
            selectedPhotoUris.addAll(toAdd)
            updatePhotoCount()
            if (uris.size > remaining) {
                Snackbar.make(binding.root, "Max 6 photos allowed", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddPropertyBinding.bind(view)

        val app = requireContext().applicationContext as StudentHousingApp
        viewModel = ViewModelProvider(this, CommonViewModelFactory(app.container.repository))[AddPropertyViewModel::class.java]

        // Listen for map picker result
        parentFragmentManager.setFragmentResultListener(
            MapPickerFragment.LOCATION_REQUEST_KEY, viewLifecycleOwner
        ) { _, bundle ->
            savedLat = bundle.getDouble(MapPickerFragment.KEY_LAT)
            savedLng = bundle.getDouble(MapPickerFragment.KEY_LNG)
            binding.locationStatus.text = getString(R.string.location_set, savedLat!!, savedLng!!)
            binding.locationStatus.setTextColor(requireContext().getColor(R.color.success))
        }

        binding.useMyLocationButton.setOnClickListener {
            findNavController().navigate(R.id.action_addProperty_to_mapPicker)
        }

        binding.addPhotosButton.setOnClickListener {
            if (selectedPhotoUris.size >= 6) {
                Snackbar.make(binding.root, "Max 6 photos already selected", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            photoPickerLauncher.launch("image/*")
        }

        binding.submitButton.setOnClickListener {
            val title = binding.titleInput.text?.toString().orEmpty().trim()
            val description = binding.descriptionInput.text?.toString().orEmpty().trim()
            val address = binding.addressInput.text?.toString().orEmpty().trim()
            val city = binding.cityInput.text?.toString().orEmpty().trim()
            val priceStr = binding.priceInput.text?.toString().orEmpty().trim()
            val roomsStr = binding.roomsInput.text?.toString().orEmpty().trim()

            binding.titleLayout.error = null
            binding.addressLayout.error = null
            binding.priceLayout.error = null

            if (title.isBlank()) { binding.titleLayout.error = getString(R.string.field_required); return@setOnClickListener }
            if (address.isBlank()) { binding.addressLayout.error = getString(R.string.field_required); return@setOnClickListener }
            if (priceStr.isBlank()) { binding.priceLayout.error = getString(R.string.field_required); return@setOnClickListener }

            val price = priceStr.toDoubleOrNull() ?: run {
                binding.priceLayout.error = "Enter a valid price"
                return@setOnClickListener
            }
            val rooms = roomsStr.toIntOrNull() ?: 1

            val selectedType = when (binding.typeToggle.checkedButtonId) {
                R.id.btnHouse -> "house"
                R.id.btnStudio -> "studio"
                R.id.btnDorm -> "dorm"
                else -> "apartment"
            }

            val coordinates = if (savedLat != null && savedLng != null) CoordinatesDto(savedLat!!, savedLng!!) else null
            val location = CreateLocationDto(
                city = city.ifBlank { null },
                state = null,
                country = "Lebanon",
                coordinates = coordinates
            )

            // Upload photos first, then create property
            binding.addPropertyProgress.visibility = View.VISIBLE
            binding.submitButton.isEnabled = false
            binding.addPropertyError.text = ""

            viewLifecycleOwner.lifecycleScope.launch {
                // Upload selected photos
                uploadedImageUrls.clear()
                for (uri in selectedPhotoUris) {
                    val url = uploadImage(uri, app)
                    if (url != null) uploadedImageUrls.add(url)
                }

                viewModel.createProperty(
                    CreatePropertyRequest(
                        title = title,
                        description = description.ifBlank { null },
                        address = address,
                        location = location,
                        images = uploadedImageUrls.ifEmpty { null },
                        price = price,
                        type = selectedType,
                        rooms = rooms,
                        floor = null,
                        amenities = emptyList()
                    )
                )
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ResultState.Loading -> {
                    binding.addPropertyProgress.visibility = View.VISIBLE
                    binding.submitButton.isEnabled = false
                    binding.addPropertyError.text = ""
                }
                is ResultState.Success -> {
                    binding.addPropertyProgress.visibility = View.GONE
                    binding.submitButton.isEnabled = true
                    Snackbar.make(binding.root, "Property added!", Snackbar.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is ResultState.Error -> {
                    binding.addPropertyProgress.visibility = View.GONE
                    binding.submitButton.isEnabled = true
                    binding.addPropertyError.text = state.message
                }
            }
        }

        updatePhotoCount()
    }

    private fun updatePhotoCount() {
        binding.photoCount.text = getString(R.string.photos_count, selectedPhotoUris.size, 6)
    }

    private suspend fun uploadImage(uri: Uri, app: StudentHousingApp): String? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = app.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val extension = when {
                mimeType.contains("png") -> "png"
                mimeType.contains("gif") -> "gif"
                else -> "jpg"
            }
            val fileName = "property_${System.currentTimeMillis()}.$extension"

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, bytes.toRequestBody(mimeType.toMediaTypeOrNull()))
                .build()

            val baseUrl = com.studenthousing.app.BuildConfig.API_BASE_URL
            val token = app.container.tokenStore.cachedToken

            val request = Request.Builder()
                .url("${baseUrl}upload/single")
                .post(requestBody)
                .apply { if (!token.isNullOrBlank()) addHeader("Authorization", "Bearer $token") }
                .build()

            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && body != null) {
                val json = JSONObject(body)
                if (json.getBoolean("success")) {
                    json.getJSONObject("file").getString("url")
                } else null
            } else null
        } catch (e: Exception) {
            android.util.Log.w("Upload", "Image upload failed", e)
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
