package com.livewire.app.authentication.ui.signup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.livewire.app.R
import com.livewire.app.authentication.activity.LoginActivity
import com.livewire.app.utils.CustomDialog
import kotlinx.android.synthetic.main.create_account_fragment.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.util.*


class SignupFragment : Fragment() {
    private val viewModel: SignupViewModel by sharedViewModel()
    private val progressDialog = CustomDialog()

    companion object {
        fun newInstance() = SignupFragment()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.create_account_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel

        btnCreateAccount.setOnClickListener {
            if (editFName.text.toString().isEmpty()) {
                editFName.error ="First name required"
            }else if (editLName.text.toString().isEmpty()){
                editLName.error ="Last name required"
            }else if (editEmail.text.toString().isEmpty()){
                editEmail.error ="Email required"
            }else{
                createAccount()
            }
        }

        txt_terms.setOnClickListener {
            goToUrl("https://www.livewire.com/documents/privacy-policy")
        }

        txt_login.setOnClickListener{
            activity?.let{
                val intent = Intent (it, LoginActivity::class.java)
                it.startActivity(intent)
            }
            activity?.finish()
        }
        txt_back.setOnClickListener { activity?.let { val intent = Intent(it, LoginActivity::class.java)
        it.startActivity(intent)}
            activity?.finish()}

    }

    private fun createAccount() {

        // Show progress dialog without Title
        progressDialog.show(this.requireContext())

        activity?.hideKeyboard()
        viewModel.firstName = editFName.text.toString().trim()
        viewModel.lastName = editLName.text.toString().trim()
        viewModel.email = editEmail.text.toString().toLowerCase(Locale.US).trim()
        viewModel.password = passwordField.passwordText.toString().trim()
        viewModel.password = passwordField.passwordText.toString().trim()
        viewModel.postalCode = "60005"

        viewModel.createAccount(this::createAccountSuccess, this::createAccountError)
    }

    private fun createAccountSuccess() {
        progressDialog.dismiss(this.requireActivity())
        signupToast()
        //Toast.makeText(requireActivity(),"Signup Done! Verification mail send to your register email please verify!",Toast.LENGTH_LONG).show()
    }

    private fun createAccountError(message: Int) {
        progressDialog.dismiss(this.requireActivity())
        activity?.let { showTextDialog(activity, it.getString(message)) }

    }
    fun Activity.hideKeyboard() {
        currentFocus?.hideKeyboard()
    }

    fun View?.hideKeyboard() {
        if (this != null) {
            val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.windowToken, 0)
        }
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
    private fun goToUrl(url: String) {
        val uriUrl = Uri.parse(url)
        val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
        startActivity(launchBrowser)
    }
    fun signupToast(){
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setMessage("User is registered successfully. Please visit your mail box and validate your email to login")
            .setPositiveButton("Go to SignIn"){ DialogInterface, which ->
                activity?.let{
                    val intent = Intent (it, LoginActivity::class.java)
                    it.startActivity(intent)
                }
                activity?.finish()
            }

        val alertDialog: AlertDialog = dialog.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}