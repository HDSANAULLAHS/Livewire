package com.livewire.audax.profile.ui.changepassword

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.livewire.audax.R
import com.livewire.audax.authentication.activity.LoginActivity
import com.livewire.audax.utils.CustomDialog
import kotlinx.android.synthetic.main.main_fragment.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ChangePasswordFragment: Fragment() {
    private val viewModel: ChangePasswordViewModel by sharedViewModel()
    private var progressDialog = CustomDialog()

    companion object {
        fun newInstance() = ChangePasswordFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        saveButton.setOnClickListener {

                updatePassword()
        }
    }

    private fun updatePassword() {
        progressDialog.show(requireContext())
        viewModel.newPassword = newPassword.text.toString().trim()
        viewModel.currentPassword = currentPassword.text.toString().trim()
        viewModel.updatePassword(this::updateSuccess, this::updateFailed)
    }

    private fun updateSuccess() {
        progressDialog.dismiss(requireContext())
        Toast.makeText(requireContext(), "Password Change Success", Toast.LENGTH_SHORT).show()
        val intent = Intent(context , LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        activity?.startActivity(intent)
        activity?.finish()
    }

    private fun updateFailed(message: Int) {
        //stopLoading()
        //showError(message)
        progressDialog.dismiss(requireContext())
        Toast.makeText(requireContext(), "Change password failed", Toast.LENGTH_SHORT).show()
    }
}
