package src;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RecipeFinderZ {

// ============================================================
// Z ALGORITHM
// ============================================================

/*
 * Builds the Z array.
 *
 * Z[i] = length of the longest substring starting at i
 * that matches the prefix of the given string.
 */
static int[] buildZArray(String str) {

    int n = str.length();

    int[] z = new int[n];

    int left = 0;
    int right = 0;

    for (int i = 1; i < n; i++) {

        if (i <= right) {
            z[i] = Math.min(
                    right - i + 1,
                    z[i - left]
            );
        }

        while (i + z[i] < n &&
                str.charAt(z[i]) ==
                str.charAt(i + z[i])) {

            z[i]++;
        }

        if (i + z[i] - 1 > right) {

            left = i;
            right = i + z[i] - 1;
        }
    }

    return z;
}


// ============================================================
// Z ALGORITHM PATTERN SEARCH
// ============================================================

/*
 * Searches for a pattern inside the text
 * using the Z Algorithm.
 */
static boolean patternSearch(
        String text,
        String pattern) {

    text = text.toLowerCase();
    pattern = pattern.toLowerCase();

    if (pattern.isEmpty()) {
        return true;
    }

    if (pattern.length() > text.length()) {
        return false;
    }

    /*
     * Create:
     *
     * Pattern + separator + text
     */
    String combined =
            pattern + "$" + text;

    int[] z =
            buildZArray(combined);

    /*
     * Start checking after the pattern
     * and separator.
     */
    int patternStart =
            pattern.length() + 1;

    for (int i = patternStart;
         i < combined.length();
         i++) {

        if (z[i] == pattern.length()) {
            return true;
        }
    }

    return false;
}


// ============================================================
// FILE HANDLING
// ============================================================

static String readFile(Path file)
        throws IOException {

    return Files.readString(file);
}


// Splits the corpus into individual recipes
static List<String> getRecipes(
        String text) {

    List<String> recipes =
            new ArrayList<>();

    String[] parts =
            text.split("(?=RECIPE:)");

    for (String part : parts) {

        if (part.trim().startsWith("RECIPE:")) {
            recipes.add(part.trim());
        }
    }

    return recipes;
}


// ============================================================
// RECIPE INFORMATION
// ============================================================

// Gets recipe name
static String getRecipeName(
        String recipe) {

    String[] lines =
            recipe.split("\\R");

    for (String line : lines) {

        if (line.startsWith("RECIPE:")) {

            return line
                    .substring(7)
                    .trim();
        }
    }

    return "Unknown Recipe";
}


// Gets ingredients
static String getIngredients(
        String recipe) {

    String[] lines =
            recipe.split("\\R");

    boolean found = false;

    StringBuilder result =
            new StringBuilder();

    for (String line : lines) {

        if (line.trim().equals(
                "INGREDIENTS:")) {

            found = true;
            continue;
        }

        if (line.trim().equals(
                "INSTRUCTIONS:")) {

            break;
        }

        if (found &&
                !line.trim().isEmpty()) {

            result.append(line.trim());
        }
    }

    return result.toString();
}


// Gets cooking instructions
static String getInstructions(
        String recipe) {

    String[] lines =
            recipe.split("\\R");

    boolean found = false;

    StringBuilder result =
            new StringBuilder();

    for (String line : lines) {

        if (line.trim().equals(
                "INSTRUCTIONS:")) {

            found = true;
            continue;
        }

        if (found &&
                !line.trim().isEmpty()) {

            result.append(line.trim())
                  .append(" ");
        }
    }

    return result.toString().trim();
}


// ============================================================
// SEARCH RECIPES
// ============================================================

/*
 * Searches the recipe corpus
 * using the Z Algorithm.
 */
static List<String> searchRecipes(
        String folder,
        String searchText)
        throws IOException {

    List<String> matchingRecipes =
            new ArrayList<>();

    Path corpus =
            Paths.get(folder);

    if (!Files.exists(corpus)) {
        return matchingRecipes;
    }

    try (DirectoryStream<Path> files =
                 Files.newDirectoryStream(
                         corpus,
                         "*.txt")) {

        for (Path file : files) {

            String text =
                    readFile(file);

            List<String> recipes =
                    getRecipes(text);

            for (String recipe : recipes) {

                if (patternSearch(
                        recipe,
                        searchText)) {

                    matchingRecipes.add(recipe);
                }
            }
        }
    }

    return matchingRecipes;
}


// ============================================================
// DISPLAY RESULTS
// ============================================================

static void displayResults(
        List<String> recipes) {

    if (recipes.isEmpty()) {

        System.out.println(
                "\nNo matching recipes found.");

        System.out.println(
                "Try another ingredient or keyword.");

        return;
    }

    System.out.println(
            "\nMatching Recipes");

    System.out.println(
            "------------------------------");

    int number = 1;

    for (String recipe : recipes) {

        System.out.println(
                "\n" + number + ". "
                + getRecipeName(recipe));

        System.out.println(
                "   Ingredients: "
                + getIngredients(recipe));

        number++;
    }

    System.out.println(
            "\nEnter the recipe number to view "
            + "complete instructions.");
}


// ============================================================
// DISPLAY COMPLETE RECIPE
// ============================================================

static void displayRecipe(
        String recipe) {

    System.out.println(
            "\n======================================");

    System.out.println(
            getRecipeName(recipe));

    System.out.println(
            "======================================");

    System.out.println(
            "\nIngredients:");

    System.out.println(
            getIngredients(recipe));

    System.out.println(
            "\nInstructions:");

    System.out.println(
            getInstructions(recipe));

    System.out.println(
            "\n======================================");
}


// ============================================================
// MAIN
// ============================================================

public static void main(String[] args)
        throws Exception {

    Scanner scanner =
            new Scanner(System.in);

    String corpusFolder =
            "corpus";

    System.out.println(
            "\n========== RECIPE FINDER ==========");

    while (true) {

        System.out.println(
                "\n1. Find Recipes");

        System.out.println(
                "2. Exit");

        System.out.print(
                "\nChoose an option: ");

        int choice =
                scanner.nextInt();

        scanner.nextLine();

        if (choice == 2) {

            System.out.println(
                    "\nThank you for using Recipe Finder!");

            break;
        }

        if (choice != 1) {

            System.out.println(
                    "\nInvalid option.");

            continue;
        }

        System.out.print(
                "\nEnter an ingredient or keyword: ");

        String searchText =
                scanner.nextLine().trim();

        if (searchText.isEmpty()) {

            System.out.println(
                    "\nPlease enter something to search.");

            continue;
        }

        System.out.println(
                "\nSearching recipes...");

        List<String> results =
                searchRecipes(
                        corpusFolder,
                        searchText);

        displayResults(results);

        if (!results.isEmpty()) {

            System.out.print(
                    "\nEnter recipe number "
                    + "(0 to return): ");

            int recipeNumber =
                    scanner.nextInt();

            scanner.nextLine();

            if (recipeNumber > 0 &&
                    recipeNumber <= results.size()) {

                displayRecipe(
                        results.get(
                                recipeNumber - 1));
            }
        }
    }

    scanner.close();
}

}
