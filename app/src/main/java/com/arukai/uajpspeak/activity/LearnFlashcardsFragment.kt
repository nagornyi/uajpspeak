package com.arukai.uajpspeak.activity

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.arukai.uajpspeak.R
import com.arukai.uajpspeak.model.Abecadlo
import com.arukai.uajpspeak.model.Flashcard
import com.arukai.uajpspeak.util.FlashcardManager
import com.arukai.uajpspeak.util.LocaleHelper

/**
 * Fragment for learning flashcards with multiple choice questions.
 * Uses spaced repetition algorithm (Anki-like).
 */
class LearnFlashcardsFragment : Fragment() {

    private lateinit var flashcardManager: FlashcardManager
    private lateinit var abecadlo: Abecadlo
    private var sessionCards = listOf<Flashcard>()
    private var currentCardIndex = 0
    private var correctAnswersInSession = 0

    // Persisted answer state – survives onDestroyView/onCreateView cycles (e.g. Alphabet overlay)
    private var currentCardOptions: List<String> = emptyList()
    private var isCurrentCardAnswered = false
    private var currentCardAnswerCorrect = false
    private var currentCardSelectedAnswer = ""

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var progressSection: View      // parent row; single toggle for bar+text
    private lateinit var phraseCard: CardView        // blue Ukrainian phrase card
    private lateinit var ukrainianText: TextView
    private lateinit var transliterationText: TextView
    private lateinit var questionText: TextView
    private lateinit var optionsContainer: LinearLayout
    private lateinit var feedbackCard: CardView
    private lateinit var feedbackText: TextView
    private lateinit var nextButton: Button
    private lateinit var completionCard: CardView
    private lateinit var completionStats: TextView
    private lateinit var continueButton: Button
    private lateinit var finishButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_learn_flashcards, container, false)

        flashcardManager = FlashcardManager(requireContext())
        abecadlo = Abecadlo()

        // Initialize views
        initializeViews(rootView)

        // Load session only if we don't have one yet.
        // The fragment instance survives on the back stack, so instance variables
        // (sessionCards, currentCardIndex, answer state) are still valid when
        // returning from Alphabet or other overlays.
        if (sessionCards.isEmpty()) {
            loadLearningSession()
        }

        // Restore or show current state
        when {
            sessionCards.isEmpty() -> showNoCardsMessage()
            currentCardIndex >= sessionCards.size -> showSessionComplete()
            else -> showCard(currentCardIndex)   // handles answered-state restoration internally
        }

        // Set up menu to hide unwanted items
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Menu already inflated by MainActivity
            }

            override fun onPrepareMenu(menu: Menu) {
                // Hide all menu items except Alphabet
                menu.findItem(R.id.action_search)?.isVisible = false
                menu.findItem(R.id.action_favorite)?.isVisible = false
                menu.findItem(R.id.action_gender_lang)?.isVisible = false
                menu.findItem(R.id.action_language)?.isVisible = false
                menu.findItem(R.id.action_theme)?.isVisible = false
                menu.findItem(R.id.action_about)?.isVisible = false
                // Keep only Alphabet visible
                menu.findItem(R.id.action_alphabet)?.isVisible = true
            }

            override fun onMenuItemSelected(item: android.view.MenuItem): Boolean {
                return false // Let MainActivity handle menu clicks
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        // Ensure toolbar is properly configured when returning from About/Alphabet
        val mainActivity = activity as? MainActivity
        mainActivity?.setActionBarTitle(getString(R.string.title_learn_flashcards))
        mainActivity?.setDrawerLocked(false)
        mainActivity?.enableBackButton(true)
    }

    private fun initializeViews(rootView: View) {
        progressSection = rootView.findViewById(R.id.progressSection)
        progressBar = rootView.findViewById(R.id.sessionProgressBar)
        progressText = rootView.findViewById(R.id.sessionProgressText)
        phraseCard = rootView.findViewById(R.id.phraseCard)
        ukrainianText = rootView.findViewById(R.id.ukrainianPhrase)
        transliterationText = rootView.findViewById(R.id.transliteration)
        questionText = rootView.findViewById(R.id.questionText)
        optionsContainer = rootView.findViewById(R.id.optionsContainer)
        feedbackCard = rootView.findViewById(R.id.feedbackCard)
        feedbackText = rootView.findViewById(R.id.feedbackText)
        nextButton = rootView.findViewById(R.id.nextCardButton)
        completionCard = rootView.findViewById(R.id.completionCard)
        completionStats = rootView.findViewById(R.id.completionStats)
        continueButton = rootView.findViewById(R.id.continueButton)
        finishButton = rootView.findViewById(R.id.finishButton)

        nextButton.setOnClickListener {
            showNextCard()
        }

        continueButton.setOnClickListener {
            loadLearningSession()
            if (sessionCards.isNotEmpty()) {
                completionCard.visibility = View.GONE
                showCard(0)
            } else {
                showNoCardsMessage()
            }
        }

        finishButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadLearningSession() {
        val categoryIds = arguments?.getIntArray("categoryIds")?.toList() ?: return

        // Reset per-card answer state for the new session
        isCurrentCardAnswered = false
        currentCardAnswerCorrect = false
        currentCardSelectedAnswer = ""
        currentCardOptions = emptyList()

        // Build phrase map for initialization
        val phrasesMap = mutableMapOf<Int, Array<String>>()
        categoryIds.forEach { categoryId ->
            phrasesMap[categoryId] = resources.getStringArray(categoryId)
        }

        // Build learning session
        sessionCards = flashcardManager.buildLearningSession(
            requireContext(),
            categoryIds,
            phrasesMap,
            maxCards = 20,
            maxNewCards = 10
        )

        currentCardIndex = 0
        correctAnswersInSession = 0
    }

    private fun showCard(index: Int) {
        if (index >= sessionCards.size) {
            showSessionComplete()
            return
        }

        val card = sessionCards[index]

        // Make sure all UI elements are visible
        progressSection.visibility = View.VISIBLE
        phraseCard.visibility = View.VISIBLE
        questionText.visibility = View.VISIBLE
        optionsContainer.visibility = View.VISIBLE
        completionCard.visibility = View.GONE

        // Update progress – use index+1 so bar and "x / y" counter always match
        progressBar.max = sessionCards.size
        progressBar.progress = index + 1
        progressText.text = "${index + 1} / ${sessionCards.size}"

        // Show Ukrainian phrase
        val ukrainianClean = card.ukrainian.replace("*", "")
        ukrainianText.text = ukrainianClean

        // Show transliteration if available (simplified)
        transliterationText.text = transliterateUkrainian(ukrainianClean)

        if (isCurrentCardAnswered && currentCardOptions.isNotEmpty()) {
            // ── Restore answered state (returning from Alphabet overlay) ──────────
            generateOptions(card, existingOptions = currentCardOptions)

            feedbackCard.visibility = View.VISIBLE
            feedbackText.text = if (currentCardAnswerCorrect) {
                getString(R.string.feedback_correct)
            } else {
                getString(R.string.feedback_wrong, card.translation)
            }
            feedbackCard.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (currentCardAnswerCorrect) R.color.correct_answer else R.color.wrong_answer
                )
            )

            // Re-apply button highlight/disable
            for (i in 0 until optionsContainer.childCount) {
                val btn = optionsContainer.getChildAt(i) as? Button ?: continue
                btn.isEnabled = false
                when {
                    btn.text == currentCardSelectedAnswer && currentCardAnswerCorrect -> {
                        btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.correct_answer))
                        btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    }
                    btn.text == currentCardSelectedAnswer && !currentCardAnswerCorrect -> {
                        btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.wrong_answer))
                        btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    }
                    btn.text == card.translation && !currentCardAnswerCorrect -> {
                        // Highlight the correct answer when the user picked wrong
                        btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.correct_answer))
                        btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    }
                }
            }
        } else {
            // ── Fresh card ────────────────────────────────────────────────────────
            feedbackCard.visibility = View.GONE
            generateOptions(card)
        }
    }

    private fun generateOptions(correctCard: Flashcard, existingOptions: List<String>? = null) {
        optionsContainer.removeAllViews()

        val options: List<String>
        if (existingOptions != null) {
            // Restore previously generated order (returning from overlay)
            options = existingOptions
        } else {
            // Draw wrong answers from ALL stored flashcards so that small sessions
            // (few cards) still produce enough distinct choices.
            val allFlashcards = flashcardManager.getAllFlashcards()
            val wrongAnswers = allFlashcards
                .map { it.translation }
                .filter { it != correctCard.translation } // exclude correct answer
                .distinct()
                .shuffled()
                .take(3)

            // Combine with correct answer and ensure no duplicates
            options = (wrongAnswers + correctCard.translation).distinct().shuffled()
        }

        // Always save the current option order so it can be restored after an overlay
        currentCardOptions = options

        // Create option buttons
        options.forEach { option ->
            val button = createOptionButton(option, option == correctCard.translation, correctCard)
            optionsContainer.addView(button)
        }
    }

    private fun createOptionButton(text: String, isCorrect: Boolean, card: Flashcard): Button {
        val button = Button(requireContext())
        button.text = text
        button.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 6, 0, 6)
        }

        // Style the button
        button.setBackgroundResource(R.drawable.flashcard_option_background)
        button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        button.setPadding(24, 20, 24, 20)
        button.textSize = 15f

        button.setOnClickListener {
            handleAnswer(isCorrect, card, button)
        }

        return button
    }

    private fun handleAnswer(isCorrect: Boolean, card: Flashcard, selectedButton: Button) {
        // Save answer state so it can be restored if the user opens Alphabet mid-card
        isCurrentCardAnswered = true
        currentCardAnswerCorrect = isCorrect
        currentCardSelectedAnswer = selectedButton.text.toString()

        // Disable all option buttons
        for (i in 0 until optionsContainer.childCount) {
            val child = optionsContainer.getChildAt(i)
            if (child is Button) {
                child.isEnabled = false
            }
        }

        // Update card based on answer quality
        val quality = if (isCorrect) {
            // If answered correctly, assign quality 4-5 based on speed/history
            if (card.repetitions > 2) 5 else 4
        } else {
            // If wrong, quality 1-2 (will reset progress)
            2
        }

        card.updateAfterReview(quality)
        flashcardManager.updateFlashcard(card)

        // Update session stats
        if (isCorrect) {
            correctAnswersInSession++
        }

        // Show feedback
        showFeedback(isCorrect, card, selectedButton)
    }

    private fun showFeedback(isCorrect: Boolean, card: Flashcard, selectedButton: Button) {
        // Highlight selected button
        if (isCorrect) {
            selectedButton.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.correct_answer)
            )
            selectedButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        } else {
            selectedButton.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.wrong_answer)
            )
            selectedButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))

            // Highlight correct answer
            for (i in 0 until optionsContainer.childCount) {
                val child = optionsContainer.getChildAt(i) as? Button
                if (child?.text == card.translation) {
                    child.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.correct_answer)
                    )
                    child.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                }
            }
        }

        // Show feedback card
        feedbackCard.visibility = View.VISIBLE
        feedbackText.text = if (isCorrect) {
            getString(R.string.feedback_correct)
        } else {
            getString(R.string.feedback_wrong, card.translation)
        }

        feedbackCard.setCardBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                if (isCorrect) R.color.correct_answer else R.color.wrong_answer
            )
        )

        // Animate feedback
        ObjectAnimator.ofFloat(feedbackCard, "alpha", 0f, 1f).apply {
            duration = 300
            start()
        }
    }

    private fun showNextCard() {
        // Reset answer state for the next card
        isCurrentCardAnswered = false
        currentCardAnswerCorrect = false
        currentCardSelectedAnswer = ""
        currentCardOptions = emptyList()

        currentCardIndex++

        if (currentCardIndex < sessionCards.size) {
            showCard(currentCardIndex)
        } else {
            showSessionComplete()
        }
    }

    private fun showSessionComplete() {
        // Hide flashcard UI
        progressSection.visibility = View.GONE
        phraseCard.visibility = View.GONE
        questionText.visibility = View.GONE
        optionsContainer.visibility = View.GONE
        feedbackCard.visibility = View.GONE

        // Show completion card
        completionCard.visibility = View.VISIBLE

        val accuracy = if (sessionCards.isNotEmpty()) {
            (correctAnswersInSession * 100) / sessionCards.size
        } else {
            0
        }

        completionStats.text = getString(
            R.string.session_complete_stats,
            correctAnswersInSession,
            sessionCards.size,
            accuracy
        )

        // Check if more cards are available for the selected categories specifically
        val categoryIds = arguments?.getIntArray("categoryIds")?.toList() ?: emptyList()
        val hasMoreCards = flashcardManager.getDueFlashcards(categoryIds).isNotEmpty() ||
                           flashcardManager.getNewFlashcards(categoryIds).isNotEmpty()

        continueButton.visibility = if (hasMoreCards) View.VISIBLE else View.GONE
    }

    private fun showNoCardsMessage() {
        // Hide all flashcard UI (same as session complete)
        progressSection.visibility = View.GONE
        phraseCard.visibility = View.GONE
        questionText.visibility = View.GONE
        optionsContainer.visibility = View.GONE
        feedbackCard.visibility = View.GONE

        // Show completion card with "no cards" message
        completionCard.visibility = View.VISIBLE
        completionStats.text = getString(R.string.no_cards_message)
        continueButton.visibility = View.GONE
    }

    /**
     * Transliterate Ukrainian text using the existing Abecadlo system.
     * For Japanese users, shows katakana. For others, shows romanization.
     */
    private fun transliterateUkrainian(text: String): String {
        val currentLang = LocaleHelper.getSavedLanguage(requireContext())

        return if (currentLang == "ja") {
            // Use Japanese katakana conversion for Japanese users
            abecadlo.convert(text)
        } else {
            // Use romanization for all other languages
            abecadlo.romanize(text)
        }
    }
}
