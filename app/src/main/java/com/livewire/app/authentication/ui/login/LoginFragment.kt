package com.livewire.app.authentication.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.livewire.app.R
import com.livewire.app.authentication.ResetPasswordActivity
import com.livewire.app.authentication.activity.SignupActivity
import com.livewire.app.dashboard.DashboardActivity
import com.livewire.app.store.SharedPreference
import com.livewire.app.utils.CustomDialog
import kotlinx.android.synthetic.main.login_fragment.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by sharedViewModel()
    private val progressDialog = CustomDialog()
    companion object {
        fun newInstance() = LoginFragment()
    }
    var email = ""
    var password = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.login_fragment, container, false)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
        txt_signup.setOnClickListener {
            activity?.let{
                val intent = Intent (it, SignupActivity::class.java)
                it.startActivity(intent)
            }
            activity?.finish()
        }

        txt_forgot_password.setOnClickListener {
            activity?.let{
                val intent = Intent (it, ResetPasswordActivity::class.java)
                it.startActivity(intent)
            }
            activity?.finish()
            //Toast.makeText(activity, "Work in progress...", Toast.LENGTH_SHORT).show()
        }

        btn_login.setOnClickListener {
                email = editEmail.text.toString()
                password = editPassword.text.toString()
                if (email.isEmpty()){
                    editEmail?.error = "Email required"
                }else{
                    if (password.isEmpty()){
                        editPassword?.error = "Password Required"
                    }else{
                        doLogin()
                    }
                }
        }

    }

    private fun doLogin() {

        progressDialog.show(this.requireContext())
        viewModel.login(email, password) { result ->

            when (result) {
                // Do nothing; handled by authentication events, same as CreateAccountFragment
                is LoginViewModel.LoginResult.Success -> {
                    activity?.let{
                        val intent = Intent (it, DashboardActivity::class.java)
                        it.startActivity(intent)
                        val sharedPreference =SharedPreference(requireContext())
                        sharedPreference.save("Email", email)
                        sharedPreference.save("Password", password)
                        progressDialog.dismiss(this.requireActivity())
                    }
                    activity?.finish()
                }
                is LoginViewModel.LoginResult.Failure -> loginFailed(result.message)
                is LoginViewModel.LoginResult.ChangePasswordRequired -> changePassword(result.regToken)
            }
        }
    }

    private fun loginFailed(message: Int) {
        val activity = activity ?: return
        showTextDialog(activity, activity.getString(message))
        progressDialog.dismiss(this.requireActivity())
    }

    fun showTextDialog(activity: Activity?, text: String, title: String? = null, confirm: () -> Unit = {}) {
        activity ?: return

        if (activity.isFinishing) {
            return
        }

        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(text)
            .setPositiveButton(android.R.string.ok) { d, _ ->
                d.dismiss()
                confirm()
            }
            .create()
            .show()
    }
    private fun changePassword(regToken: String) {
        activity?.let{
            val intent = Intent (it, DashboardActivity::class.java)
            it.startActivity(intent)
        }
    }
}