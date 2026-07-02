package com.f1.quiket.feature.history.domain.model

data class RecentActivityPage(
    val activities: List<RecentActivity>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class RecentActivity(
    val activityId: String,
    val activityType: RecentActivityType,
    val quizSessionId: String?,
    val playSessionId: String?,
    val resultId: String?,
    val title: String,
    val subjectId: String,
    val subjectName: String,
    val status: String?,
    val progressPct: Int?,
    val scoreText: String?,
    val createdAt: String,
)

enum class RecentActivityType(
    val wireValue: String,
) {
    QuizGenerating("quiz_generating"),
    QuizReady("quiz_ready"),
    QuizInProgress("quiz_in_progress"),
    QuizCompleted("quiz_completed"),
    LectureUploaded("lecture_uploaded"),
    Unknown("unknown"),
}

data class QuizResultSubmit(
    val clientSessionId: String,
    val quizSessionId: String,
    val playType: QuizPlayType,
    val parentPlaySessionId: String? = null,
    val elapsedMs: Int,
    val questionShuffled: Boolean = false,
    val optionShuffled: Boolean = true,
    val shuffleSeed: String? = null,
    val answers: List<QuizAnswerSubmitItem>,
)

data class QuizAnswerSubmitItem(
    val questionId: String,
    val selectedOptionId: String? = null,
    val selectedValue: String? = null,
    val correctClient: Boolean? = null,
    val skipped: Boolean,
    val answerElapsedMs: Int? = null,
    val marked: Boolean = false,
)

data class QuizRetry(
    val clientSessionId: String,
    val questionShuffled: Boolean = false,
    val optionShuffled: Boolean = true,
    val shuffleSeed: String? = null,
)

data class QuizResult(
    val playSessionId: String,
    val resultId: String? = null,
    val quizSessionId: String,
    val subjectId: String,
    val subjectName: String?,
    val totalCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val skipCount: Int,
    val accuracyPct: Int,
    val elapsedMs: Int,
    val scoreMatched: Boolean?,
    val abuseFlagged: Boolean?,
    val rewards: RewardSummary,
    val reviewItems: List<QuizReviewItem>,
    val retryAvailable: RetryAvailable?,
    val createdAt: String?,
)

data class RewardSummary(
    val dotoriEarned: Int,
    val xpEarned: Int,
    val leveledUp: Boolean,
    val newLevel: Int?,
    val currentDotoriBalance: Int?,
    val currentXpTotal: Int?,
)

data class RetryAvailable(
    val retryAll: Boolean,
    val retryWrong: Boolean,
    val wrongCount: Int?,
)

data class QuizReviewItem(
    val questionId: String,
    val displayOrder: Int,
    val summary: String?,
    val body: String,
    val options: List<QuestionOption>,
    val selectedOptionId: String?,
    val selectedValue: String?,
    val answerValue: String?,
    val correctServer: Boolean,
    val skipped: Boolean,
    val correctExplanation: String?,
    val incorrectExplanation: String?,
    val sourcePart: PartSummary?,
)

data class QuestionOption(
    val id: String,
    val optionNumber: Int,
    val content: String,
)

data class PartSummary(
    val id: String,
    val chapterId: String,
    val name: String,
    val partNumber: Int,
    val contentPreview: String?,
)

data class QuizPlaySession(
    val playSessionId: String,
    val clientSessionId: String,
    val quizSessionId: String,
    val playType: QuizPlayType,
    val status: QuizPlaySessionStatus,
    val quizSession: RetryQuizSession?,
)

data class RetryQuizSession(
    val id: String,
    val subjectId: String,
    val subjectName: String?,
    val quizType: String,
    val choiceCount: Int?,
    val questionCount: Int,
    val playMode: String,
    val timerEnabled: Boolean,
    val timerScope: String?,
    val timerSeconds: Int?,
    val difficulty: String,
    val status: String,
    val questions: List<RetryQuestion>,
)

data class RetryQuestion(
    val id: String,
    val subjectId: String?,
    val chapterId: String?,
    val partId: String?,
    val partName: String?,
    val questionType: String,
    val difficulty: String,
    val summary: String?,
    val body: String,
    val correctExplanation: String?,
    val incorrectExplanation: String?,
    val displayOrder: Int,
    val options: List<QuestionOption>,
    val answer: RetryQuestionAnswer,
)

data class RetryQuestionAnswer(
    val answerValue: String,
)

enum class QuizPlayType(
    val wireValue: String,
) {
    First("first"),
    RetryAll("retry_all"),
    RetryWrong("retry_wrong"),
    Unknown("unknown"),
}

enum class QuizPlaySessionStatus(
    val wireValue: String,
) {
    InProgress("in_progress"),
    Submitted("submitted"),
    Abandoned("abandoned"),
    Unknown("unknown"),
}
