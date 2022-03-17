package com.livewire.audax.authentication.ui.resetpassword

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.livewire.audax.R
import com.livewire.audax.authentication.activity.LoginActivity
import com.livewire.audax.utils.CustomDialog
import com.livewire.audax.utils.hideKeyboard
import com.livewire.audax.utils.visible
import com.livewire.audax.utils.watchText
import kotlinx.android.synthetic.main.reset_password_fragment.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ResetPasswordFragment : Fragment() {
    private val viewModel: ResetPasswordViewModel by sharedViewModel()
    private val progressDialog = CustomDialog()
    companion object {
        fun newInstance() = ResetPasswordFragment()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.reset_password_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_submit.setOnClickListener{
            activity?.let{
                val intent = Intent (it, LoginActivity::class.java)
                it.startActivity(intent)
            }
            activity?.finish()
        }
        btn_reset_password.setOnClickListener {
            resetPassword()
        }


        editEmail.watchText {
            txt_error_message.visible = false
            btn_reset_password.isEnabled = it.text.toString().isNotEmpty()
        }

        iv_back.setOnClickListener {
            activity?.let{
                val intent = Intent (it, LoginActivity::class.java)
                it.startActivity(intent)
            }
            activity?.finish()
        }
    }

    private fun resetPassword() {
        activity?.hideKeyboard()
        progressDialog.show(this.requireContext())
        viewModel.resetPassword(editEmail.text.toString(), this::resetSuccess, this::resetFailed)

    }

    private fun resetSuccess() {
        progressDialog.dismiss(this.requireContext())
        txt_forgot_password_title.text = getString(R.string.check_mail)
        txt_message.text = getString(R.string.reset_password_link)
        editEmail.visibility = View.GONE
        txt_error_message.visibility = View.GONE
        btn_reset_password.visibility = View.GONE
        btn_submit.visibility = View.VISIBLE

    }

    private fun resetFailed(message: Int, showCreateOption: Boolean) {

        progressDialog.dismiss(this.requireContext())
        txt_error_message?.setText(message)
        txt_error_message?.visible = true
        //editEmail?.setValidationError(showLine = false)
        btn_reset_password?.visible = showCreateOption
        btn_reset_password.visibility = View.VISIBLE
    }

}