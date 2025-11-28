package com.example.edupath.ui.fragments

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.edupath.adapters.RecommendationsAdapter
import com.example.edupath.adapters.SkillsAdapter
import com.example.edupath.adapters.SubjectsAdapter
import com.example.edupath.data.Subject
import com.example.edupath.databinding.FragmentGrade9DashboardBinding
import com.example.edupath.services.DocumentAnalyzer
import com.example.edupath.viewmodel.EduPathViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Grade9DashboardFragment : Fragment() {

    private var _binding: FragmentGrade9DashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EduPathViewModel by activityViewModels()
    private lateinit var subjectsAdapter: SubjectsAdapter
    private lateinit var skillsAdapter: SkillsAdapter
    private lateinit var recommendationsAdapter: RecommendationsAdapter
    private lateinit var documentAnalyzer: DocumentAnalyzer

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            processFileUpload(it)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openFilePicker()
        } else {
            showToast("Permission denied. Cannot access files.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrade9DashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        documentAnalyzer = DocumentAnalyzer(requireContext())
        setupAdapters()
        setupObservers()
        setupClickListeners()
        checkExistingData()
    }

    private fun setupAdapters() {
        subjectsAdapter = SubjectsAdapter()
        binding.rvSubjects.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = subjectsAdapter
        }

        skillsAdapter = SkillsAdapter { selectedSkills ->
            viewModel.updateSkills(selectedSkills)
        }
        binding.rvSkills.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = skillsAdapter
        }

        recommendationsAdapter = RecommendationsAdapter()
        binding.rvRecommendations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendationsAdapter
        }

        val skills = listOf(
            "Problem Solving", "Communication", "Leadership",
            "Creativity", "Analytical Thinking", "Teamwork"
        )
        skillsAdapter.submitList(skills)
    }

    private fun setupObservers() {
        viewModel.currentProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                if (it.subjects.isNotEmpty()) {
                    displaySubjects(it.subjects)
                }
            }
        }

        viewModel.recommendations.observe(viewLifecycleOwner) { recommendations ->
            recommendationsAdapter.submitList(recommendations)
            updateRecommendationsTitle(recommendations)
        }

        viewModel.isProcessing.observe(viewLifecycleOwner) { isProcessing ->
            binding.progressBar.visibility = if (isProcessing) View.VISIBLE else View.GONE
        }

        viewModel.analysisResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                showAnalysisConfidence(it.confidence)
            }
        }
    }

    private fun displaySubjects(subjects: List<Subject>) {
        subjectsAdapter.submitList(subjects)

        val strongSubjects = subjects.count { it.score >= 70 }
        val goodSubjects = subjects.count { it.score in 60..69 }
        val averageSubjects = subjects.count { it.score in 50..59 }
        val weakSubjects = subjects.count { it.score < 50 }

        val summary = "Analysis of ${subjects.size} subjects:\n" +
                "• $strongSubjects excellent (70%+)\n" +
                "• $goodSubjects good (60-69%)\n" +
                "• $averageSubjects average (50-59%)\n" +
                "• $weakSubjects need improvement"

        binding.tvPerformanceSummary.text = summary

        binding.layoutUploadPrompt.visibility = View.GONE
        binding.layoutContent.visibility = View.VISIBLE
        binding.layoutUploadError.visibility = View.GONE
    }

    private fun updateRecommendationsTitle(recommendations: List<com.example.edupath.data.Recommendation>) {
        binding.tvRecommendationsTitle.visibility = if (recommendations.isNotEmpty()) View.VISIBLE else View.GONE

        if (recommendations.isNotEmpty()) {
            val careerCount = recommendations.flatMap { it.careers }.size
            val universityCount = recommendations.flatMap { it.universities }.size
            binding.tvRecommendationsTitle.text = "Based on your results ($careerCount career pathways, $universityCount university options)"
        }
    }

    private fun showAnalysisConfidence(confidence: Float) {
        val confidencePercent = (confidence * 100).toInt()
        if (confidencePercent < 80) {
            showToast("Analysis confidence: $confidencePercent%. Please verify extracted scores.")
        }
    }

    private fun setupClickListeners() {
        binding.btnUploadNow.setOnClickListener {
            showUploadOptions()
        }

        binding.btnRetryUpload.setOnClickListener {
            showUploadOptions()
        }

        binding.fabUploadResults.setOnClickListener {
            showUploadOptions()
        }
    }

    private fun checkExistingData() {
        val hasData = viewModel.currentProfile.value?.subjects?.isNotEmpty() == true
        if (hasData) {
            binding.layoutUploadPrompt.visibility = View.GONE
            binding.layoutContent.visibility = View.VISIBLE
        } else {
            binding.layoutUploadPrompt.visibility = View.VISIBLE
            binding.layoutContent.visibility = View.GONE
        }
    }

    private fun showUploadOptions() {
        val options = arrayOf("Upload Image/PDF", "Use Sample Data", "Manual Entry")

        AlertDialog.Builder(requireContext())
            .setTitle("Upload Grade 9 Results")
            .setMessage("Choose how to provide your results")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkPermissionsAndOpenPicker()
                    1 -> useSampleData()
                    2 -> showManualEntryDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkPermissionsAndOpenPicker() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openFilePicker()
            }
            else -> {
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun openFilePicker() {
        try {
            filePickerLauncher.launch("*/*")
        } catch (e: Exception) {
            showErrorState("Cannot open file picker: ${e.message}")
        }
    }

    private fun processFileUpload(fileUri: Uri) {
        binding.layoutUploadPrompt.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.tvProcessingStatus.text = "Analyzing document... This may take a few seconds."

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    documentAnalyzer.analyzeDocument(fileUri)
                }

                if (result.subjects.isNotEmpty()) {
                    viewModel.processDocumentAnalysisResult(result, 9)
                    showToast("Document analyzed successfully! Found ${result.subjects.size} subjects.")
                } else {
                    showErrorState("No subject scores found in document. Please try manual entry.")
                }

            } catch (e: Exception) {
                showErrorState("Failed to analyze document: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showManualEntryDialog() {
        val gradeOptions = arrayOf("Grade 9", "Grade 10", "Grade 11", "Grade 12")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Your Grade")
            .setItems(gradeOptions) { _, which ->
                val grade = which + 9
                showSubjectEntryDialog(grade)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSubjectEntryDialog(grade: Int) {
        val subjectOptions = arrayOf(
            "English Home Language",
            "Afrikaans First Additional Language",
            "Mathematics",
            "Mathematical Literacy",
            "Physical Sciences",
            "Life Sciences",
            "Geography",
            "History",
            "Accounting",
            "Business Studies",
            "Economics",
            "Life Orientation",
            "Natural Sciences",
            "Social Sciences",
            "Technology",
            "Creative Arts"
        )

        val selectedSubjects = mutableListOf<String>()
        val subjectScores = mutableMapOf<String, Int>()

        AlertDialog.Builder(requireContext())
            .setTitle("Select Your Subjects")
            .setMultiChoiceItems(subjectOptions, null) { _, which, isChecked ->
                val subject = subjectOptions[which]
                if (isChecked) {
                    selectedSubjects.add(subject)
                } else {
                    selectedSubjects.remove(subject)
                }
            }
            .setPositiveButton("Next") { _, _ ->
                if (selectedSubjects.isNotEmpty()) {
                    showScoreEntryDialog(grade, selectedSubjects, subjectScores)
                } else {
                    showToast("Please select at least one subject")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showScoreEntryDialog(grade: Int, subjects: List<String>, scores: MutableMap<String, Int>) {
        val subjectList = subjects.toMutableList()
        showIndividualScoreDialog(grade, subjectList, scores, 0)
    }

    private fun showIndividualScoreDialog(grade: Int, subjects: MutableList<String>, scores: MutableMap<String, Int>, index: Int) {
        if (index >= subjects.size) {
            val subjectList = scores.map { (name, score) ->
                val isCore = name in listOf(
                    "English Home Language",
                    "Mathematics",
                    "Life Orientation",
                    "Natural Sciences"
                )
                Subject(name, score, isCore)
            }
            viewModel.processUploadedResults(grade, subjectList)
            showToast("Results processed successfully!")
            return
        }

        val currentSubject = subjects[index]
        val input = android.widget.EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "Enter percentage (0-100)"

        AlertDialog.Builder(requireContext())
            .setTitle("Enter score for $currentSubject")
            .setView(input)
            .setPositiveButton(if (index < subjects.size - 1) "Next" else "Finish") { _, _ ->
                val scoreText = input.text.toString()
                val score = scoreText.toIntOrNull() ?: 0
                if (score in 0..100) {
                    scores[currentSubject] = score
                    showIndividualScoreDialog(grade, subjects, scores, index + 1)
                } else {
                    showToast("Please enter a valid score between 0-100")
                    showIndividualScoreDialog(grade, subjects, scores, index)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun useSampleData() {
        binding.layoutUploadPrompt.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        binding.root.postDelayed({
            try {
                val sampleSubjects = listOf(
                    Subject("English Home Language", 75, true),
                    Subject("Mathematics", 82, true),
                    Subject("Natural Sciences", 78, true),
                    Subject("Social Sciences", 65, false),
                    Subject("Technology", 71, false),
                    Subject("Life Orientation", 80, true),
                    Subject("Economic Management Sciences", 67, false),
                    Subject("Creative Arts", 73, false)
                )

                viewModel.processUploadedResults(9, sampleSubjects)
                showToast("Sample data loaded successfully!")

            } catch (e: Exception) {
                showErrorState("Failed to load sample data: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }, 1500)
    }

    private fun showErrorState(errorMessage: String = "Upload failed. Please try again.") {
        binding.tvErrorDescription.text = errorMessage
        binding.layoutUploadPrompt.visibility = View.GONE
        binding.layoutContent.visibility = View.GONE
        binding.layoutUploadError.visibility = View.VISIBLE
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}