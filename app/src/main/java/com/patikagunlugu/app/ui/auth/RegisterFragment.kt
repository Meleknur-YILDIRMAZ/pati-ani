package com.patikagunlugu.app.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.FragmentRegisterBinding
import com.patikagunlugu.app.viewmodel.AuthViewModel
import com.patikagunlugu.app.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, AuthViewModelFactory())[AuthViewModel::class.java]

        setupClickListeners()
        setupPasswordStrengthWatcher()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()
            val name = binding.etFullName.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(requireContext(), getString(R.string.passwords_not_match), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), getString(R.string.password_min_length), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(email, password, name)
        }

        binding.tvLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupPasswordStrengthWatcher() {
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val strength = calculatePasswordStrength(s.toString())
                binding.tvPasswordStrength.text = getString(R.string.password_strength_format, strength)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun calculatePasswordStrength(password: String): String {
        if (password.length < 6) return getString(R.string.password_weak)
        var hasLetter = false
        var hasDigit = false
        for (c in password) {
            if (c.isLetter()) hasLetter = true
            if (c.isDigit()) hasDigit = true
        }
        return if (hasLetter && hasDigit && password.length >= 8) getString(R.string.password_strong) else getString(R.string.password_medium)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthViewModel.AuthState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnRegister.isEnabled = false
                    }
                    is AuthViewModel.AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), getString(R.string.register_success), Toast.LENGTH_LONG).show()
                        parentFragmentManager.popBackStack()
                    }
                    is AuthViewModel.AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        val errorMsg = state.messageResId?.let { getString(it) } ?: state.message ?: getString(R.string.register_failed)
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
