package com.example.communitys.view.signup

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.communitys.databinding.ActivitySignupBinding
import com.example.communitys.utils.ValidationHelper
import com.example.communitys.utils.ValidationHelper.errorMessage
import com.example.communitys.view.login.LoginActivity
import com.example.communitys.view.verification.VerifyEmailActivity
import com.example.communitys.viewmodel.AuthViewModel

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var viewModel: AuthViewModel

    // All barangays in Dagupan City, Pangasinan
    private val barangayList = listOf(
        "Bacayao Norte", "Bacayao Sur", "Barangay I (Pob.)", "Barangay II (Pob.)",
        "Barangay III (Pob.)", "Barangay IV (Pob.)", "Bolosan", "Bonuan Binloc",
        "Bonuan Boquig", "Bonuan Gueset", "Calmay", "Carael", "Caranglaan",
        "Herrero", "Lasip Chico", "Lasip Grande", "Lomboy", "Lucao", "Malued",
        "Mamalingling", "Mangin", "Mayombo", "Pantal", "Poblacion Oeste",
        "Pogo Chico", "Pogo Grande", "Pugaro Suit", "Quezon", "San Jose",
        "San Lázaro", "Salapingao", "Taloy", "Tebeng"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        setupBarangayDropdown()
        setupRealTimeValidation()
        setupClickListeners()
        observeViewModel()
    }

    // ── Observe AuthViewModel signup state ────────────────────────────────────

    private fun observeViewModel() {
        viewModel.signupState.observe(this) { state ->
            when (state) {
                is AuthViewModel.AuthState.Loading -> {
                    // Show loading
                    binding.btnSignUp.isEnabled = false
                    binding.btnSignUp.text = "Creating account..."
                }

                is AuthViewModel.AuthState.Success -> {
                    // ✅ Supabase signUp succeeded — OTP email was sent — now go to verify screen
                    binding.btnSignUp.isEnabled = true
                    binding.btnSignUp.text = "SIGN UP"

                    Toast.makeText(
                        this,
                        "Account created! Check your email for the OTP code.",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this, VerifyEmailActivity::class.java).apply {
                        putExtra("email",     binding.etEmail.text.toString().trim())
                        putExtra("firstName", binding.etFirstName.text.toString().trim())
                        putExtra("lastName",  binding.etLastName.text.toString().trim())
                        putExtra("barangay",  binding.actvBarangay.text.toString().trim())
                        putExtra("password",  binding.etPassword.text.toString())
                    }
                    startActivity(intent)
                }

                is AuthViewModel.AuthState.Error -> {
                    // ❌ Show error — don't navigate
                    binding.btnSignUp.isEnabled = true
                    binding.btnSignUp.text = "SIGN UP"

                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ── Barangay dropdown ─────────────────────────────────────────────────────

    private fun setupBarangayDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, barangayList)
        binding.actvBarangay.setAdapter(adapter)

        binding.actvBarangay.setOnItemClickListener { _, _, _, _ ->
            val error = ValidationHelper.validateBarangay(
                binding.actvBarangay.text.toString()
            ).errorMessage()
            binding.tilBarangay.error = error
            binding.tilBarangay.isErrorEnabled = error != null
        }
    }

    // ── Real-time inline validation ───────────────────────────────────────────

    private fun setupRealTimeValidation() {

        binding.etFirstName.addTextChangedListener(afterChanged { s ->
            val error = ValidationHelper.validateName(s, "First name").errorMessage()
            binding.tilFirstName.error = error
            binding.tilFirstName.isErrorEnabled = error != null
        })

        binding.etLastName.addTextChangedListener(afterChanged { s ->
            val error = ValidationHelper.validateName(s, "Last name").errorMessage()
            binding.tilLastName.error = error
            binding.tilLastName.isErrorEnabled = error != null
        })

        binding.etEmail.addTextChangedListener(afterChanged { s ->
            val error = ValidationHelper.validateEmail(s).errorMessage()
            binding.tilEmail.error = error
            binding.tilEmail.isErrorEnabled = error != null
        })

        binding.etPassword.addTextChangedListener(afterChanged { s ->
            val error = ValidationHelper.validatePassword(s).errorMessage()
            binding.tilPassword.error = error
            binding.tilPassword.isErrorEnabled = error != null

            val confirm = binding.etConfirmPassword.text.toString()
            if (confirm.isNotEmpty()) {
                val cError = ValidationHelper.validateConfirmPassword(s, confirm).errorMessage()
                binding.tilConfirmPassword.error = cError
                binding.tilConfirmPassword.isErrorEnabled = cError != null
            }
        })

        binding.etConfirmPassword.addTextChangedListener(afterChanged { s ->
            val cError = ValidationHelper.validateConfirmPassword(
                binding.etPassword.text.toString(), s
            ).errorMessage()
            binding.tilConfirmPassword.error = cError
            binding.tilConfirmPassword.isErrorEnabled = cError != null
        })
    }

    // ── Click listeners ───────────────────────────────────────────────────────

    private fun setupClickListeners() {

        binding.btnSignUp.setOnClickListener {
            if (validateAll()) {
                // ✅ This is the fix — actually call signUp() so Supabase sends the OTP
                viewModel.signUp(
                    firstName       = binding.etFirstName.text.toString().trim(),
                    lastName        = binding.etLastName.text.toString().trim(),
                    barangay        = binding.actvBarangay.text.toString().trim(),
                    email           = binding.etEmail.text.toString().trim(),
                    password        = binding.etPassword.text.toString(),
                    confirmPassword = binding.etConfirmPassword.text.toString()
                )
            }
        }

        binding.tvLogIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // ── Full validation on submit ─────────────────────────────────────────────

    private fun validateAll(): Boolean {
        val fnError = ValidationHelper.validateName(
            binding.etFirstName.text.toString(), "First name"
        ).errorMessage()

        val lnError = ValidationHelper.validateName(
            binding.etLastName.text.toString(), "Last name"
        ).errorMessage()

        val barangayError = ValidationHelper.validateBarangay(
            binding.actvBarangay.text.toString()
        ).errorMessage()

        val emError = ValidationHelper.validateEmail(
            binding.etEmail.text.toString()
        ).errorMessage()

        val pwError = ValidationHelper.validatePassword(
            binding.etPassword.text.toString()
        ).errorMessage()

        val cpError = ValidationHelper.validateConfirmPassword(
            binding.etPassword.text.toString(),
            binding.etConfirmPassword.text.toString()
        ).errorMessage()

        binding.tilFirstName.error        = fnError;       binding.tilFirstName.isErrorEnabled       = fnError != null
        binding.tilLastName.error         = lnError;       binding.tilLastName.isErrorEnabled        = lnError != null
        binding.tilBarangay.error         = barangayError; binding.tilBarangay.isErrorEnabled        = barangayError != null
        binding.tilEmail.error            = emError;       binding.tilEmail.isErrorEnabled           = emError != null
        binding.tilPassword.error         = pwError;       binding.tilPassword.isErrorEnabled        = pwError != null
        binding.tilConfirmPassword.error  = cpError;       binding.tilConfirmPassword.isErrorEnabled = cpError != null

        return listOf(fnError, lnError, barangayError, emError, pwError, cpError).all { it == null }
    }

    // ── Concise TextWatcher helper ────────────────────────────────────────────

    private fun afterChanged(block: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) { block(s.toString()) }
    }
}