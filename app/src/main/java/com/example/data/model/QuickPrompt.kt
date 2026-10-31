package com.example.data.model

enum class PromptCategory(val displayName: String, val iconName: String) {
    WRITING("Writing Assistance", "Edit"),
    CODING("Coding Help", "Code"),
    LEARNING("Learning & Explanations", "School"),
    CREATIVE("Creative & Brainstorming", "AutoAwesome"),
    BUSINESS("Professional & Business", "Work"),
    IMAGE("Image Generation Ideas", "Palette")
}

data class QuickPrompt(
    val id: String,
    val title: String,
    val category: PromptCategory,
    val template: String,
    val description: String,
    val tags: List<String>
)

object QuickPromptLibrary {
    val prompts: List<QuickPrompt> = listOf(
        QuickPrompt(
            id = "owner_info",
            title = "Who is Light GPT's Owner?",
            category = PromptCategory.LEARNING,
            template = "Who is the owner of Light GPT?",
            description = "Learn about Faisal Khan, the 19-year-old creator of Light GPT from Quetta kuchlak, Pakistan.",
            tags = listOf("Owner", "Creator", "Faisal Khan")
        ),
        QuickPrompt(
            id = "speed_solver",
            title = "⚡ Lightning Speed Solver",
            category = PromptCategory.LEARNING,
            template = "Solve this problem at top speed with the direct answer first, clean step-by-step logic, and friendly insights:\n\n\"[describe any math, science, riddle, or life question]\"",
            description = "Get immediate, razor-sharp answers in seconds for any challenge.",
            tags = listOf("Speed", "Answers", "Smart")
        ),
        QuickPrompt(
            id = "universal_mind",
            title = "🧠 Answer Any Question (Universal AI)",
            category = PromptCategory.LEARNING,
            template = "Act as an omniscient, friendly expert and give me a crystal-clear, fascinating breakdown of:\n\n\"[any question or curious topic in the universe]\"",
            description = "Explains any concept, scientific phenomenon, or deep question warmly.",
            tags = listOf("Universal", "Knowledge", "Friendly")
        ),
        // WRITING ASSISTANCE (8 items)
        QuickPrompt(
            id = "w1",
            title = "Polished Professional Email",
            category = PromptCategory.WRITING,
            template = "Please rewrite the following message into a polite, clear, and professional email to [recipient]:\n\n\"[draft text]\"",
            description = "Turns rough thoughts into an executive-ready email with appropriate tone.",
            tags = listOf("Email", "Professional", "Tone")
        ),
        QuickPrompt(
            id = "w2",
            title = "Deep Proofreading & Critique",
            category = PromptCategory.WRITING,
            template = "Review this text for grammar, phrasing, clarity, and pacing. Point out specific improvements and provide an edited version:\n\n\"[paste text here]\"",
            description = "Detailed editorial review catching subtle typos and awkward phrasing.",
            tags = listOf("Grammar", "Editing", "Clarity")
        ),
        QuickPrompt(
            id = "w3",
            title = "Executive Summary (TL;DR)",
            category = PromptCategory.WRITING,
            template = "Summarize the key findings, actionable takeaways, and open questions from the following text in 3-5 concise bullet points:\n\n\"[paste article or report]\"",
            description = "Extracts high-level executive insights from long documents.",
            tags = listOf("Summary", "Bullets", "Executive")
        ),
        QuickPrompt(
            id = "w4",
            title = "Tone Transformer",
            category = PromptCategory.WRITING,
            template = "Rewrite the following passage in a [persuasive / empathetic / witty / technical] tone while keeping all facts unchanged:\n\n\"[original text]\"",
            description = "Changes the emotional resonance and register of your writing.",
            tags = listOf("Tone", "Rewrite", "Style")
        ),
        QuickPrompt(
            id = "w5",
            title = "Blog Post / Article Outline",
            category = PromptCategory.WRITING,
            template = "Create a compelling blog post outline about \"[topic]\". Target audience is [target audience]. Include catching headers, key points, and suggested call-to-actions.",
            description = "Structured framework for articles, newsletters, and essays.",
            tags = listOf("Blogging", "Outline", "Content")
        ),
        QuickPrompt(
            id = "w6",
            title = "Compelling Hook & Headline Ideas",
            category = PromptCategory.WRITING,
            template = "Generate 10 high-engagement headlines and opening hook sentences for a piece about \"[topic or product]\". Mix intriguing, authoritative, and question formats.",
            description = "Grabs attention for social posts, videos, or articles.",
            tags = listOf("Headlines", "Hooks", "Marketing")
        ),
        QuickPrompt(
            id = "w7",
            title = "Counterargument & Devil's Advocate",
            category = PromptCategory.WRITING,
            template = "Act as a critical reviewer and provide the strongest 3 counterarguments or potential objections against this position:\n\n\"[argument or thesis]\"",
            description = "Stress-tests your thesis and spots blind spots before publishing.",
            tags = listOf("Critical Thinking", "Debate", "Review")
        ),

        // CODING HELP (8 items)
        QuickPrompt(
            id = "c1",
            title = "Debug & Fix Error",
            category = PromptCategory.CODING,
            template = "I am getting this error:\n`[error message or stack trace]`\n\nIn this [language/framework] code:\n```\n[code snippet]\n```\nPlease explain the root cause and provide the corrected code.",
            description = "Locates syntax, logic, or runtime issues with clear explanations.",
            tags = listOf("Debugging", "Error", "Fix")
        ),
        QuickPrompt(
            id = "c2",
            title = "Code Architecture & Refactoring",
            category = PromptCategory.CODING,
            template = "Refactor this code to follow clean architecture, improve readability, and eliminate redundancies:\n```[language]\n[paste code here]\n```\nExplain what design patterns or improvements were applied.",
            description = "Elevates code quality, modularity, and adherence to DRY/SOLID principles.",
            tags = listOf("Refactor", "Clean Code", "Design Patterns")
        ),
        QuickPrompt(
            id = "c3",
            title = "Explain Code Step-by-Step",
            category = PromptCategory.CODING,
            template = "Explain how this code works step-by-step as if I am an intermediate developer. Break down complex algorithms or API calls:\n```[language]\n[paste code]\n```",
            description = "Deconstructs unfamiliar code or algorithms clearly.",
            tags = listOf("Explanation", "Walkthrough", "Learning")
        ),
        QuickPrompt(
            id = "c4",
            title = "Write Comprehensive Unit Tests",
            category = PromptCategory.CODING,
            template = "Write unit tests for the following [Kotlin/Python/TypeScript/Java] function using [JUnit/MockK/pytest/Jest]. Include edge cases, null checks, and boundary conditions:\n```\n[function code]\n```",
            description = "Generates high-coverage unit tests with tricky edge cases.",
            tags = listOf("Testing", "Unit Tests", "Edge Cases")
        ),
        QuickPrompt(
            id = "c5",
            title = "API Endpoint Implementation",
            category = PromptCategory.CODING,
            template = "Create a RESTful API handler in [Ktor / Spring Boot / Express / FastAPI] that handles [feature name, e.g., user profile update with image upload]. Include input validation and error handling.",
            description = "Builds robust backend endpoints with proper status codes.",
            tags = listOf("Backend", "API", "REST")
        ),
        QuickPrompt(
            id = "c6",
            title = "Regex Generator & Tester",
            category = PromptCategory.CODING,
            template = "Write a Regular Expression (RegEx) that matches [pattern requirement, e.g., international phone numbers / ISO-8601 dates]. Provide examples of strings that match and strings that fail.",
            description = "Constructs tested regular expressions without trial-and-error.",
            tags = listOf("Regex", "Pattern", "Validation")
        ),
        QuickPrompt(
            id = "c7",
            title = "SQL Query & Performance Optimizer",
            category = PromptCategory.CODING,
            template = "Given these table schemas:\n[table 1 schema]\n[table 2 schema]\n\nWrite an optimized SQL query to [goal, e.g., fetch top 5 customers by quarterly spend]. Explain any indexing tips.",
            description = "Crafts performant SQL queries and joins.",
            tags = listOf("SQL", "Database", "Performance")
        ),
        QuickPrompt(
            id = "c8",
            title = "Convert Code Between Languages",
            category = PromptCategory.CODING,
            template = "Translate the following [source language] code into idiomatic [target language], utilizing native idioms and standard libraries:\n```\n[source code]\n```",
            description = "Transpiles code idiomatically while preserving exact behavior.",
            tags = listOf("Migration", "Translation", "Idiomatic")
        ),

        // LEARNING & EXPLANATIONS (8 items)
        QuickPrompt(
            id = "l1",
            title = "Explain Like I'm Five (ELI5)",
            category = PromptCategory.LEARNING,
            template = "Explain the concept of \"[complex concept, e.g., Quantum Computing / Zero-Knowledge Proofs]\" using everyday analogies, simple language, and zero technical jargon.",
            description = "Demystifies intricate topics using relatable real-world analogies.",
            tags = listOf("ELI5", "Simplicity", "Concepts")
        ),
        QuickPrompt(
            id = "l2",
            title = "Feynman Technique Deep-Dive",
            category = PromptCategory.LEARNING,
            template = "Let's explore \"[topic]\". Test my understanding by asking me 3 progressively challenging conceptual questions. After each of my answers, assess accuracy and provide constructive feedback.",
            description = "Active recall and Socratic learning method.",
            tags = listOf("Socratic", "Feynman", "Active Learning")
        ),
        QuickPrompt(
            id = "l3",
            title = "30-Day Master Study Plan",
            category = PromptCategory.LEARNING,
            template = "Create a structured 4-week study curriculum to learn \"[skill or subject]\" assuming 1 hour of daily commitment. Include weekly milestones, recommended practice projects, and key resources.",
            description = "Structured roadmap with milestones and hands-on exercises.",
            tags = listOf("Study Plan", "Curriculum", "Roadmap")
        ),
        QuickPrompt(
            id = "l4",
            title = "Compare & Contrast Two Technologies",
            category = PromptCategory.LEARNING,
            template = "Provide a deep comparison between [Option A] and [Option B]. Include a Markdown comparison table with criteria: Performance, Learning Curve, Ecosystem, and Ideal Use Cases.",
            description = "Head-to-head architectural decision matrix.",
            tags = listOf("Comparison", "Tradeoffs", "Evaluation")
        ),
        QuickPrompt(
            id = "l5",
            title = "Mental Model & First Principles",
            category = PromptCategory.LEARNING,
            template = "Break down \"[topic or problem]\" into its absolute first principles. What fundamental truths underpin it, and how can we reason upward from there?",
            description = "Deconstructs problems down to fundamental axioms.",
            tags = listOf("First Principles", "Mental Models", "Philosophy")
        ),
        QuickPrompt(
            id = "l6",
            title = "Interactive Quiz Generator",
            category = PromptCategory.LEARNING,
            template = "Generate a 5-question multiple choice quiz on \"[subject]\". Provide 4 options per question. Hold back the answer key and explanations until I submit my guesses.",
            description = "Interactive test to evaluate comprehension.",
            tags = listOf("Quiz", "Trivia", "Evaluation")
        ),
        QuickPrompt(
            id = "l7",
            title = "Historical Timeline & Context",
            category = PromptCategory.LEARNING,
            template = "Trace the historical evolution of \"[movement / scientific milestone / invention]\" from its origins to present day in chronological order. Highlight key turning points.",
            description = "Contextual history and milestone evolution.",
            tags = listOf("History", "Timeline", "Context")
        ),

        // CREATIVE & BRAINSTORMING (8 items)
        QuickPrompt(
            id = "cr1",
            title = "Sci-Fi / Fantasy Worldbuilding",
            category = PromptCategory.CREATIVE,
            template = "Help me build a unique setting for a story: \"[premise, e.g., a subterranean city powered by geothermal bioluminescence]\". Outline its social hierarchy, unique customs, economy, and one looming conflict.",
            description = "Rich worldbuilding framework for fiction and games.",
            tags = listOf("Worldbuilding", "Sci-Fi", "Fiction")
        ),
        QuickPrompt(
            id = "cr2",
            title = "Unconventional Brainstorming (SCAMPER)",
            category = PromptCategory.CREATIVE,
            template = "Apply the SCAMPER method (Substitute, Combine, Adapt, Modify, Put to another use, Eliminate, Reverse) to generate creative innovations for: \"[product or challenge]\".",
            description = "Proven lateral thinking technique to unblock creative ideation.",
            tags = listOf("Innovation", "SCAMPER", "Ideation")
        ),
        QuickPrompt(
            id = "cr3",
            title = "Character Persona & Dialogue Sheet",
            category = PromptCategory.CREATIVE,
            template = "Create a detailed character profile for \"[character name/archetype]\". Include: Core Motivation, Secret Flaw, Speech Mannerisms, 3 Catchphrases or signature lines, and Backstory.",
            description = "Memorable characters with distinct speech and psychology.",
            tags = listOf("Character", "Storytelling", "Dialogue")
        ),
        QuickPrompt(
            id = "cr4",
            title = "Podcast / Video Script Outline",
            category = PromptCategory.CREATIVE,
            template = "Draft an engaging 10-minute YouTube or Podcast episode script about \"[topic]\". Structure it with an explosive Cold Open hook, Introduction, 3 Core Acts, and Outro Call-To-Action.",
            description = "Script blueprint designed for high audience retention.",
            tags = listOf("Video", "Podcast", "Script")
        ),
        QuickPrompt(
            id = "cr5",
            title = "Metaphor & Imagery Generator",
            category = PromptCategory.CREATIVE,
            template = "Give me 7 poetic metaphors and sensory descriptions to convey the feeling of \"[emotion or scene, e.g., the quiet anticipation before sunrise in a bustling city]\".",
            description = "Evocative sensory language to enrich descriptive prose.",
            tags = listOf("Metaphors", "Poetry", "Sensory")
        ),
        QuickPrompt(
            id = "cr6",
            title = "Plot Twist Generator",
            category = PromptCategory.CREATIVE,
            template = "Given this story setup:\n\"[short premise]\"\n\nGenerate 4 unexpected yet logically foreshadowed plot twists ranging from psychological reveal to paradigm shift.",
            description = "Surprising, satisfying narrative twists for fiction writers.",
            tags = listOf("Twists", "Plot", "Fiction")
        ),

        // IMAGE GENERATION IDEAS (6 items)
        QuickPrompt(
            id = "im1",
            title = "Cyberpunk Neo-Tokyo Street",
            category = PromptCategory.IMAGE,
            template = "Cinematic shot of a rain-slicked futuristic alleyway in Neo-Tokyo, glowing neon holographic signs in mint green and cyan, misty atmosphere, ultra-detailed 8k, volumetric lighting.",
            description = "Atmospheric neon aesthetic with cinematic depth.",
            tags = listOf("Cyberpunk", "Cinematic", "Neon")
        ),
        QuickPrompt(
            id = "im2",
            title = "Isometric 3D Smart City",
            category = PromptCategory.IMAGE,
            template = "Vibrant isometric 3D render of an eco-friendly smart city, solar panel roofs, elevated maglev trains, lush botanical terraces, soft daylight, clay render aesthetic.",
            description = "Cute, crisp isometric miniature 3D model.",
            tags = listOf("3D", "Isometric", "Futuristic")
        ),
        QuickPrompt(
            id = "im3",
            title = "Studio Product Photography",
            category = PromptCategory.IMAGE,
            template = "High-end commercial studio product photography of a sleek minimalist [object, e.g., matte ceramic teapot] on a polished black stone pedestal, rim lighting, soft diffuse shadow, editorial quality.",
            description = "Ultra-clean luxury commercial product lighting.",
            tags = listOf("Product", "Studio", "Photography")
        ),
        QuickPrompt(
            id = "im4",
            title = "Studio Ghibli Inspired Anime Landscape",
            category = PromptCategory.IMAGE,
            template = "Hand-painted anime background in the style of Studio Ghibli, rolling emerald meadows under a sweeping summer sky with dramatic fluffy cumulus clouds, tranquil cottage in the distance, watercolor textures.",
            description = "Peaceful, nostalgic watercolor aesthetic.",
            tags = listOf("Anime", "Watercolor", "Landscape")
        ),
        QuickPrompt(
            id = "im5",
            title = "Futuristic Glassmorphic Interface HUD",
            category = PromptCategory.IMAGE,
            template = "Sleek holographic UI dashboard floating in mid-air, translucent frosted glass cards, glowing mint green telemetry data rings, elegant dark background, modern tech aesthetic.",
            description = "High-tech futuristic visual HUD interface.",
            tags = listOf("HUD", "Sci-Fi", "UI")
        )
    )
}
