package com.livewire.audax.splash.ui.launchar

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.livewire.audax.R
import com.livewire.audax.authentication.activity.LoginActivity
import com.livewire.audax.authentication.ui.login.LoginViewModel
import com.livewire.audax.homescreen.HomeScreenActivity
import com.livewire.audax.profile.ProfileActivity
import com.livewire.audax.store.SharedPreference
import com.livewire.audax.utils.CustomDialog
import org.koin.android.viewmodel.ext.android.sharedViewModel

class LauncherFragment : Fragment() {

    private val viewModel: LoginViewModel by sharedViewModel()
    private val progressDialog = CustomDialog()

    companion object {
        fun newInstance() = LauncherFragment()
    }
    var email = ""
    var password = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.launchar_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
        val sharedPreference:SharedPreference=SharedPreference(requireContext())
        email = sharedPreference.getStringValue("Email").toString()
        password = sharedPreference.getStringValue("Password").toString()
            if (email.isEmpty()){
                activity?.let{
                    val intent = Intent (it, LoginActivity::class.java)
                    it.startActivity(intent)
                    progressDialog.dismiss(this.requireActivity())
                }
                activity?.finish()
            }else{
                doLogin()
            }

    }

    private fun doLogin() {

        progressDialog.show(this.requireContext())
        viewModel.login(email, password) { result ->

            when (result) {
                // Do nothing; handled by authentication events, same as CreateAccountFragment
                is LoginViewModel.LoginResult.Success -> {
                    activity?.let{
                        val intent = Intent (it, HomeScreenActivity::class.java)
                        it.startActivity(intent)
                        progressDialog.dismiss(this.requireActivity())
                    }
                    activity?.finish()
                }
                is LoginViewModel.LoginResult.Failure -> loginFailed(result.message)
            }
        }
    }

    private fun loginFailed(message: Int) {
        val activity = activity ?: return
        progressDialog.dismiss(this.requireActivity())
        activity?.let{
            val intent = Intent (it, LoginActivity::class.java)
            it.startActivity(intent)
        }
        activity?.finish()
    }

}