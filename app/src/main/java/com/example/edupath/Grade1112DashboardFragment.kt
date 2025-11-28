package com.example.edupath.ui.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.edupath.adapters.InterestsAdapter
import com.example.edupath.adapters.SubjectsAdapter
import com.example.edupath.data.Subject
import com.example.edupath.databinding.FragmentGrade1112DashboardBinding
import com.example.edupath.viewmodel.EduPathViewModel
import com.google.android.material.tabs.TabLayoutMediator

class Grade1112DashboardFragment : Fragment() {

    private var _binding: FragmentGrade1112DashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EduPathViewModel by viewModels()
    private lateinit var subjectsAdapter: SubjectsAdapter
    private lateinit var interestsAdapter: InterestsAdapter

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            processResultsUpload(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrade1112DashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupViewPager()
        setupObservers()
        setupClickListeners()

        checkDataAvailability()
    }

    private fun setupAdapters() {
        subjectsAdapter = SubjectsAdapter()
        binding.rvSubjects.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = subjectsAdapter
        }

        interestsAdapter = InterestsAdapter { selectedInterests ->
            viewModel.updateInterests(selectedInterests)
        }
        binding.rvInterests.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = interestsAdapter
        }

        // Set available interests
        interestsAdapter.submitList(viewModel.availableInterests)
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = Grade1112PagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Universities"
                1 -> "Careers"
                2 -> "Requirements"
                else -> "Universities"
            }
        }.attach()
    }

    private fun setupObservers() {
        viewModel.currentProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                // Update APS score
                val apsScore = it.apsScore
                binding.tvAPSScore.text = apsScore.toString()
                binding.tvAPSDescription.text = "Based on ${it.subjects.size} subjects"

                // Update subjects
                subjectsAdapter.submitList(it.subjects)

                // Update subjects summary
                val strongSubjects = it.subjects.filter { subject -> subject.score >= 60 }
                binding.tvSubjectsSummary.text = "${strongSubjects.size} strong subjects (60% and above)"

                if (it.subjects.isNotEmpty()) {
                    binding.layoutNoData.visibility = View.GONE
                    binding.layoutContent.visibility = View.VISIBLE
                }
            }
        }

        viewModel.isProcessing.observe(viewLifecycleOwner) { isProcessing ->
            if (isProcessing) {
                binding.layoutNoData.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnUploadResults.setOnClickListener {
            showUploadOptions()
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

    private fun showUploadOptions() {
        val options = arrayOf("Manual Entry", "Choose from Gallery", "Upload Document")

        AlertDialog.Builder(requireContext())
            .setTitle("Upload Grade 11/12 Results")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRealisticDataEntryDialog()
                    1 -> openGallery()
                    2 -> openDocumentPicker()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun openDocumentPicker() {
        galleryLauncher.launch("application/pdf")
    }

    private fun processResultsUpload(uri: Uri?) {
        binding.layoutNoData.visibility = View.GONE
        // For demo purposes, we'll use manual entry as fallback
        showRealisticDataEntryDialog()
    }

    private fun showRealisticDataEntryDialog() {
        val gradeOptions = arrayOf("Grade 11", "Grade 12")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Your Grade")
            .setItems(gradeOptions) { _, which ->
                val grade = if (which == 0) 11 else 12
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
            "Life Orientation"
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
            // All scores entered, process the results
            val subjectList = scores.map { (name, score) ->
                val isCore = name in listOf("English Home Language", "Mathematics", "Life Orientation")
                Subject(name, score, isCore)
            }
            viewModel.processUploadedResults(grade, subjectList)
            showToast("Results processed successfully! Your APS score is ${viewModel.currentProfile.value?.apsScore}")
            return
        }

        val currentSubject = subjects[index]
        val input = EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.hint = "Enter percentage (0-100)"

        AlertDialog.Builder(requireContext())
            .setTitle("Enter score for $currentSubject")
            .setView(input)
            .setPositiveButton("Next") { _, _ ->
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

    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class Grade1112PagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> UniversityTabFragment()
                1 -> CareersTabFragment()
                2 -> RequirementsTabFragment()
                else -> UniversityTabFragment()
            }
        }
    }
}

class UniversityTabFragment : Fragment() {
    private val viewModel: EduPathViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val recyclerView = androidx.recyclerview.widget.RecyclerView(requireContext())
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        val adapter = UniversityAdapter()
        recyclerView.adapter = adapter

        viewModel.recommendations.observe(viewLifecycleOwner) { recommendations ->
            val universityRecommendations = recommendations
                .filter { it.type == com.example.edupath.data.RecommendationType.UNIVERSITY_RECOMMENDATION }
                .flatMap { it.universities }
            adapter.submitList(universityRecommendations)
        }

        return recyclerView
    }

    class UniversityAdapter : androidx.recyclerview.widget.ListAdapter<com.example.edupath.data.University, UniversityViewHolder>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<com.example.edupath.data.University>() {
            override fun areItemsTheSame(oldItem: com.example.edupath.data.University, newItem: com.example.edupath.data.University): Boolean {
                return oldItem.name == newItem.name && oldItem.program == newItem.program
            }

            override fun areContentsTheSame(oldItem: com.example.edupath.data.University, newItem: com.example.edupath.data.University): Boolean {
                return oldItem == newItem
            }
        }
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UniversityViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return UniversityViewHolder(view)
        }

        override fun onBindViewHolder(holder: UniversityViewHolder, position: Int) {
            val university = getItem(position)
            holder.bind(university)
        }
    }

    class UniversityViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(university: com.example.edupath.data.University) {
            val text1 = itemView.findViewById<android.widget.TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<android.widget.TextView>(android.R.id.text2)

            text1.text = "${university.name} - ${university.program}"
            text2.text = "Location: ${university.location} | APS Required: ${university.apsRequirement} | Status: ${university.status}"
        }
    }
}

class CareersTabFragment : Fragment() {
    private val viewModel: EduPathViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val recyclerView = androidx.recyclerview.widget.RecyclerView(requireContext())
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        val adapter = CareersAdapter()
        recyclerView.adapter = adapter

        viewModel.recommendations.observe(viewLifecycleOwner) { recommendations ->
            val careerRecommendations = recommendations
                .filter { it.type == com.example.edupath.data.RecommendationType.CAREER_GUIDANCE }
                .flatMap { it.careers }
            adapter.submitList(careerRecommendations)
        }

        return recyclerView
    }

    class CareersAdapter : androidx.recyclerview.widget.ListAdapter<com.example.edupath.data.Career, CareerViewHolder>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<com.example.edupath.data.Career>() {
            override fun areItemsTheSame(oldItem: com.example.edupath.data.Career, newItem: com.example.edupath.data.Career): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: com.example.edupath.data.Career, newItem: com.example.edupath.data.Career): Boolean {
                return oldItem == newItem
            }
        }
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CareerViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return CareerViewHolder(view)
        }

        override fun onBindViewHolder(holder: CareerViewHolder, position: Int) {
            val career = getItem(position)
            holder.bind(career)
        }
    }

    class CareerViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(career: com.example.edupath.data.Career) {
            val text1 = itemView.findViewById<android.widget.TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<android.widget.TextView>(android.R.id.text2)

            text1.text = career.title
            text2.text = "${career.description} | Demand: ${career.demand}"
        }
    }
}

class RequirementsTabFragment : Fragment() {
    private val viewModel: EduPathViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val recyclerView = androidx.recyclerview.widget.RecyclerView(requireContext())
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        val adapter = RequirementsAdapter()
        recyclerView.adapter = adapter

        viewModel.recommendations.observe(viewLifecycleOwner) { recommendations ->
            val requirements = recommendations
                .filter { it.type == com.example.edupath.data.RecommendationType.REQUIREMENTS }
                .flatMap { it.requirements }
            adapter.submitList(requirements)
        }

        return recyclerView
    }

    class RequirementsAdapter : androidx.recyclerview.widget.ListAdapter<String, RequirementViewHolder>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequirementViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return RequirementViewHolder(view)
        }

        override fun onBindViewHolder(holder: RequirementViewHolder, position: Int) {
            val requirement = getItem(position)
            holder.bind(requirement)
        }
    }

    class RequirementViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(requirement: String) {
            val textView = itemView.findViewById<android.widget.TextView>(android.R.id.text1)
            textView.text = "• $requirement"
        }
    }
}