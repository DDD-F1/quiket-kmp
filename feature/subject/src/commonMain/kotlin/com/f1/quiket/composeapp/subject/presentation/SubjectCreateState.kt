package com.f1.quiket.composeapp.subject.presentation

import com.f1.quiket.composeapp.subject.domain.model.SubjectCreateInput
import com.f1.quiket.composeapp.subject.domain.model.SubjectCreatePurpose
import com.f1.quiket.composeapp.subject.domain.model.SubjectDetail
import com.f1.quiket.composeapp.subject.domain.model.SubjectExamDetail
import com.f1.quiket.composeapp.subject.domain.model.SubjectExamDetailInput
import com.f1.quiket.composeapp.subject.domain.model.SubjectOtherDetailInput
import com.f1.quiket.composeapp.subject.domain.model.SubjectReviewDetailInput
import org.jetbrains.compose.resources.DrawableResource
import com.f1.quiket.feature.subject.resources.Res
import com.f1.quiket.feature.subject.resources.ic_addsubject_certify
import com.f1.quiket.feature.subject.resources.ic_addsubject_civil
import com.f1.quiket.feature.subject.resources.ic_addsubject_language
import com.f1.quiket.feature.subject.resources.ic_addsubject_other
import com.f1.quiket.feature.subject.resources.ic_addsubject_school
import com.f1.quiket.feature.subject.resources.ic_addsubject_university

internal data class SubjectCreateDraft(
    val subjectName: String = "",
    val studyPurpose: SubjectCreatePurpose? = null,
    val examType: SubjectExamType? = null,
    val studyField: StudyField? = null,
    val usagePurpose: UsagePurpose? = null,
    val majorCategory: UniversityMajorCategory? = null,
    val majorName: String = "",
    val courseType: CourseType? = null,
    val curriculum: MiddleHighCurriculum? = null,
    val subjectType: MiddleHighSubjectType? = null,
    val customSubjectType: String = "",
    val certificateId: String? = null,
    val certificateName: String = "",
    val languageType: LanguageType? = null,
    val englishTest: EnglishTestType? = null,
    val japaneseTest: JapaneseTestType? = null,
    val chineseTest: ChineseTestType? = null,
    val customLanguageTest: String = "",
    val civilServantGrade: CivilServantGrade? = null,
    val civilServantSeries: CivilServantSeries? = null,
    val otherExamText: String = "",
    val familiarityLevel: FamiliarityLevel? = null,
    val additionalDescription: String = "",
) {
    fun withPurpose(purpose: SubjectCreatePurpose): SubjectCreateDraft = copy(
        studyPurpose = purpose,
        examType = null,
        studyField = null,
        usagePurpose = null,
        majorCategory = null,
        majorName = "",
        courseType = null,
        curriculum = null,
        subjectType = null,
        customSubjectType = "",
        certificateId = null,
        certificateName = "",
        languageType = null,
        englishTest = null,
        japaneseTest = null,
        chineseTest = null,
        customLanguageTest = "",
        civilServantGrade = null,
        civilServantSeries = null,
        otherExamText = "",
        familiarityLevel = null,
        additionalDescription = "",
    )

    fun withExamType(type: SubjectExamType): SubjectCreateDraft = copy(
        examType = type,
        majorCategory = null,
        majorName = "",
        courseType = null,
        curriculum = null,
        subjectType = null,
        customSubjectType = "",
        certificateId = null,
        certificateName = "",
        languageType = null,
        englishTest = null,
        japaneseTest = null,
        chineseTest = null,
        customLanguageTest = "",
        civilServantGrade = null,
        civilServantSeries = null,
        otherExamText = "",
    )

    fun isStep2Complete(): Boolean = when (studyPurpose) {
        SubjectCreatePurpose.Exam -> examType != null
        SubjectCreatePurpose.Review -> studyField != null
        SubjectCreatePurpose.Other -> usagePurpose != null
        null -> false
    }

    fun isReadyToCreate(): Boolean = when (studyPurpose) {
        SubjectCreatePurpose.Exam -> isExamDetailReady()
        SubjectCreatePurpose.Review -> studyField != null && familiarityLevel != null
        SubjectCreatePurpose.Other -> usagePurpose != null
        null -> false
    }

    fun needsCustomLanguageTest(): Boolean =
        englishTest == EnglishTestType.Custom ||
            japaneseTest == JapaneseTestType.Custom ||
            chineseTest == ChineseTestType.Custom

    fun toInput(trimmedName: String): SubjectCreateInput? {
        val purpose = studyPurpose ?: return null
        return SubjectCreateInput(
            name = trimmedName,
            purpose = purpose,
            examDetail = if (purpose == SubjectCreatePurpose.Exam) toExamDetailInput() else null,
            reviewDetail = if (purpose == SubjectCreatePurpose.Review) {
                SubjectReviewDetailInput(
                    field = studyField?.wireValue ?: "",
                    studyLevel = familiarityLevel?.wireValue ?: "",
                )
            } else {
                null
            },
            otherDetail = if (purpose == SubjectCreatePurpose.Other) {
                SubjectOtherDetailInput(
                    usagePurpose = usagePurpose?.wireValue ?: "",
                    description = additionalDescription.ifBlank { null },
                )
            } else {
                null
            },
        )
    }

    fun toSkippedInput(trimmedName: String): SubjectCreateInput {
        val purpose = studyPurpose ?: SubjectCreatePurpose.Other
        return SubjectCreateInput(
            name = trimmedName,
            purpose = purpose,
            examDetail = if (purpose == SubjectCreatePurpose.Exam) toExamDetailInput() else null,
            reviewDetail = if (purpose == SubjectCreatePurpose.Review) {
                SubjectReviewDetailInput(
                    field = studyField?.wireValue ?: "",
                    studyLevel = familiarityLevel?.wireValue ?: "",
                )
            } else {
                null
            },
            otherDetail = if (studyPurpose == SubjectCreatePurpose.Other) {
                SubjectOtherDetailInput(
                    usagePurpose = usagePurpose?.wireValue ?: "",
                    description = additionalDescription.ifBlank { null },
                )
            } else {
                null
            },
        )
    }

    private fun isExamDetailReady(): Boolean = when (examType) {
        SubjectExamType.University -> true
        SubjectExamType.MiddleHigh -> curriculum != null &&
            subjectType != null &&
            (subjectType != MiddleHighSubjectType.Custom || customSubjectType.isNotBlank())
        SubjectExamType.Certificate -> certificateName.isNotBlank()
        SubjectExamType.Language -> when (languageType) {
            LanguageType.English -> englishTest != null &&
                (englishTest != EnglishTestType.Custom || customLanguageTest.isNotBlank())
            LanguageType.Japanese -> japaneseTest != null &&
                (japaneseTest != JapaneseTestType.Custom || customLanguageTest.isNotBlank())
            LanguageType.Chinese -> chineseTest != null &&
                (chineseTest != ChineseTestType.Custom || customLanguageTest.isNotBlank())
            null -> false
        }
        SubjectExamType.CivilService -> civilServantGrade != null && civilServantSeries != null
        SubjectExamType.OtherExam -> otherExamText.isNotBlank()
        null -> false
    }

    private fun toExamDetailInput(): SubjectExamDetailInput = SubjectExamDetailInput(
        examType = examType?.wireValue ?: SubjectExamType.OtherExam.wireValue,
        univMajorField = majorCategory?.wireValue,
        univMajorName = majorName.ifBlank { null },
        univCourseType = courseType?.wireValue,
        mhGrade = curriculum?.wireValue,
        mhSubjectType = if (subjectType == MiddleHighSubjectType.Custom) {
            customSubjectType.ifBlank { null }
        } else {
            subjectType?.wireValue
        },
        certificateName = certificateName.ifBlank { null },
        certificateId = certificateId,
        langType = languageType?.wireValue,
        langExamName = languageExamName(),
        civilRank = civilServantGrade?.wireValue,
        civilSeries = civilServantSeries?.wireValue,
        otherExamName = otherExamText.ifBlank { null },
    )

    private fun languageExamName(): String? = when (languageType) {
        LanguageType.English -> if (englishTest == EnglishTestType.Custom) {
            customLanguageTest.ifBlank { null }
        } else {
            englishTest?.wireValue
        }
        LanguageType.Japanese -> if (japaneseTest == JapaneseTestType.Custom) {
            customLanguageTest.ifBlank { null }
        } else {
            japaneseTest?.wireValue
        }
        LanguageType.Chinese -> if (chineseTest == ChineseTestType.Custom) {
            customLanguageTest.ifBlank { null }
        } else {
            chineseTest?.wireValue
        }
        null -> null
    }
}

internal fun SubjectDetail.toCreateDraft(): SubjectCreateDraft {
    val purpose = purpose.toCreatePurpose()
    val base = SubjectCreateDraft(
        subjectName = name,
        studyPurpose = purpose,
    )
    return when (purpose) {
        SubjectCreatePurpose.Exam -> base.withExamDetail(examDetail)
        SubjectCreatePurpose.Review -> base.copy(
            studyField = StudyField.entries.findByWireValue(reviewDetail?.field),
            familiarityLevel = FamiliarityLevel.entries.findByWireValue(reviewDetail?.studyLevel),
        )
        SubjectCreatePurpose.Other -> base.copy(
            usagePurpose = UsagePurpose.entries.findByWireValue(otherDetail?.usagePurpose),
            additionalDescription = otherDetail?.description.orEmpty(),
        )
    }
}

internal data class CertificateOption(
    val id: String?,
    val name: String,
    val featured: Boolean,
    val displayOrder: Int,
)

internal fun String.normalizedCertificateName(): String =
    trim().lowercase()

private fun String.toCreatePurpose(): SubjectCreatePurpose = when (lowercase()) {
    "exam" -> SubjectCreatePurpose.Exam
    "review",
    "self_study",
    -> SubjectCreatePurpose.Review
    "other" -> SubjectCreatePurpose.Other
    else -> SubjectCreatePurpose.Exam
}

private fun SubjectCreateDraft.withExamDetail(detail: SubjectExamDetail?): SubjectCreateDraft {
    if (detail == null) return this
    val mappedExamType = SubjectExamType.entries.findByWireValue(detail.examType)
    return copy(
        examType = mappedExamType,
        majorCategory = UniversityMajorCategory.entries.findByWireValue(detail.univMajorField),
        majorName = detail.univMajorName.orEmpty(),
        courseType = CourseType.entries.findByWireValue(detail.univCourseType),
        curriculum = MiddleHighCurriculum.entries.findByWireValue(detail.mhGrade),
        subjectType = detail.mhSubjectType.toMiddleHighSubjectType(),
        customSubjectType = detail.mhSubjectType.toCustomWireText(MiddleHighSubjectType.entries),
        certificateId = detail.certificateId,
        certificateName = detail.certificateName.orEmpty(),
        languageType = LanguageType.entries.findByWireValue(detail.langType),
        englishTest = detail.langExamName.toLanguageExamType(
            languageType = LanguageType.English,
            selectedLanguage = detail.langType,
            entries = EnglishTestType.entries,
        ),
        japaneseTest = detail.langExamName.toLanguageExamType(
            languageType = LanguageType.Japanese,
            selectedLanguage = detail.langType,
            entries = JapaneseTestType.entries,
        ),
        chineseTest = detail.langExamName.toLanguageExamType(
            languageType = LanguageType.Chinese,
            selectedLanguage = detail.langType,
            entries = ChineseTestType.entries,
        ),
        customLanguageTest = detail.langExamName.toCustomLanguageText(detail.langType),
        civilServantGrade = CivilServantGrade.entries.findByWireValue(detail.civilRank),
        civilServantSeries = CivilServantSeries.entries.findByWireValue(detail.civilSeries),
        otherExamText = detail.otherExamName.orEmpty(),
    )
}

private fun String?.toMiddleHighSubjectType(): MiddleHighSubjectType? {
    val value = this?.takeIf { it.isNotBlank() } ?: return null
    return MiddleHighSubjectType.entries.findByWireValue(value) ?: MiddleHighSubjectType.Custom
}

private fun <T> String?.toCustomWireText(entries: List<T>): String where T : Enum<T>, T : WireEnum {
    val value = this?.takeIf { it.isNotBlank() } ?: return ""
    return if (entries.findByWireValue(value) == null) value else ""
}

private fun <T> String?.toLanguageExamType(
    languageType: LanguageType,
    selectedLanguage: String?,
    entries: List<T>,
): T? where T : Enum<T>, T : WireEnum {
    if (!selectedLanguage.equalsWireValue(languageType.wireValue)) return null
    val value = this?.takeIf { it.isNotBlank() } ?: return null
    return entries.findByWireValue(value) ?: entries.find { it.wireValue == "custom" }
}

private fun String?.toCustomLanguageText(selectedLanguage: String?): String {
    val value = this?.takeIf { it.isNotBlank() } ?: return ""
    val entries = when {
        selectedLanguage.equalsWireValue(LanguageType.English.wireValue) -> EnglishTestType.entries
        selectedLanguage.equalsWireValue(LanguageType.Japanese.wireValue) -> JapaneseTestType.entries
        selectedLanguage.equalsWireValue(LanguageType.Chinese.wireValue) -> ChineseTestType.entries
        else -> return ""
    }
    return if (entries.findByWireValue(value) == null) value else ""
}

internal interface WireEnum {
    val wireValue: String
}

internal fun <T> List<T>.findByWireValue(value: String?): T? where T : Enum<T>, T : WireEnum =
    value?.lowercase()?.let { normalized ->
        firstOrNull { item ->
            item.wireValue.equals(normalized, ignoreCase = true) ||
                item.name.equals(normalized, ignoreCase = true)
        }
    }

private fun String?.equalsWireValue(value: String): Boolean =
    this?.equals(value, ignoreCase = true) == true

internal enum class SubjectExamType(
    val label: String,
    override val wireValue: String,
) : WireEnum {
    University("대학시험", "university"),
    MiddleHigh("중고등시험", "middle_high"),
    Certificate("자격증", "certificate"),
    Language("어학", "language"),
    CivilService("공무원 시험", "civil_service"),
    OtherExam("기타", "other_exam"),
}

internal val SubjectExamType.icon: DrawableResource
    get() = when (this) {
        SubjectExamType.University -> Res.drawable.ic_addsubject_university
        SubjectExamType.MiddleHigh -> Res.drawable.ic_addsubject_school
        SubjectExamType.Certificate -> Res.drawable.ic_addsubject_certify
        SubjectExamType.Language -> Res.drawable.ic_addsubject_language
        SubjectExamType.CivilService -> Res.drawable.ic_addsubject_civil
        SubjectExamType.OtherExam -> Res.drawable.ic_addsubject_other
    }

internal enum class StudyField(
    val label: String,
    override val wireValue: String,
) : WireEnum {
    Humanities("인문", "humanities"),
    KoreanHistory("한국사", "korean_history"),
    WorldHistory("세계사", "world_history"),
    Society("사회", "society"),
    Politics("정치", "politics"),
    Economics("경제", "economics"),
    Business("경영", "business"),
    ScienceTech("과학기술", "science_tech"),
    It("IT", "it"),
    CultureArt("문화예술", "culture_art"),
    Psychology("심리학", "psychology"),
    Custom("직접 입력", "custom"),
}

internal enum class UsagePurpose(
    val label: String,
    val description: String,
    override val wireValue: String,
) : WireEnum {
    Work("업무·실무 활용", "업무 자료 정리, 교육자료 복습 등", "work"),
    Personal("개인 기록·정리", "노트 아카이빙, 자료 및 필기 구조화 등", "personal"),
    Hobby("취미·가벼운 학습", "관심 분야 탐색, 교양 수준 학습 등", "hobby"),
    Memory("기억·암기 보조", "반복 학습, 단순 기억 강화 등", "memory"),
    Other("기타", "다른 목적에 조금 더 가까워요", "other"),
}

internal enum class UniversityMajorCategory(
    val label: String,
    override val wireValue: String,
) : WireEnum {
    Humanities("인문", "humanities"),
    Social("사회", "social"),
    Natural("자연", "natural"),
    Engineering("공학", "engineering"),
    Arts("예체능", "arts"),
    Education("사범", "education"),
    Medicine("의약", "medicine"),
    Other("기타", "other"),
}

internal enum class CourseType(
    val label: String,
    override val wireValue: String,
) : WireEnum {
    Major("전공", "major"),
    Liberal("교양", "liberal_arts"),
}

internal enum class MiddleHighCurriculum(
    val label: String,
    override val wireValue: String,
) : WireEnum {
    Elem("초등", "elem"),
    Middle("중학", "middle"),
    High1("고1", "high1"),
    High2("고2", "high2"),
    High3("고3", "high3"),
    Csat("수능", "csat"),
}

internal enum class MiddleHighSubjectType(
    val label: String,
    override val wireValue: String,
) : WireEnum {
    Korean("국어", "korean"),
    Math("수학", "math"),
    English("영어", "english"),
    Science("과학", "science"),
    Social("사회", "social"),
    History("역사", "history"),
    Ethics("윤리", "ethics"),
    Art("예체능", "art"),
    Custom("직접 입력", "custom"),
}

internal enum class LanguageType(
    val label: String,
    override val wireValue: String,
) : WireEnum {
    English("영어", "english"),
    Japanese("일본어", "japanese"),
    Chinese("중국어", "chinese"),
}

internal enum class EnglishTestType(
    val label: String,
    override val wireValue: String,
) : WireEnum {
    Toeic("TOEIC", "toeic"),
    Toefl("TOEFL", "toefl"),
    Ielts("IELTS", "ielts"),
    Teps("TEPS", "teps"),
    Opic("OPIc", "opic"),
    Custom("직접 입력", "custom"),
}

internal enum class JapaneseTestType(
    val label: String,
    override val wireValue: String,
) : WireEnum {
    Jlpt("JLPT", "jlpt"),
    Jpt("JPT", "jpt"),
    Custom("직접 입력", "custom"),
}

internal enum class ChineseTestType(
    val label: String,
    override val wireValue: String,
) : WireEnum {
    Hsk("HSK", "hsk"),
    Hskk("HSKK", "hskk"),
    Custom("직접 입력", "custom"),
}

internal enum class CivilServantGrade(
    val label: String,
    override val wireValue: String,
) : WireEnum {
    Grade9("9급", "grade9"),
    Grade7("7급", "grade7"),
    Grade5("5급", "grade5"),
    Police("경찰직", "police"),
    Fire("소방직", "fire"),
    Special("기타 특수직", "special"),
}

internal enum class CivilServantSeries(
    val label: String,
    override val wireValue: String,
) : WireEnum {
    Admin("행정", "admin"),
    Tax("세무", "tax"),
    CustomDuty("관세", "custom_duty"),
    SocialWelfare("사회복지", "social_welfare"),
    Education("교육행정", "education"),
    Labor("고용노동", "labor"),
    Judiciary("법원", "judiciary"),
    Prosecution("검찰", "prosecution"),
    Police("경찰", "police"),
    Fire("소방", "fire"),
    Military("군무원", "military"),
    Other("기타", "other"),
}

internal enum class FamiliarityLevel(
    val label: String,
    val description: String,
    override val wireValue: String,
) : WireEnum {
    Beginner("입문자", "처음 배우는 단계예요", "beginner"),
    Casual("초급자", "기초 개념은 알고 있어요", "casual"),
    Regular("중급자", "어느 정도 익숙해요", "regular"),
    Expert("고급자", "심화 내용도 다뤄요", "expert"),
}

internal val PopularCertificates = listOf(
    "정보처리기사",
    "컴퓨터활용능력 1급",
    "컴퓨터활용능력 2급",
    "한국사능력검정시험",
    "영어회화전문가",
    "사회조사분석사",
    "빅데이터분석기사",
    "네트워크관리사",
    "SQLD",
)

internal val PopularShortCertificates = listOf(
    "정보처리기사",
    "컴퓨터활용능력",
    "한국사능력검정",
    "SQLD",
)

internal const val SubjectCreateTotalSteps = 3
