package com.f1.quiket.feature.floating.domain.model

// ────────────────────────────────────────────
// 1depth - 학습 목적
// ────────────────────────────────────────────
enum class StudyPurpose(val title: String, val description: String) {
    EXAM("시험·자격증 대비", "시험일, 범위를 체계적으로 준비해요"),
    SELF_STUDY("자기계발·일반 복습", "언제든 퀴즈로 복습해요"),
    OTHER("기타", "자유롭게 설정할게요"),
}

// ────────────────────────────────────────────
// 2depth - 시험 유형 (EXAM)
// ────────────────────────────────────────────
enum class ExamType(val label: String) {
    UNIVERSITY("대학시험"),
    MIDDLE_HIGH("중고등시험"),
    CERTIFICATE("자격증"),
    LANGUAGE("어학"),
    CIVIL_SERVICE("공무원 시험"),
    OTHER_EXAM("기타"),
}

// 2depth - 분야 (SELF_STUDY) 12개
enum class StudyField(val label: String) {
    HUMANITIES("인문"),
    KOREAN_HISTORY("한국사"),
    WORLD_HISTORY("세계사"),
    SOCIETY("사회"),
    POLITICS("정치"),
    ECONOMICS("경제"),
    BUSINESS("경영"),
    SCIENCE_TECH("과학기술"),
    IT("IT"),
    CULTURE_ART("문화예술"),
    PSYCHOLOGY("심리학"),
    CUSTOM("직접 입력"),
}

// 2depth - 이용 목적 (OTHER) 5개
enum class UsagePurpose(val title: String, val description: String) {
    WORK("업무·실무 활용", "업모 자료 정리, 교육자료 복습 등"),
    PERSONAL("개인 기록·정리", "노트 아카이빙, 자료 및 필기 구조화 등"),
    HOBBY("취미·가벼운 학습", "관심 분야 탐색, 교양 수준 학습, 가벼운 콘텐츠 정리 등"),
    MEMORY("기억·암기 보조", "반복 학습, 단순 기억 강화 등"),
    OTHER("기타", "다른 목적에 조금 더 가까워요!")
}

// ────────────────────────────────────────────
// 3depth - 대학시험
// ────────────────────────────────────────────
enum class UniversityMajorCategory(val label: String) {
    HUMANITIES("인문"),
    SOCIAL("사회"),
    NATURAL("자연"),
    ENGINEERING("공학"),
    ARTS("예체능"),
    EDUCATION("사범"),
    MEDICINE("의약"),
    OTHER("기타"),
}

enum class CourseType(val label: String) {
    MAJOR("전공"),
    LIBERAL("교양"),
}

// 3depth - 중고등 교육과정 6개
enum class MiddleHighCurriculum(val label: String) {
    ELEM("초등"),
    MIDDLE("중학"),
    HIGH1("고1"),
    HIGH2("고2"),
    HIGH3("고3"),
    CSAT("수능"),
}

// 3depth - 중고등 과목 유형 9개 (마지막은 직접입력)
enum class MiddleHighSubjectType(val label: String) {
    KOREAN("국어"),
    MATH("수학"),
    ENGLISH("영어"),
    SCIENCE("과학"),
    SOCIAL("사회"),
    HISTORY("역사"),
    ETHICS("윤리"),
    ART("예체능"),
    CUSTOM("직접 입력"),
}

// 3depth - 어학 언어 종류
enum class LanguageType(val label: String) {
    ENGLISH("영어"),
    JAPANESE("일본어"),
    CHINESE("중국어"),
}

// 영어 시험 6개
enum class EnglishTestType(val label: String) {
    TOEIC("TOEIC"),
    TOEFL("TOEFL"),
    IELTS("IELTS"),
    TEPS("TEPS"),
    OPIc("OPIc"),
    CUSTOM("직접 입력"),
}

// 일본어 시험 3개
enum class JapaneseTestType(val label: String) {
    JLPT("JLPT"),
    JPT("JPT"),
    CUSTOM("직접 입력"),
}

// 중국어 시험 3개
enum class ChineseTestType(val label: String) {
    HSK("HSK"),
    HSKK("HSKK"),
    CUSTOM("직접 입력"),
}

// 3depth - 공무원 급수
enum class CivilServantGrade(val label: String) {
    GRADE9("9급"),
    GRADE7("7급"),
    GRADE5("5급"),
    POLICE("경찰직"),
    FIRE("소방직"),
    SPECIAL("기타 특수직"),
}

// 공무원 직렬 12개
enum class CivilServantSeries(val label: String) {
    ADMIN("행정"),
    TAX("세무"),
    CUSTOM_DUTY("관세"),
    SOCIAL_WELFARE("사회복지"),
    EDUCATION("교육행정"),
    LABOR("고용노동"),
    JUDICIARY("법원"),
    PROSECUTION("검찰"),
    POLICE("경찰"),
    FIRE("소방"),
    MILITARY("군무원"),
    OTHER("기타"),
}

// 3depth - 자기계발 숙련도 4개 (name = 백엔드 study_level 값과 일치)
enum class FamiliarityLevel(val label: String, val description: String) {
    BEGINNER("입문자", "처음 배우는 단계예요"),
    CASUAL("초급자", "기초 개념은 알고 있어요"),
    REGULAR("중급자", "어느 정도 익숙해요"),
    EXPERT("고급자", "심화 내용도 다뤄요"),
}

// ────────────────────────────────────────────
// Backend value → enum mapping helpers
// ────────────────────────────────────────────

fun examTypeFromBackendValue(value: String): ExamType? = when (value.lowercase()) {
    "university" -> ExamType.UNIVERSITY
    "middle_high" -> ExamType.MIDDLE_HIGH
    "certificate" -> ExamType.CERTIFICATE
    "language" -> ExamType.LANGUAGE
    "civil_service", "civil_servant" -> ExamType.CIVIL_SERVICE
    "other_exam", "other" -> ExamType.OTHER_EXAM
    else -> null
}

fun studyPurposeFromBackendValue(value: String): StudyPurpose = when (value.lowercase()) {
    "exam" -> StudyPurpose.EXAM
    "review", "self_study" -> StudyPurpose.SELF_STUDY
    else -> StudyPurpose.OTHER
}

data class AddSubjectState(
    val subjectName: String = "",
    val studyPurpose: StudyPurpose? = null,
    val step3Label: String = "",

    // EXAM path
    val examType: ExamType? = null,
    // university
    val majorCategory: UniversityMajorCategory? = null,
    val majorName: String = "",
    val courseType: CourseType? = null,
    // middle/high
    val curriculum: MiddleHighCurriculum? = null,
    val subjectType: MiddleHighSubjectType? = null,
    val customSubjectType: String = "",
    // certificate
    val certificateName: String = "",
    // language
    val languageType: LanguageType? = null,
    val englishTest: EnglishTestType? = null,
    val japaneseTest: JapaneseTestType? = null,
    val chineseTest: ChineseTestType? = null,
    val customLanguageTest: String = "",
    // civil servant
    val civilServantGrade: CivilServantGrade? = null,
    val civilServantSeries: CivilServantSeries? = null,
    // exam other
    val otherExamText: String = "",

    // SELF_STUDY path
    val studyField: StudyField? = null,
    val familiarityLevel: FamiliarityLevel? = null,

    // OTHER path
    val usagePurpose: UsagePurpose? = null,
    val additionalDescription: String = "",
)