package com.f1.quiket.feature.home.data.mapper

import com.f1.quiket.feature.home.data.remote.QuestionAnswerResponse
import com.f1.quiket.feature.home.data.remote.QuestionOptionResponse
import com.f1.quiket.feature.home.data.remote.QuestionResponse
import com.f1.quiket.feature.home.data.remote.QuizAnswerSubmitItemRequest
import com.f1.quiket.feature.home.data.remote.QuizPlaySessionDataResponse
import com.f1.quiket.feature.home.data.remote.QuizPlayStartRequest
import com.f1.quiket.feature.home.data.remote.QuizRetryRequest
import com.f1.quiket.feature.home.data.remote.QuizResultDataResponse
import com.f1.quiket.feature.home.data.remote.QuizResultSubmitRequest
import com.f1.quiket.feature.home.data.remote.QuizReviewItemResponse
import com.f1.quiket.feature.home.data.remote.QuizSessionDataResponse
import com.f1.quiket.feature.home.data.remote.ResultPartSummaryResponse
import com.f1.quiket.feature.home.data.remote.RetryAvailableResponse
import com.f1.quiket.feature.home.data.remote.RewardSummaryResponse
import com.f1.quiket.feature.home.domain.model.PartSummary
import com.f1.quiket.feature.home.domain.model.Question
import com.f1.quiket.feature.home.domain.model.QuestionAnswer
import com.f1.quiket.feature.home.domain.model.QuestionOption
import com.f1.quiket.feature.home.domain.model.QuizDifficulty
import com.f1.quiket.feature.home.domain.model.QuizGenerationStatus
import com.f1.quiket.feature.home.domain.model.QuizAnswerSubmitItem
import com.f1.quiket.feature.home.domain.model.QuizPlayMode
import com.f1.quiket.feature.home.domain.model.QuizPlaySession
import com.f1.quiket.feature.home.domain.model.QuizPlaySessionStatus
import com.f1.quiket.feature.home.domain.model.QuizPlayStart
import com.f1.quiket.feature.home.domain.model.QuizPlayType
import com.f1.quiket.feature.home.domain.model.QuizRetry
import com.f1.quiket.feature.home.domain.model.QuizResult
import com.f1.quiket.feature.home.domain.model.QuizResultSubmit
import com.f1.quiket.feature.home.domain.model.QuizReviewItem
import com.f1.quiket.feature.home.domain.model.RetryAvailable
import com.f1.quiket.feature.home.domain.model.RewardSummary
import com.f1.quiket.feature.home.domain.model.QuizSession
import com.f1.quiket.feature.home.domain.model.ServerQuizType

fun QuizSessionDataResponse.toDomain(): QuizSession = QuizSession(
    id = id,
    subjectId = subjectId,
    subjectName = subjectName,
    quizType = quizType.toServerQuizType(),
    choiceCount = choiceCount,
    questionCount = questionCount,
    playMode = playMode.toQuizPlayMode(),
    timerEnabled = timerEnabled,
    timerScope = timerScope,
    timerSeconds = timerSeconds,
    difficulty = difficulty.toQuizDifficulty(),
    status = status.toQuizGenerationStatus(),
    questions = questions.map { question -> question.toDomain() },
)

fun QuestionResponse.toDomain(): Question = Question(
    id = id,
    subjectId = subjectId,
    chapterId = chapterId,
    partId = partId,
    partName = partName,
    questionType = questionType.toServerQuizType(),
    difficulty = difficulty.toQuizDifficulty(),
    summary = summary,
    body = body,
    correctExplanation = correctExplanation,
    incorrectExplanation = incorrectExplanation,
    displayOrder = displayOrder,
    options = options.map { option -> option.toDomain() },
    answer = answer.toDomain(),
)

fun QuestionOptionResponse.toDomain(): QuestionOption = QuestionOption(
    id = id,
    optionNumber = optionNumber,
    content = content,
)

fun QuestionAnswerResponse.toDomain(): QuestionAnswer = QuestionAnswer(
    answerValue = answerValue,
)

fun QuizPlayStart.toRequest(): QuizPlayStartRequest = QuizPlayStartRequest(
    clientSessionId = clientSessionId,
    playType = playType.wireValue,
    parentPlaySessionId = parentPlaySessionId,
    questionShuffled = questionShuffled,
    optionShuffled = optionShuffled,
    shuffleSeed = shuffleSeed,
)

fun QuizRetry.toRequest(): QuizRetryRequest = QuizRetryRequest(
    clientSessionId = clientSessionId,
    questionShuffled = questionShuffled,
    optionShuffled = optionShuffled,
    shuffleSeed = shuffleSeed,
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

fun QuizPlaySessionDataResponse.toDomain(): QuizPlaySession = QuizPlaySession(
    playSessionId = playSessionId,
    clientSessionId = clientSessionId,
    quizSessionId = quizSessionId,
    playType = playType.toQuizPlayType(),
    status = status.toQuizPlaySessionStatus(),
    quizSession = quizSession?.toDomain(),
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

fun ResultPartSummaryResponse.toDomain(): PartSummary = PartSummary(
    id = id,
    chapterId = chapterId,
    name = name,
    partNumber = partNumber,
    contentPreview = contentPreview,
)

internal fun String.toServerQuizType(): ServerQuizType = when (this) {
    ServerQuizType.MultipleChoice.wireValue -> ServerQuizType.MultipleChoice
    ServerQuizType.Ox.wireValue -> ServerQuizType.Ox
    else -> ServerQuizType.MultipleChoice
}

internal fun String.toQuizPlayMode(): QuizPlayMode = when (this) {
    QuizPlayMode.AllAtOnce.wireValue -> QuizPlayMode.AllAtOnce
    QuizPlayMode.OneByOne.wireValue -> QuizPlayMode.OneByOne
    else -> QuizPlayMode.AllAtOnce
}

internal fun String.toQuizDifficulty(): QuizDifficulty = when (this) {
    QuizDifficulty.Easy.wireValue -> QuizDifficulty.Easy
    QuizDifficulty.Medium.wireValue -> QuizDifficulty.Medium
    QuizDifficulty.Hard.wireValue -> QuizDifficulty.Hard
    else -> QuizDifficulty.Medium
}

internal fun String.toQuizGenerationStatus(): QuizGenerationStatus =
    QuizGenerationStatus.entries.firstOrNull { status -> status.wireValue == this }
        ?: QuizGenerationStatus.Unknown

internal fun String.toQuizPlayType(): QuizPlayType =
    QuizPlayType.entries.firstOrNull { type -> type.wireValue == this }
        ?: QuizPlayType.Unknown

private fun String.toQuizPlaySessionStatus(): QuizPlaySessionStatus =
    QuizPlaySessionStatus.entries.firstOrNull { status -> status.wireValue == this }
        ?: QuizPlaySessionStatus.Unknown
