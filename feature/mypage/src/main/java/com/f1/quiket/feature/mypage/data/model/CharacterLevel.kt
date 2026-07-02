package com.f1.quiket.feature.mypage.data.model

/**
 * 큐링이 레벨 시스템 (총 10레벨, 만렙)
 *
 * 누적 XP를 기준으로 레벨업.
 *
 * Lv.1  첫걸음 다람쥐  : 0 ~ 99 XP
 * Lv.2  결심한 다람쥐   : 100 ~ 349 XP
 * Lv.3  펜굴리는 다람쥐 : 350 ~ 849 XP
 * Lv.4  노력형 다람쥐   : 850 ~ 1,749 XP
 * Lv.5  열공 다람쥐     : 1,750 ~ 3,249 XP
 * Lv.6  똑똑한 다람쥐   : 3,250 ~ 5,749 XP
 * Lv.7  박식한 다람쥐   : 5,750 ~ 9,749 XP
 * Lv.8  통달한 다람쥐   : 9,750 ~ 16,249 XP
 * Lv.9  현자 다람쥐     : 16,250 ~ 26,249 XP
 * Lv.10 레전드 다람쥐   : 26,250 XP 이상 (만렙)
 */
enum class CharacterLevel(
    val level: Int,
    val displayName: String,
    val requiredXp: Int,
    val nextRequiredXp: Int?,
) {
    FIRST_STEP(
        level = 1,
        displayName = "첫걸음 다람쥐",
        requiredXp = 0,
        nextRequiredXp = 100,
    ),
    DETERMINED(
        level = 2,
        displayName = "결심한 다람쥐",
        requiredXp = 100,
        nextRequiredXp = 350,
    ),
    PEN_ROLLING(
        level = 3,
        displayName = "펜굴리는 다람쥐",
        requiredXp = 350,
        nextRequiredXp = 850,
    ),
    HARDWORKING(
        level = 4,
        displayName = "노력형 다람쥐",
        requiredXp = 850,
        nextRequiredXp = 1750,
    ),
    STUDIOUS(
        level = 5,
        displayName = "열공 다람쥐",
        requiredXp = 1750,
        nextRequiredXp = 3250,
    ),
    SMART(
        level = 6,
        displayName = "똑똑한 다람쥐",
        requiredXp = 3250,
        nextRequiredXp = 5750,
    ),
    KNOWLEDGEABLE(
        level = 7,
        displayName = "박식한 다람쥐",
        requiredXp = 5750,
        nextRequiredXp = 9750,
    ),
    MASTERED(
        level = 8,
        displayName = "통달한 다람쥐",
        requiredXp = 9750,
        nextRequiredXp = 16250,
    ),
    SAGE(
        level = 9,
        displayName = "현자 다람쥐",
        requiredXp = 16250,
        nextRequiredXp = 26250,
    ),
    LEGEND(
        level = 10,
        displayName = "레전드 다람쥐",
        requiredXp = 26250,
        nextRequiredXp = null,
    );

    /** 현재 레벨 내 진행도 (0.0 ~ 1.0) */
    fun progressIn(totalXp: Int): Float {
        val current = (totalXp - requiredXp).coerceAtLeast(0)
        val required = nextRequiredXp?.let { it - requiredXp } ?: 1
        return if (nextRequiredXp == null) 1f
        else (current.toFloat() / required).coerceIn(0f, 1f)
    }

    /** 다음 레벨까지 남은 XP */
    fun remainingUntilNext(totalXp: Int): Int? {
        return nextRequiredXp?.let { (it - totalXp).coerceAtLeast(0) }
    }

    companion object {
        fun from(totalXp: Int): CharacterLevel =
            entries.lastOrNull { totalXp >= it.requiredXp } ?: FIRST_STEP
    }
}