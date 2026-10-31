package com.example.keyboard.model

data class ExtensionItem(
    val label: String,
    val preview: String,
    val textToInsert: String,
    val category: String = "General"
)

data class ExtensionPack(
    val id: String,
    val name: String,
    val description: String,
    val author: String = "Minima Team",
    val version: String = "1.0",
    val icon: String = "⚡",
    val isInstalled: Boolean = true,
    val isEnabled: Boolean = true,
    val isBuiltIn: Boolean = true,
    val items: List<ExtensionItem>
) {
    companion object {
        val KaomojiPack = ExtensionPack(
            id = "pack_kaomoji",
            name = "Kaomoji & ASCII Expressions",
            description = "Expressive Japanese emoticons and classic text reaction faces",
            author = "Minima Community",
            icon = "ʕ•ᴥ•ʔ",
            items = listOf(
                ExtensionItem("Happy", "(づ｡◕‿‿◕｡)づ", "(づ｡◕‿‿◕｡)づ", "Cute"),
                ExtensionItem("Shrug", "¯\\_(ツ)_/¯", "¯\\_(ツ)_/¯", "Classic"),
                ExtensionItem("Table Flip", "(╯°□°)╯︵ ┻━┻", "(╯°□°)╯︵ ┻━┻", "Action"),
                ExtensionItem("Unflip", "┬─┬ノ( º _ ºノ)", "┬─┬ノ( º _ ºノ)", "Action"),
                ExtensionItem("Deal With It", "(•_•) ( •_•)>⌐■-■ (⌐■_■)", "(•_•) ( •_•)>⌐■-■ (⌐■_■)", "Cool"),
                ExtensionItem("Bear", "ʕ•ᴥ•ʔ", "ʕ•ᴥ•ʔ", "Animals"),
                ExtensionItem("Disapproval", "ಠ_ಠ", "ಠ_ಠ", "Classic"),
                ExtensionItem("Fighting", "(ง •̀_•́)ง", "(ง •̀_•́)ง", "Excited"),
                ExtensionItem("Sparkle", "(◕‿◕✿)", "(◕‿◕✿)", "Cute"),
                ExtensionItem("Point", "(☞ﾟヮﾟ)☞", "(☞ﾟヮﾟ)☞", "Cool"),
                ExtensionItem("Confused", "(・_・ヾ", "(・_・ヾ", "Confused"),
                ExtensionItem("Cat Face", "(=^･ω･^=)", "(=^･ω･^=)", "Animals"),
                ExtensionItem("Dancing", "♪ヽ(･ˇ∀ˇ･ゞ)", "♪ヽ(･ˇ∀ˇ･ゞ)", "Fun"),
                ExtensionItem("Salute", "(｀_´)ゞ", "(｀_´)ゞ", "Respect"),
                ExtensionItem("Love Eyes", "(♥ω♥*)", "(♥ω♥*)", "Love"),
                ExtensionItem("Sneaky", "(¬‿¬)", "(¬‿¬)", "Playful")
            )
        )

        val MarkdownDevPack = ExtensionPack(
            id = "pack_dev_markdown",
            name = "Dev & Markdown Snippets",
            description = "Quick code blocks, links, bullets, and syntax boilerplate for writers and devs",
            author = "Minima Devs",
            icon = "</>",
            items = listOf(
                ExtensionItem("Code Block", "```lang\n```", "```\n\n```", "Markdown"),
                ExtensionItem("Inline Code", "`code`", "`code`", "Markdown"),
                ExtensionItem("Markdown Link", "[text](url)", "[link](https://)", "Markdown"),
                ExtensionItem("Bold", "**text**", "****", "Markdown"),
                ExtensionItem("Italic", "*text*", "**", "Markdown"),
                ExtensionItem("Task Item", "- [ ] todo", "- [ ] ", "Markdown"),
                ExtensionItem("Blockquote", "> quote", "> ", "Markdown"),
                ExtensionItem("Console Log", "console.log()", "console.log()", "Code"),
                ExtensionItem("Git Commit", "git commit -m", "git commit -m \"\"", "Dev"),
                ExtensionItem("Arrow Func", "() => {}", "() => {\n\n}", "Code"),
                ExtensionItem("Async/Await", "async/await", "async () => {\n  await \n}", "Code"),
                ExtensionItem("TODO Comment", "// TODO: ", "// TODO: ", "Dev"),
                ExtensionItem("SQL Select", "SELECT * FROM", "SELECT * FROM ", "Database"),
                ExtensionItem("HTML Tag", "<tag></tag>", "<div></div>", "Web")
            )
        )

        val MathSymbolsPack = ExtensionPack(
            id = "pack_math_symbols",
            name = "Math & Scientific Notation",
            description = "Greek letters, calculus symbols, and logic operators",
            author = "Science & Logic",
            icon = "π",
            items = listOf(
                ExtensionItem("Pi", "π", "π", "Greek"),
                ExtensionItem("Infinity", "∞", "∞", "Calculus"),
                ExtensionItem("Sqrt", "√", "√", "Algebra"),
                ExtensionItem("Sum", "∑", "∑", "Calculus"),
                ExtensionItem("Integral", "∫", "∫", "Calculus"),
                ExtensionItem("Approx", "≈", "≈", "Relation"),
                ExtensionItem("Not Equal", "≠", "≠", "Relation"),
                ExtensionItem("Less/Equal", "≤", "≤", "Relation"),
                ExtensionItem("Greater/Equal", "≥", "≥", "Relation"),
                ExtensionItem("Plus/Minus", "±", "±", "Operators"),
                ExtensionItem("Degree", "°", "°", "Units"),
                ExtensionItem("Delta", "∆", "∆", "Greek"),
                ExtensionItem("Theta", "θ", "θ", "Greek"),
                ExtensionItem("Lambda", "λ", "λ", "Greek"),
                ExtensionItem("Partial", "∂", "∂", "Calculus"),
                ExtensionItem("Divide", "÷", "÷", "Operators"),
                ExtensionItem("Multiply", "×", "×", "Operators"),
                ExtensionItem("Micro", "µ", "µ", "Units"),
                ExtensionItem("Permille", "‰", "‰", "Units"),
                ExtensionItem("One Half", "½", "½", "Fractions"),
                ExtensionItem("One Quarter", "¼", "¼", "Fractions")
            )
        )

        val FancyFontsPack = ExtensionPack(
            id = "pack_fancy_fonts",
            name = "Fancy Unicode Stylizer",
            description = "Gothic, script, double-struck, and circled typography transformers",
            author = "Typography Lab",
            icon = "𝔉",
            items = listOf(
                ExtensionItem("Fraktur Love", "𝔏𝔬𝔳𝔢", "𝔏𝔬𝔳𝔢", "Fraktur"),
                ExtensionItem("Script Vibe", "𝓥𝓲𝓫𝓮", "𝓥𝓲𝓫𝓮", "Script"),
                ExtensionItem("Double Struck", "ℂ𝕠𝕠𝕝", "ℂ𝕠𝕠𝕝", "Blackboard"),
                ExtensionItem("Circled", "Ⓦⓞⓡⓓ", "Ⓦⓞⓡⓓ", "Circled"),
                ExtensionItem("Small Caps", "ᴍɪɴɪᴍᴀ", "ᴍɪɴɪᴍᴀ", "Caps"),
                ExtensionItem("Monospace", "𝚖𝚘𝚗𝚘", "𝚖𝚘𝚗𝚘", "Mono"),
                ExtensionItem("Bold Serif", "𝐁𝐨𝐥𝐝", "𝐁𝐨𝐥𝐝", "Serif"),
                ExtensionItem("Squared", "🅂🅀🅄🄰🅁🄴", "🅂🅀🅄🄰🅁🄴", "Squares")
            )
        )

        val BuiltInPacks = listOf(
            KaomojiPack,
            MarkdownDevPack,
            MathSymbolsPack,
            FancyFontsPack
        )
    }
}
