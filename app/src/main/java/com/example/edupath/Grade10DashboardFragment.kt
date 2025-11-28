package com.example.edupath.ui.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edupath.adapters.InterestsAdapter
import com.example.edupath.adapters.RecommendationsAdapter
import com.example.edupath.adapters.SchoolsAdapter
import com.example.edupath.data.Subject
import com.example.edupath.databinding.FragmentGrade10DashboardBinding
import com.example.edupath.viewmodel.EduPathViewModel

class Grade10DashboardFragment : Fragment() {

    private var _binding: FragmentGrade10DashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EduPathViewModel by viewModels()
    private lateinit var interestsAdapter: InterestsAdapter
    private lateinit var schoolsAdapter: SchoolsAdapter
    private lateinit var recommendationsAdapter: RecommendationsAdapter

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            processGrade9Upload(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrade10DashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupObservers()
        setupClickListeners()

        checkDataAvailability()
    }

    private fun setupAdapters() {
        interestsAdapter = InterestsAdapter { selectedInterests ->
            viewModel.updateInterests(selectedInterests)
        }
        binding.rvInterests.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = interestsAdapter
        }

        schoolsAdapter = SchoolsAdapter()
        binding.rvSchools.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = schoolsAdapter
        }

        recommendationsAdapter = RecommendationsAdapter()
        binding.rvRecommendations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendationsAdapter
        }

        // Set available interests
        interestsAdapter.submitList(viewModel.availableInterests)
    }

    private fun setupObservers() {
        viewModel.currentProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.tvAcademicSummary.text = "Based on your Grade 9 performance in ${it.subjects.size} subjects"

                if (it.subjects.isNotEmpty()) {
                    binding.layoutNoData.visibility = View.GONE
                    binding.layoutContent.visibility = View.VISIBLE
                }
            }
        }

        viewModel.recommendations.observe(viewLifecycleOwner) { recommendations ->
            recommendationsAdapter.submitList(recommendations)

            // Update schools list from recommendations
            val schools = recommendations.flatMap { it.schools }
            schoolsAdapter.submitList(schools)

            binding.tvSchoolsTitle.visibility = if (schools.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isProcessing.observe(viewLifecycleOwner) { isProcessing ->
            if (isProcessing) {
                binding.layoutNoData.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnUploadGrade9.setOnClickListener {
            showGrade9UploadOptions()
        }
    }

    private fun checkDataAvailability() {
        val hasData = viewModel.currentProfile.value?.subjects?.isNotEmpty() == true
        if (hasData) {
            binding.layoutNoData.visibility = View.GONE
            binding.layoutContent.visibility = View.VISIBLE
        } else {
            binding.layoutNoData.visibility = View.VISIBLE
            binding.layoutContent.visibility = View.GONE
        }
    }

    private fun showGrade9UploadOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Upload Document")

        AlertDialog.Builder(requireContext())
            .setTitle("Upload Grade 9 Results")
            .setMessage("To get stream recommendations for Grade 10, we need your Grade 9 results.")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCameraForGrade9()
                    1 -> openGalleryForGrade9()
                    2 -> openDocumentPickerForGrade9()
                }
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun openCameraForGrade9() {
        // Simulate camera for demo
        processGrade9Upload(null)
    }

    private fun openGalleryForGrade9() {
        galleryLauncher.launch("image/*")
    }

    private fun openDocumentPickerForGrade9() {
        galleryLauncher.launch("application/pdf")
    }

    private fun processGrade9Upload(uri: Uri?) {
        // Show loading
        binding.layoutNoData.visibility = View.GONE
        // In a real app, you'd show a progress bar

        // Simulate processing
        binding.root.postDelayed({
            val simulatedSubjects = listOf(
                Subject("Home Language", 65, true),
                Subject("First Additional Language", 58, true),
                Subject("Mathematics", 72, true),
                Subject("Natural Sciences", 68, true),
                Subject("Technology", 75, true),
                Subject("Social Sciences", 62, true),
                Subject("Economic Management Sciences", 55, true),
                Subject("Life Orientation", 78, true)
            )

            viewModel.processUploadedResults(10, simulatedSubjects)
        }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}