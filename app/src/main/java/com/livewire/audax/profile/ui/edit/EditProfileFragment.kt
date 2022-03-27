package com.livewire.app.profile.ui.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.livewire.audax.R
import com.livewire.audax.profile.ui.ChangePasswordActivity
import com.livewire.audax.utils.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.lang.StringBuilder
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.edit_profile_fragment.*
import kotlinx.android.synthetic.main.profile_fragment.avatarImageView
import kotlinx.android.synthetic.main.profile_fragment.back_button

class EditProfileFragment : Fragment() {
    private val viewModel: EditProfileViewModel by sharedViewModel()
    private val imageLoader: ImageLoader by inject()
    private var progressDialog = CustomDialog()

    companion object {
        fun newInstance() = EditProfileFragment()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.userViewModel.profile.observe(this) {
            loadUserProfile()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.edit_profile_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editPasswordButton.setOnClickListener {
            editPassword()
        }

        saveButton.setOnClickListener {
            saveAndExit()
        }

        back_button.setOnClickListener {
            activity?.onBackPressed()
        }

        loadUserProfile()
        firstNameField.watchText(this::textFieldChanged)
        lastNameField.watchText(this::textFieldChanged)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        saveButton.isEnabled = false
    }


    private fun textFieldChanged(ignored: TextView) {
        dataChanged()
    }

    private fun dataChanged() {
        saveButton.isEnabled = true
    }

    private fun saveAndExit() {
        activity?.hideKeyboard()
        viewModel.first = firstNameField.text.toString().trim()
        viewModel.last = lastNameField.text.toString().trim()
        if (firstNameField.text.toString() == ""){
            firstNameField.error = "First name required"
        }else{
            if (lastNameField.text.toString() == ""){
                lastNameField.error = "Last name required"
            }else{
                progressDialog.show(requireContext())
                viewModel.updateProfile(this::updateProfileSuccess, this::updateProfileFailed)
            }
        }

    }

    private fun updateProfileSuccess() {
        activity?.onBackPressed()
        progressDialog.dismiss(requireContext())
    }

    private fun updateProfileFailed(message: Int) {
        //showError(message)
        progressDialog.dismiss(requireContext())
    }

    private fun uploadProfileImage(uri: Uri) {
        viewModel.uploadProfilePicture(uri, this::profilePictureUploaded, this::profilePictureFailure)
    }

    private fun profilePictureUploaded(url: String) {
        if (!isAdded) {
            return
        }

        imageLoader.loadImageViewWithRoundedBitmap(url, avatarImageView)
        //toggleRemovePictureButton()
        //analytics.track(AnalyticsEvent.UPLOAD_PHOTO_ACTION)
    }

    private fun profilePictureFailure(message: Int) {
        if (!isAdded) {
            return
        }

        activity?.runOnUiThread {
            //showError(message)
        }
    }

    private fun loadUserProfile() {
        val user = viewModel.userViewModel.profile.value ?: return
        if (user.photoURL.isNotEmpty()) {
            imageLoader.loadImageViewWithRoundedBitmap(user.photoURL, avatarImageView)
        }

        email.text = user.email
        firstNameField.setText(user.firstName)
        lastNameField.setText(user.lastName)
        if (user.addressLine1.isNotBlank()) {
            val builder = StringBuilder()

            builder.apply {
                append(user.firstName)
                append(" ")
                append(user.lastName)
                append("\n")
                /*append(user.addressLine1)
                append("\n")

                if (user.addressLine2.trim().isNotBlank()) {
                    append(user.addressLine2)
                    append("\n")
                }

                if (user.addressLine3.trim().isNotBlank()) {
                    append(user.addressLine3)
                    append("\n")
                }

                append(user.addressCity)
                append(", ")
                append(user.addressState)
                append(" ")
                append(user.addressZip)
                append("\n")

                append(user.addressCountry)
                append("\n")
                if (user.mobilePhoneNumber.isNotBlank()) {
                    append(user.mobilePhoneNumber)
                }*/
            }

        }
        saveButton.isEnabled = false

    }

    private fun editPassword() {
        startActivity(Intent(context, ChangePasswordActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            uploadProfileImage(result.uri)
        }
    }
}