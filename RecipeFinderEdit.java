package src;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RecipeFinderEdit {


// ============================================================
// EDIT DISTANCE
// ============================================================

/*
 * Calculates the Edit Distance between two strings.
 *
 * Allowed operations:
 * 1. Insertion
 * 2. Deletion
 * 3. Substitution
 */
static int editDistance(
        String str1,
        String str2) {

    str1 = str1.toLowerCase();
    str2 = str2.toLowerCase();

    int m = str1.length();
    int n = str2.length();

    int[][] dp =
            new int[m + 1][n + 1];

    // Convert empty string to str2
    for (int j = 0; j <= n; j++) {
        dp[0][j] = j;
    }

    // Convert str1 to empty string
    for (int i = 0; i <= m; i++) {
        dp[i][0] = i;
    }

    // Fill the DP table
    for (int i = 1; i <= m; i++) {

        for (int j = 1; j <= n; j++) {

            if (str1.charAt(i - 1) ==
                    str2.charAt(j - 1)) {

                dp[i][j] =
                        dp[i - 1][j - 1];

            } else {

                int insert =
                        dp[i][j - 1];

                int delete =
                        dp[i - 1][j];

                int substitute =
                        dp[i - 1][j - 1];

                dp[i][j] =
                        1 + Math.min(
                                insert,
                                Math.min(
                                        delete,
                                        substitute
                                )
                        );
            }
        }
    }

    return dp[m][n];
}


// ============================================================
// FILE HANDLING
// ============================================================

static String readFile(Path file)
        throws IOException {

    return Files.readString(file);
}


// Splits corpus into individual recipes
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
// SEARCH RECIPES USING EDIT DISTANCE
// ============================================================

/*
 * Searches recipe ingredients using Edit Distance.
 *
 * A small edit distance means the word is similar
 * to the user's search word.
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

                String ingredients =
                        getIngredients(recipe);

                String[] words =
                        ingredients
                                .toLowerCase()
                                .split("[^a-zA-Z]+");

                for (String word : words) {

                    if (word.isEmpty()) {
                        continue;
                    }

                    int distance =
                            editDistance(
                                    searchText,
                                    word);

                    /*
                     * Allow a maximum edit distance
                     * based on the search word length.
                     */
                    int allowedDistance;

                    if (searchText.length() <= 4) {
                        allowedDistance = 1;
                    } else {
                        allowedDistance = 2;
                    }

                    if (distance <=
                            allowedDistance) {

                        matchingRecipes.add(recipe);
                        break;
                    }
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
                "\nNo similar recipes found.");

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
            "\n====== RECIPE FINDER - EDIT DISTANCE ======");

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
                "\nSearching recipes using Edit Distance...");

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
