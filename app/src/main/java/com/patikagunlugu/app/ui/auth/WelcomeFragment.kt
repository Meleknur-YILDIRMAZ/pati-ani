package com.patikagunlugu.app.ui.auth
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.FragmentWelcomeBinding
import com.patikagunlugu.app.ui.MainActivity
import com.patikagunlugu.app.viewmodel.AuthViewModel
import com.patikagunlugu.app.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AuthViewModel

    private val GOOGLE_SIGN_IN_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, AuthViewModelFactory())[AuthViewModel::class.java]

        setupClickListeners()
        observeViewModel()
        setupTermsText()
    }

    private fun setupTermsText() {
        val text = binding.tvTerms.text.toString()
        val spannableString = SpannableString(text)
        
        val privacyStart = text.indexOf("Gizlilik Politikası")
        val privacyEnd = privacyStart + "Gizlilik Politikası".length
        if (privacyStart != -1) {
            spannableString.setSpan(UnderlineSpan(), privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        
        val termsStart = text.indexOf("Kullanım Koşulları")
        val termsEnd = termsStart + "Kullanım Koşulları".length
        if (termsStart != -1) {
            spannableString.setSpan(UnderlineSpan(), termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        
        binding.tvTerms.text = spannableString
    }

    private fun setupClickListeners() {
        binding.btnGoogleLogin.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
            startActivityForResult(googleSignInClient.signInIntent, GOOGLE_SIGN_IN_CODE)
        }

        binding.btnEmailLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EmailAuthFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                account?.idToken?.let { viewModel.googleLogin(it) }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Google Girişi İptal Edildi veya Başarısız", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthViewModel.AuthState.Loading -> {
                        binding.btnGoogleLogin.isEnabled = false
                        binding.btnEmailLogin.isEnabled = false
                    }
                    is AuthViewModel.AuthState.Success -> {
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                    is AuthViewModel.AuthState.Error -> {
                        binding.btnGoogleLogin.isEnabled = true
                        binding.btnEmailLogin.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        binding.btnGoogleLogin.isEnabled = true
                        binding.btnEmailLogin.isEnabled = true
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
