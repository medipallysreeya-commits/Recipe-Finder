import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RecipeFinder {

    // CO2 - Pattern Search Implementation

    // Builds the LPS array used internally for pattern searching
    static int[] buildLPS(String pattern) {

        int[] lps = new int[pattern.length()];

        int length = 0;
        int i = 1;

        while (i < pattern.length()) {

            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
                lps[i] = length;
                i++;
            }
            else {

                if (length != 0) {
                    length = lps[length - 1];
                }
                else {
                    lps[i] = 0;
                    i++;
                }
            }
        }

        return lps;
    }


    // CO2 - Searches for a pattern inside a text
    static boolean patternSearch(String text, String pattern) {

        text = text.toLowerCase();
        pattern = pattern.toLowerCase();

        if (pattern.isEmpty())
            return true;

        if (pattern.length() > text.length())
            return false;

        int[] lps = buildLPS(pattern);

        int i = 0;
        int j = 0;

        while (i < text.length()) {

            if (text.charAt(i) == pattern.charAt(j)) {

                i++;
                j++;

                if (j == pattern.length()) {
                    return true;
                }
            }
            else {

                if (j != 0) {
                    j = lps[j - 1];
                }
                else {
                    i++;
                }
            }
        }

        return false;
    }


    // Reads a recipe corpus file
    static String readFile(Path file) throws IOException {
        return Files.readString(file);
    }


    // Splits the corpus into individual recipes
    static List<String> getRecipes(String text) {

        List<String> recipes = new ArrayList<>();

        String[] parts = text.split("(?=RECIPE:)");

        for (String part : parts) {

            if (part.trim().startsWith("RECIPE:")) {
                recipes.add(part.trim());
            }
        }

        return recipes;
    }


    // Gets recipe name
    static String getRecipeName(String recipe) {

        String[] lines = recipe.split("\\R");

        for (String line : lines) {

            if (line.startsWith("RECIPE:")) {
                return line.substring(7).trim();
            }
        }

        return "Unknown Recipe";
    }


    // Gets ingredients
    static String getIngredients(String recipe) {

        String[] lines = recipe.split("\\R");

        boolean found = false;
        StringBuilder result = new StringBuilder();

        for (String line : lines) {

            if (line.trim().equals("INGREDIENTS:")) {
                found = true;
                continue;
            }

            if (line.trim().equals("INSTRUCTIONS:")) {
                break;
            }

            if (found && !line.trim().isEmpty()) {
                result.append(line.trim());
            }
        }

        return result.toString();
    }


    // Gets cooking instructions
    static String getInstructions(String recipe) {

        String[] lines = recipe.split("\\R");

        boolean found = false;
        StringBuilder result = new StringBuilder();

        for (String line : lines) {

            if (line.trim().equals("INSTRUCTIONS:")) {
                found = true;
                continue;
            }

            if (found && !line.trim().isEmpty()) {
                result.append(line.trim()).append(" ");
            }
        }

        return result.toString().trim();
    }


    // Searches the complete recipe corpus
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

                    // Pattern search is performed on
                    // the complete recipe content.
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


    // Displays the recipe search results
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


    // Displays complete recipe
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


    public static void main(String[] args)
            throws Exception {

        Scanner scanner =
                new Scanner(System.in);

        String corpusFolder = "corpus";

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