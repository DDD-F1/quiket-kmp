package com.f1.quiket.feature.history.data.mapper

import com.f1.quiket.feature.history.data.remote.PartSummaryResponse
import com.f1.quiket.feature.history.data.remote.QuestionOptionResponse
import com.f1.quiket.feature.history.data.remote.QuizAnswerSubmitItemRequest
import com.f1.quiket.feature.history.data.remote.QuizPlaySessionDataResponse
import com.f1.quiket.feature.history.data.remote.RetryQuestionAnswerResponse
import com.f1.quiket.feature.history.data.remote.RetryQuestionResponse
import com.f1.quiket.feature.history.data.remote.RetryQuizSessionResponse
import com.f1.quiket.feature.history.data.remote.QuizResultDataResponse
import com.f1.quiket.feature.history.data.remote.QuizResultSubmitRequest
import com.f1.quiket.feature.history.data.remote.QuizRetryRequest
import com.f1.quiket.feature.history.data.remote.QuizReviewItemResponse
import com.f1.quiket.feature.history.data.remote.RecentActivityPageResponse
import com.f1.quiket.feature.history.data.remote.RecentActivityResponse
import com.f1.quiket.feature.history.data.remote.RetryAvailableResponse
import com.f1.quiket.feature.history.data.remote.RewardSummaryResponse
import com.f1.quiket.feature.history.domain.model.PartSummary
import com.f1.quiket.feature.history.domain.model.QuestionOption
import com.f1.quiket.feature.history.domain.model.QuizAnswerSubmitItem
import com.f1.quiket.feature.history.domain.model.QuizPlaySession
import com.f1.quiket.feature.history.domain.model.QuizPlaySessionStatus
import com.f1.quiket.feature.history.domain.model.QuizPlayType
import com.f1.quiket.feature.history.domain.model.QuizResult
import com.f1.quiket.feature.history.domain.model.QuizResultSubmit
import com.f1.quiket.feature.history.domain.model.QuizRetry
import com.f1.quiket.feature.history.domain.model.QuizReviewItem
import com.f1.quiket.feature.history.domain.model.RetryQuestion
import com.f1.quiket.feature.history.domain.model.RetryQuestionAnswer
import com.f1.quiket.feature.history.domain.model.RetryQuizSession
import com.f1.quiket.feature.history.domain.model.RetryAvailable
import com.f1.quiket.feature.history.domain.model.RewardSummary
import com.f1.quiket.feature.history.domain.model.RecentActivity
import com.f1.quiket.feature.history.domain.model.RecentActivityPage
import com.f1.quiket.feature.history.domain.model.RecentActivityType

fun RecentActivityPageResponse.toDomain(): RecentActivityPage = RecentActivityPage(
    activities = content.map { activity -> activity.toDomain() },
    page = page,
    size = size,
    totalElements = totalElements,
    totalPages = totalPages,
    hasNext = hasNext,
)

fun RecentActivityResponse.toDomain(): RecentActivity = RecentActivity(
    activityId = activityId,
    activityType = activityType.toRecentActivityType(),
    quizSessionId = quizSessionId,
    playSessionId = playSessionId,
    resultId = resultId,
    title = title,
    subjectId = subjectId,
    subjectName = subjectName,
    status = status,
    progressPct = progressPct,
    scoreText = scoreText,
    createdAt = createdAt,
)

fun QuizResultSubmit.toRequest(): QuizResultSubmitRequest = QuizResultSubmitRequest(
    clientSessionId = clientSessionId,
    quizSessionId = quizSessionId,
    playType = playType.wireValue,
    parentPlaySessionId = parentPlaySessionId,
    elapsedMs = elapsedMs,
    questionShuffled = questionShuffled,
    optionShuffled = optionShuffled,
    shuffleSeed = shuffleSeed,
    answers = answers.map { answer -> answer.toRequest() },
)

fun QuizAnswerSubmitItem.toRequest(): QuizAnswerSubmitItemRequest = QuizAnswerSubmitItemRequest(
    questionId = questionId,
    selectedOptionId = selectedOptionId,
    selectedValue = selectedValue,
    correctClient = correctClient,
    skipped = skipped,
    answerElapsedMs = answerElapsedMs,
    marked = marked,
)

fun QuizRetry.toRequest(): QuizRetryRequest = QuizRetryRequest(
    clientSessionId = clientSessionId,
    questionShuffled = questionShuffled,
    optionShuffled = optionShuffled,
    shuffleSeed = shuffleSeed,
)

fun QuizResultDataResponse.toDomain(): QuizResult = QuizResult(
    playSessionId = playSessionId,
    resultId = resultId,
    quizSessionId = quizSessionId,
    subjectId = subjectId,
    subjectName = subjectName,
    totalCount = totalCount,
    correctCount = correctCount,
    wrongCount = wrongCount,
    skipCount = skipCount,
    accuracyPct = accuracyPct,
    elapsedMs = elapsedMs,
    scoreMatched = scoreMatched,
    abuseFlagged = abuseFlagged,
    rewards = rewards.toDomain(),
    reviewItems = reviewItems.map { item -> item.toDomain() },
    retryAvailable = retryAvailable?.toDomain(),
    createdAt = createdAt,
)

fun RewardSummaryResponse.toDomain(): RewardSummary = RewardSummary(
    dotoriEarned = dotoriEarned,
    xpEarned = xpEarned,
    leveledUp = leveledUp,
    newLevel = newLevel,
    currentDotoriBalance = currentDotoriBalance,
    currentXpTotal = currentXpTotal,
)

fun RetryAvailableResponse.toDomain(): RetryAvailable = RetryAvailable(
    retryAll = retryAll,
    retryWrong = retryWrong,
    wrongCount = wrongCount,
)

fun QuizReviewItemResponse.toDomain(): QuizReviewItem = QuizReviewItem(
    questionId = questionId,
    displayOrder = displayOrder,
    summary = summary,
    body = body,
    options = options.map { option -> option.toDomain() },
    selectedOptionId = selectedOptionId,
    selectedValue = selectedValue,
    answerValue = answerValue,
    correctServer = correctServer,
    skipped = skipped,
    correctExplanation = correctExplanation,
    incorrectExplanation = incorrectExplanation,
    sourcePart = sourcePart?.toDomain(),
)

fun QuestionOptionResponse.toDomain(): QuestionOption = QuestionOption(
    id = id,
    optionNumber = optionNumber,
    content = content,
)

fun PartSummaryResponse.toDomain(): PartSummary = PartSummary(
    id = id,
    chapterId = chapterId,
    name = name,
    partNumber = partNumber,
    contentPreview = contentPreview,
)

fun QuizPlaySessionDataResponse.toDomain(): QuizPlaySession = QuizPlaySession(
    playSessionId = playSessionId,
    clientSessionId = clientSessionId,
    quizSessionId = quizSessionId,
    playType = playType.toQuizPlayType(),
    status = status.toQuizPlaySessionStatus(),
    quizSession = quizSession?.toDomain(),
)

fun RetryQuizSessionResponse.toDomain(): RetryQuizSession = RetryQuizSession(
    id = id,
    subjectId = subjectId,
    subjectName = subjectName,
    quizType = quizType,
    choiceCount = choiceCount,
    questionCount = questionCount,
    playMode = playMode,
    timerEnabled = timerEnabled,
    timerScope = timerScope,
    timerSeconds = timerSeconds,
    difficulty = difficulty,
    status = status,
    questions = questions.map { question -> question.toDomain() },
)

fun RetryQuestionResponse.toDomain(): RetryQuestion = RetryQuestion(
    id = id,
    subjectId = subjectId,
    chapterId = chapterId,
    partId = partId,
    partName = partName,
    questionType = questionType,
    difficulty = difficulty,
    summary = summary,
    body = body,
    correctExplanation = correctExplanation,
    incorrectExplanation = incorrectExplanation,
    displayOrder = displayOrder,
    options = options.map { option -> option.toDomain() },
    answer = answer.toDomain(),
)

fun RetryQuestionAnswerResponse.toDomain(): RetryQuestionAnswer = RetryQuestionAnswer(
    answerValue = answerValue,
)

private fun String.toQuizPlayType(): QuizPlayType =
    QuizPlayType.entries.firstOrNull { type -> type.wireValue == this }
        ?: QuizPlayType.Unknown

private fun String.toQuizPlaySessionStatus(): QuizPlaySessionStatus =
    QuizPlaySessionStatus.entries.firstOrNull { status -> status.wireValue == this }
        ?: QuizPlaySessionStatus.Unknown

private fun String.toRecentActivityType(): RecentActivityType =
    RecentActivityType.entries.firstOrNull { type -> type.wireValue == this }
        ?: RecentActivityType.Unknown
