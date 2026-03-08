package com.example.communitys.view.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.communitys.databinding.FragmentProfileBinding
import com.example.communitys.view.login.LoginActivity
import com.example.communitys.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data     = result.data
            val name     = data?.getStringExtra("name")     ?: return@registerForActivityResult
            val email    = data?.getStringExtra("email")    ?: return@registerForActivityResult
            val barangay = data?.getStringExtra("barangay") ?: return@registerForActivityResult

            binding.tvUserName.text = name
            binding.tvFullName.text = name
            binding.tvEmail.text    = email
            binding.tvBarangay.text = barangay

            viewModel.updateProfile(name, email, barangay)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            binding.tvUserName.text = profile.name
            binding.tvFullName.text = profile.name
            binding.tvEmail.text    = profile.email
            binding.tvBarangay.text = profile.barangay
            binding.tvPoints.text   = profile.points.toString()
        }

        viewModel.logoutState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileViewModel.LogoutState.Loading -> {
                    binding.btnLogOut.isEnabled = false
                    binding.btnLogOut.text = "Logging out..."
                }
                is ProfileViewModel.LogoutState.Success -> {
                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().finish()
                }
                is ProfileViewModel.LogoutState.Error -> {
                    binding.btnLogOut.isEnabled = true
                    binding.btnLogOut.text = "LOG OUT"
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabEditPhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Photo upload coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnClaimReward.setOnClickListener {
            Toast.makeText(requireContext(), "Rewards feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnEditProfile.setOnClickListener {
            launchEditProfile()
        }

        binding.btnLogOut.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun launchEditProfile() {
        val profile = viewModel.userProfile.value ?: return
        val intent = Intent(requireContext(), EditProfileActivity::class.java).apply {
            putExtra("name",     profile.name)
            putExtra("email",    profile.email)
            putExtra("barangay", profile.barangay)
        }
        editProfileLauncher.launch(intent)
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ -> viewModel.logout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}