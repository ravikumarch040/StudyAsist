package com.studyasist.ui.components

object DailyQuoteProvider {

    private val quotes = listOf(
        "The expert in anything was once a beginner.",
        "Success is the sum of small efforts, repeated day in and day out.",
        "Don't watch the clock; do what it does. Keep going.",
        "The secret of getting ahead is getting started.",
        "It does not matter how slowly you go, as long as you do not stop.",
        "Education is the most powerful weapon which you can use to change the world.",
        "The beautiful thing about learning is that nobody can take it away from you.",
        "Push yourself, because no one else is going to do it for you.",
        "Small daily improvements are the key to staggering long-term results.",
        "Your limitation — it's only your imagination.",
        "Wake up with determination. Go to bed with satisfaction.",
        "Dream it. Wish it. Do it.",
        "The harder you work for something, the greater you'll feel when you achieve it.",
        "Stay focused and never give up.",
        "Believe in yourself and all that you are.",
        "Study hard, for the well is deep, and our brains are shallow.",
        "The only way to do great work is to love what you do.",
        "Don't let what you cannot do interfere with what you can do.",
        "Learning is a treasure that will follow its owner everywhere.",
        "The future belongs to those who believe in the beauty of their dreams.",
        "Success is not the key to happiness. Happiness is the key to success.",
        "Live as if you were to die tomorrow. Learn as if you were to live forever.",
        "A mind that is stretched by new experience can never go back to its old dimensions.",
        "Education is not preparation for life; education is life itself.",
        "An investment in knowledge pays the best interest.",
        "The roots of education are bitter, but the fruit is sweet.",
        "There is no substitute for hard work.",
        "Genius is 1% inspiration and 99% perspiration.",
        "Knowledge is power.",
        "The more that you read, the more things you will know.",
        "In learning you will teach, and in teaching you will learn.",
        "Every accomplishment starts with the decision to try.",
        "Strive for progress, not perfection.",
        "The only person you are destined to become is the person you decide to be.",
        "What we learn with pleasure we never forget.",
        "The mind is not a vessel to be filled, but a fire to be kindled.",
        "You don't have to be great to start, but you have to start to be great.",
        "Success usually comes to those who are too busy to be looking for it.",
        "Hard work beats talent when talent doesn't work hard.",
        "Study while others are sleeping.",
        "The capacity to learn is a gift; the ability to learn is a skill; the willingness to learn is a choice.",
        "Start where you are. Use what you have. Do what you can.",
        "You are never too old to set another goal or to dream a new dream.",
        "Discipline is the bridge between goals and accomplishment.",
        "Focus on being productive instead of busy.",
        "The only limit to our realization of tomorrow is our doubts of today.",
        "Your future is created by what you do today, not tomorrow.",
        "Excellence is not a skill. It is an attitude.",
        "Don't wish it were easier. Wish you were better.",
        "Perseverance is not a long race; it is many short races one after the other."
    )

    fun getQuoteForToday(): String {
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        return quotes[dayOfYear % quotes.size]
    }

    fun getRandomQuote(): String = quotes.random()
}
