package com.example.edupath.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.edupath.R
import com.example.edupath.databinding.FragmentGradeSelectionBinding

class GradeSelectionFragment : Fragment() {

    private var _binding: FragmentGradeSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGradeSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardGrade9.setOnClickListener {
            findNavController().navigate(R.id.action_gradeSelection_to_grade9Dashboard)
        }

        binding.cardGrade10.setOnClickListener {
            findNavController().navigate(R.id.action_gradeSelection_to_grade10Dashboard)
        }

        binding.cardGrade1112.setOnClickListener {
            findNavController().navigate(R.id.action_gradeSelection_to_grade1112Dashboard)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}