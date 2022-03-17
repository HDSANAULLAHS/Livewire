package com.livewire.audax.profile.ui.edit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.livewire.audax.App
import com.livewire.audax.R
import com.livewire.audax.authentication.ui.login.LoginFragment
import com.livewire.audax.locale.LinksLocalizer
import com.livewire.audax.utils.*
import kotlinx.android.synthetic.main.profile_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.lang.StringBuilder
import com.theartofdev.edmodo.cropper.CropImage

class EditProfileFragment : Fragment() {
    private val viewModel: EditProfileViewModel by sharedViewModel()
    private val imageLoader: ImageLoader by inject()
    /*private val analytics: AnalyticsManager by inject()*/
    private val links: LinksLocalizer by inject()
    private val preferences: App by inject()

    companion object {
        fun newInstance() = LoginFragment()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.userViewModel.profile.observe(this, {
            loadUserProfile()
        })

        /* if (savedInstanceState == null) {
             analytics.track(AnalyticsEvent.VIEW_EDIT_PROFILE_PAGE)
         }*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.edit_profile_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editEmailButton.isEnabled = viewModel.canEditEmail

        toggleRemovePictureButton()

        avatarImageView.setOnClickListener {
            selectProfileImage()
        }

        camera.setOnClickListener {
            selectProfileImage()
        }

        removeProfilePhotoButton.setOnClickListener {
            removeProfileImage()
        }


        editEmailButton.setOnClickListener {
            editEmail()
        }

        editPasswordButton.setOnClickListener {
            editPassword()
        }

        saveButton.setOnClickListener {
            saveAndExit()
        }

        btn_edit_address.setOnClickListener {
            editAddress()
        }

        back_button.setOnClickListener {
            activity?.onBackPressed()
        }

        tv_emailOptIn.linkStarredText(
            mapOf(
                //getString(R.string.opt_in_link_privacy) to links.privacyPolicyLink,
            )
        ) {
            activity?.openExternalBrowser(it)
        }

        loadUserProfile()
        firstNameField.watchText(this::textFieldChanged)
        middleNameField.watchText(this::textFieldChanged)
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
        viewModel.middle = middleNameField.text.toString().trim()
        viewModel.last = lastNameField.text.toString().trim()
        viewModel.emailOptIn = emailOptIn.isChecked
        viewModel.updateProfile(this::updateProfileSuccess, this::updateProfileFailed)
    }

    private fun updateProfileSuccess() {
        //showConfirmation(R.string.changes_saved, toast = true)
        Toast.makeText(requireContext(), "Changes saved", Toast.LENGTH_SHORT).show()
    }

    private fun updateProfileFailed(message: Int) {
        //showError(message)
    }


    private fun removeProfileImage() {

        viewModel.removeProfilePicture(this::profilePictureRemoved, this::profilePictureFailure)
    }

    private fun uploadProfileImage(uri: Uri) {
        viewModel.uploadProfilePicture(uri, this::profilePictureUploaded, this::profilePictureFailure)
    }

    private fun profilePictureRemoved() {
        if (!isAdded) {
            return
        }

        toggleRemovePictureButton()
        avatarImageView.setImageResource(R.drawable.ic_avatar_placeholder)
        camera.setImageResource(R.drawable.edit_pic)
    }

    private fun profilePictureUploaded(url: String) {
        if (!isAdded) {
            return
        }

        imageLoader.loadImageViewWithRoundedBitmap(url, avatarImageView)
        toggleRemovePictureButton()
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

    private fun selectProfileImage() {
        CropImage.activity().setAspectRatio(1, 1)
            .setFixAspectRatio(true)
            .setRequestedSize(1024, 1024)
            .start(requireContext(), this)
    }

    private fun loadUserProfile() {
        val user = viewModel.userViewModel.profile.value ?: return

        if (user.photoURL.isNotEmpty()) {
            imageLoader.loadImageViewWithRoundedBitmap(user.photoURL, avatarImageView)
        }

        val stateArray = resources.getStringArray(R.array.state_array)
        email.text = user.email
        firstNameField.setText(user.firstName)
        middleNameField.setText(user.middleName)
        lastNameField.setText(user.lastName)
        emailOptIn.isChecked = user.emailOptIn

        if (user.addressLine1.isNotBlank()) {
            val builder = StringBuilder()

            builder.apply {
                append(user.firstName)
                append(" ")
                append(user.lastName)
                append("\n")
                append(user.addressLine1)
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
            }

            if (builder.toString().isNullOrBlank().not()) {
                tv_address.text = builder.toString()
                tv_address.visible = true
                tv_address_name.visible = true
                btn_edit_address.text = "Edit Address"
            }
        }

        saveButton.isEnabled = false
    }



    private fun toggleRemovePictureButton() {
        if (viewModel.userViewModel.profile.value?.photoURL.isNullOrEmpty()) {
            removeProfilePhotoButton.visibility = View.GONE
        } else {
            removeProfilePhotoButton.visibility = View.VISIBLE
        }
    }

    private fun editEmail() {
        //analytics.track(AnalyticsEvent.ACTION_EDIT_EMAIL)

        /*if (preferences.haveRecentlyVerifiedIdentity) {
            loadCurrentTab(EditEmailFragment())
        } else {
            loadCurrentTab(VerifyIdentityForEmailFragment())
        }*/
    }

    private fun editAddress() {
        //loadCurrentTab(EditAddressFragment())
    }

    private fun editPassword() {
        //analytics.track(AnalyticsEvent.ACTION_EDIT_PASSWORD)

        //loadCurrentTab(EditPasswordFragment())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            uploadProfileImage(result.uri)
        }
    }
}