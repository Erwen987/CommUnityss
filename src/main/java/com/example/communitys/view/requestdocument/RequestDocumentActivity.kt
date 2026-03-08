package com.example.communitys.view.requestdocument

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.communitys.R
import com.example.communitys.databinding.ActivityRequestDocumentBinding

class RequestDocumentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestDocumentBinding
    private var selectedPaymentMethod: PaymentMethod? = null

    enum class PaymentMethod {
        GCASH, PAY_ON_SITE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        // Don't call updatePaymentSelection() - no default selection
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // GCash payment
        binding.cvGCash.setOnClickListener {
            selectedPaymentMethod = PaymentMethod.GCASH
            updatePaymentSelection()
        }

        // Pay on Site
        binding.cvPayOnSite.setOnClickListener {
            selectedPaymentMethod = PaymentMethod.PAY_ON_SITE
            updatePaymentSelection()
        }

        // Upload proof
        binding.cvUploadProof.setOnClickListener {
            // TODO: Implement image picker
            Toast.makeText(this, "Image upload coming soon", Toast.LENGTH_SHORT).show()
        }

        // Submit request
        binding.btnSubmitRequest.setOnClickListener {
            submitRequest()
        }

        // Clear errors when user starts typing
        binding.etDocument.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.tilDocument.error = null
        }

        binding.etPurpose.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.tilPurpose.error = null
        }

        binding.etDocument.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilDocument.error = null
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.etPurpose.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilPurpose.error = null
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun updatePaymentSelection() {
        when (selectedPaymentMethod) {
            PaymentMethod.GCASH -> {
                // Show GCash as selected with visual feedback
                binding.cvGCash.cardElevation = 12f
                binding.cvGCash.alpha = 1.0f

                binding.cvPayOnSite.cardElevation = 2f
                binding.cvPayOnSite.alpha = 0.5f

                // Show proof of payment section
                binding.tvProofOfPayment.visibility = View.VISIBLE
                binding.cvUploadProof.visibility = View.VISIBLE
            }
            PaymentMethod.PAY_ON_SITE -> {
                // Show Pay on Site as selected with visual feedback
                binding.cvGCash.cardElevation = 2f
                binding.cvGCash.alpha = 0.5f

                binding.cvPayOnSite.cardElevation = 12f
                binding.cvPayOnSite.alpha = 1.0f

                // Hide proof of payment section
                binding.tvProofOfPayment.visibility = View.GONE
                binding.cvUploadProof.visibility = View.GONE
            }
            null -> {
                // No selection - reset both to default state
                binding.cvGCash.cardElevation = 2f
                binding.cvGCash.alpha = 1.0f

                binding.cvPayOnSite.cardElevation = 2f
                binding.cvPayOnSite.alpha = 1.0f

                // Hide proof of payment section
                binding.tvProofOfPayment.visibility = View.GONE
                binding.cvUploadProof.visibility = View.GONE
            }
        }
    }

    private fun submitRequest() {
        val document = binding.etDocument.text.toString().trim()
        val purpose = binding.etPurpose.text.toString().trim()

        // Clear previous errors
        binding.tilDocument.error = null
        binding.tilPurpose.error = null

        var hasError = false

        // Validation
        when {
            document.isEmpty() -> {
                binding.tilDocument.error = "Document type is required"
                binding.etDocument.requestFocus()
                hasError = true
            }
            document.length < 3 -> {
                binding.tilDocument.error = "Document type must be at least 3 characters"
                binding.etDocument.requestFocus()
                hasError = true
            }
        }

        when {
            purpose.isEmpty() -> {
                binding.tilPurpose.error = "Purpose is required"
                if (!hasError) binding.etPurpose.requestFocus()
                hasError = true
            }
            purpose.length < 3 -> {
                binding.tilPurpose.error = "Purpose must be at least 3 characters"
                if (!hasError) binding.etPurpose.requestFocus()
                hasError = true
            }
        }

        // Validate payment method selection
        if (selectedPaymentMethod == null) {
            Toast.makeText(
                this,
                "Please select a payment method",
                Toast.LENGTH_SHORT
            ).show()
            hasError = true
        }

        if (hasError) return

        // TODO: Save to database
        val message = when (selectedPaymentMethod) {
            PaymentMethod.GCASH -> "Request submitted! Please upload proof of payment."
            PaymentMethod.PAY_ON_SITE -> "Request submitted! Pay at the Barangay Office."
            null -> "" // This won't happen due to validation above
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }
}
