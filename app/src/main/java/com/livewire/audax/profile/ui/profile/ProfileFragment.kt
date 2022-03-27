package com.livewire.audax.profile.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.livewire.audax.R
import com.livewire.audax.authentication.UserViewModel
import com.livewire.audax.authentication.activity.LoginActivity
import com.livewire.audax.homescreen.HomeScreenActivity
import com.livewire.audax.profile.EditProfileActivity
import com.livewire.audax.store.SharedPreference
import com.livewire.audax.utils.CustomDialog
import com.livewire.audax.utils.ImageLoader
import kotlinx.android.synthetic.main.main_fragment.avatarImageView
import kotlinx.android.synthetic.main.main_fragment.ownersName
import kotlinx.android.synthetic.main.profile_fragment.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ProfileFragment : Fragment() {
    private val viewModel: ProfileViewModel by sharedViewModel()
    private val imageLoader: ImageLoader by inject()
    private val user: UserViewModel by inject()
    //private val messages: MessageCenterViewModel by inject()

    private val progressDialog = CustomDialog()
    companion object {
        fun newInstance() = ProfileFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //viewModel.refreshUser()
        progressDialog.show(this.requireContext())
        user.profile.observe(this, {
            updateProfileHeader()
        })


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.profile_fragment, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        configureLogoutButton()

        tv_profile.setOnClickListener {
            startActivity(Intent(context, EditProfileActivity::class.java))
        }

        tv_message.setOnClickListener {
            startActivity(Intent(context, HomeScreenActivity::class.java))
        }
        back_button.setOnClickListener {
            activity?.onBackPressed()
        }

    }

    override fun onResume() {
        progressDialog.dismiss(requireContext())
        super.onResume()
    }


    private fun updateProfileHeader() {
        val user = user.profile.value ?: return

        if (user.photoURL.isNotEmpty()) {
            imageLoader.loadImageViewWithRoundedBitmap(user.photoURL, avatarImageView)
        } else {
            avatarImageView.setImageResource(R.drawable.ic_avatar_placeholder)
        }

        ownersName.text = user.firstLastName
        ownersEmails.text = user.email
        progressDialog.dismiss(this.requireActivity())
    }

    private fun configureLogoutButton() {
        logoutButton.setOnClickListener {

            progressDialog.show(this.requireContext())
            viewModel.logout()
            activity?.let{
                val intent = Intent (it, LoginActivity::class.java)
                it.startActivity(intent)
                val sharedPreference =SharedPreference(requireContext())
                sharedPreference.removeValue("Email")
                sharedPreference.removeValue("Password")
            }
            activity?.finish()
            progressDialog.dismiss(this.requireActivity())
        }
    }

}