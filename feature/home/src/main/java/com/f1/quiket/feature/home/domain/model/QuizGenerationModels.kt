package com.f1.quiket.feature.home.domain.model

data class QuizScope(
    val subjectId: String,
    val subjectName: String,
    val chapters: List<QuizScopeChapter>,
)

data class QuizScopeChapter(
    val id: String,
    val subjectId: String,
    val name: String,
    val displayOrder: Int,
    val parts: List<QuizScopePart>,
)

data class QuizScopePart(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String?,
)

data class QuizCreate(
    val subjectId: String,
    val partIds: List<String>,
    val quizType: ServerQuizType,
    val choiceCount: Int?,
    val questionCount: Int,
    val playMode: QuizPlayMode = QuizPlayMode.AllAtOnce,
    val timerEnabled: Boolean = false,
    val timerScope: QuizTimerScope? = null,
    val timerSeconds: Int? = null,
    val difficulty: QuizDifficulty,
)

data class QuizGenerationAccepted(
    val quizSessionId: String,
    val jobId: String,
    val status: QuizGenerationStatus,
    val estimatedSeconds: Int?,
)

data class QuizGenerationProgress(
    val quizSessionId: String,
    val jobId: String,
    val status: QuizGenerationStatus,
    val estimatedSeconds: Int?,
    val progressPct: Int?,
    val generatedCount: Int?,
    val failReason: String?,
)

enum class ServerQuizType(
    val wireValue: String,
) {
    MultipleChoice("multiple_choice"),
    Ox("ox"),
}

enum class QuizPlayMode(
    val wireValue: String,
) {
    AllAtOnce("all_at_once"),
    OneByOne("one_by_one"),
}

enum class QuizTimerScope(
    val wireValue: String,
) {
    PerQuestion("per_question"),
    Total("total"),
}

enum class QuizDifficulty(
    val wireValue: String,
) {
    Easy("easy"),
    Medium("medium"),
    Hard("hard"),
}

enum class QuizGenerationStatus(
    val wireValue: String,
) {
    Pending("pending"),
    InProgress("in_progress"),
    Completed("completed"),
    Failed("failed"),
    Unknown("unknown"),
}
