package com.example.edupath.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.edupath.data.*
import com.example.edupath.services.DocumentAnalysisResult
import com.example.edupath.services.ExtractedSubject

class EduPathViewModel : ViewModel() {

    private val _currentProfile = MutableLiveData<StudentProfile>()
    val currentProfile: LiveData<StudentProfile> = _currentProfile

    private val _recommendations = MutableLiveData<List<Recommendation>>()
    val recommendations: LiveData<List<Recommendation>> = _recommendations

    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> = _isProcessing

    private val _analysisResult = MutableLiveData<DocumentAnalysisResult?>()
    val analysisResult: LiveData<DocumentAnalysisResult?> = _analysisResult

    val availableInterests = listOf(
        "Technology", "Engineering", "Healthcare", "Business",
        "Arts", "Science", "Education", "Sports"
    )

    val availableSkills = listOf(
        "Problem Solving", "Communication", "Leadership",
        "Creativity", "Analytical Thinking", "Teamwork"
    )

    init {
        _currentProfile.value = StudentProfile(9, emptyList())
    }

    fun processDocumentAnalysisResult(result: DocumentAnalysisResult, grade: Int) {
        _isProcessing.value = true
        _analysisResult.value = result

        // Convert extracted subjects to app's Subject format
        val subjects = result.subjects.map { extracted ->
            Subject(
                name = extracted.name,
                score = extracted.score,
                isCore = isCoreSubject(extracted.name)
            )
        }

        val currentSkills = _currentProfile.value?.selectedSkills ?: emptyList()
        val currentInterests = _currentProfile.value?.selectedInterests ?: emptyList()
        val apsScore = calculateAPS(subjects)

        _currentProfile.value = StudentProfile(grade, subjects, currentSkills, currentInterests, apsScore)
        generateRecommendations(grade, subjects, apsScore, currentInterests, currentSkills)

        _isProcessing.value = false
    }

    fun processUploadedResults(grade: Int, subjects: List<Subject>) {
        _isProcessing.value = true

        val currentSkills = _currentProfile.value?.selectedSkills ?: emptyList()
        val currentInterests = _currentProfile.value?.selectedInterests ?: emptyList()
        val apsScore = calculateAPS(subjects)

        _currentProfile.value = StudentProfile(grade, subjects, currentSkills, currentInterests, apsScore)
        generateRecommendations(grade, subjects, apsScore, currentInterests, currentSkills)

        _isProcessing.value = false
    }

    fun updateInterests(interests: List<String>) {
        val currentProfile = _currentProfile.value ?: return
        val apsScore = calculateAPS(currentProfile.subjects)
        _currentProfile.value = currentProfile.copy(selectedInterests = interests, apsScore = apsScore)
        generateRecommendations(
            currentProfile.grade,
            currentProfile.subjects,
            apsScore,
            interests,
            currentProfile.selectedSkills
        )
    }

    fun updateSkills(skills: List<String>) {
        val currentProfile = _currentProfile.value ?: return
        val apsScore = calculateAPS(currentProfile.subjects)
        _currentProfile.value = currentProfile.copy(selectedSkills = skills, apsScore = apsScore)
        generateRecommendations(
            currentProfile.grade,
            currentProfile.subjects,
            apsScore,
            currentProfile.selectedInterests,
            skills
        )
    }

    fun clearData() {
        _currentProfile.value = StudentProfile(9, emptyList())
        _recommendations.value = emptyList()
        _analysisResult.value = null
    }

    private fun isCoreSubject(subjectName: String): Boolean {
        val coreSubjects = listOf(
            "mathematics", "english", "home language", "life orientation",
            "first additional language", "natural science", "science"
        )
        return coreSubjects.any { subjectName.lowercase().contains(it) }
    }

    private fun calculateAPS(subjects: List<Subject>): Int {
        return subjects.map { subject ->
            when {
                subject.score >= 80 -> 7
                subject.score >= 70 -> 6
                subject.score >= 60 -> 5
                subject.score >= 50 -> 4
                subject.score >= 40 -> 3
                subject.score >= 30 -> 2
                else -> 1
            }
        }.sum()
    }

    private fun generateRecommendations(
        grade: Int,
        subjects: List<Subject>,
        apsScore: Int,
        interests: List<String>,
        skills: List<String>
    ) {
        val recommendations = mutableListOf<Recommendation>()

        // Calculate subject strengths
        val strongSubjects = subjects.filter { it.score >= 70 }
        val weakSubjects = subjects.filter { it.score < 50 }

        when (grade) {
            9 -> {
                // Grade 9: Career exploration based on subject performance
                if (strongSubjects.isNotEmpty()) {
                    val careerRecommendations = generateCareerRecommendations(
                        strongSubjects,
                        interests,
                        skills,
                        apsScore
                    )
                    recommendations.add(careerRecommendations)
                }

                // Skills development recommendation
                if (weakSubjects.isNotEmpty()) {
                    recommendations.add(
                        Recommendation(
                            title = "Skills Development Focus",
                            description = "Consider focusing on these areas for improvement:",
                            type = RecommendationType.CAREER_GUIDANCE,
                            requirements = weakSubjects.map { "Improve ${it.name} (current: ${it.score}%)" }
                        )
                    )
                }
            }
            10 -> {
                // Grade 10: Stream recommendations
                val streamRecommendations = generateStreamRecommendations(subjects, interests, apsScore)
                recommendations.add(streamRecommendations)

                // School recommendations
                val schoolRecommendations = generateSchoolRecommendations(streamRecommendations.schools)
                recommendations.add(schoolRecommendations)
            }
            in 11..12 -> {
                // University recommendations
                val universityRecommendations = generateUniversityRecommendations(apsScore, interests, subjects)
                recommendations.add(universityRecommendations)

                // Career pathways
                val careerPathways = generateCareerPathways(apsScore, interests, skills)
                recommendations.add(careerPathways)

                // Admission requirements
                recommendations.add(generateAdmissionRequirements(apsScore))
            }
        }

        _recommendations.value = recommendations
    }

    private fun generateCareerRecommendations(
        strongSubjects: List<Subject>,
        interests: List<String>,
        skills: List<String>,
        apsScore: Int
    ): Recommendation {
        val careers = mutableListOf<Career>()

        // Analyze subject patterns for career matching
        val hasMathStrength = strongSubjects.any { it.name.lowercase().contains("math") }
        val hasScienceStrength = strongSubjects.any { it.name.lowercase().contains("science") }
        val hasLanguageStrength = strongSubjects.any { it.name.lowercase().contains("english") || it.name.lowercase().contains("language") }

        if (hasMathStrength && (interests.contains("Technology") || interests.contains("Engineering"))) {
            careers.add(
                Career(
                    title = "Software Developer",
                    description = "Design, develop, and test software applications and systems.",
                    demand = "High",
                    requirements = listOf("Mathematics", "Computer Science", "Problem Solving"),
                    skillsNeeded = listOf("Logical Thinking", "Creativity", "Attention to Detail"),
                    elevatorPitch = "Build the digital future with code and innovation"
                )
            )
        }

        if (hasScienceStrength && (interests.contains("Healthcare") || interests.contains("Science"))) {
            careers.add(
                Career(
                    title = "Medical Doctor",
                    description = "Diagnose and treat medical conditions, promote health and wellness.",
                    demand = "Very High",
                    requirements = listOf("Life Sciences", "Physical Sciences", "Mathematics"),
                    skillsNeeded = listOf("Empathy", "Communication", "Critical Thinking"),
                    elevatorPitch = "Save lives and make a difference in healthcare"
                )
            )
        }

        if (hasLanguageStrength && (interests.contains("Business") || interests.contains("Education"))) {
            careers.add(
                Career(
                    title = "Marketing Manager",
                    description = "Develop strategies to promote products and services to target audiences.",
                    demand = "High",
                    requirements = listOf("Business Studies", "Languages", "Economics"),
                    skillsNeeded = listOf("Creativity", "Communication", "Analytical Thinking"),
                    elevatorPitch = "Shape brand stories and connect with customers"
                )
            )
        }

        // Fallback careers based on APS score
        if (careers.isEmpty()) {
            careers.addAll(getDefaultCareers(apsScore))
        }

        return Recommendation(
            title = "Career Pathways",
            description = "Based on your strengths in ${strongSubjects.size} subjects and interests",
            type = RecommendationType.CAREER_GUIDANCE,
            careers = careers
        )
    }

    private fun generateStreamRecommendations(
        subjects: List<Subject>,
        interests: List<String>,
        apsScore: Int
    ): Recommendation {
        val recommendedStreams = mutableListOf<String>()

        val mathScore = subjects.find { it.name.lowercase().contains("math") }?.score ?: 0
        val scienceScore = subjects.find { it.name.lowercase().contains("science") }?.score ?: 0
        val commerceScore = subjects.find { it.name.lowercase().contains("business") || it.name.lowercase().contains("economic") }?.score ?: 0

        if (mathScore >= 60 && scienceScore >= 60 && apsScore >= 25) {
            recommendedStreams.add("Science Stream")
            if (interests.contains("Engineering")) {
                recommendedStreams.add("Engineering Focus")
            }
            if (interests.contains("Technology")) {
                recommendedStreams.add("IT Focus")
            }
        }

        if (commerceScore >= 60 && apsScore >= 22) {
            recommendedStreams.add("Commerce Stream")
            if (interests.contains("Business")) {
                recommendedStreams.add("Business Management")
            }
        }

        if (recommendedStreams.isEmpty()) {
            recommendedStreams.add("General Stream")
            recommendedStreams.add("Vocational Stream")
        }

        return Recommendation(
            title = "Stream Recommendations",
            description = "Recommended academic streams based on your performance",
            type = RecommendationType.STREAM_RECOMMENDATION,
            requirements = recommendedStreams,
            schools = generateSchoolsForStreams(recommendedStreams)
        )
    }

    private fun generateSchoolRecommendations(schools: List<School>): Recommendation {
        return Recommendation(
            title = "Recommended Schools",
            description = "Schools offering your recommended streams in your area",
            type = RecommendationType.STREAM_RECOMMENDATION,
            schools = schools
        )
    }

    private fun generateUniversityRecommendations(
        apsScore: Int,
        interests: List<String>,
        subjects: List<Subject>
    ): Recommendation {
        val universities = mutableListOf<University>()

        // University recommendations based on APS score and interests
        if (apsScore >= 40) {
            universities.add(
                University(
                    name = "University of Cape Town",
                    program = "BSc Computer Science",
                    location = "Cape Town",
                    apsRequirement = 42,
                    status = if (apsScore >= 42) AdmissionStatus.EXCEEDS_REQUIREMENT else AdmissionStatus.MEETS_REQUIREMENT
                )
            )
        }

        if (apsScore >= 35) {
            universities.add(
                University(
                    name = "University of Witwatersrand",
                    program = "BCom Accounting",
                    location = "Johannesburg",
                    apsRequirement = 38,
                    status = when {
                        apsScore >= 38 -> AdmissionStatus.EXCEEDS_REQUIREMENT
                        apsScore >= 35 -> AdmissionStatus.MEETS_REQUIREMENT
                        else -> AdmissionStatus.BELOW_REQUIREMENT
                    }
                )
            )
        }

        if (apsScore >= 30) {
            universities.add(
                University(
                    name = "University of Mpumalanga",
                    program = "BEd Foundation Phase",
                    location = "Mbombela",
                    apsRequirement = 30,
                    status = if (apsScore >= 30) AdmissionStatus.MEETS_REQUIREMENT else AdmissionStatus.BELOW_REQUIREMENT
                )
            )
        }

        // Add universities based on interests
        if (interests.contains("Engineering") && apsScore >= 35) {
            universities.add(
                University(
                    name = "Stellenbosch University",
                    program = "BEng Mechanical Engineering",
                    location = "Stellenbosch",
                    apsRequirement = 40,
                    status = if (apsScore >= 40) AdmissionStatus.EXCEEDS_REQUIREMENT else AdmissionStatus.BELOW_REQUIREMENT
                )
            )
        }

        return Recommendation(
            title = "University Pathways",
            description = "Bachelor's programs matching your APS score of $apsScore",
            type = RecommendationType.UNIVERSITY_RECOMMENDATION,
            universities = universities
        )
    }

    private fun generateCareerPathways(
        apsScore: Int,
        interests: List<String>,
        skills: List<String>
    ): Recommendation {
        val careers = mutableListOf<Career>()

        // Career recommendations based on APS score ranges
        when {
            apsScore >= 40 -> {
                careers.addAll(getHighDemandCareers())
            }
            apsScore >= 30 -> {
                careers.addAll(getMediumDemandCareers())
            }
            else -> {
                careers.addAll(getVocationalCareers())
            }
        }

        // Filter by interests
        val filteredCareers = careers.filter { career ->
            interests.any { interest ->
                career.title.lowercase().contains(interest.lowercase()) ||
                        career.skillsNeeded.any { skill -> skills.contains(skill) }
            }
        }

        return Recommendation(
            title = "Career Pathways",
            description = "High-demand careers matching your profile",
            type = RecommendationType.CAREER_GUIDANCE,
            careers = if (filteredCareers.isNotEmpty()) filteredCareers else careers.take(3)
        )
    }

    private fun generateAdmissionRequirements(apsScore: Int): Recommendation {
        val requirements = mutableListOf(
            "Minimum 30% in Home Language",
            "50% in four subjects including Mathematics or Mathematical Literacy",
            "Valid South African ID",
            "Grade 12 Certificate",
            "Minimum APS score of 28 for most programs"
        )

        if (apsScore < 30) {
            requirements.add("Consider foundation programs or bridging courses")
            requirements.add("Improve your APS score by retaking key subjects")
        }

        return Recommendation(
            title = "Admission Requirements",
            description = "Key requirements for tertiary education applications",
            type = RecommendationType.REQUIREMENTS,
            requirements = requirements
        )
    }

    private fun generateSchoolsForStreams(streams: List<String>): List<School> {
        val schools = mutableListOf<School>()

        if (streams.any { it.contains("Science") || it.contains("Engineering") }) {
            schools.add(
                School(
                    name = "Mpumalanga Science Academy",
                    location = "Nelspruit",
                    distance = "5km",
                    type = "Public",
                    streams = listOf("Science", "Engineering", "IT")
                )
            )
        }

        if (streams.any { it.contains("Commerce") || it.contains("Business") }) {
            schools.add(
                School(
                    name = "Nelspruit Commercial High",
                    location = "Nelspruit",
                    distance = "3km",
                    type = "Public",
                    streams = listOf("Commerce", "Business Studies", "Economics")
                )
            )
        }

        schools.add(
            School(
                name = "Tech Innovation Academy",
                location = "Mbombela",
                distance = "8km",
                type = "Technical",
                streams = listOf("Engineering", "IT", "Technical Drawing")
            )
        )

        return schools
    }

    private fun getDefaultCareers(apsScore: Int): List<Career> {
        return listOf(
            Career(
                title = "IT Support Specialist",
                description = "Provide technical assistance and support for computer systems and software.",
                demand = "High",
                requirements = listOf("Computer Studies", "Mathematics"),
                skillsNeeded = listOf("Problem Solving", "Communication"),
                elevatorPitch = "Help people solve technology problems every day"
            ),
            Career(
                title = "Healthcare Assistant",
                description = "Support medical staff in providing patient care in various healthcare settings.",
                demand = "Very High",
                requirements = listOf("Life Sciences", "Life Orientation"),
                skillsNeeded = listOf("Empathy", "Teamwork", "Communication"),
                elevatorPitch = "Make a difference in patients' lives every day"
            )
        )
    }

    private fun getHighDemandCareers(): List<Career> {
        return listOf(
            Career(
                title = "Data Scientist",
                description = "Analyze and interpret complex data to help organizations make better decisions.",
                demand = "Very High",
                requirements = listOf("Mathematics", "Statistics", "Computer Science"),
                skillsNeeded = listOf("Analytical Thinking", "Programming", "Statistics"),
                elevatorPitch = "Turn data into insights that drive business decisions"
            ),
            Career(
                title = "Software Engineer",
                description = "Design, develop, and maintain software systems and applications.",
                demand = "Very High",
                requirements = listOf("Mathematics", "Computer Science", "Physics"),
                skillsNeeded = listOf("Problem Solving", "Logic", "Creativity"),
                elevatorPitch = "Create technology that changes how people live and work"
            )
        )
    }

    private fun getMediumDemandCareers(): List<Career> {
        return listOf(
            Career(
                title = "Registered Nurse",
                description = "Provide and coordinate patient care, educate patients about health conditions.",
                demand = "High",
                requirements = listOf("Life Sciences", "Physical Sciences", "Mathematics"),
                skillsNeeded = listOf("Compassion", "Communication", "Critical Thinking"),
                elevatorPitch = "Provide compassionate care and save lives"
            ),
            Career(
                title = "Marketing Specialist",
                description = "Develop and implement marketing strategies to promote products and services.",
                demand = "Medium",
                requirements = listOf("Business Studies", "Languages", "Economics"),
                skillsNeeded = listOf("Creativity", "Communication", "Analytical Skills"),
                elevatorPitch = "Connect brands with their ideal customers"
            )
        )
    }

    private fun getVocationalCareers(): List<Career> {
        return listOf(
            Career(
                title = "Electrician",
                description = "Install, maintain, and repair electrical power systems and equipment.",
                demand = "High",
                requirements = listOf("Mathematics", "Physical Sciences", "Technical Drawing"),
                skillsNeeded = listOf("Problem Solving", "Technical Skills", "Safety Awareness"),
                elevatorPitch = "Power communities with essential electrical services"
            ),
            Career(
                title = "IT Technician",
                description = "Install, maintain, and repair computer systems and networks.",
                demand = "Medium",
                requirements = listOf("Computer Studies", "Mathematics"),
                skillsNeeded = listOf("Technical Skills", "Problem Solving", "Customer Service"),
                elevatorPitch = "Keep technology running smoothly for businesses"
            )
        )
    }
}