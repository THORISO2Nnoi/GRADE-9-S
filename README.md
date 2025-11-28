# EduPath - Grade 9 Career Guidance Application

## 📱 Application Overview

EduPath is an Android application designed to help Grade 9 students in South Africa make informed decisions about their academic and career paths. The app analyzes students' academic performance and provides personalized career recommendations.

## 🎯 Purpose

The application addresses the critical decision point where Grade 9 students need to choose their subjects and streams for Grades 10-12. It provides data-driven insights to help students understand their strengths and potential career paths.

## ✨ Key Features

### 📊 Academic Performance Analysis
- **Upload Grade 9 report cards** via camera, gallery, or PDF
- **Automatic subject extraction** and performance analysis
- **Visual performance breakdown** with color-coded progress bars
- **Strength identification** across different subject areas

### 🎓 Career Pathway Recommendations
- **Personalized career suggestions** based on academic strengths
- **Subject-specific career matching** (STEM, Arts, Commerce, etc.)
- **Skills assessment** and development recommendations
- **Future demand analysis** for suggested careers

### 📚 Skills Development
- **Interactive skills selection** from a comprehensive list
- **Skill-strength correlation** with academic performance
- **Development area identification**

## 🏗️ Technical Architecture

### Frontend
- **Kotlin** with **Android Jetpack** components
- **Material Design 3** for modern UI/UX
- **ViewBinding** for type-safe view references
- **Navigation Component** for fragment management

### Architecture
- **MVVM (Model-View-ViewModel)** pattern
- **LiveData** for reactive UI updates
- **Repository pattern** for data management

### Key Components
- **Fragments**: Grade-specific dashboards
- **Adapters**: RecyclerView adapters for lists
- **ViewModels**: Business logic and data management
- **Data Classes**: Type-safe data models

## 📁 Project Structure




## 🚀 Installation & Setup

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 34
- Minimum SDK 24 (Android 7.0)

### Build Instructions
1. Clone the repository
2. Open in Android Studio
3. Sync project with Gradle files
4. Build the project (Ctrl+F9)
5. Run on emulator or device (Shift+F10)

## 🎮 Usage Guide

### For Grade 9 Students:
1. **Launch the app** and select "Grade 9"
2. **Upload your report card** using one of three methods:
   - Take a photo of your report card
   - Choose from gallery
   - Upload PDF document
3. **View performance analysis** of your subjects
4. **Select your skills** from the provided list
5. **Receive personalized career recommendations**

### Features in Action:
- **Subject Performance Cards**: Visual representation of scores
- **Skills Assessment**: Interactive skill selection
- **Career Pathways**: Data-driven career suggestions
- **Progress Tracking**: Academic performance insights

## 🔧 Technical Implementation Details

### Data Models
- `StudentProfile`: Main student data container
- `Subject`: Academic subject with scores
- `Recommendation`: Career and pathway suggestions
- `Career`: Detailed career information

### ViewModels
- `EduPathViewModel`: Central business logic
- Manages student profiles and recommendations
- Handles data processing and analysis

### Adapters
- `SubjectsAdapter`: Displays subject performance
- `SkillsAdapter`: Handles skill selection
- `RecommendationsAdapter`: Shows career suggestions

## 🎨 UI/UX Design

- **Material Design 3** components
- **Responsive layouts** for different screen sizes
- **Color-coded performance indicators**
- **Intuitive navigation flow**
- **Accessibility considerations**

## 📈 Future Enhancements

- [ ] **AI/ML Integration** for document processing
- [ ] **University Database** integration
- [ ] **Career Market Trends** data
- [ ] **Mentorship Program** connections
- [ ] **Scholarship Opportunities** database
- [ ] **Parent/Teacher Portal**
- [ ] **Multi-language Support**

## 🤝 Contributing

We welcome contributions! Please feel free to submit pull requests or open issues for bugs and feature requests.

### Development Setup
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- South African Department of Basic Education curriculum
- Material Design 3 components
- Android Jetpack libraries
- Kotlin programming language

## 📞 Support

For support and questions:
- Open an issue on GitHub
- Contact the development team

---

**Built with ❤️ for South African students' educational journey**
