package com.patikagunlugu.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.FragmentEmailAuthBinding
import com.patikagunlugu.app.ui.MainActivity
import com.patikagunlugu.app.viewmodel.AuthViewModel
import com.patikagunlugu.app.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch

class EmailAuthFragment : Fragment() {

    private var _binding: FragmentEmailAuthBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel
    private val auth = FirebaseAuth.getInstance()

    private var currentMode = Mode.LOGIN

    enum class Mode {
        LOGIN, REGISTER
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, AuthViewModelFactory())[AuthViewModel::class.java]

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSubmit.setOnClickListener {
            handleSubmission()
        }

        binding.tvSwitchMode.setOnClickListener {
            if (currentMode == Mode.LOGIN) {
                switchToRegisterMode()
            } else {
                switchToLoginMode()
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Şifre sıfırlama e-postası göndermek için geçerli bir e-posta adresi girin."
                return@setOnClickListener
            }
            binding.tilEmail.error = null
            showLoading(true)
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    showLoading(false)
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Şifre sıfırlama bağlantısı e-postanıza gönderildi.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Şifre sıfırlama başarısız: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Default mode is Login
        switchToLoginMode()

        observeViewModel()
    }

    private fun handleSubmission() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Geçersiz e-posta adresi"
            return
        } else {
            binding.tilEmail.error = null
        }

        when (currentMode) {
            Mode.LOGIN -> performLogin(email)
            Mode.REGISTER -> performRegister(email)
        }
    }

    private fun switchToLoginMode() {
        currentMode = Mode.LOGIN
        binding.tvTitle.text = "Tekrar Hoş Geldiniz!"
        binding.tvSubtitle.text = "E-posta ve şifrenizle giriş yapabilirsiniz."
        
        binding.tilEmail.isEnabled = true
        binding.tilName.visibility = View.GONE
        binding.tilPassword.visibility = View.VISIBLE
        binding.tilConfirmPassword.visibility = View.GONE
        binding.tvForgotPassword.visibility = View.VISIBLE
        
        binding.btnSubmit.text = "Giriş Yap"
        binding.tvSwitchMode.text = "Hesabınız yok mu? Kayıt Olun"

        // Clear errors
        binding.tilEmail.error = null
        binding.tilName.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    private fun switchToRegisterMode() {
        currentMode = Mode.REGISTER
        binding.tvTitle.text = "Aramıza Katılın!"
        binding.tvSubtitle.text = "Bilgilerinizi tamamlayarak hesabınızı oluşturun."
        
        binding.tilEmail.isEnabled = true
        binding.tilName.visibility = View.VISIBLE
        binding.tilPassword.visibility = View.VISIBLE
        binding.tilConfirmPassword.visibility = View.VISIBLE
        binding.tvForgotPassword.visibility = View.GONE
        
        binding.btnSubmit.text = "Kayıt Ol"
        binding.tvSwitchMode.text = "Zaten hesabınız var mı? Giriş Yapın"

        // Clear errors
        binding.tilEmail.error = null
        binding.tilName.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    private fun performLogin(email: String) {
        val password = binding.etPassword.text.toString()
        if (password.isEmpty()) {
            binding.tilPassword.error = "Şifre gerekli"
            return
        }
        viewModel.login(email, password)
    }

    private fun performRegister(email: String) {
        val name = binding.etName.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (name.isEmpty()) {
            binding.tilName.error = "Ad gerekli"
            return
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Şifre en az 6 karakter olmalı"
            return
        }
        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Şifreler eşleşmiyor"
            return
        }
        
        viewModel.register(email, password, name)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !isLoading
        binding.tvSwitchMode.isEnabled = !isLoading
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthViewModel.AuthState.Loading -> showLoading(true)
                    is AuthViewModel.AuthState.Success -> {
                        showLoading(false)
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                    is AuthViewModel.AuthState.Error -> {
                        showLoading(false)
                        val errorMsg = state.message ?: ""
                        val resolvedMessage = state.messageResId?.let { getString(it) } ?: errorMsg
                        
                        if (errorMsg.contains("already in use", ignoreCase = true) || errorMsg.contains("already use", ignoreCase = true)) {
                            Toast.makeText(requireContext(), getString(R.string.email_already_registered), Toast.LENGTH_LONG).show()
                            val savedEmail = binding.etEmail.text.toString().trim()
                            switchToLoginMode()
                            binding.etEmail.setText(savedEmail)
                        } else if (errorMsg.contains("password", ignoreCase = true) && (errorMsg.contains("invalid", ignoreCase = true) || errorMsg.contains("wrong", ignoreCase = true))) {
                            Toast.makeText(requireContext(), getString(R.string.wrong_password), Toast.LENGTH_LONG).show()
                        } else if (errorMsg.contains("no user", ignoreCase = true) || errorMsg.contains("not found", ignoreCase = true) || errorMsg.contains("no matching", ignoreCase = true)) {
                            Toast.makeText(requireContext(), getString(R.string.user_not_found), Toast.LENGTH_LONG).show()
                            val savedEmail = binding.etEmail.text.toString().trim()
                            switchToRegisterMode()
                            binding.etEmail.setText(savedEmail)
                        } else {
                            Toast.makeText(requireContext(), resolvedMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                    else -> showLoading(false)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
